package com.richard.weger.wqc.repository;

import com.richard.weger.wqc.domain.Device;

public interface DeviceRepository extends IRepository<Device> {
	public Device getByDeviceid(String deviceid);
}
