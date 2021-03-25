package com.richard.weger.wqc.domain;

import javax.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data @AllArgsConstructor @NoArgsConstructor
public class Language extends AuditableEntity {
	
	private String reference;
	private String description;

}
