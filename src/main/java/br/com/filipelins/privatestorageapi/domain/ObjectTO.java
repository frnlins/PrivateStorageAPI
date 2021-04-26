package br.com.filipelins.privatestorageapi.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.validation.constraints.NotBlank;

public class ObjectTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotBlank(message = "{object.nome.not.blank}")
	private String nome;
	private Long tamanho;
	private ZonedDateTime ultimaAlteracao;

	public ObjectTO() {
	}

	public ObjectTO(String nome, Long tamanho, ZonedDateTime ultimaAlteracao) {
		this.nome = nome;
		this.tamanho = tamanho;
		this.ultimaAlteracao = ultimaAlteracao.withZoneSameInstant(ZoneId.systemDefault());
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
}
