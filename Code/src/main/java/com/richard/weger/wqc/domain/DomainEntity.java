package com.richard.weger.wqc.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import lombok.Data;

@SuppressWarnings("serial")
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = Project.class, name="Project" ),
	@JsonSubTypes.Type(value = DrawingRef.class, name="DrawingRef" ),
	@JsonSubTypes.Type(value = AutomaticReport.class, name="AutomaticReport" ),
	@JsonSubTypes.Type(value = CheckReport.class, name="CheckReport" ),
	@JsonSubTypes.Type(value = ItemReport.class, name="ItemReport" ),
	@JsonSubTypes.Type(value = AutomaticItem.class, name="AutomaticItem" ),
	@JsonSubTypes.Type(value = Page.class, name="Page" ),
	@JsonSubTypes.Type(value = Mark.class, name="Mark" ),
	@JsonSubTypes.Type(value = Item.class, name="Item" ),
	@JsonSubTypes.Type(value = Picture.class, name="Picture" ),
	@JsonSubTypes.Type(value = Device.class, name="Device" ),
	@JsonSubTypes.Type(value = Role.class, name="Role"),
	@JsonSubTypes.Type(value = ParamConfigurations.class, name="ParamConfigurations")
})
@Data
public abstract class DomainEntity implements Serializable {
	
	public DomainEntity() {
		setType(getClass().getSimpleName());
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@TableGenerator(name="entity_gen", initialValue = 1)
	private Long id = 0L;
	
	@Version
	private Long version = 0L;
	
	private String type;
	
	@JsonIgnore
	private transient DomainEntity savedState;

	public DomainEntity getSavedState() {
		return savedState;
	}

	public void setSavedState(DomainEntity savedState) {
		this.savedState = savedState;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
