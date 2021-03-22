package com.richard.weger.wqc.faccade;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Role;
import com.richard.weger.wqc.domain.dto.FileDTO;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.result.ResultWithContent;
import com.richard.weger.wqc.result.SingleObjectResult;
import com.richard.weger.wqc.result.SuccessResult;
import com.richard.weger.wqc.service.DeviceService;
import com.richard.weger.wqc.service.EntityService;
import com.richard.weger.wqc.service.FileService;
import com.richard.weger.wqc.service.ProjectService;
import com.richard.weger.wqc.service.RoleService;

import jxl.common.Logger;

@RestController
@RequestMapping("/rest")
public class RestFaccade {
	
	public static String APP_VERSION = "2.7.1.1";
		
	@Autowired private ProjectService projectService;
	@Autowired private DeviceService deviceService;
	@Autowired private RoleService roleService;
	@Autowired private FileService fileService;
	@Autowired private EntityService entityService;
	
	Logger logger;
	
	public RestFaccade() {
		logger = Logger.getLogger(getClass());
	}
	
	// REST METHODS - BEGIN
	// -----------------------------------------------------------------------------------------------

	@GetMapping(value = "/qrcode/projects", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Project> projectLoad(@RequestParam(value = "qrcode") String qrCode) {
		
		AbstractResult res = projectService.getSingle(qrCode);
		
		if (res instanceof ResultWithContent) {
			Project project = ResultService.getSingleResult(res, Project.class);
			HttpHeaders headers = new HttpHeaders();
			headers.set("version", APP_VERSION);
			return new ResponseEntity<>(project, headers, HttpStatus.OK);
		}
		
		return entityService.objectlessReturn(res);
		
	}

	@PostMapping(value = "/qrcode/projects", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Project> projectCreate(@RequestParam(value = "qrcode") String qrCode, UriComponentsBuilder b) {

		AbstractResult res;
		res = projectService.createSingle(qrCode);

		if (res instanceof SuccessResult) {
			URI uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/rest/scan/projects/{qrCode}")
					.buildAndExpand(qrCode.replace("/", "").replace("\\","")).toUri();
			return ResponseEntity.created(uri).build();
		}
		
		return entityService.objectlessReturn(res);
	}
	
	@GetMapping(value = "/devices/{deviceid}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Device> deviceLoadWithAndroidId(@PathVariable(value = "deviceid") String deviceid) {

		AbstractResult res = deviceService.getSingle(deviceid);
		
		if (res instanceof ResultWithContent) {
			Device device = ResultService.getSingleResult(res, Device.class);
			HttpHeaders headers = new HttpHeaders();
			headers.set("version", APP_VERSION);
			return new ResponseEntity<>(device, headers, HttpStatus.OK);
		}
		
		return entityService.objectlessReturn(res);
	}
	
	@GetMapping(value = "/roles/{roleDescription}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Role> roleLoadByDescription(@PathVariable(value = "roleDescription") String roleDescription) {

		AbstractResult res = roleService.getByDescription(roleDescription);

		if (res instanceof ResultWithContent) {
			Role role = ResultService.getSingleResult(res, Role.class);
			HttpHeaders headers = new HttpHeaders();
			headers.set("version", APP_VERSION);
			return new ResponseEntity<>(role, headers, HttpStatus.OK);
		}
		
		return entityService.objectlessReturn(res);
	}
	
	@GetMapping(value="/{entity}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DomainEntity>> entitiesList(
			@RequestParam(value = "parentid", required = false) Long parentid,
			@PathVariable(value = "entity") String entityName
			) {
		
		AbstractResult res = entityService.entitiesList(parentid, entityName);
		
		if (res instanceof ResultWithContent) {
			HttpHeaders headers = new HttpHeaders();
			headers.set("version", APP_VERSION);
			List<DomainEntity> list = ResultService.getMultipleResult(res, DomainEntity.class);
			return new ResponseEntity<>(list, headers, HttpStatus.OK);
		}
		
		return entityService.objectListReturn(res);
		
	}
	
	@GetMapping(value="/{entity}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DomainEntity> entityGet(@PathVariable(value="id") Long id) {
		
		AbstractResult res = entityService.getEntity(id);
		
		if(res instanceof ResultWithContent) {
			HttpHeaders headers = new HttpHeaders();
			headers.set("version", APP_VERSION);
			DomainEntity e = ResultService.getSingleResult(res, DomainEntity.class);
			return new ResponseEntity<DomainEntity>(e, headers, HttpStatus.OK);
		}
		
		return entityService.objectlessReturn(res);
	}
			
	@RequestMapping(value="/{entity}", method = {RequestMethod.POST, RequestMethod.PUT}, produces = MediaType.APPLICATION_JSON_VALUE)
	public <T extends DomainEntity> ResponseEntity<T> entityPost( 
			@PathVariable(value="entity") String entityName, 
			@RequestParam(value="parentid", required=false) Long parentid,
			@RequestBody T entity,
			@RequestParam(value="qrcode") String qrcode) {
		
		AbstractResult res;
		
		try {
			res = entityService.postEntity(entity, parentid, entityName, qrcode);
		} catch (ObjectOptimisticLockingFailureException ex) {
			ErrorResult err = new ErrorResult(ErrorCode.STALE_ENTITY, "Your data is stale! Please try again.", ErrorLevel.WARNING, getClass());
			return entityService.objectlessReturn(err);
		}
		
		if (res instanceof SuccessResult) {
			SingleObjectResult<DomainEntity> oRes = ResultService.getSingleResultContainer(res, DomainEntity.class);
			
			
			if(oRes.isUpdated()) {
				return new ResponseEntity<T>(entity, HttpStatus.OK);
			} else {
				URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/rest/{entity}/{id}")
						.buildAndExpand(entityName, oRes.getObject().getId()).toUri();
				return ResponseEntity.created(uri).build();
			}
		}
		
		return entityService.objectlessReturn(res);
		
	}
	
	@DeleteMapping(value = "/{entity}")
	public ResponseEntity<DomainEntity> entityDelete(
			@RequestParam(value="id") Long id, @RequestParam(value="version") Long version,
			@RequestParam(value="qrcode") String qrcode) {
		
		AbstractResult res;
		
		try {	
			res = entityService.deleteEntity(id, version, qrcode);
		} catch (ObjectOptimisticLockingFailureException ex) {
			ErrorResult err = new ErrorResult(ErrorCode.STALE_ENTITY, "Your data is stale! Please try again.", ErrorLevel.WARNING, getClass());
			return entityService.objectlessReturn(err);
		}

		if (res instanceof SuccessResult) {
			return ResponseEntity.status(HttpStatus.OK).body(null);
		}
		
		return entityService.objectlessReturn(res);
	}

	@PostMapping(value = "/picture", consumes = {"multipart/form-data" } )
	public ResponseEntity<String> picUpload(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "qrcode") String qrCode, 
			@RequestParam(value = "filename") String fileName,
			@RequestParam(value = "pictype") int pictureType,
			@RequestParam(value = "id", required = false) Long itemId ) {

		HttpHeaders headers;
		AbstractResult res;
		res = fileService.pictureUpload(qrCode, fileName, file, pictureType, itemId);
		headers = new HttpHeaders();
		headers.set("version", APP_VERSION);

		if (res instanceof SuccessResult) {
			String newName = ResultService.getSingleResult(res, File.class).getName();
			headers = fileService.getHeadersWithFilenames(fileName, newName);
			headers.set("version", APP_VERSION);
			return ResponseEntity.ok().headers(headers).body(null);
		}
		
		return entityService.objectlessReturn(res);
	}
	
	@GetMapping(value = "/pdfdocuments")
	public ResponseEntity<List<FileDTO>> getExistingPdfs(
			@RequestParam(value = "qrcode") String qrCode) {
		
		List<FileDTO> existing = fileService.getExistingPdfs(qrCode);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("version", APP_VERSION);

		return new ResponseEntity<List<FileDTO>>(existing, headers, HttpStatus.OK);
	}

	@GetMapping(value = "/pdfdocument")
	public ResponseEntity<ByteArrayResource> getPdf(
			@RequestParam(value = "filename") String filename,
			@RequestParam(value = "qrcode") String qrcode) {
		
		HttpHeaders headers;
		AbstractResult res;
		
		headers = new HttpHeaders();
		headers.set("version", APP_VERSION);
//		headers.add("Content-Disposition", "inline; filename=" + filename);
		
		res = fileService.getOriginalPdf(filename, qrcode);
		
		if (res instanceof EmptyResult) {
			headers.add("fileName", filename);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(null);
		} else if(res instanceof SingleObjectResult) {
			return ResponseEntity.ok()
					.headers(headers)
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(ResultService.getSingleResult(res, ByteArrayResource.class));
		} else {
			headers = ResultService.getErrorHeaders(ResultService.getErrorResult(res));
			headers.set("version", APP_VERSION);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.headers(headers)
					.body(null);
		}
		
	}
	
	@GetMapping(value = "/picture")
	public ResponseEntity<ByteArrayResource> getPicture(
			@RequestParam(value = "filename") String filename,
			@RequestParam(value = "qrcode") String qrcode) {
		HttpHeaders headers;
		
		headers = new HttpHeaders();
		headers.set("version", APP_VERSION);
//		headers.add("Content-Disposition", "inline; filename=" + filename);
		
		AbstractResult res = fileService.getPicture(filename, qrcode);

		if (res instanceof EmptyResult) {
			headers.add("fileName", filename);
			headers.set("version", APP_VERSION);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(null);
		} else if (res instanceof SingleObjectResult) {
			ByteArrayResource iRes = ResultService.getSingleResult(res, ByteArrayResource.class);
			headers.add("LastModified", fileService.getPictureLastModifiedDate(filename, qrcode).toString());
			return ResponseEntity.ok()
					.headers(headers)
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(iRes);
		} else {
			headers = ResultService.getErrorHeaders(ResultService.getErrorResult(res));
			headers.set("version", APP_VERSION);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.headers(headers)
					.body(null);
		}
	}

	@GetMapping(value = "/pictures")
	public ResponseEntity<List<FileDTO>> getExistingPictures(
			@RequestParam(value = "qrcode") String qrCode,
			@RequestParam(value = "pictype") int pictureType) {
		
		List<FileDTO> existing = fileService.getExistingPictures(qrCode, pictureType);
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("version", APP_VERSION);

		return new ResponseEntity<List<FileDTO>>(existing, headers, HttpStatus.OK);
	}

	// REST METHODS - END
	// -----------------------------------------------------------------------------------------------
	
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<String> exceptionHandle(Exception ex){
		String message;
		ErrorResult err;
		
		message = "A generic fault has ocurred at the server";
		err = new ErrorResult(ErrorCode.GENERAL_SERVER_FAILURE, message, ErrorLevel.SEVERE, getClass());
		logger.fatal(message, ex);
		
		return entityService.objectlessReturn(err);
		
	}

}
