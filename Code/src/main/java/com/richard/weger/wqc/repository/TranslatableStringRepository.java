package com.richard.weger.wqc.repository;

import java.util.List;

import com.richard.weger.wqc.domain.TranslatableString;

public interface TranslatableStringRepository extends IRepository<TranslatableString> {

	public List<TranslatableString> getByCode(String code);
	public void deleteByCode(String code);
}
