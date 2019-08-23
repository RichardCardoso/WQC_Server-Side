package com.richard.weger.wqc.exception;

import com.richard.weger.wqc.result.ErrorResult;

@SuppressWarnings("serial")
public class WebException extends Exception {
	
	public WebException(ErrorResult err, String pathToRedirect) {
		this.err = err;
		this.pathToRedirect = pathToRedirect;
	}
	
	private ErrorResult err;
	private String pathToRedirect;
	
	public ErrorResult getErr() {
		return err;
	}
	public void setErr(ErrorResult err) {
		this.err = err;
	}
	public String getPathToRedirect() {
		return pathToRedirect;
	}
	public void setPathToRedirect(String pathToRedirect) {
		this.pathToRedirect = pathToRedirect;
	}
		
}
