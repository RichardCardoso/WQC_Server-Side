package com.richard.weger.wqc.faccade;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.richard.weger.wqc.domain.BaseCheckReport;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.exception.WebException;
import com.richard.weger.wqc.repository.BaseCheckReportRepository;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SingleObjectResult;
import com.richard.weger.wqc.service.EntityService;

@Controller
@RequestMapping(value = "/web/settings" )
public class SettingsWebFaccade {
	
	@Autowired private EntityService entityService;
	@Autowired private BaseCheckReportRepository baseReportRep;
	@Autowired private ParamConfigurationsRepository configRep;
	
	@GetMapping(value = "")
	public ModelAndView settings() {
		ModelAndView modelAndView;
		
		ParamConfigurations conf = configRep.getDefaultConfig();

		modelAndView = new ModelAndView("setting");
		modelAndView.addObject("ParamConfigurations", conf);
		modelAndView.addObject("baseReports", baseReportRep.findAll());
		return modelAndView;
	}

	@PostMapping(value = "")
	public ModelAndView settings(@RequestParam(value = "operation") String operation,
			@Validated @ModelAttribute("ParamConfigurations") ParamConfigurations nConf, RedirectAttributes attr) {

		ModelAndView mv = new ModelAndView("setting");
		
		mv.addObject("ParamConfigurations", nConf);
				

		if (operation.toLowerCase().equals("save")) {
			
			ParamConfigurations def = configRep.getDefaultConfig();
			def.setOriginalDocsPath(nConf.getOriginalDocsPath());
			def.setControlCardReportCode(nConf.getControlCardReportCode());
			def.setOriginalDocsExtension(nConf.getOriginalDocsExtension());
			def.setServerPath(nConf.getServerPath());
			def.setRootPath(nConf.getRootPath());
			def.setYearPrefix(nConf.getYearPrefix());
			configRep.save(def);
			attr.addFlashAttribute("message", "Changes saved!");
		} else {
			attr.addFlashAttribute("message", "Operation cancelled!");
		}
		mv = new ModelAndView("redirect:/web");
		return mv;
	}

	@SuppressWarnings("unchecked")
	@PostMapping(value = "/baseReports", params = {"operation=edit", "id!=0"})
	public ModelAndView baseReportEdit(@RequestParam("id") Long id)  throws WebException {
		
		ModelAndView mv;
		
		mv = new ModelAndView("baseReport");
		
		AbstractResult res = entityService.getEntity(id);
		if (res instanceof SingleObjectResult) {
			SingleObjectResult<?> resW = (SingleObjectResult<?>) res;
			if (BaseCheckReport.class.isInstance(resW.getObject())) {
				BaseCheckReport rep = ((SingleObjectResult<BaseCheckReport>) res).getObject();
				mv.addObject("entity", rep);
				return mv;
			}
		}
		throw new WebException(new ErrorResult(ErrorCode.ENTITY_RETRIEVAL_FAILED, "Unable to find valid entity with id=" + id + ", which was meant to be edited.", ErrorLevel.SEVERE, getClass()), "/web/settings");		
	}
	
	@PostMapping(value = "/baseReports", params = {"operation=delete", "id!=0"})
	public ModelAndView baseReportDelete(@RequestParam("id") Long id)  throws WebException {	
		
		baseReportRep.deleteById(id);
		return new ModelAndView("redirect:/web/settings");
	}
	
	
	@GetMapping(value = "/baseReports", params = {"operation=add"})
	public ModelAndView baseReportAdd()  throws WebException {
		
		ModelAndView mv;
		
		mv = new ModelAndView("baseReport");
		mv.addObject("entity", new BaseCheckReport(null));
		
		return mv;		
	}
	
	@PostMapping(value = "/baseReports", params = {"operation=cancel", "id!=0"})
	public ModelAndView baseReportCancel()  throws WebException {
		
		return new ModelAndView("redirect:/web/settings");
	}
	
	
	@PostMapping(value = "/baseReports", params = {"operation=save"})
	public ModelAndView baseReportsSave(
			@RequestParam("operation") String operation, 
			@Validated @ModelAttribute BaseCheckReport baseRep, 
			BindingResult result, RedirectAttributes attr, HttpServletRequest req) {

		if (result.hasErrors() && result.getFieldErrors().stream().filter(e -> !e.getField().equals("roles")).count() > 0) {
			return new ModelAndView("main");
		}

		ModelAndView modelAndView = new ModelAndView("redirect:/web/settings");;

		String message = null;
		List<BaseCheckReport> reports = baseReportRep.findAll();
		if(CollectionUtils.isEmpty(baseRep.getTranslatableString().getTranslations()) || baseRep.getTranslatableString().getTranslations().stream().anyMatch(x -> !StringUtils.hasText(x.getValue()))) {
			message = "An error ocurred when trying to proccess the translations!";
		} else if (!StringUtils.hasLength(baseRep.getCode())) {
			message = "Code cannot be empty!";
		} else if (baseRep.getCode().length() != 4) {
			message = "Code must have four (4) digits!";
		} else if (reports.stream().anyMatch(x -> ObjectUtils.nullSafeEquals(baseRep.getCode(), x.getCode()))) {
			message = "Report code should be unique!";
		} else {
			ParamConfigurations conf = configRep.getDefaultConfig();
			baseRep.setParent(conf);
			conf.getBaseCheckReports().add(baseRep);
			AbstractResult res = entityService.postEntity(baseRep, null, baseRep.getClass().getSimpleName(), null, null);
			if(res instanceof ErrorResult) {
				ErrorResult err = ResultService.getErrorResult(res);
				message = err.getCode().concat(" - ").concat(err.getDescription());
			}
		}
		if (message != null) {
			attr.addFlashAttribute("message", message);
			modelAndView = new ModelAndView("redirect:../settings/baseReports?operation=edit&id=" + baseRep.getId());
			return modelAndView;
		} else {
			attr.addFlashAttribute("message", "Changes saved!");
		}
		
		return modelAndView;
	}
}
