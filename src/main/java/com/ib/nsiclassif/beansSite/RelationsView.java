package com.ib.nsiclassif.beansSite;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.RelationDAO;
import com.ib.nsiclassif.search.RelationsSearch;
import com.ib.nsiclassif.system.NSIConstants;

@Named("relationsView")
@ViewScoped
public class RelationsView extends IndexUIbean implements Serializable {

	/**
	 * Разглеждане на релации на коресп. таблици
	 * 
	 */
	private static final long serialVersionUID = 4955665421466533568L;	
	static final Logger LOGGER = LoggerFactory.getLogger(RelationsView.class);
	
	private transient RelationDAO dao;

	private RelationsSearch relationSearch=new RelationsSearch();
	
	private LazyDataModelSQL2Array relationsList;

	private Integer lang;
	private Integer relId;
	

	@PostConstruct
	public void initData() {
		System.out.println("relation init");
		this.dao = new RelationDAO(getUserData());
	 

		try {
			
			if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idObj"))) {
				this.relId = Integer.valueOf(JSFUtils.getRequestParameter("idObj"));
				
				if (JSFUtils.getRequestParameter("lang") != null && !"".equals(JSFUtils.getRequestParameter("lang"))) {
					this.setLang(Integer.valueOf(JSFUtils.getRequestParameter("lang")));					
				} else {
					this.setLang(NSIConstants.CODE_DEFAULT_LANG);
				}
				loadRelations();
			} 

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на данни за класификация!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

	}
	
	public void loadRelations() {
		if (JSFUtils.getRequestParameter("searchString") != null && !"".equals(JSFUtils.getRequestParameter("searchString"))) {
			this.relationSearch.setSearchCode(JSFUtils.getRequestParameter("searchString"));
		}
		if (relId!=null) {
			try {
				
				this.relationSearch.setCorespTableId(relId); 
				this.relationSearch.buildQuery();
				this.relationsList = new LazyDataModelSQL2Array(this.getRelationSearch(), null);
	
			} catch (Exception e) {
				LOGGER.error("Грешка при зареждане на класификации! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
		}
	}
	
	public String actionClear() {
		this.relationSearch=new RelationsSearch();
		return "relations?faces-redirect=true&amp;includeViewParams=true";
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getRelId() {
		return relId;
	}

	public void setRelId(Integer relId) {
		this.relId = relId;
	}

	public RelationDAO getDao() {
		return dao;
	}

	public void setDao(RelationDAO dao) {
		this.dao = dao;
	}

	public RelationsSearch getRelationSearch() {
		return relationSearch;
	}

	public void setRelationSearch(RelationsSearch relationSearch) {
		this.relationSearch = relationSearch;
	}

	public LazyDataModelSQL2Array getRelationsList() {
		return relationsList;
	}

	public void setRelationsList(LazyDataModelSQL2Array relationsList) {
		this.relationsList = relationsList;
	}
	
	 

}
