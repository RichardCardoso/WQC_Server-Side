package com.richard.weger.wqc.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.richard.weger.wqc.domain.Part;
import com.richard.weger.wqc.repository.projections.PartProjection;

public interface PartRepository extends PagingAndSortingRepository<Part, Long> {
	
	public List<PartProjection> findAllProjectedBy();
}
