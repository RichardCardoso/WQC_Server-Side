package com.richard.weger.wqc.listener;

import java.io.IOException;

import javax.persistence.PostLoad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.helper.ProjectHelper;

public class DomainEntityListener {

	@PostLoad
	public void postload(DomainEntity entity) {
		ObjectMapper mapper = new ObjectMapper();
		String sEntity;
		try {
			sEntity = mapper.writeValueAsString(entity);
			DomainEntity savedEntity = mapper.readValue(sEntity, entity.getClass());
			if(savedEntity instanceof ParentAwareEntity) {
				ParentAwareEntity e = (ParentAwareEntity) savedEntity;
				DomainEntity parent = ((ParentAwareEntity) savedEntity).getParent();
				ProjectHelper.linkReferences(parent, e);
			}
			entity.setSavedState(savedEntity);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}
