package com.richard.weger.wqc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.helper.ItemReportHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.ExportService;
import com.richard.weger.wqc.service.ProjectService;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

@Service
public class WorkbookHandler {
	
	@Autowired ItemReportHelper helper;
	@Autowired ProjectService projectService;
	@Autowired ExportService exportService;
	
	Logger logger;
	
	public WorkbookHandler() {
		logger = Logger.getLogger(getClass());
	}

	static AppConstants consts = FactoryAppConstants.getAppConstants();

	public AbstractResult handleWorkbook(ItemReport report, String username) {
		FileHandler fileHandler = new FileHandler();
		Workbook workbook;
		WritableWorkbook finalWorkbook;
		WritableSheet writableSheet;
		WorkbookSettings ws;
		InputStream inputStream = null;
		OutputStream outputStream;
		String outputFileName;
		File outputFile;
		AbstractResult res;
		
		Project project = report.getParent().getParent();

		ws = new WorkbookSettings();
		ws.setEncoding("CP1250");

		try {
			inputStream = new FileInputStream(fileHandler.getResourcesFile(consts.getCONTROLCARDREPORT_FILENAME()));
		} catch (FileNotFoundException e1) {
			String message = "The base xls file for 'Kontrollkarte' was not found!";
			logger.fatal(message, e1);
			return new ErrorResult(ErrorCode.BASE_FILE_RETRIEVAL_FAILED, message, ErrorLevel.SEVERE, getClass());
		}

		outputFileName = exportService.getReportsFolderPath(project);
		res = helper.getXlsFileName(report);
		if(res instanceof SuccessResult) {
			String sRes = ResultService.getSingleResult(res);
			outputFileName = outputFileName.concat(sRes.replaceAll(".xls", "_tmp.xls"));
		} else {
			try {
				inputStream.close();
			} catch (Exception ex) {
				logger.warn("Failed to close the input stream of the 'Kontrollkarte' base file!", ex);
			}
			return res;
		}

		outputFile = new File(outputFileName);
		outputFile.getParentFile().mkdirs();
		outputStream = null;
		try {
			outputStream = new FileOutputStream(outputFile);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, read);
			}
		} catch (IOException e) {
			String message = "Failed to read from the 'Kontrollkarte' base file!";
			logger.fatal(message, e);
			return new ErrorResult(ErrorCode.BASE_FILE_IO_FAILED, message, ErrorLevel.SEVERE, getClass());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.warn("Failed to close the input stream of the 'Kontrollkarte' base file!", e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.warn("Failed to close the output stream of the 'Kontrollkarte' base file!", e);
				}
			}
		}

		try {
			workbook = Workbook.getWorkbook(outputFile, ws);

			File fWork = new File(outputFileName.replace("_tmp", ""));
			try {
				finalWorkbook = Workbook.createWorkbook(fWork, workbook);
				writableSheet = finalWorkbook.getSheet(0);
				writeData(writableSheet, report, project, username);
				try {
					finalWorkbook.write();
				} catch (Exception e) {
					String message = "Failed to write to the 'Kontrollkarte' output file!";
					logger.fatal(message, e);
					return new ErrorResult(ErrorCode.WRITE_OPERATION_FAILED, message, ErrorLevel.SEVERE, getClass());
				}
				finalWorkbook.close();
				workbook.close();
			} catch (FileNotFoundException ex) {
				String message = "Unable to proccess file ".concat(outputFileName)
						.concat(" because it is beign used by another proccess");
				logger.fatal(message, ex);
				return new ErrorResult(ErrorCode.WRITE_OPERATION_FAILED, message, ErrorLevel.SEVERE, getClass());
			}
		} catch (IOException | BiffException | WriteException e) {
			String message = "General failure when trying to write to the 'Kontrollkarte' output file!";
			logger.fatal(message, e);
			return new ErrorResult(ErrorCode.WRITE_OPERATION_FAILED, message, ErrorLevel.SEVERE, getClass());
		}
		outputFile.delete();
		return null;
	}

	private static void writeData(WritableSheet writableSheet, ItemReport report, Project project, String username) {
		int actualLine = consts.getCCRF_FIRSTLINE();
		WritableCellFormat cellFormat;
		WritableCellFormat cellFormat2;
		Label label;

		try {
			for (Item item : report.getItems()) {
				int col = 0;

				cellFormat = new WritableCellFormat();
				cellFormat2 = new WritableCellFormat();

				if (item.getStatus() == consts.getITEM_APROVED_KEY()) {
					col = consts.getCCRF_IO();
				} else if (item.getStatus() == consts.getITEM_NOT_APROVED_KEY()) {
					col = consts.getCCRF_NIO();
				} else if (item.getStatus() == consts.getITEM_NOT_APLICABLE_KEY()) {
					col = consts.getCCRF_NA();
				} else {
					col = 0;
				}

				cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
				cellFormat.setAlignment(Alignment.LEFT);
				cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

				label = new Label(consts.getCCRF_COMMENTS(), actualLine, item.getComments(), cellFormat);
				writableSheet.addCell(label);

				if (col > 0) {
					cellFormat2.setBorder(Border.ALL, BorderLineStyle.THIN);
					cellFormat2.setAlignment(Alignment.CENTRE);
					cellFormat2.setVerticalAlignment(VerticalAlignment.CENTRE);
					label = new Label(col, actualLine, "x", cellFormat2);
					writableSheet.addCell(label);
				}

				for (Integer i : consts.getCCRF_JUMPONEMORE()) {
					if (actualLine == i)
						actualLine++;
				}
				actualLine++;
			}
			// Client name
			cellFormat = new WritableCellFormat();
			cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
			label = new Label(consts.getCCRF_CLIENTCOLUMN(), consts.getCCRF_CLIENTROW(), report.getClient(),
					cellFormat);
			writableSheet.addCell(label);

			// Project number
			cellFormat = new WritableCellFormat();
			cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
			label = new Label(consts.getCCRF_COMMISSIONCOLUMN(), consts.getCCRF_COMMISSIONROW(), project.getReference(),
					cellFormat);
			writableSheet.addCell(label);

			// Drawing number
			label = new Label(consts.getCCRF_DRAWINGCOLUMN(), consts.getCCRF_DRAWINGROW(),
					String.valueOf(project.getDrawingRefs().get(0).getDnumber()), cellFormat);
			writableSheet.addCell(label);

			// Part number
			label = new Label(consts.getCCRF_PARTCOLUMN(), consts.getCCRF_PARTROW(),
					String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber()), cellFormat);
			writableSheet.addCell(label);

			// Report comments
			cellFormat = new WritableCellFormat();
			label = new Label(consts.getCCRF_REPORTCOMMENTSCOLUMN(), consts.getCCRF_REPORTCOMMENTSROW(),
					report.getComments(), cellFormat);
			writableSheet.addCell(label);

			// Report date
			cellFormat = new WritableCellFormat();
			cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
			// String sDate = DateFormat.getDateInstance().format(report.getDate());
			SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy");
			Date date = report.getLastModifiedDate();
			if(date == null) {
				date = new Date();
			}
			String sDate = sfd.format(date);
			label = new Label(consts.getCCRF_REPORTDATECOLUMN(), consts.getCCRF_REPORTDATEROW(), sDate, cellFormat);
			writableSheet.addCell(label);

			// Report responsible
			cellFormat = new WritableCellFormat();
			cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
			label = new Label(consts.getCCRF_REPORTRESPONSIBLECOLUMN(), consts.getCCRF_REPORTRESPONSIBLEROW(), username,
					cellFormat);
			writableSheet.addCell(label);

			label = new Label(consts.getCCRF_PARTCOLUMN(), consts.getCCRF_PARTROW(),
					String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber()), cellFormat);
			writableSheet.addCell(label);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
}
