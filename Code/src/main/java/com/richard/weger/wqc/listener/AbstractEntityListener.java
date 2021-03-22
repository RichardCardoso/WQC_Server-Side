package com.richard.weger.wqc.listener;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.util.BeanUtil;

public abstract class AbstractEntityListener {
	
	public <T extends DomainEntity> T getEntity(Class<T> eClass, Long id) {
		FlushModeType prevMode;
		T entity = null;
		
		EntityManager em = BeanUtil.getBean(EntityManager.class);
		prevMode = em.getFlushMode();
		em.setFlushMode(FlushModeType.COMMIT);
		
		entity = em.find(eClass, id);
		
		em.setFlushMode(prevMode);
		
		return entity;
	}
	
	public <T extends DomainEntity> T getEntity(Class<T> eClass, String field, String value){
		FlushModeType prevMode;
		T entity = null;
		
		if(getEntities(eClass).size() > 0) {
			EntityManager em = BeanUtil.getBean(EntityManager.class);
			prevMode = em.getFlushMode();
			em.setFlushMode(FlushModeType.COMMIT);
			
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<T> query = builder.createQuery(eClass);
			Root<T> root = query.from(eClass);
			Predicate classType = builder.equal(root.get(field), value);
			TypedQuery<T> typedQuery = em.createQuery(
				query.select(root)
				.where(classType)
			);
			
			List<T> entities = typedQuery.getResultList();
			if(entities.size() > 0) {
				entity = entities.get(0);
			}
			
			em.setFlushMode(prevMode);
		}
		
		return entity;
	}
		
	public <T extends DomainEntity> List<T> getEntities(Class<T> eClass){
		FlushModeType prevMode;
		List<T> entities = null;
		
		EntityManager em = BeanUtil.getBean(EntityManager.class);
		prevMode = em.getFlushMode();
		em.setFlushMode(FlushModeType.COMMIT);
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> query = builder.createQuery(eClass);
		Root<T> root = query.from(eClass);
		Predicate classType = builder.equal(root.get("type"), eClass.getSimpleName());
		TypedQuery<T> typedQuery = em.createQuery(
			query.select(root)
			.where(classType)
		);
		entities = typedQuery.getResultList();
		
		em.setFlushMode(prevMode);
		
		return entities;
	}
	
	@SuppressWarnings("unchecked")
	public void childrenPopulate(ParentAwareEntity entity) {
		if(entity.getChildren() != null) {
			List<ParentAwareEntity> children = entity.getChildren();
			Class<ParentAwareEntity> eClass = (Class<ParentAwareEntity>) ((ParameterizedType) children.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
			
			FlushModeType prevMode;
			EntityManager em = BeanUtil.getBean(EntityManager.class);
			prevMode = em.getFlushMode();
			em.setFlushMode(FlushModeType.COMMIT);
			
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<ParentAwareEntity> query = builder.createQuery(eClass);
			Root<ParentAwareEntity> root = query.from(eClass);
			Predicate parentid = builder.equal(root.get("parent_id"), entity.getId());
			TypedQuery<ParentAwareEntity> typedQuery = em.createQuery(
				query.select(root)
				.where(parentid)
			);
			children = typedQuery.getResultList();
			
			em.setFlushMode(prevMode);
			
			entity.setChildren(children);
		}
	}
	
}
