package com.richard.weger.wqc.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.FactoryProject;
import com.richard.weger.wqc.domain.Part;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.ReportHelper;
import com.richard.weger.wqc.repository.DrawingRefRepository;
import com.richard.weger.wqc.repository.ProjectRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.SingleObjectResult;
import com.richard.weger.wqc.util.TimeUtils;

@Service
public class ProjectService {

	TimeUtils timeUtils = new TimeUtils();
	Map<String, String> mapValues;
	List<Project> projects;
	
	Logger logger;
	

	@Autowired private ProjectRepository projectRep;
	@Autowired private DrawingRefRepository drawingRep;
	@Autowired private QrTextHandler qrHandler;
	@Autowired private ReportHelper helper;
	
	AppConstants c;
	
	public ProjectService() {
		 c = FactoryAppConstants.getAppConstants();
		 logger = Logger.getLogger(getClass());
	}

	public AbstractResult getSingle(String qrCode){
		
		Project project = new Project();
		int drawingNumber, partNumber;
		String projectReference;
		
		if(qrCode == null) {
			String message = "A null qr code was received at ProjectService.getSingle method";
			return new ErrorResult(ErrorCode.INVALID_QRCODE, message, ErrorLevel.SEVERE, getClass());
		}
		
		// QrCode Translation - begin
		mapValues = qrHandler.getParameters(qrCode);
		if (mapValues == null) {
			String message = "An error has ocurred while trying to retrieve some parameters of the QR code";
			return new ErrorResult(ErrorCode.INVALID_QRCODE, message, ErrorLevel.SEVERE, getClass());
		}
		drawingNumber = Integer.valueOf(mapValues.get(c.getDRAWING_NUMBER_KEY()));
		partNumber = Integer.valueOf(mapValues.get(c.getPART_NUMBER_KEY()));
		projectReference = mapValues.get(c.getPROJECT_NUMBER_KEY());
		// QrCode Translation - end
		
		// Project load - begin
		project = projectRep.findByReferenceAndDrawingRefsDnumberAndDrawingRefsPartsNumber(projectReference, drawingNumber, partNumber);
		// Project load - end
		
		if(project != null) {
			
			// Reports handling - begin
			boolean saveResult = false;
			if(helper.handleReports(project.getDrawingRefs().get(0), mapValues, false)) {
				project = projectRep.save(project);
				saveResult = project != null;
			} else {
				saveResult = true; 
			}
			// Reports handling - end
			if(saveResult) {
				
				// filter reports and drawings by drawing number
				childFilter(project, drawingNumber, partNumber);
				
				return new SingleObjectResult<>(Project.class, project);
			} else {
				return new ErrorResult(ErrorCode.ENTITY_PERSIST_FAILED, "Failed to proccess report's file list for a project!", ErrorLevel.SEVERE, getClass());
			}
		} else {
			String message = "No project with qr code '" + qrCode + "' was found!";
			logger.info(message);
			return new EmptyResult();
		}

	}

	public AbstractResult createSingle(String qrCode) {
		Project project = new Project();
		DrawingRef drawing;
		boolean newProject, saveResult;
		
		logger.info("A project creation request was received.");

		mapValues = qrHandler.getParameters(qrCode);
		if(mapValues == null) {
			return new ErrorResult(ErrorCode.INVALID_QRCODE, "Invalid or corrupted qr code!", ErrorLevel.SEVERE, getClass());
		}
		
		String projectRef = mapValues.get(c.getPROJECT_NUMBER_KEY());
		
		int drawingNumber = Integer.valueOf(mapValues.get(c.getDRAWING_NUMBER_KEY()));
		int partNumber = Integer.valueOf(mapValues.get(c.getPART_NUMBER_KEY()));
		
		project = projectRep.findByReference(projectRef);
		
		if(project == null) {
			logger.info("The project '" + qrCode + "' does not exist and will be created.");
			newProject = true;
			project = FactoryProject.getProject(mapValues);
			drawing = project.getDrawingRefs().get(0);
		} else {
			logger.info("The project '" + qrCode + "' does exist.");
			newProject = false;
			drawing = project.getDrawingRefs().stream()
					.filter(d -> d.getDnumber() == drawingNumber)
					.findFirst().orElse(null);
			if(drawing == null) {
				logger.info("The drawing " + drawingNumber + " does not exist and will be created.");
				drawing = new DrawingRef();
				drawing.setDnumber(drawingNumber);
				drawing.setParent(project);
			}
			Part p = drawing.getParts().stream().filter(part -> part.getNumber() == partNumber).findFirst().orElse(null);
			if(p == null) {
				logger.info("The part " + partNumber + " does not exist and will be created.");
				p = new Part();
				p.setNumber(partNumber);
				p.setParent(drawing);
				drawing.getParts().add(p);
			}
		}
		
		helper.handleReports(drawing, mapValues, false);
		
		if(newProject) {
			project = projectRep.save(project);
			saveResult = project != null;
		} else {
			drawing = drawingRep.save(drawing);
			saveResult = drawing != null;
		}

		if(saveResult) {
			// filter reports by drawing number
			childFilter(project, drawingNumber, partNumber);
			return new SingleObjectResult<>(Project.class, project);
		} else {
			return new ErrorResult(ErrorCode.ENTITY_NOT_FOUND, "Project creation failed!", ErrorLevel.SEVERE, getClass());
		}
	}
	
	@Transactional(readOnly=true)
	private void childFilter(Project project, long drawingNumber, long partNumber) {
		DrawingRef dRef = project.getDrawingRefs().stream().filter(d -> d.getDnumber() == drawingNumber).findFirst().orElse(null);
		if(dRef != null) {
			Part pt = dRef.getParts().stream().filter(p -> p.getNumber() == partNumber).findFirst().orElse(null);
			List<Report> rList = dRef.getReports().stream()
					.filter(r -> r.getParent().getDnumber() == drawingNumber)
					.collect(Collectors.toList());
			if(rList.size() > 0 && pt != null) {
				dRef.setParts(Collections.singletonList(pt));
				dRef.setReports(rList);
				project.setDrawingRefs(Collections.singletonList(dRef));
			}
		}
	}
	
}
