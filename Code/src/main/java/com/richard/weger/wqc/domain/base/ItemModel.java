package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.richard.weger.wqc.domain.ParentAwareEntity;

@Entity
public class ItemModel extends ParentAwareEntity {
	
	@JsonIgnore
	@ManyToOne
	private ItemReportModel report;
		
	@OneToMany(cascade=CascadeType.ALL, mappedBy = "parent", orphanRemoval = true)
	private List<LocalizedDescription> descriptions;
	
	public ItemModel() {
		setParent(new ItemReportModel());
	}

	public List<LocalizedDescription> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<LocalizedDescription> descriptions) {
		this.descriptions = descriptions;
	}

	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getDescriptions();
	}

	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setDescriptions((List<LocalizedDescription>) children);
	}
	
}
