package com.richard.weger.wqc.domain;

import java.util.Map;

import com.richard.weger.wqc.appconstants.AppConstants;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;

public abstract class FactoryProject {
	public static Project getProject(Map<String, String> mapValues) {
		Project p = new Project();
		AppConstants c = FactoryAppConstants.getAppConstants();
		if (mapValues != null 
				&& mapValues.containsKey(c.getDRAWING_NUMBER_KEY())
				&& mapValues.containsKey(c.getPROJECT_NUMBER_KEY())) {
			
			p.setReference(mapValues.get(c.getPROJECT_NUMBER_KEY()));
			
			DrawingRef d = new DrawingRef();
			d.setParent(p);
			d.setDnumber(Integer.valueOf(mapValues.get(c.getDRAWING_NUMBER_KEY())));
			
			p.getDrawingRefs().add(d);
			
			Part part = new Part();
			part.setParent(d);
			part.setNumber(Integer.valueOf(mapValues.get(c.getPART_NUMBER_KEY())));
			
			d.getParts().add(part);
		}
		return p;
	}
	
	public static Project getProject(long pid, long did, long rid, long pgid) {
		Project p = new Project();
		p.setId(pid);
		
		DrawingRef d = new DrawingRef();
		d.setId(did);
		
		CheckReport r = new CheckReport();
		r.setId(rid);
		
		Page page = new Page();
		page.setId(pgid);
		
		p.getDrawingRefs().add(d);
		d.getReports().add(r);
		r.getPages().add(page);
		
		d.setParent(p);
		r.setParent(d);
		page.setParent(r);
		
		return p;
	}
	
	public static Project getProject(long pid, long did, long rid) {
		Project p = new Project();
		p.setId(pid);
		
		DrawingRef d = new DrawingRef();
		d.setId(did);
		
		CheckReport r = new CheckReport();
		r.setId(rid);
		
		
		p.getDrawingRefs().add(d);
		d.getReports().add(r);
		
		d.setParent(p);
		r.setParent(d);
		
		return p;
	}
}
