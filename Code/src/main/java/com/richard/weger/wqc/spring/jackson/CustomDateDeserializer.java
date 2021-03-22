package com.richard.weger.wqc.spring.jackson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.richard.weger.wqc.appconstants.FactoryAppConstants;

public class CustomDateDeserializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		ObjectCodec oc = p.getCodec();
		JsonNode node = oc.readTree(p);
		String msDateString = node.asText();
		
		if(msDateString == null || msDateString.length() == 0) {
			return null;
		}
		
		SimpleDateFormat sdf = FactoryAppConstants.getAppConstants().getSDF();
		Date date;
		try {
			date = sdf.parse(msDateString);
			return date;
		} catch (ParseException e) {
			throw new IOException("Invalid date format");
		}
	}
	
}
