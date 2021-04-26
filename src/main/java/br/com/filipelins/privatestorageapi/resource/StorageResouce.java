package br.com.filipelins.privatestorageapi.resource;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.filipelins.privatestorageapi.domain.BucketTO;
import br.com.filipelins.privatestorageapi.domain.ExtendedObjectTO;
import br.com.filipelins.privatestorageapi.domain.ObjectTO;
import br.com.filipelins.privatestorageapi.domain.Utils;
import br.com.filipelins.privatestorageapi.service.StorageService;

@RestController
@RequestMapping("/storage")
public class StorageResouce {

	@Autowired
	private StorageService storageService;

	/**
	 * Busca todos os bucktes do storage de objetos
	 * 
	 * @return lista de todos os buckets do object storage
	 */
	@GetMapping
	public ResponseEntity<List<BucketTO>> listBuckets() {
		List<BucketTO> bucketList = storageService.listBuckets();
		return ResponseEntity.ok(bucketList);
	}

	/**
	 * Cria uma bucket a partir de um nome passado no body
	 * 
	 * @param bucketTO objeto que contém o nome do bucket
	 * @return no header é possível ver a url para o bucket criado
	 */
	@PostMapping
	public ResponseEntity<Void> createBucket(@Valid @RequestBody BucketTO bucketTO) {
		storageService.createBucket(bucketTO.getNome());
		URI createdURI = ServletUriComponentsBuilder.fromCurrentRequest().path("/{bucketName}")
				.buildAndExpand(bucketTO.getNome()).toUri();
		return ResponseEntity.created(createdURI).build();
	}

	/**
	 * Obtém os objetos de um bucket
	 * 
	 * @param bucketName nome do bucket
	 * @return lista de objetos do bucket
	 */
	@GetMapping("/{bucketName}")
	public ResponseEntity<List<ObjectTO>> listObjects(@PathVariable("bucketName") String bucketName) {
		List<ObjectTO> objectList = storageService.listBucketObjects(bucketName);
		return ResponseEntity.ok(objectList);
	}

	/**
	 * Este método demonstra apenas uma outra maneira de chamar e então obter os
	 * objetos de um determinado bucket
	 * 
	 * @param bucketTO objeto contendo o nome do bucket
	 * @return lista de objetos do bucket
	 */
	@GetMapping("/listobjects")
	public ResponseEntity<List<ObjectTO>> listObjects(@Valid @RequestBody BucketTO bucketTO) {
		List<ObjectTO> objectList = storageService.listBucketObjects(bucketTO.getNome());
		return ResponseEntity.ok(objectList);
	}

	/**
	 * Deleta um bucket do storage junto com seus objetos.
	 * 
	 * @param bucketTO objeto contendo o nome do bucket que será deletado
	 * @return
	 */
	@DeleteMapping
	public ResponseEntity<Void> deleteBucket(@Valid @RequestBody BucketTO bucketTO) {
		storageService.deleteBucket(bucketTO.getNome());
		return ResponseEntity.noContent().build();
	}

	/**
	 * Deleta um ou mais objetos de um determinado bucket
	 * 
	 * @param bucketName   nome do bucket
	 * @param listObjectTO lista dos com o nome dos objetos a serem deletados
	 * @return
	 */
	@DeleteMapping("/{bucketName}")
	public ResponseEntity<Void> deleteObject(@PathVariable("bucketName") String bucketName,
			@RequestBody ObjectTO... listObjectTO) {
		storageService.deleteObject(bucketName, listObjectTO);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Faz upload de um ou mais arquivos para o serviço de storage
	 * 
	 * @param bucketName     nome do bucket onde ficarão os arquivos
	 * @param multipartFiles o(s) arquivos que serão enviados
	 * @return
	 */
	@PutMapping(path = "/{bucketName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> putObject(@PathVariable("bucketName") String bucketName,
			@RequestParam("object") MultipartFile... multipartFiles) {
		storageService.putObject(bucketName, multipartFiles);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Busca informações mais detalhadas sobre um determinado objeto de um bucket
	 * 
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	@GetMapping(path = "/{bucketName}/info/{objectName}")
	public ResponseEntity<ExtendedObjectTO> objectInfo(@PathVariable("bucketName") String bucketName,
			@PathVariable("objectName") String objectName) {
		return ResponseEntity.ok(storageService.objectInfo(bucketName, objectName));
	}

	/**
	 * Recupera o objeto do storage para download pelo usuário
	 * 
	 * @param bucketName
	 * @param objectName
	 * @return Array de bytes contendo o objeto do bucket.
	 */
	@GetMapping(path = "/{bucketName}/{objectName}")
	public ResponseEntity<ByteArrayResource> getObject(@PathVariable("bucketName") String bucketName,
			@PathVariable("objectName") String objectName) {
		var object = storageService.objectInfo(bucketName, objectName);
		var resource = storageService.getObject(bucketName, objectName);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + object.getNome())
				.contentType(Utils.getMediaTypeFromContentType(object.getContentType()))
				.contentLength(resource.contentLength()).body(resource);
	}

	/**
	 * Retorna uma url para executar uma ação (get, post, delete) válida por 1 min
	 * 
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	@GetMapping(path = "/presignedget/{bucketName}/{objectName}")
	public ResponseEntity<String> getPresignedObject(@PathVariable("bucketName") String bucketName,
			@PathVariable("objectName") String objectName) {
		String url = storageService.getPresignedObjectUrl(bucketName, objectName);
		return ResponseEntity.ok(url);
	}
	
	/**
	 * Retorna uma url para executar uma ação (get, post, delete) válida por 1 min
	 * 
	 * @param bucketName
	 * @param objectName
	 * @return
	 */
	@GetMapping(path = "/presignedput/{bucketName}/{objectName}")
	public ResponseEntity<String> putPresignedObject(@PathVariable("bucketName") String bucketName,
			@PathVariable("objectName") String objectName) {
		String url = storageService.putPresignedObjectUrl(bucketName, objectName);
		return ResponseEntity.ok(url);
	}
}
