package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.navigation.Navigation;
import com.ib.indexui.navigation.NavigationData;
import com.ib.indexui.navigation.NavigationDataHolder;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;

@Named("versionCopy")
@ViewScoped
public class VersionCopy extends IndexUIbean implements Serializable {
	
	/**
	 * Копиране на версия
	 * 	 
	 */
	private static final long serialVersionUID = 8691715558851441634L;
	static final Logger LOGGER = LoggerFactory.getLogger(VersionCopy.class);
	private static final String ID_CLASSIF = "idClassif";

	private transient VersionDAO dao;
	private Version version;
	private Integer lang;
	private Integer idClassif;
	
	//Копиране
	private List<Object[]> versionsListForCopy;
	private Integer idVerForCopy;
	
	private boolean copyBasicData;
	private boolean copyLevels;	
	private boolean copyTables;	
	private boolean copyUsers;
	private boolean copyScheme;
	
	@PostConstruct
	public void initData() {
		
		LOGGER.debug("PostConstruct - VersionCopy!!!");

		this.dao = new VersionDAO(getUserData());
		this.lang = NSIConstants.CODE_DEFAULT_LANG;
		this.versionsListForCopy = new ArrayList<>();
		
		this.copyBasicData = true;
		this.copyLevels  = false;
		this.copyTables = false; 
		this.copyUsers = false; 
		this.copyScheme = false; 

		if (JSFUtils.getRequestParameter(ID_CLASSIF) != null && !"".equals(JSFUtils.getRequestParameter(ID_CLASSIF))) {
			this.idClassif = Integer.valueOf(JSFUtils.getRequestParameter(ID_CLASSIF));

			loadVersionsList();
		}
	}	

	/**
	 * Зарежда списък на всички версии, за избор на версия предшественик/наследник.
	 * 
	 */
	private void loadVersionsList() {

		try {
			
			JPA.getUtil().runWithClose(() -> this.versionsListForCopy = this.dao.getClassifVersions(this.idClassif, lang));

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на списък на версия!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public String actionCopyVersion() {

		if (this.idVerForCopy != null) {
			
			try {
			
				JPA.getUtil().runInTransaction(() -> this.version = this.dao.copyVersion(this.idVerForCopy, this.copyLevels, this.copyTables, this.copyUsers, this.copyScheme));
				
				loadVersionsList();
				
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
							classif.loadVersions(); // да презареди списъка с версии, за да се коригира броя резултати
						}
					}					
				}
				
				return "versionEdit.jsf?faces-redirect=true&idClassif=" + this.idClassif + "&idObj=" + this.version.getId() + "&fromCopy=" + 1; 
			
			} catch (BaseException e) {
				LOGGER.error("Грешка при копиране на версия!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
			
		} else {
			JSFUtils.addMessage("formVersionCopy:idVerCopy", FacesMessage.SEVERITY_ERROR, getMessageResourceString( UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "versionEdit.versionSource")));
			return null;
		}
		
		return null;
	}	

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	} 
	
	public Integer getIdClassif() {
		return idClassif;
	}

	public void setIdClassif(Integer idClassif) {
		this.idClassif = idClassif;
	}

	public List<Object[]> getVersionsListForCopy() {
		return versionsListForCopy;
	}

	public void setVersionsListForCopy(List<Object[]> versionsListForCopy) {
		this.versionsListForCopy = versionsListForCopy;
	}

	public Integer getIdVerForCopy() {
		return idVerForCopy;
	}

	public void setIdVerForCopy(Integer idVerForCopy) {
		this.idVerForCopy = idVerForCopy;
	}

	public boolean isCopyBasicData() {
		return copyBasicData;
	}

	public void setCopyBasicData(boolean copyBasicData) {
		this.copyBasicData = copyBasicData;
	}

	public boolean isCopyLevels() {
		return copyLevels;
	}

	public void setCopyLevels(boolean copyLevels) {
		this.copyLevels = copyLevels;
	}

	public boolean isCopyTables() {
		return copyTables;
	}

	public void setCopyTables(boolean copyTables) {
		this.copyTables = copyTables;
	}

	public boolean isCopyUsers() {
		return copyUsers;
	}

	public void setCopyUsers(boolean copyUsers) {
		this.copyUsers = copyUsers;
	}

	public boolean isCopyScheme() {
		return copyScheme;
	}

	public void setCopyScheme(boolean copyScheme) {
		this.copyScheme = copyScheme;
	}
	
}
