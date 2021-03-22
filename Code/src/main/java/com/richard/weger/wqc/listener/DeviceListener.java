package com.richard.weger.wqc.listener;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.springframework.stereotype.Component;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.exception.CrudException;
import com.richard.weger.wqc.exception.DuplicatedDataException;

@Component
public class DeviceListener extends AbstractEntityListener {
	
	@PrePersist
	private void prePersist(Device entity) throws CrudException {
		checkDuplicated(entity);
	}

	@PreUpdate
	public void preUpdate(Device entity) {
		List<Role> roles = getEntities(Role.class);
		List<String> sRoles = roles.stream().map(Role::getDescription).collect(Collectors.toList());
		for(int i = 0; i < entity.getRoles().size(); i++) {
			Role r = entity.getRoles().get(i);
			if(!sRoles.contains(r.getDescription())) {
				throw new CrudException("Role " + r.getDescription() + " does not exists!");
			}
		}
		return;
	}
	
	private void checkDuplicated(Device entity) {
		Device d = getEntity(Device.class, "deviceid", entity.getDeviceid());
		if(d != null) {
			throw new DuplicatedDataException("There is already a device with this deviceid " + entity.getDeviceid() + ")!");
		}
	}
	
}
