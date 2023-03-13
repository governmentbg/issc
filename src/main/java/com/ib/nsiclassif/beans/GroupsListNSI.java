package com.ib.nsiclassif.beans;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.component.export.PDFOptions;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.customexporter.CustomExpPreProcess;
import com.ib.nsiclassif.db.dao.AdmGroupDAO;
import com.ib.nsiclassif.db.dao.AdmUserDAO;
import com.ib.nsiclassif.db.dto.AdmGroup;
import com.ib.nsiclassif.db.dto.AdmGroupRole;
import com.ib.nsiclassif.db.dto.AdmUser;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.indexui.utils.TreeUtils;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;

/**
 *   Въвеждане и актуализаця на групи потребители
 *  Автор  Г.Белев,  адаптиран  за системата от Стоян
 *
 */

@ViewScoped
@Named(value = "groupsListNSI")
public class GroupsListNSI extends IndexUIbean {

    /**
     *
     */
    private static final long serialVersionUID = -7056074939252508693L;

    static final Logger LOGGER = LoggerFactory.getLogger(GroupsListNSI.class);

    private Integer userId;
    private AdmGroupDAO groupsDAO;
    private AdmUserDAO userDao;
    private AdmGroup group;
    private List<AdmGroup> listGrops = new ArrayList<>();

    private String usersName;
    private List<Integer> usersList = new ArrayList<>();
    private List<String> usersImena = new ArrayList<>();

    private List<String[]> sysClassifList = new ArrayList<>();
    private String selClassif;

    private TreeNode rootNode;
    private TreeNode[] selectedNode;

    private boolean fromEdit = false;
    private String selRkvForGr;                      //  Ред на избрана група в таблицата на групите
    private String selRkvForDos;                    // Ред на избрана класификация в таблицата с класификациите за достъп
    
    private String nameClassifIndDostap;       // Име на класификация за избор на достъп
    
    /**
	 * класификации за контрол на достъпа, за които ако нищо не е избрано се дава пълен достъп
	 * ("system.classificationsNotFilteredIfNotGranted")
	 */
	private Set<Integer> freeAccessList;     // Няма да има такива
	
	/**
	 * класификации за Опции от менюто, които са забранени за избор при даване на достъп - списък за подаване на дървото за readOnlyCodes
	 * ("system.menuForbiddenItems")
	 */
	private List<Integer> forbiddenAccessList;    // Няма да има такива

	/** да се показва ли компонентат за преглед на история */
	private boolean showCompObjAudit;   // Няма да се показва
	
    public GroupsListNSI() {

    }

    @PostConstruct
    public void initData() {

        LOGGER.debug("PostConstruct!!!");

        sysClassifList.clear();


        try {
        	
    //    	this.showCompObjAudit = "1".equals(getSystemData().getSettingsValue("system.showCompObjAudit"));
        	this.showCompObjAudit = false;   // Няма да се използува история
        	
			this.userId = getUserData().getUserId();

			this.groupsDAO = new AdmGroupDAO(getUserData());
			this.userDao = new AdmUserDAO(getUserData());

			String dostap = getSystemData().getSettingsValue("system.classificationsForAccessControl");     // Само достъп до менюто класификация 7
			if (dostap == null)
	                  dostap = String.valueOf(Constants.CODE_CLASSIF_MENU);     // Достъп само до менюто чрез класификацията за меню;
			
			if (dostap != null) {
				this.sysClassifList = new ArrayList<>();

				String[] clasList = dostap.split(",");

				for (int i = 0; i < clasList.length; i++) {
					this.sysClassifList.add(new String[] { clasList[i], getSystemData().getNameClassification(Integer.valueOf(clasList[i]), getCurrentLang()) });
				}
			}

	//		String notFilteredIfNotGranted = getSystemData().getSettingsValue("system.classificationsNotFilteredIfNotGranted");
			String notFilteredIfNotGranted = null;      // Класификации, по които не се филтрира, ако няма зададен достъп - няма
//			if (notFilteredIfNotGranted != null) {
//				this.freeAccessList = new HashSet<>();
//
//				String[] classifIDs = notFilteredIfNotGranted.split(",");
//				for (String id : classifIDs) {
//					this.freeAccessList.add(Integer.valueOf(id));
//				}
//			}
			
//			String menuForbiddenItems = getSystemData().getSettingsValue("system.menuForbiddenItems");
			String menuForbiddenItems = null;              // Опции от менюто, които са збранени при задаве на достъп - няма
//			if (menuForbiddenItems != null) {
//				this.forbiddenAccessList = new ArrayList<>();
//
//				String[] classifIDs = menuForbiddenItems.split(",");
//				for (String id : classifIDs) {
//					this.forbiddenAccessList.add(Integer.valueOf(id));
//				}
//			}

			this.listGrops = this.groupsDAO.findAll();

        } catch (NumberFormatException e) {
            LOGGER.error("Грешка при обработка на данните!", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.formatExc"));

        } catch (DbErrorException e) {
            LOGGER.error("Грешка при зареждане на системна класификация! ", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));

        } catch (Exception e) {
            LOGGER.error("Грешка при работа със системата!!!", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.exception"));
        }

        if (this.sysClassifList != null && this.sysClassifList.size() == 1 ) {  
			this.nameClassifIndDostap = this.sysClassifList.get(0)[1];        // Име на  единствена класификация за достъп
;
		}	
        
    }

    public void actionSave() {

        if (this.group.getGroupName() == null || this.group.getGroupName().isEmpty()) {

            JSFUtils.addMessage("formGroupList:nameGr", FacesMessage.SEVERITY_ERROR,
                    getMessageResourceString(UI_beanMessages, "general.pleaseInsert", getMessageResourceString(UI_LABELS, "groupsList.nameGroup")));

        } else {

            try {

                JPA.getUtil().begin();

                this.group.getGroupUsers().clear();

                if (!this.usersList.isEmpty()) {
                    for (Integer idUser : this.usersList) {

                        this.group.getGroupUsers().add(this.userDao.findById(idUser));
                    }
                }

                this.group = this.groupsDAO.save(this.group);

                JPA.getUtil().commit();

                if (this.group.getId() == null) {

                    this.listGrops.add(this.group);

                } else {

                    this.listGrops = this.groupsDAO.findAll();

                    if (this.selRkvForGr != null && !this.selRkvForGr.isEmpty()) {
                        PrimeFaces.current().executeScript("PrimeFaces.widgets.groupWV.selectRow(" + this.selRkvForGr + ")");
                    }

                    if (this.selRkvForDos != null && !this.selRkvForDos.isEmpty()) {
                        PrimeFaces.current().executeScript("PrimeFaces.widgets.dostapWV.selectRow(" + this.selRkvForDos + ")");
                    }
                }

                JSFUtils.addInfoMessage(getMessageResourceString(UI_beanMessages, "general.succesSaveMsg"));

            } catch (DbErrorException e) {
                JPA.getUtil().rollback();
                LOGGER.error("Грешка при запис на група! ", e);
                JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));

            } catch (Exception e) {
                JPA.getUtil().rollback();
                LOGGER.error("Грешка при работа със системата!!!", e);
                JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.exception"));

            } finally {
                JPA.getUtil().closeConnection();
            }
        }

    }

    public void actionEdit(AdmGroup selectedCroup) {

        this.fromEdit = true;
        this.usersList.clear();
        this.usersImena.clear();

        this.selRkvForGr = JSFUtils.getRequestParameter("rkvIndexG");

        try {

            this.group = groupsDAO.findById(selectedCroup.getId());

            if (!this.group.getGroupUsers().isEmpty()) {

                for (AdmUser user : this.group.getGroupUsers()) {
                    this.usersImena.add(user.getNames());
                    this.usersList.add(user.getId());
                }
                
            	Collections.sort(this.usersImena);
            }

            this.group.getGroupRoles().size();

            PrimeFaces.current().executeScript("PrimeFaces.widgets.groupWV.unselectAllRows()");
            PrimeFaces.current().executeScript("PrimeFaces.widgets.groupWV.selectRow(" + this.selRkvForGr + ")");

        } catch (NumberFormatException e) {
            LOGGER.error("Грешка при обработка на данните!", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.formatExc"));

        } catch (Exception e) {
            LOGGER.error("Грешка при работа със системата!!!", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.exception"));

        } finally {
            JPA.getUtil().closeConnection();
            this.rootNode = null;
            this.selectedNode = null;
        }
        
        if (this.sysClassifList != null && this.sysClassifList.size() == 1 ) {  
			actionLoadTree();
;
		}	

    }

    public void actionNew() {

        this.fromEdit = true;
        this.group = new AdmGroup();
        this.group.setGroupRoles(new ArrayList<>());
        
        this.usersList.clear();
        this.usersImena.clear();
        this.usersName = "";
        this.selClassif = "";

        this.rootNode = null;
        this.selectedNode = null;
        
        if (this.sysClassifList != null && this.sysClassifList.size() == 1 ) {  
     			actionLoadTree();
     ;
     	}	
    }

    public void actionDisablePanelNew () {
    	actionNew();
    	this.group = null;
    }
    
    public void actionDelete() {


        try {

            JPA.getUtil().begin();

            this.groupsDAO.deleteById(this.group.getId());

            JPA.getUtil().commit();

            JSFUtils.addInfoMessage(getMessageResourceString(UI_beanMessages, "general.successDeleteMsg"));

        } catch (DbErrorException e) {
            JPA.getUtil().rollback();
            LOGGER.error("Грешка при изтриване на група! ", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));

        } catch (Exception e) {
            JPA.getUtil().rollback();
            LOGGER.error("Грешка при работа със системата!!!", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.exception"));

        } finally {
            JPA.getUtil().closeConnection();
            actionNew();
            initData();
            this.fromEdit = false;
        }
    }

    public void actionLoadTree() {

        this.rootNode = null;
        this.selectedNode = null;

        this.selClassif = null;
        this.selRkvForDos	 = null;
        if (this.sysClassifList != null && this.sysClassifList.size() > 1) {
        	this.selClassif = JSFUtils.getRequestParameter("idObjAccEdit");
            this.selRkvForDos = JSFUtils.getRequestParameter("rkvIndexD");
        } else {
        	if (this.sysClassifList != null) {
        		this.selClassif =  this.sysClassifList.get(0)[0];
        		this.selRkvForDos =  "0";
        	}
        	
        }
        
        try {

            List<SystemClassif> listItems = getSystemData().getSysClassification(Integer.valueOf(this.selClassif), getToday(), getCurrentLang());

            List<Integer> rolesList = new ArrayList<>();
            if (this.group != null && this.group.getId() != null) {
                for (AdmGroupRole roleTmp : this.group.getGroupRoles()) {
                    if (roleTmp.getCodeClassif().equals(Integer.valueOf(this.selClassif))) {
                        rolesList.add(roleTmp.getCodeRole());
                    }
                }
            }

            rootNode = new TreeUtils().loadCheckboxTree(listItems, false, rolesList, true, this.forbiddenAccessList, null);

            PrimeFaces.current().executeScript("PrimeFaces.widgets.dostapWV.unselectAllRows()");
            PrimeFaces.current().executeScript("PrimeFaces.widgets.dostapWV.selectRow(" + this.selRkvForDos + ")");

        } catch (NumberFormatException e) {
            LOGGER.error("Грешка при обработка на данните!", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.formatExc"));

        } catch (DbErrorException e) {
            LOGGER.error("Грешка при зареждане на дърво! ", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));

        } catch (Exception e) {
            LOGGER.error("Грешка при работа със системата!!!", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.exception"));
        }

    }

    public void actionDeleteUsersList() {

        this.usersList.clear();
        this.usersImena.clear();
        this.usersName = "";
    }

    public void actionDeleteUser() {

        String selectedName = JSFUtils.getRequestParameter("selectedName");

        try {

            for (Integer user : this.usersList) {

                AdmUser userTmp = this.userDao.findById(user);

                if (userTmp.getNames().equals(selectedName)) {
                    this.usersImena.remove(selectedName);
                    this.usersList.remove(user);
                    break;
                }
                
                Collections.sort(this.usersImena);
            }

        } catch (DbErrorException e) {
            LOGGER.error("Грешка при изтриване на участник в групата! ", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));
        }
    }

    // при маркиране се добавят ролите към списъка с роли  на групата
    public void actionNodeSelect(NodeSelectEvent event) {

        TreeNode node = event.getTreeNode();
        AdmGroupRole grRoles;

        if (node.getParent().isPartialSelected() || node.getParent().isSelected()) { // ако родителя е избран или са избрани само някои от децата

            if (isContansCodeRole(((SystemClassif) node.getData()).getCode())) {
                grRoles = new AdmGroupRole();

                grRoles.setCodeClassif(((SystemClassif) node.getData()).getCodeClassif());
                grRoles.setCodeRole(((SystemClassif) node.getData()).getCode());

                this.group.getGroupRoles().add(grRoles);
            }

            List<SystemClassif> parentsList = findAllParents(((SystemClassif) node.getData()).getCodeClassif(), (((SystemClassif) node.getData()).getCode()));

            for (SystemClassif sc : parentsList) {
                if (sc.getCodeParent() != 0 && isContansCodeRole(sc.getCodeParent())) {

                    grRoles = new AdmGroupRole();
                    grRoles.setCodeClassif(sc.getCodeClassif());
                    grRoles.setCodeRole(sc.getCodeParent());

                    this.group.getGroupRoles().add(grRoles);
                }
            }

            List<TreeNode> childrenList = node.getChildren(); // децата на родителя

            for (TreeNode treeNode : childrenList) { // обикалят се децата

                if (!treeNode.getChildren().isEmpty()) {

                    List<TreeNode> childsList = treeNode.getChildren(); // ако децата са родители

                    for (TreeNode nodeTree : childsList) { // обикалят се техните деца и се добавят

                        if (isContansCodeRole(((SystemClassif) nodeTree.getData()).getCode())) {
                            grRoles = new AdmGroupRole();

                            grRoles.setCodeClassif(((SystemClassif) nodeTree.getData()).getCodeClassif());
                            grRoles.setCodeRole(((SystemClassif) nodeTree.getData()).getCode());

                            this.group.getGroupRoles().add(grRoles);
                        }
                    }
                }

                if (isContansCodeRole(((SystemClassif) treeNode.getData()).getCode())) {
                    grRoles = new AdmGroupRole(); // добавят се децата, които не са родители

                    grRoles.setCodeClassif(((SystemClassif) treeNode.getData()).getCodeClassif());
                    grRoles.setCodeRole(((SystemClassif) treeNode.getData()).getCode());

                    this.group.getGroupRoles().add(grRoles);
                }
            }
        }
    }

    // проверява се дали този код на роля вече го има добавен в за групата
    public boolean isContansCodeRole(Integer code) {

        for (AdmGroupRole role : this.group.getGroupRoles()) {

            if (Integer.valueOf(this.selClassif).equals(role.getCodeClassif())) {
                if (code.equals(role.getCodeRole())) {
                    return false;
                }
            }
        }
        return true;
    }

    // при размаркиране се махат ролите от  групата
    public void actionNodeUnselect(NodeUnselectEvent event) {

        TreeNode node = event.getTreeNode();

        Integer codeClassif = ((SystemClassif) node.getData()).getCodeClassif(); // код на системна класификация
        Integer codeRole = ((SystemClassif) node.getData()).getCode(); // код на роля
        Integer codeParent = ((SystemClassif) node.getData()).getCodeParent(); // код на родител

        List<AdmGroupRole> listForRemove = new ArrayList<>(); // списък за махане на ролите

        listForRemove.add(findRole(codeClassif, codeRole)); // към листа се добавя роля за махане

        if (!node.getParent().isPartialSelected() || !node.getParent().isSelected()) { // ако е избран родителя

            List<TreeNode> childrenList = node.getChildren();

            for (TreeNode nodeChilds : childrenList) {

                if (!nodeChilds.isSelected() && !isContansCodeRole(((SystemClassif) nodeChilds.getData()).getCode())) {
                    if (!isExistForRemove(listForRemove, ((SystemClassif) nodeChilds.getData()).getCode())) {
                        listForRemove.add(findRole(codeClassif, ((SystemClassif) nodeChilds.getData()).getCode())); // към листа се добавя всички деца на избрания родител
                    }
                }
            }
        }

        if (!node.isLeaf()) {
            List<TreeNode> childsList = node.getParent().getChildren(); // списък с децата

            boolean remove = false;
            for (TreeNode nodeTree : childsList) { // обикаля се списъка с децата, ако са махнати всички, трябва да се махне и родителя

                if (nodeTree.isSelected()) {
                    remove = false;
                    break;
                } else {
                    remove = true;
                }
            }

            if (remove) {
                if (!isExistForRemove(listForRemove, codeParent) && !isContansCodeRole(codeParent)) {
                    listForRemove.add(findRole(codeClassif, codeParent)); // всички деца са махнати и затова се добавя родителя да се махне също
                }
            }

        } else {

            List<TreeNode> treeParents = new ArrayList<>();
            TreeUtils.getParents(node, treeParents);

            for (int i = 0; i < treeParents.size() - 1; i++) {
                if (!treeParents.get(i).isPartialSelected()) {
                    SystemClassif sc = ((SystemClassif) treeParents.get(i).getData());
                    if (!isExistForRemove(listForRemove, sc.getCode()) && !isContansCodeRole(sc.getCode())) {
                        listForRemove.add(findRole(codeClassif, sc.getCode()));
                    }
                }
            }
        }

        for (AdmGroupRole remRole : listForRemove) { // обикаля се списъка за махане и се махат всички роли, които са в него
            this.group.getGroupRoles().remove(remRole);
        }
    }

    // проверява се дали този код на роля вече го има в списъка за махане
    public boolean isExistForRemove(List<AdmGroupRole> listForRemove, Integer code) {

        for (AdmGroupRole role : listForRemove) {

            if (Integer.valueOf(this.selClassif).equals(role.getCodeClassif())) {
                if (code.equals(role.getCodeRole())) {
                    return true;
                }
            }
        }
        return false;
    }

    // намира конкретния елемент по подаден код на класификация и код на роля
    public AdmGroupRole findRole(Integer codeClassif, Integer codeRole) {

        AdmGroupRole grRole = new AdmGroupRole();

        for (AdmGroupRole role : this.group.getGroupRoles()) {

            if (Integer.valueOf(this.selClassif).equals(codeClassif)) {
                if (codeRole.equals(role.getCodeRole())) {
                    grRole = role;
                }
            }
        }

        return grRole;
    }

    // намиране на всички родители на конкретния код
    public List<SystemClassif> findAllParents(Integer codeClassif, Integer codeRole) {

        List<SystemClassif> parentsList = new ArrayList<>();

        try {

            parentsList = getSystemData().getParents(codeClassif, codeRole, getToday(), getCurrentLang());

        } catch (DbErrorException e) {
            LOGGER.error("Грешка при търсене на списъка с родителите! ", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));
        }

        return parentsList;
    }
    
    /**
	 * @param id
	 * @return <code>true</code> ако класификацията е обяване за достъп до всичко ако не е дадено нищо
	 */
	public boolean isClassifFreeAccess(Integer id) {
		return this.freeAccessList != null && this.freeAccessList.contains(id);
	}
	
	/** @return <code>true</code> ако има поне една класификация */
	public boolean showFreeAccessMessage() {
		return this.freeAccessList != null && !this.freeAccessList.isEmpty();
	}

    public Integer getCodeClassifUser() {
        return SysConstants.CODE_CLASSIF_USERS;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public AdmGroup getGroup() {
        return group;
    }

    public void setGroup(AdmGroup group) {
        this.group = group;
    }

    public List<AdmGroup> getListGrops() {
        return listGrops;
    }

    public void setListGrops(List<AdmGroup> listGrops) {
        this.listGrops = listGrops;
    }

    public String getUsersName() {
        return usersName;
    }

    public void setUsersName(String usersName) {
        this.usersName = usersName;
    }

    public List<Integer> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<Integer> usersList) {

        this.usersImena = new ArrayList<>();

        if (!usersList.isEmpty()) {

            try {

                for (Integer user : usersList) {

                    AdmUser userTmp = this.userDao.findById(user);
                    usersImena.add(userTmp.getNames());
                }
                
                Collections.sort(this.usersImena);

            } catch (DbErrorException e) {
                LOGGER.error("Грешка при търсене на участник в групата! ", e);
                JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));
            }
        }

        this.usersList = usersList;
    }

    public List<String> getUsersImena() {
        return usersImena;
    }

    public void setUsersImena(List<String> usersImena) {
        this.usersImena = usersImena;
    }

    public List<String[]> getSysClassifList() {
        return sysClassifList;
    }

    public void setSysClassifList(List<String[]> sysClassifList) {
        this.sysClassifList = sysClassifList;
    }

    public String getSelClassif() {
        return selClassif;
    }

    public void setSelClassif(String selClassif) {
        this.selClassif = selClassif;
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public TreeNode[] getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode[] selectedNode) {
        this.selectedNode = selectedNode;
    }

    public boolean isFromEdit() {
        return fromEdit;
    }

    public void setFromEdit(boolean fromEdit) {
        this.fromEdit = fromEdit;
    }

    public String getSelRkvForGr() {
        return selRkvForGr;
    }

    public void setSelRkvForGr(String selRkvForGr) {
        this.selRkvForGr = selRkvForGr;
    }

    public String getSelRkvForDos() {
        return selRkvForDos;
    }

    public void setSelRkvForDos(String selRkvForDos) {
        this.selRkvForDos = selRkvForDos;
    }
    
    public Set<Integer> getFreeAccessList() {
		return freeAccessList;
	}

	public void setFreeAccessList(Set<Integer> freeAccessList) {
		this.freeAccessList = freeAccessList;
	}

    public List<Integer> getForbiddenAccessList() {
		return forbiddenAccessList;
	}

	public void setForbiddenAccessList(List<Integer> forbiddenAccessList) {
		this.forbiddenAccessList = forbiddenAccessList;
	}

	public Date getToday() {
        return new Date();
    }

    private Date dateFrom;

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }
    
/******************************** EXPORTS **********************************/
	
	/**
	 * за експорт в excel - добавя заглавие и дата на изготвяне на справката и др. - за групи потребителите
	 */
	public void postProcessXLS(Object document) {
		
		String title = getMessageResourceString(UI_LABELS, "groupsList.reportTitle");		  
    	new CustomExpPreProcess().postProcessXLS(document, title, null, null, null);		
     
	}

	/**
	 * за експорт в pdf - добавя заглавие и дата на изготвяне на справката - за групи потребителите
	 */
	public void preProcessPDF(Object document)  {
		try{
			
			String title = getMessageResourceString(UI_LABELS, "groupsList.reportTitle");		
			new CustomExpPreProcess().preProcessPDF(document, title, null, null, null);		
						
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(),e);			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);			
		} 
	}
	
	/**
	 * за експорт в pdf 
	 * @return
	 */
	public PDFOptions pdfOptions() {
		PDFOptions pdfOpt = new CustomExpPreProcess().pdfOptions(null, null, null);
        return pdfOpt;
	}

	/** @return the showCompObjAudit */
	public boolean isShowCompObjAudit() {
		return this.showCompObjAudit;
	}

	public String getNameClassifIndDostap() {
		return nameClassifIndDostap;
		
	}

	public void setNameClassifIndDostap(String nameClassifIndDostap) {
		this.nameClassifIndDostap = nameClassifIndDostap;
		
	}
}


