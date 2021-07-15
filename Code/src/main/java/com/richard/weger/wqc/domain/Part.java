package com.richard.weger.wqc.domain;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "parts")
public class Part extends ParentAwareEntity {
	
	@JsonIgnore
	public DrawingRef getParent() {
		return super.getParent(DrawingRef.class);
	}
	
	@Override
	public <T extends ParentAwareEntity> List<T> getChildren() {
		return null;
	}
	
	@Override
	public <T extends ParentAwareEntity> void setChildren(List<T> children) {
		
	}
		
    private int number;
    
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}
