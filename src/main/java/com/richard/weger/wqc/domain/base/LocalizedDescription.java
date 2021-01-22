package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.richard.weger.wqc.domain.ParentAwareEntity;

@Entity
public class LocalizedDescription extends ParentAwareEntity {

	private String description;
			
	@JsonIgnore
	@ManyToOne
	private Language language;

	public LocalizedDescription() {
		setParent(new ItemModel());
	}

	public Language getLanguage() {
		return language;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}

	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {}
	
}
