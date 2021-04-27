package br.com.filipelins.privatestorageapi.domain;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.minio.http.Method;

public class ConfigPresignedURL implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank(message = "{presigned.bucket.notblank}")
	private String bucketName;
	
	@NotBlank(message = "{presigned.object.notblank}")
	private String objectName;
	
	@NotNull(message = "{presigned.expiryTime.notnull}")
	private Integer expiryTime;
	
	@NotBlank(message = "{presigned.method.notblank}")
	private String method;
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBucketName() {
		return bucketName;
	}

	public String getObjectName() {
		return objectName;
	}

	public Integer getExpiryTime() {
		return expiryTime;
	}

	public Method getMethod() {
		return Method.valueOf(method.toUpperCase());
	}
}
