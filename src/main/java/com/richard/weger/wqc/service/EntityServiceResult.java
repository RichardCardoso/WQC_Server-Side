package com.richard.weger.wqc.service;

import com.richard.weger.wqc.domain.DomainEntity;

public class EntityServiceResult<T extends DomainEntity> {
	
	private String message;
	private T entity;
	private boolean existingEntity;
	
	public boolean isExistingEntity() {
		return existingEntity;
	}
	public void setExistingEntity(boolean existingEntity) {
		this.existingEntity = existingEntity;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public T getEntity() {
		return entity;
	}
	public void setEntity(T entity) {
		this.entity = entity;
	}
	
}
