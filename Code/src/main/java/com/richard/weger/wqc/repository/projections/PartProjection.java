package com.richard.weger.wqc.repository.projections;

import org.springframework.beans.factory.annotation.Value;

public interface PartProjection {
	
	@Value("#{target.parent.parent.reference + '_Z_' + target.parent.dnumber + '_T_' + target.number}")
	String getQrCode();
}
