package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.CorespTableLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;


/**
 * Разглеждане на основни данни на кореспондираща таблица
 * 
 */

@Named("corespTableView")
@RequestScoped
public class CorespTableViewBean extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8931178899066275903L;
	static final Logger LOGGER = LoggerFactory.getLogger(CorespTableViewBean.class);

	private static final String IDOBJ = "idObj";
	private static final String LANG = "lang";

	private transient CorespTableDAO corespDao;
	private transient VersionDAO versionDao;
	private CorespTable corespTable;
	private CorespTableLang corespTableLang;

	private List<Object[]> versionsList;

	private Integer idClassif;
	private Integer lang;
	private Integer idObj;
	private String decodedText;
	
	private String panelInfo;

	@PostConstruct
	void initData() {
		System.out.println("CorrespTableViewBean init");

		this.corespDao = new CorespTableDAO(ActiveUser.DEFAULT);
		this.corespTable = new CorespTable();
		this.corespTableLang = new CorespTableLang();
		this.versionDao = new VersionDAO(ActiveUser.DEFAULT);
		this.lang = NSIConstants.CODE_DEFAULT_LANG;

		try {

			if (JSFUtils.getRequestParameter(IDOBJ) != null && !"".equals(JSFUtils.getRequestParameter(IDOBJ))) {
				this.idObj = Integer.valueOf(JSFUtils.getRequestParameter(IDOBJ));

				if (JSFUtils.getRequestParameter(LANG) != null && !"".equals(JSFUtils.getRequestParameter(LANG))) {
					checkLang();
				} 

				searchCorespTable();

			}

		} catch (NumberFormatException e) {
			LOGGER.error("Подадени са невалидни параметри! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param"));
			
		} catch (Exception e) {
			LOGGER.error("Грешка инициализиране кореспондираща таблица! ", e);
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

	private void searchCorespTable() {
		try {

			JPA.getUtil().runWithClose(() -> this.corespTable = this.corespDao.findById(idObj));

			if (this.corespTable != null) {
				
				if (this.corespTable.getLangMap().containsKey(this.lang)) {
					this.corespTableLang = corespTable.getLangMap().get(this.lang);
				}
				
				//попълване на информативният панел
				JPA.getUtil().runWithClose(() -> panelInfo =  new CorespTableDAO(ActiveUser.DEFAULT).decodeCorrespTableIdent(idObj, lang));
				
			} else {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param")); //няма намерено ид
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на кореспондираща таблица!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public String actionDecode(Integer versionId, Integer lang) {
		try {

			JPA.getUtil().runWithClose(() ->  this.decodedText =  this.versionDao.decodeVersionIdent(versionId, lang));

		} catch (Exception e) {
			LOGGER.error("Грешка при разкодиране на идентификатор!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
		return decodedText;
	}
	

	public CorespTableDAO getCorespDao() {
		return corespDao;
	}

	public void setCorespDao(CorespTableDAO corespDao) {
		this.corespDao = corespDao;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getIdObj() {
		return idObj;
	}

	public void setIdObj(Integer idObj) {
		this.idObj = idObj;
	}

	public CorespTable getCorespTable() {
		return corespTable;
	}

	public void setCorespTable(CorespTable corespTable) {
		this.corespTable = corespTable;
	}

	public CorespTableLang getCorespTableLang() {
		return corespTableLang;
	}

	public void setCorespTableLang(CorespTableLang corespTableLang) {
		this.corespTableLang = corespTableLang;
	}

	public List<Object[]> getVersionsList() {
		return versionsList;
	}

	public void setVersionsList(List<Object[]> versionsList) {
		this.versionsList = versionsList;
	}

	public Integer getIdClassif() {
		return idClassif;
	}

	public void setIdClassif(Integer idClassif) {
		this.idClassif = idClassif;
	}

	public VersionDAO getVersionDao() {
		return versionDao;
	}

	public void setVersionDao(VersionDAO versionDao) {
		this.versionDao = versionDao;
	}

	public String getDecodedText() {
		return decodedText;
	}

	public void setDecodedText(String decodedText) {
		this.decodedText = decodedText;
	}

	public String getPanelInfo() {
		return panelInfo;
	}

	public void setPanelInfo(String panelInfo) {
		this.panelInfo = panelInfo;
	}

}
