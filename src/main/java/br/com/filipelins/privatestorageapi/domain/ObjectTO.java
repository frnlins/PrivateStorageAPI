package br.com.filipelins.privatestorageapi.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.validation.constraints.NotBlank;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;

public class ObjectTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank(message = "{object.nome.not.blank}")
	private String nome;
	private Long tamanho;
	private ZonedDateTime ultimaAlteracao;
	private boolean diretorio;

	public ObjectTO() {
	}

	public ObjectTO(String nome, Long tamanho, ZonedDateTime ultimaAlteracao, boolean diretorio) {
		this.nome = nome;
		this.tamanho = tamanho;
		this.diretorio = diretorio;

		if (!this.diretorio)
			this.ultimaAlteracao = ultimaAlteracao.withZoneSameInstant(ZoneId.systemDefault());
	}

	public ObjectTO(Item item) {
		this(item.objectName(), item.size(), (!item.isDir()) ? item.lastModified() : null, item.isDir());
	}

	public ObjectTO(StatObjectResponse stat) {
		this(stat.object(), stat.size(), stat.lastModified(), false);
	}

	public String getNome() {
		return nome;
	}

	public String getTamanho() {
		return Utils.getBytesToKB(BigDecimal.valueOf(tamanho), 2) + " KB";
	}

	public boolean getDiretorio() {
		return diretorio;
	}

	public String getUltimaAlteracao() {
		if (ultimaAlteracao != null)
			return Utils.getFormattedDateTime(ultimaAlteracao);
		else
			return "";
	}
}
