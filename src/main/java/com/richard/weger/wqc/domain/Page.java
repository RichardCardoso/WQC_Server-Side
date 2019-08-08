package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="pages")
public class Page extends ParentAwareEntity {
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return (List<T>) getMarks();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		setMarks((List<Mark>) children);
	}
		
	private int number;

	public Page() {
		this.number = 0;
		this.marks = new ArrayList<>();
		setParent(new CheckReport());
	}

	@OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy="parent")
	private List<Mark> marks;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<Mark> getMarks() {
		return marks;
	}

	public void setMarks(List<Mark> mark) {
		this.marks = mark;
	}

}
