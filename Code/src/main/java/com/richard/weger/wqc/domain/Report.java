package com.richard.weger.wqc.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Data;

@SuppressWarnings("serial")
@Entity
//@EntityListeners(ReportListener.class)
@Table(name = "reports")
@Data
public abstract class Report extends ParentAwareEntity {
	
	protected Report() {
		this.reference = "";
		setParent(new DrawingRef());
	}
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private ReportLock lock;
	
	private String reference;
	
	@Lob
	private String comments;

	private boolean finished;

	public DrawingRef getParent() {
		return super.getParent(DrawingRef.class);
	}

}
