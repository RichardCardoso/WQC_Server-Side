package com.richard.weger.wqc.spring.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.result.ErrorResult.ErrorCode;
import com.richard.weger.wqc.result.ErrorResult.ErrorLevel;
import com.richard.weger.wqc.result.MultipleObjectResult;

@Service
public class WebSocketHandler extends TextWebSocketHandler {
	
	Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		Logger logger = Logger.getLogger(WebSocketHandler.class);
		String msg = String.valueOf(message.getPayload());
		switch(msg) {
			default:
				logger.info("Connected to " + session.getRemoteAddress());
		}
		
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessions.add(session);
	}
	
	public AbstractResult broadcastMessage(String message) {
		List<ErrorResult> errors = new ArrayList<>();
		for(WebSocketSession s : sessions) {
			try {
				if(s.isOpen()) {
					s.sendMessage(new TextMessage(message));
				} else {
					sessions.remove(s);
				}
			} catch (Exception e) {
				String remoteAddr = "UNKNOWN";
				try {
					remoteAddr = s.getRemoteAddress().getHostString();
				} catch (Exception ex) {
					
				}
				errors.add(new ErrorResult(ErrorCode.MESSAGE_BROADCAST_FAILED, "Failed to deliver message to " + remoteAddr + " with message=" + e.getMessage(), ErrorLevel.WARNING, WebSocketHandler.class));
			}
		}
		if(errors.size() > 0) {
			return new MultipleObjectResult<>(ErrorResult.class, errors);
		}
		return new EmptyResult();
	}

}
