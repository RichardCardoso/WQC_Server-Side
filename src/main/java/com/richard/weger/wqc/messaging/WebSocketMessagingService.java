package com.richard.weger.wqc.messaging;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ResultService;
import com.richard.weger.wqc.spring.websocket.WebSocketHandler;

@Service
public class WebSocketMessagingService implements IMessagingService {
	
	@Autowired WebSocketHandler handler;

	@Override
	public void sendUpdateNotice(String qrcode, Long entityId, Long parentId) {
		MessagingDTO dto = new MessagingDTO();
		dto.setQrcode(qrcode);
		dto.setId(entityId);
		dto.setParentId(parentId);
		Gson gson = new Gson();
		String content = gson.toJson(dto);
		AbstractResult result = handler.broadcastMessage(content);
		if(!(result instanceof EmptyResult) )	{
			List<ErrorResult> errors = ResultService.getMultipleResult(result, ErrorResult.class);
			errors.forEach(e -> Logger.getLogger(WebSocketMessagingService.class).warn(e.getDescription()));
		}
	}

}
