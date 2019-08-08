package com.richard.weger.wqc.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
//@EntityListeners(ReportListener.class)
@Table(name = "reports")
public abstract class Report extends ParentAwareEntity {
	
	protected Report() {
		this.reference = "";
		setParent(new DrawingRef());
	}
	
	private String reference;
	
	private boolean finished;
	
	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public DrawingRef getParent() {
		return super.getParent();
	}

//	public String getType() {
//		return type;
//	}
//
//	/**
//	 * Set the report's type
//	 * 
//	 * @param type
//	 *            Report's type
//	 *            0 = none, 1 = ItemsReport, 2 = CheckReport, 3 = AutomaticReport
//	 */
//	public void setType(String type) {
//		this.type = type;
//	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

}
