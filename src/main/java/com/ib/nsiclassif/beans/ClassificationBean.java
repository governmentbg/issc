package com.ib.nsiclassif.beans;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.search.ClassificationSearch;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.utils.DateUtils;

/**
 * Управление на статистически класификации
 * 
 * @author s.arnaudova
 */

@Named("classifsList")
@ViewScoped
public class ClassificationBean extends IndexUIbean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5268649410943987172L;
	static final Logger LOGGER = LoggerFactory.getLogger(ClassificationBean.class);

	private ClassificationSearch classifSearch;
	private LazyDataModelSQL2Array classificationsList;

	private Integer lang;

	private List<SystemClassif> periodList;
	private Integer period;
	private Date dateFrom;
	private Date dateTo;

	private List<Object[]> versionsList;

	@PostConstruct
	public void initData() {

		LOGGER.info("ClassificationList init..."+getCurrentLang());
		this.classifSearch = new ClassificationSearch();
		this.lang = getCurrentLang();

		try {

			this.periodList = getSystemData()
					.getSysClassification(Constants.CODE_CLASSIF_PERIOD_NOFUTURE, new Date(), this.getCurrentLang())
					.stream().sorted((e1, e2) -> e1.getTekst().compareToIgnoreCase(e2.getTekst()))
					.collect(Collectors.toList());

			actionSearchClassifs();

		} catch (Exception e) {
			LOGGER.error("Грешка инициализиране на класификации! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

	}

	public void changePeriod() {
		if (this.period != null) {
			Date[] di;
			di = DateUtils.calculatePeriod(this.period);
			setDateFrom(di[0]);
			setDateTo(di[1]);
		} else {
			setDateFrom(null);
			setDateTo(null);
		}
	}

	public void actionClear() {
		dateFrom = null;
		dateTo = null;
		period = null;
		changePeriod();
		this.classifSearch = new ClassificationSearch();
		this.lang = NSIConstants.CODE_DEFAULT_LANG;
		actionSearchClassifs();

	}

	public void changeDate() {
		this.setPeriod(null);
	}

	public void actionSearchClassifs() {
		try {
			
			this.classifSearch.setLang(this.lang);
			this.classifSearch.buildQuery();
			this.classificationsList = new LazyDataModelSQL2Array(this.classifSearch, "ident ASC");

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на класификации! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void changeLang() {
		
		UserData ud = (UserData) getUserData();
		
		ud.setCurrentLang(lang.intValue()); //TODO da se smeni teku]iqt ezik
		
		actionSearchClassifs();
	}
	
	public void onRowToggle(Integer classifId) {
		try {

			JPA.getUtil().runWithClose(
					() -> this.versionsList = new VersionDAO(getUserData()).getClassifVersions(classifId, lang));

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на версии към статистическа класификация! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}

	public String actionGoto(Integer idObj) {
		return "classificationEdit.jsf?faces-redirect=true&idObj=" + idObj;
	}

	public String actionGotoNew() {
		return "classificationEdit.jsf?faces-redirect=true";
	}

	public String redirectToVersions(Integer idClassif) {
		
		if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idVersion"))) {
			Integer idObj = Integer.valueOf(JSFUtils.getRequestParameter("idObj"));

			return "versionEdit.jsf?faces-redirect=true&idClassif=" + idClassif + "&idObj=" + idObj;
		} else {
			return "versionEdit.jsf?faces-redirect=true&idClassif=" + idClassif;
		}
	}

	public String redirectToCopyVersion(Integer idClassif) {		
		
		return "versionCopy.jsf?faces-redirect=true&idClassif=" + idClassif;		
	}
	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public List<SystemClassif> getPeriodList() {
		return periodList;
	}

	public void setPeriodList(List<SystemClassif> periodList) {
		this.periodList = periodList;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public ClassificationSearch getClassifSearch() {
		return classifSearch;
	}

	public void setClassifSearch(ClassificationSearch classifSearch) {
		this.classifSearch = classifSearch;
	}

	public LazyDataModelSQL2Array getClassificationsList() {
		return classificationsList;
	}

	public void setClassificationsList(LazyDataModelSQL2Array classificationsList) {
		this.classificationsList = classificationsList;
	}

	public List<Object[]> getVersionsList() {
		return versionsList;
	}

	public void setVersionsList(List<Object[]> versionsList) {
		this.versionsList = versionsList;
	}

}
