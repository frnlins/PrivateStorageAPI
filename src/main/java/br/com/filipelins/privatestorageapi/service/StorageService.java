package br.com.filipelins.privatestorageapi.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;

@Service
public class StorageService {

	@Autowired
	private MinioClient minioStorage;

	public ReturnMessage<BucketTO> listBuckets() {
		ReturnMessage<BucketTO> retorno = null;
		List<BucketTO> bucketTOList = Collections.emptyList();

		try {
			List<Bucket> bucketList = minioStorage.listBuckets();

			if (!bucketList.isEmpty()) {
				bucketTOList = bucketList.stream().map(bucket -> new BucketTO(bucket)).collect(Collectors.toList());
				retorno = new ReturnMessage<BucketTO>("Buckets encontrados com sucesso", HttpStatus.OK, bucketTOList);
			} else {
				retorno = new ReturnMessage<BucketTO>("Não há buckets!", HttpStatus.OK);
			}

		} catch (Exception e) {
			retorno = new ReturnMessage<BucketTO>("Erro ao listar os Buckets", HttpStatus.INTERNAL_SERVER_ERROR,
					e.getMessage());
		}

		return retorno;
	}

	public ReturnMessage<BucketTO> createBucket(String bucketName) {
		ReturnMessage<BucketTO> retorno;
		try {
			if (!bucketName.isBlank()) {
				if (!isBucketExists(bucketName)) {
					minioStorage.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
					retorno = new ReturnMessage<BucketTO>("Bucket: '" + bucketName + "', criado com sucesso",
							HttpStatus.OK);
				} else {
					retorno = new ReturnMessage<BucketTO>("Erro: O bucket, '" + bucketName + "', já existe");
				}
			} else {
				retorno = new ReturnMessage<BucketTO>("Erro: O nome do bucket é nulo ou vazio");
			}
		} catch (Exception e) {
			retorno = new ReturnMessage<BucketTO>("Erro ao criar o bucket: '" + bucketName + "' " + e);
		}

		return retorno;
	}

	public ReturnMessage<ObjectTO> listBucketObjects(String bucketName) {

		ReturnMessage<ObjectTO> retorno = null;
		List<ObjectTO> objectTOList;

		try {
			if (isBucketExists(bucketName)) {
				objectTOList = getBucketObjects(bucketName);

				if (objectTOList.isEmpty()) {
					retorno = new ReturnMessage<ObjectTO>("Não há objetos no bucket: '" + bucketName + "'",
							HttpStatus.OK);
				} else {
					retorno = new ReturnMessage<ObjectTO>("Objetos encontados no bucket: '" + bucketName + "'",
							HttpStatus.OK, objectTOList);
				}
			} else {
				retorno = new ReturnMessage<ObjectTO>("O bucket: '" + bucketName + "' não existe");
			}
		} catch (Exception e) {
			retorno = new ReturnMessage<ObjectTO>("Erro ao recuperar os objetos do bucket: " + bucketName,
					HttpStatus.BAD_REQUEST, e.getMessage());
		}

		return retorno;
	}

	public ReturnMessage<BucketTO> deleteBucket(String bucketName) {
		ReturnMessage<BucketTO> retorno = null;
		String objectsDeleteResult = "";

		try {
			if (isBucketExists(bucketName)) {

				List<ObjectTO> bucketObjects = getBucketObjects(bucketName);

				if (!bucketObjects.isEmpty()) {
					objectsDeleteResult = deleteObjectsLazy(bucketName, bucketObjects);
				}

				minioStorage.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
				retorno = new ReturnMessage<BucketTO>(
						"O bucket: '" + bucketName + "' e todos so seus objetos foram deletados com sucesso!",
						HttpStatus.OK);
			} else {
				retorno = new ReturnMessage<BucketTO>("Erro: o bucket '" + bucketName + "' não existe!");
			}
		} catch (Exception e) {
			retorno = new ReturnMessage<BucketTO>(
					"Erro ao deletar o bucket: '" + bucketName + "'\n" + objectsDeleteResult, HttpStatus.BAD_REQUEST,
					e.getMessage());
		}

		return retorno;
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

	private boolean isBucketExists(String bucketName) throws Exception {
		return !bucketName.isBlank()
				&& minioStorage.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
	}

	private void deleteObject(String bucketName, ObjectTO bucketObject) throws Exception {
		minioStorage.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(bucketObject.getNome()).build());
	}

	private String deleteObjectsLazy(String bucketName, List<ObjectTO> bucketObjects) throws Exception {
		StringBuffer sb = new StringBuffer();

		List<DeleteObject> deletedObjects = bucketObjects.stream().map(obj -> new DeleteObject(obj.getNome()))
				.collect(Collectors.toList());

		Iterable<Result<DeleteError>> results = minioStorage
				.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(deletedObjects).build());

		for (Result<DeleteError> result : results) {
			DeleteError err = result.get();
			sb.append("Erro ao deletar o objeto: " + err.objectName() + "\n");
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
}