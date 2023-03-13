package com.ib.nsiclassif.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.lang3.function.Failable;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import oracle.net.aso.f;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dao.RelationDAO;
import com.ib.nsiclassif.db.dto.PositionLang;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.PositionUnits;
import com.ib.nsiclassif.opendata.OpenDataObj;
import com.ib.nsiclassif.opendata.OpenDataRestClient;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.RestClientException;
import com.ib.system.exceptions.UnexpectedResultException;
import com.ib.system.utils.SearchUtils;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Named("downloadFile")
@ViewScoped
public class DownloadFile extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3268223017717512329L;
	static final Logger LOGGER = LoggerFactory.getLogger(DownloadFile.class);
	
	private Integer progress=0;
	private Integer idObj=null;
	private String fileName="";
	private boolean hasAllParams=true;
	private boolean version=false;
	private boolean coresp=false;
	private boolean opendata=false;	
	private boolean opendataSave=true;
	private Integer verLangId;
	private String type="";
	private String format="";
	private String filePathAndNameSave="";
	private String filePathAndNameFolder="";
	private String filePathAndNameDownload="";
	private String separator=";";
	private List<Object[]> result; 
//	private List<PositionS> listPost=new ArrayList<PositionS>();
//	private boolean usingSimpleObject=true;
	private List<String> headerNames=new ArrayList<String>();
	private OpenDataObj openDataObj=new OpenDataObj();
	private String message="Вашия файл се подготвя, моля изчакайте.";
	private boolean error=false;
	
	private Map<String,Integer> headerColumnOrderCoresp= new HashMap<String,Integer>();
	private Map<String,Integer> headerColumnOrderVersion= new HashMap<String,Integer>();
	private List<Integer> order= new ArrayList<Integer>();
	

	@PostConstruct
	public void paramsFromGet() {
		//order of coresponding tables export
		headerColumnOrderCoresp= new HashMap<String,Integer>();
		headerColumnOrderCoresp.put("Код на позиция - източник",0);
		headerColumnOrderCoresp.put("Част на позиция - източник",1);
		headerColumnOrderCoresp.put("Код на позиция - цел",2);
		headerColumnOrderCoresp.put("Част на позиция - цел",3);
		
		//order of version  export
		headerColumnOrderVersion= new HashMap<String,Integer>();
		headerColumnOrderVersion.put( "Код на позиция - източник",0);
		headerColumnOrderVersion.put( "Част на позиция - източник",1);
		headerColumnOrderVersion.put( "Код на позиция - източник чужд език 1",2);
		headerColumnOrderVersion.put( "Част на позиция - източник чужд език 1",3);
	
		if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idObj"))) {
			
			idObj=Integer.valueOf(JSFUtils.getRequestParameter("idObj"));
		}else {
			hasAllParams=false;
		}
		
		
		if (JSFUtils.getRequestParameter("typeObj") != null && !"".equals(JSFUtils.getRequestParameter("typeObj"))) {
			setType(JSFUtils.getRequestParameter("typeObj"));
			if (getType().equals("version")) {
				version=true;
				if(hasAllParams) {
					String sql="Select v.ident from VERSION_LANG v where v.version_id=:version_id  and v.lang=:lang";
					Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
					q.setParameter("lang", getCurrentLang());
					q.setParameter("version_id", idObj);
					Object resultList = q.getSingleResult();
					if (resultList!=null) {
						String[] split = SearchUtils.asString(resultList).trim().split(" ");
						for (int i = 0; i < split.length; i++) {
							if (i>0) {
								fileName+="_";
							}
							fileName+=split[i];	
							
						}
												
					}

				}
				
				if (JSFUtils.getRequestParameter("headers") != null && !"".equals(JSFUtils.getRequestParameter("headers"))) {
					String header=JSFUtils.getRequestParameter("headers");
//					System.out.println(header);
					String[] headers= header.split(",");
					for(String h:headers) {
						Integer val=headerColumnOrderVersion.get(h);
						if(val!=null) {
							order.add(val);
						}
					}
				}
			}else {
				if (getType().equals("coresp")) {
					coresp=true;
					if(hasAllParams) {
						String sql="Select c.ident from TABLE_CORRESP_LANG c where c.TABLE_CORRESP_ID=:corespId  and c.lang=:lang";
						Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
						q.setParameter("corespId", getIdObj());
						q.setParameter("lang", getCurrentLang());
						Object resultList = q.getSingleResult();
						if (resultList!=null) {
							String[] split = SearchUtils.asString(resultList).trim().split(" ");
							for (int i = 0; i < split.length; i++) {
								if (i>0) {
									fileName+="_";
								}
								fileName+=split[i];	
								
							}						
						}

					}
					if (JSFUtils.getRequestParameter("headers") != null && !"".equals(JSFUtils.getRequestParameter("headers"))) {
						String header=JSFUtils.getRequestParameter("headers");
//						System.out.println(header);
						String[] headers= header.split(",");
						for(String h:headers) {
							Integer val=headerColumnOrderCoresp.get(h);
							if(val!=null) {
								order.add(val);
							}
						}
					}
				}else {
					hasAllParams=false;
				}
			}			
		}else {
			hasAllParams=false;
		}
		
		if (JSFUtils.getRequestParameter("format") != null && !"".equals(JSFUtils.getRequestParameter("format"))) {
			format=JSFUtils.getRequestParameter("format");
			if (!format.equals("xlsx") && !format.equals("ods") && !format.equals("csv") && !format.equals("html") && !format.equals("txt") && !format.equals("pdf") && !format.equals("sdmx") && !format.equals("sdmx-json")) {
				hasAllParams=false;
			}else {
				if(format.equals("sdmx")) {
					fileName+="-sdmx.xml";
				}else {
					if (format.equals("sdmx-json")) {
						fileName+="-sdmx.json";
					}else{
						fileName+="."+JSFUtils.getRequestParameter("format");		
					}
				}
			}
			
		}else {
			hasAllParams=false;
		}
		if (hasAllParams) {
			
			try {
				//TOVA TUK E ZA IZPRASHTANE NA DANNI KAM OPENDATA - za tova preskachame momenta s failovete
				if (JSFUtils.getRequestParameter("opendata")!=null && !"".equals(JSFUtils.getRequestParameter("opendata"))) {
					// zarejdame i izprashtame danni
					opendata=true;
					openDataObj.setResource_uri(""+JSFUtils.getRequestParameter("opendata"));
					String s=JSFUtils.getRequestParameter("lastOpenDate");
					if (JSFUtils.getRequestParameter("lastOpenDate")!=null && !"".equals(JSFUtils.getRequestParameter("lastOpenDate"))) {
						opendataSave=false;
					}
					if (JSFUtils.getRequestParameter("verLangId")!=null && !"".equals(JSFUtils.getRequestParameter("verLangId"))) {
						verLangId=Integer.valueOf(JSFUtils.getRequestParameter("verLangId"));
					}
					 message="Публикуване на данни в opendata, моля изчакайте.";
						
				}else {
					if (version) {
						// ako exportvame positions - polzvame i EZKIKA kato papka.
						filePathAndNameSave=getSystemData().getSettingsValue("FilesSaveLocation")+getType()+"/"+idObj+"/Lang"+getCurrentLang()+"/"+fileName;
						filePathAndNameDownload=getSystemData().getSettingsValue("FilesDownloadLocation")+getType()+"/"+idObj+"/Lang"+getCurrentLang()+"/"+URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
						filePathAndNameFolder=getSystemData().getSettingsValue("FilesSaveLocation")+getType()+"/"+idObj+"/Lang"+getCurrentLang()+"/";
					}else {
						// relaciite nqmat ezik za sega za tova ne slagame papka s ezik
						filePathAndNameSave=getSystemData().getSettingsValue("FilesSaveLocation")+getType()+"/"+idObj+"/"+fileName;
						filePathAndNameDownload=getSystemData().getSettingsValue("FilesDownloadLocation")+getType()+"/"+idObj+"/"+URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
						filePathAndNameFolder=getSystemData().getSettingsValue("FilesSaveLocation")+getType()+"/"+idObj+"/";	
					}
					
					
//					String fileName="КИД_-_2008.txt";
//
//					FileOutputStream f1= new FileOutputStream(new File(getSystemData().getSettingsValue("FilesSaveLocation")+"/1"+fileName));
//					f1.flush();
//					f1.close();
//					FileOutputStream f2= new FileOutputStream(new File(getSystemData().getSettingsValue("FilesSaveLocation")+"/2"+URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())));
//					f2.flush();
//					f2.close();
//					FileOutputStream f3= new FileOutputStream(new File(getSystemData().getSettingsValue("FilesSaveLocation")+"/3"+new String(fileName.getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_8)));
//					f3.flush();
//					f3.close();
//					FileOutputStream f4= new FileOutputStream(new File(getSystemData().getSettingsValue("FilesSaveLocation")+"/4"+new String(fileName.getBytes(), StandardCharsets.UTF_8)));
//					f4.flush();
//					f4.close();
					
					File f= new File(filePathAndNameSave);
					 
					boolean fileExists =f.exists();
					if (fileExists) {
						// Ако имаме файла директно сваляме
						message= "Успешно изтегляне на файл.";
						redirect();
					}
				
				}
				
				//for column order
				
			
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		
	}
	public void redirect() throws DbErrorException, IOException {
		ExternalContext  context= FacesContext.getCurrentInstance().getExternalContext();

		context.redirect(filePathAndNameDownload);
	}
	
	public void download() {
		try {
			
			if (version) {
//				if (format.equals("sdmx") || format.equals("sdmx-json")) {
//					usingSimpleObject=false;
//				}
				//zarejdame spisacite za pozicii vatre e gotov i header-a.
				loadPositions();
				
			}else {
				//relacii 
				headerNames.add("Код източник");
				headerNames.add("Код цел");
				headerNames.add("Символ");
				
				result = new RelationDAO(getUserData()).loadRelationsForExport(idObj);
			}
			
			
			if (hasAllParams) {
		
				if (opendata) {
					//call rest service
					progress=70;
					
					
		    		
					if (opendataSave) {
						new OpenDataRestClient().actionSaveNewData(getSystemData(), openDataObj);
					}else {
						new OpenDataRestClient().actionUpdateData(getSystemData(), openDataObj);	
					}
					progress=90;
					JPA.getUtil().begin();
					//update LAST_DATE_SENT in VERSION_LANG
					Query q = JPA.getUtil().getEntityManager().createNativeQuery("UPDATE VERSION_LANG SET OPENDATA_LAST_SENT = :dat WHERE  ID = :idVerLang");
					 
					q.setParameter("dat", new Date());
					q.setParameter("idVerLang", verLangId);
					q.executeUpdate();
					
					//insert v journal
					Query q1 = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO SYSTEM_JOURNAL (ID, DATE_ACTION, ID_USER, CODE_ACTION, CODE_OBJECT, ID_OBJECT, IDENT_OBJECT)    "
							+ "VALUES( next value for SEQ_SYSTEM_JOURNAL, getdate(), :idUser, 2, "+NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION+", :idVersion, :identObj)");
		    		q1.setParameter("idUser", getUserData().getUserId());
		    		q1.setParameter("idVersion", idObj);
		    		q1.setParameter("identObj", "Публикуване на данни в Open Data Portal.");
		    		q1.executeUpdate();
		    		JPA.getUtil().commit();
					progress=100;
					message= "Успешно публикувахте в opendata.";
				}else {
					
					File folder= new File(filePathAndNameFolder);
					if (!folder.exists()) {
						folder.mkdirs();
					}
					
					File f= new File(filePathAndNameSave);
					 
					boolean fileExists =f.exists();
					
					// ako sme v relacii skipvame 1 ako sme v pozicii skipvame 4
					int skipColumns=1;
					
					if (!fileExists) {
						if (version) {						
							skipColumns=3;
						}
						
						
						
						if (format.equals("sdmx") || format.equals("sdmx-json")) {
							if (format.equals("sdmx") ) {
								SDMXutils.createSDMX(idObj, f, skipColumns, result, null, true, progress);
							}
							if (format.equals("sdmx-json")) {

								SDMXutils.createSDMXJ(idObj, f, skipColumns, result, null, true);
							}
						}else {	
						
							if (format.equals("html")) {
								createRelationHtml(f,skipColumns);	
							}else {
								if (format.equals("csv") || format.equals("txt")) {
									createRelationCsvOrTxt(f,skipColumns);
								}else {
									if (format.equals("ods")) {
										createRelationOds(f,skipColumns);
									}else {
										if (format.equals("xlsx")) {
											createRelationExcel(f,skipColumns);
										}else {
											if (format.equals("pdf")) {
												createRelationPdf(f,skipColumns);
											}	
										}
									}
								}
							}
						}
						
						
					} 
					message= "Успешно изтегляне на файл.";
					redirect();
				}
				
			
			}else {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Подадени са невалидни параметри!");
			}
			
		} catch (RestClientException e) {
			LOGGER.error("Грешка при подаване на данни към opendata портала!", e);
			message="Грешка при публикуване на данни! Моля, проверете API_KEY-a който е въведен!";
			error=true;
			progress=100;
			JPA.getUtil().rollback();
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на позиции!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			JPA.getUtil().rollback();
		} catch (IOException e) {
			LOGGER.error("Грешка при сваляне на файл!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			JPA.getUtil().rollback();
		} catch (InvalidParameterException e) {
			LOGGER.error("Грешка в подадените параметри!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			JPA.getUtil().rollback();
		} catch (UnexpectedResultException e) {
			LOGGER.error("Грешка при сваляне на файл!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			JPA.getUtil().rollback();
		}

		 
		 
		
		
	}
	
	public void loadPositions() throws InvalidParameterException, DbErrorException, UnexpectedResultException {
		// *********ATRIBUTITE DOPUSTIMI ZA KLASIFIKACIQTA - TOVA VEROQTNO SHTE SE PODAVA OT BEANOVETE AMA ZA TESTA TAKA.
		Map <Integer,Boolean> schemePosAttr = new HashMap<Integer, Boolean>();
		Map <Integer,String> schemePosAttrLabels = new HashMap<Integer, String>();
		
		
		List<Integer> listAttr = new ArrayList<Integer>();
		
		listAttr = new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idObj);
		
		schemePosAttr.clear();
		
		for(Integer attr : listAttr) {
			schemePosAttr.put(attr, Boolean.TRUE);
		}
		
		List<SystemClassif> items = new SystemData().getSysClassification(NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, new Date(), NSIConstants.CODE_DEFAULT_LANG); //izpolzwa se za lejbyli i zatowa ne mi trqbwa na drug ezik
		schemePosAttrLabels.clear();
		for(SystemClassif item : items) {
			schemePosAttrLabels.put(item.getCode(), item.getTekst());
		}
		progress=1;		
		//************************ KRAI NA ATRIBUTITE 
		// IZVLICHANE NA POZICII
		result = new PositionSDAO(ActiveUser.DEFAULT).loadPositionsForExport(idObj, schemePosAttr,true);
		progress=2;
		//IZVLICHANE NA LANGOVE
		List<Object[]> listLang=new PositionSDAO(ActiveUser.DEFAULT).loadPositionsLangsForExport(idObj, 2, schemePosAttr,true);
		progress=5;
		//izvlichane na merni edinici
		HashMap<Integer, HashMap<Integer, String>> mapUnit=new HashMap<Integer, HashMap<Integer,String>>();
		List<Object[]> unitsList=new ArrayList<Object[]>();
	
		if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA) || schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {
			unitsList=new PositionSDAO(ActiveUser.DEFAULT).loadPositionsUnitsForExport(idObj);
//			if (usingSimpleObject) {
				mapUnit=new PositionSDAO(ActiveUser.DEFAULT).decodeUnitsAsMap(unitsList, getSystemData(), 1);	
//			}
		}
		
		progress=6;
		
		
//			if (usingSimpleObject) {
				result=addLangToMainPosAsObj(result, listLang, mapUnit, schemePosAttr);
				progress=7;
				// SORT
				new PositionSDAO(ActiveUser.DEFAULT).doSortSchemePrevAsObj(result, idObj);
				progress=8;
				//header-a pak po atributite na clasifikaciqta i posle samo puskash dolnoto i bi trqbvalo da e tova.
				headerNames=getHeaderNames(schemePosAttr);

				// za OPENDATA
				if (opendata) {
					
					headerNames=getHeaderNames(schemePosAttr);
					
					openDataObj.getData().put("headers", headerNames.toArray());
					for (int i = 0; i < result.size(); i++) {
						openDataObj.getData().put("row"+(i+1),Arrays.copyOfRange(result.get(i), 4, result.get(i).length));
					}
				}
//			}else {
////				//samo za SMDX i SDMX-JSON
////				listPost=addLangToMainPosAsPositionS(result, listLang, unitsList, schemePosAttr);
////				progress=25;
////				// SORT
////				new PositionSDAO(ActiveUser.DEFAULT).doSortSchemePrev(listPost);
////				progress=30;
//				
//				 
//			}
	
	}
	
	public List<String> getHeaderNames(Map <Integer,Boolean> schemePosAttr){
		List<String> rez=new ArrayList<String>();
		
		rez.add("Код");
		if (schemePosAttr.containsKey(2)) {
			rez.add("Пълен код");	
		}
    	if (schemePosAttr.containsKey(3)) {
    		rez.add("Код с разделители");	
    	}
    	if (schemePosAttr.containsKey(4)) {
    		rez.add("Тип");	
    	}
    	if (schemePosAttr.containsKey(5)) {
    		rez.add("Статус");	
    	}
    	if (schemePosAttr.containsKey(6)) {
    		rez.add("Номер на ниво");	
    	}
    	if (schemePosAttr.containsKey(7)) {
    		rez.add("Национална измерителна единица 1");	
    	}
    	if (schemePosAttr.containsKey(7)) {
    		rez.add("Национална измерителна единица 2");	
    	}
    	if (schemePosAttr.containsKey(8)) {
    		rez.add("Международна измерителна единица 1");	
    	}
    	if (schemePosAttr.containsKey(8)) {
    		rez.add("Международна измерителна единица 2");	
    	}
    	if (schemePosAttr.containsKey(11)) {
    		rez.add("Официално наименование");	
    	}
    	if (schemePosAttr.containsKey(12)) {
    		rez.add("Стандартно кратко наименование");	
    	}
    	if (schemePosAttr.containsKey(13)) {
    		rez.add("Стандартно дълго наименование");	
    	}
    	if (schemePosAttr.containsKey(14)) {
    		rez.add("Алтернативни наименования");	
    	}
    	if (schemePosAttr.containsKey(15)) {
    		rez.add("Коментар");	
    	}
    	if (schemePosAttr.containsKey(16)) {
    		rez.add("Включва");	
    	}
    	if (schemePosAttr.containsKey(17)) {
    		rez.add("Включва още");	
    	}
    	if (schemePosAttr.containsKey(18)) {
    		rez.add("Не включва");	
    	}
    	if (schemePosAttr.containsKey(19)) {
    		rez.add("Правила");	
    	}
    	if (schemePosAttr.containsKey(20)) {
    		rez.add("Препратка");	
    	}
    	if (schemePosAttr.containsKey(21)) {
    		rez.add("Статистически показател");	
    	}
		return rez;
	}
	
	public List<Object[]> addLangToMainPosAsObj(List<Object[]> pos, List<Object[]> listLang, HashMap<Integer, HashMap<Integer, String>> mapUnit, Map <Integer,Boolean> schemePosAttr) {
		
		//langMap e sortiran: order by l.POSITION_ID asc,l.LANG desc
		List<Object[]> resultList = new ArrayList<>();
		int curr=0;
		
		for (int i = 0; i < pos.size(); i++) {
			
			
			
			boolean found=false;
			for (int j = curr; j < listLang.size(); j++) {
				
				if (SearchUtils.asInteger(listLang.get(j)[1]).equals(SearchUtils.asInteger(pos.get(i)[0]))) {
					if (!found) {
						 
						
						String s7=null;
						String s8=null;
						
						if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA)) {
							if (mapUnit.containsKey(SearchUtils.asInteger(pos.get(i)[0])) && mapUnit.get(SearchUtils.asInteger(pos.get(i)[0])).containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA)) {
								s7=mapUnit.get(SearchUtils.asInteger(pos.get(i)[0])).get(NSIConstants.CODE_ZNACHENIE_NACIONALNA);	
							}else {
								s7="";
							}
						}
						if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {
							if (mapUnit.containsKey(SearchUtils.asInteger(pos.get(i)[0])) && mapUnit.get(SearchUtils.asInteger(pos.get(i)[0])).containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA)) {
								s8=mapUnit.get(SearchUtils.asInteger(pos.get(i)[0])).get(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA);	
							}else {
								s8="";
							}
						}
						//tova ostava ako shte e v 2 koloni						
//						String[] s=new String[1];
//						
//						if (s7!=null && s8!=null) {
//							s=new String[2];							
//						}
//						if (s7!=null) {
//							s[0]=s7;
//						}
//						if (s8!=null) {
//							if (s7==null) {
//								s[0]=s8;	
//							}else {
//								s[1]=s8;
//							}
//						}
						
						// TOVA E SHTOTO PRAVIM 4 koloni - 2 nac. i 2 mejd edinici - gluposti obshto vzeto shtot ako ima 3-ta shte q propusnem... ama tehen problem. 
						//TEL. Obajdane ot KK na 17.02.2023 - iskane ot klient.
						String[] s=new String[2];
						if (s7!=null && s8!=null) {
							//zna4i shte pravim 4 koloni
							String [] t7=s7.split(";");
							String [] t8=s8.split(";");
							s=new String[4];
							for (int k = 0; k < t7.length; k++) {
								// tuk e <2 shtoto vzemame samo parvite 2 edinici.
								if(k<2) {									
									s[k]=t7[k];	
								}								
							}
							for (int k = 0; k < t8.length; k++) {
								// tuk e <2 shtoto vzemame samo parvite 2 edinici.
								if(k<2) {
									//na s preska4ame parvite 2 shtoto tam e nacionalnata									
									s[k+2]=t8[k];	
								}								
							}
							
						}else {
							if (s7!=null) {
								String [] t7=s7.split(";");
								for (int k = 0; k < t7.length; k++) {
									// tuk e <2 shtoto vzemame samo parvite 2 edinici.
									if(k<2) {									
										s[k]=t7[k];	
									}								
								}
							}
							if (s8!=null) {
								String [] t8=s8.split(";");
								for (int k = 0; k < t8.length; k++) {
									// tuk e <2 shtoto vzemame samo parvite 2 edinici.
									if(k<2) {									
										s[k]=t8[k];	
									}								
								}
							}
							
						}
						
						
					
						if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA) || schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {
							Object[] both = Arrays.copyOf(pos.get(i), pos.get(i).length + s.length);
							System.arraycopy(s, 0, both, pos.get(i).length, s.length);
							
							Object[] three = Arrays.copyOf(both, both.length + listLang.get(j).length-3);
							System.arraycopy( Arrays.copyOfRange(listLang.get(j), 3, listLang.get(j).length), 0, three, both.length, listLang.get(j).length-3);
							resultList.add(three);
							
						}else {
							Object[] both = Arrays.copyOf(pos.get(i), pos.get(i).length + listLang.get(j).length-3);
							System.arraycopy( Arrays.copyOfRange(listLang.get(j), 3, listLang.get(j).length), 0, both, pos.get(i).length, listLang.get(j).length-3);
							resultList.add(both);
						}
						
					     
					}
					found=true;
				}else {
					curr=j;
					break;
				}
				
				
			}
			
		 
		}
		return resultList;
		
		 
	}
	
	public List<PositionS> addLangToMainPosAsPositionS(List<Object[]> pos, List<Object[]> listLang, List<Object[]> unitList, Map <Integer,Boolean> schemePosAttr) {
		
		//langMap e sortiran: order by l.POSITION_ID asc,l.LANG desc
		List<PositionS> resultList = new ArrayList<PositionS>();
		int curr=0;
		
		for (int i = 0; i < pos.size(); i++) {
			PositionS p=new PositionS();
			p.setVersionId(idObj);
			p.setId(SearchUtils.asInteger(pos.get(i)[0]));
			p.setIdPrev(SearchUtils.asInteger(pos.get(i)[1]));
			p.setIdParent(SearchUtils.asInteger(pos.get(i)[2]));
			p.setCode(SearchUtils.asString(pos.get(i)[3]));
			p.setCodeFull(SearchUtils.asString(pos.get(i)[4]));
			p.setCodeSeparate(SearchUtils.asString(pos.get(i)[5]));
			p.setCodeType(SearchUtils.asInteger(pos.get(i)[6]));
			p.setStatus(SearchUtils.asInteger(pos.get(i)[7]));
			p.setLevelNumber(SearchUtils.asInteger(pos.get(i)[8]));
			
			
			
			boolean found=false;
			for (int j = curr; j < listLang.size(); j++) {
				
				if (SearchUtils.asInteger(listLang.get(j)[0]).equals(SearchUtils.asInteger(pos.get(i)[0]))) {
					if (!found) {
						
						
						//LANGS
						PositionLang pl=new PositionLang();
						pl.setId(SearchUtils.asInteger(listLang.get(j)[0]));
//						pl.setPosition();
						pl.setLang(SearchUtils.asInteger(listLang.get(j)[2]));
						pl.setOffitialTitile(SearchUtils.asString(listLang.get(j)[3]));
						pl.setLongTitle(SearchUtils.asString(listLang.get(j)[4]));
						pl.setShortTitle(SearchUtils.asString(listLang.get(j)[5]));
						pl.setAlternativeNames(SearchUtils.asString(listLang.get(j)[6]));
						
						pl.setComment(SearchUtils.asString(listLang.get(j)[7]));
						pl.setIncludes(SearchUtils.asString(listLang.get(j)[8]));
						pl.setAlsoIncludes(SearchUtils.asString(listLang.get(j)[9]));
						pl.setExcludes(SearchUtils.asString(listLang.get(j)[10]));
						pl.setRules(SearchUtils.asString(listLang.get(j)[11]));
						pl.setPrepratka(SearchUtils.asString(listLang.get(j)[12]));
						pl.setStatPokazatel(SearchUtils.asString(listLang.get(j)[13]));
						
						
						
						
						p.getLangMap().put(SearchUtils.asInteger(listLang.get(j)[2]), pl);
						// UNITS
						for (int j2 = 0; j2 < unitList.size(); j2++) {
							PositionUnits pu=new PositionUnits();
							pu.setId(SearchUtils.asInteger(unitList.get(j2)[0]));
//							pu.setPosition(p);
							pu.setUnit(SearchUtils.asInteger(unitList.get(j2)[2]));
							pu.setTypeUnit(SearchUtils.asInteger(unitList.get(j2)[3]));
							
							p.getUnits().add(pu);	
						}
						
						
						resultList.add(p);
					}
					found=true;
				}else {
					curr=j;
					break;
				}
				
				
			}
			
			
		}
		return resultList;
		
		
	}
	 

	public Integer getProgress(){
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}
	public Integer getIdObj() {
		return idObj;
	}
	public void setIdObj(Integer idObj) {
		this.idObj = idObj;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public boolean isHasAllParams() {
		return hasAllParams;
	}
	public void setHasAllParams(boolean hasAllParams) {
		this.hasAllParams = hasAllParams;
	}
	public boolean isVersion() {
		return version;
	}
	public void setVersion(boolean version) {
		this.version = version;
	}
	public boolean isCoresp() {
		return coresp;
	}
	public void setCoresp(boolean coresp) {
		this.coresp = coresp;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	
	
	
	public void createRelationPdf(File file, int skipFirst) {
		try {
			
			Document doc = new Document();
			FileOutputStream f= new FileOutputStream(file);
			PdfWriter writer = PdfWriter.getInstance(doc, f);
			
			
			PdfPTable table = new PdfPTable(headerNames.size());
			table.setWidthPercentage(100);
	      
			 
		    for (int i = 0; i < headerNames.size(); i++) {
		    	table.addCell(headerNames.get(i));
			}
		    
		    int i=0;
		    for (int j = 0; j < result.size(); j++) {
		    	updateProgress(result.size(), i);
	    	 	for (int a = skipFirst; a < result.get(i).length; a++) {
	    	 		table.addCell(result.get(i)[a]==null?"":(""+result.get(i)[a]).replace("\r", "").replace("\n", "")+"");
		        }
				i++;
		    }
		    
			progress = 100;
			doc.open();

			doc.add(table);
			doc.close();
			writer.close();
			 f.flush();
		        f.close();
		} catch (Exception e) {			
			e.printStackTrace();
			
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	public void createRelationExcel(File file, int skipFirst) {
		try {
			
			
			
			FileOutputStream f= new FileOutputStream(file);
			
			
			XSSFWorkbook workbook = new XSSFWorkbook();
	        XSSFSheet sheet = workbook.createSheet("Data");
	         

	        int rowNum = 0;
	         
			org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
			int colNum = 0;
		    for (int i = 0; i < headerNames.size(); i++) {
		    	
		    	org.apache.poi.ss.usermodel.Cell cell = row.createCell(colNum++);
		    	cell.setCellValue(headerNames.get(i));
				 
			}
		    
		    int i=0;
		    DataFormat fmt = workbook.createDataFormat();
	 		CellStyle textStyle = workbook.createCellStyle();
	 		textStyle.setDataFormat(fmt.getFormat("@"));
		    
		    for (int j = 0; j < result.size(); j++) {
		    	updateProgress(result.size(), i);
		    	org.apache.poi.ss.usermodel.Row row1 = sheet.createRow(rowNum++);
		    	int colNum1 = 0;
	    	 	for (int a = skipFirst; a < result.get(i).length; a++) {
	    	 		org.apache.poi.ss.usermodel.Cell cell = row1.createCell(colNum1++);
	    	 		
	    	 		cell.setCellStyle(textStyle);
	    	 		cell.setCellValue(result.get(i)[a]==null?"":(""+result.get(i)[a]).replace("\r", "").replace("\n", "")+"");
		        }
				i++;
		    }
			progress=100; 
			
			workbook.write(f);
	        workbook.close();
	        f.flush();
	        f.close();
		} catch (Exception e) {			
			e.printStackTrace();
			
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	public void createRelationCsvOrTxt(File file, int skipFirst) {
		try {
			
			
			StringBuilder sb = new StringBuilder();
			
			FileOutputStream f= new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(f, "CP1251"));
			
			for (int i = 0; i < headerNames.size(); i++) {
	            sb.append(headerNames.get(i));
				sb.append(separator);
			}
			sb.append(System.lineSeparator());
			
			
			for (int i = 0; i < result.size(); i++) {
				updateProgress(result.size(), i);
				for (int a = skipFirst; a < result.get(i).length; a++) {
					sb.append(result.get(i)[a]==null?"":""+(""+result.get(i)[a]).replace("\r", "").replace("\n", "")+"");
					if (a<result.get(i).length-1) {
						sb.append(separator);	
					}
				}
				sb.append(System.lineSeparator());
				 
			}
			progress=100; 
			
//			f.write(sb.toString().getBytes());
//		    f.flush();
//		    f.close();
			byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
			String result = new String(bytes, StandardCharsets.UTF_8);
				
			bw.write(result);
			bw.flush();
			bw.close();
			 f.flush();
		        f.close();
		} catch (Exception e) {			
			e.printStackTrace();
			 
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
	/**
	 * 
	 * @param idTable - Id na korespondirashta tablica
	 * @param filePathAndName - acceptable Types: ods
	 */
	public void createRelationOds(File file, int skipFirst) {
		try {
			
			SpreadsheetDocument ods = SpreadsheetDocument.newSpreadsheetDocument();
		    Table table = Table.getInstance(ods.getTables().get(0));
		    
		    Row r1=table.appendRow();
		    for (int i = 0; i < headerNames.size(); i++) {
				Cell cell = r1.getCellByIndex(i);
	            cell.setStringValue(headerNames.get(i));
			}
		    
		    for (int j = 0; j < result.size(); j++) {
		    	updateProgress(result.size(), j);
				Row r=table.appendRow();		    	
	    	 	for (int a = skipFirst; a < result.get(j).length; a++) {
		            Cell cell = r.getCellByIndex(a-skipFirst);
		            cell.setStringValue(result.get(j)[a]==null?"":(""+result.get(j)[a]).replace("\r", "").replace("\n", "")+"");
		        }
		    }
		    progress=100;
			// Save document
			ods.save(""+file.getAbsoluteFile().getPath());
			
			ods.close();
			
		} catch (Exception e) {			
			e.printStackTrace();
			
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	/**
	 * 
	 * @param idTable - Id na korespondirashta tablica
	 * @param filePathAndName - acceptable type: html
	 */
	public void createRelationHtml(File file, int skipFirst) {
		try {
			
			
			FileOutputStream f= new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(f, "CP1251"));
			
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
				sb.append("<table border=\"1px solid thin black\" style=\" border-collapse: collapse;\">");
			
				sb.append("<tr>");
				for (int i = 0; i < headerNames.size(); i++) {
					sb.append("<td>");
						sb.append(headerNames.get(i));
					sb.append("</td>");
					
				}
				sb.append("</tr>");
				
				
				for (int i = 0; i < result.size(); i++) {
					updateProgress(result.size(), i);
					
					sb.append("<tr>");
					// DA NE ZABRAVISH DA SKIPNESH PARVITE 3 te sa ID,ID_PREV,ID_PARENT - polzvat se za sortiraneto i ne vlizat v export-a
					for (int j = skipFirst; j < result.get(i).length; j++) {
						sb.append("<td>");
							sb.append(result.get(i)[j]==null?"":(""+result.get(i)[j]).replace("\r", "").replace("\n", "")+"");
						sb.append("</td>");
					}
						
					sb.append("</tr>");
				}
				sb.append("</table>");
			sb.append("</html>");
			progress=100;
//			f.write(sb.toString().getBytes());
//			f.flush();
//			f.close();
			byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
			String result = new String(bytes, StandardCharsets.UTF_8);
				
			bw.write(result);
			bw.flush();
			bw.close();
			 f.flush();
		        f.close();
		} catch (Exception e) {			
			e.printStackTrace();
			
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	public void updateProgress(Integer size, Integer current) {
		if (current>0) {
			if (((current*100)/size+1)>progress) {
				progress=(current*100)/size+1;	
			}
		}	
	}
	
	
	public String getFilePathAndNameSave() {
		return filePathAndNameSave;
	}
	public void setFilePathAndNameSave(String filePathAndNameSave) {
		this.filePathAndNameSave = filePathAndNameSave;
	}
	public String getSeparator() {
		return separator;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	public String getFilePathAndNameFolder() {
		return filePathAndNameFolder;
	}
	public void setFilePathAndNameFolder(String filePathAndNameFolder) {
		this.filePathAndNameFolder = filePathAndNameFolder;
	}
	public String getFilePathAndNameDownload() {
		return filePathAndNameDownload;
	}
	public void setFilePathAndNameDownload(String filePathAndNameDownload) {
		this.filePathAndNameDownload = filePathAndNameDownload;
	}
	public List<Object[]> getResult() {
		return result;
	}
	public void setResult(List<Object[]> result) {
		this.result = result;
	}
//	public boolean isUsingSimpleObject() {
//		return usingSimpleObject;
//	}
//	public void setUsingSimpleObject(boolean usingSimpleObject) {
//		this.usingSimpleObject = usingSimpleObject;
//	}
//	public List<PositionS> getListPost() {
//		return listPost;
//	}
//	public void setListPost(List<PositionS> listPost) {
//		this.listPost = listPost;
//	}
	public List<String> getHeaderNames() {
		return headerNames;
	}
	public void setHeaderNames(List<String> headerNames) {
		this.headerNames = headerNames;
	}
	public boolean isOpendata() {
		return opendata;
	}
	public void setOpendata(boolean opendata) {
		this.opendata = opendata;
	}
	public OpenDataObj getOpenDataObj() {
		return openDataObj;
	}
	public void setOpenDataObj(OpenDataObj openDataObj) {
		this.openDataObj = openDataObj;
	}
	public Integer getVerLangId() {
		return verLangId;
	}
	public void setVerLangId(Integer verLangId) {
		this.verLangId = verLangId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isError() {
		return error;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	
	public Map<String,Integer> getHeaderColumnOrderCoresp() {
		return headerColumnOrderCoresp;
	}
	public void setHeaderColumnOrderCoresp(Map<String,Integer> headerColumnOrderCoresp) {
		this.headerColumnOrderCoresp = headerColumnOrderCoresp;
	}
	public Map<String,Integer> getHeaderColumnOrderVersion() {
		return headerColumnOrderVersion;
	}
	public void setHeaderColumnOrderVersion(Map<String,Integer> headerColumnOrderVersion) {
		this.headerColumnOrderVersion = headerColumnOrderVersion;
	}
	public List<Integer> getOrder() {
		return order;
	}
	public void setOrder(List<Integer> order) {
		this.order = order;
	}
	
 
}
