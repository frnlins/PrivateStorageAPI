package br.com.filipelins.privatestorageapi.domain;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;

public class ReturnMessage<T> {

	private String message;
	private HttpStatus httpStatus;
	private String exceptionMessage;
	private List<T> lista;

	public ReturnMessage(String message) {
		this(message, HttpStatus.BAD_REQUEST);
	}

	public ReturnMessage(String message, HttpStatus httpStatus) {
		this(message, httpStatus, "", Collections.emptyList());
	}

	public ReturnMessage(String message, HttpStatus httpStatus, String exceptionMessage) {
		this(message, httpStatus, exceptionMessage, Collections.emptyList());
	}

	public ReturnMessage(String message, HttpStatus httpStatus, List<T> lista) {
		this(message, httpStatus, "", lista);
	}

	public ReturnMessage(String message, HttpStatus httpStatus, String exceptionMessage, List<T> lista) {
		this.message = message;
		this.httpStatus = httpStatus;
		this.exceptionMessage = exceptionMessage;
		this.lista = lista;
	}

	public String getMessage() {
		return message;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public List<T> getLista() {
		return lista;
	}
}
