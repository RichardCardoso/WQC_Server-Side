package com.richard.weger.wqc.service;

import static com.richard.weger.wqc.util.Logger.customLog;
import static com.richard.weger.wqc.util.Logger.failureLog;
import static com.richard.weger.wqc.util.Logger.successLog;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.FactoryProject;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Part;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.CheckReportHelper;
import com.richard.weger.wqc.helper.ReportHelper;
import com.richard.weger.wqc.repository.DrawingRefRepository;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.repository.ProjectRepository;
import com.richard.weger.wqc.util.TimeUtils;
import com.richard.weger.wqc.util.WorkbookHandler;

@Service
public class ProjectService {

	TimeUtils timeUtils = new TimeUtils();
	Map<String, String> mapValues;
	List<Project> projects;
	
	@Autowired private QrTextHandler qrHandler;
	@Autowired private ProjectRepository projectRep;
	@Autowired private DrawingRefRepository drawingRep;
	@Autowired private ParamConfigurationsRepository paramConfigsRep;
	@Autowired private WorkbookHandler wbHandler;
	
	@Autowired private CheckReportHelper checkReportHelper;
	@Autowired private ReportHelper helper;

	public Project getSingle(String qrCode) {
		
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();

		customLog(stackTrace, "Loading project using Qr Code.", getClass());

		Project project = new Project();
		AppConstants c = FactoryAppConstants.getAppConstants();
		int drawingNumber, partNumber;
		String projectReference;

		// Tratamento do código QR
		mapValues = qrHandler.getParameters(qrCode);

		if (mapValues == null) {
			return null;
		}

		// Carregamento do projeto
		drawingNumber = Integer.valueOf(mapValues.get(c.getDRAWING_NUMBER_KEY()));
		partNumber = Integer.valueOf(mapValues.get(c.getPART_NUMBER_KEY()));
		projectReference = mapValues.get(c.getPROJECT_NUMBER_KEY());
		project = projectRep.findByReferenceAndDrawingRefsDnumberAndDrawingRefsPartsNumber(projectReference, drawingNumber, partNumber);

		if(project != null) {
			helper.createReports(project.getDrawingRefs().get(0), mapValues, false);
			
			return project;
		} else {
			return null;
		}

	}

	public int createSingle(String qrCode) {
		String restResult = "";
		Project project = new Project();
		AppConstants c = FactoryAppConstants.getAppConstants();
		DrawingRef drawing;
		boolean newProject, saveResult;
		
		StackTraceElement[] stackTrace = new Throwable().getStackTrace();

		// Tratamento do código QR
		mapValues = qrHandler.getParameters(qrCode);
		if(mapValues == null ||
				mapValues.get(c.getPROJECT_NUMBER_KEY()) == null ||
				mapValues.get(c.getDRAWING_NUMBER_KEY()) == null ||
				mapValues.get(c.getPART_NUMBER_KEY()) == null
		) {
			customLog(stackTrace, "Error while trying to resolve qr code data!", getClass());
			customLog(stackTrace, 
					"Project number: "
					.concat(mapValues.get(c.getPROJECT_NUMBER_KEY()))
					.concat(", Drawing number: ")
					.concat(mapValues.get(c.getDRAWING_NUMBER_KEY()))
					.concat(", Part number: ")
					.concat(mapValues.get(c.getPART_NUMBER_KEY()))
					, getClass());
			return 0;
		}
		
		String projectRef = mapValues.get(c.getPROJECT_NUMBER_KEY());
		project = projectRep.findByReference(projectRef);
		
		if(project == null) {
			customLog(stackTrace, "Project ".concat(projectRef).concat(" was not found and will be created!"), getClass());
			newProject = true;
			project = FactoryProject.getProject(mapValues);
			drawing = project.getDrawingRefs().get(0);
			int partNumber = Integer.valueOf(mapValues.get(c.getPART_NUMBER_KEY()));
			Part p = new Part();
			p.setNumber(partNumber);
			p.setParent(drawing);
			drawing.getParts().add(p);
		} else {
			customLog(stackTrace, "Project ".concat(projectRef).concat(" was found and will be updated!"), getClass());
			newProject = false;
			int drawingNumber = Integer.valueOf(mapValues.get(c.getDRAWING_NUMBER_KEY()));
			int partNumber = Integer.valueOf(mapValues.get(c.getPART_NUMBER_KEY()));
			drawing = project.getDrawingRefs().stream()
					.filter(d -> d.getDnumber() == drawingNumber)
					.findFirst().orElse(null);
			if(drawing == null) {
				customLog(stackTrace, "Drawing ".concat(String.valueOf(drawingNumber)).concat(" was not found and will be created!"), getClass());
				drawing = new DrawingRef();
				drawing.setDnumber(drawingNumber);
				drawing.setParent(project);
			}
			Part p = drawing.getParts().stream().filter(part -> part.getNumber() == partNumber).findFirst().orElse(null);
			if(p == null) {
				customLog(stackTrace, "Part ".concat(String.valueOf(partNumber)).concat(" was not found and will be created!"), getClass());
				p = new Part();
				p.setNumber(partNumber);
				p.setParent(drawing);
				drawing.getParts().add(p);
			}
		}
		
		customLog(stackTrace, "Starting routine to create drawing's reports", getClass());
		restResult = helper.createReports(drawing, mapValues, false);
		if (restResult != null) {
			customLog(stackTrace, "Reports creation failed! Resulting message is: ".concat(restResult), getClass());
			customLog(stackTrace, restResult, getClass());
			customLog(stackTrace, "Execution will continue since this is no longer considered as a severe exception.", getClass());
		}
		
		if(newProject) {
			project = projectRep.save(project);
			saveResult = project != null;
		} else {
			drawing = drawingRep.save(drawing);
			saveResult = drawing != null;
		}

		if (saveResult) {
			successLog(stackTrace, getClass());
			return 1;
		} else {
			failureLog(stackTrace, getClass());
			return 0; // error
		}
	}
	
	@Transactional(readOnly = true)
	public String export(Report report) {
		if(report instanceof ItemReport) {
			ItemReport ir = (ItemReport) report;
			return export(ir);
		} else if (report instanceof CheckReport) {
			CheckReport cr = (CheckReport) report;
			return export(cr);
		} else {
			return "Invalid report type " + report.getClass().getSimpleName();
		}
	}
	
	public String export(ItemReport report) {
		String message;
		message = wbHandler.handleWorkbook(report);
		return message;
	}
	
	public String export(CheckReport report) {
		String message;
		message = checkReportHelper.bitmap2Pdf(report);
		return message;
	}

	public String getReportsFolderPath(Project project) {
		AppConstants consts = FactoryAppConstants.getAppConstants();
		Map<String, String> mapValues = qrHandler.getParameters(project);

		String path = paramConfigsRep.getDefaultConfig().getServerPath();
		path = path.concat(paramConfigsRep.getDefaultConfig().getRootPath());
		path = path.concat(mapValues.get(consts.getCOMMON_PATH_KEY()));
		path = path.concat("/Technik/Qualitaetskontrolle/");
		return path;
	}
	
	public List<String> getExistingPictures(String qrCode, int pictureType){
		ParamConfigurations conf = paramConfigsRep.getDefaultConfig();
		AppConstants c = FactoryAppConstants.getAppConstants();
		Map<String, String> mapValues;
		String folderPath;
		List<String> existing = new ArrayList<>();
		File folder;
		String pictureReference;
		
		mapValues = qrHandler.getParameters(qrCode);
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
					existing.add(fileName);
				}
			}
		} else {
			System.out.println("The folder" + folder.getPath() + " was not found. This happened during an attempt to check for existing QP pictures.");
			return new ArrayList<String>();
		}
		return existing;
		
	}
	
	public int generalPictureUpload(String qrCode, String originalFileName, MultipartFile file, HttpHeaders httpHeaders) {
		Map<String, String> mapValues = qrHandler.getParameters(qrCode);
		AppConstants c = FactoryAppConstants.getAppConstants();
		ParamConfigurations conf = paramConfigsRep.getDefaultConfig();
		
		httpHeaders.add("originalFileName", originalFileName);
		String pictureReference = mapValues.get(c.getPROJECT_NUMBER_KEY())
				.concat("Z").concat(mapValues.get(c.getDRAWING_NUMBER_KEY()))
				.concat("T").concat(mapValues.get(c.getPART_NUMBER_KEY()))
				.concat("QP");
		String targetFilePrefix = "//".concat(conf.getServerPath())
				.concat(conf.getRootPath())
				.concat(mapValues.get(c.getCOMMON_PATH_KEY()).concat("Fotos/"));
		File targetFile = getNonExistingFile(originalFileName, targetFilePrefix, pictureReference, httpHeaders);

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
					e.printStackTrace();
					return 0;
				} else {
					trials++;
				}
				targetFile = getNonExistingFile(originalFileName, targetFilePrefix, pictureReference, httpHeaders);
			}
		} while (!saved);
		return 1;
	}
	
	private static File getNonExistingFile(String originalFileName, String targetFilePrefix, String pictureReference, HttpHeaders httpHeaders) {
		File targetFile;
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
				httpHeaders.remove("newFileName");
				httpHeaders.add("newFileName", newFileName);
			}
		} while (targetFile.exists());
		return targetFile;
	}
	
	public boolean removeInvalidReports(Project project) {
		boolean removed = false;
		ParamConfigurations conf = paramConfigsRep.getDefaultConfig();
		do {
			removed = false;
			for(Report r : project.getDrawingRefs().get(0).getReports()) {
				if(r instanceof CheckReport) {
					CheckReport cr = (CheckReport) r;
					mapValues = qrHandler.getParameters(qrHandler.createQrText(project));
					String fileName = cr.getFileName();
					String filePath = "//".concat(conf.getServerPath())
							.concat(conf.getRootPath())
							.concat(mapValues.get(FactoryAppConstants.getAppConstants().getTECHNICAL_PATH_KEY())).concat(fileName);
					File reportFile = new File(filePath);
					if(!reportFile.exists()) {
						project.getDrawingRefs().get(0).getReports().remove(r);
						removed = true;
						break;
					}
				}
			}
		} while (removed);
		return false;
	}
	
}
