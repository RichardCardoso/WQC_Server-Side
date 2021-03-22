package com.richard.weger.wqc.domain;

import javax.persistence.Entity;

@Entity
public class Role extends DomainEntity {
	
	private String description;
	private String comments;

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String type) {
		this.description = type;
	}

}
