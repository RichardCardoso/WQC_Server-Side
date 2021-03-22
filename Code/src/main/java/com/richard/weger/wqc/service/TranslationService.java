package com.richard.weger.wqc.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.richard.weger.wqc.domain.base.Language;

public class TranslationService {
	public static String translate(String textToTranslate, Language source, Language target) {
		String ret = null;
		
		Translate translate = TranslateOptions.getDefaultInstance().getService();
		
		Translation translation = translate.translate(
				textToTranslate,
				TranslateOption.sourceLanguage(source.getReference()),
				TranslateOption.targetLanguage(target.getReference()));
		
		ret = translation.getTranslatedText();
		
		return ret;
	}
}
