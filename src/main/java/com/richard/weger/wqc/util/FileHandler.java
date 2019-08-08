package com.richard.weger.wqc.util;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;

public class FileHandler {
	
	public File getResourcesFile(String fileName) {
		File f;
		try {
			f = new File(new ClassPathResource("/static").getFile() + "/" + fileName);
		} catch (IOException e) {
			f = null;
			e.printStackTrace();
		}
		return f;
	}
}
