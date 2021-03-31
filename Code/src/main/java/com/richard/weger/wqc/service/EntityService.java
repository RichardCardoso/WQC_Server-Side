package com.richard.weger.wqc.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.domain.ReportLock;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.messaging.WebSocketMessagingService;
import com.richard.weger.wqc.repository.DeviceRepository;
import com.richard.weger.wqc.repository.DomainEntityRepository;
import com.richard.weger.wqc.repository.ParentAwareEntityRepository;
import com.richard.weger.wqc.repository.ReportRepository;
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
	
	@Value("${build.version}")
	private String appVersion;
	
	@Autowired private ReportRepository reportRep;
	@Autowired private DeviceRepository deviceRep;
	@Autowired private DomainEntityRepository rep;
	@Autowired private ParentAwareEntityRepository parentRep;
	@Autowired private ExportService exportService;
	@Autowired private WebSocketMessagingService messagingHandler;
	@Autowired private AuditingHandler auditingHandler;
	
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
		String message = "Failed to remove the entity!";
		try {
			DomainEntity e;
			DomainEntity parent = null;
			e = rep.getById(id);
			if(id == null || id == 0 || e == null) {
				return new ErrorResult(ErrorCode.INVALID_ENTITY_ID, "Invalid entity id was received!", ErrorLevel.SEVERE, getClass());
			} else if (version == null) {
				return new ErrorResult(ErrorCode.INVALID_ENTITY_VERSION, "Invalid entity version was received!", ErrorLevel.SEVERE, getClass());
			} else if(e.getVersion() != version) {
				return new ErrorResult(ErrorCode.STALE_ENTITY, "Your data is stale. Please go back to the previous screen and try again.", ErrorLevel.SEVERE, getClass());
			}
			if(e instanceof ParentAwareEntity) {
				parent = ((ParentAwareEntity) e).getParent(DomainEntity.class);
			}
			try {
				rep.deleteById(id);
				e = rep.getById(id);
				if(e == null) {
					if(!Strings.isEmpty(qrcode)) {
						Long parentId = -1L;
						if(parent != null) {
							parentId = parent.getId();	
						}
						messagingHandler.sendUpdateNotice(qrcode, id, parentId);
					} else {
						logger.warn("Invalid qr code received at a DELETE procedure. Unable to send firebase push message!");
					}
					if(parent != null) {
						auditingHandler.markModified(parent);
						rep.save(parent);
					}
					return new EmptyResult();
				}
			} catch (Exception ex) {
				logger.fatal(message, ex);
			}
		} catch (ObjectOptimisticLockingFailureException ex) {
			return new ErrorResult(ErrorCode.STALE_ENTITY, "Your data is stale! Please try again.", ErrorLevel.WARNING, getClass());
		}
		return new ErrorResult(ErrorCode.ENTITY_DELETE_FAILED, message, ErrorLevel.SEVERE, getClass());
	}
	
	@Transactional(readOnly=false)
	public <T extends DomainEntity> AbstractResult postEntity(T entity, Long parentid, String entityName, String qrcode, String deviceid) {
		
		try {
			Long eId;
			DomainEntity parent = null;
			
			if(entity == null) {
				return new ErrorResult(ErrorCode.INVALID_ENTITY, "An invalid entity was received at a POST procedure!", ErrorLevel.SEVERE, getClass());
			} else if (entity.getId() == null) {
				return new ErrorResult(ErrorCode.INVALID_ENTITY_ID, "An invalid entity id was received at a POST procedure!", ErrorLevel.SEVERE, getClass());
			} else if (Strings.isEmpty(entityName)) {
				return new ErrorResult(ErrorCode.INVALID_ENTITY_NAME, "An invalid entity name was received at a POST procedure!", ErrorLevel.SEVERE, getClass());
			} else if (entity instanceof ParentAwareEntity && parentid != null) {
				parent = rep.getById(parentid);
			}
			
			eId = entity.getId();
			
			if (parent == null) {
				AbstractResult res = forceJpaRefreshForQuery(entity, parentid);
				if(res instanceof ErrorResult) {
					return res;
				}
			} else {
				((ParentAwareEntity) entity).getParent(DomainEntity.class).setId(parent.getId());
			}
			
			try {
				lockValidation(entity, deviceid);
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
				boolean shouldSendUpdateNotice;
				
				if(parent != null) {
					auditingHandler.markModified(parent);
					rep.save(parent);
				}
				
				shouldSendUpdateNotice = !(entity instanceof ParamConfigurations || entity instanceof Device);
				if(entity instanceof Report) {
					Report r = (Report) entity;
					if (r.isFinished()) {
						exportService.export(r);
					}
				} 
				if (shouldSendUpdateNotice) {
					if(!Strings.isEmpty(qrcode)) {
						Long parentId = -1L;
						if(parent != null) {
							parentId = parent.getId();	
						}
						messagingHandler.sendUpdateNotice(qrcode, entity.getId(), parentId);
					} else {
						logger.warn("Invalid qr code received at a POST procedure. Unable to send firebase push message!");
					}
				} else {
					logger.info("Updates to " + entity.getClass().getSimpleName() + " does not require a notification to be sent to the client.");
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
		} catch (ObjectOptimisticLockingFailureException ex) {
			return new ErrorResult(ErrorCode.STALE_ENTITY, "Your data is stale! Please try again.", ErrorLevel.WARNING, getClass());
		}
		
	}
	
	private void lockValidation(DomainEntity e, String deviceId) throws Exception {
		
		Report parent = null;
		if (e instanceof Mark) {
			Mark m = (Mark) e;
			Page p = (Page) rep.getById(m.getParent(DomainEntity.class).getId());
			parent = p.getParent(CheckReport.class);
		} else if (e instanceof Item) {
			Item i = (Item) e;
			parent = i.getParent(ItemReport.class);
		} else if (e instanceof Report) {
			parent = (Report) e;
		}
		if (parent != null && deviceId != null) {
			Report r = (Report) rep.getById(parent.getId());
			if (r.getLock() != null) {
				checkAllowed(r.getLock(), deviceRep.getByDeviceid(deviceId));
			}
		} else if (parent != null) {
			throw new Exception("");
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
		
//		entityName = entityName.substring(0, entityName.length() - 1);
		
		if(parentid != null && parentid > 0) {
			List<ParentAwareEntity> list = parentRep.getAllByParentIdAndType(parentid, entityName); 
			entities = new ArrayList<DomainEntity>();
			entities.addAll(list);
		} else {
			entities = rep.getAllByType(entityName);
		}
		
		if (CollectionUtils.isEmpty(entities)) {
			return new EmptyResult();
		}
		setParentString(entities);
		return new MultipleObjectResult<>(DomainEntity.class, entities);		
	}
	
	private void setParentString(List<DomainEntity> entities) {
		
		for (DomainEntity ref : entities) {
			if (ref instanceof ParentAwareEntity) {
				ParentAwareEntity parent = (ParentAwareEntity) ref;
				setParentString(parent);
			}
		}
	}
	
	private void setParentString(ParentAwareEntity parent) {
		
		if (!CollectionUtils.isEmpty(parent.getChildren())) {
			for (DomainEntity ref : parent.getChildren()) {
				ParentAwareEntity child = (ParentAwareEntity) ref;
				setParentString(parent, child);
			}
		}
	}
	
	@Transactional
	public AbstractResult reportFinish(Long reportId, boolean finish) {
		
		AbstractResult res;
		
		Report r = reportRep.getById(reportId);
		
		res = new SingleObjectResult<>(Report.class, r);
		
		try {
			r.setFinished(finish);
			if (finish) {
				exportService.export(r);
			}
			rep.save(r);
		} catch (Exception ex) {
			res = new ErrorResult(ErrorCode.ENTITY_PERSIST_FAILED, reportId + "", ErrorLevel.SEVERE, getClass());
		}
		
		return res;
	}
	
	@Transactional
	public AbstractResult reportLock(Long reportId, String deviceId, boolean lock) {
		
		AbstractResult res;
		
		Report r = reportRep.getById(reportId);
		Device d = deviceRep.getByDeviceid(deviceId);
		
		res = new SingleObjectResult<>(Report.class, r);
		
		if (lock) {
			try {
				lock(r, d);
			} catch (Exception ex) {
				res = new ErrorResult(ErrorCode.REPORT_LOCK_FAILED, deviceId, ErrorLevel.SEVERE, getClass());
			}
		} else {
			try {
				unlock(r, d);
			} catch (Exception ex) {
				res = new ErrorResult(ErrorCode.REPORT_UNLOCK_FAILED, deviceId, ErrorLevel.SEVERE, getClass());
			}
		}
		
		return res;
	}
	
	private void lock(Report r, Device d) throws Exception {
		
		ReportLock l;
		l = r.getLock();
		if (l != null) {
			checkAllowed(l, d);
			rep.delete(l);
		}
		l = new ReportLock();
		l.setDevice(d);
		l.setLastPing(new Date());
		r.setLock(l);
		rep.save(r);
	}
	
	private void unlock(Report r, Device d) throws Exception {
		
		ReportLock l;
		l = r.getLock();
		if (l != null) {
			checkAllowed(l, d);
			rep.delete(l);
			r.setLock(null);
			rep.save(r);
		}
	}
	
	private void checkAllowed(ReportLock l, Device d) throws Exception {
		
		if (!l.getDevice().getDeviceid().equals(d.getDeviceid())) {
			Date now = new Date();
			Date lastPing = l.getLastPing();
			long diff = (now.getTime() - lastPing.getTime()) / 1000;
			if (Math.abs(diff) < 10) {
				throw new Exception();
			}
		}
	}
	
	private void setParentString(ParentAwareEntity parent, ParentAwareEntity child) {
		
		GsonBuilder builder = new GsonBuilder().addSerializationExclusionStrategy(serializationExclusionStrategy);
		Gson gson = builder.create();
		String str = gson.toJson(parent);
		child.setParentJson(str);
		if (!CollectionUtils.isEmpty(child.getChildren())) {
			setParentString(child);
		}
	}
	
	final ExclusionStrategy serializationExclusionStrategy = new ExclusionStrategy() {
		  @Override
		  public boolean shouldSkipField(FieldAttributes f) {
		    return f.getAnnotation(JsonIgnore.class) != null
		           || f.getAnnotation(JsonBackReference.class) != null 
		           || f.getAnnotation(OneToMany.class) != null;
		  }

		  @Override
		  public boolean shouldSkipClass(Class<?> aClass) {
		    return false;
		  }
		};
		
	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<T> objectlessSuccessReturn(AbstractResult res) {
		
		if (res instanceof SingleObjectResult) {
			SingleObjectResult<T> r = (SingleObjectResult<T>) res;
			return ResponseEntity.status(HttpStatus.OK).body(r.getObject());
		} else {
			return objectlessReturn(res);
		}
	}
		
	public <T> ResponseEntity<T> objectlessReturn(AbstractResult res) {
		 if (res instanceof ErrorResult) {
			HttpHeaders headers = ResultService.getErrorHeaders(ResultService.getErrorResult(res));
			headers.set("version", getAppVersion());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}	
	
	public <T> ResponseEntity<List<T>> objectListReturn(AbstractResult res) {
		 if (res instanceof ErrorResult) {
			HttpHeaders headers = ResultService.getErrorHeaders(ResultService.getErrorResult(res));
			headers.set("version", getAppVersion());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	public String getAppVersion() {
		return appVersion;
	}
	
	
}
