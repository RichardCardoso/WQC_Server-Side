package com.richard.weger.wqc.service;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.domain.Device;
import com.richard.weger.wqc.repository.DeviceRepository;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.SingleObjectResult;

@Service
public class DeviceService {
	
	@Autowired private DeviceRepository deviceRep;
	
	public AbstractResult getSingle(String deviceid) {
		Device device;
		
		if(Strings.isEmpty(deviceid)) {
			return new ErrorResult(ErrorCode.INVALID_ENTITY_ID, "An invalid device id was received!", ErrorLevel.SEVERE, getClass());
		}
		
		device = deviceRep.getByDeviceid(deviceid);
		if(device != null) {
			return new SingleObjectResult<>(Device.class, device);
		} else {
			return new EmptyResult();
		}
	}
}
