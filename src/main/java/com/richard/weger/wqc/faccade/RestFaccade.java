package com.richard.weger.wqc.faccade;

import static com.richard.weger.wqc.util.Logger.customLog;
import static com.richard.weger.wqc.util.Logger.requestLog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.richard.weger.wqc.appconstants.FactoryAppConstants;
import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.ParentAwareEntity;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.exception.DuplicatedDataException;
import com.richard.weger.wqc.firebase.FirebaseMessagingHelper;
import com.richard.weger.wqc.repository.DeviceRepository;
import com.richard.weger.wqc.repository.DomainEntityRepository;
import com.richard.weger.wqc.repository.IRepository;
import com.richard.weger.wqc.repository.ParamConfigurationsRepository;
import com.richard.weger.wqc.repository.ParentAwareEntityRepository;
import com.richard.weger.wqc.repository.RoleRepository;
import com.richard.weger.wqc.service.EntityService;
import com.richard.weger.wqc.service.EntityServiceResult;
import com.richard.weger.wqc.service.ItemService;
import com.richard.weger.wqc.service.ProjectService;
import com.richard.weger.wqc.service.QrTextHandler;

@RestController
@RequestMapping("/rest")
public class RestFaccade {
	
	@Autowired private DomainEntityRepository rep;
	@Autowired private ParentAwareEntityRepository parentRep;
	
	@Autowired private ParamConfigurationsRepository paramConfigsRep;
	@Autowired private DeviceRepository deviceRep;
	@Autowired private RoleRepository roleRep;
	
	@Autowired private ProjectService projectService;
	@Autowired private ItemService itemService;
	
	@Autowired private QrTextHandler handler;
	
	@Autowired private EntityService entityService;

	@Autowired private FirebaseMessagingHelper firebase;
	
	// REST METHODS - BEGIN
	// -----------------------------------------------------------------------------------------------

	@GetMapping(value = "/qrcode/projects")
	public ResponseEntity<Project> projectLoad(@RequestParam(value = "qrcode") String qrCode) {
		
		Project project;

		project = projectService.getSingle(qrCode.replace("\\", "").replace("/", ""));

		if (project != null) {
			return new ResponseEntity<Project>(project, HttpStatus.OK);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	@PostMapping(value = "/qrcode/projects")
	public ResponseEntity<Project> projectCreate(@RequestParam(value = "qrcode") String qrCode, UriComponentsBuilder b) {
		customLog(new Throwable().getStackTrace(), "Creating project using Qr Code.", getClass());

		// Declaração de variáveis
		int result;
		requestLog(new Throwable().getStackTrace(), getClass());
		result = projectService.createSingle(qrCode.replace("\\", "").replace("/", ""));

		if (result >= 1) {
			// Fechar a conexão
			URI uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/rest/scan/projects/{qrCode}")
					.buildAndExpand(qrCode.replace("/", "").replace("\\","")).toUri();
			return ResponseEntity.created(uri).build();
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

	}
	
	@GetMapping(value = "/devices/{deviceid}")
	public ResponseEntity<Device> deviceLoadWithAndroidId(@PathVariable(value = "deviceid") String deviceid) {

		Device device = deviceRep.getByDeviceid(deviceid);

		if (device != null) {
			return new ResponseEntity<Device>(device, HttpStatus.OK);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
	
	@GetMapping(value = "/roles/{roleDescription}")
	public ResponseEntity<Role> roleLoadByDescription(@PathVariable(value = "roleDescription") String roleDescription) {

		Role role = roleRep.getByDescription(roleDescription);

		if (role != null) {
			return new ResponseEntity<Role>(role, HttpStatus.OK);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
	
	@GetMapping(value="/{entity}")
	public ResponseEntity<List<DomainEntity>> entitiesList(
			@RequestParam(value = "parentid", required = false) Long parentid,
			@PathVariable(value = "entity") String entityName
			) {
		
		List<DomainEntity> entities = null;
		
		entityName = entityName.substring(0, entityName.length() - 1);
		
		if(parentid != null && parentid > 0) {
			List<ParentAwareEntity> list = parentRep.getAllByParentIdAndTypeStartingWith(parentid, entityName); 
			entities = new ArrayList<DomainEntity>();
			entities.addAll(list);
		} else {
			entities = rep.getAllByTypeStartingWith(entityName);
		}
		
		if(entities == null) {
			entities = new ArrayList<DomainEntity>();
		}
		
		return new ResponseEntity<List<DomainEntity>>(entities, HttpStatus.OK);
		
	}
	
	@GetMapping(value="/{entity}/{id}")
	public ResponseEntity<DomainEntity> entityGet(@PathVariable(value="id") Long id) {
		DomainEntity e = rep.getById(id);
		
		if(e == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} else {
			return new ResponseEntity<DomainEntity>(e, HttpStatus.OK);
		}
	}
			
	@RequestMapping(value="/{entity}", method = {RequestMethod.POST, RequestMethod.PUT})
	public <T extends DomainEntity> ResponseEntity<T> entityPost( 
			@PathVariable(value="entity") String entityName, 
			@RequestParam(value="parentid", required=false) Long parentid,
			@RequestBody T entity,
			@RequestParam(value="qrcode") String qrcode) {
		
		HttpHeaders headers = new HttpHeaders();
		String message = null;
		boolean existingEntity = false;
		EntityServiceResult<T> result = new EntityServiceResult<>();
		
		try {
			result = entityService.postEntity(entity, parentid, entityName);
			existingEntity = result.isExistingEntity();
			message = result.getMessage();
		} catch (Exception ex) {
			if(ex instanceof ObjectOptimisticLockingFailureException) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
			} else if (ex instanceof DuplicatedDataException) {
				return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(null);
			} else {
				message = ex.getMessage();
			}
		}
		
		if(message != null) {
			customLog(new Throwable().getStackTrace(), message, getClass());
			headers.add("message", message);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(null);
		} else {
			Long id = 0L;
			
			firebase.sendUpdateNotice(qrcode);
			
			if(result.getEntity() != null) {
				id = result.getEntity().getId();
			}
			if(existingEntity) {
				customLog(new Throwable().getStackTrace(), "Updated entity " + entityName + " with id " + id, getClass());
				return new ResponseEntity<T>(entity, HttpStatus.OK);
			} else {
				customLog(new Throwable().getStackTrace(), "Created entity " + entityName + " with new id " + id, getClass());
				URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/rest/{entity}/{id}")
						.buildAndExpand(entityName, id).toUri();
				return ResponseEntity.created(uri).build();
			}
		}
		
	}
	
	@DeleteMapping(value = "/{entity}")
	public ResponseEntity<DomainEntity> entityDelete(
			@RequestParam(value="id") Long id, @RequestParam(value="version") Long version,
			@RequestParam(value="qrcode") String qrcode) {

		boolean result;
		
		try {
			DomainEntity e = rep.getById(id);
			if(e.getVersion() != version) {
				throw new ObjectOptimisticLockingFailureException(e.getClass(), id);
			}
			rep.deleteById(id);
		} catch (Exception ex) {
			if(ex instanceof ObjectOptimisticLockingFailureException) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
			} else {
				String message = ex.getMessage();
				HttpHeaders headers = new HttpHeaders();
				headers.add("message", message);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(null);
			}
		}
		
		result = (rep.getById(id) == null);

		if (result) {
			firebase.sendUpdateNotice(qrcode);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} else {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}

	}

	@PostMapping(value = "/picture", consumes = {"multipart/form-data" } )
	public ResponseEntity<String> picUpload(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "qrcode") String qrCode, 
			@RequestParam(value = "filename") String fileName,
			@RequestParam(value = "pictype") int pictureType,
			@RequestParam(value = "id", required = false) Long itemId ) {

		HttpHeaders httpHeaders;
		int result = 0;

		httpHeaders = new HttpHeaders();
		if(pictureType == 0 && itemId > 0) {
			result = itemService.pictureUpload(qrCode, fileName, file, httpHeaders);
		} else if (pictureType == 1) {
			result = projectService.generalPictureUpload(qrCode, fileName, file, httpHeaders);
		}

		if (result == 1) {
			return ResponseEntity.ok().headers(httpHeaders).body(null);
		} else {
			if(pictureType == 0 && (itemId == 0)) {
				httpHeaders.add("message", "Zeroed itemId '" + itemId + "'");
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(httpHeaders).body(null);
		}
	}

	@GetMapping(value = "/pdfdocument")
	public ResponseEntity<InputStreamResource> getPdf(
			@RequestParam(value = "filename") String fileName,
			@RequestParam(value = "qrcode") String qrcode) {
		customLog(new Throwable().getStackTrace(), "Sending pdf file " + fileName + " to the client.", getClass());
		HttpHeaders headers = new HttpHeaders();
		ByteArrayInputStream pdf;
		Map<String, String> mapValues;
		ParamConfigurations conf;
		
		conf = paramConfigsRep.getDefaultConfig();

		if (!fileName.endsWith(".pdf")) {
			fileName = fileName.concat(".pdf");
		}

		headers.add("Content-Disposition", "inline; filename=" + fileName);
		mapValues = handler.getParameters(qrcode);
		String filePath = "//"
				.concat(conf.getServerPath())
				.concat(conf.getRootPath())
				.concat(mapValues.get(FactoryAppConstants.getAppConstants().getTECHNICAL_PATH_KEY()))
				.concat(fileName);
		try {
			pdf = new ByteArrayInputStream(Files.readAllBytes(new File(filePath).toPath()));
		} catch (IOException e) {
			customLog(new Throwable().getStackTrace(),"There was a problem while trying to access the following file: '" + filePath + "'! Creation proccess aborted!", getClass());
			e.printStackTrace();
			return ResponseEntity.ok()
					.headers(headers)
					.contentType(MediaType.APPLICATION_PDF)
					.body(null);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
		return ResponseEntity.ok()
				.headers(headers)
				.contentType(MediaType.APPLICATION_PDF)
				.body(new InputStreamResource(pdf));
	}

	@GetMapping(value = "/pictures")
	public ResponseEntity<List<String>> getExistingPictures(
			@RequestParam(value = "qrcode") String qrCode,
			@RequestParam(value = "pictype") int pictureType) {
		
		List<String> existing = projectService.getExistingPictures(qrCode, pictureType);

		return new ResponseEntity<List<String>>(existing, HttpStatus.OK);
	}

	@GetMapping(value = "/picture")
	public ResponseEntity<InputStreamResource> getPicture(
			@RequestParam(value = "filename") String fileName,
			@RequestParam(value = "qrcode") String qrText) {
		customLog(new Throwable().getStackTrace(), ": Sending jpg file " + fileName + " to the client.", getClass());
		HttpHeaders headers = new HttpHeaders();
		ByteArrayInputStream jpg;
		Map<String, String> mapValues;
		ParamConfigurations conf;
		
		conf = paramConfigsRep.getDefaultConfig();

		mapValues = handler.getParameters(qrText);

		if (!fileName.endsWith(".jpg")) {
			fileName = fileName.concat(".jpg");
		}

		headers.add("Content-Disposition", "inline; filename=" + fileName);
		String filePath = "//"
				.concat(conf.getServerPath())
				.concat(conf.getRootPath())
				.concat(mapValues.get(FactoryAppConstants.getAppConstants().getCOMMON_PATH_KEY()))
				.concat("Fotos/")
				.concat(fileName);

		File file = new File(filePath);
		if (!file.exists()) {
			headers = new HttpHeaders();
			headers.add("fileName", fileName);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(null);
		}
		try {
			jpg = new ByteArrayInputStream(Files.readAllBytes(new File(filePath).toPath()));
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
		return ResponseEntity.ok().headers(headers).contentType(MediaType.IMAGE_JPEG)
				.body(new InputStreamResource(jpg));
	}

	// REST METHODS - END
	// -----------------------------------------------------------------------------------------------

}
