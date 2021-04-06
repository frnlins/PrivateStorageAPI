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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.filipelins.privatestorageapi.domain.BucketTO;
import br.com.filipelins.privatestorageapi.domain.ObjectTO;
import br.com.filipelins.privatestorageapi.domain.ReturnMessage;
import br.com.filipelins.privatestorageapi.domain.Utils;
import br.com.filipelins.privatestorageapi.service.StorageService;

@RestController
@RequestMapping("/storage")
public class StorageResouce {

	@Autowired
	private StorageService storageService;

	/**
	 * Busca todos os bucktes do storage de objetos
	 * @return lista de todos os buckets do object storage 
	 */
	@GetMapping
	public ResponseEntity<List<BucketTO>> listBuckets() {
		List<BucketTO> bucketList = storageService.listBuckets();
		return ResponseEntity.ok(bucketList);
	}

	/**
	 * Cria uma bucket a partir de um nome passado no body
	 * @param bucketTO
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
	 * @param bucketName
	 * @return lista de objetos do bucket
	 */
	@GetMapping("/{bucketName}")
	public ResponseEntity<List<ObjectTO>> listObjects(@PathVariable("bucketName") String bucketName) {
		List<ObjectTO> objectList = storageService.listBucketObjects(bucketName);
		return ResponseEntity.ok(objectList);
	}
	
	/**
	 * Este método demonstra apenas uma outra maneira de chamar e então obter os objetos de um determinado bucket
	 * @param bucketTO
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
	 * @param bucketTO
	 * @return
	 */
	@DeleteMapping
	public ResponseEntity<Void> deleteBucket(@Valid @RequestBody BucketTO bucketTO) {
		storageService.deleteBucket(bucketTO.getNome());
		return ResponseEntity.noContent().build();
	}

	@PostMapping(path = "/{bucketName}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ReturnMessage<ObjectTO>> uploadObject(@PathVariable("bucketName") String bucketName,
			@RequestParam("object") MultipartFile multipartFile) {
		ReturnMessage<ObjectTO> rm = storageService.putObject(bucketName, multipartFile);
		return new ResponseEntity<ReturnMessage<ObjectTO>>(rm, rm.getHttpStatus());
	}

	@GetMapping(path = "/{bucketName}/{objectName}")
	public ResponseEntity<ByteArrayResource> downloadObject(@PathVariable("bucketName") String bucketName,
			@PathVariable("objectName") String objectName) {
		ByteArrayResource bar = storageService.downloadObjetc(bucketName, objectName);
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + objectName)
				.contentType(Utils.extractMediaType(objectName)).contentLength(bar.contentLength()).body(bar);
	}

	@GetMapping(path = "/getobject")
	public ResponseEntity<ByteArrayResource> downloadObject(@RequestBody ObjectTO objectTO) {
		ByteArrayResource bar = storageService.downloadObjetcLocal(objectTO.getBucketName(), objectTO.getNome());
		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + objectTO.getNome())
				.contentType(Utils.extractMediaType(objectTO.getNome())).contentLength(bar.contentLength()).body(bar);
	}

	@GetMapping(path = "/downloadlocal")
	public ResponseEntity<Void> downloadObjectLocal(@RequestBody ObjectTO objectTO) {
		storageService.downloadObjetcLocalFileSystem(objectTO.getBucketName(), objectTO.getNome());
		return ResponseEntity.noContent().build();
	}
}
