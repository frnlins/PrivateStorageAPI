package br.com.filipelins.privatestorageapi.domain;

import java.util.Map;
import java.util.stream.Collectors;

import io.minio.StatObjectResponse;

public class ExtendedObjectTO extends ObjectTO {

	private static final long serialVersionUID = 1L;

	private String contentType;
	private String versionId;
	private Map<String, String> userMetadata;

	public ExtendedObjectTO(StatObjectResponse stat) {
		super(stat);
		this.contentType = stat.contentType();
		this.versionId = stat.versionId();
		this.userMetadata = stat.userMetadata();
	}

	public String getContentType() {
		return contentType;
	}

	public String getVersionId() {
		return versionId;
	}

	public String getUserMetadata() {
		String retorno = "";
		if (userMetadata != null) {
			retorno = userMetadata.entrySet().stream().map(entry -> entry.getKey() + " = " + entry.getValue())
					.collect(Collectors.joining(","));
		}
		return retorno;
	}
}
