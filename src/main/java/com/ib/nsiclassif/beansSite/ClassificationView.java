package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.util.ArrayList;
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
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.ClassificationAttributes;
import com.ib.nsiclassif.db.dto.ClassificationLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;

@Named
@RequestScoped
public class ClassificationView extends IndexUIbean implements Serializable {

	/**
	 * Разглеждане на статистическа класификация
	 * 
	 */
	private static final long serialVersionUID = 4955665421466533568L;	
	static final Logger LOGGER = LoggerFactory.getLogger(ClassificationView.class);
	
	private transient ClassificationDAO dao;
	private Classification classif;
	private ClassificationLang classifLang;

	private Integer lang;
	private Integer idClassif;
	
	private List<ClassificationAttributes> classAtrr;

	@PostConstruct
	public void initData() {
		System.out.println("classification init");
		this.dao = new ClassificationDAO(ActiveUser.DEFAULT);
		this.classifLang = new ClassificationLang();
		this.classAtrr = new ArrayList<>();		

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
					
					JPA.getUtil().runWithClose(() -> this.classif = this.dao.findById(this.idClassif));

					if (this.classif != null) {
						this.classAtrr = this.classif.getAttributes();
						
						actionChangeLang();
					} 
				}
			}
			
		} catch (NumberFormatException e) {
			LOGGER.error("Подаден е невалиден параметър! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param"));

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на данни за класификация!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

	}
	
	public void actionChangeLang() {

		if (this.classifLang != null && this.classifLang.getLang() != null) {
			this.classif.getLangMap().put(this.classifLang.getLang(), this.classifLang);
		}

		try {

			if (this.classif.getLangMap().containsKey(this.lang)) {
				this.classifLang = this.classif.getLangMap().get(this.lang);
			
			} else {
				
				ClassificationLang langTmp = new ClassificationLang();
				this.classif = new Classification();
				langTmp = this.classif.getLangMap().get(NSIConstants.CODE_DEFAULT_LANG);

				if (langTmp != null) {
					
					this.classifLang = langTmp.clone();
					this.classifLang.setId(null);
					this.classifLang.setClassif(this.classif);
					this.classifLang.setLang(this.lang);
				
				} else {
					
					this.classifLang = new ClassificationLang();
					this.classifLang.setLang(this.lang);
					this.classifLang.setClassif(this.classif);
				}
				
			}
		
		} catch (Exception e) {
			LOGGER.error("Грешка при промяна на език!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}	
	
	public Classification getClassif() {
		return classif;
	}

	public void setClassif(Classification classif) {
		this.classif = classif;
	}

	public ClassificationLang getClassifLang() {
		return classifLang;
	}

	public void setClassifLang(ClassificationLang classifLang) {
		this.classifLang = classifLang;
	}

	public Integer getLang() {
		System.out.println("classif getLang:"+lang);
		return lang;
	}

	public void setLang(Integer lang) {
		System.out.println("classif setLang:"+lang);
		this.lang = lang;
	}	
	
	public Integer getIdClassif() {
		return idClassif;
	}

	public void setIdClassif(Integer idClassif) {
		this.idClassif = idClassif;
	}

	public List<ClassificationAttributes> getClassAtrr() {
		return classAtrr;
	}

	public void setClassAtrr(List<ClassificationAttributes> classAtrr) {
		this.classAtrr = classAtrr;
	}

}
