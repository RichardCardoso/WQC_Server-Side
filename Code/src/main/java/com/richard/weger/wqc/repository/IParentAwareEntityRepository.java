package com.richard.weger.wqc.repository;

import java.util.List;

import com.richard.weger.wqc.domain.ParentAwareEntity;

public interface IParentAwareEntityRepository <T extends ParentAwareEntity> extends IRepository<T> {
	public List<T> getAllByParentIdAndType(Long id, String type);
	public List<T> getAllByParentIdAndTypeStartingWith(Long id, String type);
	public List<T> getAllByParentId(Long id);
}
