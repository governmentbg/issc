package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;

@Named
@ViewScoped
public class SearchBean extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7661830720565487636L;

	static final Logger LOGGER = LoggerFactory.getLogger(SearchBean.class);		

	private Integer lang;
	private static final String LANG = "lang";
	
	private LazyDataModelSQL2Array resultList;
	
	private String nameSearch;
	private boolean oficialTitle=true;
	private boolean longTitle;
	private boolean shortTitle;
	private boolean alternateTitle;
	private boolean include;
	private boolean alsoInclude;
	private boolean exclude;
	private boolean rules;
	private boolean comment;
	
	private boolean markedAll=false;
	
	 
	
	@PostConstruct
	public void initData() {
		this.lang = NSIConstants.CODE_DEFAULT_LANG;
		lang=getUserData().getCurrentLang();
		try {

			if (JSFUtils.getRequestParameter(LANG) != null && !"".equals(JSFUtils.getRequestParameter(LANG))) {
				checkLang();
			}
			
		} catch (NumberFormatException e) {
			LOGGER.error("Подадени са невалидни параметри! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param"));
			
		} catch (Exception e) {
			LOGGER.error("Грешка инициализиране на дърво! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void markAll() {
		if (markedAll) {
			oficialTitle=false;
			longTitle=false;
			shortTitle=false;
			alternateTitle=false;
			include=false;
			alsoInclude=false;
			exclude=false;
			rules=false;
			comment=false;
			
			markedAll=false;	
		}else {
			oficialTitle=true;
			longTitle=true;
			shortTitle=true;
			alternateTitle=true;
			include=true;
			alsoInclude=true;
			exclude=true;
			rules=true;
			comment=true;
			
			markedAll=true;
		}
		
		
	}
	
	private void checkLang() {
		try {

			List<Integer> tmpList = new ArrayList<>();

			tmpList = getSystemData().getSysClassification(NSIConstants.CODE_CLASSIF_LANG, new Date(), 1)
					.stream()
					.map(SystemClassif::getCode)
					.collect(Collectors.toList());

			for (Integer lang : tmpList) {
				if(lang == Integer.valueOf(JSFUtils.getRequestParameter(LANG))) {
					this.lang = Integer.valueOf(JSFUtils.getRequestParameter(LANG));
				}
			}

		} catch (NumberFormatException e) {
			LOGGER.error("Подадени са невалидни параметри! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param"));
			
		} catch (Exception e) {
			LOGGER.error("Грешка при проверка на език! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

	}

	
	public void actionSearch() {
		try {
			if (nameSearch==null || nameSearch.isBlank() || nameSearch.length()<3) {
				 JSFUtils.addMessage("searchForm:classifName", FacesMessage.SEVERITY_ERROR, "Текста за търсене трябва да е поне 3 символа!");
				return;
			}
			if (oficialTitle==false && longTitle==false && shortTitle==false && alternateTitle==false && include==false && alsoInclude==false && exclude==false && rules==false && comment==false) {
				JSFUtils.addMessage("searchForm:panelCheckBoxes", FacesMessage.SEVERITY_ERROR, "Моля, изберете поне един от чекбоксовете!");
				return;
			}
			JPA.getUtil().runWithClose(() -> {
				SelectMetadata sm=new PositionSDAO(getUserData()).findPositionsByText(nameSearch, getLang(), oficialTitle, longTitle, shortTitle, alternateTitle, include, alsoInclude, exclude, rules, comment);
				resultList=new LazyDataModelSQL2Array(sm, "cl.IDENT,vl.IDENT,pl.POSITION_ID");  
			});
		} catch (BaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void actionClear() {
		resultList=null;
		
		nameSearch=null;
		
		oficialTitle=true;
		longTitle=false;
		shortTitle=false;
		alternateTitle=false;
		include=false;
		alsoInclude=false;
		exclude=false;
		rules=false;
		comment=false;
	}
  
	
	public String checkWhereFound(Object[] row) {
		if (oficialTitle && row[4].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.offiName");
		}
		if (longTitle && row[5].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.longTitle");
		}
		if (shortTitle && row[6].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.shortTitle");
		}
		if (alternateTitle && row[7].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.alternativeNames");
		}
		if (include && row[8].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.includes");
		}
		if (alsoInclude && row[9].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.alsoIncludes");
		}
		if (exclude && row[10].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.excludes");
		}
		if (rules && row[11].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.rules");
		}
		if (comment && row[12].toString().toLowerCase().contains(nameSearch.toLowerCase())) {
			return getMessageResourceString("labels", "position.comment");
		}
		return "";
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

	public LazyDataModelSQL2Array getResultList() {
		return resultList;
	}

	public void setResultList(LazyDataModelSQL2Array resultList) {
		this.resultList = resultList;
	}

	public String getNameSearch() {
		return nameSearch;
	}

	public void setNameSearch(String nameSearch) {
		this.nameSearch = nameSearch;
	}
	
	public String getNameSearchUpper() {
		return nameSearch.toUpperCase();
	}
	
	 

	public boolean isOficialTitle() {
		return oficialTitle;
	}

	public void setOficialTitle(boolean oficialTitle) {
		this.oficialTitle = oficialTitle;
	}

	public boolean isLongTitle() {
		return longTitle;
	}

	public void setLongTitle(boolean longTitle) {
		this.longTitle = longTitle;
	}

	public boolean isShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(boolean shortTitle) {
		this.shortTitle = shortTitle;
	}

	public boolean isAlternateTitle() {
		return alternateTitle;
	}

	public void setAlternateTitle(boolean alternateTitle) {
		this.alternateTitle = alternateTitle;
	}

	public boolean isInclude() {
		return include;
	}

	public void setInclude(boolean include) {
		this.include = include;
	}

	public boolean isAlsoInclude() {
		return alsoInclude;
	}

	public void setAlsoInclude(boolean alsoInclude) {
		this.alsoInclude = alsoInclude;
	}

	public boolean isExclude() {
		return exclude;
	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

	public boolean isRules() {
		return rules;
	}

	public void setRules(boolean rules) {
		this.rules = rules;
	}

	public boolean isComment() {
		return comment;
	}

	public void setComment(boolean comment) {
		this.comment = comment;
	}

	public boolean isMarkedAll() {
		return markedAll;
	}

	public void setMarkedAll(boolean markedAll) {
		this.markedAll = markedAll;
	}



}
