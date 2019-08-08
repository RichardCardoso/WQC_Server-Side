package com.richard.weger.wqc.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class Logger {
	public static void writeData(String message) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_hh:mm:ss");
		String date = sdf.format(Calendar.getInstance().getTime());
		System.out.println(date.concat(" --> ").concat(message));
	}
	
	public static <T> void customLog(StackTraceElement[] stackTrace, String message, Class<T> clazz) {
		writeData("(".concat(clazz.getSimpleName()).concat(".").concat(stackTrace[0].getMethodName()).concat(") ").concat(message));
	}
	
	public static <T> void requestLog(StackTraceElement[] stackTrace, Class<T> clazz) {
		writeData("(".concat(clazz.getSimpleName()).concat(".").concat(stackTrace[0].getMethodName()).concat(") request received."));
	}
	
	public static <T> void successLog(StackTraceElement[] stackTrace, Class<T> clazz) {
		writeData("(".concat(clazz.getSimpleName()).concat(".").concat(stackTrace[0].getMethodName()).concat(") request was processed successfully"));
	}
	
	public static <T> void failureLog(StackTraceElement[] stackTrace, Class<T> clazz) {
		writeData("(".concat(clazz.getSimpleName()).concat(".").concat(stackTrace[0].getMethodName()).concat(") request has failed"));
	}
}
