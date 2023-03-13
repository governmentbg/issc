package com.ib.nsiclassif.beansSite;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
//import javax.servlet.http.HttpSession;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.indexui.system.IndexUIbean;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dto.Publication;
import com.ib.nsiclassif.db.dto.PublicationLang;
import com.ib.nsiclassif.db.dao.PublicationDAO;
import com.ib.nsiclassif.db.dao.PublicationLangDAO;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.nsiclassif.system.NSIConstants;


@Named("publExtData")
@ViewScoped
public class PublExtBean extends IndexUIbean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2224304047817601824L;


	/**
	 * Основен javaBean клас за въвеждане/актуализация на публикациите
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(PublExtBean.class);
	
	
	private Publication publication = new Publication();
	private Integer idPubl = null;
	private String sectionText="";
	private Integer codeSection=null;
	private Integer idUser=null;
	private StreamedContent imageCont=null;
	private StreamedContent imageContG=null;
	private Integer param=null;
//	private Integer oldLang=null;
	private PublicationLang publSelLang = new PublicationLang();
	private List<PublicationLang> publLangList = new ArrayList<PublicationLang>();
//	private HashMap<Long, PublicationLang> publListHM = new HashMap<Long, PublicationLang>();
	
	//Files
	
	private List<Files> filesList = new ArrayList<Files>();
//	private HashMap<Long, List<Files>> filesListHM = new HashMap<Long, List<Files>>();
	
	private String titleText="";
	
	private List<Object[]> images;
	private Files fullF;
	private String navi;
	private String classL = "p-col-12 p-md-4";
	private String classR = "p-col-12 p-md-8";
	private Integer lang;
	private String locale;
	
	
	/**Инициира/сетва първоначалните стойности на атрибутите на обектите. Чете предадените параметри от други екрани 
	 * 
	 */
	@PostConstruct
	public void initData(){
		LOGGER.debug("PostConstruct!");
		
		this.locale = JSFUtils.getRequestParameter("locale");
		
		if (null != this.locale && this.locale.equalsIgnoreCase("en")) {
			this.lang= 2;
		}else {
			this.setLang(NSIConstants.CODE_DEFAULT_LANG);
		}
		
		getUserData().getUserId();
		actionClear();
		
		String par =JSFUtils.getRequestParameter("idPubl");
		this.navi=null;
		if (null!=par){
			String[] params = par.split(";");
			int br=params.length;
			if (br>0) {
				if(null!=params[0])
					this.idPubl = Integer.valueOf(params[0].toString());
				
				if(br>1 && null!=params[1])
					this.navi= params[1].toString();
				
			}
			
			
		}else {
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "general.invalidParameter"));
			return;
		}
		
		String sect =JSFUtils.getRequestParameter("sect");
		if (sect != null && !sect.trim().isEmpty()){
			this.setCodeSection(Integer.valueOf(sect));
		}else {
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "general.invalidParameter"));
			return;
		}
	
		if (null!=this.idPubl)
			loadPublById(this.idPubl);
		

	}
	
	/**Изтрива стойностите на определени атрибути на обектите
	 * 
	 */
	public void actionClear(){
		this.publication = new Publication();
		this.publLangList = new ArrayList<PublicationLang>();
		this.publSelLang = new PublicationLang();
		this.setIdPubl(null);

		this.sectionText="";
		this.codeSection=null;
		
		this.imageCont=null;
		this.filesList = new ArrayList<>();
	
//		this.oldLang = getCurrentLang();
//		clearListHM();
	}

	public Publication getPublication() {
		return publication;
	}

	public void setPublication(Publication publication) {
		this.publication = publication;
	}

	
	public String getSectionText() {
		return sectionText;
	}

	public void setSectionText(String sectionText) {
		this.sectionText = sectionText;
	}

	public Integer getCodeSection() {
		return codeSection;
	}

	public void setCodeSection(Integer codeSection) {
		this.codeSection = codeSection;
		this.publication.setSection(codeSection);
	}

	public Date getCurrentDate() {
		return new Date();
	}

	public StreamedContent getImageCont() {
		return imageCont;
	}

	public void setImageCont(StreamedContent imageCont) {
		this.imageCont = imageCont;
	}

	
	
/** Избор на файлове за присъединяване към публикациите 
 * @param event
 */


	public List<Files> getFilesList() {
		return filesList;
	}
	
	public void setFilesList(List<Files> filesList) {
		this.filesList = filesList;
	}
	

		
	/** Извлича присъединените файлове от БД и ги предлага за разглеждане/съхраняване при клиента 
	 * @param file
	 */
	public void download(Files file){
		try {
			
			if(file.getContent() == null && file.getId() != null) {
			
				FilesDAO filesDAO = new FilesDAO(getUserData());
				file = filesDAO.findById(file.getId());
				
				if(file.getPath() != null && !file.getPath().isEmpty()){
					Path path = Paths.get(file.getPath());
					file.setContent(java.nio.file.Files.readAllBytes(path));
				}
			
			}
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
		    ExternalContext externalContext = facesContext.getExternalContext();
		    externalContext.setResponseHeader("Content-Type", "application/x-download");
		    externalContext.setResponseHeader("Content-Length", file.getContent().length + "");
		    externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + file.getFilename() + "\"");
			externalContext.getResponseOutputStream().write(file.getContent());
			facesContext.responseComplete();
			
		} catch (DbErrorException e) {
			LOGGER.error("DbErrorException: " + e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString("beanMessages", "general.errDataBaseMsg"),e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при сваляне на файла!: ",e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при сваляне на файла!: ",e.getMessage());
		}
	}

	
	public Integer getIdPubl() {
		return idPubl;
	}


	public void setIdPubl(Integer idPubl) {
		this.idPubl = idPubl;
	}

	
	/** Извлича данни за определена публикация по ид.
	 * @param idPubl
	 */
	
	public void loadPublById(Integer idPubl){
		
		try {
	
			// Езиковите версии

			JPA.getUtil().runWithClose(() -> {this.publLangList = (List<PublicationLang>) new PublicationLangDAO(getUserData()).findByIdPublSingleLang(this.idPubl, this.lang);
			
			if (null==this.idPubl)
				this.idPubl = new PublicationDAO(getUserData()).checkForSinglePublInSectNoLang(this.codeSection, this.lang);
			if (null!=this.idPubl) {
				this.publication = (Publication) new PublicationDAO(getUserData()).findById(this.idPubl);
	
				if(this.publication.getTypePub()==null || this.publication.getTypePub().intValue()==NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_MATERIALI) {
					this.filesList = (List<Files>)new FilesDAO(getUserData()).selectByFileObjectDop(this.idPubl, NSIConstants.CODE_ZNACHENIE_JOURNAL_PUBLICATION);
				} else {
					this.filesList = (List<Files>)new FilesDAO(getUserData()).selectByFileObject(this.idPubl, NSIConstants.CODE_ZNACHENIE_JOURNAL_PUBLICATION);	
				}	
			}
			});
			
			if (null!=this.publication) {
				this.codeSection = this.publication.getSection();
				
				if (null!=this.publLangList && ! this.publLangList.isEmpty()) {
					refreshImagePub();
					
					if(publication.getTypePub().intValue()==NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_IMAGES) {
						this.images = new ArrayList<Object[]>();
						
						for(Files f :this.filesList) {
							fullF = new Files();
							JPA.getUtil().runWithClose(() -> fullF = (Files) new FilesDAO(getUserData()).findById(f.getId()));
							InputStream imageStreamG = new ByteArrayInputStream(fullF.getContent());
							imageContG = DefaultStreamedContent.builder().contentType("image/jpeg").name("").stream(() -> imageStreamG).build();
							//this.images.add(new Photo(imageContG, null, fullF.getFileInfo().toString(), fullF.getFilename().toString()));
							this.images.add(new Object[]{imageContG,fullF.getFileInfo()});
						}
					}
		
					if (null!=this.publLangList && this.publLangList.size()>0)
						this.publSelLang = this.publLangList.get(0);
					
					if (this.publication.getTypePub()==1) {
						this.classL ="p-col-12 p-md-3";
						this.classR = "p-col-12 p-md-9";
					}else if (this.publication.getTypePub()==2) {
						this.classL ="p-col-12 p-md-6";
						this.classR = "p-col-12 p-md-6";
					}
				
				}
			}
						
//			loadLangHM();
			
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане секцията! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		}
		
	}
	
	public Integer getIdUser() {
		return idUser;
	}
	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}

	public Integer getParam() {
		return param;
	}

	public void setParam(Integer param) {
		this.param = param;
	}



	/*public void clearListHM() {
		List<SystemClassif> langElementsList= new ArrayList<SystemClassif>();
		try {
			langElementsList=getSystemData().getSysClassification(Constants.CODE_CLASSIF_LANG, new Date(), getCurrentLang(), this.idUser);
		} catch (DbErrorException e) {
			LOGGER.error("DbErrorException: " + e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString("beanMessages", "general.errDataBaseMsg"),e.getMessage());
		}
		this.publListHM = new HashMap<Long, PublicationLang>();
		for (SystemClassif item: langElementsList){
			this.publListHM.put(Long.valueOf(item.getCode()), new PublicationLang());
		}
		
		this.filesListHM = new HashMap<Long, List<Files>>();
		for (SystemClassif item: langElementsList){
			this.filesListHM.put(Long.valueOf(item.getCode()), new ArrayList<Files>());
		}
	
	}*/
	

	
	public List<PublicationLang> getPublLangList() {
		return publLangList;
	}

	public void setPublLangList(List<PublicationLang> publLangList) {
		this.publLangList = publLangList;
	}

	/*public HashMap<Long, PublicationLang> getPublListHM() {
		return publListHM;
	}

	public void setPublListHM(HashMap<Long, PublicationLang> publListHM) {
		
		this.publListHM = publListHM;
	}*/

	/*
	 * public Integer getOldLang() { return oldLang; }
	 * 
	 * public void setOldLang(Integer oldLang) { this.oldLang = oldLang; }
	 */

	public PublicationLang getPublSelLang() {
		return publSelLang;
	}

	public void setPublSelLang(PublicationLang publSelLang) {
		this.publSelLang = publSelLang;
	}
	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	
	/*public HashMap<Long, String> getLangHM() {
		return langHM;
	}

	public void setLangHM(HashMap<Long, String> langHM) {
		this.langHM = langHM;
	}*/

	/*public List<PublicationLang> getPubLdelLang() {
		return pubLdelLang;
	}

	public void setPubLdelLang(List<PublicationLang> pubLdelLang) {
		this.pubLdelLang = pubLdelLang;
	}*/
	
	/*public void loadLangHM() {
		// Publication langs		
		if (null!=this.publLangList && this.publLangList.size() > 0) {
			for (int i = 0; i < this.publLangList.size(); i++) {
				if (null!=this.publLangList.get(i).getLang()) 
					this.publListHM.put(this.publLangList.get(i).getLang(), this.publLangList.get(i));
			}
		}
		
		// Проверява за липсващи езикови версии 
		List<Long> lanYes=new ArrayList<>(); 
		List<Long> lanNo=new ArrayList<>(); 
		Set<Long> hmSet=this.publListHM.keySet(); 
		for (Long item: hmSet) {
			if (null!=this.publListHM.get(item).getLang()){
				lanYes.add(item);
			}else {
				lanNo.add(item);
			}
		}
		
		if (null!=lanNo && lanNo.size()>0) {
			PublicationLang pBG=new PublicationLang();
			if(!lanYes.isEmpty() && lanYes.contains(Long.valueOf(Constants.CODE_ZNACHENIE_LANG_BG))) {
				pBG =this.publListHM.get(Long.valueOf(Constants.CODE_ZNACHENIE_LANG_BG));
				for (Long item: lanNo) {
					this.publListHM.get(item).setAnnotation(pBG.getAnnotation());
					this.publListHM.get(item).setFullText(pBG.getFullText());
					this.publListHM.get(item).setLang(item);
					this.publListHM.get(item).setOtherInfo(pBG.getOtherInfo());
					this.publListHM.get(item).setTitle(pBG.getTitle());
					this.publListHM.get(item).setUrlPub(pBG.getUrlPub());
				}
			}
		}
		
		// Files langs
		if (null!=this.filesList && this.filesList.size() > 0) {
			for (int i = 0; i < this.filesList.size(); i++) {
				if (null!=this.filesList.get(i).getLang())
					this.filesListHM.get(this.filesList.get(i).getLang()).add(this.filesList.get(i));
			}
		}

	}*/
	
	public void refreshImagePub() {
		if(null!=this.publication.getImagePub()){
			InputStream imageStream = new ByteArrayInputStream(this.publication.getImagePub());
			//this.imageCont = new DefaultStreamedContent(imageStream, "image/jpeg");
			this.imageCont = DefaultStreamedContent.builder().contentType("image/jpeg").name("").stream(() -> imageStream).build();
		}
	}

	public List<Object[]> getImages() {
		return images;
	}

	public void setImages(List<Object[]> images) {
		this.images = images;
	}

	/*public HashMap<Long, List<Files>> getFilesListHM() {
		return filesListHM;
	}

	public void setFilesListHM(HashMap<Long, List<Files>> filesListHM) {
		this.filesListHM = filesListHM;
	}*/

	public String getParamsVideo() throws UnsupportedEncodingException {
		if(publication!=null) {
			String pr =publication.getId()+";"+getCurrentLang();
			return new String(Base64.getEncoder().encodeToString(pr.getBytes()));
		}
		return "";
	}

	public String getNavi() {
		return navi;
	}

	public void setNavi(String navi) {
		this.navi = navi;
	}

	public String getClassL() {
		return classL;
	}

	public void setClassL(String classL) {
		this.classL = classL;
	}

	public String getClassR() {
		return classR;
	}

	public void setClassR(String classR) {
		this.classR = classR;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	
	/*
	 * public void controlMainExt() {
	 * 
	 * 
	 * 
	 * 
	 * }
	 */
}
