package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.Entity;

import com.richard.weger.wqc.domain.ParentAwareEntity;

@Entity
public class DirectoryBasedCheckReport extends ACheckReportModel {
	
	private String relativeSearchPath;

	public String getRelativeSearchPath() {
		return relativeSearchPath;
	}

	public void setRelativeSearchPath(String relativeSearchPath) {
		this.relativeSearchPath = relativeSearchPath;
	}

	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}

	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {}

}
