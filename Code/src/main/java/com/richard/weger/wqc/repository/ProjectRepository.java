package com.richard.weger.wqc.repository;

import com.richard.weger.wqc.domain.Project;

public interface ProjectRepository extends IRepository<Project> {
	public Project findByReference(String reference);
	public Project findByReferenceAndDrawingRefsDnumberAndDrawingRefsPartsNumber(String reference, int drawingNumber, int partNumber);
}
