package br.com.filipelins.privatestorageapi.domain;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.validation.constraints.NotBlank;

import io.minio.messages.Bucket;

public class BucketTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank(message = "{bucket.nome.not.blank}")
	private String nome;
	private ZonedDateTime dataCriacao;

	public BucketTO() {
	}

	public BucketTO(Bucket bucket) {
		this.nome = bucket.name();
		if (bucket.creationDate() != null)
			this.dataCriacao = bucket.creationDate().withZoneSameInstant(ZoneId.systemDefault());
	}

	public String getNome() {
		return nome;
	}

	public String getDataCriacao() {
		return Utils.getFormattedDateTime(dataCriacao);
	}
}
