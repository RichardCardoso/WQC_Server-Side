package com.richard.weger.wqc.messaging;

import java.util.UUID;

public class MessagingDTO {
	
	private String messageId;
	private String qrcode;
	private Long id;
	private Long parentId;
	
	public MessagingDTO() {
		messageId = UUID.randomUUID().toString();
	}
	
	public String getMessageId() {
		return messageId;
	}
	
	public String getQrcode() {
		return qrcode;
	}
	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	
}
