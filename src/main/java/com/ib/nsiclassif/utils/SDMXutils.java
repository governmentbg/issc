package com.ib.nsiclassif.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.utils.JSonUtils;


public class SDMXutils {
	
	
	
	
	public static void createSDMX(int idObj,File file, int skipFirst, List<Object[]> result, List<Boolean> includedInReport, boolean pos, int progress) {
		//corresponding tables only
		
		if(pos) {
			includedInReport=new ArrayList<Boolean>();
			Boolean[] arr=new Boolean[result.size()];
			Arrays.fill(arr, Boolean.TRUE);
			includedInReport.addAll(Arrays.asList(arr));
		}
		String separator=",";

		
		
		try {
			

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			org.w3c.dom.Document document = null;
			List<Integer> includedAttributes= new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idObj);
			Collections.sort(includedAttributes);
			
			try {
				// optional, but recommended
				// process XML securely, avoid attacks like XML External Entities (XXE)
				dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

				// parse XML file
				DocumentBuilder db = dbf.newDocumentBuilder();
				// The class loader that loaded the class
				//URL urlConfig = DownloadFileExp.class.getResource("//export//SDMXtemplate.xml" );
				InputStream is = DownloadFileExp.class.getClassLoader().getResourceAsStream("/SDMXtemplate.xml");
				copyInputStreamToFile(is, file);
			       
				//LOGGER.info("File Size "+file.getTotalSpace());
				document = db.parse(file);
				
				if(skipFirst==1) {
					//ako sme v ralacii
					List<Obs> observations = new ArrayList<Obs>();
					for (int i = 0; i < result.size(); i++) {
						Object[] temp = result.get(i);
						if(includedInReport.get(i) ) {
							observations.add(new Obs("" + temp[2], "" + temp[1], "" + temp[3]));
						}
					}
					createHeaders(document);
					createObservations(document, observations, separator);
				}else {
					//ako sme vav versii
					ArrayList<CodeItem> codes = new ArrayList<CodeItem>();
//					for(int i=0;i<listPost.size();i++) {
//						PositionS pos= listPost.get(i);
//						Set <Integer> langs=pos.getLangMap().keySet();
//						String[] names= new String[langs.size()];
//						String[] descriptions=new String[langs.size()];
//						int j=0;
//						for (Integer lang : langs) {
//							PositionLang pl=pos.getLangMap().get(lang);
//							names[j]=pl.getLongTitle();
//							descriptions[j]=pl.getComment();
//							j++;
//						}
//						CodeItem item=new CodeItem(pos.getCode(), names, descriptions);
//						codes.add(item);
//					}
					for (int i = 0; i < result.size(); i++) {
								
						if(includedInReport.get(i) ) {
							Object[] ob= result.get(i);
							String[] names= {""+ob[3+includedAttributes.indexOf(11)]};
							String[] descriptions= {""+ob[3+includedAttributes.indexOf(15)]};
							CodeItem item=new CodeItem(""+ob[3+includedAttributes.indexOf(3)], names, descriptions);
							codes.add(item);
						}

					}

					createHeaders(document);
					createCodeListContent(document,codes);
					//da se izgradi nakraq hierarchy
				}
				

				DOMSource source = new DOMSource(document);
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","1");
				FileOutputStream f= new FileOutputStream(file);
				StreamResult res = new StreamResult(f);
				
				//StreamResult result = new StreamResult("versii.xml");
				transformer.transform(source, res);
				
				
				
				f.flush();
				f.close();

			} catch (ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
	
	public static void createSDMXJ(int idObj, File file, int skipFirst,List<Object[]> result,List<Boolean> includedInReport, boolean pos) {
		if(pos) {
			includedInReport=new ArrayList<Boolean>();
			Boolean[] arr=new Boolean[result.size()];
			Arrays.fill(arr, Boolean.TRUE);
			includedInReport.addAll(Arrays.asList(arr));
		}
		
		try {
			
			List<Integer> includedAttributes= new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idObj);
			Collections.sort(includedAttributes);

			//Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (FileWriter fw = new FileWriter(file)) {
				Header h = createHeaders();
				String json="";
				//corresponding tables only
				if(skipFirst==1) {
					JSONContent content=null;
					List<RelationshipItem> corespondingCodes = new ArrayList<RelationshipItem>();
					for (int i = 0; i < result.size(); i++) {
						Object[] temp = result.get(i);	
						if(includedInReport.get(i) ) {
							corespondingCodes.add(new RelationshipItem("" + (temp[0]==null?"":temp[0]), "" + (temp[1]==null?"":temp[1]), 
									"" + (temp[2]==null?"":temp[2]), "" + (temp[3]==null?"":temp[3])));
						}
					}
					ArrayList<Attributes> codeItemsJSON = classifToCodeListJSON(corespondingCodes);
					 content = new JSONContent(h, codeItemsJSON);
					 json=JSonUtils.object2json(content, true);
				}else {
					ArrayList<CodeItem> codes = new ArrayList<CodeItem>();
//					for(int i=0;i<listPost.size();i++) {
//						PositionS pos= listPost.get(i);
//						Set <Integer> langs=pos.getLangMap().keySet();
//						String[] names= new String[langs.size()];
//						String[] descriptions=new String[langs.size()];
//						int j=0;
//						for (Integer lang : langs) {
//							PositionLang pl=pos.getLangMap().get(lang);
//							names[j]=pl.getLongTitle();
//							descriptions[j]=pl.getComment();
//							j++;
//						}
//						CodeItem item=new CodeItem(pos.getCode(), names, descriptions);
//						codes.add(item);
//					}
					for (int i = 0; i < result.size(); i++) {	
						Object[] ob= result.get(i);
						if(includedInReport.get(i) ) {
							String[] names= {""+ob[3+includedAttributes.indexOf(11)]};
							String[] descriptions= {""+ob[3+includedAttributes.indexOf(15)]};
							CodeItem item=new CodeItem(""+ob[3+includedAttributes.indexOf(3)], names, descriptions);
							codes.add(item);
						}
						
						}
					
					
					
					JSONContentV content=null;
					ArrayList<CodeItemJSON> codeItemsV=classifToCodeListJSON(codes);
					
					 content = new JSONContentV(h, codeItemsV);
					 json=JSonUtils.object2json(content, true);
				}
				
//				if(skipFirst==3) {
//					JSONContent content=null;
//					List<RelationshipItem> corespondingCodes = new ArrayList<RelationshipItem>();
//					for (int i = 0; i < result.size(); i++) {
//						Object[] temp = result.get(i);
//						corespondingCodes.add(new RelationshipItem("" + (temp[0]==null?"":temp[0]), "" + (temp[1]==null?"":temp[1]), 
//								"" + (temp[2]==null?"":temp[2]), "" + (temp[3]==null?"":temp[3])));
//					}
//					ArrayList<Attributes> codeItemsJSON = classifToCodeListJSON(corespondingCodes);
//					 content = new JSONContent(h, codeItemsJSON);
//					 json=JSonUtils.object2json(content, true);
//				}else {
//					JSONContentV content=null;
//					ArrayList<CodeItemJSON> data=classifToCodeListJSON(this.codes);
//					 content = new JSONContentV(h, data);
//					 json=JSonUtils.object2json(content, true);
//				}		
				//gson.toJson(content, fw);
				
				fw.write(json);
				fw.flush();
				fw.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			JPA.getUtil().closeConnection();
		}

	}
	
	
	
	
	/*==============================================================================================================================================
																SDMX Utils and structures
	=================================================================================================================================================*/
	
	 private static void copyInputStreamToFile(InputStream inputStream, File file)
	            throws IOException {

	        // append = false
	        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
	            int read;
	            byte[] bytes = new byte[8192];
	            while ((read = inputStream.read(bytes)) != -1) {
	                outputStream.write(bytes, 0, read);
	            }
	        }

	    }
	/* Headers for the xml files*/
	public static void createHeaders(org.w3c.dom.Document document) {
	

		String INDENTIFIER = "IDREF1";
		String SENDER = "NSIsystemClassif";
		String RECEIVER = "INDEXBG";

		Collection<Header> headers = new ArrayList<Header>();
		Element root = document.getDocumentElement();
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		Header h = new Header(INDENTIFIER, sdf.format(today), SENDER, RECEIVER);
		headers.add(h);

		for (Header header : headers) {
			// header elements
			Element headerEl = document.createElement("mes:Header");

			// id
			Element id = document.createElement("mes:ID");
			id.appendChild(document.createTextNode(header.getId()));
			headerEl.appendChild(id);

			// prepared
			Element prepared = document.createElement("mes:Prepared");
			prepared.appendChild(document.createTextNode(header.getPrepared()));
			headerEl.appendChild(prepared);

			// sender
			Element sender = document.createElement("mes:Sender");
			sender.appendChild(document.createTextNode(header.getSender()));
			headerEl.appendChild(sender);

			// receiver
			Element receiver = document.createElement("mes:Receiver");
			receiver.setAttribute("id", "Unknown");
			receiver.appendChild(document.createTextNode(header.getReceiver()));
			headerEl.appendChild(receiver);

			root.appendChild(headerEl);
		}

	}
	
	public static void createCodeListContent(org.w3c.dom.Document  document,Collection<CodeItem> codes) {
		Element root = document.getDocumentElement();
		Element structureEl = document.createElement("mes:Structures");
		Element codeListEl = document.createElement("str:Codelists");
		for (CodeItem item : codes) {
			// codeItem elements
			Element codeItemEl = document.createElement("str:Code");
			codeItemEl.setAttribute("id", item.getCode());
			codeItemEl.setAttribute("urn", "urn:sdmx:org.sdmx.infomodel.codelist.Codelist="+item.getCode());
			codeItemEl.setAttribute("isFinal", "true");//dali klasifikaciata e finalna
			codeItemEl.setAttribute("version", "1.2");//versia na klasifikaciata
			
			// names
			if(!item.getNames()[0].isEmpty()) {
				Element name = document.createElement("com:Name");
				name.setAttribute("xml:lang", "bg");
				name.appendChild(document.createTextNode(item.getNames()[0]));
				codeItemEl.appendChild(name);
			}
//			if(!item.getNames()[1].isEmpty()) {
//				Element name = document.createElement("com:Name");
//				name.setAttribute("xml:lang", "eng");
//				name.appendChild(document.createTextNode(item.getNames()[1]));
//				codeItemEl.appendChild(name);
//			}
		  // descriptions
          if(!item.getDescriptions()[0].isEmpty()) {
            Element description = document.createElement("com:Description");
            description.setAttribute("xml:lang", "bg");
            description.appendChild(document.createTextNode(item.getDescriptions()[0]));
            codeItemEl.appendChild(description);
          }
//          if(!item.getDescriptions()[1].isEmpty()) {
//            Element description = document.createElement("com:Description");
//            description.setAttribute("xml:lang", "eng");
//            description.appendChild(document.createTextNode(item.getDescriptions()[1]));
//            codeItemEl.appendChild(description);
//          }
          codeListEl.appendChild(codeItemEl);
		}

		root.appendChild(structureEl);
		Element str = (Element) root.getElementsByTagName("mes:Structures").item(0);
		str.appendChild(codeListEl);
		
	}
	
	
	/* Headers for the json files*/
	public static Header createHeaders() {

		StringBuffer sb = new StringBuffer();

		String INDENTIFIER = "IDREF1";
		String SENDER = "NSIsystemClassif";
		String RECEIVER = "INDEXBG";

		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		Header h = new Header(INDENTIFIER, sdf.format(today), SENDER, RECEIVER);

		return h;

	}

	/* Transforming function for the sdmx-json export */
	public static ArrayList<Attributes> classifToCodeListJSON(List<RelationshipItem> corespondingCodes) {
		ArrayList<Attributes> attr = new ArrayList<Attributes>();
		Map<String, Integer> usedTargetCodes = new HashMap<String, Integer>();
		ArrayList<Object> dataset1 = new ArrayList<Object>();
		ArrayList<Object> series1 = new ArrayList<Object>();
		ArrayList<Observation> observs = new ArrayList<Observation>();
		Attributes attribut = new Attributes(dataset1, series1, observs);
		attr.add(attribut);

		// if in the sytem there is such target code already
		for (int i = 0; i < corespondingCodes.size(); i++) {
			RelationshipItem item = corespondingCodes.get(i);

			if (usedTargetCodes.get(item.getCodeTarget()) != null) {
				int index = usedTargetCodes.get(item.getCodeTarget());
				Attributes atr = attr.get(0);
				ObservationContent obContent = new ObservationContent(item.getCodeSource(), item.getDescription());
				ArrayList<ObservationContent> vals = new ArrayList<ObservationContent>();
				vals.add(obContent);
				Observation observation = new Observation(item.getCodeTarget(), vals);
				observs = atr.getObservation();
				observs.add(observation);
				attr.set(0, atr);
			}
			ObservationContent obContent = new ObservationContent(item.getCodeSource(), item.getDescription());
			ArrayList<ObservationContent> vals = new ArrayList<ObservationContent>();
			vals.add(obContent);
			Observation observation = new Observation(item.getCodeTarget(), vals);
			observs.add(observation);
			usedTargetCodes.put(item.getCodeTarget(), i);
		}

		return attr;
	}
	
	
	/* Transforming function for the sdmx export */
	public static void createObservations(org.w3c.dom.Document document,Collection<Obs> observations, String separator){
		Element root = document.getDocumentElement();
		Element structureEl = document.createElement("mes:DataSet");
		Element codeListEl = document.createElement("Series");
		codeListEl.setAttribute("EXR_TYPE",separator);// for delimeter there
		codeListEl.setAttribute("TITLE", "classifA");//the name of the classification value
		codeListEl.setAttribute("SOURCE_AGENCY", "1.2");//where is the classification from 
		for (Obs item : observations) {
			// for the different value of particulat value from classification
			Element codeItemEl = document.createElement("Obs");
			codeItemEl.setAttribute("TIME_PERIOD", item.getTimePeriod());// for delimeter
			codeItemEl.setAttribute("OBS_VALUE", item.getObsValue());// for delimeter
			codeItemEl.setAttribute("OBS_STATUS", item.getObsStatus());// for delimeter

          codeListEl.appendChild(codeItemEl);
		}

		root.appendChild(structureEl);
		Element str = (Element) root.getElementsByTagName("mes:DataSet").item(0);
		str.appendChild(codeListEl);
		
	}
	
	
	public static  ArrayList<CodeItem> classifToCodeList(Map<String, String> classif) {
		ArrayList<CodeItem> codes = new ArrayList<CodeItem>();
		for(String id:classif.keySet()) {
			String classifInfo=classif.get(id);
			String[] names= classifInfo.split(";")[0].split(",");
			String[] descriptions= classifInfo.split(";")[1].split(",");
			CodeItem item=new CodeItem(id, names, descriptions);
			codes.add(item);
		}
		return codes;
	}
	
	public static  ArrayList<CodeItemJSON> classifToCodeListJSON(ArrayList<CodeItem> codes) {
		ArrayList<CodeItemJSON> code = new ArrayList<CodeItemJSON>();
		
		for(int i=0;i<codes.size();i++) {
			CodeItem item= codes.get(i);
			String id=item.getCode();
			Map<Integer, String> languages= new HashMap<Integer, String>();
			languages.put(0, "bg");//CODE_LANG_BG=1
			if(item.names.length>1) {
				languages.put(1, "eng");//CODE_LANG_EN=2
			}
			Map <String, String> names=new HashMap<String, String>();
			Map <String, String> descriptions=new HashMap<String, String>();
			for(int j=0;j<languages.size();j++) {
				names.put(languages.get(j),""+item.names[j]);
				descriptions.put(languages.get(j),""+item.descriptions[j]);
			}
			CodeItemJSON jsonItem = new CodeItemJSON(id, names, descriptions);
			code.add(jsonItem);
		}
		
		
//		for(String id:classif.keySet()) {
//			String classifInfo=classif.get(id);
//			String[] names= classifInfo.split(";")[0].split(",");
//			String[] descriptions= classifInfo.split(";")[1].split(",");
//			String[] languages= {"bg","en"};
//			CodeItemJSON item = new CodeItemJSON(id, new HashMap<String, String>(), new HashMap<String, String>());
//			for(int i=0;i<languages.length;++i) {
//				item.getNames().put(languages[i], names[i]);
//				item.getDescriptions().put(languages[i], descriptions[i]);
//			}
//			code.add(item);
//		}
		return code;
	}
	

	/*================================================= Structures =================================================*/
	
	public static class Header {
		public String id;
		public String prepared;
		public String sender;
		public String receiver;

		public Header(String id, String prepared, String sender, String receiver) {
			this.id = id;
			this.prepared = prepared;
			this.sender = sender;
			this.receiver = receiver;
		}

		public String getId() {
			return id;
		}

		public String getPrepared() {
			return prepared;
		}

		public String getSender() {
			return sender;
		}

		public String getReceiver() {
			return receiver;
		}
	}

	public static class RelationshipItem {
		public String id;
		public String codeSource;
		public String codeTarget;
		public String description;

		public RelationshipItem(String id, String codeSource, String codeTarget, String description) {

			this.id = id;
			this.codeSource = codeSource;
			this.codeTarget = codeTarget;
			this.description = description;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getCodeSource() {
			return codeSource;
		}

		public void setCodeSource(String codeSource) {
			this.codeSource = codeSource;
		}

		public String getCodeTarget() {
			return codeTarget;
		}

		public void setCodeTarget(String codeTarget) {
			this.codeTarget = codeTarget;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	public static class ObservationContent {
		public String id;
		public String description;

		public ObservationContent(String id, String description) {
			this.id = id;
			this.description = description;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

	public static class Observation {
		public String id;
		public ArrayList<ObservationContent> values;

		public Observation(String id, ArrayList<ObservationContent> values) {
			this.id = id;
			this.values = values;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public ArrayList<ObservationContent> getValues() {
			return values;
		}

		public void setValues(ArrayList<ObservationContent> values) {
			this.values = values;
		}

	}

	public static class Attributes {
		public ArrayList<Object> dataset;
		public ArrayList<Object> series;
		public ArrayList<Observation> observation;

		public Attributes(ArrayList<Object> dataset, ArrayList<Object> series, ArrayList<Observation> observation) {
			this.dataset = dataset;
			this.series = series;
			this.observation = observation;
		}

		public ArrayList<Object> getDataset() {
			return dataset;
		}

		public void setDataset(ArrayList<Object> dataset) {
			this.dataset = dataset;
		}

		public ArrayList<Object> getSeries() {
			return series;
		}

		public void setSeries(ArrayList<Object> series) {
			this.series = series;
		}

		public ArrayList<Observation> getObservation() {
			return observation;
		}

		public void setObservation(ArrayList<Observation> observation) {
			this.observation = observation;
		}
	}

	public static class JSONContent {
		public Header header;
		public List<Attributes> attributes;
		
		public JSONContent(Header header, List<Attributes> attributes) {
			this.header = header;
			this.attributes = attributes;
		}

		public Header getHeader() {
			return header;
		}

		public void setHeader(Header header) {
			this.header = header;
		}

		public List<Attributes> getAttributes() {
			return attributes;
		}

		public void setAttributes(List<Attributes> attributes) {
			this.attributes = attributes;
		}
		
	}

	public static class Obs {
		public String timePeriod;
		public String obsValue;
		public String obsStatus;

		public Obs(String timePeriod, String obsValue, String obsStatus) {
			this.timePeriod = timePeriod;
			this.obsValue = obsValue;
			this.obsStatus = obsStatus;
		}

		public String getTimePeriod() {
			return timePeriod;
		}

		public void setTimePeriod(String timePeriod) {
			this.timePeriod = timePeriod;
		}

		public String getObsValue() {
			return obsValue;
		}

		public void setObsValue(String obsValue) {
			this.obsValue = obsValue;
		}

		public String getObsStatus() {
			return obsStatus;
		}

		public void setObsStatus(String obsStatus) {
			this.obsStatus = obsStatus;
		}

	}
	
	public static class CodeItem {
		public String code;
		public String[] names;
		public String[]  descriptions;

		public CodeItem(String code, String[] names,String[] descriptions) {
			this.code = code;
			this.names = names;
			this.descriptions = descriptions;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String[] getNames() {
			return names;
		}

		public void setNames(String[] names) {
			this.names = names;
		}

		public String[] getDescriptions() {
			return descriptions;
		}

		public void setDescriptions(String[] descriptions) {
			this.descriptions = descriptions;
		}
		
		@Override
		public String toString() {
			return "CodeItem [code=" + code + ", names=" + Arrays.toString(names) + ", descriptions="
					+ Arrays.toString(descriptions) + "]";
		}

	}
	
	public static class HCodeItem{
		public String[] names;
		public String hierarchicalCode;
		public String codelistAliasRef;
		public String codeID;
		
		public HCodeItem(String[] names, String hierarchicalCode, String codelistAliasRef, String codeID) {
			this.names = names;
			this.hierarchicalCode = hierarchicalCode;
			this.codelistAliasRef = codelistAliasRef;
			this.codeID = codeID;
		}
		public String[] getNames() {
			return names;
		}

		public void setNames(String[] names) {
			this.names = names;
		}
		public String getHierarchicalCode() {
			return hierarchicalCode;
		}
		public void setHierarchicalCode(String hierarchicalCode) {
			this.hierarchicalCode = hierarchicalCode;
		}
		public String getCodelistAliasRef() {
			return codelistAliasRef;
		}
		public void setCodelistAliasRef(String codelistAliasRef) {
			this.codelistAliasRef = codelistAliasRef;
		}
		public String getCodeID() {
			return codeID;
		}
		public void setCodeID(String codeID) {
			this.codeID = codeID;
		}
	}
	
	public static class CodeItemJSON{
		public String code;
		private Map<String, String> names; 
		private Map<String, String> descriptions;
		
		
		public CodeItemJSON(String code, Map<String, String> names, Map<String, String> descriptions) {

			this.code = code;
			this.names = names;
			this.descriptions = descriptions;
		}
		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public Map<String, String> getNames() {
			return names;
		}
		public void setNames(Map<String, String> names) {
			this.names = names;
		}
		public Map<String, String> getDescriptions() {
			return descriptions;
		}
		public void setDescriptions(Map<String, String> descriptions) {
			this.descriptions = descriptions;
		} 
		
		
	}
	
	public static class JSONContentV{
		public Header meta;
		public ArrayList<CodeItemJSON> data;
		
		
		public JSONContentV(Header header, ArrayList<CodeItemJSON> items) {
			this.meta = header;
			this.data = items;
		}
		public Header getHeader() {
			return meta;
		}
		public void setHeader(Header header) {
			this.meta = header;
		}
		public ArrayList<CodeItemJSON> getItems() {
			return data;
		}
		public void setItems(ArrayList<CodeItemJSON> items) {
			this.data = items;
		}
		
	}

}
