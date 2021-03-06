package com.richard.weger.wqc.repository;

import com.richard.weger.wqc.domain.Language;

public interface LanguageRepository extends IRepository<Language> {
	
	public Language getByReference(String reference);

}
