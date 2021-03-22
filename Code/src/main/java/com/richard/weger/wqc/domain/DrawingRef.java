package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints = 
@UniqueConstraint(columnNames= {"id", "dnumber"}, name="UK_DRW_NUMBER")
)
public class DrawingRef extends ParentAwareEntity {
	
	public DrawingRef() {
		this.dnumber = 0;
		this.reports = new ArrayList<Report>();
		this.parts = new ArrayList<Part>();
		setParent(new Project());
	}

	private int dnumber;

	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent", orphanRemoval = true)
	private List<Report> reports;
	
	@OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent", orphanRemoval = true)
	private List<Part> parts;

	@JsonIgnore
	public Project getParent() {
		return super.getParent(Project.class);
	}

	public int getDnumber() {
		return dnumber;
	}

	public void setDnumber(int dnumber) {
		this.dnumber = dnumber;
	}

	public List<Report> getReports() {
		return reports;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	public List<Part> getParts() {
		return parts;
	}

	public void setParts(List<Part> parts) {
		this.parts = parts;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		
		List<T> child = new ArrayList<>();
		child.addAll((Collection<? extends T>) getReports());
		child.addAll((Collection<? extends T>) getParts());
		return child;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setReports((List<Report>) children);
	}

}
