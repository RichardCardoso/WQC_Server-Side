package com.richard.weger.wqc.exception;

@SuppressWarnings("serial")
public class DuplicatedDataException extends CrudException {

	public DuplicatedDataException(String message) {
		super(message);
	}

}
