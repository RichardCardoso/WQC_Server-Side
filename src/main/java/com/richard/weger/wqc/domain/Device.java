package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.richard.weger.wqc.listener.DeviceListener;

@Entity
@EntityListeners(DeviceListener.class)
@Table(name = "devices")
public class Device extends AuditableEntity {
	
	public Device() {
		this.marks = new ArrayList<>();
		this.roles = new ArrayList<>();
		this.enabled = true;
	}
		
	private String deviceid;
	private String name;
	private boolean enabled;
	
	@ManyToMany
	private List<Role> roles;
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval = true, mappedBy = "parent")
	@JsonIgnore
	private List<Mark> marks;
	
	public String getDeviceid() {
		return deviceid;
	}
	public void setDeviceid(String deviceId) {
		this.deviceid = deviceId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Mark> getMarks() {
		return marks;
	}
	public void setMarks(List<Mark> marks) {
		this.marks = marks;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public List<Role> getRoles() {
		return roles;
	}
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
}
