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

	@GetMapping
	public ResponseEntity<List<BucketTO>> listBuckets() {
		List<BucketTO> bucketList = storageService.listBuckets();
		return ResponseEntity.ok(bucketList);
	}

	@PostMapping
	public ResponseEntity<Void> createBucket(@Valid @RequestBody BucketTO bucketTO) {
		storageService.createBucket(bucketTO.getNome());
		URI createdURI = ServletUriComponentsBuilder.fromCurrentRequest().path("/{bucketName}")
				.buildAndExpand(bucketTO.getNome()).toUri();
		return ResponseEntity.created(createdURI).build();
	}

	@GetMapping("/listobjects")
	public ResponseEntity<ReturnMessage<ObjectTO>> listObjects(@RequestBody BucketTO bucketTO) {
		ReturnMessage<ObjectTO> rm = storageService.listBucketObjects(bucketTO.getNome());
		return new ResponseEntity<ReturnMessage<ObjectTO>>(rm, rm.getHttpStatus());
	}

	@GetMapping("/{bucketName}")
	public ResponseEntity<ReturnMessage<ObjectTO>> listObjects(@PathVariable("bucketName") String bucketName) {
		ReturnMessage<ObjectTO> rm = storageService.listBucketObjects(bucketName);
		return new ResponseEntity<ReturnMessage<ObjectTO>>(rm, rm.getHttpStatus());
	}

	@DeleteMapping
	public ResponseEntity<ReturnMessage<BucketTO>> deleteBucket(@RequestBody BucketTO bucketTO) {
		ReturnMessage<BucketTO> rm = storageService.deleteBucket(bucketTO.getNome());
		return new ResponseEntity<ReturnMessage<BucketTO>>(rm, rm.getHttpStatus());
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
