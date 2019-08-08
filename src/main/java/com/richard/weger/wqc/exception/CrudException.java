package com.richard.weger.wqc.exception;

import org.springframework.dao.DataAccessException;

public class CrudException extends DataAccessException{
	
	public CrudException(String message) {
		super(message);
	}
	
}
