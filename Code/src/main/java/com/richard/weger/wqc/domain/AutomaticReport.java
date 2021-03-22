package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "automaticreports")
//@DiscriminatorValue("AutomaticReport")
public class AutomaticReport extends Report {
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getAutomaticItems();
	}

	public AutomaticReport() {
		this.automaticItems = new ArrayList<AutomaticItem>();
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AutomaticItem> automaticItems;

	public List<AutomaticItem> getAutomaticItems() {
		return automaticItems;
	}

	public void setAutomaticItems(List<AutomaticItem> automaticItem) {
		this.automaticItems = automaticItem;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setAutomaticItems((List<AutomaticItem>) children);
	}

}
