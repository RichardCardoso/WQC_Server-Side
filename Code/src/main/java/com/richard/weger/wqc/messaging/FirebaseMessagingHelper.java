package com.richard.weger.wqc.messaging;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.service.QrTextHandler;

import jxl.common.Logger;

@Service
public class FirebaseMessagingHelper implements IMessagingService {
	
	private Logger logger;
	
	@Autowired QrTextHandler qrHandler;
	
	@Override
	public void sendUpdateNotice(String qrcode, Long entityId, Long parentId) {
		// https://stackoverflow.com/questions/37576705/firebase-java-server-to-send-push-notification-to-all-devices
		
		logger = Logger.getLogger(getClass());
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
		post.setHeader("Content-type", "application/json");
		post.setHeader("Authorization", "key=AAAAt_sER9w:APA91bGP20YfUE9oy-zoDZb9WQkTZFhHB_TstnBJrZ0F7LKN3my8xZ8MgTYlBB6NqPALAuE-Q_xcfuPe3MyS7rVtF7nlzePMZURZANEE4u9xYwJC0AtiHJaH8uxMnLjYwus4F59hmSaP");
		
		JSONObject message = new JSONObject();
		message.put("to", "/topics/WQC2-0");
		message.put("priority", "high");
		
		JSONObject data = new JSONObject();
		data.put("qrCode", qrcode);
		data.put("id", entityId);
		data.put("parentId", parentId);
		
		message.put("data", data);
		
		post.setEntity(new StringEntity(message.toString(), "UTF-8"));
		
		try {
			HttpResponse response = client.execute(post);
			logger.info("Firebase message send result: " + response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			logger.fatal("Firebase message send error", e);
		}
		
	}
}
