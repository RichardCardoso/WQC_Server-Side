package com.richard.weger.wqc.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.repository.DeviceRepository;
import com.richard.weger.wqc.repository.DomainEntityRepository;
import com.richard.weger.wqc.repository.ParentAwareEntityRepository;

@Service
public class EntityService {
	
	@Autowired private DomainEntityRepository rep;
	@Autowired private ParentAwareEntityRepository parentRep;
	@Autowired private ProjectService projectService;
	@Autowired private DeviceRepository devRep;
	
	@PersistenceContext
	EntityManager em;
	
	public <T extends DomainEntity> EntityServiceResult<T> postEntity(T entity, Long parentid, String entityName) {
		EntityServiceResult<T> result = new EntityServiceResult<>();
		if(entity.getId() > 0) {
			result.setExistingEntity(true);
			if(entity instanceof ParentAwareEntity) {
				ParentAwareEntity e = (ParentAwareEntity) entity;
				ParentAwareEntity currE = parentRep.getById(entity.getId());
				DomainEntity parent = null;
				if(e.getParent() != null) {
					parent = e.getParent();
					parent.setId(currE.getParent().getId());
				}
				if(currE.getChildren() != null) {
					e.setChildren(currE.getChildren());
				}
				ProjectHelper.linkReferences(parent, e);
			} else {
				DomainEntity currE = rep.getById(entity.getId());
			}
		} else {
			if (entity instanceof ParentAwareEntity) {
				ParentAwareEntity e = (ParentAwareEntity) entity;
				if(e != null && (parentid == null || parentid == 0)) {
					result.setMessage("Could not find parent with id=" + parentid);
				} else if(e.getParent() != null) {
					e.getParent().setId(parentid);
				}
			}
		}
		if(result.getMessage() == null) {
			entity = rep.save(entity);
			if(entity != null) {
				result.setEntity(entity);
				if(entity instanceof Report) {
					Report r = (Report) entity;
					projectService.export(r);
				}
				
			} else {
				result.setMessage("Error while trying to persist the entity " + entityName);
			}
		}
		
		return result;
	}
}
