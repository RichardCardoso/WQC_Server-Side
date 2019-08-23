package com.richard.weger.wqc.result;

import org.apache.log4j.Logger;

public class ErrorResult extends AbstractResult {
	
	public enum ErrorLevel {
		SEVERE,
		WARNING,
		LOG
	}
	
	public enum ErrorCode {
		INVALID_QRCODE,
		INVALID_ENTITY_ID,
		INVALID_ENTITY_VERSION,
		INVALID_REPORT_TYPE,
		INVALID_FILE_CONTENT,
		INVALID_ROLE_DESCRIPTION,
		INVALID_ENTITY_NAME,
		INVALID_ENTITY,
		INVALID_PICTURE_TYPE,
		UNMET_CONDITIONS,
		ENTITY_NOT_FOUND,
		ENTITY_CREATION_FAILED,
		ENTITY_PERSIST_FAILED,
		ENTITY_DELETE_FAILED,
		ENTITY_EXPORT_FAILED,	
		ENTITY_RETRIEVAL_FAILED,
		REPORTS_FOLDER_PATH_RETRIEVAL_FAILED,
		NULL_ENTITY_RECEIVED,
		FILENAME_RETRIEVAL_FAILED,
		FILE_PREVIEW_FAILED,
		QR_TRANSLATION_FAILED,
		WRITE_OPERATION_FAILED,
		FILE_UPLOAD_FAILED,
		FILE_DOWNLOAD_PREPARATION_FAILED,
		STALE_ENTITY,
		BASE_FILE_RETRIEVAL_FAILED,
		BASE_FILE_IO_FAILED,
		UNKNOWN_ERROR,
		GENERAL_SERVER_FAILURE
	}
	
	private String code;
	private String description;
	private ErrorLevel level;
	
	private transient Logger logger;
	
	public ErrorLevel getLevel() {
		return level;
	}

	public void setLevel(ErrorLevel level) {
		this.level = level;
	}

	public ErrorResult(ErrorCode code, String description, ErrorLevel level, Class<?> callerClass) {
		logger = Logger.getLogger(callerClass);
		
		if(code != null) {
			setCode(code.toString());
		} else {
			setCode(ErrorCode.UNKNOWN_ERROR.toString());
			logger.warn("Null error code passed to ErrorResult constructor");
		}
		
		setDescription(description);
		
		if(level == ErrorLevel.SEVERE) {
			logger.fatal(description);
		} else if (level == ErrorLevel.WARNING) {
			logger.warn(description);
		} else if (level == ErrorLevel.LOG){
			logger.info(description);
		}
		
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}
