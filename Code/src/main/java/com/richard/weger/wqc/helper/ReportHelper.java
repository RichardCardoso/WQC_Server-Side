package com.richard.weger.wqc.helper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.BaseCheckReport;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.util.PdfHandler;

@Service
public class ReportHelper {
	
	@Autowired ParamConfigurationsRepository rep;
	Logger logger;
	
	public ReportHelper() {
		logger = Logger.getLogger(ParamConfigurationsRepository.class);
	}

	public boolean handleReports(DrawingRef drawing, Map<String, String> mapValues, boolean forceCreation) {
		long zeroPagesReportsCount;
		long oldRepCount;
		zeroPagesReportsCount = getZeroPagesReportsCount(drawing);
		oldRepCount = getReportsCount(drawing);
		if (forceCreation || oldRepCount == 0 || zeroPagesReportsCount > 0)
		{
			createReportsList(drawing, mapValues);
		}
		drawing.getReports().stream().filter(r -> r instanceof CheckReport).map(r -> (CheckReport) r).forEach(r -> {
			r.getPages().forEach(p -> p.getMarks().stream().count());
		});
		return oldRepCount != getReportsCount(drawing);
	}
	
	private long getZeroPagesReportsCount(DrawingRef drawing) {
		long count = 0;
		
		try {
			count = drawing.getReports().stream()
				.filter(r -> r instanceof CheckReport && r != null)
				.map(r -> (CheckReport) r)
				.filter(r -> r.getPagesCount() == 0)
				.count();
		} catch (Exception ex) {
			logger.warn("Error while trying to get zero pages reports count!", ex);
		}
		
		return count;
	}

	private void createReportsList(DrawingRef drawing, Map<String, String> mapValues) {
		ParamConfigurations conf;
		List<File> searchFolders;
		File[] fList;

		conf = rep.getDefaultConfig();
		searchFolders = Arrays.asList(
			new File(getCommonPathLocation(mapValues))
		);

		if(drawing.getReports().stream().filter(r -> r instanceof ItemReport).count() == 0) {
			// adiciona um relatório do tipo "ItemReport"
			ItemReport ir = new ItemReport();
			ir.setParent(drawing);
			ir.setReference("0000");
			drawing.getReports().add(ir);
			ir.setItems((new ItemReportHelper()).getDefaultItems(ir));
			ir.getItems().stream().forEach(i -> i.setParent(ir));
		}

		for (File projectFolder : searchFolders) {
			if (projectFolder.exists()) {
				// lista os arquivos da pasta do projeto
				fList = projectFolder.listFiles();
	
				// itera para selecionar cada um dos tipos de relatório utilizados
				for (BaseCheckReport b : conf.getBaseCheckReports()) {
					String strCode;
					String strExtension = conf.getOriginalDocsExtension();
					int codeLen;
					int extLen;
					strCode = b.getCode();
					codeLen = strCode.length();
					extLen = strExtension.length();
					
					for (File f : fList) {
						String sWork = fileHandled(drawing, strCode, codeLen, extLen, strExtension, f);
						if(sWork != null)
							logger.warn(sWork);
					}
				}
			} else {
				logger.warn("This project's folder (" + projectFolder + ") wasn't found at the server!");
			}
		}
	}

	private String fileHandled(DrawingRef drawing, String strCode, int codeLen, int extLen, String strExtension, File f) {
		String sWork = null;
		if (f.isDirectory() && !f.getName().equals("Qualitaetskontrolle")) {
			for(File file : f.listFiles()) {
				sWork = fileHandled(drawing, strCode, codeLen, extLen, strExtension, file);
				if(sWork != null) {
					return sWork;
				}
			}
		}
		
		if (f.getName().endsWith("-Q.pdf")) {
			return null;
		}
		
		String fileCode, fileExt, dNumberRef;
		try {
			fileCode = f.getName().substring(0, codeLen);
			fileExt = f.getName().substring(f.getName().length() - extLen, f.getName().length());
			dNumberRef = strCode.concat("Z").concat(String.valueOf(drawing.getDnumber()));
		} catch (Exception ex) {
			sWork = "Skipping file '" + f.getName() + "' due to being unable of classifying it as a report!";
			return sWork;
		}
		
		if (fileCode.equals(strCode) && fileExt.equals(strExtension) && f.getName().contains(dNumberRef + "-")) {
			CheckReport report = drawing.getReports().stream()
					.filter(r -> r instanceof CheckReport && r != null)
					.map(r -> (CheckReport) r)
					.filter(r -> r.getFileName().toLowerCase().equals(f.getName().toLowerCase()))
					.filter(r -> r.getPagesCount() > 0)
					.findFirst()
					.orElse(null);
			boolean mustAdd = false;
			if(report == null) {
				report = new CheckReport();
				mustAdd = true;
			}
			int pagesCount = (new PdfHandler()).getPagesCount(f.getPath());
			if (pagesCount == 0) {
				logger.warn("A report pdf file is invalid or corrupted (" + f.getName() + ")");
			} else if (report.getPages().size() < pagesCount) {
				int missing =  Math.abs(pagesCount - report.getPages().size());
				for (int j = 0; j < missing; j++) {
					report.addBlankPage().setParent(report);
				}
			}
			report.setReference(strCode);
			report.setFileName(f.getName());
			report.setParent(drawing);
			if(mustAdd) {
				drawing.getReports().add(report);
			}
		}
		return null;
	}

	public int getCheckReportsPagesCount(Project p) {
		int cnt = 0;
		if (p != null) {
			for (DrawingRef d : p.getDrawingRefs()) {
				for (Report r : d.getReports()) {
					if (r instanceof CheckReport) {
						CheckReport c = (CheckReport) r;
						cnt += c.getPagesCount();
					}
				}
			}
		}
		return cnt;
	}

	public long getReportsCount(DrawingRef d) {
		long cnt = 0;
		if(d.getReports() != null) {
			cnt = d.getReports().stream().filter(r -> r instanceof CheckReport).count();
		}
		return cnt;
	}
	
	public String getCommonPathLocation(Map<String, String> mapValues) {
		
		ParamConfigurations conf = rep.getDefaultConfig();
		AppConstants appConstants = FactoryAppConstants.getAppConstants();
		String ret;
		ret = conf.getServerPath() + conf.getRootPath() + mapValues.get(appConstants.getCOMMON_PATH_KEY());
		return ret;
	}

	
	public String getConstructionPathLocation(Map<String, String> mapValues) {
		
		ParamConfigurations conf = rep.getDefaultConfig();
		AppConstants appConstants = FactoryAppConstants.getAppConstants();
		String ret;
		ret = conf.getServerPath() + conf.getRootPath() + mapValues.get(appConstants.getCONSTRUCTION_PATH_KEY());
		return ret;
	}

	public String getTechnicalPathLocation(Map<String, String> mapValues) {
		
		ParamConfigurations conf = rep.getDefaultConfig();
		AppConstants appConstants = FactoryAppConstants.getAppConstants();
		String ret;
		ret = conf.getServerPath() + conf.getRootPath() + mapValues.get(appConstants.getTECHNICAL_PATH_KEY());
		return ret;
	}
}
