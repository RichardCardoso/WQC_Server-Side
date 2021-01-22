package com.richard.weger.wqc.domain.base.unused;

import java.util.List;

import com.richard.weger.wqc.domain.AuditableEntity;
import com.richard.weger.wqc.domain.DomainEntity;

public abstract class AChildAwareEntity<C extends DomainEntity> extends AuditableEntity implements IChildAwareEntity<C> {
	
	private List<C> children;

	@Override
	public List<C> getChildren() {
		return children;
	}

	@Override
	public void setChildren(List<C> children) {
		this.children = children;
	}

}
