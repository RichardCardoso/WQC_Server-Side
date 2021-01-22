package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.Entity;

import com.richard.weger.wqc.domain.ParentAwareEntity;

@Entity
public class PrefixBasedCheckReport extends ACheckReportModel {

	public PrefixBasedCheckReport() {
		super();
	}

	private String prefix;
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}

	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {}

}
