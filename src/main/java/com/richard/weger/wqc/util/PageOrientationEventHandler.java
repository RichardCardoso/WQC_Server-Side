package com.richard.weger.wqc.util;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;

public class PageOrientationEventHandler implements IEventHandler {
	
    /* Constants form itext5 */
    public static final PdfNumber INVERTEDPORTRAIT = new PdfNumber(180);
    public static final PdfNumber LANDSCAPE = new PdfNumber(90);
    public static final PdfNumber PORTRAIT = new PdfNumber(0);
    public static final PdfNumber SEASCAPE = new PdfNumber(270);
	
	protected PdfNumber orientation = PORTRAIT;
	
	public void setOrientation(PdfNumber orientation) {
		this.orientation = orientation;
	}

	@Override
	public void handleEvent(Event event) {
		PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
		docEvent.getPage().put(PdfName.Rotate, orientation);	
	}

}
