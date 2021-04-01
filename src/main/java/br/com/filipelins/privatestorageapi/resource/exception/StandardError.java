package br.com.filipelins.privatestorageapi.resource.exception;

import java.time.LocalDateTime;

import br.com.filipelins.privatestorageapi.domain.Utils;

public class StandardError {

	private final Integer status;
	private final String error;
	private final String localDateTime;

	public StandardError(Integer status, String error, LocalDateTime localDateTime) {
		super();
		this.status = status;
		this.error = error;
		this.localDateTime = Utils.getFormattedDateTime(localDateTime);
	}

	public Integer getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}

	public String getLocalDateTime() {
		return localDateTime;
	}
}
