package com.richard.weger.wqc.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("serial")
@Entity
//@EntityListeners(ParentAwareEntityListener.class)
public abstract class ParentAwareEntity extends AuditableEntity {
	
	@ManyToOne
	@JsonIgnore
	private DomainEntity parent;
	
	public abstract <T extends ParentAwareEntity> List<T> getChildren();
	public abstract <T extends ParentAwareEntity> void setChildren(List<T> children);

	@JsonIgnore
	@SuppressWarnings("unchecked")
	public <T extends DomainEntity> T getParent(Class<T> clz) {
		return (T) parent;
	}

	@JsonIgnore
	public <T extends DomainEntity>  void setParent(T parent) {
		this.parent = parent;
	}
}
