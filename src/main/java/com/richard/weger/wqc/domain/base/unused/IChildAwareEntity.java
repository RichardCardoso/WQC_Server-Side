package com.richard.weger.wqc.domain.base.unused;

import java.util.List;

import com.richard.weger.wqc.domain.DomainEntity;

public interface IChildAwareEntity<C extends DomainEntity> {
	public List<C> getChildren();
	public void setChildren(List<C> children);
}
