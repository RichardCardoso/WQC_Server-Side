package com.richard.weger.wqc.domain.base;

import javax.persistence.Entity;

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

}
