package com.richard.weger.wqc.repository;

import org.springframework.data.jpa.repository.Query;

import com.richard.weger.wqc.domain.ParamConfigurations;

public interface ParamConfigurationsRepository extends IRepository<ParamConfigurations> {
	
	@Query("SELECT c from ParamConfigurations c where c.id = 1")
	public ParamConfigurations getDefaultConfig();
	
}
