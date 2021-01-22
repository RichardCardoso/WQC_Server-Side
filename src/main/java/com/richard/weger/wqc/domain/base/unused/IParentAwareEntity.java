package com.richard.weger.wqc.domain.base.unused;

import com.richard.weger.wqc.domain.DomainEntity;

public interface IParentAwareEntity<P extends DomainEntity> {
	public P getParent();
	public void setParent(P parent);
}
