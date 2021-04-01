package br.com.filipelins.privatestorageapi.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import io.minio.messages.Item;

public class ObjectTO {

	private String nome;
	private Long tamanho;
	private ZonedDateTime ultimaAlteracao;
	private String bucketName;

	public ObjectTO() {
	}

	public ObjectTO(Item item, String bucketName) {
		this.nome = item.objectName();
		this.tamanho = item.size();
		this.ultimaAlteracao = item.lastModified();
		this.bucketName = bucketName;
	}

	public String getNome() {
		return nome;
	}

	public String getTamanho() {
		return Utils.getBytesToKB(BigDecimal.valueOf(tamanho), 2) + " KB";
	}

	public String getUltimaAlteracao() {
		return Utils.getFormattedDateTime(ultimaAlteracao);
	}

	public String getBucketName() {
		return bucketName;
	}
}
