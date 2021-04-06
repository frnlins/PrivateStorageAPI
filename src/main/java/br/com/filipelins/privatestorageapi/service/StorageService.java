package br.com.filipelins.privatestorageapi.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.filipelins.privatestorageapi.domain.BucketTO;
import br.com.filipelins.privatestorageapi.domain.ObjectTO;
import br.com.filipelins.privatestorageapi.domain.ReturnMessage;
import br.com.filipelins.privatestorageapi.service.exception.PrivateStorageException;
import io.minio.BucketExistsArgs;
import io.minio.DownloadObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

@Service
public class StorageService {

	@Autowired
	private MinioClient minioStorage;

	public List<BucketTO> listBuckets() {
		List<Bucket> bucketList;

		try {
			bucketList = minioStorage.listBuckets();
		} catch (Exception e) {
			throw new PrivateStorageException("Erro ao listar os buckets", e);
		}

		return bucketList.stream().map(bucket -> new BucketTO(bucket)).collect(Collectors.toList());
	}

	public void createBucket(String bucketName) {
		try {
			minioStorage.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
		} catch (Exception e) {
			throw new PrivateStorageException("Não foi possível criar o bucket '" + bucketName + "'", e);
		}
	}

	public List<ObjectTO> listBucketObjects(String bucketName) {
		List<ObjectTO> objectTOList = new ArrayList<ObjectTO>();

		Iterable<Result<Item>> bucketObjects = minioStorage
				.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());

		Item objectItem = null;
		Iterator<Result<Item>> it = bucketObjects.iterator();
		while (it.hasNext()) {
			objectItem = getResultObject(it.next());
			objectTOList.add(new ObjectTO(objectItem, bucketName));
		}

		return objectTOList;
	}

	public void deleteBucket(String bucketName) {
		List<ObjectTO> bucketObjects = listBucketObjects(bucketName);
		String deleteObjectsResult;
		
		if (!bucketObjects.isEmpty()) {
			try {
				deleteObjectsResult = deleteObjects(bucketName, bucketObjects);
				if (!deleteObjectsResult.isEmpty()) {
					throw new Exception(deleteObjectsResult);
				}
			} catch (Exception e) {
				throw new PrivateStorageException(e.getMessage());
			}
		}
		
		try {
			minioStorage.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
		} catch (Exception e) {
			throw new PrivateStorageException("Erro ao deletar o bucket", e);
		}
	}

	public ByteArrayResource downloadObjetc(String bucketName, String objectName) {
		ByteArrayResource bar = null;
		try (GetObjectResponse getObjectResponse = minioStorage
				.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {
			bar = new ByteArrayResource(getObjectResponse.readAllBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bar;
	}

	public ByteArrayResource downloadObjetcLocal(String bucketName, String objectName) {
		ByteArrayResource bar = null;
		try (GetObjectResponse getObjectResponse = minioStorage
				.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {
			FileSystem fs = FileSystems.getDefault();
			Path dirPath = Path.of(".." + fs.getSeparator(), "downloaded_objects");
			Path filePath = dirPath.resolve(getObjectResponse.object());

			if (!Files.isDirectory(dirPath)) {
				Files.createDirectory(dirPath);
			}
			try (OutputStream out = Files.newOutputStream(filePath)) {
				out.write(getObjectResponse.readAllBytes());

				try (InputStream in = Files.newInputStream(filePath)) {
					bar = new ByteArrayResource(in.readAllBytes());
				}
			}

		} catch (Exception e) {
			System.out.println("Erro ao fazer download do arquivo: " + objectName);
		}

		return bar;
	}

	public void downloadObjetcLocalFileSystem(String bucketName, String objectName) {
		FileSystem fs = FileSystems.getDefault();
		Path dirPath = Path.of(".." + fs.getSeparator(), "downloaded_objects");
		Path filePath = dirPath.resolve(objectName);

		try {
			minioStorage.downloadObject(DownloadObjectArgs.builder().bucket(bucketName).object(objectName)
					.filename(filePath.toString()).build());
		} catch (Exception e) {
			System.out.println("Erro ao fazer download do arquivo localmente: " + e.getMessage());
		}
	}

	public ReturnMessage<ObjectTO> putObject(String bucketName, MultipartFile multipartFile) {
		ReturnMessage<ObjectTO> retorno = null;

		try {
			InputStream is = multipartFile.getInputStream();

			minioStorage.putObject(PutObjectArgs.builder().bucket(bucketName)
					.object(multipartFile.getOriginalFilename()).contentType(multipartFile.getContentType())
					.stream(is, multipartFile.getSize(), -1).build());

			retorno = new ReturnMessage<ObjectTO>("O upload do objeto: '" + multipartFile.getOriginalFilename()
					+ "'foi realizado com sucesso para o bucket: '" + bucketName + "'", HttpStatus.OK);
		} catch (Exception e) {
			retorno = new ReturnMessage<ObjectTO>(
					"Erro ao fazer upload do objeto: " + multipartFile.getOriginalFilename(), HttpStatus.BAD_REQUEST,
					e.getMessage());
		}

		return retorno;
	}

	private void deleteObject(String bucketName, ObjectTO bucketObject) throws Exception {
		minioStorage.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(bucketObject.getNome()).build());
	}

	private String deleteObjects(String bucketName, List<ObjectTO> objects) {
		StringBuffer sb = new StringBuffer();

		List<DeleteObject> objectsToDelete = objects.stream().map(obj -> new DeleteObject(obj.getNome()))
				.collect(Collectors.toList());

		Iterable<Result<DeleteError>> results = minioStorage
				.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objectsToDelete).build());
		
		Iterator<Result<DeleteError>> it = results.iterator();
		if(it.hasNext()) {
			sb.append("Erro ao deletar objetos do bucket: \n");
			DeleteError err = null;
			while(it.hasNext()) {
				err = getResultObject(it.next());
				sb.append("objeto: ").append(err.objectName());
				sb.append("\t mensagem: ").append(err.message()).append("\n");
			}
		}

		return sb.toString();
	}

	private List<ObjectTO> getBucketObjects(String bucketName) throws Exception {
		List<ObjectTO> retorno = new ArrayList<ObjectTO>();
		Iterable<Result<Item>> bucketobjects = minioStorage
				.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());

		for (Result<Item> result : bucketobjects) {
			retorno.add(new ObjectTO(result.get(), bucketName));
		}

		return retorno;
	}

	private <T> T getResultObject(Result<T> result) {
		T retorno = null;
		try {
			retorno = result.get();
		} catch (Exception e) {
			throw new PrivateStorageException("Erro ao acessar um resultado de operação realizada no object storage", e);
		}
		return retorno;
	}
}