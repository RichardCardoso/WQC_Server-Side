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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.helper.ItemReportHelper;
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

	static AppConstants consts = FactoryAppConstants.getAppConstants();

	public String handleWorkbook(ItemReport report) {
		FileHandler fileHandler = new FileHandler();
		Workbook workbook;
		WritableWorkbook finalWorkbook;
		WritableSheet writableSheet;
		WorkbookSettings ws;
		InputStream inputStream = null;
		OutputStream outputStream;
		String outputFileName;
		File outputFile;
		
		Project project = report.getParent().getParent();
		String username = report.getLastModifiedBy();
		if(username != null) {
			username = username.substring(0, report.getLastModifiedBy().indexOf(" ("));
		} else {
			username = "PREVIEW-SYSTEM";
		}

		ws = new WorkbookSettings();
		ws.setEncoding("CP1250");

		try {
			inputStream = new FileInputStream(fileHandler.getResourcesFile(consts.getCONTROLCARDREPORT_FILENAME()));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return e1.getMessage();
		}

		outputFileName = projectService.getReportsFolderPath(project);
		outputFileName = outputFileName.concat(helper.getXlsFileName(report).replaceAll(".xls", "_tmp.xls"));

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
			e.printStackTrace();
			return e.getMessage();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					return e.getMessage();
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					return e.getMessage();
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
					e.printStackTrace();
					return e.getMessage();
				}
				finalWorkbook.close();
				workbook.close();
			} catch (FileNotFoundException ex) {
				String error = "Unable to proccess file ".concat(outputFileName)
						.concat(" because its beign used by another proccess");
				System.out.println(error);
				return error;
			}
		} catch (IOException | BiffException | WriteException e) {
			e.printStackTrace();
			return e.getMessage();
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
