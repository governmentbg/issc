package com.ib.nsiclassif.opendata;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.system.BaseSystemData;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.RestClientException;

public class OpenDataRestClient {

	static final Logger LOGGER = LoggerFactory.getLogger(OpenDataRestClient.class);
	
	public void actionSaveNewData(BaseSystemData sd, OpenDataObj obj) throws DbErrorException, RestClientException {
		
		LOGGER.debug("Saving data to OpenData");
		
		Client client = ClientBuilder.newBuilder().build();
	    WebTarget target = client.target(sd.getSettingsValue("opendata.api.location"));
	    WebTarget path = target.path("addResourceData");
	    
		obj.setApi_key(sd.getSettingsValue("opendata.api.key"));
		obj.setExtension_format("csv");
		
		Response response = path.request().post(Entity.json(obj));
		
//		System.out.println(response.getStatus());
		
		if (response.getStatus()!=200) {					
			String readEntity = ""+response.readEntity(String.class);	
			
			LOGGER.error("Грешка при изпращане на данни към opendata портала: " + readEntity);
			throw new RestClientException("Грешка при изпращане на данни!");
		}
		client.close();
		
	}
	
	public void actionUpdateData(BaseSystemData sd, OpenDataObj obj) throws DbErrorException, RestClientException {
		
		LOGGER.debug("Updating data to OpenData");
		
		Client client = ClientBuilder.newBuilder().build();
		WebTarget target = client.target(sd.getSettingsValue("opendata.api.location"));
		WebTarget path = target.path("updateResourceData");
		
		obj.setApi_key(sd.getSettingsValue("opendata.api.key"));
		obj.setExtension_format("csv");
		
		Response response = path.request().post(Entity.json(obj));
		
//		System.out.println(response.getStatus());
		
		if (response.getStatus()!=200) {					
			String readEntity = ""+response.readEntity(String.class);	
			
			LOGGER.error("Грешка при изпращане на данни към opendata портала: " + readEntity);
			client.close();
			throw new RestClientException("Грешка при изпращане на данни!");
		}
		client.close();
		
	}
	
	 
}
