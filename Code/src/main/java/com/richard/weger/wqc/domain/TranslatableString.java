package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import lombok.Data;

@Data @Entity
public class TranslatableString extends AuditableEntity {

	private boolean locked;
	
	private String code;
	
	@OneToMany
	private List<Translation> translations = new ArrayList<>();
}
