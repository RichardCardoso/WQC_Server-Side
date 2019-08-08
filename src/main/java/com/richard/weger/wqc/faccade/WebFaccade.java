package com.richard.weger.wqc.faccade;

import static com.richard.weger.wqc.util.Logger.customLog;
import static com.richard.weger.wqc.util.Logger.writeData;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.repository.DeviceRepository;
import com.richard.weger.wqc.repository.DrawingRefRepository;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.repository.ProjectRepository;
import com.richard.weger.wqc.repository.ReportRepository;
import com.richard.weger.wqc.repository.RoleRepository;
import com.richard.weger.wqc.service.EntityService;
import com.richard.weger.wqc.service.EntityServiceResult;
import com.richard.weger.wqc.service.ReportService;
import com.richard.weger.wqc.util.TimeUtils;

@Controller
@RequestMapping(value = "/web" )
public class WebFaccade {
	
	@Autowired private HttpServletRequest request;

	@Autowired private ReportService reportService;
	
	@Autowired private ProjectRepository projectRep;
	@Autowired private DrawingRefRepository drawingRep;
	@Autowired private ReportRepository reportRep;
	@Autowired private ParamConfigurationsRepository configRep;
	@Autowired private DeviceRepository deviceRep;
	@Autowired private RoleRepository roleRep;
	@Autowired private EntityService entityService;
	
	HttpHeaders headers;

	public WebFaccade() {
		writeData("System started... Version 2.0.6.1");
	}

	public String getCurrentTime() {
		return (new TimeUtils()).getCurrentTime();
	}

	// JSP METHODS - BEGIN
	// -----------------------------------------------------------------------------------------------

	@RequestMapping(value = { "/" })
	public ModelAndView index(@ModelAttribute(value = "message") String message) {
		ModelAndView mv = new ModelAndView("main");
		mv.addObject("message", message);
		return mv;
	}

	@GetMapping(value = { "/projects" })
	public ModelAndView projectsList() {
		ModelAndView modelAndView;
		
		List<Project> projects = projectRep.findAll();

		modelAndView = new ModelAndView("projects").addObject("projects", projects);

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
	public String reportView(@PathVariable(value = "rid") Long reportid, HttpServletResponse response) {

		reportService.getPreviewFileResponse(response, reportid);
		return null;
	}

	@GetMapping(value = "/settings/")
	public ModelAndView settings() {
		ModelAndView modelAndView;
		
		ParamConfigurations conf = configRep.getDefaultConfig();

		modelAndView = new ModelAndView("setting");
		modelAndView.addObject("ParamConfigurations", conf);
		return modelAndView;
	}

	@PostMapping(value = "/settings/")
	public ModelAndView settings(@RequestParam(value = "operation") String operation,
			@Validated @ModelAttribute("ParamConfigurations") ParamConfigurations paramConfigurations, RedirectAttributes attr) {

		String result = null;
		ModelAndView mv = new ModelAndView("setting");
		
		mv.addObject("ParamConfigurations", paramConfigurations);

		if (operation.toLowerCase().equals("save")) {
			EntityServiceResult<ParamConfigurations> res = entityService.postEntity(paramConfigurations, null, paramConfigurations.getClass().getSimpleName());
			result = res.getMessage();
			if (result != null) {
				attr.addFlashAttribute("message", result);
			} else {
				attr.addFlashAttribute("message", "Changes saved!");
			}
		} else {
			attr.addFlashAttribute("message", "Operation cancelled!");
		}
		mv = new ModelAndView("redirect:/web/");
		return mv;
	}

	@GetMapping(value = "/devices/")
	public ModelAndView devicesGet(@RequestParam(value = "operation", required = false) String operation,
			@RequestParam(value = "deviceid", required = false) String deviceid, @ModelAttribute(value = "message") String message) {

		Map<String, String> pages = new HashMap<String, String>();
		// pages.put("add", "device");
		pages.put("edit", "device");
		pages.put("back", "redirect:/web/");

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

	@PostMapping(value = "/devices/")
	public ModelAndView devicesPost(@RequestParam(value = "operation") String operation,
			@Validated @ModelAttribute(value = "Device") Device device, BindingResult result, RedirectAttributes attr) {

		if (result.hasErrors() && result.getFieldErrors().stream().filter(e -> !e.getField().equals("roles")).count() > 0) {
			return new ModelAndView("main");
		}

		ModelAndView modelAndView = new ModelAndView("redirect:../devices/");;

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
				EntityServiceResult<Device> res = entityService.postEntity(device, null, device.getClass().getSimpleName());
				message = res.getMessage();
			}
			if (message != null) {
				attr.addFlashAttribute("message", message);
				modelAndView = new ModelAndView("redirect:../devices/?operation=edit&deviceid=" + device.getDeviceid());
				return modelAndView;
			} else {
				attr.addFlashAttribute("message", "Changes saved!");
			}
		} else {
			attr.addFlashAttribute("message", "Operation cancelled!");
		}
		return modelAndView;
	}

	// JSP METHODS - END
	// -----------------------------------------------------------------------------------------------
	
	
	private <T extends DomainEntity> List<T> getEntities(String resource, Class<T> clazz){
		List<T> entities = new ArrayList<>();
		RestTemplate template;
		URI uri;
		template = new RestTemplate();
		
		try {
			String url = resource;
			String query = null;
			if(resource.contains("?")) {
				url = resource.substring(0, resource.indexOf("?"));
				query = resource.substring(resource.indexOf("?") + 1);
			}
			String context = request.getContextPath();
			uri = new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), context.concat("/rest").concat(url), query, null);
			System.out.println(uri.toString());
			ResponseEntity<List<T>> response;
			response = template.exchange(
				uri, 
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<T>>() {}
			);
			if(response != null && response.getBody() != null) {
				entities = response.getBody();
				return entities;
			} else {
				return null;
			}
		} catch (Exception e) {
			customLog(new Throwable().getStackTrace(), e.getMessage(), getClass());
			return null;
		}
	}
	
	private <T extends DomainEntity> T getEntity(String resource, Class<T> clazz) {
		T entity = null;
		RestTemplate template;
		URI uri;
		template = new RestTemplate();
		try {
			String context = request.getContextPath();
			uri = new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), context.concat("/rest").concat(resource), null, null);
			System.out.println(uri.toString());
			ResponseEntity<T> response;
			response = template.exchange(
				uri, 
				HttpMethod.GET,
				null,
				clazz
			);
			if(response != null && response.getBody() != null) {
				entity = response.getBody();
			}
			return entity;
		} catch (Exception e) {
			customLog(new Throwable().getStackTrace(), e.getMessage(), getClass());
			return null;
		}
	}
	
	private <T extends DomainEntity> String postEntity(String resource, T e, Class<T> clazz){
		RestTemplate template;
		URI uri;
		template = new RestTemplate();
		
		HttpEntity<T> entity = new HttpEntity<T>(e);
		
		try {
			String context = request.getContextPath();
			uri = new URI(request.getScheme(), null, request.getServerName(), request.getServerPort(), context.concat("/rest").concat(resource), null, null);
			System.out.println(uri.toString());
			ResponseEntity<T> response = null;
			try {
				response = template.exchange(
					uri, 
					HttpMethod.POST,
					entity,
					clazz
				);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if(response != null && (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED)) {
				if(response.getHeaders().get("message") != null) {
					String message = response.getHeaders().get("message").get(0);
					if(response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
						if(message != null) {
							return message;
						}
					}
				}
			} else {
				return "Internal server error! Please try again later or contact your system admin!";
			}
			return null;
		} catch (URISyntaxException ex) {
			customLog(new Throwable().getStackTrace(), ex.getMessage(), getClass());
			return "An error has ocurred while trying to process your request";
		}
	}

}
