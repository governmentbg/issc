package com.ib.nsiclassif.opendata;

import java.util.LinkedHashMap;
import java.util.Map;

public class OpenDataObj {

	private String api_key;
	private String resource_uri;
	private String extension_format;
	
	private Map<String, Object[]> data = new LinkedHashMap<String, Object[]>();
	

	public String getApi_key() {
		return api_key;
	}

	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	public Map<String, Object[]> getData() {
		return data;
	}

	public void setData(Map<String, Object[]> data) {
		this.data = data;
	}

	public String getResource_uri() {
		return resource_uri;
	}

	public void setResource_uri(String resource_uri) {
		this.resource_uri = resource_uri;
	}

	public String getExtension_format() {
		return extension_format;
	}

	public void setExtension_format(String extension_format) {
		this.extension_format = extension_format;
	}

	 
	 
}
