package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;

@Named
@RequestScoped
public class VersionsListView extends IndexUIbean implements Serializable {

	/**
	 * Разглеждане на списък на версии в класификация
	 * 
	 */
	private static final long serialVersionUID = 1513308488482508610L;
	static final Logger LOGGER = LoggerFactory.getLogger(VersionsListView.class);
	
	private List<Object[]> versionsList;

	private Integer lang;
	private Integer idClassif;
	
	private String panelInfo;

	@PostConstruct
	public void initData() {
		System.out.println("VersionsListView init");
		try {
			
			if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idObj"))) {
				this.idClassif = Integer.valueOf(JSFUtils.getRequestParameter("idObj"));
				
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
		
				if (this.idClassif != null) {

					JPA.getUtil().runWithClose(() -> {

						this.versionsList = new VersionDAO(ActiveUser.DEFAULT).getPublishedVersions(this.idClassif, this.lang);

						// попълване на информативният панел
						this.panelInfo = new ClassificationDAO(ActiveUser.DEFAULT).decodeClassifIdent(this.idClassif, this.lang);

					});
				}					
			}			
		
		} catch (NumberFormatException e) {
			LOGGER.error("Подаден е невалиден параметър! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param"));
		
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на версии към статистическа класификация!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

	}
	
	public void actionChangeLang() {
		
		initData();
	}	
	
	public List<Object[]> getVersionsList() {
		return versionsList;
	}

	public void setVersionsList(List<Object[]> versionsList) {
		this.versionsList = versionsList;
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

	public String getPanelInfo() {
		return panelInfo;
	}

	public void setPanelInfo(String panelInfo) {
		this.panelInfo = panelInfo;
	}

	

}
