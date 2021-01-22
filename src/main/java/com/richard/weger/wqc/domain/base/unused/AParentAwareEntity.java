package com.richard.weger.wqc.domain.base.unused;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.richard.weger.wqc.domain.AuditableEntity;
import com.richard.weger.wqc.domain.DomainEntity;

@SuppressWarnings("serial")
//@EntityListeners(ParentAwareEntityListener.class)
public abstract class AParentAwareEntity<P extends DomainEntity> extends AuditableEntity implements IParentAwareEntity<P> {
	
	@ManyToOne
	@JsonIgnore
	private P parent;
	
	public AParentAwareEntity(Class<P> parentClz) {
		try {
			parent = parentClz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}		
	}

	@JsonIgnore
	public P getParent() {
		return parent;
	}

	@JsonIgnore
	public void setParent(P parent) {
		this.parent = parent;
	}
}
