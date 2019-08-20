package com.richard.weger.wqc.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.domain.AuditableEntity;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.repository.IRepository;

import jxl.common.Logger;

@Service
public class BeanUtil implements ApplicationContextAware {
		
	private static ApplicationContext context;
	
	Logger logger;
	
	public BeanUtil() {
		logger = Logger.getLogger(getClass());
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
	
	public static <T> T getBean(Class<T> beanClass) {
		return context.getBean(beanClass);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends DomainEntity> IRepository<T> getRepository(String entityName){
		Class<IRepository<T>> clz = null;
		try {
			String clzName = "com.richard.weger.wqc.repository.".concat(entityName).concat("Repository");
			clz = (Class<IRepository<T>>) Class.forName(clzName);
			return getBean(clz);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
	    fields.addAll(Arrays.asList(type.getDeclaredFields()));

	    if (type.getSuperclass() != null) {
	        getAllFields(fields, type.getSuperclass());
	    }

	    return fields;
	}
	
	@SuppressWarnings("unchecked")
	@Bean
	private <T extends ParentAwareEntity> Map<String, IRepository<T>> repositoryLookup(){
		Map<String, IRepository<T>> map = new HashMap<>();
		
		List<IRepository<T>> repositoriesList = new ArrayList<>();
		context.getBeansOfType(IRepository.class).forEach((key, value) -> repositoriesList.add((IRepository<T>) value));
		
		for(IRepository<T> r : repositoriesList) {
			String key;
			key = r.getClass().getInterfaces()[0].getSimpleName().toLowerCase();
			map.put(key, r);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends DomainEntity> IRepository<T> getRepository(T entity){
		IRepository<T> r = null;
		if(entity != null) {
			String key = entity.getClass().getSimpleName().toLowerCase().concat("repository");
			r = (IRepository<T>) repositoryLookup().get(key);
		}
		return r;
	}
	
	public <T extends AuditableEntity> void backupCreationInfo(T source, T target) {
		try {
			List<Field> fields = new LinkedList<>();
			BeanUtil.getAllFields(fields, source.getClass());
			for(Field f : fields) {
				if(f.isAnnotationPresent(CreatedBy.class)) {
					f.setAccessible(true);
					f.set(target, source.getCreatedBy());
				} else if (f.isAnnotationPresent(CreatedDate.class)) {
					f.setAccessible(true);
					f.set(target, source.getCreatedDate());
				}
			}
		} catch (Exception ex) {
			logger.warn("Falha ao manter informações de criação da entidade " + target.getClass().getSimpleName() + ", id " + target.getId());
		}
	}
	
	
}
