package com.richard.weger.wqc.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.firebase.FirebaseMessagingHelper;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.repository.DomainEntityRepository;
import com.richard.weger.wqc.repository.ParentAwareEntityRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.MultipleObjectResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SingleObjectResult;

@Service
public class EntityService {
	
	@Autowired private DomainEntityRepository rep;
	@Autowired private ParentAwareEntityRepository parentRep;
	@Autowired private ExportService exportService;
	@Autowired private FirebaseMessagingHelper firebase;
	
	@PersistenceContext
	EntityManager em;
	
	Logger logger;
	
	public EntityService() {
		logger = Logger.getLogger(getClass());
	}
	
	public AbstractResult getEntity(Long id) {
		if(id == null || id == 0) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY_ID, "Invalid entity id was received!", ErrorLevel.SEVERE, getClass());
		}
		
		DomainEntity e = rep.getById(id);
		if(e != null) {
			return new SingleObjectResult<>(DomainEntity.class,e);
		} else {
			return new EmptyResult();
		}
		
	}
	
	public AbstractResult deleteEntity(Long id, Long version, String qrcode) {
		DomainEntity e;
		String message;
		e = rep.getById(id);
		message = "Failed to remove the entity!";
		if(id == null || id == 0) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY_ID, "Invalid entity id was received!", ErrorLevel.SEVERE, getClass());
		} else if (version == null) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY_VERSION, "Invalid entity version was received!", ErrorLevel.SEVERE, getClass());
		} else if(e.getVersion() != version) {
			return new ErrorResult(ErrorCode.STALE_ENTITY, "Your data is stale. Please go back to the previous screen and try again.", ErrorLevel.SEVERE, getClass());
		}
		try {
			rep.deleteById(id);
			e = rep.getById(id);
			if(e == null) {
				if(Strings.isEmpty(qrcode)) {
					firebase.sendUpdateNotice(qrcode);
				} else {
					logger.warn("Invalid qr code received at a DELETE procedure. Unable to send firebase push message!");
				}
				return new EmptyResult();
			}
		} catch (Exception ex) {
			logger.fatal(message, ex);
		}
		return new ErrorResult(ErrorCode.ENTITY_DELETE_FAILED, message, ErrorLevel.SEVERE, getClass());
	}
	
	public <T extends DomainEntity> AbstractResult postEntity(T entity, Long parentid, String entityName, String qrcode) {
		
		Long eId;
		
		if(entity == null) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY, "An invalid entity was received at a POST procedure!", ErrorLevel.SEVERE, getClass());
		} else if (entity.getId() == null) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY_ID, "An invalid entity id was received at a POST procedure!", ErrorLevel.SEVERE, getClass());
		} else if (Strings.isEmpty(entityName)) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY_NAME, "An invalid entity name was received at a POST procedure!", ErrorLevel.SEVERE, getClass());
		}
		
		eId = entity.getId();
		
		AbstractResult res = forceJpaRefreshForQuery(entity, parentid);
		if(res instanceof ErrorResult) {
			return res;
		}
		
		try {
			entity = rep.save(entity);
		} catch (Exception ex) {
			ErrorResult errRes;
			String message = null;
			if(eId > 0) {
				message = "Entity save failed!";
				errRes = new ErrorResult(ErrorCode.ENTITY_PERSIST_FAILED, message, ErrorLevel.SEVERE, getClass());
			} else {
				message = "Entity creation failed!";
				errRes = new ErrorResult(ErrorCode.ENTITY_CREATION_FAILED, message, ErrorLevel.SEVERE, getClass());
			}
			logger.fatal(message, ex);
			return errRes;
		}
		
		if(entity != null) {
			if(entity instanceof Report) {
				Report r = (Report) entity;
				exportService.export(r);
			} else if (!(entity instanceof ParamConfigurations) && !(entity instanceof Device)) {
				if(Strings.isEmpty(qrcode)) {
					firebase.sendUpdateNotice(qrcode);
				} else {
					logger.warn("Invalid qr code received at a POST procedure. Unable to send firebase push message!");
				}
			}
			SingleObjectResult<DomainEntity> oRes = new SingleObjectResult<>(DomainEntity.class, entity);
			if(eId > 0) {
				oRes.setUpdated(true);
			}
			return oRes;
		} else {
			if(eId > 0) {
				return new ErrorResult(ErrorCode.ENTITY_PERSIST_FAILED, "Entity save failed!", ErrorLevel.SEVERE, getClass());
			} else {
				return new ErrorResult(ErrorCode.ENTITY_CREATION_FAILED, "Entity creation failed!", ErrorLevel.SEVERE, getClass());
			}
		}
		
	}
	
	private <T extends DomainEntity> AbstractResult forceJpaRefreshForQuery(T entity, Long parentid) {
		Long eId = entity.getId();
		if(eId > 0) {
			if(entity instanceof ParentAwareEntity) {
				ParentAwareEntity e = (ParentAwareEntity) entity;
				ParentAwareEntity currE = parentRep.getById(entity.getId());
				DomainEntity parent = null;
				if(e.getParent(DomainEntity.class) != null) {
					parent = e.getParent(DomainEntity.class);
					parent.setId(currE.getParent(DomainEntity.class).getId());
				}
				if(currE.getChildren() != null) {
					e.setChildren(currE.getChildren());
				}
				ProjectHelper.linkReferences(parent, e);
			} else {
				DomainEntity currE = rep.getById(entity.getId());
				logger.info(currE);
			}
		} else {
			if (entity instanceof ParentAwareEntity) {
				ParentAwareEntity e = (ParentAwareEntity) entity;
				if(e != null && (parentid == null || parentid == 0)) {
					return new ErrorResult(ErrorCode.ENTITY_NOT_FOUND, "Could not find parent with id=" + parentid, ErrorLevel.SEVERE, getClass());
				} else if(e.getParent(DomainEntity.class) != null) {
					e.getParent(DomainEntity.class).setId(parentid);
				}
			}
		}
		return new EmptyResult();
	}
	
	public AbstractResult entitiesList(Long parentid, String entityName) {
		
		List<DomainEntity> entities = null;
		
		if(Strings.isEmpty(entityName)) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY_NAME, "Invalid entityName was received!", ErrorLevel.SEVERE, getClass());
		}
		
		entityName = entityName.substring(0, entityName.length() - 1);
		
		if(parentid != null && parentid > 0) {
			List<ParentAwareEntity> list = parentRep.getAllByParentIdAndTypeStartingWith(parentid, entityName); 
			entities = new ArrayList<DomainEntity>();
			entities.addAll(list);
		} else {
			entities = rep.getAllByTypeStartingWith(entityName);
		}
		
		if(entities != null && !entities.isEmpty()) {
			return new MultipleObjectResult<>(DomainEntity.class, entities);
		} else {
			return new EmptyResult();
		}
		
		
	}
		
	public <T> ResponseEntity<T> objectlessReturn(AbstractResult res) {
		 if (res instanceof ErrorResult) {
			HttpHeaders headers = ResultService.getErrorHeaders(ResultService.getErrorResult(res));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}	
	
	public <T> ResponseEntity<List<T>> objectListReturn(AbstractResult res) {
		 if (res instanceof ErrorResult) {
			HttpHeaders headers = ResultService.getErrorHeaders(ResultService.getErrorResult(res));
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
	
	
}
