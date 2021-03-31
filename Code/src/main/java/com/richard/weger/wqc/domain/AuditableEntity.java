package com.richard.weger.wqc.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.richard.weger.wqc.spring.jackson.CustomDateDeserializer;


@SuppressWarnings("serial")
@Entity
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends DomainEntity {
	
	@CreatedBy
	private String createdBy;
	
	@CreatedDate
	private Date createdDate;
	
	@LastModifiedBy
	private String lastModifiedBy;
	
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	@LastModifiedDate
	@Temporal(TemporalType.TIMESTAMP)
	@JsonDeserialize(using = CustomDateDeserializer.class)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss'T'")
	private Date lastModifiedDate;

	public String getCreatedBy() {
		return createdBy;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	
}
