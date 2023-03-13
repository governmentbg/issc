package com.ib.nsiclassif.beansSite;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.PublicationDAO;
import com.ib.nsiclassif.system.NSIConstants;

@Named("publExtControler")
@RequestScoped
public class PublExtControler extends IndexUIbean {

	/**
	 * @author IliaG
	 * @author IvanC
	 *
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -543996712160973766L;
	static final Logger LOGGER = LoggerFactory.getLogger(PublExtControler.class);
	private Integer publId=null;
	private Integer lang;
	private String locale;
	
	/**
	 *
	 */
	
	public String controlPublData(){
		
		this.locale = JSFUtils.getRequestParameter("locale");
		
		if (null != this.locale && this.locale.equalsIgnoreCase("en")) {
			this.lang= 2;
		}else {
			this.setLang(NSIConstants.CODE_DEFAULT_LANG);
		}
		
		String sect =JSFUtils.getRequestParameter("sect");
		if (sect != null && !sect.trim().isEmpty()){
			addSessionScopeAttribute("sect", sect);
			
			try {
				JPA.getUtil().runWithClose(() -> this.publId = new PublicationDAO(getUserData()).checkForSinglePublicInSect(Integer.valueOf(sect),this.lang));
				//if(this.publId!=null) {
					//if(this.publId.equals(-1)) {
						//return null;
					//} else {
						//String par= this.getPublId() +";"+1;
						//return "publExtData?faces-redirect=true&amp;idPubl="+par +"&amp;sect="+sect+"&amp;locale="+this.locale+"&amp;lang="+this.lang;
					//}
				//} 	
				if(null==this.publId) {
					return null;
				} else {
					String par= this.getPublId() +";"+1;
					return "publExtData?faces-redirect=true&amp;idPubl="+par +"&amp;sect="+sect+"&amp;locale="+this.locale+"&amp;lang="+this.lang;
			}
				 
				
			} catch (BaseException e) {
				LOGGER.error("Грешка при зареждане секцията! ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
			}
			
			
		} else {
			return null;
//			return "pageNotFound.jsf";
		}
		
	
	
		return null;
	}


	public Integer getPublId() {
		return publId;
	}


	public Integer setPublId(Integer publId) {
		this.publId = publId;
		return publId;
	}


	public Integer getLang() {
		return lang;
	}


	public void setLang(Integer lang) {
		this.lang = lang;
	}
	
	/**
	 * Method adds new key value pair into Session scope of JSF 2 app
	 * @param key - key for value to be associated with 
	 * @param value - value to put in.
	 */
	public final void addSessionScopeAttribute(String key, Object value){
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		if(session != null){
			session.setAttribute(key, value);
		}
	}


	public String getLocale() {
		return locale;
	}


	public void setLocale(String locale) {
		this.locale = locale;
	}

}
