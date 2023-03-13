package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.search.CorespTablesSearch;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;

@Named
@RequestScoped
public class CorrespTableListView extends IndexUIbean implements Serializable {

	/**
	 * Разглеждане на списък с кореспондентски таблици във версия
	 * 
	 */
	private static final long serialVersionUID = -8044452002193156541L;
	static final Logger LOGGER = LoggerFactory.getLogger(CorrespTableListView.class);		

	private Integer lang;
	private Integer idVersion;
	
	//Кореспондиращи таблици към версия
	private transient CorespTablesSearch corespTabSearch;
	//private LazyDataModelSQL2Array corespTablesList;
	private List<Object[]> corespTablesList;
	
	private String panelInfo;
	
	@PostConstruct
	public void initData() {
		System.out.println("CorrespTableListView init");
		this.corespTabSearch = new CorespTablesSearch();

		try {
			
			if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idObj"))) {
				this.idVersion = Integer.valueOf(JSFUtils.getRequestParameter("idObj"));
				
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
		
				if (this.idVersion != null) {
					
					/**
					this.corespTabSearch.setLang(this.lang);
					this.corespTabSearch.setVersionId(this.idVersion); 
					this.corespTabSearch.buildQuery();
					this.corespTablesList = new LazyDataModelSQL2Array(this.corespTabSearch, null);	**/
					
					this.corespTablesList = new CorespTableDAO(ActiveUser.DEFAULT).getPublishedTables(this.idVersion, this.lang);
				
					//попълване на информативният панел
					JPA.getUtil().runWithClose(() ->panelInfo =  new VersionDAO(ActiveUser.DEFAULT).decodeVersionIdentName(idVersion, lang));
				}
			} 
		
		} catch (NumberFormatException e) {
			LOGGER.error("Подаден е невалиден параметър! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param"));

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на кореспондентски таблици във версия!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

	}
	
	public void actionChangeLang() {
		
		initData();
	}
	
	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getIdVersion() {
		return idVersion;
	}

	public void setIdVersion(Integer idVersion) {
		this.idVersion = idVersion;
	}

	public CorespTablesSearch getCorespTabSearch() {
		return corespTabSearch;
	}

	public void setCorespTabSearch(CorespTablesSearch corespTabSearch) {
		this.corespTabSearch = corespTabSearch;
	}

	public String getPanelInfo() {
		return panelInfo;
	}

	public void setPanelInfo(String panelInfo) {
		this.panelInfo = panelInfo;
	}

	public List<Object[]> getCorespTablesList() {
		return corespTablesList;
	}

	public void setCorespTablesList(List<Object[]> corespTablesList) {
		this.corespTablesList = corespTablesList;
	}

}
