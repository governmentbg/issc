package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.SelectEvent;
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
import com.ib.nsiclassif.db.dao.RelationDAO;
import com.ib.nsiclassif.db.dao.SchemeDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.CorespTableLang;
import com.ib.nsiclassif.db.dto.Relation;
import com.ib.nsiclassif.search.RelationsSearch;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.SearchUtils;

@Named
@ViewScoped
public class CorespTableBean extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3853501323161671907L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CorespTableBean.class);

	private transient CorespTableDAO corespDao;
	private VersionDAO vDao;

	private CorespTable corespTable = new CorespTable();
	private CorespTableLang corespTableLang = new CorespTableLang();
	private Date decodeDate = new Date();

	private List<Object[]> versionsList;
	private List<SelectItem> versionsByGroup;
	private Integer idClassif;
	private Integer idVersion;
	private Integer lang;
	private static final String ID_CLASSIF = "idClassif";
	private static final String ID_VERSION = "idVersion";
	
	/******** РЕЛАЦИИ СВИЛЕН *********/
	private LazyDataModelSQL2Array relationsList;
	
	private RelationsSearch relationSearch=new RelationsSearch();
	
	private List<Relation> currentPositionsList=new ArrayList<Relation>(); 
	private Relation rel;
	private Object[] rowSelectedRel=null;

	private String sourceCodeText=null;
	private String targetCodeText=null;
	private boolean sourceCodeFound=true;
	private boolean targetCodeFound=true;
	private Integer typeRelation;//141 класификаци
	
	private boolean oneToOne=true;
	private boolean oneToZero=false;
	private boolean zeroToOne=false;
	
	
	/******** КРАЙ РЕЛАЦИИ **********/
	

	@PostConstruct
	void initData() {

		LOGGER.debug("PostConstruct!!!");

		this.lang = getCurrentLang();
		this.corespDao = new CorespTableDAO(getUserData());
		this.vDao = new VersionDAO(getUserData());

		if (JSFUtils.getRequestParameter(ID_CLASSIF) != null && !"".equals(JSFUtils.getRequestParameter(ID_CLASSIF))) {
			this.idClassif = Integer.valueOf(JSFUtils.getRequestParameter(ID_CLASSIF));
			loadVersionsList();
		}
		
		if (JSFUtils.getRequestParameter(ID_VERSION) != null && !"".equals(JSFUtils.getRequestParameter(ID_VERSION))) {
			this.idVersion = Integer.valueOf(JSFUtils.getRequestParameter(ID_VERSION));			
		}

		String idObj = JSFUtils.getRequestParameter("idObj");
		
		if (SearchUtils.isEmpty(idObj)) {
			actionNew();
			this.corespTable.setIdVersSource(this.idVersion);
			this.corespTable.setIdVersTarget(this.idVersion); 
		
		} else {
			
			try {
				
				JPA.getUtil().runWithClose(() -> this.corespTable = this.corespDao.findById(Integer.valueOf(idObj)));
				
				if (this.corespTable.getLangMap().containsKey(this.lang)) {
					this.corespTableLang = corespTable.getLangMap().get(this.lang);
				}

			} catch (BaseException e) {
				LOGGER.error("Грешка при търсене на кореспондираща таблица! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			}
		}
		/******** РЕЛАЦИИ СВИЛЕН *********/
		rel=new Relation();
		
		
		if (corespTable.getId()!=null) {
			rel.setIdTable(corespTable.getId());	
		}
		loadRelations();
		/******** КРАЙ РЕЛАЦИИ **********/
	}

	private void loadVersionsList() {
		
		try {
		
			JPA.getUtil().runWithClose(() -> this.versionsList = this.vDao.getAllVesrionsList(this.lang));
			
			this.versionsByGroup = new ArrayList<>();			
			List <SelectItem>itemInGroup = new ArrayList<SelectItem>();
			
			if(this.versionsList != null && this.versionsList.size() > 0) {
			
				String identClassif = SearchUtils.asString(this.versionsList.get(0)[2]); //инициализиране				
				
				for (Object[] item : this.versionsList) {
						
					Integer verId = SearchUtils.asInteger(item[0]);
					String verIdent = SearchUtils.asString(item[1]);
					String classifIdent = SearchUtils.asString(item[2]);
					
					if(identClassif.equals(classifIdent)) {
						
						if (verIdent == null) {
							verIdent = "Няма превод на БГ"; // Това е, защото има версии, които нямат нищо на дефолтния език и излизат празни редове в падащия списък
						}
						
						itemInGroup.add(new SelectItem(verId, verIdent));
					
					} else {
						// създаване групата с елементите
						SelectItemGroup itemGroup = new SelectItemGroup(identClassif); 
						itemGroup.setSelectItems(itemInGroup.toArray(new SelectItem[itemInGroup.size()]));
						this.versionsByGroup.add(itemGroup);
						
						//започване нова група
						identClassif = classifIdent;
						itemInGroup = new ArrayList<SelectItem>();
						
						if (verIdent == null) {
							verIdent = "Няма превод на БГ"; 
						}
						
						itemInGroup.add(new SelectItem(verId, verIdent)); // добавяне на първия елемент в новата група 		
					}					
				}
				
				//когато стигне до края трябва да се добави последната група с елементи
				SelectItemGroup itemGroup = new SelectItemGroup(identClassif); 
				itemGroup.setSelectItems(itemInGroup.toArray(new SelectItem[itemInGroup.size()]));
				this.versionsByGroup.add(itemGroup);			
			}

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на списък с версии! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}

	public void actionNew() {
		
		this.corespTable = new CorespTable();
		this.corespTableLang = new CorespTableLang();
		this.corespTableLang.setLang(lang);
		this.corespTable.setRelationsCount(0);
		this.corespTable.setSourcePosCount(0);
		this.corespTable.setTargetPosCount(0);
		this.corespTableLang.setCorespTable(corespTable);
	}

	public boolean checkData() {

		boolean save = false;

		if (this.corespTable.getIdVersSource() == null) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:idVersSource", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "corespTable.versionSource")));
			save = true;
		}

		if (this.corespTable.getIdVersTarget() == null) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:idVersInh", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "corespTable.versionInh")));
			save = true;
		}
		
		if (this.corespTable.getIdVersSource() != null && this.corespTable.getIdVersTarget() != null
				&& this.corespTable.getIdVersSource().equals(this.corespTable.getIdVersTarget())) { 
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:idVersInh", FacesMessage.SEVERITY_ERROR, 
					getMessageResourceString(LABELS, "corespTable.sameVerSourceAndInh"));			
			save = true;
		}

		if (this.corespTableLang.getIdent() == null || "".equals(this.corespTableLang.getIdent().trim())) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:identifikator", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "classificEdit.ident")));
			save = true;
		}

		if (this.corespTableLang.getName() == null || "".equals(this.corespTableLang.getName().trim())) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:name", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "classificEdit.name")));
			save = true;
		}

		if (this.corespTable.getStatus() == null) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:status",
					FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "versionEdit.status")));
			save = true;
		}

		if (this.corespTable.getTableType() == null) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:typeTable",
					FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "corespTable.typeTable")));
			save = true;
		}

		if (this.corespTable.getRelationType() == null) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:typeConn",
					FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "corespTable.typeConn")));
			save = true;
		}

		if (this.corespTable.getSourcePosCount() == null) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:numberPossSource", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "corespTable.numPossitionsSource")));
			save = true;
		}

		if (this.corespTable.getTargetPosCount() == null) {
			JSFUtils.addMessage("corespTableForm:tabViewCorespTable:numberPossTarget", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "corespTable.numberPossTarget")));
			save = true;
		}

		return save;
	}

	public void getDataByLang() {

		loadVersionsList();
		
		if (this.corespTableLang != null && this.corespTableLang.getLang() != null && this.corespTable != null) {
			this.corespTable.getLangMap().put(this.corespTableLang.getLang(), this.corespTableLang);
		}

		try {

			if (this.corespTable.getLangMap().containsKey(this.lang)) {
				this.corespTableLang = this.corespTable.getLangMap().get(this.lang);
			
			} else {

				CorespTableLang langTmp = new CorespTableLang();
				langTmp = this.corespTable.getLangMap().get(NSIConstants.CODE_DEFAULT_LANG);

				if (langTmp != null) {
					
					this.corespTableLang = langTmp.clone();
					this.corespTableLang.setId(null);
					this.corespTableLang.setCorespTable(this.corespTable);
					this.corespTableLang.setLang(this.lang);
				
				} else {
				
					this.corespTableLang = new CorespTableLang();
					this.setLang(this.lang);
					this.corespTableLang.setCorespTable(this.corespTable);
				}
			}
		
		} catch (Exception e) {
			LOGGER.error("Грешка при промяна на език!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void actionChangeLang() {
		
		getDataByLang();
		
		UserData ud = (UserData) getUserData();
		
		ud.setCurrentLang(lang.intValue());
	}

	public void actionSave() {

		if (checkData()) {
			return;
		}

		try {
			
			this.corespTable.getLangMap().put(this.lang, this.corespTableLang);
			
			JPA.getUtil().runInTransaction(() -> this.corespTable = this.corespDao.save(this.corespTable));

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, "general.succesSaveMsg"));
			
			loadRelations();
			
		} catch (ObjectInUseException e) {
			LOGGER.error("ObjectInUseException-> {}", e.getMessage()); 
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		
		} catch (Exception e) {
			LOGGER.error("Грешка при запис на кореспондираща таблица! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
	
	public void actionPublicate() {
		 
		if (corespTable.getPublicated()) {
			actionSave();
		}else {
			String path="";
			try {
				path = getSystemData().getSettingsValue("FilesSaveLocation")+"coresp/"+corespTable.getId();
			} catch (DbErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			new ClassificationDAO(getUserData()).deleteExportedFiles(path);
			actionSave();
		}
	}

	public void actionDelete() {

		try {

			JPA.getUtil().runInTransaction(() ->  this.corespDao.delete(corespTable));

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, "general.successDeleteMsg"));
			
			actionNew();
			
			Navigation navHolder = new Navigation();
			int i = navHolder.getNavPath().size();
			if (i > 1) {

				NavigationDataHolder holder = (NavigationDataHolder) JSFUtils.getManagedBean("navigationSessionDataHolder");
				Stack<NavigationData> stackPath = holder.getPageList();
				NavigationData nd = stackPath.get(i - 2);
				if (nd != null) {
					Map<String, Object> mapV = nd.getViewMap();
					ClassificationEditBean classif = (ClassificationEditBean) mapV.get("classifEdit");
					if (classif != null) {
						classif.actionSearchCorrespTables(); // да презареди списъка, за да се коригира броя резултати
					}
				}
				navHolder.goBack(); // връща към предходната страница - списъка на събитията
			}

		
		} catch (ObjectInUseException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на кореспондираща таблица! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}

	}
	
	/***************************** РЕЛАЦИИ СВИЛЕН *************************/
	public void loadRelations() {
		try {

			this.relationSearch.setCorespTableId(corespTable.getId()); 
			this.relationSearch.buildQuery();
			this.relationsList = new LazyDataModelSQL2Array(this.getRelationSearch(), null);

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на класификации! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void sourceCodeChange() {
		 
		try {
			if (rel.getSourceCode()!=null) {
				rel.setSourceCode(rel.getSourceCode().strip());
			}
			if (rel.getTargetCode()!=null) {
				rel.setTargetCode(rel.getTargetCode().strip());
			}
	
			sourceCodeText=null;
			sourceCodeFound=true;
			typeRelation=null;
			currentPositionsList.clear();
	
			decodeSourceCode();
			
			if ((rel.getSourceCode()!=null && !rel.getSourceCode().isEmpty() && sourceCodeFound) || (rel.getTargetCode()!=null && !rel.getTargetCode().isEmpty() && targetCodeFound)) {
				loadCurrentPositionsList();
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на версии към статистическа класификация! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	public void targetCodeChange() {
		
		try {
			if (rel.getSourceCode()!=null) {
				rel.setSourceCode(rel.getSourceCode().strip());
			}
			if (rel.getTargetCode()!=null) {
				rel.setTargetCode(rel.getTargetCode().strip());
			}
 
			 
			targetCodeText=null;
			targetCodeFound=true;
			typeRelation=null;
			currentPositionsList.clear();
			
			
			decodeTargetCode();
			
			
			if ((rel.getSourceCode()!=null && !rel.getSourceCode().isEmpty() && sourceCodeFound) || (rel.getTargetCode()!=null && !rel.getTargetCode().isEmpty() && targetCodeFound)) {
				loadCurrentPositionsList();
			} 
			 
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на версии към статистическа класификация! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	public void loadCurrentPositionsList() {
		
		try {
			
			this.currentPositionsList = new RelationDAO(getUserData()).getRelationsByCodeSourceTarget(rel.getIdTable(), rel.getSourceCode(), rel.getTargetCode());
			
			
				if (!currentPositionsList.isEmpty()) {
					if (currentPositionsList.size()==1) {
						if (currentPositionsList.get(0).getSourceCode()!=null && currentPositionsList.get(0).getSourceCode().isEmpty()) {
							currentPositionsList.get(0).setSourceCode(null);
						}
						if (currentPositionsList.get(0).getTargetCode()!=null && currentPositionsList.get(0).getTargetCode().isEmpty()) {
							currentPositionsList.get(0).setTargetCode(null);
						}
						if (currentPositionsList.get(0).getSourceCode()!=null && currentPositionsList.get(0).getTargetCode()!=null) {
							typeRelation=1;	
						}else {
							if (currentPositionsList.get(0).getSourceCode()==null && currentPositionsList.get(0).getTargetCode()!=null) {
								typeRelation=6;	
							}else {
								if (currentPositionsList.get(0).getSourceCode()!=null && currentPositionsList.get(0).getTargetCode()==null) {
									typeRelation=5;
								}
							}
						}
					}
					
					int codeSourceCount=1;
					int codeTargetCount=1;
					for (int i = 0; i < currentPositionsList.size(); i++) {
						if (currentPositionsList.get(i).getSourceCode()!=null && currentPositionsList.get(i).getSourceCode().isEmpty()) {
							currentPositionsList.get(i).setSourceCode(null);
						}
						if (currentPositionsList.get(i).getTargetCode()!=null && currentPositionsList.get(i).getTargetCode().isEmpty()) {
							currentPositionsList.get(i).setTargetCode(null);
						}
						if (rel.getSourceCode()!=null && currentPositionsList.get(i).getSourceCode()!=null && !currentPositionsList.get(i).getSourceCode().equals(rel.getSourceCode())) {
							codeSourceCount++;
						}
						if (rel.getTargetCode()!=null && currentPositionsList.get(i).getTargetCode()!=null && !currentPositionsList.get(i).getTargetCode().equals(rel.getTargetCode())) {
							codeTargetCount++;
						}
						
						if (((rel.getSourceCode()==null && currentPositionsList.get(i).getSourceCode()==null) || currentPositionsList.get(i).getSourceCode()!=null && rel.getSourceCode()!=null && currentPositionsList.get(i).getSourceCode().equals(rel.getSourceCode()))
							&&
							((rel.getTargetCode()==null && currentPositionsList.get(i).getTargetCode()==null) || currentPositionsList.get(i).getTargetCode()!=null && rel.getTargetCode()!=null && currentPositionsList.get(i).getTargetCode().equals(rel.getTargetCode()))){
							if (rel.getId()==null || currentPositionsList.get(i).getId()!=rel.getId()) {
								rel=currentPositionsList.get(i);
							}
						}
					}
					if (currentPositionsList.size()>1) {
						if (codeSourceCount>1 && codeTargetCount>1) {
							typeRelation=4;	
						}else {
							if (codeSourceCount>1) {
								typeRelation=3;	
							}else {
								if (codeTargetCount>1) {
									typeRelation=2;	
								}	
							}
						}
						
					}
				}
			
 
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на версии към статистическа класификация! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	public void actionSaveRelation() {
		if (validate()) {
			try {
				rel.setIdTable(corespTable.getId());
				JPA.getUtil().runInTransaction(() -> this.rel = new RelationDAO(getUserData()).save(this.rel));
				new RelationDAO(getUserData()).changeCorespTableBroiRelacii(corespTable);
				actionSave();
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
//						getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
				loadRelations();
				loadCurrentPositionsList();
			} catch (BaseException e) {
				LOGGER.error("Грешка при запис!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
		}
	}
	public void actionGenerateRelations() {
		
			try {
				
				JPA.getUtil().runInTransaction(() -> new RelationDAO(ActiveUser.DEFAULT).generateRelationsHist(corespTable.getId(), corespTable.getIdVersSource(), corespTable.getIdVersSource(), oneToOne, oneToZero, zeroToOne));
				new RelationDAO(getUserData()).changeCorespTableBroiRelacii(corespTable);
				actionSave();
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
//						getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
				loadRelations();
				loadCurrentPositionsList();
			} catch (BaseException e) {
				LOGGER.error("Грешка при запис!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
	}
	
	public boolean validate() {
		boolean save=true;
		 
		if ((rel.getSourceCode()==null || rel.getSourceCode().trim().equals("")) && (rel.getTargetCode()==null || rel.getTargetCode().trim().equals(""))) {
			JSFUtils.addMessage("corespTableForm:sourceCode", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages,
					MSGPLSINS, "код източник или код цел"));
			save = false;
		}
		
		
		if (!sourceCodeFound) {
			//zna4i ne sme go namerili za razkodirane ne zapisvame 
			JSFUtils.addMessage("corespTableForm:sourceCode", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "relations.noPositionFound"));
			save = false;
		}
		
		if (!targetCodeFound) {
			//zna4i ne sme go namerili za razkodirane ne zapisvame 
			JSFUtils.addMessage("corespTableForm:targetCode", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "relations.noPositionFound"));
			save = false;
		}
	 
		return save;
	}
	
	public void searchCodeChange() {
		loadRelations();
	}
	public void searchLocationChange() {
		if (relationSearch.getSearchCode()!=null && !relationSearch.getSearchCode().isEmpty()) {
			loadRelations();	
		}
	}
	
	public void rowSelectedChange(SelectEvent<Object[]> selectEvent) {
		try {
			if (selectEvent.getObject()[0]!=null) {
				rel=new RelationDAO(getUserData()).findById(Integer.valueOf(selectEvent.getObject()[0].toString()));
				//razkodirame
				decodeSourceCode();
				//razkodirame i tam se izvikvat tekushtite relacii za tova sa razli4ni. zada ne se vika 2 pati
				targetCodeChange();
			}
			
			    
		} catch (BaseException e) {			
			LOGGER.error("Грешка при актуализация на преписка в преписка! ", e);
			
		}
	}
	
	public void decodeSourceCode() throws DbErrorException {
		List<Object[]> rez;
		
		if (rel.getSourceCode()!=null && !rel.getSourceCode().isEmpty()) {
			rez= new SchemeDAO(getUserData()).decodePositionByVersionCode(corespTable.getIdVersSource(), rel.getSourceCode(), getCurrentLang());
					
			if (rez==null || rez.size()==0 || rez.get(0)==null) {
				sourceCodeFound=false;
				JSFUtils.addMessage("corespTableForm:sourceCode", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "relations.noPositionFound"));
			}else {
				if (rez.size()>1) {
					JSFUtils.addMessage("corespTableForm:sourceCode", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "relations.multyPosFound"));	
				}else {
					if (rez.get(0)[1]==null) {
						sourceCodeText="непреведно значение";
					}else {
						sourceCodeText=SearchUtils.asString(rez.get(0)[1]);	
					}
					
				}
			}
		}
	}
	public void decodeTargetCode() throws DbErrorException {
		List<Object[]> rez;
		 
		if (rel.getTargetCode()!=null && !rel.getTargetCode().isEmpty()) {
			rez= new SchemeDAO(getUserData()).decodePositionByVersionCode(corespTable.getIdVersTarget(), rel.getTargetCode(), getCurrentLang());
			
			if (rez==null || rez.size()==0 || rez.get(0)==null) {
				targetCodeFound=false;
				JSFUtils.addMessage("corespTableForm:targetCode", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "relations.noPositionFound"));
			}else {
				if (rez.size()>1) {
					JSFUtils.addMessage("corespTableForm:targetCode", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "relations.multyPosFound"));	
				}else {
					if (rez.get(0)[1]==null) {
						targetCodeText="непреведно значение";
					}else {
						targetCodeText=SearchUtils.asString(rez.get(0)[1]);	
					}
				}
			}
		}
	}
	
	public void actionNewRelation() {
		this.rel=new Relation();
		rel.setIdTable(corespTable.getId());	
		this.currentPositionsList.clear();
		this.sourceCodeText=null;
		this.targetCodeText=null;
		this.rowSelectedRel=null;
		this.typeRelation=null;
		
	}
	
	public void actionDeleteRelation(Integer idRel) {	
		
		try {

			if (idRel != null) {

				JPA.getUtil().runInTransaction(() -> new RelationDAO(getUserData()).deleteById(idRel));
				new RelationDAO(getUserData()).changeCorespTableBroiRelacii(corespTable);
				actionSave();
			}
			
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UI_beanMessages, "general.successDeleteMsg"));	
			actionNewRelation();		
			loadRelations();
		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на кореспондентска таблица! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}		
	}	
	/********************* КРАЙ РЕЛАЦИИ *****************************/
	
	/******************* EXPORTS ******************************/
	
	
	public void sendDownload(String filePathAndName) {
		ExternalContext  context= FacesContext.getCurrentInstance().getExternalContext();
		try {
			
			context.redirect(filePathAndName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
	
	






	
	/******************** END EXPORTS *************************/

	public Date getDecodeDate() {
		return decodeDate;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate != null ? new Date(decodeDate.getTime()) : null;
	}

	public CorespTable getCorespTable() {
		return corespTable;
	}

	public void setCorespTable(CorespTable corespTable) {
		this.corespTable = corespTable;
	}

	public List<Object[]> getVersionsList() {
		return versionsList;
	}

	public void setVersionsList(List<Object[]> versionsList) {
		this.versionsList = versionsList;
	}	
	
	public List<SelectItem> getVersionsByGroup() {
		return versionsByGroup;
	}

	public void setVersionsByGroup(List<SelectItem> versionsByGroup) {
		this.versionsByGroup = versionsByGroup;
	}

	public Integer getIdClassif() {
		return idClassif;
	}

	public void setIdClassif(Integer idClassif) {
		this.idClassif = idClassif;
	}

	public Integer getIdVersion() {
		return idVersion;
	}

	public void setIdVersion(Integer idVersion) {
		this.idVersion = idVersion;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public CorespTableLang getCorespTableLang() {
		return corespTableLang;
	}

	public void setCorespTableLang(CorespTableLang corespTableLang) {
		this.corespTableLang = corespTableLang;
	}
	
	public LazyDataModelSQL2Array getRelationsList() {
		return relationsList;
	}

	public void setRelationsList(LazyDataModelSQL2Array relationsList) {
		this.relationsList = relationsList;
	}

	public RelationsSearch getRelationSearch() {
		return relationSearch;
	}

	public void setRelationSearch(RelationsSearch relationSearch) {
		this.relationSearch = relationSearch;
	}


	public Relation getRel() {
		return rel;
	}

	public void setRel(Relation rel) {
		this.rel = rel;
	}

	public List<Relation> getCurrentPositionsList() {
		return currentPositionsList;
	}

	public void setCurrentPositionsList(List<Relation> currentPositionsList) {
		this.currentPositionsList = currentPositionsList;
	}

	public String getSourceCodeText() {
		return sourceCodeText;
	}

	public void setSourceCodeText(String sourceCodeText) {
		this.sourceCodeText = sourceCodeText;
	}

	public String getTargetCodeText() {
		return targetCodeText;
	}

	public void setTargetCodeText(String targetCodeText) {
		this.targetCodeText = targetCodeText;
	}

	public boolean isSourceCodeFound() {
		return sourceCodeFound;
	}

	public void setSourceCodeFound(boolean sourceCodeFound) {
		this.sourceCodeFound = sourceCodeFound;
	}

	public boolean isTargetCodeFound() {
		return targetCodeFound;
	}

	public void setTargetCodeFound(boolean targetCodeFound) {
		this.targetCodeFound = targetCodeFound;
	}

	public Integer getTypeRelation() {
		return typeRelation;
	}

	public void setTypeRelation(Integer typeRelation) {
		this.typeRelation = typeRelation;
	}

	public Object[] getRowSelectedRel() {
		return rowSelectedRel;
	}

	public void setRowSelectedRel(Object[] rowSelectedRel) {
		this.rowSelectedRel = rowSelectedRel;
	}

	public boolean isOneToOne() {
		return oneToOne;
	}

	public void setOneToOne(boolean oneToOne) {
		this.oneToOne = oneToOne;
	}

	public boolean isOneToZero() {
		return oneToZero;
	}

	public void setOneToZero(boolean oneToZero) {
		this.oneToZero = oneToZero;
	}

	public boolean isZeroToOne() {
		return zeroToOne;
	}

	public void setZeroToOne(boolean zeroToOne) {
		this.zeroToOne = zeroToOne;
	}
 

}