package com.ib.nsiclassif.beansSite;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.dataview.DataView; 
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.indexui.system.IndexUIbean;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.system.db.SelectMetadata;
import com.ib.system.utils.DateUtils;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.PublicationDAO;
import com.ib.nsiclassif.db.dao.PublicationLangDAO;
import com.ib.nsiclassif.system.NSIConstants;

@Named("publExtList")
@ViewScoped
public class PublExtList extends IndexUIbean{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7693307494393667314L;
	/**
	 * Основен javaBean клас за търсене на публикации по зададени критерии за търсене и 
	 * за обръщане към javaBean клас за въвеждане/актуализация
	 */
	
	
	static final Logger LOGGER = LoggerFactory.getLogger(PublExtList.class);
	private Integer idUser=null;
	private String sectionText=null;
	private Integer codeSection=null;
	private Date dateFrom=null;
	private Date dateTo=null;
	private String annotation=null;
	private Integer period=null;
	private LazyDataModelSQL2Array pubListT = null;
//	private Publication selectedPubl=null;
//	private Long param=null;
	private String titleF=null;
	private List<Object[]> pubLangList = new ArrayList<Object[]>();
	private SelectMetadata smd = null;
	private String sortCol = "";
	private boolean hasSearched = false;
	private int gridcolumn =1;
	
//	private static final String LANG = "lang";
	private Integer lang;
	private String locale;
	private int brRows;

//	private MenuModel model = new DefaultMenuModel();
	/*
	 * private static final String LANG = "lang"; private Integer lang;
	 */
	
	
	/**
	 * Инициира/сетва първоначалните стойности на атрибутите на филтъра за търсене. Чете предадените параметри от други екрани
	 */
	@PostConstruct
	public void initData(){
				
		this.locale = JSFUtils.getRequestParameter("locale");
			
		if (null != this.locale && this.locale.equalsIgnoreCase("en")) {
			this.lang= 2;
		}else {
			this.setLang(NSIConstants.CODE_DEFAULT_LANG);
		}
				
		String sect =JSFUtils.getRequestParameter("sect");
		if (sect != null && !sect.trim().isEmpty()){
			this.setCodeSection(Integer.valueOf(sect));
		}else {
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "general.invalidParameter"));
			return;
		}
		
		try{	

			this.idUser=getUserData().getUserId();
			
			@SuppressWarnings("unchecked")
			Map<String, Object> params  = (Map<String, Object>) getSessionScopeValue("publExtListFindAttr");	
			if(params != null && !params.isEmpty()){
				if (null!=params.get("dateFrom")) 
					this.dateFrom=(Date)params.get("dateFrom");

				if (null!=params.get("dateTo"))
					this.dateTo=(Date)params.get("dateTo");
				
				if (null!=params.get("titleF")) 
					this.titleF=(String)params.get("titleF");
			
				
				if (null!=getSessionScopeValue("period"))
					this.period =  (Integer) getSessionScopeValue("period");
		
				actionFind();
				
				if(getSessionScopeValue("publExtListPage") != null) {
					
					DataView d = (DataView) FacesContext.getCurrentInstance().getViewRoot().findComponent("formExtList:tableGrid");
					
					if(d != null) { 					
						int page = (int) getSessionScopeValue("publExtListPage");
						d.setFirst(page); 
					}
				}
	
			}else {
				actionFind();
			}
		
			actionClear();
//			createDynamicMenu();

		
		}catch (Exception e) {
			LOGGER.error("Грешка при работа със системата!!!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString("beanMessages", "general.exception"), e.getMessage());
		} /*
			 * finally { JPA.getUtil().closeConnection(); }
			 */
	
	
	}
	
	
	/**Изтрива стойностите на филтъра за търсене на публикациите
	 * 
	 */
	public void actionClear(){
		this.sectionText=null;
		this.dateFrom=null;
		this.dateTo=null;
		this.period=null;
//		this.setSelectedPubl(new Publication());
//		this.pubListT = null;
		this.titleF=null;
		actionFind();
		
		
		//махаме запазените параметри от сесията
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		session.removeAttribute("publExtListPage");
		session.removeAttribute("publExtListFindAttr");
		session.removeAttribute("period");
				
		DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("formPublList:tablePubl");		
		if (null!=d)
		d.setFirst(0); 
		
	// Dynamic MenuModel	
//		model = new DefaultMenuModel();
		
	}
	
	

	public Integer getIdUser() {
		return idUser;
	}

	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
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
	}

	

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}
	
	/**Проверява дата от и дата до на публикациите
	 * @param nom
	 */
	public boolean checkDates(int nom){
		boolean rez=true;
		this.setPeriod(null);
		if (this.getDateFrom() != null && this.getDateTo() != null) {
			if(this.getDateFrom().compareTo(this.getDateTo()) > 0) {
				rez=false;
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString("beanMessages","section.dateFromLessDateTo"));
			}
		} 
		return rez;
	}
	
	

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	public Date getCurrentDate() {
		return new Date();
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}
	
	public void changePeriod () {
    	if (this.period != null) {
			Date[] di;
			di = DateUtils.calculatePeriod(this.period);
			this.setDateFrom(di[0]);
			this.setDateTo(di[1]);
		} else {
			this.setDateFrom(null);
			this.setDateTo(null);
		}
		return ;
    }
	
	
	
	
	/** Метод за търсене в БД на публикации по зададените критерии/филтър 
	 * 
	 */
	public String actionFind(){
		
		if(titleF!=null && !titleF.trim().isEmpty() && titleF.length()<3) {
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString("beanMessages","section.searchLenght"));
			return null;
		}
		
		this.pubListT = null;

		this.smd = new SelectMetadata();
		
		PublicationDAO publDao = new PublicationDAO(getUserData());
		
		try {
			JPA.getUtil().runWithClose(() -> { this.smd = publDao.findPublExtFilter(this.dateFrom, this.dateTo, this.codeSection, this.titleF, this.lang, new Date());
			this.sortCol = "A4 DESC";
			this.pubListT = publDao.newLazyDataModel(this.smd, this.sortCol);
			});
			
			this.brRows = this.pubListT.getRowCount();
				
			Map<String, Object> params = new HashMap<String, Object>();
			/*if (null != this.getCodeSection())
				params.put("codeSection", this.codeSection);	*/
			
			if (null != this.getDateFrom())
				params.put("dateFrom", this.dateFrom);	
			
			if (null != this.getDateTo())
				params.put("dateTo", this.dateTo);	
			
			if (null != this.getTitleF())
				params.put("titleF", this.titleF);	


			addSessionScopeAttribute("publExtListFindAttr", params);
			
			if (null!=this.getPeriod())
				addSessionScopeAttribute("period", this.period);
			
			
			
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане секцията! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		}
		
		return null;
		/*if(this.pubListT.getRowCount()>1)
			this.hasSearched = false;*/
		
	}

	public LazyDataModelSQL2Array getPubListT() {
		return pubListT;
	}

	public void setPubListT(LazyDataModelSQL2Array pubListT) {
		this.pubListT = pubListT;
	}

	public void prepareGo(){
		this.pubListT.getResult().clear();
	}
	
	
	
	/** Метод за скалиране/редуциране на размера на изображенията, 
	 * показвани в таблицата на намерените публикации по зададен филтър на търсене в БД 
	 * @param ba
	 * @param proc
	 * @return
	 */
	public StreamedContent createImageCont(byte[] ba, int proc){
		StreamedContent imagCont=null;

		if (null!=ba){
				
	        try {
	        	
				ByteArrayInputStream inS = new ByteArrayInputStream(ba);
	        	BufferedImage img = ImageIO.read(inS);
	        	int h=img.getHeight();
	        	int w=img.getWidth();
	        	h=(img.getHeight()*proc)/100;
	        	w=(img.getWidth()*proc)/100;
	            
	            Image scaledImage = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
	            BufferedImage imageBuff = new BufferedImage(w, h, img.getType());
	            imageBuff.getGraphics().drawImage(scaledImage, 0, 0, null);

	            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	            ImageIO.write(imageBuff, "jpeg", buffer);
	            buffer.flush();
	            imageBuff.flush();
	            byte[] resizeBa=buffer.toByteArray();
	            buffer.close();
	        	
	            InputStream imageStream = new ByteArrayInputStream(resizeBa);
	            imagCont = DefaultStreamedContent.builder().contentType("image/jpeg").name(null).stream(() -> imageStream).build(); 
//	            imagCont = new DefaultStreamedContent(imageStream, "image/jpeg");
	            imageStream.close();
	            
			} catch (Exception e) {
				LOGGER.error(e.getMessage(),e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при показване на изображение!", e.getMessage());
			}
		}
		return imagCont;
	}

	/*public Publication getSelectedPubl() {
		return selectedPubl;
	}



	public void setSelectedPubl(Publication selectedPubl) {
		this.selectedPubl = selectedPubl;
	}*/
	

	public String getTitleF() {
		return titleF;
	}



	public void setTitleF(String titleF) {
		this.titleF = titleF;
	}
	
	//Извлича Теми/Рубрики по код секция от класификацията
	public void findPublBySection(Integer codeSect){

		try {
			
			JPA.getUtil().runWithClose(() -> this.pubLangList = new PublicationLangDAO(getUserData()).findPublLangBySect(codeSect, new Date(), this.lang));
			
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане секцията! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		}
	}



	public List<Object[]> getPubLangList() {
		return pubLangList;
	}



	public void setPubLangList(List<Object[]> pubLangList) {
		this.pubLangList = pubLangList;
	}
	
	public void sortReport() {
		if(null!=getSortCol()) {
			PublicationDAO publDao = new PublicationDAO(getUserData());
			// Тука пресортиране на изхода	
			try {
				
				JPA.getUtil().runWithClose(() -> this.pubListT = publDao.newLazyDataModel(getSmd(), getSortCol()));
				this.pubListT.getRowCount();
			
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане секцията! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
			}
			
		}
	}



	public SelectMetadata getSmd() {
		return smd;
	}



	public void setSmd(SelectMetadata smd) {
		this.smd = smd;
	}



	public String getSortCol() {
		return sortCol;
	}



	public void setSortCol(String sortCol) {
		this.sortCol = sortCol;
	}



	public int getGridcolumn() {
		return gridcolumn;
	}



	public void setGridcolumn(int gridcolumn) {
		this.gridcolumn = gridcolumn;
	}
	
	public void actionGridTable() {
		gridcolumn=4;
	}
	
	public void actionListTable() {
		gridcolumn=1;
	}



	public boolean isHasSearched() {
		return hasSearched;
	}

	public void setHasSearched(boolean hasSearched) {
		this.hasSearched = hasSearched;
	}
	
	
	public void changePage() {
		
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);		
		
		session.removeAttribute("publExtListPage");
		
		DataView d = (DataView) FacesContext.getCurrentInstance().getViewRoot().findComponent("formExtList:tableGrid");
		
		if(d != null) { 
			addSessionScopeAttribute("publExtListPage", d.getFirst());		
		}

	}
	
	/**
	 * Method returns value added to session scope and if it exists it will be removed and returned
	 * @param key - key of the value
	 * @return - value
	 */
	public final Object getSessionScopeValue(String key){
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		if(session == null){
			return null;
		}else{
			Object value = session.getAttribute(key);
			//session.removeAttribute(key); - ne bi trqbvalo da se maha
			return value;
		}
	}
	
	
	/**
	 * Method adds new key value pair into Session scope of JSF 2 app
	 * @param key - key for value to be associated with 
	 * @param value - value to put in.
	 */
	public final void addSessionScopeAttribute(String key, Object value){
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		if(session != null){
			session.setAttribute(key, value);
		}
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


	public int getBrRows() {
		return brRows;
	}


	public void setBrRows(int brRows) {
		this.brRows = brRows;
	}


	/*
	 * public Integer getLang() { return lang; }
	 * 
	 * 
	 * public void setLang(Integer lang) { this.lang = lang; }
	 */


}
