package com.richard.weger.wqc.repository;

import com.richard.weger.wqc.domain.TranslatableString;

public interface TranslatableStringRepository extends IRepository<TranslatableString> {

	public TranslatableString getByCode(String code);
}
