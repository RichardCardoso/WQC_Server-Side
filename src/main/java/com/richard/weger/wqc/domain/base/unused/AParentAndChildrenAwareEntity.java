package com.richard.weger.wqc.domain.base.unused;

import java.util.ArrayList;
import java.util.List;

import com.richard.weger.wqc.domain.AuditableEntity;
import com.richard.weger.wqc.domain.DomainEntity;

public class AParentAndChildrenAwareEntity<P extends DomainEntity, C extends DomainEntity> extends AuditableEntity implements IChildAwareEntity<C>, IParentAwareEntity<P> {
	
	private P parent;
	private List<C> children = new ArrayList<>();
	
	public AParentAndChildrenAwareEntity(Class<P> parentClz) {
		try {
			parent = parentClz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public P getParent() {
		return parent;
	}
	
	@Override
	public void setParent(P parent) {
		this.parent = parent;
	}
	
	@Override
	public List<C> getChildren() {
		return children;
	}
	
	@Override
	public void setChildren(List<C> children) {
		this.children = children;
	}
	

}
