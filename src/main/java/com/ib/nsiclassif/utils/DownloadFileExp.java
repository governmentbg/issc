package com.ib.nsiclassif.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dao.RelationDAO;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.PositionLang;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.PositionUnits;
import com.ib.nsiclassif.db.dto.Relation;
import com.ib.nsiclassif.opendata.OpenDataObj;
import com.ib.nsiclassif.opendata.OpenDataRestClient;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.RestClientException;
import com.ib.system.exceptions.UnexpectedResultException;
import com.ib.system.utils.JSonUtils;
import com.ib.system.utils.SearchUtils;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;





//import oracle.net.aso.f;

@Named("downloadFileExp")
@ViewScoped
public class DownloadFileExp extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3268223017717512329L;
	static final Logger LOGGER = LoggerFactory.getLogger(DownloadFileExp.class);
	
	private Integer progress=0;
	private Integer idObj=null;
	private String fileName="";
	private boolean hasAllParams=true;
	private boolean version=false;
	private boolean coresp=false;
	private boolean opendata=false;	
	private boolean opendataSave=true;
	private Integer verLangId;
	private Integer lang;
	private String type="";
	private String format="";
	private String filePathAndNameSave="";
	private String filePathAndNameFolder="";
	private String filePathAndNameDownload="";
	private String separator=",";
	private List<Object[]> result; 
	private List<Object[]> oldResult;
	private List<Boolean> includedInReport=new ArrayList<Boolean>(); 
	private List<PositionS> listPost=new ArrayList<PositionS>();
	private boolean usingSimpleObject=true;
	private List<String> headerNames=new ArrayList<String>();
	private OpenDataObj openDataObj=new OpenDataObj();
	private String message="Вашия файл се подготвя, моля изчакайте.";
	private boolean error=false;
	//from relacii
	private String typeRel="";
	private String changeRel="";
	
	//from versions
	private String pos="";
    private String begs="";
    private String ends="";
    private String levels="";
    private String nodeCodes="";
    private String headers="";
    private String expName="";
    private String units="";

	





	private List<String> order= new ArrayList<String>();
	
	private ArrayList<CodeItem> codes = new ArrayList<CodeItem>();
	

	@PostConstruct
	public void paramsFromGet() {
		order= new ArrayList<String>();
		lang = NSIConstants.CODE_DEFAULT_LANG;
		
		LOGGER.info("downloadFileExp");
		
		//expName - the name of the export
		if(JSFUtils.getRequestParameter("expName") != null && !"".equals(JSFUtils.getRequestParameter("expName"))) {
			setExpName(JSFUtils.getRequestParameter("idObj").trim());//.replaceAll("[^-_.A-Za-z0-9]","")
			LOGGER.info("expName "+expName);
		}
		
		//typeRel
		if(JSFUtils.getRequestParameter("typeRel") != null && !"".equals(JSFUtils.getRequestParameter("typeRel"))) {
			setTypeRel(JSFUtils.getRequestParameter("typeRel"));
			LOGGER.info("typeRel "+getTypeRel());
		}
		
		//changeRel
		if(JSFUtils.getRequestParameter("changeRel") != null && !"".equals(JSFUtils.getRequestParameter("changeRel"))) {
			setChangeRel(JSFUtils.getRequestParameter("changeRel"));
			LOGGER.info("changeRel "+getChangeRel());
		}
		//pos
		if(JSFUtils.getRequestParameter("pos") != null && !"".equals(JSFUtils.getRequestParameter("pos"))) {
			setPos(JSFUtils.getRequestParameter("pos"));
			LOGGER.info("pos "+getPos());
		}
		
		//units
		if(JSFUtils.getRequestParameter("units") != null && !"".equals(JSFUtils.getRequestParameter("units"))) {
			setUnits(JSFUtils.getRequestParameter("units"));
			LOGGER.info("units "+getUnits());
		}
		
		
		//headers
		if (JSFUtils.getRequestParameter("headers") != null && !"".equals(JSFUtils.getRequestParameter("headers"))) {
			String header=JSFUtils.getRequestParameter("headers");
			LOGGER.info("header "+header);
			String[] headers= header.split(",");
			for(String h:headers) {
				order.add(h);
				//LOGGER.info("header "+h);
			}
		}

		//parameter shows the selected language
		lang = NSIConstants.CODE_DEFAULT_LANG;
		if(JSFUtils.getRequestParameter("lang") != null && !"".equals(JSFUtils.getRequestParameter("lang"))) {
			lang=Integer.valueOf(JSFUtils.getRequestParameter("lang"));
			LOGGER.info("lang "+lang);
		}
		
		//parameter shows the chosen separator
		if(JSFUtils.getRequestParameter("razd") != null && !"".equals(JSFUtils.getRequestParameter("razd"))) {
			separator=JSFUtils.getRequestParameter("razd");
			LOGGER.info("separator "+separator);
		}
		
		//izpolzva se za selektirani niva za versii
		if(JSFUtils.getRequestParameter("levels") != null && !"".equals(JSFUtils.getRequestParameter("levels"))) {
			levels=JSFUtils.getRequestParameter("levels");
			LOGGER.info("levels "+levels);
		}
		
		//izpolzva se za nachalo na intervalite pri versii
		if(JSFUtils.getRequestParameter("begs") != null && !"".equals(JSFUtils.getRequestParameter("begs"))) {
			begs=JSFUtils.getRequestParameter("begs");
			LOGGER.info("begs "+begs);
		}
		
		//izpolzva se za krai na intervalite pri versii
		if(JSFUtils.getRequestParameter("ends") != null && !"".equals(JSFUtils.getRequestParameter("ends"))) {
			ends=JSFUtils.getRequestParameter("ends");
			LOGGER.info("ends "+ends);
		}
		
		//izpolzva se za vavedeni kodove ot darvoto za versii
		if(JSFUtils.getRequestParameter("nodeCodes") != null && !"".equals(JSFUtils.getRequestParameter("nodeCodes"))) {
			nodeCodes=JSFUtils.getRequestParameter("nodeCodes");
			LOGGER.info("nodeCodes"+nodeCodes);
		}
		

		
		if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idObj"))) {
			//fileName=JSFUtils.getRequestParameter("idObj");
			idObj=Integer.valueOf(JSFUtils.getRequestParameter("idObj"));
		}else {
			hasAllParams=false;
		}
		
		if (JSFUtils.getRequestParameter("typeObj") != null && !"".equals(JSFUtils.getRequestParameter("typeObj"))) {
			setType(JSFUtils.getRequestParameter("typeObj"));
			if (getType().equals("version")) {
				version=true;
			}else {
				if (getType().equals("coresp")) {
					coresp=true;
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
					fileName=expName+"-sdmx.xml";
				}else {
					if (format.equals("sdmx-json")) {
						fileName+=expName+"-sdmx.json";
					}else{
						fileName+=expName+"."+JSFUtils.getRequestParameter("format");		
					}
				}
			}
			
		}else {
			hasAllParams=false;
		}
		LOGGER.info("fileName "+fileName.toString());
		
		
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
						filePathAndNameDownload=getSystemData().getSettingsValue("FilesDownloadLocation")+getType()+"/"+idObj+"/Lang"+getCurrentLang()+"/"+fileName;
						filePathAndNameFolder=getSystemData().getSettingsValue("FilesSaveLocation")+getType()+"/"+idObj+"/Lang"+getCurrentLang()+"/";
					}else {
						// relaciite nqmat ezik za sega za tova ne slagame papka s ezik
						filePathAndNameSave=getSystemData().getSettingsValue("FilesSaveLocation")+getType()+"/"+idObj+"/"+fileName;
						filePathAndNameDownload=getSystemData().getSettingsValue("FilesDownloadLocation")+getType()+"/"+idObj+"/"+fileName;
						filePathAndNameFolder=getSystemData().getSettingsValue("FilesSaveLocation")+getType()+"/"+idObj+"/";	
					}
					
					
					File f= new File(filePathAndNameSave);
					//ako file-a go ima i generate e false, vzemi go ot diska
//					boolean fileExists =f.exists();
//					if (fileExists && ! generate) {
//						// Ако имаме файла директно сваляме
//						message= "Успешно изтегляне на файл.";
//						redirect();
//					}
				
				}
				
				//for column order
				
			
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
	}

	public void redirect() throws DbErrorException, IOException {
		ExternalContext  context= FacesContext.getCurrentInstance().getExternalContext();
		//context.setResponseBufferSize(chunkSize);
		context.redirect(filePathAndNameDownload);
	}
	
	public void download() {
		try {
			
			if (version) {
				if (format.equals("sdmx") || format.equals("sdmx-json")) {
					usingSimpleObject=false;
				}
				//zarejdame spisacite za pozicii vatre e gotov i header-a.
				loadPositions();
				
			}else {
				//relacii 

				headerNames.add("Код източник");
				headerNames.add("Текст източник");
				headerNames.add("Код цел");
				headerNames.add("Текст цел");
				headerNames.add("Характер на промяната");

				result = new RelationDAO(getUserData()).loadRelationsForExport(idObj);
				
				addTextToRelations(result);//добавят се колоните с езика 
				
				oldResult=new ArrayList<Object []>();
				oldResult.addAll(result);
				includedInReport= new ArrayList<Boolean>();
				Boolean[] arr=new Boolean[result.size()];
				Arrays.fill(arr ,Boolean.TRUE);
				includedInReport.addAll(Arrays.asList(arr));
				
				if(order.size()>0) {
					headerNames=new ArrayList<String>();
					for(String s:order) {
						headerNames.add(s);
					}
					//LOGGER.info(Arrays.toString(headerNames.toArray()));
					for(int i=0;i<result.size();i++) {
						Object[] temp=result.get(i);
						//changeType check
						if(!changeRel.isBlank()) {

							String codeSource=(null!=temp[1])?""+temp[1]:"";
							String codeTarget=(null!=temp[2])?""+temp[2]:""; 
							String symbol=(null!=temp[3])?""+temp[3]:"";
							int result=0;
							
							if(codeSource.isBlank() && !codeTarget.isBlank()) {
								result=3;//prekratena
							}
							if(!codeSource.isBlank() && codeTarget.isBlank()) {
								result=1;//syzdadena
							}
							if(!codeSource.isBlank() && !codeTarget.isBlank()) {
								result=4;//neizmenena
							}
							if(!symbol.isBlank()) {
								result=2;//izmenena
							}
							if(!changeRel.contains(""+result)) {
								includedInReport.set(i, Boolean.FALSE);
							}
						}
						
						
						//type relationship
						if(!typeRel.isBlank()) {
							String codeSource=(null!=temp[1])?""+temp[1]:null;
							String codeTarget=(null!=temp[2])?""+temp[2]:null; 
							//String symbol=(null!=temp[3])?""+temp[3]:"";
	
							int typeRelation=0;
							
							List<Relation> currentPositionsList = new RelationDAO(getUserData()).getRelationsByCodeSourceTarget(idObj, codeSource, codeTarget);
							
							
							if (!currentPositionsList.isEmpty()) {
								if (currentPositionsList.size()==1) {
									if (currentPositionsList.get(0).getSourceCode()!=null && currentPositionsList.get(0).getSourceCode().isEmpty()) {
										currentPositionsList.get(0).setSourceCode(null);
									}
									if (currentPositionsList.get(0).getTargetCode()!=null && currentPositionsList.get(0).getTargetCode().isEmpty()) {
										currentPositionsList.get(0).setTargetCode(null);
									}
									if (currentPositionsList.get(0).getSourceCode()!=null && currentPositionsList.get(0).getTargetCode()!=null) {
										typeRelation=1;	// edno kam edno 
									}else {
										if (currentPositionsList.get(0).getSourceCode()==null && currentPositionsList.get(0).getTargetCode()!=null) {
											typeRelation=6;	//nula kam edno
										}else {
											if (currentPositionsList.get(0).getSourceCode()!=null && currentPositionsList.get(0).getTargetCode()==null) {
												typeRelation=5; //edno kam nula
											}
										}
									}
								}
								
								int codeSourceCount=1;
								int codeTargetCount=1;
								
								Relation rel= new Relation();
								rel.setId(Integer.valueOf(""+temp[0]));
								rel.setSourceCode(codeSource);
								rel.setTargetCode(codeTarget);
								
								for (int j = 0; i < currentPositionsList.size(); j++) {
									if (currentPositionsList.get(j).getSourceCode()!=null && currentPositionsList.get(j).getSourceCode().isEmpty()) {
										currentPositionsList.get(j).setSourceCode(null);
									}
									if (currentPositionsList.get(j).getTargetCode()!=null && currentPositionsList.get(j).getTargetCode().isEmpty()) {
										currentPositionsList.get(j).setTargetCode(null);
									}
									

									if (rel.getSourceCode()!=null && currentPositionsList.get(j).getSourceCode()!=null && !currentPositionsList.get(j).getSourceCode().equals(rel.getSourceCode())) {
										codeSourceCount++;
									}
									if (rel.getTargetCode()!=null && currentPositionsList.get(j).getTargetCode()!=null && !currentPositionsList.get(j).getTargetCode().equals(rel.getTargetCode())) {
										codeTargetCount++;
									}
									
									if (((rel.getSourceCode()==null && currentPositionsList.get(j).getSourceCode()==null) || currentPositionsList.get(j).getSourceCode()!=null && rel.getSourceCode()!=null && currentPositionsList.get(j).getSourceCode().equals(rel.getSourceCode()))
									&&((rel.getTargetCode()==null && currentPositionsList.get(j).getTargetCode()==null) || currentPositionsList.get(j).getTargetCode()!=null && rel.getTargetCode()!=null && currentPositionsList.get(j).getTargetCode().equals(rel.getTargetCode()))){
										if (rel.getId()==null || currentPositionsList.get(j).getId()!=rel.getId()) {
											rel=currentPositionsList.get(j);
										}
									}
									
								}
								if (currentPositionsList.size()>1) {
									if (codeSourceCount>1 && codeTargetCount>1) {
										typeRelation=4;	//mnogo kam mnogo
									}else {
										if (codeSourceCount>1) {
											typeRelation=3;	//mnogo kam edno
										}else {
											if (codeTargetCount>1) {
												typeRelation=2;	//edno kam mnogo
											}	
										}
									}
									
								}
							}
							
							if(!typeRel.contains(""+typeRelation)) {
								includedInReport.set(i, Boolean.FALSE);
							}
						}


						//LOGGER.info(Arrays.toString(temp));
						int size=headerNames.size()+1;
						Object[]ordered=new Object[size];
						ordered[0]=temp[0];
						for(int j=1;j<temp.length;j++) {
							if(j<size) {
								if(headerNames.get(j-1).equals("Код източник") ||headerNames.get(j-1).equals("Code source")) {
									ordered[j]=temp[1];
								}
								if(headerNames.get(j-1).equals("Текст източник") ||headerNames.get(j-1).equals("Text source")) {
									ordered[j]=temp[2];
								}
								if(headerNames.get(j-1).equals("Код цел") ||headerNames.get(j-1).equals("Code target")) {
									ordered[j]=temp[3];
								}
								if(headerNames.get(j-1).equals("Текст цел") ||headerNames.get(j-1).equals("Text target")) {
									ordered[j]=temp[4];
								}
								if(headerNames.get(j-1).equals("Характер на промяната") ||headerNames.get(j-1).equals("Change character")) {
									ordered[j]=temp[5];
								}
							}
						}
						//LOGGER.info(Arrays.toString(ordered));
						result.set(i, ordered);
					}
				}
				
				
			}
			//tuk rqbwa da prepodredim kolonite za relacii headerORder- pokazwa reda i koi coloni sa vklucheni v exporta
			//positions -za versii da pokazva koi pozicii sa vklucheni v exporta
			//prepareDATAforExport(headerNames,result,headerOrder,positions);
			
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
					
					File f= new File(filePathAndNameSave.trim());
					 
					//boolean fileExists =f.exists();
					
					// ako sme v relacii skipvame 1 ako sme v pozicii skipvame 3
					int skipColumns=1;
					
					//if (!fileExists) {
						if (version) {						
							skipColumns=3;
						}
						
						if (format.equals("sdmx") ) {
							createSDMX(f,skipColumns);
						}
						if (format.equals("sdmx-json")) {

							createSDMXJ(f,skipColumns);
						}

						if (format.equals("html")) {
							createRelationHtml(f, skipColumns);
						}

						if (format.equals("csv") || format.equals("txt")) {
							createRelationCsvOrTxt(f, skipColumns);
						}

						if (format.equals("ods")) {
							createRelationOds(f, skipColumns);
						}

						if (format.equals("xlsx")) {
							createRelationExcel(f, skipColumns);
						}

						if (format.equals("pdf")) {
							createRelationPdf(f, skipColumns);
						}
					//} 
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
	
	
	/**
	 * @param result
	 * Добавя две колони извличащи текстовите значения за кодовете
	 */
	public void addTextToRelations(List<Object[]> result) {
		try {
			CorespTable coresp= new CorespTableDAO(ActiveUser.DEFAULT).findById(idObj);
			Integer versSource=coresp.getIdVersSource();
			Integer versTarget=coresp.getIdVersTarget();
			
			List<Object[]> listSource = new ArrayList<Object[]>();
			List<Object[]> listTarget = new ArrayList<Object[]>();
			
			listSource=new PositionSDAO(ActiveUser.DEFAULT).loadRelationTextsExport(versSource,NSIConstants.CODE_DEFAULT_LANG);
			HashMap<String, String> mapSource= new HashMap<String, String>();
			for(int i=0;i<listSource.size();i++) {
				Object[] ob=listSource.get(i);
				mapSource.put(""+ob[3], ""+ob[4]);//0-id, 1-code prev, 2-code parent, 3 code, 4 text 
			}
			
			listTarget=new PositionSDAO(ActiveUser.DEFAULT).loadRelationTextsExport(versTarget,NSIConstants.CODE_DEFAULT_LANG);
			HashMap<String, String> mapTarget= new HashMap<String, String>();
			for(int i=0;i<listTarget.size();i++) {
				Object[] ob=listTarget.get(i);
				mapTarget.put(""+ob[3], ""+ob[4]);//0-id, 1-code prev, 2-code parent, 3 code, 4 text 
			}
			
			
			for(int i=0;i<result.size();i++) {
				Object[] ob=result.get(i);//id, source, target, symbol
				Object[] texted= new Object[ob.length+2];
				texted[0]=ob[0];//id
				texted[1]=ob[1];//source
				texted[2]=(mapSource.get(""+ob[1])!=null?mapSource.get(""+ob[1]):"");//source text
				texted[3]=ob[2];//target
				texted[4]=(mapTarget.get(""+ob[2])!=null?mapTarget.get(""+ob[2]):"");
				texted[5]=ob[3];//symbol
				result.set(i, texted);
			}
			
			
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при извличане на текстови значения за релациии!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			JPA.getUtil().rollback();
		}
	}
	
	
	
	public void loadPositions() throws InvalidParameterException, DbErrorException, UnexpectedResultException {
		// *********ATRIBUTITE DOPUSTIMI ZA KLASIFIKACIQTA - TOVA VEROQTNO SHTE SE PODAVA OT BEANOVETE AMA ZA TESTA TAKA.
		Map <Integer,Boolean> schemePosAttr = new HashMap<Integer, Boolean>();
//		Map <Integer,String> schemePosAttrLabels = new HashMap<Integer, String>();
		
		
		List<Integer> listAttr = new ArrayList<Integer>();
		
		listAttr = new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idObj);
		
		schemePosAttr.clear();
		
		for(Integer attr : listAttr) {
			schemePosAttr.put(attr, Boolean.TRUE);
		}
		
//		List<SystemClassif> items = new SystemData().getSysClassification(NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, new Date(), NSIConstants.CODE_DEFAULT_LANG); //izpolzwa se za lejbyli i zatowa ne mi trqbwa na drug ezik
//		schemePosAttrLabels.clear();
//		for(SystemClassif item : items) {
//			schemePosAttrLabels.put(item.getCode(), item.getTekst());
//		}
		
		
		progress=1;		
		//************************ KRAI NA ATRIBUTITE 
		// IZVLICHANE NA POZICII
		result = new PositionSDAO(ActiveUser.DEFAULT).loadPositionsForExport(idObj, schemePosAttr,usingSimpleObject);
		
		progress=10;
		//IZVLICHANE NA LANGOVE
		List<Object[]> listLang=new PositionSDAO(ActiveUser.DEFAULT).loadPositionsLangsForExport(idObj, 2, schemePosAttr,usingSimpleObject);
		progress=15;
		//izvlichane na merni edinici
		HashMap<Integer, HashMap<Integer, String>> mapUnit=new HashMap<Integer, HashMap<Integer,String>>();
		List<Object[]> unitsList=new ArrayList<Object[]>();
	
		if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA) || schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {
			unitsList=new PositionSDAO(ActiveUser.DEFAULT).loadPositionsUnitsForExport1(idObj);
			if (usingSimpleObject) {
				mapUnit=new PositionSDAO(ActiveUser.DEFAULT).decodeUnitsAsMap1(unitsList, getSystemData(), 1);	
			}
		}
		
		progress=20;
		
		
			if (usingSimpleObject) {
				result=addLangToMainPosAsObj(result, listLang, mapUnit, schemePosAttr);
				progress=20;
				// SORT
				new PositionSDAO(ActiveUser.DEFAULT).doSortSchemePrevAsObj(result, idObj);
				progress=25;
				//header-a pak po atributite na clasifikaciqta i posle samo puskash dolnoto i bi trqbvalo da e tova.
				//TODO yonchoy - tuka triabva da pravim magiata
				headerNames=new ArrayList<String>();
				if(pos.equals("positions")) {
					headerNames=getHeaderNames(schemePosAttr);
					includedInReport=new ArrayList<Boolean>();
					Boolean[] arr=new Boolean[result.size()];
					Arrays.fill(arr, Boolean.TRUE);
					includedInReport.addAll(Arrays.asList(arr));
				}else {
				if(order.size()>0) {
					
//					result = new PositionSDAO(ActiveUser.DEFAULT).loadPositionsForExport(idObj, schemePosAttr,false);
//					result=addLangToMainPosAsObj(result, listLang, mapUnit, schemePosAttr);
//					new PositionSDAO(ActiveUser.DEFAULT).doSortSchemePrevAsObj(result, idObj);
					
					List<Integer> allAttributes=new ArrayList<Integer>();
					List<Integer> idxAttributes=new ArrayList<Integer>();
					List <SystemClassif> sc=getSystemData()
					.getSysClassification(NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, new Date(), lang);
					for(int i=0;i<order.size();i++) {
						allAttributes=new ArrayList<Integer>();
						for(int j=0;j<sc.size();j++) {
							SystemClassif classif=sc.get(j);
							allAttributes.add(classif.getCode());
							if(order.get(i).equals(""+classif.getCode())) {
								idxAttributes.add(j);
								headerNames.add(classif.getTekst());
							}
						}
					}
					List<Integer> includedAttributes= new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idObj);
					Collections.sort(includedAttributes);
					Integer [] arr1=new Integer [allAttributes.size()], arr2= new Integer[includedAttributes.size()];
					arr2=(Integer[]) includedAttributes.toArray(new Integer[includedAttributes.size()]);
					//int arr[]=correction(allAttributes.toArray(arr1), includedAttributes.toArray(arr2));
					//LOGGER.info("allAttributes " +Arrays.toString(allAttributes.toArray()));
					//LOGGER.info("includedAttributes " +Arrays.toString(arr2));
					//LOGGER.info("idxAttributes " +Arrays.toString(idxAttributes.toArray()));
//					LOGGER.info("Arr " +Arrays.toString(arr));
					
					includedInReport=new ArrayList<Boolean>();
					boolean hasIntervals=!begs.isEmpty()&&!ends.isEmpty();
					boolean hasLevels=!levels.isEmpty();
					boolean hasNodeCodes=null!=nodeCodes&&!nodeCodes.isBlank();
					Boolean [] includedInInterval=new Boolean[result.size()];
					if(hasIntervals) {
						includedInInterval=codesBetween(result,begs.split(","), ends.split(","));
					}
					includedInReport.addAll(Arrays.asList(new Boolean[result.size()]));
					for(int i=0;i<result.size();i++) {
						Object[] temp=result.get(i);
						//LOGGER.info("temp "+Arrays.toString(temp));
						includedInReport.set(i, Boolean.TRUE);
						if(hasIntervals) {
							includedInReport.set(i, includedInInterval[i]);
						}

						//tuk po niakakvi kriterii shte mahame redove ot eksporta
						//mahame nivata ot eksporta
						boolean inExport=true;
						if(hasLevels) {
							if(!levels.contains(""+temp[3+includedAttributes.indexOf(6)])) {
								inExport=false;
								includedInReport.set(i, Boolean.FALSE);
							}
						}
						
						if(hasNodeCodes) {
							if(nodeCodes.contains(""+temp[3])) {
								inExport=false;
								includedInReport.set(i, Boolean.FALSE);
							}
						}
						
						if(inExport) {
						//int size=Math.min(headerNames.size()+1,temp.length-3);
						int size=headerNames.size()+3;
						Object[]ordered=new Object[size];
						Arrays.fill(ordered, " ");
						ordered[0]=temp[0];
						ordered[1]=temp[1];
						ordered[2]=temp[2];
//						LOGGER.error("temp.length"+temp.length);
//						LOGGER.error("ordered.length"+ordered.length);
						//j e ravno na broia skipnati koloni za versii te sa 3;
						//LOGGER.info(Arrays.toString(temp));
						for(int j=0;j<idxAttributes.size();j++) {
							//if(idxAttributes.get(j)<allAttributes.get(idxAttributes.get(j))) {
								//LOGGER.info("index"+(idxAttributes.get(j)!=18?idxAttributes.get(j):11));
								//LOGGER.info(""+allAttributes.get(idxAttributes.get(j)!=18?idxAttributes.get(j):18));
								//LOGGER.info("columns index "+(3+includedAttributes.indexOf(allAttributes.get(idxAttributes.get(j)!=18?idxAttributes.get(j):18))));
								
								ordered[j+3]=temp[3+includedAttributes.indexOf(allAttributes.get(idxAttributes.get(j)!=18?idxAttributes.get(j):18))];
							//}else {
								//LOGGER.info("columns index "+(3+allAttributes.get(idxAttributes.get(j))+arr[j]-3));
								//ordered[j+3]=temp[3+allAttributes.get(idxAttributes.get(j))+arr[j]-3];
							//}
																
						}

						//.info(Arrays.toString(ordered));
						result.set(i, ordered);
						}
					}
				}
				}

				
				
			//	headerNames=getHeaderNames(schemePosAttr);

				// za OPENDATA
				if (opendata) {
					
//					headerNames=getHeaderNames(schemePosAttr);
					
					openDataObj.getData().put("headers", headerNames.toArray());
					for (int i = 0; i < result.size(); i++) {
						openDataObj.getData().put("row"+(i+1),Arrays.copyOfRange(result.get(i), 4, result.get(i).length));
					}
				}
			}else {
				//samo za SMDX i SDMX-JSON
				LOGGER.info("Pravim SDMX");
				//listPost=addLangToMainPosAsPositionS(result, listLang, unitsList, schemePosAttr);
				// SORT
				//new PositionSDAO(ActiveUser.DEFAULT).doSortSchemePrev(listPost);
				//za vrashtane na stariq resultat moje da go pomnim i v promenliva
				//result = new PositionSDAO(ActiveUser.DEFAULT).loadPositionsForExport(idObj, schemePosAttr,usingSimpleObject);
//				result=
				result=addLangToMainPosAsObj(result, listLang, mapUnit, schemePosAttr);
				new PositionSDAO(ActiveUser.DEFAULT).doSortSchemePrevAsObj(result, idObj);
				progress=25;
				
				progress=30;
				
				//TODO CREATE FILE - CHAKAME YONCHO.
				//transformirame PositionsS in codeItem
				LOGGER.info("listPost.size() "+listPost.size());
				LOGGER.info("result.size() "+result.size());
				codes = new ArrayList<CodeItem>();
//				for(int i=0;i<listPost.size();i++) {
//					PositionS pos= listPost.get(i);
//					Set <Integer> langs=pos.getLangMap().keySet();
//					String[] names= new String[langs.size()];
//					String[] descriptions=new String[langs.size()];
//					int j=0;
//					for (Integer lang : langs) {
//						PositionLang pl=pos.getLangMap().get(lang);
//						names[j]=pl.getLongTitle();
//						descriptions[j]=pl.getComment();
//						j++;
//					}
//					CodeItem item=new CodeItem(pos.getCode(), names, descriptions);
//					codes.add(item);
//				}
				
				List<Integer> includedAttributes= new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idObj);
				Collections.sort(includedAttributes);

				includedInReport=new ArrayList<Boolean>();
				
				boolean hasIntervals=!begs.isEmpty()&&!ends.isEmpty();
				boolean hasLevels=!levels.isEmpty();
				boolean hasNodeCodes=null!=nodeCodes&&!nodeCodes.isBlank();
				Boolean [] includedInInterval=new Boolean[result.size()];
				if(hasIntervals) {
					includedInInterval=codesBetween(result,begs.split(","), ends.split(","));
				}
				includedInReport.addAll(Arrays.asList(new Boolean[result.size()]));
				
				for(int i=0; i<result.size();i++) {
					/*================= Generate includedInReport array===================*/
					Object[] temp=result.get(i);
				
					includedInReport.set(i, Boolean.TRUE);
					if(hasIntervals) {
						includedInReport.set(i, includedInInterval[i]);
					}

					if(hasLevels) {
						if(!levels.contains(""+temp[3+includedAttributes.indexOf(6)])) {
							includedInReport.set(i, Boolean.FALSE);
						}
					}
					
					if(hasNodeCodes) {
						if(nodeCodes.contains(""+temp[3])) {
							includedInReport.set(i, Boolean.FALSE);
						}
					}
					
				/*=================End includedInReport array ===================*/
					
					Object[] ob= result.get(i);
					String[] names= {""+ob[3+includedAttributes.indexOf(11)]};//11
					String[] descriptions= {""+ob[3+includedAttributes.indexOf(15)]};//15
					CodeItem item=new CodeItem(""+ob[3+includedAttributes.indexOf(3)], names, descriptions);//3
					codes.add(item);
					//LOGGER.info("includedAttributes "+Arrays.toString(includedAttributes.toArray()));
					//LOGGER.info(""+(3+includedAttributes.indexOf(3))+" - "+(3+includedAttributes.indexOf(11))+" - "+(3+includedAttributes.indexOf(15)));
					//LOGGER.info("item "+ob[3+includedAttributes.indexOf(3)]+" - "+ ob[3+includedAttributes.indexOf(11)]+" - "+ob[3+includedAttributes.indexOf(15)]);
					//LOGGER.info(Arrays.toString(temp));
				}
				//if we come from positions we show the whole list
				if(pos.equals("positions")) {
					includedInReport=new ArrayList<Boolean>();
					Boolean[] arr=new Boolean[result.size()];
					Arrays.fill(arr, Boolean.TRUE);
					includedInReport.addAll(Arrays.asList(arr));
				}
				LOGGER.info("codes.size() "+codes.size());
				
			}
	
	}
	
	
	/**
	 * Метода връща масив където с true са отбелязани стойностите между два интервал, където начало на интервал е в begs, а край в ends
	 */
	public static Boolean[] codesBetween(List<Object[]> result2, String[] begs, String ends[]) {
		Boolean[] result = new Boolean[result2.size()];
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		for (int i = 0; i < result2.size(); i++) {
			result[i] = Boolean.FALSE;
			for (int j = 0; j < begs.length; j++) {
				if ((""+result2.get(i)[3]).trim().equals(begs[j])) {
					indexes.add(i);
				}
				if ((""+result2.get(i)[3]).trim().equals(ends[j])) {
					indexes.add(i);
				}
			}
		}
		for (int i = 0; i < indexes.size(); i += 2) {
			Arrays.fill(result, indexes.get(i), indexes.get(i+1)+1, Boolean.TRUE);
		}
		return result;
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
    		rez.add("Национална измерителна единица");	
    	}
    	if (schemePosAttr.containsKey(8)) {
    		rez.add("Международна измерителна единица");	
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
		//System.out.println("pos"+ Arrays.toString( pos.get(0)));
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
						String[] s=new String[1];
						if (s7!=null && s8!=null) {
							s=new String[2];							
						}
						if (s7!=null) {
							s[0]=s7;
						}
						if (s8!=null) {
							if (s7==null) {
								s[0]=s8;	
							}else {
								s[1]=s8;
							}
						}
				//System.out.println("s7 "+s7+" s8 "+s8);
						
					
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
		
		//System.out.println("resultList"+Arrays.toString(resultList.get(0)));
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
	
	public void createSDMX(File file, int skipFirst) {
		//corresponding tables only
		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			org.w3c.dom.Document document = null;
			
			try {
				// optional, but recommended
				// process XML securely, avoid attacks like XML External Entities (XXE)
				dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

				// parse XML file
				DocumentBuilder db = dbf.newDocumentBuilder();
				  // The class loader that loaded the class
				//URL urlConfig = DownloadFile.class.getResource("//export//SDMXtemplate.xml" );
				InputStream is = DownloadFileExp.class.getClassLoader().getResourceAsStream("/SDMXtemplate.xml");
				copyInputStreamToFile(is, file);
			       
				//LOGGER.info("File Size "+file.getTotalSpace());
				document = db.parse(file);
				
				if(skipFirst==1) {
					//ako sme v ralacii
					List<Obs> observations = new ArrayList<Obs>();
					for (int i = 0; i < oldResult.size(); i++) {
						Object[] temp = oldResult.get(i);
						if(includedInReport.get(i) ) {
							observations.add(new Obs("" + temp[2], "" + temp[1], "" + temp[3]));
						}
					}
					createHeaders(document);
					createObservations(document, observations, separator);
				}else {
					//ako sme vav versii
					createHeaders(document);
					ArrayList<CodeItem> tmpcodes = new ArrayList<CodeItem>();
					for(int i=0;i<codes.size();++i) {
						if(includedInReport.get(i)) {
							tmpcodes.add(codes.get(i));
						}
					}
					
					createCodeListContent(document,tmpcodes);
					//createCodeListContent(document,codes);
					//da se izgradi nakraq hierarchy
				}
					

				DOMSource source = new DOMSource(document);
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				//za po-goliama chetimost
				//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","1");

				FileOutputStream f= new FileOutputStream(file);
				StreamResult res = new StreamResult(f);
				
				//StreamResult result = new StreamResult("versii.xml");
				transformer.transform(source, res);
				
				
				progress = 100;
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
	
	
	public void createSDMXJ(File file, int skipFirst) {
		
		try {

			//Gson gson = new GsonBuilder().setPrettyPrinting().create();
			try (FileWriter fw = new FileWriter(file)) {
				Header h = createHeaders();
				String json="";
				//corresponding tables only
				if(skipFirst==1) {
					JSONContent content=null;
					List<RelationshipItem> corespondingCodes = new ArrayList<RelationshipItem>();
					for (int i = 0; i < oldResult.size(); i++) {
						Object[] temp = oldResult.get(i);	
						if(includedInReport.get(i) ) {
							updateProgress(oldResult.size(), i);
							corespondingCodes.add(new RelationshipItem("" + (temp[0]==null?"":temp[0]), "" + (temp[1]==null?"":temp[1]), 
									"" + (temp[2]==null?"":temp[2]), "" + (temp[3]==null?"":temp[3])));
						}
					}
					ArrayList<Attributes> codeItemsJSON = classifToCodeListJSON(corespondingCodes);
					 content = new JSONContent(h, codeItemsJSON);
					 json=JSonUtils.object2json(content, true);
				}else {
					JSONContentV content=null;
					
					ArrayList<CodeItem> tmpcodes = new ArrayList<CodeItem>();
					for(int i=0;i<codes.size();++i) {
						if(includedInReport.get(i)) {
							updateProgress(codes.size(), i);
							tmpcodes.add(codes.get(i));
						}
					}
					
					ArrayList<CodeItemJSON> codeItemsV=classifToCodeListJSON(tmpcodes);
					//ArrayList<CodeItemJSON> codeItemsV=classifToCodeListJSON(codes);
					
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
				progress = 100;
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
	
	public void createRelationPdf(File file, int skipFirst) {
		try {
			
			Document doc = new Document();
			//ako sme vav versii landscape
//			if(skipFirst==3) {
//				doc=new Document(PageSize.A4.rotate(), 0f, 0f, 0f, 0f) ;
//			}
			PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));


			
//			if(order.size()>0) {
//				for(int i=0;i<order.size();i++) {
//					
//				}
//			}
			
			int idxNat=headerNames.indexOf("Национална измерителна единица");
			int idxInt=headerNames.indexOf("Международна измерителна единица");
			
			addUnitsHeaders();
				
				
			PdfPTable table = new PdfPTable(headerNames.size());
			table.setWidthPercentage(100);
	      	
		    for (int i = 0; i < headerNames.size(); i++) {
		    	table.addCell(headerNames.get(i));
			}
		    
		    int i=0;
		    for (int j = 0; j < result.size(); j++) {
		    	updateProgress(result.size(), i);
		    	if(includedInReport.get(j) ) {
		    		for (int a = skipFirst; a < result.get(i).length; a++) {
		    			if(this.units.equals("true")) {
		    				if(a==(skipFirst +idxNat) || a==(skipFirst +idxInt)){
								String unitVals=result.get(i)[a]==null?"":(""+result.get(i)[a]);
								String [] vals=unitVals.split(",");
								
								//val1
								PdfPCell cell = new PdfPCell(new Paragraph(new Chunk(vals[0].replace("\r", "").replace("\n", ""),FontFactory.getFont(FontFactory.HELVETICA, 12))));
								cell.setNoWrap(false);
								table.addCell(vals[0].replace("\r", "").replace("\n", ""));
								
								//val2
								cell = new PdfPCell(new Paragraph((vals.length>1)?vals[1].replace("\r", "").replace("\n", ""):"",FontFactory.getFont(FontFactory.HELVETICA, 12)));
								cell.setNoWrap(false);
								table.addCell((vals.length>1)?vals[1].replace("\r", "").replace("\n", ""):"");
							}else {
								PdfPCell cell = new PdfPCell(new Paragraph(new Chunk(result.get(i)[a]==null?"":(""+result.get(i)[a]).replace("\r", "").replace("\n", ""),FontFactory.getFont(FontFactory.HELVETICA, 12))));
				    			cell.setNoWrap(false);
				    	 		table.addCell(result.get(i)[a]==null?"":(""+result.get(i)[a]).replace("\r", "").replace("\n", ""));
							}
		    			}else {
		    				PdfPCell cell = new PdfPCell(new Paragraph(new Chunk(result.get(i)[a]==null?"":(""+result.get(i)[a]).replace("\r", "").replace("\n", ""),FontFactory.getFont(FontFactory.HELVETICA, 12))));
			    			cell.setNoWrap(false);
			    	 		table.addCell(result.get(i)[a]==null?"":(""+result.get(i)[a]).replace("\r", "").replace("\n", ""));
		    			}
			        }
		    	}
				i++;
		    }
		    
			progress = 100;
			doc.open();

			doc.add(table);
			doc.close();
			writer.close();
			
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
	        XSSFSheet sheet = workbook.createSheet("Relationship table");
	        
	      //ako sme vav versii landscape
			if(skipFirst==3) {
				sheet.getPrintSetup().setLandscape(true);
			}
	        
	        int rowNum = 0; 
			org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
			int colNum = 0;
			
			
			int idxNat=headerNames.indexOf("Национална измерителна единица");
			int idxInt=headerNames.indexOf("Международна измерителна единица");
			
			addUnitsHeaders();
			
		    for (int i = 0; i < headerNames.size(); i++) {
		    	org.apache.poi.ss.usermodel.Cell cell = row.createCell(colNum++);
		    	cell.setCellValue(headerNames.get(i));
			}
//		    LOGGER.info("colNum " +colNum);
//		    LOGGER.info("includedInReport " +includedInReport.toString());
//		    LOGGER.info("result " +result.size());
		    int i=0;
		    for (int j = 0; j < result.size(); j++) {
		    	updateProgress(result.size(), i);
		    	if(includedInReport.get(j)) {
		    		org.apache.poi.ss.usermodel.Row row1 = sheet.createRow(rowNum++);
			    	int colNum1 = 0;
		    	 	for (int a = skipFirst; a < result.get(i).length; a++) {
		    	 		if(this.units.equals("true")) {
		    				if(a==(skipFirst +idxNat) || a==(skipFirst +idxInt)){
								String unitVals=result.get(i)[a]==null?"":(""+result.get(i)[a]);
								String [] vals=unitVals.split(",");
								
								//val1
								org.apache.poi.ss.usermodel.Cell cell = row1.createCell(colNum1++);
				    	 		cell.setCellValue(vals[0].replace("\r", "").replace("\n", ""));
				    	 		
				    	 		//vall2
				    	 		cell = row1.createCell(colNum1++);
				    	 		cell.setCellValue((vals.length>1)?vals[1].replace("\r", "").replace("\n", ""):"");
								
		    				}else {
		    					org.apache.poi.ss.usermodel.Cell cell = row1.createCell(colNum1++);
				    	 		cell.setCellValue(result.get(i)[a]==null?"":(""+result.get(i)[a]).replace("\r", "").replace("\n", ""));
		    				}
		    	 		}else {
		    	 			org.apache.poi.ss.usermodel.Cell cell = row1.createCell(colNum1++);
			    	 		cell.setCellValue(result.get(i)[a]==null?"":(""+result.get(i)[a]).replace("\r", "").replace("\n", ""));
		    	 		}
			        }
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
	
	/**
	 * Metod na Silvia za encoding-a, raboti pod windows 
	 */
	public void createRelationCsvOrTxtSilvia(File file, int skipFirst) {
		try {
			StringBuilder sb = new StringBuilder();
			
			//FileOutputStream f= new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file), "CP1251"));
			
			for (int i = 0; i < headerNames.size(); i++) {
	            sb.append(headerNames.get(i));
				sb.append(separator);
			}
			sb.append(System.lineSeparator());
			
			
			for (int i = 0; i < result.size(); i++) {
				updateProgress(result.size(), i);
				if(includedInReport.get(i)) {
					for (int a = skipFirst; a < result.get(i).length; a++) {
						sb.append(result.get(i)[a]==null?"":""+(""+result.get(i)[a]).replace("\r", "").replace("\n", ""));
						if (a<result.get(i).length-1) {
							sb.append(separator);	
						}
					}
					sb.append(System.lineSeparator());
				}
			}
			progress=100; 
			
//	      f.write(sb.toString().getBytes());
//	      f.flush();
//	      f.close();
		byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
		String result = new String(bytes, StandardCharsets.UTF_8);
			
		bw.write(result);
		bw.flush();
		bw.close();
			
		} catch (Exception e) {			
			e.printStackTrace();
			 
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
	public void createRelationCsvOrTxt(File file, int skipFirst) {
		try {
			StringBuilder sb = new StringBuilder();
			
			//FileOutputStream f= new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
			
			int idxNat=headerNames.indexOf("Национална измерителна единица");
			int idxInt=headerNames.indexOf("Международна измерителна единица");
			
			addUnitsHeaders();
			
			for (int i = 0; i < headerNames.size(); i++) {
	            sb.append(headerNames.get(i));
				sb.append(separator);
			}
			sb.append(System.lineSeparator());
			
			
			for (int i = 0; i < result.size(); i++) {
				updateProgress(result.size(), i);
				if(includedInReport.get(i)) {
					for (int a = skipFirst; a < result.get(i).length; a++) {
						if(this.units.equals("true")) {
		    				if(a==(skipFirst +idxNat) || a==(skipFirst +idxInt)){
								String unitVals=result.get(i)[a]==null?"":(""+result.get(i)[a]);
								String [] vals=unitVals.split(",");
								
								//val1
								sb.append(vals[0].replace("\r", "").replace("\n", ""));
								
								//val2
								sb.append((vals.length>1)?vals[1].replace("\r", "").replace("\n", ""):"");
								
		    				}else {
		    					sb.append(result.get(i)[a]==null?"":""+(""+result.get(i)[a]).replace("\r", "").replace("\n", ""));
		    				}
						}else {
							sb.append(result.get(i)[a]==null?"":""+(""+result.get(i)[a]).replace("\r", "").replace("\n", ""));
						}
						
						if (a<result.get(i).length-1) {
							sb.append(separator);	
						}
					}
					sb.append(System.lineSeparator());
				}
			}
			progress=100; 
			
//	      f.write(sb.toString().getBytes());
//	      f.flush();
//	      f.close();
	      
		bw.write(sb.toString());
		bw.flush();
		bw.close();
			
		} catch (Exception e) {			
			e.printStackTrace();
			 
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	

	public void createRelationOds(File file, int skipFirst) {
		int counterODS=0;//promenliva, koiato pokava kolko redove shte ima v exporta
		for(int i=0;i<includedInReport.size();++i) {
			if(includedInReport.get(i)) {
				counterODS+=1;
			}
		}
		try {
			
			  	//int rows = result.size()+1;//headers+broi zapisi
				int rows=counterODS+1;//included rows in the export +headers
	            int columns = headerNames.size();
	            Sheet sheet = new Sheet("Corresponding tables", rows, columns);
	            SpreadSheet spread = new SpreadSheet();
	            spread.appendSheet(sheet);
	            
	        	int idxNat=headerNames.indexOf("Национална измерителна единица");
				int idxInt=headerNames.indexOf("Международна измерителна единица");
				
				addUnitsHeaders();
	            
	            for (int i = 0; i < headerNames.size(); i++) {
	            	sheet.getRange(0,i).setValue(headerNames.get(i));
	            }
	             int cntRows=1;//startirame ot 1vi red sled headeri
	            for (int j = 0; j < result.size(); j++) {
			    	updateProgress(result.size(), j);
			    	if(includedInReport.get(j)) {
				    	for (int a = skipFirst; a < result.get(j).length; a++) {
				    		// j+1, за да запазим хеадерите и a-skipFirst, за да се позиционираме на първата клетка в редицата
				    		if(this.units.equals("true")) {
			    				if(a==(skipFirst +idxNat) || a==(skipFirst +idxInt)){
									String unitVals=result.get(j)[a]==null?"":(""+result.get(j)[a]);
									String [] vals=unitVals.split(",");
									
									sheet.getRange(cntRows,a-skipFirst).setValue(vals[0].replace("\r", "").replace("\n", ""));
									sheet.getRange(cntRows,a-skipFirst+1).setValue((vals.length>1)?vals[1].replace("\r", "").replace("\n", ""):"");
			    				}else {
			    					sheet.getRange(cntRows,a-skipFirst).setValue(result.get(j)[a]==null?"":(""+result.get(j)[a]).replace("\r", "").replace("\n", ""));
			    				}
				    		}else{
				    			sheet.getRange(cntRows,a-skipFirst).setValue(result.get(j)[a]==null?"":(""+result.get(j)[a]).replace("\r", "").replace("\n", ""));
				    		}
			    				
				    	 
				    	 }   
				    	cntRows+=1;
			    	}
	
			    }
			    progress=100;
				// Save document
			    spread.save(new File(""+file.getAbsoluteFile().getPath()));
			
		} catch (Exception e) {			
			e.printStackTrace();
			
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	

	public void createRelationHtml(File file, int skipFirst) {
		try {
			
			
//			FileOutputStream f= new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			//PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(f, "UTF-8"));
			String title = (skipFirst==1?"Кореспондираща таблица":"Таблица на версиите");
			
			StringBuilder sb = new StringBuilder();
			sb.append("<html>")
			.append("<head>")
			.append("<meta charset=\"UTF-8\">")
			.append("<title>").append(title).append("</title>")
			.append("</head>")
			.append("<body>");
				sb.append("<table border=\"1px solid thin black\" style=\" border-collapse: collapse;\">");
			
				sb.append("<tr>");
				
				int idxNat=headerNames.indexOf("Национална измерителна единица");
				int idxInt=headerNames.indexOf("Международна измерителна единица");
				
				addUnitsHeaders();
				
				
				for (int i = 0; i < headerNames.size(); i++) {
					sb.append("<td>");
						sb.append(headerNames.get(i));
					sb.append("</td>");
					
				}
				sb.append("</tr>");
				
				
				for (int i = 0; i < result.size(); i++) {
					updateProgress(result.size(), i);
					if(includedInReport.get(i)) {
						sb.append("<tr>");
						// DA NE ZABRAVISH DA SKIPNESH PARVITE 3 te sa ID,ID_PREV,ID_PARENT - polzvat se za sortiraneto i ne vlizat v export-a
						for (int j = skipFirst; j < result.get(i).length; j++) {
							if(this.units.equals("true")) {
			    				if(j==(skipFirst +idxNat) || j==(skipFirst +idxInt)){
									String unitVals=result.get(i)[j]==null?"":(""+result.get(i)[j]);
									String [] vals=unitVals.split(",");
									
									sb.append("<td>");
									sb.append(vals[0].replace("\r", "").replace("\n", ""));
									sb.append("</td>");
									sb.append("<td>");
									sb.append((vals.length>1)? vals[1].replace("\r", "").replace("\n", ""):"");
									sb.append("</td>");
			    				}else {
			    					sb.append("<td>");
									sb.append(result.get(i)[j]==null?"":(""+result.get(i)[j]).replace("\r", "").replace("\n", ""));
								sb.append("</td>");
			    				}
							}else{
								sb.append("<td>");
								sb.append(result.get(i)[j]==null?"":(""+result.get(i)[j]).replace("\r", "").replace("\n", ""));
							sb.append("</td>");
							}
							
						}
							
						sb.append("</tr>");
					}
				}
				sb.append("</table>");
				sb.append("</body>");
			sb.append("</html>");
			progress=100;
//			f.write(sb.toString().getBytes());
//			f.flush();
//			f.close();
			
			bw.write(sb.toString());
			bw.flush();
			bw.close();

//			printWriter.println("<html>");
//				printWriter.println("<table border=\"1px solid thin black\" style=\" border-collapse: collapse;\">");
//			
//				printWriter.println("<tr>");
//				for (int i = 0; i < headerNames.size(); i++) {
//					printWriter.println("<td>");
//						printWriter.println(headerNames.get(i));
//					printWriter.println("</td>");
//					
//				}
//				printWriter.println("</tr>");
//				
//				
//				for (int i = 0; i < result.size(); i++) {
//					updateProgress(result.size(), i);
//					
//					printWriter.println("<tr>");
//					// DA NE ZABRAVISH DA SKIPNESH PARVITE 3 te sa ID,ID_PREV,ID_PARENT - polzvat se za sortiraneto i ne vlizat v export-a
//					for (int j = skipFirst; j < result.get(i).length; j++) {
//						printWriter.println("<td>");
//							printWriter.println(result.get(i)[j]==null?"":("\""+result.get(i)[j]).replace("\r", "").replace("\n", "")+"\"");
//						printWriter.println("</td>");
//					}
//						
//					printWriter.println("</tr>");
//				}
//				printWriter.println("</table>");
//			printWriter.println("</html>");
//			progress=100;
//			printWriter.close();
			
		} catch (Exception e) {			
			e.printStackTrace();
			
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
	public void addUnitsHeaders() {
		if(this.units.equals("true")) {
			int idxNat1=headerNames.indexOf("Национална измерителна единица");
			headerNames.set(idxNat1, "Национална измерителна единица 1");
			headerNames.add(idxNat1+1,"Национална измерителна единица 2");
			int idxInt1=headerNames.indexOf("Международна измерителна единица");
			headerNames.set(idxInt1, "Международна измерителна единица 1");
			headerNames.add(idxInt1+1,"Международна измерителна единица 2");
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

	public boolean isUsingSimpleObject() {
		return usingSimpleObject;
	}

	public void setUsingSimpleObject(boolean usingSimpleObject) {
		this.usingSimpleObject = usingSimpleObject;
	}

	public List<PositionS> getListPost() {
		return listPost;
	}

	public void setListPost(List<PositionS> listPost) {
		this.listPost = listPost;
	}

	public List<Object[]> getOldResult() {
		return oldResult;
	}

	public void setOldResult(List<Object[]> oldResult) {
		this.oldResult = oldResult;
	}

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

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
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

	public ArrayList<CodeItem> getCodes() {
		return codes;
	}

	public void setCodes(ArrayList<CodeItem> codes) {
		this.codes = codes;
	}

	public List<String> getOrder() {
		return order;
	}

	public void setOrder(List<String> order) {
		this.order = order;
	}
	
	public String getTypeRel() {
		return typeRel;
	}

	public void setTypeRel(String typeRel) {
		this.typeRel = typeRel;
	}

	public String getChangeRel() {
		return changeRel;
	}

	public void setChangeRel(String changeRel) {
		this.changeRel = changeRel;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getBegs() {
		return begs;
	}

	public void setBegs(String begs) {
		this.begs = begs;
	}

	public String getEnds() {
		return ends;
	}

	public void setEnds(String ends) {
		this.ends = ends;
	}

	public String getLevels() {
		return levels;
	}

	public void setLevels(String levels) {
		this.levels = levels;
	}

	public String getNodeCodes() {
		return nodeCodes;
	}

	public void setNodeCodes(String nodeCodes) {
		this.nodeCodes = nodeCodes;
	}
	

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}
	
	public List<Boolean> getIncludedInReport() {
		return includedInReport;
	}

	public void setIncludedInReport(List<Boolean> includedInReport) {
		this.includedInReport = includedInReport;
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

		String INDENTIFIER = "IDREF1";
		String SENDER = "NSIsystemClassif";
		String RECEIVER = "INDEXBG";

		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		Header h = new Header(INDENTIFIER, sdf.format(today), SENDER, RECEIVER);

		return h;

	}

	public String getExpName() {
		return expName;
	}

	public void setExpName(String expName) {
		this.expName = expName;
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
				//int index = usedTargetCodes.get(item.getCodeTarget());
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
