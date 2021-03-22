package com.richard.weger.wqc.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="generalpictures")
public class Picture extends ParentAwareEntity {
	
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}
	
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		
	}
	
	public Picture() {
		this.caption = "";
		this.fileName = "";
	}

	private String caption;

	private String fileName;
	
	@JsonIgnore
	@OneToOne(mappedBy = "picture")
	@JoinColumn(name = "picture_item")
	private Item item;
	
	
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String filePath) {
		this.fileName = filePath;
	}

	public Item getItem() {
		return item;
	}

	public void setDrawingref(Item item) {
		this.item = item;
	}

}
