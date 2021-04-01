package br.com.filipelins.privatestorageapi.domain;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT).format(dataCriacao);
	}
}
