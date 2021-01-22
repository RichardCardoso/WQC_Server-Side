package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.richard.weger.wqc.domain.ParentAwareEntity;

@Entity
public class ProjectModel extends ParentAwareEntity {

	private String reference;
	
	@ManyToOne
	private Language language;
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy = "parent", orphanRemoval = true)
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

	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getReports();
	}

	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setReports((List<AReportModel>) children);		
	}
		
}
