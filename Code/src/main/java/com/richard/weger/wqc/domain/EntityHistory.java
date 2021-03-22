package com.richard.weger.wqc.domain;

import javax.persistence.Entity;

@Entity
public class EntityHistory extends AuditableEntity {
	
	public EntityHistory(String event) {
		this.event = event;
	}
	
	private String event;

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}
	
}
