package com.richard.weger.wqc.repository;

import java.util.List;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.exception.CrudException;

public interface DomainEntityRepository extends IRepository<DomainEntity> {
	public <T extends DomainEntity> List<T> getAllByType(String type);
	public <T extends DomainEntity> List<T> getAllByTypeStartingWith(String type);
	
	@Override
	public <T extends DomainEntity> T save(T entity) throws CrudException;
	
	@Override
	public void deleteById(Long id) throws CrudException;
}
