package com.richard.weger.wqc.faccade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.richard.weger.wqc.ReportExportDTO;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.exception.WebException;
import com.richard.weger.wqc.repository.DeviceRepository;
import com.richard.weger.wqc.repository.DrawingRefRepository;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.repository.ProjectRepository;
import com.richard.weger.wqc.repository.RoleRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.EntityService;
import com.richard.weger.wqc.service.ExportService;
import com.richard.weger.wqc.util.TimeUtils;

@Controller
@RequestMapping(value = "/web" )
public class WebFaccade {

	@Autowired private ExportService exportService;
	
	@Autowired private ProjectRepository projectRep;
	@Autowired private DrawingRefRepository drawingRep;
	@Autowired private ParamConfigurationsRepository configRep;
	@Autowired private DeviceRepository deviceRep;
	@Autowired private RoleRepository roleRep;
	@Autowired private EntityService entityService;
	
	HttpHeaders headers;
	
	Logger logger;

	@PostConstruct
	public void postConstruct() {
		logger = Logger.getLogger(getClass());
		logger.info("System started... Version " + entityService.getAppVersion());
	}

	public String getCurrentTime() {
		return (new TimeUtils()).getCurrentTime();
	}

	// JSP METHODS - BEGIN
	// -----------------------------------------------------------------------------------------------

	@RequestMapping(value = { "" })
	public ModelAndView index(@ModelAttribute(value = "message") String message) {
		ModelAndView mv = new ModelAndView("main");
		mv.addObject("message", message);
		mv.addObject("version", entityService.getAppVersion());
		return mv;
	}

	@GetMapping(value = { "/projects" })
	public ModelAndView projectsList() {
		ModelAndView modelAndView;
		
		List<Project> projects = projectRep.findAll();
		
		boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

		modelAndView = new ModelAndView("projects").addObject("projects", projects);
		modelAndView.addObject("isDebug", isDebug);

		return modelAndView;
	}

	@GetMapping(value = { "/drawings" })
	public ModelAndView drawingsList(@RequestParam(value = "parentid") Long projectid) {
		ModelAndView modelAndView;
		
		List<DrawingRef> drawings = drawingRep.getAllByParentId(projectid);
		Project p = projectRep.getById(projectid);

		modelAndView = new ModelAndView("drawings").addObject("drawings", drawings).addObject("project", p);

		return modelAndView;
	}
	
	@GetMapping(value = { "/reports" })
	public ModelAndView reportsList(@RequestParam(value = "parentid") Long drawingrefId, @RequestParam(value = "projectid") Long projectid) {
		
		ModelAndView modelAndView;
		
		ParamConfigurations conf = configRep.getDefaultConfig();
		DrawingRef drawing = drawingRep.getById(drawingrefId);
		Project project = drawing.getParent();
		List<Report> reports = drawing.getReports();

		modelAndView = new ModelAndView("reports")
				.addObject("reports", reports)
				.addObject("confs", conf)
				.addObject("drawing", drawing)
				.addObject("project", project);

		return modelAndView;
	}

	@GetMapping(value = { "/reports/{rid}" })
	public ResponseEntity<?> reportView(@PathVariable(value = "rid") Long reportid, HttpServletResponse response) throws WebException {
		AbstractResult res = exportService.getExportedReport(reportid);
		if(res instanceof SuccessResult) {
			ReportExportDTO dto;
			HttpHeaders headers;
			
			dto = ResultService.getSingleResult(res);
			
			headers = new HttpHeaders();
			headers.setExpires(-1);
			headers.setContentType(MediaType.valueOf(dto.getContentType()));
			headers.setContentLength(dto.getContent().length);
			headers.add("Content-Disposition", "inline; filename=" + dto.getFileName());
			
			return new ResponseEntity<byte[]>(dto.getContent(), headers, HttpStatus.OK);
		} else {
			ErrorResult err = new ErrorResult(ErrorCode.FILE_PREVIEW_FAILED, "Unable to show this document at this moment. Please try again later.", ErrorLevel.WARNING, getClass());
			throw new WebException(err, "/web/reports");
		}
	}
		
	@GetMapping(value = "/devices")
	public ModelAndView devicesGet(@RequestParam(value = "operation", required = false) String operation,
			@RequestParam(value = "deviceid", required = false) String deviceid, @ModelAttribute(value = "message") String message) {

		Map<String, String> pages = new HashMap<String, String>();
		// pages.put("add", "device");
		pages.put("edit", "device");
		pages.put("back", "redirect:/web");

		ModelAndView modelAndView = null;

		if (operation == null) {
			List<Device> devices = deviceRep.findAll();
			modelAndView = new ModelAndView("devices");
			modelAndView.addObject("Devices", devices);
		} else {
			modelAndView = new ModelAndView(pages.get(operation));
			if (operation.toLowerCase().equals("edit")) {
				Device device;
				device = deviceRep.getByDeviceid(deviceid);
				modelAndView.addObject("Device", device);
				modelAndView.addObject("rolesList", roleRep.findAll());
			}
		}
		
		modelAndView.addObject("message", message);

		return modelAndView;
	}
	
	@PostMapping(value = "/devices")
	public ModelAndView devicesPost(@RequestParam(value = "operation") String operation,
			@Validated @ModelAttribute(value = "Device") Device device, BindingResult result, RedirectAttributes attr) {

		if (result.hasErrors() && result.getFieldErrors().stream().filter(e -> !e.getField().equals("roles")).count() > 0) {
			return new ModelAndView("main");
		}

		ModelAndView modelAndView = new ModelAndView("redirect:../web/devices");;

		if (operation.toLowerCase().equals("save")) {
			String message = null;
			List<Device> devices = deviceRep.findAll();
			if(device.getName() == null || device.getName().isEmpty()) {
				message = "Name cannot be empty!";
			} else if (device.getRoles().size() == 0) {
				message = "At least one role must be selected!";
			} else if (device.getRoles().size() > 1 && device.getRoles().stream().filter(r -> r.getDescription().equals("TE")).count() >= 1) {
				message = "A device cannot have another roles along with a 'Technician' role!";
			} else if (devices.size() > 0 && devices.stream().filter(d -> d.getName() != null).anyMatch(d -> d.getName().equals(device.getName()) && d.getId() != device.getId())) {
				message = "Device name should be unique!";
			} else {
				AbstractResult res = entityService.postEntity(device, null, device.getClass().getSimpleName(), null, null);
				if(res instanceof ErrorResult) {
					ErrorResult err = ResultService.getErrorResult(res);
					message = err.getCode().concat(" - ").concat(err.getDescription());
				}
			}
			if (message != null) {
				attr.addFlashAttribute("message", message);
				modelAndView = new ModelAndView("redirect:../web/devices?operation=edit&deviceid=" + device.getDeviceid());
				return modelAndView;
			} else {
				attr.addFlashAttribute("message", "Changes saved!");
			}
		} else {
			attr.addFlashAttribute("message", "Operation cancelled!");
		}
		return modelAndView;
	}
	
	@ExceptionHandler(WebException.class)
	public ModelAndView handleWebError(final WebException e) {
		ErrorResult err;
		ModelAndView errMv;
		
		err = e.getErr();
		errMv = new ModelAndView("errorPage");
		errMv.addObject("code", err.getCode());
		errMv.addObject("message", err.getDescription());
		errMv.addObject("redirectPath", e.getPathToRedirect());
		
		return errMv;
	}
	
	@ModelAttribute("Device")
	private Device getDevice(HttpServletRequest request) {
		Device device = new Device();
		
		String[] roles = request.getParameterValues("roles");
		
		if(roles != null) {
			for(String role : roles) {
				Role r = roleRep.getByDescription(role);
				device.getRoles().add(r);
			}
		}
		
		return device;
	}

	// JSP METHODS - END
	// -----------------------------------------------------------------------------------------------

}
