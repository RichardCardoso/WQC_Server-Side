package com.richard.weger.wqc.messaging;

public interface IMessagingService {
	public void sendUpdateNotice(String qrcode, Long entityId, Long parentId);
}
