package com.richard.weger.wqc.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import lombok.Data;

@Entity @Data
public class ReportLock extends AuditableEntity {

	@OneToOne
	private Device device;
	
	private Date lastPing;
}
