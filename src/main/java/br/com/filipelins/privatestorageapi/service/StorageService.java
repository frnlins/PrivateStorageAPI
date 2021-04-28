package br.com.filipelins.privatestorageapi.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.filipelins.privatestorageapi.domain.BucketTO;
import br.com.filipelins.privatestorageapi.domain.ConfigPresignedURL;
import br.com.filipelins.privatestorageapi.domain.ExtendedObjectTO;
import br.com.filipelins.privatestorageapi.domain.ObjectTO;
import br.com.filipelins.privatestorageapi.service.exception.PrivateStorageException;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
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

	public List<ObjectTO> listBucketObjects(String bucketName, String folder) {
		return listBucketObjects(bucketName, folder, false);
	}
	
	public List<ObjectTO> listBucketObjects(String bucketName) {
		return listBucketObjects(bucketName, "", true);
	}

	public void deleteBucket(String bucketName) {
		List<ObjectTO> bucketObjects = listBucketObjects(bucketName, "", true);

		if (!bucketObjects.isEmpty()) {
			deleteObjects(bucketName, bucketObjects);
		}

		try {
			minioStorage.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
		} catch (Exception e) {
			throw new PrivateStorageException("Erro ao deletar o bucket", e);
		}
	}

	public void putObject(String bucketName, String folder, MultipartFile[] multipartFiles) {
		folder = getFolderName(folder);
		for (MultipartFile multipartFile : multipartFiles) {
			try (InputStream is = multipartFile.getInputStream()) {
				minioStorage.putObject(PutObjectArgs.builder().bucket(bucketName)
						.object(folder + multipartFile.getOriginalFilename())
						.contentType(multipartFile.getContentType()).stream(is, multipartFile.getSize(), -1).build());
			} catch (Exception e) {
				throw new PrivateStorageException(
						"Erro ao fazer upload do objeto: " + multipartFile.getOriginalFilename(), e);
			}
		}
	}

	public void deleteObject(String bucketName, ObjectTO[] listObjectTO) {
		switch (listObjectTO.length) {
		case 0:
			throw new PrivateStorageException("Não foram passados objetos para serem deletados");
		case 1:
			deleteObject(bucketName, listObjectTO[0]);
			break;
		default:
			deleteObjects(bucketName, Arrays.asList(listObjectTO));
		}
	}

	public ExtendedObjectTO objectInfo(String bucketName, String objectName, String folder) {
		ExtendedObjectTO objectTO = null;
		folder = getFolderName(folder);
		try {
			var response = minioStorage
					.statObject(StatObjectArgs.builder().bucket(bucketName).object(folder + objectName).build());
			objectTO = new ExtendedObjectTO(response);
		} catch (Exception e) {
			throw new PrivateStorageException("Erro ao recuperar informações do objeto", e);
		}
		return objectTO;
	}

	public ByteArrayResource getObject(String bucketName, String objectName, String folder) {
		folder = getFolderName(folder);
		ByteArrayResource bar = null;
		try (GetObjectResponse getObjectResponse = minioStorage
				.getObject(GetObjectArgs.builder().bucket(bucketName).object(folder + objectName).build())) {
			bar = new ByteArrayResource(getObjectResponse.readAllBytes());
		} catch (Exception e) {
			throw new PrivateStorageException("Erro ao recuperar o objeto do storage", e);
		}

		return bar;
	}

	public String presignedURL(@Valid ConfigPresignedURL config) {
		try {
			return minioStorage.getPresignedObjectUrl(
					GetPresignedObjectUrlArgs.builder().bucket(config.getBucketName()).object(config.getObjectName())
							.expiry(config.getExpiryTime(), TimeUnit.MINUTES).method(config.getMethod()).build());

		} catch (Exception e) {
			throw new PrivateStorageException("Não foi possível obter uma url pré-assinada", e);
		}
	}

	private List<ObjectTO> listBucketObjects(String bucketName, String folder, boolean recursive) {
		folder = getFolderName(folder);
		List<ObjectTO> objectTOList = new ArrayList<ObjectTO>();
		Iterable<Result<Item>> bucketObjects = minioStorage
				.listObjects(ListObjectsArgs.builder().bucket(bucketName).prefix(folder).recursive(recursive).build());

		Item objectItem = null;
		Iterator<Result<Item>> it = bucketObjects.iterator();
		while (it.hasNext()) {
			objectItem = getResultObject(it.next());
			objectTOList.add(new ObjectTO(objectItem));
		}

		return objectTOList;
	}

	private void deleteObject(String bucketName, ObjectTO objectTO) {
		try {
			minioStorage.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectTO.getNome()).build());
		} catch (Exception e) {
			throw new PrivateStorageException("Erro ao deletar o objeto do bucket", e);
		}
	}

	private void deleteObjects(String bucketName, List<ObjectTO> objects) {
		StringBuffer sb = new StringBuffer();

		List<DeleteObject> objectsToDelete = objects.stream().map(obj -> new DeleteObject(obj.getNome()))
				.collect(Collectors.toList());

		Iterable<Result<DeleteError>> results = minioStorage
				.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(objectsToDelete).build());

		Iterator<Result<DeleteError>> it = results.iterator();
		if (it.hasNext()) {
			sb.append("Os seguintes objetos não foram deletados do bucket: ");
			DeleteError err = null;
			while (it.hasNext()) {
				err = getResultObject(it.next());
				sb.append("[objeto: ").append(err.objectName());
				sb.append(", mensagem: ").append(err.message()).append("],");
			}
		}

		if (sb.length() > 0) {
			throw new PrivateStorageException(sb.toString());
		}
	}

	private <T> T getResultObject(Result<T> result) {
		T retorno = null;
		try {
			retorno = result.get();
		} catch (Exception e) {
			throw new PrivateStorageException("Erro ao acessar um resultado de operação realizada no object storage",
					e);
		}
		return retorno;
	}

	private String getFolderName(String folder) {
		return (folder == null || folder.isEmpty()) ? "" : folder + "/";
	}
}