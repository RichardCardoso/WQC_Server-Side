package com.richard.weger.wqc.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;

@SuppressWarnings("deprecation")
@Service
public class CheckReportService {
	
	@Autowired private ParamConfigurationsRepository rep;
	@Autowired private QrTextHandler handler;

	public int radius = 6;
	AppConstants consts = FactoryAppConstants.getAppConstants();
	
    /* Constants form itext5 */
    public static final PdfNumber INVERTEDPORTRAIT = new PdfNumber(180);
    public static final PdfNumber LANDSCAPE = new PdfNumber(90);
    public static final PdfNumber PORTRAIT = new PdfNumber(0);
    public static final PdfNumber SEASCAPE = new PdfNumber(270);

	public String bitmap2Pdf(CheckReport report) {
		ParamConfigurations conf = rep.getDefaultConfig();
		
		Map<String, String> mapValues = handler.getParameters(report.getParent().getParent());

		String originalFilePath = conf.getServerPath().concat(conf.getRootPath())
				.concat(mapValues.get(consts.getTECHNICAL_PATH_KEY())).concat(report.getFileName());
		String destinyFilePath = conf.getServerPath().concat(conf.getRootPath())
				.concat(mapValues.get(consts.getCOMMON_PATH_KEY())).concat("/Technik/Qualitaetskontrolle/")
				.concat(report.getFileName().replaceAll(".pdf", "-Q".concat(".pdf")));
		
		(new File(destinyFilePath)).getParentFile().mkdirs();

		try {
			PdfReader reader = new PdfReader(originalFilePath);
			PdfWriter writer = new PdfWriter(destinyFilePath);			
			PdfDocument document = new PdfDocument(reader, writer);
			Document doc = new Document(document);
			
//			PageOrientationEventHandler ev = new PageOrientationEventHandler();
//			document.addEventHandler(PdfDocumentEvent.START_PAGE, ev);
//			Document doc = new Document(document);

			for (int j = 0; j < report.getPagesCount(); j++) {
				Page reportPage = report.getPages().get(j);
				List<Mark> markList = reportPage.getMarks();

				PdfPage pdfPage = document.getPage(j + 1);
				PdfCanvas canvas = new PdfCanvas(pdfPage);
				PdfFont font = PdfFontFactory.createFont(FontConstants.HELVETICA);		
				
				float pageWidth = pdfPage.getPageSizeWithRotation().getWidth(),
						pageHeight = pdfPage.getPageSizeWithRotation().getHeight();
				
				PdfNumber rotate = pdfPage.getPdfObject().getAsNumber(PdfName.Rotate);
				
//				ev.setOrientation(LANDSCAPE);

				for (int i = 0; i < markList.size(); i++) {
					Mark mark = markList.get(i);
					String text = mark.getRoleToShow();
					double x, y, angle;				
			
					if(rotate != null && rotate.getValue() > 0) {
						pdfPage.setRotation(0);
						
						pageWidth = pdfPage.getPageSizeWithRotation().getWidth();
						pageHeight = pdfPage.getPageSizeWithRotation().getHeight();

						x = pageWidth - (mark.getY()) * pageWidth;
						y = pageHeight - (mark.getX()) * pageHeight;
						angle = rotate.getValue();
					} else {
						x = mark.getX() * pageWidth;
						y = pageHeight - (mark.getY() * pageHeight);
						angle = 0;
					}
											
					canvas.setColor(ColorConstants.RED, true);
					canvas.circle(x, y, radius);
					canvas.fill();
					
					doc.showTextAligned(new Paragraph(text).setFontColor(ColorConstants.YELLOW).setFont(font).setFontSize(radius), 
							(float) x, (float) y, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, (float) (angle * (Math.PI / 180)));
										
					if(rotate != null && rotate.getValue() > 0) {
						pdfPage.setRotation((int)rotate.getValue());
					}
					
				}
			}
			document.close();
			if(doc != null) {
				doc.close();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
}
