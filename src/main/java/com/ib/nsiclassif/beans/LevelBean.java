package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.LevelDAO;
import com.ib.nsiclassif.db.dto.Level;
import com.ib.nsiclassif.db.dto.LevelLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.BaseException;
import com.ib.system.utils.SearchUtils;

/**
 * таб "Нива" - актуализация на класификационни нива
 * 
 * @author s.arnaudova
 */

@Named("level")
@ViewScoped
public class LevelBean extends IndexUIbean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6720213606907585798L;
	static final Logger LOGGER = LoggerFactory.getLogger(LevelBean.class);

	public static final String FORM = "versionEditForm:tabsVersion";
	private static final String SUCCESSDELETEMSG = "general.successDeleteMsg";

	private transient LevelDAO dao;
	private Level level;
	private LevelLang levelLang;

	
	private Integer lang;
	private Integer versionId;

	private List<Level> levelsInVersionList;
	
	private String versionIdent; 
	
	
	public void initData() {
		
		
		VersionEditBean mainBean = (VersionEditBean) JSFUtils.getManagedBean("versionEdit");
		
		lang = mainBean.getLang(); 
		if(lang==null) {lang =NSIConstants.CODE_DEFAULT_LANG; }
		
		LOGGER.info("INIT LEVELS....lang->"+lang); 
		
		if (mainBean != null && !Objects.equals(mainBean.getVersionId(), this.versionId)) { // ако вече сме отваряли
																							// табчето не го зареждаме
																							// отново
			this.dao = new LevelDAO(getUserData());
			this.levelLang = new LevelLang();
			
			try {
				versionId = mainBean.getVersionId();
				versionIdent =  mainBean.getVersionLang().getIdent();
				
				loadLevelsInVersionList(versionId);
				
			} catch (Exception e) {
				LOGGER.error("Грешка при инициализиране на нива на йерархия!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
		}
		
		actionNewLevel(); //vinagi inicializirame now pri otwarqne na taba

	}

	private void loadLevelsInVersionList(Integer versionId) {
		try {

			JPA.getUtil().runWithClose(() -> this.levelsInVersionList = this.dao.levelsInVersionList(versionId));

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на нива във версия!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}

	
	public void actionSave() {
		try {

			if (checkData()) {
				level.getLangMap().put(levelLang.getLang(), levelLang);

				if (!SearchUtils.isEmpty(this.level.getMaskReal())) {
					this.level.setSymbolCount(this.level.getMaskReal().length());
				}
				
				JPA.getUtil().runInTransaction(() -> this.level = this.dao.save(this.level));

				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));

				loadLevelsInVersionList(versionId); // презарежда списъка на екрана
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при запис на ниво!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}

	private boolean checkData() {
		boolean flag = true;

		if (this.level.getLevelNumber() == null) {
			JSFUtils.addMessage(FORM + ":levelNumber", FacesMessage.SEVERITY_ERROR, getMessageResourceString(
					UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "levelsEdit.levelNum")));
			flag = false;
		}

		if (this.level.getLevelName() == null) {
			JSFUtils.addMessage(FORM + ":levelName", FacesMessage.SEVERITY_ERROR, getMessageResourceString(
					UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "levelsEdit.name")));
			flag = false;
		}
		
		if (SearchUtils.isEmpty(this.level.getMaskReal())) {
			JSFUtils.addMessage(FORM + ":maskReal", FacesMessage.SEVERITY_ERROR, getMessageResourceString(
					UI_beanMessages, MSGPLSINS, getMessageResourceString(LABELS, "levelsEdit.maskReal")));
			flag = false;
		}

		if (!SearchUtils.isEmpty(this.level.getMaskReal())) { 
			
			if (validateMaskReal(this.level.getMaskReal())) {
				JSFUtils.addMessage(FORM + ":maskReal", FacesMessage.SEVERITY_ERROR, "Невалиден символ на маска!");
				flag = false;
			}
		}

		return flag;
	}
	
	/**
	 * Валидира маската по следните правила: 9 - за цифров код в позиция; C - за
	 * буквено-цифров код; L - за голяма латиница; К - за голяма кирилица; l - за
	 * малка латиница; к - за малка латиница; X – не се контролира позицията в кода;
	 * 
	 * 
	 * Шаблонът е взет от старата система
	 */
	private boolean validateMaskReal(String mask) {
		boolean valid = false;
		String shablon = "9CLКlкX!@#$%^&*()_+=-][}{\\';|\\\":/\\\\.,?>< ";

		for (int i = 0; i < mask.length(); i++) {

			if (shablon.indexOf(mask.substring(i, i + 1)) == -1) {
				return true; // невалидна маска
			}
		}
		return valid;
	}

	public void actionChangeLang() {
		//сменя езика в основният бийн на табовете
		
		VersionEditBean mainBean = (VersionEditBean) JSFUtils.getManagedBean("versionEdit");
		
		System.out.println("lang versionBean"+mainBean.getLang());
		mainBean.setLang(lang);
		mainBean.changeLang();
		
		System.out.println("lang versionBeanSet"+mainBean.getLang());
		
		//((UserData) getUserData()).setCurrentLang(lang.intValue()); //TODO da se smeni teku]iqt ezik
		
		if (this.levelLang != null && this.levelLang.getLang() != null && this.level != null) {
			level.getLangMap().put(levelLang.getLang(), levelLang);
		}

		try {

			if (this.level.getLangMap().containsKey(lang)) {
				this.levelLang = level.getLangMap().get(lang);
			} else {

				LevelLang langTmp = new LevelLang();
				langTmp = this.level.getLangMap().get(NSIConstants.CODE_DEFAULT_LANG);

				if (langTmp != null) {
					this.levelLang = langTmp.clone();
					this.levelLang.setId(null);
					this.levelLang.setLang(lang);
					this.levelLang.setLevel(this.level); 
				} else {
					this.levelLang = new LevelLang();
					this.levelLang.setLang(lang);
					this.levelLang.setLevel(this.level); 
				}
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при промяна на език!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void actionNewLevel() {

		this.level = new Level();
		this.levelLang = new LevelLang();
		this.level.setVersionId(versionId);

		this.levelLang.setLevel(level);
		this.levelLang.setLang(lang);
		
		this.level.setLevelNumber(this.levelsInVersionList.size() + 1);

	}
	
	public void actionEdit(Integer idObj) {

		try {

			if (idObj != null) {

				JPA.getUtil().runWithClose(() -> this.level = this.dao.findById(idObj));
			
				if (this.level.getLangMap().containsKey(this.lang)) {
					this.levelLang = level.getLangMap().get(this.lang);
				} else {
					this.levelLang = new LevelLang();
					this.levelLang.setLevel(level);
					this.levelLang.setLang(lang);
					this.level.getLangMap().put(this.lang, this.levelLang);
				}
			}
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на ниво! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG));
		}

	}
	
	public void actionDelete() {
		
		try {
			
			JPA.getUtil().runInTransaction(() -> this.dao.delete(this.level));
			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSDELETEMSG));
			
			loadLevelsInVersionList(versionId);
			
			actionNewLevel();
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на ниво! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG));
		}
	}

	
	public void actionDeleteRow(Integer idObj) {

		try {

			if (idObj != null) {

				JPA.getUtil().runInTransaction(() -> this.dao.deleteById(idObj));
			}
			

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSDELETEMSG));
						
			loadLevelsInVersionList(versionId);
			
			actionNewLevel();
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на ниво! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG));
		}

	}

	public LevelDAO getDao() {
		return dao;
	}

	public void setDao(LevelDAO dao) {
		this.dao = dao;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public LevelLang getLevelLang() {
		return levelLang;
	}

	public void setLevelLang(LevelLang levelLang) {
		this.levelLang = levelLang;
	}

	public Integer getLang() {
		return lang;
	}

	public List<Level> getLevelsInVersionList() {
		return levelsInVersionList;
	}

	public void setLevelsInVersionList(List<Level> levelsInVersionList) {
		this.levelsInVersionList = levelsInVersionList;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	public String getVersionIdent() {
		return versionIdent;
	}

	public void setVersionIdent(String versionIdent) {
		this.versionIdent = versionIdent;
	}

	
}
