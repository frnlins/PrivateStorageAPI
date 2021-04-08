package br.com.filipelins.privatestorageapi.domain;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtendedObjectTO extends ObjectTO {

	private String contentType;
	private String versionId;
	private Map<String, String> userMetadata;

	public ExtendedObjectTO(String nome, long tamanho, ZonedDateTime ultimaAlteracao, String contentType, String versionId,
			Map<String, String> userMetadata) {
		super(nome, tamanho, ultimaAlteracao);
		this.contentType = contentType;
		this.versionId = versionId;
		this.userMetadata = userMetadata;
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
