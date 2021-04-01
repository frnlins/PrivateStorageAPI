package br.com.filipelins.privatestorageapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class StorageConfig {

	@Value("${storage.accesskey}")
	private String accessKey;

	@Value("${storage.secretkey}")
	private String secretKey;

	@Value("${storage.endpoint}")
	private String endpoint;

	@Bean()
	public MinioClient minioStorage() {
		return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
	}
}
