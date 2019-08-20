package com.richard.weger.wqc.firebase;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.richard.weger.wqc.service.QrTextHandler;

@Service
public class FirebaseMessagingHelper {
	
	@Autowired QrTextHandler qrHandler;
	
	public void sendUpdateNotice(String qrcode) {
		// https://stackoverflow.com/questions/37576705/firebase-java-server-to-send-push-notification-to-all-devices
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
		post.setHeader("Content-type", "application/json");
		post.setHeader("Authorization", "key=AAAAt_sER9w:APA91bGP20YfUE9oy-zoDZb9WQkTZFhHB_TstnBJrZ0F7LKN3my8xZ8MgTYlBB6NqPALAuE-Q_xcfuPe3MyS7rVtF7nlzePMZURZANEE4u9xYwJC0AtiHJaH8uxMnLjYwus4F59hmSaP");
		
		JSONObject message = new JSONObject();
		message.put("to", "/topics/wqc-2.0");
		message.put("priority", "high");
		
		JSONObject data = new JSONObject();
		data.put("qrCode", qrcode);
		
		message.put("data", data);
		
		post.setEntity(new StringEntity(message.toString(), "UTF-8"));
//		HttpResponse response = null;
		
		try {
//			response = client.execute(post);
			client.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
