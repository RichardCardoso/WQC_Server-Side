package com.richard.weger.wqc.domain.base;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.richard.weger.wqc.domain.ParentAwareEntity;

@Entity
public abstract class AReportModel extends ParentAwareEntity {

	@OneToMany
	private List<LocalizedDescription> descriptions = new ArrayList<>();

	public List<LocalizedDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<LocalizedDescription> descriptions) {
		this.descriptions = descriptions;
	}

	public AReportModel() {
		setParent(new ProjectModel());
	}

}
