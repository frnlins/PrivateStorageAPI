package br.com.filipelins.privatestorageapi.service.exception;

public class PrivateStorageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PrivateStorageException() {
		super();
	}

	public PrivateStorageException(String message, Throwable cause) {
		super(message, cause);
	}

	public PrivateStorageException(String message) {
		super(message);
	}
}
