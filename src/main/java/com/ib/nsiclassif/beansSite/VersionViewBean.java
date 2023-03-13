package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.db.dto.VersionLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.utils.SearchUtils;



@Named("versionView")
@RequestScoped
public class VersionViewBean extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4441961551851509040L;
	static final Logger LOGGER = LoggerFactory.getLogger(VersionViewBean.class);
	
	private static final String ID_OBJ = "idObj";


	private transient VersionDAO dao;
	private Version version;
	private VersionLang versionLang;

	private Integer lang;
	private Integer versionId;

	private List<Object[]> versionsInList;
	private Map <Integer,String> versionsMap = new HashMap<>();
	
	private String panelInfo;
	
	@PostConstruct
	public void initData() {
		System.out.println("VersionViewBean init");
		System.out.println("eziko-------------> "+FacesContext.getCurrentInstance().getViewRoot().getLocale());


		this.dao = new VersionDAO(ActiveUser.DEFAULT);
		this.versionLang = new VersionLang();
	
		try {
			
			if (JSFUtils.getRequestParameter(ID_OBJ) != null && !"".equals(JSFUtils.getRequestParameter(ID_OBJ))) {
				versionId = Integer.valueOf(JSFUtils.getRequestParameter(ID_OBJ));

				this.lang = NSIConstants.CODE_DEFAULT_LANG;
				
				if (JSFUtils.getRequestParameter("lang") != null && !"".equals(JSFUtils.getRequestParameter("lang"))) {
					
					try { 
						
						this.lang = Integer.valueOf(JSFUtils.getRequestParameter("lang"));	
						if(this.lang < 0) { 
							this.lang = NSIConstants.CODE_DEFAULT_LANG;
						}
					
					} catch (Exception e) {
						this.lang = NSIConstants.CODE_DEFAULT_LANG;
					}									
				} 

				if (versionId != null) {
					loadVersion(versionId);
				}

			} else {
				this.version = new Version();
				this.versionLang.setVersion(this.version);
				this.versionLang.setLang(this.lang);
			}

			LOGGER.info(" VERSION: " + versionId);

			loadVersionsList();
			if (versionsInList != null) {
				for (Object[] item : versionsInList) {
					versionsMap.put(SearchUtils.asInteger(item[0]), SearchUtils.asString(item[1]));
				}
			}

		} catch (NumberFormatException e) {
			LOGGER.error("Подаден е невалиден параметър! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param"));
			
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
			
				panelInfo = versionLang.getIdent() +" / "+versionLang.getTitle();
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

			JPA.getUtil().runWithClose(() -> this.versionsInList = this.dao.getClassifVersions(version.getIdClss(), lang));

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

	public List<Object[]> getVersionsInList() {
		return versionsInList;
	}

	public void setVersionsInList(List<Object[]> versionsInList) {
		this.versionsInList = versionsInList;
	}

	public Map <Integer,String> getVersionsMap() {
		return versionsMap;
	}

	public void setVersionsMap(Map <Integer,String> versionsMap) {
		this.versionsMap = versionsMap;
	}

	public String getPanelInfo() {
		return panelInfo;
	}

	public void setPanelInfo(String panelInfo) {
		this.panelInfo = panelInfo;
	}

	


}
