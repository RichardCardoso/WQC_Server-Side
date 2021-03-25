package com.richard.weger.wqc.repository;

import com.richard.weger.wqc.domain.BaseCheckReport;

public interface BaseCheckReportRepository extends IParentAwareEntityRepository<BaseCheckReport> {
	
	public BaseCheckReport getByCode(String code);
}
