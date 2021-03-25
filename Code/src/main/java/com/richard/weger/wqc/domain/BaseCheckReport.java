package com.richard.weger.wqc.domain;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Entity @NoArgsConstructor
//@Table(uniqueConstraints= {@UniqueConstraint(columnNames = {"code"})})
public class BaseCheckReport extends ParentAwareEntity {

	public BaseCheckReport(String code) {
		super();
		this.code = code;
	}
	
	private String code;
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private TranslatableString translatableString;
	
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {

		return null;
	}
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		// TODO Auto-generated method stub
		
	}
}
