package com.richard.weger.wqc.domain.base;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.richard.weger.wqc.domain.AuditableEntity;
import com.richard.weger.wqc.domain.Translation;

@Entity
public abstract class AReportModel extends AuditableEntity {

	@OneToMany
	private List<Translation> descriptions = new ArrayList<>();

	public List<Translation> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<Translation> descriptions) {
		this.descriptions = descriptions;
	}

}
