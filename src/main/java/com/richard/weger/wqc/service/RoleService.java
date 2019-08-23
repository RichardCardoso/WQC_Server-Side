package com.richard.weger.wqc.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.repository.RoleRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.SingleObjectResult;

@Service
public class RoleService {
	
	@Autowired private RoleRepository roleRep;
	
	public AbstractResult getByDescription(String description) {
		
		if(Strings.isEmpty(description)) {
			return new ErrorResult(ErrorCode.INVALID_ROLE_DESCRIPTION, "An invalid role description was received!", ErrorLevel.SEVERE, getClass());
		}
		
		Role role = roleRep.getByDescription(description);
		if(role != null) {
			return new SingleObjectResult<>(Role.class, role);
		} else {
			return new EmptyResult();
		}
		
	}
}
