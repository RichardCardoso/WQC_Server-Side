package com.richard.weger.wqc.repository;

import com.richard.weger.wqc.domain.Role;

public interface RoleRepository extends IRepository<Role> {
	public Role getByDescription(String description);
}
