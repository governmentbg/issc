package com.ib.nsiclassif.beansSite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.search.TreeSearch;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.dto.SystemClassif;

@Named("classificationsListBean")
@ViewScoped
public class ClassificationsListBean extends IndexUIbean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7687755605770835150L;
	static final Logger LOGGER = LoggerFactory.getLogger(ClassificationsListBean.class);
	
	//private static final String SEARCHSTRING = "searchString";
	private static final String LANG = "lang";
	private static final String ACTIVE_VERSION = "activeVersion";
	
	private transient TreeSearch treeSearch;

	private Integer lang;
	//private String searchString;
	
	private String ident;
	private String nameC;
	
	private Boolean activeVersion;
	
	
	@PostConstruct
	public void initData() {
		
		LOGGER.info("ClassificationsListBean init");
		
		this.treeSearch = new TreeSearch();
		this.lang = NSIConstants.CODE_DEFAULT_LANG;

		try {

			if (JSFUtils.getRequestParameter(LANG) != null && !"".equals(JSFUtils.getRequestParameter(LANG))) {				
				checkLang();
			}
			
			if (JSFUtils.getRequestParameter("ident") != null && !"".equals(JSFUtils.getRequestParameter("ident"))) {
				this.ident = JSFUtils.getRequestParameter("ident");
			}
			
			if (JSFUtils.getRequestParameter("nameC") != null && !"".equals(JSFUtils.getRequestParameter("nameC"))) {
				this.nameC = JSFUtils.getRequestParameter("nameC");
	
			}
			
			activeVersion = Boolean.valueOf(true);
			if (JSFUtils.getRequestParameter(ACTIVE_VERSION)!=null) {
				if("false".equals(JSFUtils.getRequestParameter(ACTIVE_VERSION))) {
					activeVersion = Boolean.valueOf(false);
				}
			}
			
			treeSearch.setActiveVersion(activeVersion.booleanValue());
			treeSearch.init();
			treeSearch.setLang(lang);
			
			if ((this.ident != null && !this.ident.isEmpty()) || (this.nameC != null && !this.nameC.isEmpty())) {
				searchTree();
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

	private void searchTree() {
		try {
			treeSearch.setActiveVersion(activeVersion.booleanValue());
			
			this.treeSearch.fullTree = this.treeSearch.buildTree(this.ident, this.nameC, this.lang);

		} catch (Exception e) {
			LOGGER.error("Грешка при търсене на статистически класификации! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}

	public String actionClear() {

		//this.searchString = "";
		this.ident = "";
		this.nameC = "";
		searchTree();
		
		return "classificationsList?faces-redirect=true";
	}

	public String checkString(String value,  Integer key) {
		
		String mask = ident;
		if (key == 1) {
			mask = nameC;
		}
		
		try {

			if (value != null && !value.isEmpty()) {

				String[] wordsInSearchString = mask.split(" ");
				String[] wordsInValue = value.split(" ");

				for (String word : wordsInSearchString) {
					Pattern pattern = Pattern.compile("(" + word + ")",
							Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

					for (String wordInValue : wordsInValue) {
						Matcher matcher = pattern.matcher(value);

						while (matcher.find()) {
							value = value.replaceAll(matcher.group(), String.format("<b>%s</b>", matcher.group()));
						}
					}
				}
			}

		} catch (Exception e) {
			LOGGER.error("Грешка при маркиране на съвпадащи резултати! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return value;
	}
	

	public TreeSearch getTreeSearch() {
		return treeSearch;
	}

	public void setTreeSearch(TreeSearch treeSearch) {
		this.treeSearch = treeSearch;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	//public String getSearchString() {
		//return searchString;
	//}

	//public void setSearchString(String searchString) {
		//this.searchString = searchString;
	//}

	public Boolean getActiveVersion() {
		return activeVersion;
	}

	public void setActiveVersion(Boolean activeVersion) {
		this.activeVersion = activeVersion;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getNameC() {
		return nameC;
	}

	public void setNameC(String nameC) {
		this.nameC = nameC;
	}

}
