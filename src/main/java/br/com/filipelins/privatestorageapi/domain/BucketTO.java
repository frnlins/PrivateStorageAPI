package br.com.filipelins.privatestorageapi.domain;

import java.time.ZonedDateTime;

import io.minio.messages.Bucket;

public class BucketTO {

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
