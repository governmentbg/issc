package com.ib.nsiclassif.beans;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
//import com.ib.system.exceptions.DbErrorException;
//import com.ib.system.exceptions.ObjectNotFoundException;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.system.db.SelectMetadata;
import com.ib.system.utils.DateUtils;
import com.ib.indexui.utils.JSFUtils;

import com.ib.nsiclassif.db.dto.Publication;
import com.ib.nsiclassif.db.dao.PublicationDAO;
/*import com.ib.nsiclassif.db.dao.PublicationLangDAO;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.indexui.system.Constants;*/
import com.ib.indexui.system.IndexUIbean;

@Named("publDataList")
@ViewScoped
public class PublicationList extends IndexUIbean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -627735156023066059L;
	/**
	 * Основен javaBean клас за търсене на публикации по зададени критерии за
	 * търсене и за обръщане към javaBean клас за въвеждане/актуализация
	 */

	static final Logger LOGGER = LoggerFactory.getLogger(PublicationList.class);
	private Integer idUser = null;
	private String sectionText = null;
//	private Integer codeSection=null;
	private List<Integer> codeSections = new ArrayList<Integer>();
	private Date dateFrom = null;
	private Date dateTo = null;
	private String annotation = null;
	private Integer period = null;
	private LazyDataModelSQL2Array pubListT = null;
	private Publication selectedPubl = null;
	private String titleF = null;
	private List<Object[]> pubLangList = new ArrayList<Object[]>();
	private Integer lang = getCurrentLang();
	private Boolean codeNoLang = false;
	private Integer pageHidden;
	SelectMetadata smd = null;
	/** за конкретната система */
	/*
	 * public static final String beanMessages = "beanMessages";
	 *//** за конкретната система *//*
									 * public static final String LABELS = "labels";
									 */

	/**
	 * Инициира/сетва първоначалните стойности на атрибутите на филтъра за търсене.
	 * Чете предадените параметри от други екрани
	 */
	@PostConstruct
	public void initData() {

		try {

			this.idUser = getUserData().getUserId();

			@SuppressWarnings("unchecked")
			Map<String, Object> params = (Map<String, Object>) getSessionScopeValue("publListFindAttr");
			String filt = JSFUtils.getRequestParameter("filtCl");
			if (params != null && null == filt) {
				if (null != params.get("codeSections"))
					this.codeSections = (List<Integer>) params.get("codeSections");

				/*
				 * if (null!=params.get("codeSection"))
				 * this.codeSection=(Integer)params.get("codeSection");
				 */

				if (null != params.get("dateFrom"))
					this.dateFrom = (Date) params.get("dateFrom");

				if (null != params.get("dateTo"))
					this.dateTo = (Date) params.get("dateTo");

				if (null != params.get("titleF"))
					this.titleF = (String) params.get("titleF");

				if (null != params.get("langF"))
					this.lang = (Integer) params.get("langF");

				if (null != params.get("codeNoLangF"))
					this.codeNoLang = (Boolean) params.get("codeNoLangF");

				if (null != getSessionScopeValue("sectionText"))
					this.sectionText = (String) getSessionScopeValue("sectionText");

				if (null != getSessionScopeValue("period"))
					this.period = (Integer) getSessionScopeValue("period");

				actionFind();

				if (getSessionScopeValue("publListPage") != null) {

					DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
							.findComponent("formPublList:tablePubl");
					int page = (int) getSessionScopeValue("publListPage");
					if (d != null) {
						page = (int) getSessionScopeValue("publListPage");
						d.setFirst(page);
					}
				}

			}

			/*
			 * }catch (ObjectNotFoundException e) {
			 * JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
			 * getMessageResourceString(beanMessages,"general.objectNotFound"),
			 * e.getMessage()); LOGGER.error(e.getMessage(), e); this.idUser = -1;
			 */
		} catch (Exception e) {
			LOGGER.error("Грешка при работа със системата!!!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "general.exception"));
		} finally {
			JPA.getUtil().closeConnection();
		}

	}

	/**
	 * Изтрива стойностите на филтъра за търсене на публикациите
	 * 
	 */
	public void actionClear() {
		this.sectionText = null;
//		this.codeSection=null;
		this.codeSections = new ArrayList<Integer>();
		this.dateFrom = null;
		this.dateTo = null;
		this.period = null;
		this.setSelectedPubl(new Publication());
		this.pubListT = null;
		this.titleF = null;
		this.lang = null;
		if (null == this.lang)
			this.lang = getCurrentLang();

		// махаме запазените параметри от сесията
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		session.removeAttribute("publListPage");
		session.removeAttribute("publListFindAttr");
		session.removeAttribute("sectionText");
		session.removeAttribute("period");

		DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
				.findComponent("formPublList:tablePubl");
		if (null != d)
			d.setFirst(0);
	}

	public Integer getIdUser() {
		return idUser;
	}

	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}

	public String getSectionText() {
		return sectionText;
	}

	public void setSectionText(String sectionText) {
		this.sectionText = sectionText;
	}

	/*
	 * public Integer getCodeSection() { return codeSection; }
	 * 
	 * public void setCodeSection(Integer codeSection) { this.codeSection =
	 * codeSection; }
	 */

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

	/**
	 * Проверява дата от и дата до на публикациите
	 * 
	 * @param nom
	 */
	public boolean checkDates(int nom) {
		boolean rez = true;
		this.setPeriod(null);
		if (this.getDateFrom() != null && this.getDateTo() != null) {
			if (this.getDateFrom().compareTo(this.getDateTo()) > 0) {
				rez = false;

				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(beanMessages, "section.dateFromLessDateTo"));
			}
		}
		return rez;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public Date getCurrentDate() {
		return new Date();
	}

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public void changePeriod() {
		if (this.period != null) {
			Date[] di;
			di = DateUtils.calculatePeriod(this.period);
			this.setDateFrom(di[0]);
			this.setDateTo(di[1]);
		} else {
			this.setDateFrom(null);
			this.setDateTo(null);
		}
		return;
	}

	/**
	 * Метод за търсене в БД на публикации по зададените критерии/филтър
	 * 
	 */
	public void actionFind() {

		this.pubListT = null;

		PublicationDAO publDao = new PublicationDAO(getUserData());

		try {

			JPA.getUtil().runWithClose(() -> {
				smd = publDao.findPublFilter(this.dateFrom, this.dateTo, this.getCodeSections(), this.titleF, this.lang,
						this.codeNoLang);
				String sortCol = "A2 DESC";
				this.pubListT = publDao.newLazyDataModel(smd, sortCol);

			});

			this.pubListT.getRowCount();

			Map<String, Object> params = new HashMap<String, Object>();

			if (null != this.getCodeSections())
				params.put("codeSections", this.codeSections);

			/*
			 * if (null != this.getCodeSection()) params.put("codeSection",
			 * this.codeSection);
			 */

			if (null != this.getDateFrom())
				params.put("dateFrom", this.dateFrom);

			if (null != this.getDateTo())
				params.put("dateTo", this.dateTo);

			if (null != this.getTitleF())
				params.put("titleF", this.titleF);

			if (null != this.getLang())
				params.put("langF", this.lang);

			if (null != this.getCodeNoLang())
				params.put("codeNoLangF", this.codeNoLang);

			addSessionScopeAttribute("publListFindAttr", params);

			if (null != this.getSectionText())
				addSessionScopeAttribute("sectionText", this.sectionText);
			if (null != this.getPeriod())
				addSessionScopeAttribute("period", this.period);

		} catch (BaseException e) {
			LOGGER.error("Грешка при четене на Секция! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Грешка при работа със системата!!!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "general.exception"));
		}
		return;
	}

	public LazyDataModelSQL2Array getPubListT() {
		return pubListT;
	}

	public void setPubListT(LazyDataModelSQL2Array pubListT) {
		this.pubListT = pubListT;
	}

	public void prepareGo() {
		this.pubListT.getResult().clear();
	}

	/**
	 * Метод за скалиране/редуциране на размера на изображенията, показвани в
	 * таблицата на намерените публикации по зададен филтър на търсене в БД
	 * 
	 * @param ba
	 * @param proc
	 * @return
	 */
	public StreamedContent createImageCont(byte[] ba, int proc) {
		StreamedContent imagCont = null;

		if (null != ba) {

			try {

				ByteArrayInputStream inS = new ByteArrayInputStream(ba);
				BufferedImage img = ImageIO.read(inS);
				int h = img.getHeight();
				int w = img.getWidth();
				h = (img.getHeight() * proc) / 100;
				w = (img.getWidth() * proc) / 100;

				Image scaledImage = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
				BufferedImage imageBuff = new BufferedImage(w, h, img.getType());
				imageBuff.getGraphics().drawImage(scaledImage, 0, 0, null);

				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				ImageIO.write(imageBuff, "jpeg", buffer);
				buffer.flush();
				imageBuff.flush();
				byte[] resizeBa = buffer.toByteArray();
				buffer.close();

				InputStream imageStream = new ByteArrayInputStream(resizeBa);
//	            imagCont = new DefaultStreamedContent(imageStream, "image/jpeg");
				imagCont = DefaultStreamedContent.builder().contentType("image/jpeg").name(null)
						.stream(() -> imageStream).build();
				imageStream.close();

			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при показване на изображение!",
						e.getMessage());
			}
		}
		return imagCont;
	}

	public Publication getSelectedPubl() {
		return selectedPubl;
	}

	public void setSelectedPubl(Publication selectedPubl) {
		this.selectedPubl = selectedPubl;
	}

	/*
	 * public Integer getParam() { return param; }
	 * 
	 * 
	 * 
	 * public void setParam(Integer param) { this.param = param; }
	 */

	public String getTitleF() {
		return titleF;
	}

	public void setTitleF(String titleF) {
		this.titleF = titleF;
	}

	/*
	 * public void findPublBySection(Integer codeSect){
	 * 
	 * try {
	 * 
	 * this.pubLangList = new
	 * PublicationLangDAO(this.idUser).findPublLangBySect(codeSect, new Date(),
	 * getCurrentLang());
	 * 
	 * } catch (DbErrorException e) { LOGGER.error(e.getMessage(), e);
	 * JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
	 * getMessageResourceString(beanMessages,"general.errDataBaseMsg"),
	 * e.getMessage());
	 * 
	 * } finally { JPA.getUtil().closeConnection(); } }
	 */

	public List<Object[]> getPubLangList() {
		return pubLangList;
	}

	public void setPubLangList(List<Object[]> pubLangList) {
		this.pubLangList = pubLangList;
	}

	public String actionGoto(Integer idPubl) {

		/*
		 * FacesContext context = FacesContext.getCurrentInstance(); HttpSession session
		 * = (HttpSession) context.getExternalContext().getSession(false);
		 * 
		 * session.removeAttribute("publListPage");
		 * 
		 * DataTable d = (DataTable)
		 * FacesContext.getCurrentInstance().getViewRoot().findComponent(
		 * "formPublList:tablePubl");
		 * 
		 * if(d != null) { int a= d.getFirst(); addSessionScopeAttribute("publListPage",
		 * d.getFirst()); }
		 */

		return "publData.jsf?faces-redirect=true&idPubl=" + idPubl + "&langF=" + this.lang;

	}

	public void changePage() {

		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

		session.removeAttribute("publListPage");

		DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
				.findComponent("formPublList:tablePubl");

		if (d != null) {
			int a = d.getFirst();
			addSessionScopeAttribute("publListPage", d.getFirst());
		}
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Boolean getCodeNoLang() {
		return codeNoLang;
	}

	public void setCodeNoLang(Boolean codeNoLang) {
		this.codeNoLang = codeNoLang;
	}

	public Integer getPageHidden() {
		if (pageHidden == null) {

			pageHidden = 1;

			if (getSessionScopeValue("publListPage") != null) {

				DataTable d = (DataTable) FacesContext.getCurrentInstance().getViewRoot()
						.findComponent("formPublList:tablePubl");

				if (d != null) {
					int page = (int) getSessionScopeValue("publListPage");
					d.setFirst(page);
				}
			}
		}

		return pageHidden;
	}

	public void setPageHidden(Integer pageHidden) {
		this.pageHidden = pageHidden;
	}

	/**
	 * @return the codeSections
	 */
	public List<Integer> getCodeSections() {
		return codeSections;
	}

	/**
	 * @param codeSections the codeSections to set
	 */
	public void setCodeSections(List<Integer> codeSections) {
		this.codeSections = codeSections;
	}

	/**
	 * Method adds new key value pair into Session scope of JSF 2 app
	 * 
	 * @param key   - key for value to be associated with
	 * @param value - value to put in.
	 */
	public final void addSessionScopeAttribute(String key, Object value) {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		if (session != null) {
			session.setAttribute(key, value);
		}
	}

	/**
	 * Method returns value added to session scope and if it exists it will be
	 * removed and returned
	 * 
	 * @param key - key of the value
	 * @return - value
	 */
	public final Object getSessionScopeValue(String key) {
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		if (session == null) {
			return null;
		} else {
			Object value = session.getAttribute(key);
			// session.removeAttribute(key); - ne bi trqbvalo da se maha
			return value;
		}
	}

}
