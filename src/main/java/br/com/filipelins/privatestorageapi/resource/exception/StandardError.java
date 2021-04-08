package br.com.filipelins.privatestorageapi.resource.exception;

import java.time.LocalDateTime;

import br.com.filipelins.privatestorageapi.domain.Utils;

public class StandardError {

	private final Integer status;
	private final String error;
	private final String exceptionMessage;
	private final String dataHora;

	public StandardError(Integer status, String error, String exceptionMessage) {
		super();
		this.status = status;
		this.error = error;
		this.exceptionMessage = exceptionMessage;
		this.dataHora = Utils.getFormattedDateTime(LocalDateTime.now());
	}

	public Integer getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}
	
	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public String getDataHora() {
		return dataHora;
	}
}
