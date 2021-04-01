package br.com.filipelins.privatestorageapi.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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
		BigDecimal tamanhoKB = BigDecimal.valueOf(tamanho);
		return tamanhoKB.divide(BigDecimal.valueOf(1024)).setScale(2, RoundingMode.HALF_UP) + " KB";
	}

	public String getUltimaAlteracao() {
		return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).format(ultimaAlteracao);
	}

	public String getBucketName() {
		return bucketName;
	}
}
