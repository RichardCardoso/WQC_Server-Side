package com.richard.weger.wqc.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import lombok.Data;

@Entity
@Data
public class ParamConfigurations extends AuditableEntity {
	
	public ParamConfigurations() {
		
		setId(1L);
		baseCheckReports = new ArrayList<>();
	}
	
	private String originalDocsPath = "Technik/Kundendaten/";
	private String controlCardReportCode = "0000";
	/*
	private String wiredDrawingCode = "5032";
	private String wiredDatasheetCode = "5033";
	private String cablelessDrawingCode = "5001";
	private String cablelessDatasheetCode = "5002";
	*/
	private String originalDocsExtension = ".pdf";
	private String rootPath = "Auftrag/";
	private String serverPath = "//srv-weger1/dados/publico/";
	private String serverUsername = "";
	private String serverPassword = "";
	private String yearPrefix = "20";
	private String appPassword = "147258369";
	
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<BaseCheckReport> baseCheckReports; 

}
