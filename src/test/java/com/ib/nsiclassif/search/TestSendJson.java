package com.ib.nsiclassif.search;

import org.junit.Test;

public class TestSendJson {
	/** KOMENTIRANO ZA DA NE VKARVAT HORATA SLU4aino danni  
	@Test
    public void testRestEasy() throws UnsupportedEncodingException {
		System.out.println("=== START ===");

		 Client client = ClientBuilder.newBuilder().build();
	     WebTarget target = client
	            .target("https://testdata.egov.bg/api/");
	       
		 
		WebTarget path = target.path("listUsers");
		String data = 
				"{\r\n"
				+ "\"api_key\" : \"aa\",\r\n"
				+ "\"records_per_page\": 2,\r\n"
				+ "\"page_number\": 2,\r\n"
				+ "\"criteria\": {\r\n"
				+ "\"active\" : 1\r\n"
				+ "},\r\n"
				+ "\"order\": {\r\n"
				+ "\"field\": \"name\"\r\n"
				+ "}\r\n"
				+ "}";
//		OpenDataObj ob=new OpenDataObj();
//		ob.setApi_key("4a34ab9a-e3ab-4444-b074-21e5092357e4");
//		Response response = path.request().post(Entity.json(ob));
		Response response = path.request().post(Entity.json(data));
		
		
		
		String readEntity = ""+response.readEntity(String.class);
		
		
		
		System.out.println(StringEscapeUtils.unescapeJson(readEntity));
		client.close();
		System.out.println("=== end ===");
    }
	@Test
	public void testJson() {
		OpenDataObj ob=new OpenDataObj();
		ob.setApi_key("4a34ab9a-e3ab-4444-b074-21e5092357e4");
		 
		String[] headerNames=new String[3];
 		headerNames[0]="Код източник";
 		headerNames[1]="Код цел";
 		headerNames[2]="Символ";
		
		
		String[] da=new String[3];
 		da[0]="1";
 		da[1]="2";
 		da[2]="c";
 		
		ob.getData().put("headers", headerNames);
		ob.getData().put("row1", da);
		ob.getData().put("row2", da);
		
		 
//		ob.getData().getRow().add(headerNames);
//		ob.getData().getRow().add(headerNames);
//		dp.setRow(null);
		
		Gson g=new Gson();
		
		
		System.out.println(g.toJson(ob));
	}
	
	@Test
	public void addResourceMetadata()  {
		try {
			URL url = new URL("https://testdata.egov.bg/api/addResourceMetadata");
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.setRequestProperty("Accept", "application/json");
			http.setRequestProperty("Content-Type", "application/json");
			 
			String data = 
					"{\r\n"
					+ "\"api_key\" : \"4a34ab9a-e3ab-4444-b074-21e5092357e4\",\r\n"
					+ "\"dataset_uri\" : \"6b5726b9-984f-4bbf-a48a-71946c106e01\" ,\r\n"
					+ "\"data\" : {\r\n"
					+ "\"locale\" : \"bg\",\r\n"
					+ "\"name\" : \"Ресурс от java-ta\",\r\n"
					+ "\"type\" : 4,\r\n"
					+ "\"description\" : \"Класификация тест\",\r\n"
					+ "\"category_id\" : 7,\r\n"
					+ "\"support_email\" : \"autoJava@mail.bg\"\r\n"
					+ "}\r\n"
					+ "}";
			
//		String data = 
//				 "{\r\n"
//				+ "\"api_key\" : \"4a34ab9a-e3ab-4444-b074-21e5092357e4\" ,\r\n"
//				+ "\"dataset_uri\" : \"431c1e33-a690-452b-bac2-f90ffd6404bf\"\r\n"
//				+ "}";
			
//		String data = "{\r\n"
//				+ "\"api_key\" : \"4f7be417-16cd-492c-bb2a-03a5a66c175a\" ,\r\n"
//				+ "\"resource_uri\" : \"3493ba53-db5b-4b2e-ba0d-183faa2f07cd\" ,\r\n"
//				+ "\"extension_format\" : \"csv\",\r\n"
//				+ "\"data\" : {\r\n"
//				+ "\"headers\" : [\"Данни, Месец, Брой\"],\r\n"
//				+ "\"row1\" : [\"тестови данни, Май, 4\"]\r\n"
//				+ "}\r\n"
//				+ "}";
			
			byte[] out = data.getBytes(StandardCharsets.UTF_8);
			
			OutputStream stream = http.getOutputStream();
			stream.write(out);
			
//			System.out.println("ааааааа "+ http.getResponseCode() + " " + http.getResponseMessage());
			if (http.getResponseCode()>200) {
				try(BufferedReader br = new BufferedReader(
						new InputStreamReader(http.getErrorStream(), "UTF-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					System.out.println(StringEscapeUtils.unescapeJson(response.toString()));
				}
			}else {
				try(BufferedReader br = new BufferedReader(
						new InputStreamReader(http.getInputStream(), "UTF-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					System.out.println(StringEscapeUtils.unescapeJson(response.toString()));
				}
			}
			
			http.disconnect();
			
			
			
			
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}  
		
	}
	@Test
	public void addResourceData()  {
		try {
			URL url = new URL("https://testdata.egov.bg/api/addResourceData");
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.setRequestProperty("Accept", "application/json");
			http.setRequestProperty("Content-Type", "application/json");

			String data = 
					 "{\r\n"
					 + "\"api_key\" : \"4a34ab9a-e3ab-4444-b074-21e5092357e4\" ,\r\n"
					 + "\"resource_uri\" : \"793315af-93d6-4a99-a4b3-43d33e58a775\" ,\r\n"
					 + "\"extension_format\" : \"csv\",\r\n"
					 + "\"data\" : {\r\n"
					 + "\"headers\" : [\"Данни, Месец, Брой\"],\r\n"
					 + "\"row1\" : [\"тестови данни, Май, 4\"]\r\n"
					 + "}\r\n"
					 + "}";
			
 

			byte[] out = data.getBytes(StandardCharsets.UTF_8);

			OutputStream stream = http.getOutputStream();
			stream.write(out);

//			System.out.println("ааааааа "+ http.getResponseCode() + " " + http.getResponseMessage());
			if (http.getResponseCode()>200) {
				try(BufferedReader br = new BufferedReader(
						  new InputStreamReader(http.getErrorStream(), "UTF-8"))) {
						    StringBuilder response = new StringBuilder();
						    String responseLine = null;
						    while ((responseLine = br.readLine()) != null) {
						        response.append(responseLine.trim());
						    }
						   System.out.println(StringEscapeUtils.unescapeJson(response.toString()));
						}
			}else {
				try(BufferedReader br = new BufferedReader(
					  new InputStreamReader(http.getInputStream(), "UTF-8"))) {
					    StringBuilder response = new StringBuilder();
					    String responseLine = null;
					    while ((responseLine = br.readLine()) != null) {
					        response.append(responseLine.trim());
					    }
					    System.out.println(StringEscapeUtils.unescapeJson(response.toString()));
					}
			}
			
			http.disconnect();
			
			
			 
			
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}  

	}
	@Test
	public void updateResourceData()  {
		try {
			URL url = new URL("https://testdata.egov.bg/api/updateResourceData");
			HttpURLConnection http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.setRequestProperty("Accept", "application/json");
			http.setRequestProperty("Content-Type", "application/json");
			
			String data = 
					"{\r\n"
							+ "\"api_key\" : \"4a34ab9a-e3ab-4444-b074-21e5092357e4\" ,\r\n"
							+ "\"resource_uri\" : \"793315af-93d6-4a99-a4b3-43d33e58a775\" ,\r\n"
							+ "\"extension_format\" : \"csv\",\r\n"
							+ "\"data\" : {\r\n"
							+ "\"headers\" : [\"Данни, Месец, Брой\"],\r\n"
							+ "\"row1\" : [\"тестови данни, Август, 10\"]\r\n"
							+ "}\r\n"
							+ "}";
			
			
			
			byte[] out = data.getBytes(StandardCharsets.UTF_8);
			
			OutputStream stream = http.getOutputStream();
			stream.write(out);
			
			if (http.getResponseCode()>200) {
				try(BufferedReader br = new BufferedReader(
						new InputStreamReader(http.getErrorStream(), "UTF-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					System.out.println(StringEscapeUtils.unescapeJson(response.toString()));
				}
			}else {
				try(BufferedReader br = new BufferedReader(
						new InputStreamReader(http.getInputStream(), "UTF-8"))) {
					StringBuilder response = new StringBuilder();
					String responseLine = null;
					while ((responseLine = br.readLine()) != null) {
						response.append(responseLine.trim());
					}
					System.out.println(StringEscapeUtils.unescapeJson(response.toString()));
				}
			}
			
			http.disconnect();
			
			
			
			
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}  
		
	}
//	**/	
//	@Test
//	public void readUnicode() {
//		String s="\\u0413\\u0440\\u0435\\u0448\\u043a\\u0430 \\u043f\\u0440\\u0438 \\u0434\\u043e\\u0431\\u0430\\u0432\\u044f\\u043d\\u0435 \\u043d\\u0430 \\u0434\\u0430\\u043d\\u043d\\u0438 \\u0437\\u0430 \\u0440\\u0435\\u0441\\u0443\\u0440\\u0441";
////		String s="{"success":false,"status":500,"errors":{"resource_uri":["\u041f\u043e\u043b\u0435 \u0443\u043d\u0438\u043a\u0430\u043b\u0435\u043d \u0438\u0434\u0435\u043d\u0442\u0438\u0444\u0438\u043a\u0430\u0442\u043e\u0440 \u043d\u0430 \u0440\u0435\u0441\u0443\u0440\u0441 \u0435 \u043d\u0435\u0432\u0430\u043b\u0438\u0434\u043d\u043e."]},"error":{"type":"\u041e\u0431\u0449\u0430","message":"\u0412\u044a\u0437\u043d\u0438\u043a\u043d\u0430 \u0433\u0440\u0435\u0448\u043a\u0430 \u043f\u0440\u0438 \u043e\u0431\u043d\u043e\u0432\u044f\u0432\u0430\u043d\u0435 \u043d\u0430 \u0434\u0430\u043d\u043d\u0438 \u0437\u0430 \u0440\u0435\u0441\u0443\u0440\u0441."}}";
//		System.out.println(org.apache.commons.lang3.StringEscapeUtils.escapeJson(s));
//		
//	}

}
