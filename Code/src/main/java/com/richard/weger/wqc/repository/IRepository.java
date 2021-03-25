package com.richard.weger.wqc.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.richard.weger.wqc.domain.DomainEntity;

public interface IRepository <T extends DomainEntity> extends PagingAndSortingRepository<T, Long> {
	
	public T getById(Long Id);
	public List<T> findAll();
}
