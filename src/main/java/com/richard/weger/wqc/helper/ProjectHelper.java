package com.richard.weger.wqc.helper;

import java.util.List;

import com.richard.weger.wqc.domain.AutomaticItem;
import com.richard.weger.wqc.domain.AutomaticReport;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.domain.Part;
import com.richard.weger.wqc.domain.Picture;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;

public class ProjectHelper {
	
	public static void linkReferences(DomainEntity parent, ParentAwareEntity entity) {
		List<ParentAwareEntity> child = entity.getChildren();
		if(child != null) {
			for(ParentAwareEntity p : child) {
				linkReferences(entity, p);
			}
		}
		if(parent != null) {
			entity.getParent(DomainEntity.class).setId(parent.getId());
		}
		
	}

	public static void linkReferences(Project project) {
		if(project != null) {
			for (DrawingRef d : project.getDrawingRefs()) {
				d.setParent(project);
				for (Report r : d.getReports()) {
					r.setParent(d);
					if (r instanceof CheckReport) {
						CheckReport cr = (CheckReport) r;
						for (Page p : cr.getPages()) {
							p.setParent(cr);
							for (Mark m : p.getMarks()) {
								m.setParent(p);
								Device device = m.getDevice();
								device.getMarks().add(m);
							}
						}
					} else if (r instanceof ItemReport) {
						ItemReport ir = (ItemReport) r;
						for (Item i : ir.getItems()) {
							i.setParent(ir);
							Picture p = i.getPicture();
							p.setDrawingref(i);
						}
					} else if (r instanceof AutomaticReport) {
						AutomaticReport ar = (AutomaticReport) r;
						for (AutomaticItem ai : ar.getAutomaticItems()) {
							ai.getId();
						}
					}
				}
				for (Part p : d.getParts()) {
					p.setParent(d);
				}
			}
		}
	}

}
