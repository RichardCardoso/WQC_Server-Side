package com.richard.weger.wqc.domain.base;

import javax.persistence.Entity;

import com.richard.weger.wqc.domain.DomainEntity;

@Entity
public class Language extends DomainEntity {
	
	private String reference;
	
	public Language() {
		super();
	}

	public Language(String reference) {
		super();
		this.reference = reference;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
}
