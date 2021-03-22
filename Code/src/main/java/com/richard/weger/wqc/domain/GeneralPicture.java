package com.richard.weger.wqc.domain;

//@Entity
//@Table(name="pictures")
@SuppressWarnings("serial")
public class GeneralPicture extends DomainEntity {
		
	public GeneralPicture() {
		this.caption = "";
		this.fileName = "";
	}

	private String caption;

	private String fileName;
	
//	@JsonIgnore
//	@ManyToOne
//	@JoinColumn(name = "generalpicture_drawingref")
	private DrawingRef drawingref;
	
	
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String filePath) {
		this.fileName = filePath;
	}

	public DrawingRef getDrawingref() {
		return drawingref;
	}

	public void setDrawingref(DrawingRef drawingref) {
		this.drawingref = drawingref;
	}

}
