package com.richard.weger.wqc.result;

import java.util.List;

import org.springframework.http.HttpHeaders;

@SuppressWarnings("unchecked")
public class ResultService {
	
	public static <T> SingleObjectResult<T> getSingleResultContainer(AbstractResult res, Class<T> clz) {
		SingleObjectResult<T> ret = null;
		if(res instanceof SingleObjectResult) {
			SingleObjectResult<?> rTest = (SingleObjectResult<?>) res;
			if(rTest.getContentClz().isAssignableFrom(clz)) {
				ret = (SingleObjectResult<T>) res;
			}
		}
		return ret;
	}
	
	public static <T> T getSingleResult(AbstractResult res, Class<T> clz) {
		T ret = null;
		SingleObjectResult<T> rWork = getSingleResultContainer(res, clz);
		if(rWork != null) {
			ret = rWork.getObject();
		}
		return ret;
	}
	
	public static <T> List<T> getMultipleResult(AbstractResult res, Class<T> clz) {
		List<T> lst = null;
		if(res instanceof MultipleObjectResult) {
			MultipleObjectResult<T> rWork = null;
			MultipleObjectResult<?> rTest = (MultipleObjectResult<?>) res;
			if(rTest.getContentClz().isAssignableFrom(clz)) {
				rWork = (MultipleObjectResult<T>) res;
				lst = rWork.getObjects();
			}
		}
		return lst;
	}
	
	public static ErrorResult getErrorResult(AbstractResult res) {
		if(res instanceof ErrorResult) {
			return (ErrorResult) res;
		} else {
			return null;
		}
	}
	
	public static HttpHeaders getErrorHeaders(ErrorResult err) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("code", String.valueOf(err.getCode()));
		headers.set("message", err.getDescription());
		headers.set("level", err.getLevel().toString());
		return headers;
	}
	
}
