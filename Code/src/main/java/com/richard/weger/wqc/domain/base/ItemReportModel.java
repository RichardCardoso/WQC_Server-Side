package com.richard.weger.wqc.domain.base;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

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

}

