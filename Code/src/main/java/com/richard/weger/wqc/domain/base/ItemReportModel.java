package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.richard.weger.wqc.domain.ParentAwareEntity;

@Entity
public class ItemReportModel extends AReportModel {
	
	@OneToMany
	private List<ItemModel> items;

	public List<ItemModel> getItems() {
		return items;
	}

	public void setItems(List<ItemModel> items) {
		this.items = items;
	}

	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getItems();
	}

	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setItems((List<ItemModel>) children);
	}


}

