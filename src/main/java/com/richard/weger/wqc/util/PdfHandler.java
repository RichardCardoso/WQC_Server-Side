package com.richard.weger.wqc.util;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

public class PdfHandler {
	public int getPagesCount(String filePath) {
		PDDocument doc = null;
		try {
			doc = PDDocument.load(new File(filePath));
			return doc.getNumberOfPages();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (doc != null) {
				try {
					doc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
