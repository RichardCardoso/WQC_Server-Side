package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.richard.weger.wqc.domain.AuditableEntity;
import com.richard.weger.wqc.domain.Translation;

import lombok.Data;

@Entity @Data
public class ItemModel extends AuditableEntity {
	
	@JsonIgnore
	@ManyToOne
	private ItemReportModel report;
		
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval = true)
	private List<Translation> descriptions;
	
}
