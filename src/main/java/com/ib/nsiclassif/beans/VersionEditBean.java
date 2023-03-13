package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.navigation.Navigation;
import com.ib.indexui.navigation.NavigationData;
import com.ib.indexui.navigation.NavigationDataHolder;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dao.LevelDAO;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.PositionLang;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.PositionUnits;
import com.ib.nsiclassif.db.dto.SchemeItem;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.db.dto.VersionLang;
import com.ib.nsiclassif.search.CorespTablesSearch;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.exceptions.ObjectNotFoundException;
import com.ib.system.utils.SearchUtils;

/**
 * Въвеждане и актуализация на класификационни версии
 * 
 * @author s.arnaudova
 */

@Named("versionEdit")
@ViewScoped
public class VersionEditBean extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4441961551851509040L;
	static final Logger LOGGER = LoggerFactory.getLogger(ClassificationEditBean.class);
	public static final String FORM = "versionEditForm:tabsVersion";
	private static final String ID_CLASSIF = "idClassif";
	private static final String ID_OBJ = "idObj";
	private static final String SUCCESSDELETEMSG = "general.successDeleteMsg";
	private static final String SEARCH_TEXT = "search_text";
	private static final String POSITION = "position";
	
	private transient VersionDAO dao;
	private Version version;
	private VersionLang versionLang;

	private Integer lang;
	private Integer versionId;
	private Integer classifId;

	private List<Object[]> versionsInList;
	//Nodes Experiments starts
	
	
	//Само деца
	private boolean position2Initialized=false;
	
	private TreeNode rootS = new DefaultTreeNode("aaa", null);
	
	private TreeNode selectedNodeV;
	//Table Positions
	private boolean position3Initialized=false;
	private List<PositionS> positions;
	//selected item from left table
	private PositionS selectedItem;
	//After select on left table - load all position here
	//see setSelectedItem
	private PositionS selectedPos;
	private List<PositionS> parentPossitions= new ArrayList<PositionS>();
	private Map <Integer,Boolean> schemePosAttr = new HashMap<Integer, Boolean>();
	private Map <Integer,String> schemePosAttrLabels = new HashMap<Integer, String>();
	private DualListModel<SystemClassif> nacionalnaList;
	private DualListModel<SystemClassif> mejdunarodnaList;

	PositionSDAO posDao;
	
	/**при нова позиция, показа дали е преди,след,дете	 */
	private int typeOfNew;
	
	//Кореспондиращи таблици към версия
	private CorespTablesSearch corespTabSearch;
	private LazyDataModelSQL2Array corespTablesList;
	private CorespTable reversiveCorrTable;
	
	private int activeIndex;
	
	//част от текст по който ще се търси позиция във версията
	private String searchText;
	private List<PositionS> positionsSearch;
	
	private boolean showSearchData;
	
	private PositionS selectedItemSearch;
	
	private boolean optionFindCod;
	private boolean optionFindName;
	
	//Nodes Experiments end
	@PostConstruct
	public void initData() {
		LOGGER.info("INIT VersionEditBean....");
		this.dao = new VersionDAO(getUserData());
		this.versionLang = new VersionLang();
		this.lang =  getCurrentLang();//  NSIConstants.CODE_DEFAULT_LANG;
		
		//System.out.println("getCurrentLang----->"+ lang);
		
		posDao= new PositionSDAO(getUserData());
		
		
		nacionalnaList = new DualListModel<>();
		mejdunarodnaList = new DualListModel<>();
		
		optionFindCod = true;
		optionFindName = true;
		
		activeIndex = 0;
		
		showSearchData = false;
		try {

			if (JSFUtils.getRequestParameter(ID_CLASSIF) != null && !"".equals(JSFUtils.getRequestParameter(ID_CLASSIF))) {
				classifId = Integer.valueOf(JSFUtils.getRequestParameter(ID_CLASSIF));

				if (JSFUtils.getRequestParameter(ID_OBJ) != null && !"".equals(JSFUtils.getRequestParameter(ID_OBJ))) {
					versionId = Integer.valueOf(JSFUtils.getRequestParameter(ID_OBJ));

					if (versionId != null) {
						loadVersion(versionId);
					}

				} else {
					this.version = new Version();
					this.versionLang.setVersion(this.version);
					this.versionLang.setLang(this.lang);
				}

				LOGGER.debug("CLASSIF: " + classifId + " VERSION: " + versionId);

				this.version.setIdClss(classifId);
				loadVersionsList();
				
				
				//ako идва от екрана за глобално търсене зарежда дървото ,позицията ,родителите и я маркира
				if (JSFUtils.getRequestParameter(SEARCH_TEXT) != null && !"".equals(JSFUtils.getRequestParameter(SEARCH_TEXT).trim())) {
					//System.out.println("searching....");
					
					if (JSFUtils.getRequestParameter(POSITION) != null && !"".equals(JSFUtils.getRequestParameter(POSITION).trim())) {
							
						Integer idPos =  Integer.valueOf(JSFUtils.getRequestParameter(POSITION));
						
						activeIndex = 2;
						
						try {
							selectedPos=posDao.findById(idPos);
							
							if(selectedPos == null) {
								JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане на позиция с ид:"+selectedItem.getId()+" !");
							} else {
								copyLangDataPos(); 
								// units init
								initUnitsPosition();
								
								
								parentPossitions = new ArrayList<PositionS>();
								//parentPossitions.add(new PositionS(0, "", versionLang.getIdent(), versionId, 0, 0, 0, 0));
								
								List<Object[]> listParent = posDao.loadParentsPosition(idPos, lang);
								for(Object[] item :listParent) {
									Integer idP = SearchUtils.asInteger(item[0]);
									if(!selectedPos.getId().equals(idP)) {
										parentPossitions.add(new PositionS(idP, SearchUtils.asInteger(item[1]) ,SearchUtils.asString(item[2]),SearchUtils.asString(item[3])));
									}
								}
							}
							
						}catch (Exception e) {
							LOGGER.error("Грешка при зареждане от базата на позиция с ид:"+selectedItem.getId()+" !", e);
							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане от базата на позиция с ид:"+selectedItem.getId()+" !", e.getMessage());
						}finally {
							JPA.getUtil().closeConnection();
						}
						
						loadDataPosition();
						loadSchemeAttPosition();
						
						//fin pos int list and mar
						for(PositionS pTreeItem : positions) {
							if(pTreeItem.getId().equals(selectedPos.getId())) {
								selectedItem = pTreeItem;
								break;
							}
						}
					}
				}
			}

			//ако екрана е извикан от копиране на версия се се махне от навигацията междиният екран за копиране
			if(JSFUtils.getRequestParameter("fromCopy") != null) {
				NavigationDataHolder nav = (NavigationDataHolder) JSFUtils.getManagedBean("navigationSessionDataHolder");
				nav.getPageList().pop();
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на класификационни версии!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}

	}
	
	private void loadVersion(Integer id) {
		try {

			JPA.getUtil().runWithClose(() -> this.version = this.dao.findById(id));

			if (this.version != null) {
				getDataByLang();
			}

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на данни за версия!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}

	/**
	 * Зарежда списък на всички версии, за избор на версия предшественик/наследник.
	 * Ако сме в редакция премахва текущата версия.
	 */
	private void loadVersionsList() {
		try {

			JPA.getUtil().runWithClose(() -> this.versionsInList = this.dao.getClassifVersions(classifId, lang));

			if (this.versionsInList != null) {
				versionsInList.removeIf(v -> v[0].equals(versionId));
			}

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на списък за избор на версия!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}

	public void getDataByLang() {

		if (this.versionLang != null && this.versionLang.getLang() != null) {
			version.getLangMap().put(versionLang.getLang(), versionLang);
		}

		try {

			if (this.version.getLangMap().containsKey(lang)) {
				this.versionLang = version.getLangMap().get(lang);
			} else {

				VersionLang langTmp = new VersionLang();
				langTmp = this.version.getLangMap().get(NSIConstants.CODE_DEFAULT_LANG);

				if (langTmp != null) {
					this.versionLang = langTmp.clone();
					this.versionLang.setId(null);
					this.versionLang.setVersion(this.version);
					this.versionLang.setLang(lang);
				} else {
					this.versionLang = new VersionLang();
					this.versionLang.setLang(lang);
					this.versionLang.setVersion(this.version);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при промяна на език!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void changeLang() {
		
		getDataByLang(); //version
		
		if(position3Initialized) { // ako weè sa otwarqni poziciite
			loadDataPosition();
			
			if(selectedPos!=null) { //ima izbrana poziciq;
				copyLangDataPos();
				// units init
				initUnitsPosition();
			}
		}
		
		UserData ud = (UserData) getUserData();
		ud.setCurrentLang(lang.intValue()); 
	}
	
	public void onTabChange(TabChangeEvent<?> event) {
		if (event != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("onTabChange Active Tab: {}", event.getTab().getId());
			}

			String activeTab = event.getTab().getId();

			if (activeTab.equals("tabLevels")) {
				
				
//				LevelBean level = (LevelBean) JSFUtils.getManagedBean("level");
//				if(level!=null) {level.initData();} 

			}
			
			//Само ако е избран таб-а се зарежда дървото!!!
			if (activeTab.equals("tabPositions2")) {
				if (!isPosition2Initialized()) {
						rootS = new DefaultTreeNode("Root", null);
						addSubTree(versionId, rootS,0);
						setPosition2Initialized(true);
					}
					PrimeFaces.current().ajax().update(":tabPositions2:positionPanel2");
			}
			//Само ако е избран таб-а се зарежда дървото!!!
			if (activeTab.equals("tabPositions3")) {
				if (!isPosition3Initialized()) {
					loadDataPosition();
					loadSchemeAttPosition();
				}
				PrimeFaces.current().ajax().update(":tabPositions3:positionPanel3");
			}
			
			if (activeTab.equals("tabCorrespTabForVer")) {

				this.corespTabSearch = new CorespTablesSearch();
				actionSearchCorrespTables();				
			}
			
			if (activeTab.equals("tabUsers")) {
				PrimeFaces.current().ajax().update(":tabUsers:compusrPanel");
			}
			
			if (activeTab.equals("tabDocs")) {
				//PrimeFaces.current().ajax().update(":tabDocs:compdocs");
			}

		}
	}	

	private boolean checkData() {
		boolean flag = true;

		if (this.versionLang.getIdent().isEmpty()) {
			JSFUtils.addMessage(FORM + ":ident", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages,
					MSGPLSINS, getMessageResourceString(LABELS, "versionEdit.ident")));
			flag = false;
		}

		if (this.versionLang.getTitle().isEmpty()) {
			JSFUtils.addMessage(FORM + ":name", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages,
					MSGPLSINS, getMessageResourceString(LABELS, "versionEdit.name")));
			flag = false;
		}

		if (this.version.getCopyright() == null) {
			JSFUtils.addMessage(FORM + ":copyright", FacesMessage.SEVERITY_ERROR, getMessageResourceString(
					UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "versionEdit.copyright")));
			flag = false;
		}
		
		// Васко поиска да не са задължителни Дата на утвърждаване и Дата на влизане в сила - 04.03.2022
//		if (this.version.getConfirmDate() == null) {
//			JSFUtils.addMessage(FORM + ":confirmDate", FacesMessage.SEVERITY_ERROR, getMessageResourceString(
//					UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "versionEdit.validationDate")));
//			flag = false;
//		}
//
//		if (this.version.getReleaseDate() == null) {
//			JSFUtils.addMessage(FORM + ":releaseDate", FacesMessage.SEVERITY_ERROR, getMessageResourceString(
//					UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "versionEdit.enactment")));
//			flag = false;
//		}

		if (this.version.getStatus() == null) {
			JSFUtils.addMessage(FORM + ":status", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages,
					MSGPLSINS, getMessageResourceString(LABELS, "versionEdit.status")));
			flag = false;
		}

		return flag;
	}

	public void actionSave() {
		try {

			if (checkData()) {
				version.getLangMap().put(versionLang.getLang(), versionLang);
				
				/**@author s.arnaudova
				 * 
				 * по молба на КК добавям датата в края на идентификатора и наименованието (САМО АКО е нов запис)
				 * */
				if (this.version.getId() == null) {
					SimpleDateFormat dtf = new SimpleDateFormat("dd.MM.yyyy");

					String tmpIdent = this.versionLang.getIdent() + "(" + dtf.format(new Date()) + ")";
					this.versionLang.setIdent(tmpIdent);

					String tmpName = this.versionLang.getTitle() + "(" + dtf.format(new Date()) + ")";
					this.versionLang.setTitle(tmpName);
				}

				
				JPA.getUtil().runInTransaction(() -> this.dao.save(this.version));

				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
				
				
				if (this.versionId == null) {
					setVersionId(this.version.getId());
				}
				
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
							classif.loadVersions(); // да презареди списъка, за да се коригира броя резултати
						}
					}
				}

			}
		} catch (Exception e) {
			LOGGER.error("Грешка при запис!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void actionPublicate() {
		 
		if (version.getPublicated()) {
			String path="";
			try {
				path = getSystemData().getSettingsValue("FilesSaveLocation")+"version/"+version.getId();
			} catch (DbErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			new ClassificationDAO(getUserData()).deleteExportedFiles(path);
			actionSave();
		}else {
			
			actionSave();
		}
	}
	
	public void actionSearchCorrespTables() {		

		this.corespTabSearch.setLang(this.lang);
		this.corespTabSearch.setVersionId(this.version.getId()); 
		this.corespTabSearch.buildQuery();
		this.corespTablesList = new LazyDataModelSQL2Array(this.corespTabSearch, "DATE_REG DESC");		
	}
	
	public String redirectToCorespTable(Integer idObj) {
		
		if (idObj != null) {			
			return "corespTable.jsf?faces-redirect=true&idClassif=" + this.classifId + "&idVersion=" + this.version.getId() + "&idObj=" + idObj;
		
		} else {
			return "corespTable.jsf?faces-redirect=true&idClassif=" + this.classifId + "&idVersion=" + this.version.getId();
		}
		
	}
	
	public String actionCreateReverseTable(Integer idCorrTab) {
		
		try {
			
			if (idCorrTab != null) {
				
				this.reversiveCorrTable = new CorespTable();				
				
				JPA.getUtil().runInTransaction(() -> this.reversiveCorrTable =  new CorespTableDAO(getUserData()).createReverseTable(idCorrTab));
				
				return "corespTable.jsf?faces-redirect=true&idClassif=" + this.classifId + "&idVersion=" + this.version.getId() + "&idObj=" + this.reversiveCorrTable.getId();					
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
	
	public void actionDeleteVersion() {
		try {
			
			JPA.getUtil().runInTransaction(() ->  this.dao.delete(version));

			this.version = new Version();
			
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
					
					ClassificationEditBean classifEdit = (ClassificationEditBean) mapV.get("classifEdit");
					if (classifEdit != null) {
						classifEdit.loadVersions();
					}
				}
				navHolder.goBack(); 
			}

			
		} catch (ObjectInUseException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на версия! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
		
	public VersionDAO getDao() {
		return dao;
	}

	public void setDao(VersionDAO dao) {
		this.dao = dao;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public VersionLang getVersionLang() {
		return versionLang;
	}

	public void setVersionLang(VersionLang versionLang) {
		this.versionLang = versionLang;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	public Integer getClassifId() {
		return classifId;
	}

	public void setClassifId(Integer classifId) {
		this.classifId = classifId;
	}

	public List<Object[]> getVersionsInList() {
		return versionsInList;
	}

	public void setVersionsInList(List<Object[]> versionsInList) {
		this.versionsInList = versionsInList;
	}

	
	
	//For Nodes Tree Only children
	public boolean isPosition2Initialized() {
		return position2Initialized;
	}

	public void setPosition2Initialized(boolean position2Initialized) {
		this.position2Initialized = position2Initialized;
	}
	public TreeNode getRootS() {
		return rootS;
	}

	public void setRootS(TreeNode rootS) {
		this.rootS = rootS;
	}
	
	public void onNodeExpand(NodeExpandEvent event) {
		TreeNode currentTmpNode = event.getTreeNode();
		if (currentTmpNode.getChildren()!=null && currentTmpNode.getChildren().size()==1 && currentTmpNode.getChildren().get(0).getData().equals(-1)) {
			currentTmpNode.getChildren().clear();
			addSubTree(versionId,currentTmpNode,((SchemeItem)currentTmpNode.getData()).getId());
		}
	}
	
	public void onNodeSelectV(NodeSelectEvent event) {
		setSelectedNodeV(event.getTreeNode());

	}
	
	/**
	 * Зарежда сдецата на конкретен нод. Само Преките наследници
	 * Важно е да се знае, че ако нода има деца, се слага един служебен с дата="-1", за да може да се експандва дървото
	 * После при еьпанд се проверява и ако го има това -1 се мха и се зареждат истинските
	 * @param versionId
	 * @param currentTmpNode
	 * @param id - всички деца на това ид се добавят
	 */
	private void addSubTree(Integer versionId,TreeNode currentTmpNode,Integer id) {
		try {
			Date start = new Date();
			//SchemeItem data = (SchemeItem) currentTmpNode.getData();
			
//			System.out.println("id="+data.getId());
			List<PositionS> items = posDao.loadScheme(versionId, id,lang,null,0);
			
			for (Iterator iterator = items.iterator(); iterator.hasNext();) {
				PositionS pos = (PositionS) iterator.next();
				SchemeItem schemeItem= new SchemeItem(pos.getId(),pos.getCode(),pos.getName() ,pos.getVersionId(), pos.getId() ,pos.getIdPrev(), pos.getIdParent(),pos.getLevelNumber(), pos.getIndChild());
						//ID_CLASSIF, FORM, versionId, id, id, id, versionId, id
				DefaultTreeNode node = new DefaultTreeNode(schemeItem, currentTmpNode);
				if (schemeItem.getIndChild().equals(1)) {
					node.getChildren().add(new DefaultTreeNode(-1));
				}
			}
			Date end = new Date();
			//System.out.println("=== Get data from DB time spent --->" + (end.getTime() - start.getTime()) + " ms.");

			
			
			
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на дървото с позиции addSubTree !", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG),e.getMessage());
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
	
	// For Nodes TABLE - Start
	
	public boolean isPosition3Initialized() {
		return position3Initialized;
	}

	public void setPosition3Initialized(boolean position3Initialized) {
		this.position3Initialized = position3Initialized;
	}

	public List<PositionS> getPositions() {
		return positions;
	}

	public void setPositions(List<PositionS> positions) {
		this.positions = positions;
	}

	public PositionS getSelectedPos() {
		return selectedPos;
	}

	public void setSelectedPos(PositionS selectedPos) {
		this.selectedPos = selectedPos;
	}

	public List<PositionS> getParentPossitions() {
		return parentPossitions;
	}

	public void setParentPossitions(List<PositionS> parentPossitions) {
		this.parentPossitions = parentPossitions;
	}

	public CorespTablesSearch getCorespTabSearch() {
		return corespTabSearch;
	}

	public void setCorespTabSearch(CorespTablesSearch corespTabSearch) {
		this.corespTabSearch = corespTabSearch;
	}

	public void loadPositionsTbl(Integer versionId,Integer parentId) {
		try {
			positions = posDao.loadScheme(versionId, parentId,lang,null,0);
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на дървото с позиции loadPositionsTbl !", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	/**Изпълнява се, когато се избере нещо от стека
	 * @param index
	 */
	public void actionSelectParent(int index) {
		//System.out.println("actionSelectParent:"+index);
		PositionS pos = parentPossitions.get(index);
		loadPositionsTbl(versionId, pos.getIdParent());  //pos.getId()
		parentPossitions.subList(index, parentPossitions.size()).clear();
		
		//setSelectedItem(pos);
		
		selectedItem = null;
		selectedPos  = null;
	}
	
	public void actionGetChildren(PositionS pos) {
		//System.out.println("Selected pos:"+pos.getCode());
		parentPossitions.add(pos);
		loadPositionsTbl(versionId, pos.getId());
		
		//setSelectedItem(pos);
		
		selectedItem = null;
		selectedPos  = null;
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

	public PositionS getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(PositionS selectedItem) {
		//System.out.println("setSelectedItem");
		if(selectedItem!=null) {  // pri skoroliraneto na tablicata e null i wliza seta
			this.selectedItem = selectedItem;
			try {
				selectedPos=posDao.findById(selectedItem.getId());
				
				if(selectedPos == null) {
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане на позиция с ид:"+selectedItem.getId()+" !");
				} else {
					copyLangDataPos(); 
					// units init
					initUnitsPosition();
				}
				
				selectedItemSearch = null;
			}catch (Exception e) {
				LOGGER.error("Грешка при зареждане от базата на позиция с ид:"+selectedItem.getId()+" !", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане от базата на позиция с ид:"+selectedItem.getId()+" !", e.getMessage());
			}finally {
				JPA.getUtil().closeConnection();
			}
		}
	}
	
	private void loadDataPosition() {
		
		Integer codeLoad = 0;
		if(parentPossitions!=null && !parentPossitions.isEmpty()) {
			codeLoad  =  parentPossitions.get(parentPossitions.size()-1).getId();
		} else {
			//Изкуствено слагам рут-а. Ест, тук трябва да е ието на версията
			parentPossitions = new ArrayList<PositionS>();
			//parentPossitions.add(new PositionS(0, "", versionLang.getIdent(), versionId, 0, 0, 0, 0));
		}
		
		loadPositionsTbl(versionId, codeLoad);
		
		setPosition3Initialized(true);
		
		if(positions==null || positions.size()==0) {
			actionNewPos(-1);
		}
		
	}
	
	private void loadSchemeAttPosition() {
		try {
			List<Integer> listAttr = new ArrayList<Integer>();
			
			listAttr = posDao.loadPositionAttr(versionId);
			
			schemePosAttr.clear();
			
			for(Integer attr : listAttr) {
				schemePosAttr.put(attr, Boolean.TRUE);
			}
			
			List<SystemClassif> items = getSystemData().getSysClassification(NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, new Date(), NSIConstants.CODE_DEFAULT_LANG); //izpolzwa se za lejbyli i zatowa ne mi trqbwa na drug ezik
			schemePosAttrLabels.clear();
			for(SystemClassif item : items) {
				schemePosAttrLabels.put(item.getCode(), item.getTekst());
			}
			
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на схема на атрибути на позиция!!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при зареждане на схема на атрибути на позиция!");
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	/**
	 * Запис на позиция
	 */
	public void actionSavePos() {
		
		//System.out.println(selectedPos);
		
		//проверка на задължителните параметри
		if(checkCodeAndName()) return;
		
		try {
			
			selectedPos.getUnits().clear();
			
			for(int i=0; i<nacionalnaList.getTarget().size(); i++) {
				
				Object item = nacionalnaList.getTarget().get(i);
				
				PositionUnits pu = new PositionUnits();
				pu.setPosition(selectedPos);
				pu.setUnit(((SystemClassif)item).getCode());
			//	pu.setUnit(Integer.valueOf((String)item));
				pu.setTypeUnit(NSIConstants.CODE_ZNACHENIE_NACIONALNA);
				selectedPos.getUnits().add(pu);
			}
			for(int i=0; i<mejdunarodnaList.getTarget().size(); i++){
				
				Object item = mejdunarodnaList.getTarget().get(i);
				
				PositionUnits pu = new PositionUnits();
				pu.setPosition(selectedPos);
				pu.setUnit(((SystemClassif)item).getCode());
			//	pu.setUnit(Integer.valueOf((String)item));
				pu.setTypeUnit(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA);
				selectedPos.getUnits().add(pu);
			}
			
			JPA.getUtil().begin();
			//Update
			if (selectedPos.getId()!=null) {
				selectedPos = posDao.save(selectedPos);
			//New insert
			}else{
				Integer idSelectedItem = Integer.valueOf(0);
				if(selectedItem!=null) {
					idSelectedItem = selectedItem.getId();
				}
				//System.out.println("=== its new type:"+typeOfNew);
				//System.out.println("selectedItem id:"+idSelectedItem);
				//System.out.println(selectedPos);
				selectedPos = posDao.saveSchemePosition(selectedPos, typeOfNew, idSelectedItem, versionId);
			}
			JPA.getUtil().commit();
			
			//Refresh
			positions = posDao.loadScheme(versionId, selectedPos.getIdParent(),lang,null,0);
			
			//if position is new and item is new children add current in stack
			if (typeOfNew==3) {
				parentPossitions.add(selectedItem);
				//select new as current 3-insert child
				selectedItem=positions.get(0);
				
			}else if (typeOfNew==1) {
				//select new as current 1-insert before
				int indexOldPos = positions.indexOf(selectedItem);
				selectedItem=positions.get(indexOldPos-1);
				
				//System.out.println("=== indexOldPos:"+indexOldPos);
			}else if (typeOfNew==2) {
				//select new as current 2-insert after
				int indexOldPos = positions.indexOf(selectedItem);
				selectedItem=positions.get(indexOldPos+1);
				
				//System.out.println("=== indexOldPos:"+indexOldPos);
			}else if (typeOfNew==-1) {
				//select new as current -1 first time insert item
				selectedItem=positions.get(0);
			}
			typeOfNew=0; // маркираме че не е нова
			
			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
			
		} catch (DbErrorException e) {
			JPA.getUtil().rollback();
			//e.printStackTrace();
			LOGGER.error("Грешка при запис на позиция!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG) ,e.getMessage());
		} catch (Exception e) {
			JPA.getUtil().rollback();
			//e.printStackTrace();
			LOGGER.error("Грешка при запис на позиция!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	/**
	 * @param type:къде (NSIConstants  --> CODE_ZNACHENIE_INSERT_BEFORE, CODE_ZNACHENIE_INSERT_AFTER, CODE_ZNACHENIE_INSERT_AS_CHILD)
	 */
	public void actionNewPos(int type) {
		//System.out.println("Create new position as[-1 first time insert item, 0-update item , 1-insert before,2-insert after,3-insert child:"+type);
	    //System.out.println("parent is:"+getSelectedPos().getSchemeItem().getId());
		this.typeOfNew=type;
		PositionS p = new PositionS();
		//Tuk prawq prostotii za da mi e po-lesno pri zapisa
		if(selectedItem!=null) {
			p.setLevelNumber(type==3?selectedItem.getLevelNumber()+1:selectedItem.getLevelNumber());
		}else {
			p.setLevelNumber(1);
		}
		
		PositionLang lang = new PositionLang();
		lang.setLang(this.lang);
		lang.setPosition(p);
		p.getLangMap().put(this.lang, lang);
		setSelectedPos(p);
		
		//unit
		initUnitsPosition();
	}
	
	public void actionDeletePos() {
		
		try {
			JPA.getUtil().begin();
			
			//проверка за релации
			boolean check = posDao.checkRelationPosition(versionId , selectedPos.getCode());
			
			if(check) {
				JPA.getUtil().rollback();
				LOGGER.error("Poziciqta uchastwa v relacii i nemoje da bade iztrita!");
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(beanMessages, "position.relPos") );
			} else {
			
				Integer parent = selectedPos.getIdParent();
				
				posDao.deletePosition(selectedPos);
				
				JPA.getUtil().commit();
				
				//Refresh
				positions = posDao.loadScheme(versionId, parent,lang,null,0);
				
				
				//clear selected position
				selectedPos = null;
				
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, "general.successDeleteMsg"));
			}
		}catch (DbErrorException e) {
			JPA.getUtil().rollback();
			//e.printStackTrace();
			LOGGER.error("Грешка при изтриване на позиция!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG) ,e.getMessage());
		} catch (ObjectInUseException e) {
			LOGGER.error("Позиция се използва и неможе да бъде изтрита!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, "general.objectInUse"), e.getMessage());
		} catch (Exception e) {
			JPA.getUtil().rollback();
			//e.printStackTrace();
			LOGGER.error("Грешка при изтриване на позиция!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	private void copyLangDataPos() {
		if(!selectedPos.getLangMap().containsKey(lang)) {
			
			PositionLang lang = new PositionLang();
			lang.setLang(this.lang);
			lang.setPosition(selectedPos);
			
			PositionLang defLang = selectedPos.getLangMap().get(Constants.CODE_DEFAULT_LANG);
			
			if(defLang!=null) {
				lang.setOffitialTitile("!!!Непреведено!!! - "+defLang.getOffitialTitile()); //задължително като значение
				
				if(defLang.getShortTitle()!=null && !defLang.getShortTitle().isEmpty()) {
					lang.setShortTitle("!!!Непреведено!!! - "+defLang.getShortTitle());
				}
				
				if(defLang.getLongTitle()!=null && !defLang.getLongTitle().isEmpty()) {
					lang.setLongTitle("!!!Непреведено!!! - "+defLang.getLongTitle());
				}
				
				if(defLang.getAlsoIncludes()!=null && !defLang.getAlsoIncludes().isEmpty()) {
					lang.setAlternativeNames("!!!Непреведено!!! - "+defLang.getAlsoIncludes());
				}
				
				if(defLang.getIncludes()!=null && !defLang.getIncludes().isEmpty()) {
					lang.setIncludes("!!!Непреведено!!! - "+defLang.getIncludes());
				}
				
				if(defLang.getAlsoIncludes()!=null && !defLang.getAlsoIncludes().isEmpty()) {
					lang.setAlsoIncludes("!!!Непреведено!!! - "+defLang.getAlsoIncludes());
				}
				
				if(defLang.getExcludes()!=null && !defLang.getExcludes().isEmpty()) {
					lang.setExcludes("!!!Непреведено!!! - "+defLang.getExcludes());
				}
				
				if(defLang.getComment()!=null && !defLang.getComment().isEmpty()) {
					lang.setComment("!!!Непреведено!!! - "+defLang.getComment());
				}
				
				if(defLang.getRules()!=null && !defLang.getRules().isEmpty()) {
					lang.setRules("!!!Непреведено!!! - "+defLang.getRules());
				}
				
				if(defLang.getPrepratka()!=null && !defLang.getPrepratka().isEmpty()) {
					lang.setPrepratka("!!!Непреведено!!! - "+defLang.getPrepratka());
				}
				
				if(defLang.getStatPokazatel()!=null && !defLang.getStatPokazatel().isEmpty()) {
					lang.setStatPokazatel("!!!Непреведено!!! - "+defLang.getStatPokazatel());
				}
			}
			selectedPos.getLangMap().put(this.lang, lang);
		}
	}
	
	
	private void initUnitsPosition() {
		
		try {
			nacionalnaList =   new DualListModel<>(getSystemData().getSysClassification(NSIConstants.CODE_CLASSIF_UNITS, new Date(), lang) ,new ArrayList<SystemClassif>());
			mejdunarodnaList = new DualListModel<>(getSystemData().getSysClassification(NSIConstants.CODE_CLASSIF_UNITS, new Date(), lang) ,new ArrayList<SystemClassif>());
			
			if(selectedPos!=null) {
				List<PositionUnits> units = selectedPos.getUnits();
				
				if(units!=null && !units.isEmpty()) {
					
					for(PositionUnits unit:units) {
						SystemClassif itemTarget = getSystemData().decodeItemLite(NSIConstants.CODE_CLASSIF_UNITS, unit.getUnit(), lang, selectedPos.getDateLastMod(), false);
						if(unit.getTypeUnit().intValue() == NSIConstants.CODE_ZNACHENIE_NACIONALNA) {
							nacionalnaList.getTarget().add(itemTarget);
							nacionalnaList.getSource().remove(itemTarget); //nacionalnaList.getSource().remove(itemTarget);
						} else {
							mejdunarodnaList.getTarget().add(itemTarget);
							mejdunarodnaList.getSource().remove(itemTarget);
						}
					}
				}
			}
		} catch(DbErrorException e) { //tuk ne se zatwarq sesiq h2
			LOGGER.error("Грешка при разкодиране на мерни еденици!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при разкодиране на мерни еденици");
		}
	}
	
	/** Проверявам:
     *  1. за код на съвпадение в базата по id на версия/вариант;
     *  2. за коректост на маската на кода по id на версия/вариант и id на нивото
     *  3. ако е избран тип на позицията 'без код' горните 2 условия се прескачат
     *  4. проверка за въведено наименование
     * @return true/false 
     */
     private boolean checkCodeAndName() {
        
            boolean check = false;
           
         
            if( selectedPos.getCodeType().intValue()== NSIConstants.CODE_ZNACHENIE_LIPSVAST_COD){
                if(selectedPos.getCode()!=null && !selectedPos.getCode().equals("")){
                    check = true;
                    
                    JSFUtils.addMessage(FORM + ":inCodeT", FacesMessage.SEVERITY_ERROR, "Типа на кода на позицията не позволява Код 'със стойност'!");
                }
            }else{
                /**Намирам номера на нивото*/
               
                Integer localLevelNumber = 1;
                if(selectedItem!=null) { //ако е избрана позиция
                	localLevelNumber = selectedItem.getLevelNumber();
                	if (typeOfNew==3) { //ако е казано  ново трябва да увеличим нивото с 1-ца
                		localLevelNumber++;
                	}
                }
                 
                try {
                    /** проверка на маската на кода на позицията за нивото*/
                    if(posDao.checkCode(selectedPos.getCode() , new LevelDAO(getUserData()).findMask (versionId, localLevelNumber))){
                        /** проверка за съвпадение на кода на позицията*/
                        if( posDao.checkCodeByIdPosition(versionId, selectedPos.getCode() ,selectedPos.getId()) ){
                            check = true;
                            JSFUtils.addMessage(FORM + ":inCodeT", FacesMessage.SEVERITY_ERROR, "Вече има въведена позиция с такъв код.!");
                        }
                    }else{
                        check = true;
                        JSFUtils.addMessage(FORM + ":inCodeT", FacesMessage.SEVERITY_ERROR, "Въведеният код не отговаря на маската на нивото.!");
                    }
                } catch (ObjectNotFoundException e) {
                    check = true;
                    JSFUtils.addMessage(FORM + ":inCodeT", FacesMessage.SEVERITY_ERROR, "Няма описано ниво с номер " + selectedPos.getCode() + " за тази версия");
                } catch (Exception e) {
                    check = true;
                    LOGGER.error("Грешка при разкодиране на маска на ниво и позиция - проверка!", e);
                    JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при разкодиране на маска на ниво и позиция - проверка!");
                } finally {
        			JPA.getUtil().closeConnection();
        		}
           }
            
           /*проверка за въведено наименование*/
           if( selectedPos.getLangMap().get(lang).getOffitialTitile()== null || selectedPos.getLangMap().get(lang).getOffitialTitile().trim().equals("") ){
                check = true;
                JSFUtils.addMessage(FORM + ":inNameT", FacesMessage.SEVERITY_ERROR, "Моля, въведете '"+schemePosAttrLabels.get(11)+"'!");
           } 
          
           return check;
     }
	

 	public void actionSearchPosition() {
 		
 		System.out.println("find code:"+optionFindCod);
 		System.out.println("find name:"+optionFindName);
 		
 		//TODO
 		showSearchData = false;
 		selectedItemSearch=null;
 		selectedItem = null;
 		selectedPos = null;
 		if (searchText==null || searchText.trim().isEmpty()) {
 			// 
 			JSFUtils.addMessage(FORM + ":searchText", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages,"general.pleasInsertText"));
 		} else {
 			
 			//
 			try {
 				positionsSearch = posDao.searchPositionsVersion(searchText, lang, versionId, optionFindCod,optionFindName);
 				
 				if(positionsSearch!=null && !positionsSearch.isEmpty()) {
 					showSearchData = true;
 				} else {
 					JSFUtils.addMessage(FORM + ":searchText", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages,"general.notFoundResult"));
 				}
 			} catch (Exception e) {
 				LOGGER.error("Грешка при зареждане на дървото с позиции loadPositionsTbl !", e);
 				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
 			} finally {
 				JPA.getUtil().closeConnection();
 			}
 		}
 	}
 	
 	public void actionSearchPositionClear() {
 		showSearchData = false;
 		searchText = "";
 		positionsSearch = new ArrayList<PositionS>();
 		selectedItemSearch=null;
 	}
     
	public TreeNode getSelectedNodeV() {
		return selectedNodeV;
	}

	public void setSelectedNodeV(TreeNode selectedNodeV) {
		this.selectedNodeV = selectedNodeV;
	}

	public Map <Integer,Boolean> getSchemePosAttr() {
		return schemePosAttr;
	}

	public void setSchemePosAttr(Map <Integer,Boolean> schemePosAttr) {
		this.schemePosAttr = schemePosAttr;
	}

	public Map <Integer,String> getSchemePosAttrLabels() {
		return schemePosAttrLabels;
	}

	public void setSchemePosAttrLabels(Map <Integer,String> schemePosAttrLabels) {
		this.schemePosAttrLabels = schemePosAttrLabels;
	}

	public DualListModel<SystemClassif> getNacionalnaList() {
		return nacionalnaList;
	}

	public void setNacionalnaList(DualListModel<SystemClassif> nacionalnaList) {
		this.nacionalnaList = nacionalnaList;
	}

	public DualListModel<SystemClassif> getMejdunarodnaList() {
		return mejdunarodnaList;
	}

	public void setMejdunarodnaList(DualListModel<SystemClassif> mejdunarodnaList) {
		this.mejdunarodnaList = mejdunarodnaList;
	}

	public int getActiveIndex() {
		return activeIndex;
	}

	public void setActiveIndex(int activeIndex) {
		this.activeIndex = activeIndex;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public List<PositionS> getPositionsSearch() {
		return positionsSearch;
	}

	public void setPositionsSearch(List<PositionS> positionsSearch) {
		this.positionsSearch = positionsSearch;
	}

	public boolean isShowSearchData() {
		return showSearchData;
	}

	public void setShowSearchData(boolean showSearchData) {
		this.showSearchData = showSearchData;
	}

	public PositionS getSelectedItemSearch() {
		return selectedItemSearch;
	}

	public void setSelectedItemSearch(PositionS selectedItemSearch) {
		
		if(selectedItemSearch!=null) {
			
			setSelectedItem(selectedItemSearch);
			
			if(selectedPos!=null) {
				parentPossitions.clear();
				try {
					List<Object[]> listParent = posDao.loadParentsPosition(selectedPos.getId(), lang);
					for(Object[] item :listParent) {
						Integer idP = SearchUtils.asInteger(item[0]);
						if(!selectedPos.getId().equals(idP)) {
							parentPossitions.add(new PositionS(idP, SearchUtils.asInteger(item[1]) ,SearchUtils.asString(item[2]),SearchUtils.asString(item[3])));
						}
					}
					
					
				}catch (Exception e) {
					LOGGER.error("Грешка при зареждане от базата на позиция с ид:"+selectedItem.getId()+" !", e);
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане от базата на позиция с ид:"+selectedItem.getId()+" !", e.getMessage());
				}finally {
					JPA.getUtil().closeConnection();
				}
				
				loadDataPosition();
				
				showSearchData = false;
			}
		}
		
		this.selectedItemSearch = selectedItemSearch;
	}
	
	public void showPositionList() {
		showSearchData = false;
	}
	public void showSearchPositionList() {
		showSearchData = true;
	}
	
	public boolean isOptionFindCod() {
		return optionFindCod;
	}

	public void setOptionFindCod(boolean optionFindCod) {
		this.optionFindCod = optionFindCod;
	}

	public boolean isOptionFindName() {
		return optionFindName;
	}

	public void setOptionFindName(boolean optionFindName) {
		this.optionFindName = optionFindName;
	}


}
