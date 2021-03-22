package com.richard.weger.wqc.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.SingleObjectResult;

@Service
public class ItemService {
	
	@Autowired private QrTextHandler handler;
	@Autowired private ParamConfigurationsRepository paramConfigsRep;
		
	public List<Item> getExistingPictures(String qrCode, List<Item> items) {
		Map<String, String> mapValues;
		String superFolder;
		List<Item> existing = new ArrayList<>();
		ParamConfigurations conf;
		conf = paramConfigsRep.getDefaultConfig();
		
		mapValues = handler.getParameters(qrCode);
		superFolder = "//".concat(conf.getServerPath())
				.concat(conf.getRootPath())
				.concat(mapValues.get(FactoryAppConstants.getAppConstants().getCOMMON_PATH_KEY())).concat("Fotos/");
		
		for(Item item : items) {
			String fileName = item.getPicture().getFileName();
			if(!fileName.endsWith(".jpg")) {
				fileName = fileName.concat(".jpg");
			}
			String filePath = superFolder.concat(fileName);
			File picFile = new File(filePath);
			
			if(picFile.exists()) {
				existing.add(item);
			}
		}
		
		return existing;
	}
	
	public AbstractResult itemPictureUpload(String qrCode, String fileName, MultipartFile file) {
		AppConstants appConst = FactoryAppConstants.getAppConstants();
		
		ParamConfigurations conf;
		Map<String, String> mapValues;
		
		if(qrCode == null) {
			return new ErrorResult(ErrorCode.INVALID_QRCODE, "Invalid qr code was received in a 'general picture upload' request!", ErrorLevel.SEVERE, getClass());
		}
		conf = paramConfigsRep.getDefaultConfig();	
		mapValues = handler.getParameters(qrCode);
		
		if(!fileName.endsWith(".jpg")) {
			fileName = fileName.concat(".jpg");
		}

		File targetFile = new File("//".concat(conf.getServerPath())
				.concat(conf.getRootPath())
				.concat(mapValues.get(appConst.getCOMMON_PATH_KEY()).concat("Fotos/").concat(fileName)));

		String superFolder;
		if(targetFile.getPath().contains("\\")) {
			superFolder = targetFile.getPath().substring(0, targetFile.getPath().lastIndexOf("\\"));
		} else {
			superFolder = targetFile.getPath().substring(0, targetFile.getPath().lastIndexOf("/"));
		}
		File sFolder = new File(superFolder);
		if (!sFolder.exists()) {
			sFolder.mkdirs();
		}

		try {
			Files.write(targetFile.toPath(), file.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			return new ErrorResult(ErrorCode.FILE_UPLOAD_FAILED, "Failed to save a general pic's content into the disk!", ErrorLevel.SEVERE, getClass());
		}

		return new SingleObjectResult<>(File.class, targetFile);
	}
}
