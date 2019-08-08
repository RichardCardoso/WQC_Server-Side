package com.richard.weger.wqc.spring.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.repository.DomainEntityRepository;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;

@Component
public class DataLoader implements CommandLineRunner {

	@Autowired private ParamConfigurationsRepository rep;
	@Autowired private DomainEntityRepository domainRep;
	
	@Override
	public void run(String... args) throws Exception {
		System.out.println("Started default data loader");
		ParamConfigurations conf = rep.getDefaultConfig();
		if(conf == null) {
			conf = new ParamConfigurations();
			conf = rep.save(conf);
			if(conf == null) {
				System.out.println("Error while trying to save default paramconfigs!");
				System.err.println("Error while trying to save default paramconfigs!");
			} else {
				System.out.println("Param configs saved with id=" + conf.getId());
			}
		}
		
		List<String> defaultRoles = new ArrayList<>();
		defaultRoles.addAll(Arrays.asList("QC", "EL", "MG", "TE"));
		Map<String, String> rolesComments = new HashMap<>();
		rolesComments.put("QC", "Quality Control");
		rolesComments.put("EL", "Electrician");
		rolesComments.put("MG", "Warehouse manager");
		rolesComments.put("TE", "Technician");
		
		List<Role> roles = domainRep.getAllByTypeStartingWith(Role.class.getSimpleName());
		List<String> existingRoles = roles.stream().map(Role::getDescription).collect(Collectors.toList());
		for(String role : defaultRoles) {
			if(!existingRoles.contains(role)) {
				Role r = new Role();
				r.setDescription(role);
				r.setComments(rolesComments.get(role));
				r = domainRep.save(r);
				if(r == null) {
					System.err.println("Error while trying to save default role " + role);
					System.out.println("Error while trying to save default role " + role);
				} else {
					System.out.println("Role " + r.getDescription() + " saved with id=" + r.getId());
				}
			}
		}
//		roles = domainRep.getAllByTypeStartingWith(Role.class.getSimpleName());
//		System.out.println("Existing roles: " + roles.stream().map(Role::getDescription).collect(Collectors.joining(", ")));
	}

}
