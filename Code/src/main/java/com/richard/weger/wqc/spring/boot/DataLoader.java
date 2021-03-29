package com.richard.weger.wqc.spring.boot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.richard.weger.wqc.domain.BaseCheckReport;
import com.richard.weger.wqc.domain.Language;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.domain.TranslatableString;
import com.richard.weger.wqc.domain.Translation;
import com.richard.weger.wqc.repository.BaseCheckReportRepository;
import com.richard.weger.wqc.repository.DomainEntityRepository;
import com.richard.weger.wqc.repository.LanguageRepository;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.repository.TranslatableStringRepository;

@Component
public class DataLoader implements CommandLineRunner {

	@Autowired private ParamConfigurationsRepository confRep;
	@Autowired private BaseCheckReportRepository baseReportRep;
	@Autowired private DomainEntityRepository domainRep;
	@Autowired private LanguageRepository langRep;
	@Autowired private TranslatableStringRepository translationRep;
	
	Logger logger;
	
	public DataLoader() {
		logger = Logger.getLogger(getClass());
	}
	
	@Override @Transactional
	public void run(String... args) throws Exception {
		logger.info("Started default data loader");
		ParamConfigurations conf = confRep.getDefaultConfig();
		if(conf == null) {
			conf = new ParamConfigurations();
			conf = confRep.save(conf);
			if(conf == null) {
				logger.warn("Error while trying to save default paramconfigs!");
			} else {
				logger.info("Param configs saved with id=" + conf.getId());
			}
		}
		
		List<String> defaultRoles = new ArrayList<>();
		defaultRoles.addAll(Arrays.asList("QC", "EL", "MG", "TE"));
		Map<String, String> rolesComments = new HashMap<>();
		rolesComments.put("QC", "Quality Control");
		rolesComments.put("EL", "Electrician");
		rolesComments.put("MG", "Warehouse manager");
		rolesComments.put("TE", "Technician");
		
		List<Role> roles = domainRep.getAllByTypeStartingWith(Role.class.getSimpleName());
		List<String> existingRoles = roles.stream().map(Role::getDescription).collect(Collectors.toList());
		for(String role : defaultRoles) {
			if(!existingRoles.contains(role)) {
				Role r = new Role();
				r.setDescription(role);
				r.setComments(rolesComments.get(role));
				r = domainRep.save(r);
				if(r == null) {
					logger.warn("Error while trying to save default role " + role);
				} else {
					logger.info("Role " + r.getDescription() + " saved with id=" + r.getId());
				}
			}
		}
		
		List<Language> exL = domainRep.getAllByType(Language.class.getSimpleName().toLowerCase());
		List<Language> curr = new ArrayList<>();
		Map<String, String> languageMap = new HashMap<>();
		languageMap.put("en", "English");
		languageMap.put("de", "German");
		languageMap.put("it-rIT", "Italian");
		
		for (Entry<String, String> e : languageMap.entrySet()) {
			Language l = exL.stream()
					.filter(x -> x.getReference().equals(e.getKey()))
					.findFirst()
					.orElse(null);
			if (l == null) {
				l = new Language();
				l.setReference(e.getKey());
				l.setDescription(e.getValue());
				domainRep.save(l);
			}
			curr.add(l);
		}
		
		Map<String, Map<String, String>> translationsMap = new HashMap<>();
		translationsMap.put("5001", new HashMap<String, String>() 
			{{
				put("en", "Drawing (non-wired machine)");
				put("de", "Zeichnung (nicht verdrahtete Maschine)");
				put("it-rIT", "Disegno (macchina non cablata)");
			}}
		);
		translationsMap.put("5002", new HashMap<String, String>()
			{{
				put("en", "Datasheet (non-wired machine)");
				put("de", "Datenblatt (nicht verdrahtete Maschine)");
				put("it-rIT", "Scheda tecnica (macchina non cablata)");
			}}
		);
		translationsMap.put("5031", new HashMap<String, String>()
			{{
				put("en", "Drawing (construction)");
				put("de", "Zeichnung (Konstruktion)");
				put("it-rIT", "Disegno (costruzione)");
			}}
		);
		translationsMap.put("5032", new HashMap<String, String>()
			{{
				put("en", "Drawing (wired machine)");
				put("de", "Zeichnung (Kabelmaschine)");
				put("it-rIT", "Drawing (non-wired machine)");
			}}
		);
		translationsMap.put("5033", new HashMap<String, String>()
			{{
				put("en", "Datasheet (wired machine)");
				put("de", "Datenblatt (Kabelmaschine)");
				put("it-rIT", "Drawing (non-wired machine)");
			}}
		);
		
		List<String> reportCodes = Arrays.asList("5001", "5002", "5031", "5032", "5033");
		Map<String, List<Translation>> repsMap = new HashMap<>();
		for (String c : reportCodes) {
			repsMap.put(c, translationsMap.get(c).entrySet().stream()
					.map(x -> new Translation(langRep.getByReference(x.getKey()), x.getValue()))
					.map(x -> domainRep.save(x))
					.collect(Collectors.toList()));
		}
		
		for (Entry<String, List<Translation>> e : repsMap.entrySet()) {
			BaseCheckReport r = baseReportRep.getByCode(e.getKey());
			if (r == null) {
				r = new BaseCheckReport(e.getKey());
				r.setParent(null);
				domainRep.save(r);
			}
		}
		
		for (Entry<String, List<Translation>> e : repsMap.entrySet()) {
			List<TranslatableString> strs = translationRep.getByCode("report_description_" + e.getKey());
			for (TranslatableString str : strs) {
				str.getTranslations().forEach(x -> domainRep.delete(x));
				str.getTranslations().clear();
				domainRep.delete(str);
			}
		}
			
		
		for (Entry<String, List<Translation>> e : repsMap.entrySet()) {
			TranslatableString str = new TranslatableString();
			str.setCode("report_description_" + e.getKey());
			domainRep.save(str);
		}
				
		for (Entry<String, List<Translation>> e : repsMap.entrySet()) {
			BaseCheckReport r = baseReportRep.getByCode(e.getKey());
			TranslatableString str = translationRep.getByCode("report_description_" + e.getKey()).get(0);
			str.getTranslations().addAll(e.getValue());
			r.setTranslatableString(str);
			domainRep.save(str);
		}
		
		for (Entry<String, List<Translation>> e : repsMap.entrySet()) {
			BaseCheckReport r = baseReportRep.getByCode(e.getKey());
			if (conf.getBaseCheckReports().stream().noneMatch(x -> x.getCode().equals(r.getCode()))) {
				conf.getBaseCheckReports().add(r);
				confRep.save(conf);
			}
		}
//		roles = domainRep.getAllByTypeStartingWith(Role.class.getSimpleName());
//		System.out.println("Existing roles: " + roles.stream().map(Role::getDescription).collect(Collectors.joining(", ")));
	}

}
