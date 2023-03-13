package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.TabChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.navigation.Navigation;
import com.ib.indexui.navigation.NavigationData;
import com.ib.indexui.navigation.NavigationDataHolder;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.ClassificationAttributes;
import com.ib.nsiclassif.db.dto.ClassificationLang;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.search.CorespTablesSearch;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.ObjectInUseException;

/**
 * Въвеждане и актуализация на статистически класификации
 * 
 * @author s.arnaudova
 */

@Named("classifEdit")
@ViewScoped
public class ClassificationEditBean extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7762786621346747534L;
	static final Logger LOGGER = LoggerFactory.getLogger(ClassificationEditBean.class);

	private static final String ID_OBJ = "idObj";
	public static final String FORM = "classificationEditForm:tabsClassif";
	private static final String SUCCESSDELETEMSG = "general.successDeleteMsg";

	private transient ClassificationDAO dao;
	private Classification classif;
	private ClassificationLang classifLang;

	private Integer lang;
	private Integer classifId;

	private List<Object[]> versionsList;
	private Version version;

	private List<SelectItem> positionsClassif;
	private List<Integer> selectedAttr;
	
	private CorespTablesSearch corespTabSearch;
	private LazyDataModelSQL2Array corespTablesList;
	private CorespTable reversiveCorrTable;

	@PostConstruct
	public void initData() {

		this.dao = new ClassificationDAO(getUserData());
		this.classifLang = new ClassificationLang();
		this.selectedAttr = new ArrayList<>();
		this.lang = getCurrentLang(); //NSIConstants.CODE_DEFAULT_LANG;

		try {

			if (JSFUtils.getRequestParameter(ID_OBJ) != null && !"".equals(JSFUtils.getRequestParameter(ID_OBJ))) {
				classifId = Integer.valueOf(JSFUtils.getRequestParameter(ID_OBJ));
		
				if (classifId != null) {
					loadClassif(classifId);


					this.selectedAttr = this.classif.getAttributes().stream()
							.map(ClassificationAttributes::getCodeAttrib).collect(Collectors.toList());
				}

			} else {

				this.classif = new Classification();
				this.classifLang.setClassif(this.classif);
				this.classifLang.setLang(this.lang);
			}

			loadAttributes();

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на данни за класификация!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}

	}

	/***
	 * Зарежда значенията на системна класификация
	 * NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES премахва значенията "Код",
	 * "Номер на ниво" и "Официално наименование" тези позиции трябва винаги да са
	 * избрани и да не се ънчекват
	 */
	private void loadAttributes() {
		try {

			this.positionsClassif = createItemsList(false, NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, new Date(),
				false);

			for (Iterator<SelectItem> iterator = positionsClassif.iterator(); iterator.hasNext();) {

				SelectItem currentItem = iterator.next();		

				if (currentItem.getValue().equals(1) || currentItem.getValue().equals(6) || currentItem.getValue().equals(11)) {
					
					currentItem.setDisabled(true);
				}
			}

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на атрибути!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	private void loadClassif(Integer id) {
		try {

			JPA.getUtil().runWithClose(() -> this.classif = this.dao.findById(id));

			if (this.classif != null) {
				getDataByLang();
			}

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на данни за класификация!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void changeLang() {
		
		getDataByLang();
		
		//da prezaredi versii 
		versionsList = null;
		// da prezaredi korespondirashti tablici
		actionSearchCorrespTables();
		
		UserData ud = (UserData) getUserData();
		
		ud.setCurrentLang(lang.intValue()); //TODO da se smeni teku]iqt ezik
	}
	
	public void getDataByLang() {

		if (this.classifLang != null && this.classifLang.getLang() != null) {
			classif.getLangMap().put(classifLang.getLang(), classifLang);
		}

		try {

			if (this.classif.getLangMap().containsKey(lang)) {
				this.classifLang = classif.getLangMap().get(lang);
			} else {

				ClassificationLang langTmp = new ClassificationLang();
				langTmp = this.classif.getLangMap().get(NSIConstants.CODE_DEFAULT_LANG);

				if (langTmp != null) {
					this.classifLang = langTmp.clone();
					this.classifLang.setId(null);
					this.classifLang.setClassif(classif);
					this.classifLang.setLang(lang);
				} else {
					this.classifLang = new ClassificationLang();
					this.classifLang.setLang(lang);
					this.classifLang.setClassif(classif);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при промяна на език!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}

	public void loadVersions() {
		try {

			JPA.getUtil().runWithClose(
					() -> this.versionsList = new VersionDAO(getUserData()).getClassifVersions(classifId, lang));

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на версии към статистическа класификация! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void actionSearchCorrespTables() {		

		this.corespTabSearch.setLang(this.lang);
		this.corespTabSearch.setClassificationId(this.classif.getId()); 
		this.corespTabSearch.buildQuery();
		this.corespTablesList = new LazyDataModelSQL2Array(this.corespTabSearch, null);		
	}

	public void actionSave() {
		try {
			if (checkData()) {
				classif.getLangMap().put(classifLang.getLang(), classifLang);

				if (this.classif.getAttributes() == null) {
					this.classif.setAttributes(new ArrayList<>());
				} else {
					this.classif.getAttributes().clear(); // зачиствам за да не се записват повторения
				}

				// добавям задължителните
				if (!this.selectedAttr.contains(1)) {
					this.selectedAttr.add(1);
				}
				if (!this.selectedAttr.contains(6)) {
					this.selectedAttr.add(6);
				}
				if (!this.selectedAttr.contains(11)) {
					this.selectedAttr.add(11);
				}

				for (Iterator<Integer> iterator = selectedAttr.iterator(); iterator.hasNext();) {

					ClassificationAttributes tempAttr = new ClassificationAttributes();
					Integer currentAttr = iterator.next();
					tempAttr.setClassif(this.classif);
					tempAttr.setCodeAttrib(currentAttr);
					
					this.classif.getAttributes().add(tempAttr);
					
				}

				/**@author s.arnaudova
				 * 
				 * по молба на КК добавям датата в края на идентификатора и наименованието (САМО АКО е нов запис)
				 * */
				if (this.classif.getId() == null) {
					SimpleDateFormat dtf = new SimpleDateFormat("dd.MM.yyyy");
					
					String tmpIdent = this.classifLang.getIdent() + "(" + dtf.format(new Date()) + ")";
					this.classifLang.setIdent(tmpIdent);
					
					String tmpName= this.classifLang.getNameClassif() + "(" + dtf.format(new Date()) + ")";
					this.classifLang.setNameClassif(tmpName);
				}
				

				JPA.getUtil().runInTransaction(() -> this.classif = this.dao.save(this.classif));

				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
						getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
				
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при запис!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void actionPublicate() {
		 
		if (classif.getPublicated()) {
			actionSave();
		}else {
			String path="";
			new ClassificationDAO(getUserData()).deleteExportedFiles(path);
			
			actionSave();
		}
	}

	private boolean checkData() {
		boolean flag = true;

		if (this.classifLang.getIdent().isEmpty()) {
			JSFUtils.addMessage(FORM + ":ident", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages,
					MSGPLSINS, getMessageResourceString(LABELS, "classificEdit.ident")));
			flag = false;
		}

		if (this.classifLang.getNameClassif().isEmpty()) {
			JSFUtils.addMessage(FORM + ":name", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages,
					MSGPLSINS, getMessageResourceString(LABELS, "classificEdit.name")));
			flag = false;
		}

		if (this.classif.getClassUnit() == null) {
			JSFUtils.addMessage(FORM + ":unit", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages,
					MSGPLSINS, getMessageResourceString(LABELS, "classificEdit.unit")));
			flag = false;
		}

		if (this.classif.getClassType() == null) {
			JSFUtils.addMessage(FORM + ":type", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages,
					MSGPLSINS, getMessageResourceString(LABELS, "classificEdit.type")));
			flag = false;
		}
		return flag;
	}

	public void onTabChange(TabChangeEvent<?> event) {
		if (event != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("onTabChange Active Tab: {}", event.getTab().getId());
			}

			String activeTab = event.getTab().getId();

			if (activeTab.equals("tabClassifVersions")) {

				if (this.versionsList == null) {
					loadVersions();
				}

			} else if (activeTab.equals("tabCorrespTables")) {

				this.corespTabSearch = new CorespTablesSearch();
				actionSearchCorrespTables();				
			}

		}

	}

	public String redirectToVersions(Integer idClassif) {

		if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idVersion"))) {
			Integer idObj = Integer.valueOf(JSFUtils.getRequestParameter("idObj"));

			return "versionEdit.jsf?faces-redirect=true&idClassif=" + idClassif + "&idObj=" + idObj;
		} else {
			return "versionEdit.jsf?faces-redirect=true&idClassif=" + idClassif;
		}
	}

	public String redirectToCorespTable(Integer idObj) {
		
		if (idObj != null) {			
			return "corespTable.jsf?faces-redirect=true&idClassif=" + classif.getId() + "&idObj=" + idObj;
		
		} else {
			return "corespTable.jsf?faces-redirect=true&idClassif=" + classif.getId();
		}		
	}
	
	public String redirectToCopyVersion() {		
		
		return "versionCopy.jsf?faces-redirect=true&idClassif=" + classif.getId();		
	}
	
	public String actionCreateReverseTable(Integer idCorrTab) {
		
		try {
			
			if (idCorrTab != null) {
				
				this.reversiveCorrTable = new CorespTable();				
				
				JPA.getUtil().runInTransaction(() -> this.reversiveCorrTable =  new CorespTableDAO(getUserData()).createReverseTable(idCorrTab));
				
				return "corespTable.jsf?faces-redirect=true&idClassif=" + classif.getId() + "&idObj=" + this.reversiveCorrTable.getId();					
			}			
			
		} catch (BaseException e) {
			LOGGER.error("Грешка при създаване на реверсивна таблица! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}	
		
		return "";		
	}
	
	public void actionDeleteCorrTable(Integer idCorrTab) {	
		
		try {

			if (idCorrTab != null) {

				JPA.getUtil().runInTransaction(() -> new CorespTableDAO(getUserData()).deleteById(idCorrTab));
			}
			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UI_beanMessages, SUCCESSDELETEMSG));	
			actionSearchCorrespTables();		

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на кореспондентска таблица! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}		
	}
	
	public void actionDeleteVersion(Integer idVersion) {
		try {

			if (idVersion != null) {

				JPA.getUtil().runInTransaction(() -> this.version = new VersionDAO(getUserData()).findById(idVersion));

				if (this.version != null) {
					
					JPA.getUtil().runInTransaction(() -> new VersionDAO(getUserData()).delete(version));

					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UI_beanMessages, SUCCESSDELETEMSG));
					
					loadVersions();
				}
			}

		} catch (ObjectInUseException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на версия! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	public void actionDeleteClassification() {
		try {
			
			JPA.getUtil().runInTransaction(() ->  this.dao.delete(classif));

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, "general.successDeleteMsg"));
			
			this.classif = new Classification();
			
			Navigation navHolder = new Navigation();
			int i = navHolder.getNavPath().size();
			
			if (i > 1) {
				NavigationDataHolder holder = (NavigationDataHolder) JSFUtils.getManagedBean("navigationSessionDataHolder");
				Stack<NavigationData> stackPath = holder.getPageList();
				NavigationData nd = stackPath.get(i - 2);
				if (nd != null) {
					Map<String, Object> mapV = nd.getViewMap();
					ClassificationBean classifsList = (ClassificationBean) mapV.get("classifsList");
					
					if (classifsList != null) {
						classifsList.actionSearchClassifs();
					}
				}
				
				navHolder.goBack(); 
			}

			
		} catch (ObjectInUseException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на класификация! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	public ClassificationDAO getDao() {
		return dao;
	}

	public void setDao(ClassificationDAO dao) {
		this.dao = dao;
	}

	public Classification getClassif() {
		return classif;
	}

	public void setClassif(Classification classif) {
		this.classif = classif;
	}

	public ClassificationLang getClassifLang() {
		return classifLang;
	}

	public void setClassifLang(ClassificationLang classifLang) {
		this.classifLang = classifLang;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getClassifId() {
		return classifId;
	}

	public void setClassifId(Integer classifId) {
		this.classifId = classifId;
	}

	public List<Object[]> getVersionsList() {
		return versionsList;
	}

	public void setVersionsList(List<Object[]> versionsList) {
		this.versionsList = versionsList;
	}

	public List<Integer> getSelectedAttr() {
		
		if(selectedAttr!=null) {
			
			if (!selectedAttr.contains(1)) {
				selectedAttr.add(1);
			}
			if (!selectedAttr.contains(6)) {
				selectedAttr.add(6);
			}
			if (!selectedAttr.contains(11)) {
				selectedAttr.add(11);
			}
		}
		
		return selectedAttr;
	}

	public void setSelectedAttr(List<Integer> selectedAttr) {
		this.selectedAttr = selectedAttr;
	}

	public List<SelectItem> getPositionsClassif() {
		return positionsClassif;
	}

	public void setPositionsClassif(List<SelectItem> positionsClassif) {
		this.positionsClassif = positionsClassif;
	}

	public CorespTablesSearch getCorespTabSearch() {
		return corespTabSearch;
	}

	public void setCorespTabSearch(CorespTablesSearch corespTabSearch) {
		this.corespTabSearch = corespTabSearch;
	}

	public LazyDataModelSQL2Array getCorespTablesList() {
		return corespTablesList;
	}

	public void setCorespTablesList(LazyDataModelSQL2Array corespTablesList) {
		this.corespTablesList = corespTablesList;
	}

	public CorespTable getReversiveCorrTable() {
		return reversiveCorrTable;
	}

	public void setReversiveCorrTable(CorespTable reversiveCorrTable) {
		this.reversiveCorrTable = reversiveCorrTable;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

}
