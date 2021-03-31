package com.richard.weger.wqc.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.dto.FileDTO;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SingleObjectResult;
import com.richard.weger.wqc.result.SuccessResult;

@Service
public class FileService {
	
	@Autowired private ParamConfigurationsRepository paramConfigsRep;
	@Autowired private QrTextHandler handler;
	@Autowired private ItemService itemService;
	@Autowired private ProjectService projectService;
	
	AppConstants c;
	Logger logger;
	
	public FileService() {
		logger = Logger.getLogger(getClass());
		c = FactoryAppConstants.getAppConstants();
	}
	
	public AbstractResult pictureUpload(String qrCode, String fileName, MultipartFile file, int pictureType, Long itemId) {
		if (pictureType == 0 && itemId == null) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY_ID, "Invalid entity id was received in a 'item picture upload' request!", ErrorLevel.SEVERE, getClass());
		} else if(pictureType < 0 || pictureType > 1) {
			return new ErrorResult(ErrorCode.INVALID_PICTURE_TYPE, "Invalid picture type (" + String.valueOf(pictureType) + ") was received in a 'item picture upload' request!", ErrorLevel.SEVERE, getClass());
		} else if(pictureType == 0 && itemId > 0) {
			return itemService.itemPictureUpload(qrCode, fileName, file);
		} else if (pictureType == 1) {
			return generalPictureUpload(qrCode, fileName, file);
		}
		return new ErrorResult(ErrorCode.UNMET_CONDITIONS, "Failed to proccess a picture upload request!", ErrorLevel.SEVERE, getClass());
	}

	public AbstractResult generalPictureUpload(String qrCode, String originalFileName, MultipartFile file) {
		Map<String, String> mapValues;
		ParamConfigurations conf;
		String pictureReference;
		String targetFilePrefix;
		AbstractResult targetFileResult;
		File targetFile;
		
		if(qrCode == null) {
			return new ErrorResult(ErrorCode.INVALID_QRCODE, "Invalid qr code was received in a 'general picture upload' request!", ErrorLevel.SEVERE, getClass());
		}
		
		mapValues = handler.getParameters(qrCode);
		conf = paramConfigsRep.getDefaultConfig();
		
		pictureReference = mapValues.get(c.getPROJECT_NUMBER_KEY())
				.concat("Z").concat(mapValues.get(c.getDRAWING_NUMBER_KEY()))
				.concat("T").concat(mapValues.get(c.getPART_NUMBER_KEY()))
				.concat("QP");
		targetFilePrefix = "//".concat(conf.getServerPath())
				.concat(conf.getRootPath())
				.concat(mapValues.get(c.getCOMMON_PATH_KEY()).concat("Fotos/"));
		targetFileResult = getNonExistingFile(originalFileName, targetFilePrefix, pictureReference);
		
		if(targetFileResult instanceof ErrorResult) {
			return targetFileResult;
		} else {
			targetFile = ResultService.getSingleResult(targetFileResult);
		}

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
		
		int trials = 0;
		boolean saved = false;
		do {
			try {
				Files.write(targetFile.toPath(), file.getBytes());
				saved = true;
			} catch (Exception e) {
				if(!targetFile.exists() && trials >= 6) {
					return new ErrorResult(ErrorCode.WRITE_OPERATION_FAILED, "Failed to save a general pic's content into the disk!", ErrorLevel.SEVERE, getClass());
				} else {
					trials++;
				}
				
				targetFileResult = getNonExistingFile(originalFileName, targetFilePrefix, pictureReference);
				if(targetFileResult instanceof ErrorResult) {
					return targetFileResult;
				} else {
					targetFile = ResultService.getSingleResult(targetFileResult);
				}
			}
		} while (!saved);
		return new SingleObjectResult<>(File.class, targetFile);
	}
	

	private AbstractResult getNonExistingFile(String originalFileName, String targetFilePrefix, String pictureReference) {
		File targetFile;
		try {
		if(!originalFileName.endsWith(".jpg")) {
			originalFileName = originalFileName.concat(".jpg");
		}
		String newFileName = originalFileName;
		do {
			targetFile = new File(targetFilePrefix.concat(newFileName));
			if(targetFile.exists()) {
				String oldPictureNumber = newFileName.substring(pictureReference.length(), newFileName.indexOf(".jpg"));
				String newPictureNumber = String.valueOf(Integer.valueOf(oldPictureNumber) + 1);
				newFileName = newFileName.replace(oldPictureNumber, newPictureNumber);
			}
		} while (targetFile.exists());
		} catch (Exception ex) {
			String message = "Failed to get a valid non existent file to serve as container for the file contents!";
			logger.fatal(message, ex);
			return new ErrorResult(ErrorCode.FILE_UPLOAD_FAILED, message, ErrorLevel.SEVERE, getClass());
		}
		return new SingleObjectResult<>(File.class, targetFile);
	}
	
	public List<FileDTO> getExistingPictures(String qrCode, int pictureType){
		ParamConfigurations conf = paramConfigsRep.getDefaultConfig();
		Map<String, String> mapValues;
		String folderPath;
		List<FileDTO> existing = new ArrayList<>();
		File folder;
		String pictureReference;
		
		try {
			mapValues = handler.getParameters(qrCode);
			pictureReference = mapValues.get(c.getPROJECT_NUMBER_KEY())
					.concat("Z").concat(mapValues.get(c.getDRAWING_NUMBER_KEY()))
					.concat("T").concat(mapValues.get(c.getPART_NUMBER_KEY()));
			if(pictureType == 0) {
				pictureReference = pictureReference.concat("Q");
			} else if (pictureType == 1) {
				pictureReference = pictureReference.concat("QP");
			}
			folderPath = "//".concat(conf.getServerPath())
					.concat(conf.getRootPath())
					.concat(mapValues.get(c.getCOMMON_PATH_KEY())).concat("Fotos/");
			folder = new File(folderPath);
			if(folder.exists()) {
				for(File f : folder.listFiles()) {
					String fileName = f.getName();
					if(fileName.startsWith(pictureReference) && fileName.endsWith(".jpg")) {
						FileDTO dto = new FileDTO();
						dto.setFileName(fileName);
						dto.setLastModifiedDate(f.lastModified());
						dto.setFileSize(f.length());
						existing.add(dto);
					}
				}
			} else {
				logger.warn("The folder" + folder.getPath() + " was not found. This happened during an attempt to check for existing QP pictures.");
			}
		} catch (Exception ex) {
			logger.warn("An unknown error happened while checking for existing QP pictures.", ex);
		}
		return existing;
		
	}
	
	public HttpHeaders getHeadersWithFilenames(String originalFilename, String newFilename) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("originalFileName", originalFilename);
		headers.add("newFileName", newFilename);
		return headers;
	}
	
	public List<FileDTO> getExistingPdfs(String qrcode) {
		List<FileDTO> existing;
		ParamConfigurations conf;
		String filePath;
		Project p;
		AbstractResult res;
		Map<String, String> mapValues;
		
		conf = paramConfigsRep.getDefaultConfig();
		existing = new ArrayList<>();
		res = projectService.getSingle(qrcode);
		mapValues = handler.getParameters(qrcode);
		
		if(res instanceof SuccessResult) {
	
			p = ResultService.getSingleResult(res);
			if(p != null) {
				List<String> targets = p.getDrawingRefs().stream()
						.flatMap(d -> d.getReports().stream()
									.filter(x -> x instanceof CheckReport)
									.map(x -> ((CheckReport) x).getFileName())
								)
						.collect(Collectors.toList());
				
				filePath = "//"
						.concat(conf.getServerPath())
						.concat(conf.getRootPath())
						.concat(mapValues.get(FactoryAppConstants.getAppConstants().getCOMMON_PATH_KEY()));
				
				File rootFolder = new File(filePath);
				for(String target : targets) {
					File f = getReportFile(rootFolder, target);
					if(f != null) {
						FileDTO result = new FileDTO();
						result.setFileName(target);
						result.setFileSize(f.length());
						result.setLastModifiedDate(f.lastModified());
						existing.add(result);
					}
				}
			}
			
		}
		
		return existing;
	}
	
	
	public AbstractResult getOriginalPdf(String filename, String qrcode) {
		ByteArrayResource pdf;
		Map<String, String> mapValues;
		ParamConfigurations conf;
		
		conf = paramConfigsRep.getDefaultConfig();

		if (!filename.endsWith(".pdf")) {
			filename = filename.concat(".pdf");
		}

		mapValues = handler.getParameters(qrcode);
		String filePath = "//"
				.concat(conf.getServerPath())
				.concat(conf.getRootPath())
				.concat(mapValues.get(FactoryAppConstants.getAppConstants().getCOMMON_PATH_KEY()));
//				.concat(filename);
		try {
			File pdfFile = getReportFile(new File(filePath), filename);
			pdf = new ByteArrayResource(Files.readAllBytes(pdfFile.toPath()));
		} catch (IOException e) {
			String message = "There was a problem while trying to access the following file: '" + filePath + "'! Creation proccess aborted!";
			logger.fatal(message, e);
			return new ErrorResult(ErrorCode.FILE_DOWNLOAD_PREPARATION_FAILED, message, ErrorLevel.SEVERE, getClass());
		}
		return new SingleObjectResult<>(ByteArrayResource.class, pdf);
	}
	
	private File getReportFile(File rootFolder, String targetFilename) {
		if(rootFolder != null && targetFilename != null) {
			for(File f : rootFolder.listFiles()) {
				if(f.isDirectory()) {
					File ret = getReportFile(f, targetFilename);
					if(ret != null) {
						return ret;
					}
				}
				if(f.getName().contains(targetFilename)) {
					return f;
				}
			}
		}
		return null;
	}
	
	private String getPictureAbsolutePath(String filename, String qrcode) {
		Map<String, String> mapValues;
		ParamConfigurations conf;
		String filePath;
		
		conf = paramConfigsRep.getDefaultConfig();

		mapValues = handler.getParameters(qrcode);

		if (!filename.endsWith(".jpg")) {
			filename = filename.concat(".jpg");
		}
		
		filePath = "//"
				.concat(conf.getServerPath())
				.concat(conf.getRootPath())
				.concat(mapValues.get(FactoryAppConstants.getAppConstants().getCOMMON_PATH_KEY()))
				.concat("Fotos/")
				.concat(filename);
		return filePath;
	}
	
	public AbstractResult getPicture(String filename, String qrcode) {
		ByteArrayResource jpg;
		String filePath;

		filePath = getPictureAbsolutePath(filename, qrcode);

		File file = new File(filePath);
		if (!file.exists()) {
			return new EmptyResult();
		}
		try {
			jpg = new ByteArrayResource(Files.readAllBytes(new File(filePath).toPath()));
		} catch (IOException e) {
			String message = "Failed to get a picture file!";
			logger.fatal(message, e);
			return new ErrorResult(ErrorCode.FILE_DOWNLOAD_PREPARATION_FAILED, message,ErrorLevel.SEVERE, getClass());
		}
		return new SingleObjectResult<>(ByteArrayResource.class, jpg);
	}
	
	public Long getPictureLastModifiedDate(String filename, String qrcode) {
		String filepath;
		
		try {
			filepath = getPictureAbsolutePath(filename, qrcode);
			return getPictureLastModifiedDate(filepath);
		} catch (Exception ex) {
			logger.warn("Unable to retrieve a file for last modified date retrieval!", ex);
			return Calendar.getInstance().getTimeInMillis();
		}
	}
	
	public Long getPictureLastModifiedDate(String filepath) {
		Long lastModified;
		File f;
		
		try {
			f = new File(filepath);
			lastModified = f.lastModified();
		} catch (Exception ex) {
			logger.warn("Unable to retrieve a file for last modified date retrieval!", ex);
			lastModified = Calendar.getInstance().getTimeInMillis();
		}
		return lastModified;
	}
	
}
