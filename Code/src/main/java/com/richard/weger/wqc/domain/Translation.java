package com.richard.weger.wqc.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data @Entity @AllArgsConstructor @NoArgsConstructor @Accessors(chain = true)
public class Translation extends AuditableEntity {

	@ManyToOne
	private Language language;
	
	private String value;

	
}
