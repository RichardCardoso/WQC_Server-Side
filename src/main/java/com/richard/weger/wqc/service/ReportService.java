package com.richard.weger.wqc.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.ItemReportHelper;
import com.richard.weger.wqc.repository.ReportRepository;

@Service
public class ReportService {
	
	@Autowired private ItemReportHelper helper;
	@Autowired private ReportRepository reportRep;
	
	@Autowired private ProjectService projectService;
	
	public String getFilePath(Report report) {

		if (report != null) {
			
			DrawingRef d = report.getParent();
			Project p = d.getParent();

			if (report instanceof CheckReport) {
				return projectService.getReportsFolderPath(p).concat("/")
						.concat(((CheckReport) report).getFileName().replaceAll(".pdf", "-Q.pdf"));
			} else if (report instanceof ItemReport) {
				return projectService.getReportsFolderPath(p).concat("/")
						.concat(helper.getXlsFileName((ItemReport) report));
			} else {
				return null;
			}
		} else {
			return null;
		}
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

	public void getPreviewFileResponse(HttpServletResponse response, Long reportid) {
		Report report = reportRep.getById(reportid);
		

		if (report != null ) {
			String msg = projectService.export(report);
			if(msg == null) {
				byte[] documentInBytes = getFileInBytes(report);
				if (documentInBytes != null) {
					response.setDateHeader("Expires", -1);
					String fileName = "";
					if (report instanceof CheckReport) {
						fileName = ((CheckReport) report).getFileName();
						fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
						response.setHeader("Content-Disposition", "inline; filename=".concat(fileName));
						response.setContentType("application/pdf");
					} else if (report instanceof ItemReport) {
						fileName = helper.getXlsFileName((ItemReport) report);
						if (fileName.contains("/"))
							fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
						response.setHeader("Content-Disposition", "inline; filename=".concat(fileName));
						response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
					} else {
						return;
					}
					response.setContentLength(documentInBytes.length);
					try {
						response.getOutputStream().write(documentInBytes);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
