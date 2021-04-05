package br.com.filipelins.privatestorageapi.domain;

import java.time.ZonedDateTime;

import javax.validation.constraints.NotBlank;

import io.minio.messages.Bucket;

public class BucketTO {

	@NotBlank(message = "{bucket.nome.not.blank}")
	private String nome;
	private ZonedDateTime dataCriacao;

	public BucketTO() {
	}

	public BucketTO(Bucket bucket) {
		this.nome = bucket.name();
		this.dataCriacao = bucket.creationDate();
	}

	public String getNome() {
		return nome;
	}

	public String getDataCriacao() {
		return Utils.getFormattedDateTime(dataCriacao);
	}
}
