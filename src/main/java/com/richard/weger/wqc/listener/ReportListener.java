package com.richard.weger.wqc.listener;

import javax.persistence.PostUpdate;

import org.springframework.transaction.annotation.Transactional;

import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.service.ExportService;
import com.richard.weger.wqc.util.BeanUtil;

public class ReportListener extends AbstractEntityListener {
	
	@PostUpdate @Transactional(readOnly = true)
	public void preUpdate(Report report) {
//		String event = "Report changed by " + report.getLastModifiedBy();
		ExportService service = BeanUtil.getBean(ExportService.class);
		if(report.isFinished()) {
			service.export((Report) report.getSavedState());
		}
	}
	
  	/*
	private void auditChange(Report report, String event) {
		if(report instanceof ItemReport) {
			ItemReport ir = (ItemReport) report;
			
		} else if (report instanceof CheckReport) {
			CheckReport cr = (CheckReport) report;
			
		}
		EntityHistory history = new EntityHistory(event);
		
		EntityManager em = BeanUtil.getBean(EntityManager.class);
		em.persist(history);
	}
	*/
	
}
