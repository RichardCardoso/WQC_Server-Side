package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.richard.weger.wqc.domain.AuditableEntity;
import com.richard.weger.wqc.domain.Language;

@Entity 
public class ProjectModel extends AuditableEntity {

	private String reference;
	
	@ManyToOne
	private Language language;
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval = true)
	private List<AReportModel> reports;

	public List<AReportModel> getReports() {
		return reports;
	}

	public void setReports(List<AReportModel> reports) {
		this.reports = reports;
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
		
}
