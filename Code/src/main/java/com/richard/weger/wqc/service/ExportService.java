package com.richard.weger.wqc.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.richard.weger.wqc.ReportExportDTO;
import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.CheckReportHelper;
import com.richard.weger.wqc.helper.ItemReportHelper;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.repository.ReportRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SingleObjectResult;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.util.WorkbookHandler;

@Service
public class ExportService {
	
	@Autowired private ItemReportHelper irHelper;
	@Autowired private ReportRepository reportRep;
	
	@Autowired private WorkbookHandler wbHandler;
	
	@Autowired private CheckReportHelper checkReportHelper;
	
	@Autowired private QrTextHandler qrHandler;
	
	@Autowired private ParamConfigurationsRepository paramConfigsRep;
	
	AppConstants c;
	
	public ExportService() {
		 c = FactoryAppConstants.getAppConstants();
	}
	
	public String getReportsFolderPath(Project project) {
		Map<String, String> mapValues = qrHandler.getParameters(project);

		String path = paramConfigsRep.getDefaultConfig().getServerPath();
		path = path.concat(paramConfigsRep.getDefaultConfig().getRootPath());
		path = path.concat(mapValues.get(c.getCOMMON_PATH_KEY()));
		path = path.concat("/Technik/Qualitaetskontrolle/");
		return path;
	}
	
	public AbstractResult export(Report report) {
		
		return export(report, "PREVIEW-SYSTEM");
	}
	
	@Transactional(readOnly = true)
	public AbstractResult export(Report report, String deviceid) {
		if(report == null) {
			return new ErrorResult(ErrorCode.NULL_ENTITY_RECEIVED, "Null report type received to export!", ErrorLevel.WARNING, getClass());
		}
		if(report instanceof ItemReport) {
			ItemReport ir = (ItemReport) report;
			return export(ir, deviceid);
		} else if (report instanceof CheckReport) {
			CheckReport cr = (CheckReport) report;
			return export(cr);
		} else {
			return new ErrorResult(ErrorCode.INVALID_REPORT_TYPE, "Unrecognized report type! (" + report.getClass().getSimpleName() + ")", ErrorLevel.WARNING, getClass());
		}
	}
	
	public AbstractResult export(ItemReport report, String deviceid) {
		AbstractResult res;
		res = wbHandler.handleWorkbook(report, deviceid);
		if(res instanceof ErrorResult) {
			return res;
		} else {
			return new EmptyResult();
		}
	}
	
	public AbstractResult export(CheckReport report) {
		String message;
		message = checkReportHelper.bitmap2Pdf(report);
		if(message != null) {
			return new ErrorResult(ErrorCode.ENTITY_EXPORT_FAILED, message, ErrorLevel.WARNING, getClass());
		} else {
			return new EmptyResult();
		}
	}
	
	public String getFilePath(Report report) {
		if (report != null) {
			DrawingRef d = report.getParent();
			Project p = d.getParent();
			if (report instanceof CheckReport) {
				return getReportsFolderPath(p).concat("/")
						.concat(((CheckReport) report).getFileName().replaceAll(".pdf", "-Q.pdf"));
			} else if (report instanceof ItemReport) {
				AbstractResult aRes = irHelper.getXlsFileName((ItemReport) report);
				if(aRes instanceof SuccessResult) {
					String fileName = ResultService.getSingleResult(aRes);
					return getReportsFolderPath(p).concat("/")
							.concat(fileName);
				}
			}
		}
		return null;
	}

	public byte[] getFileInBytes(Report report) {
		String filePath;
		File file;
		FileInputStream fis;
		byte[] result;
		
		filePath = getFilePath(report);
		file = new File(filePath);
		result = new byte[(int) file.length()];
		
		try {
			fis = new FileInputStream(file);
			fis.read(result);
			fis.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public AbstractResult getExportedReport(Long id) {
		Report report;
		ReportExportDTO dto;
		String fileName;
		byte[] content;
		
		report = reportRep.getById(id);

		AbstractResult exportResult = export(report);
		if(exportResult instanceof SuccessResult) {
			
			dto = new ReportExportDTO();
			content = getFileInBytes(report);
			if (content != null) {
				dto.setContent(content);
				
				if (report instanceof CheckReport) {
					fileName = ((CheckReport) report).getFileName();
					fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
					dto.setContentType(MediaType.APPLICATION_PDF_VALUE);
				} else if (report instanceof ItemReport) {
					AbstractResult res = irHelper.getXlsFileName((ItemReport) report); 
					if(res instanceof SuccessResult) {
						fileName = ResultService.getSingleResult(res);
						if (fileName.contains("/"))
							fileName = fileName.substring(fileName.lastIndexOf("/") + 1);	
					} else {
						return new ErrorResult(ErrorCode.REPORTS_FOLDER_PATH_RETRIEVAL_FAILED, "Failed to get reports folder path!", ErrorLevel.WARNING, getClass());
					}
					dto.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				} else {
					return new ErrorResult(ErrorCode.INVALID_REPORT_TYPE, "Unrecognized report type! (" + report.getClass().getSimpleName() + ")",ErrorLevel.WARNING, getClass());
				}
				dto.setFileName(fileName);
				return new SingleObjectResult<>(ReportExportDTO.class, dto);
			} else {
				return new ErrorResult(ErrorCode.INVALID_FILE_CONTENT, "Invalid file content",ErrorLevel.WARNING, getClass());
			}
		} else {
			return exportResult;
		}
	}
	
}
