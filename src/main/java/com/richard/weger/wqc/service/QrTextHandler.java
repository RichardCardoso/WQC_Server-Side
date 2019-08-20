package com.richard.weger.wqc.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;

@Service
public class QrTextHandler {
	
	Logger logger;
	
	public QrTextHandler() {
		logger = Logger.getLogger(QrTextHandler.class);
	}
	
	@Autowired private ParamConfigurationsRepository rep;

	/*
	 * Output example: 
	 * { 
	 * drawingNumber=1,
	 * constructionPath=2017/17-1-___/17-1-435/Technik/Kundendaten/Teil01-Z01/,
	 * technicalPath=2017/17-1-___/17-1-435/Technik/Kundendaten/,
	 * projectNumber=17-1-435, 
	 * commonPath=2017/17-1-___/17-1-435/, 
	 * partNumber=1 
	 * }
	 */
	public Map<String, String> getParameters(String qrText) {

		ParamConfigurations conf = rep.getDefaultConfig();
		AppConstants appconst = FactoryAppConstants.getAppConstants();
		Map<String, String> mapValues = new HashMap<>();
		
		qrText = qrText.replace("\\", "").replace("/", "");

		try {
			StringBuilder sb = new StringBuilder();
			int a, b;
			if (!qrText.startsWith("\\")) {
				qrText = "\\" + qrText;
			}
			qrText.replace("[", "");
			qrText.replace("]", "");
			sb.append(conf.getYearPrefix());
			sb.append(qrText.substring(1, 3));
			sb.append("/");
			sb.append(qrText.substring(1, 3));
			sb.append("-");
			a = qrText.indexOf('-');
			b = qrText.indexOf('-', a + 1);
			sb.append(qrText.substring(a + 1, b));
			sb.append("-___/");
			b = qrText.indexOf('Z');
			sb.append(qrText.substring(1, b - 1));
			sb.append("/");

			mapValues.put(appconst.getCOMMON_PATH_KEY(), sb.toString());

			sb.append(conf.getOriginalDocsPath());
			sb.append("Teil");
			a = qrText.indexOf('T');
			sb.append(String.format("%02d", Integer.valueOf(qrText.substring(a + 2, qrText.length()))));
			sb.append("-Z");
			b = qrText.indexOf('Z');
			sb.append(String.format("%02d", Integer.valueOf(qrText.substring(b + 2, a - 1))));

			mapValues.put(appconst.getCONSTRUCTION_PATH_KEY(), sb.toString().concat("/"));

			sb = new StringBuilder();
			sb.append(mapValues.get(appconst.getCOMMON_PATH_KEY()).concat(conf.getOriginalDocsPath()));
			mapValues.put(appconst.getTECHNICAL_PATH_KEY(), sb.toString());

			// qr_text_sample: \17-1-435_Z_1_T_1
			b = qrText.indexOf('Z') - 1;
			mapValues.put(appconst.getPROJECT_NUMBER_KEY(), qrText.substring(1, b));
			a = qrText.indexOf('Z') + 2;
			b = qrText.indexOf('T') - 1;
			mapValues.put(appconst.getDRAWING_NUMBER_KEY(), qrText.substring(a, b));
			a = qrText.indexOf('T') + 2;
			b = qrText.length();
			mapValues.put(appconst.getPART_NUMBER_KEY(), qrText.substring(a, b));

			return mapValues;
		} catch (Exception e) {
			if(Strings.isEmpty(qrText)) {
				logger.fatal("A null qr code was received!", e);
			} else {
				logger.fatal("An error has ocurred while reading the following qr code --> " + qrText, e);
			}
			return null;
		}
	}
	
	public Map<String, String> getParameters(Project project) {
		return getParameters(createQrText(project));
	}

	public String createQrText(Project project) {
		// qr_text_sample: \17-1-435_Z_1_T_1
		try {
			StringBuilder sb = new StringBuilder();
			String projectNumber = project.getReference();
			int drawingNumber = project.getDrawingRefs().get(0).getDnumber();
			int partNumber = project.getDrawingRefs().get(0).getParts().get(0).getNumber();
			sb.append('\\');
			sb.append(projectNumber);
			sb.append("_Z_");
			sb.append(drawingNumber);
			sb.append("_T_");
			sb.append(partNumber);
			return sb.toString();
		} catch (Exception e) {
			if(project == null) {
				logger.fatal("A null project was received!", e);
			} else {
				logger.fatal("The qr code creation has failed!", e);
			}
			return null;
		}
	}
}
