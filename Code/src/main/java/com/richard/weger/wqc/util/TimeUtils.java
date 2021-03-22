package com.richard.weger.wqc.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtils {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public String getCurrentTime() {
		Calendar calendar = Calendar.getInstance();
		return sdf.format(calendar.getTime());
	}
}
