package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.ib.indexui.system.Constants;
import com.ib.nsiclassif.system.NSIClassifAdapter;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Метаданни на Версия
 *
 * @author мамун
 */
@Entity
@Table(name = "version")
public class Version extends TrackableEntity implements AuditExt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937970471067755831L;
	
	@SequenceGenerator(name = "Version", sequenceName = "SEQ_VERSION", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "Version")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	
	@Column(name = "STATUS")
	@JournalAttr(label = "status", defaultText = "Статус", classifID = "" + NSIConstants.CODE_CLASSIF_VERSION_STATUS )
	private Integer status;
	
	@Column(name = "COPYRIGHT")
	@JournalAttr(label = "copyright", defaultText = "Авторско право", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_AVTORSKO_PRAVO )
	private Integer copyright;
	
	@Column(name = "RAZPROSTRANENIE")
	@JournalAttr(label = "razprostranenie", defaultText = "Разрешено разпространение", classifID = "" + NSIConstants.CODE_CLASSIF_DANE )
	private Integer razprostranenie;
	
	@Column(name = "POSITION_COUNT")
	@JournalAttr(label = "positionCount", defaultText = "Брой позиции")
	private Integer positionCount;
	
	@Column(name = "LEVEL_COUNT")
	@JournalAttr(label = "levelCount", defaultText = "Брой на нивата")
	private Integer levelCount;
	
	@Column(name = "EXPANDED_LEVEL")
	@JournalAttr(label = "expandedLevel", defaultText = "Ниво на развитие")
	private Integer expandedLevel;
	
	@Column(name = "ID_CLASS")
	@JournalAttr(label = "idClss", defaultText = "Класификация")
	private Integer idClss;
	
	@Column(name = "ID_NEXT_VER")
	@JournalAttr(label = "IdNextVer", defaultText = "Следваща версия")
	private Integer IdNextVer;
	
	@Column(name = "ID_PREV_VER")
	@JournalAttr(label = "idPrevVer", defaultText = "Предишна версия")
	private Integer idPrevVer;
	
	
	@Column(name = "CONFIRM_DATE")
	@JournalAttr(label = "confirmDate", defaultText = "Дата на утвърждаване", dateMask = "dd.MM.yyyy")
	private Date confirmDate;
	
	
	@Column(name = "RELEASE_DATE")
	@JournalAttr(label = "releaseDate", defaultText = "Дата на влизане в сила", dateMask = "dd.MM.yyyy")
	private Date releaseDate;
	
	@Column(name = "TERMINATION_DATE")
	@JournalAttr(label = "terminationDate", defaultText = "Дата на прекратяване", dateMask = "dd.MM.yyyy")
	private Date terminationDate;
	
	@Column(name = "FINALIZED")
	@JournalAttr(label = "finaliezd", defaultText = "Публикувана на сайта", classifID = "" + NSIConstants.CODE_CLASSIF_DANE)
	private Integer finalized;
	
	@Transient
	private boolean publicated;
	
	@JournalAttr(label = "langMap", defaultText = "Езикови атрибути")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL,  orphanRemoval = true,targetEntity = VersionLang.class)
	@JoinColumn(name = "VERSION_ID", referencedColumnName = "ID", nullable = false,insertable = false,updatable = false)
	@MapKey(name = "lang")
	private Map<Integer,VersionLang> langMap = new HashMap<Integer,VersionLang>();
	
	
	public Version() {
		super();		
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	

	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public Integer getCopyright() {
		return copyright;
	}


	public void setCopyright(Integer copyright) {
		this.copyright = copyright;
	}


	public Integer getRazprostranenie() {
		return razprostranenie;
	}


	public void setRazprostranenie(Integer razprostranenie) {
		this.razprostranenie = razprostranenie;
	}


	public Integer getPositionCount() {
		return positionCount;
	}


	public void setPositionCount(Integer positionCount) {
		this.positionCount = positionCount;
	}


	public Integer getLevelCount() {
		return levelCount;
	}


	public void setLevelCount(Integer levelCount) {
		this.levelCount = levelCount;
	}


	public Integer getExpandedLevel() {
		return expandedLevel;
	}


	public void setExpandedLevel(Integer expandedLevel) {
		this.expandedLevel = expandedLevel;
	}


	public Integer getIdClss() {
		return idClss;
	}


	public void setIdClss(Integer idClss) {
		this.idClss = idClss;
	}


	public Integer getIdNextVer() {
		return IdNextVer;
	}


	public void setIdNextVer(Integer idNextVer) {
		IdNextVer = idNextVer;
	}


	public Integer getIdPrevVer() {
		return idPrevVer;
	}


	public void setIdPrevVer(Integer idPrevVer) {
		this.idPrevVer = idPrevVer;
	}


	public Date getConfirmDate() {
		return confirmDate;
	}


	public void setConfirmDate(Date confirmDate) {
		this.confirmDate = confirmDate;
	}


	public Date getReleaseDate() {
		return releaseDate;
	}


	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}


	public Date getTerminationDate() {
		return terminationDate;
	}


	public void setTerminationDate(Date terminationDate) {
		this.terminationDate = terminationDate;
	}


	public Map<Integer, VersionLang> getLangMap() {
		return langMap;
	}


	public void setLangMap(Map<Integer, VersionLang> langMap) {
		this.langMap = langMap;
	}
	
	
	@Override
	public String getIdentInfo() throws DbErrorException {
		String identInfo = "Id: "+this.id; 
		if (getLangMap() != null && getLangMap().size() > 0) {
			VersionLang deffLang = getLangMap().get(NSIConstants.CODE_DEFAULT_LANG) ;
			if (deffLang == null) {
				deffLang = getLangMap().entrySet().iterator().next().getValue();
			}
			identInfo = deffLang.getIdent();
		}
		
		return identInfo;
	}


	@Override
	public Integer getCodeMainObject() {		
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION;
	}


	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal j = new SystemJournal();
		j.setCodeObject(getCodeMainObject());
		j.setIdObject(getId());
		j.setIdentObject(getIdentInfo());		
		j.setJoinedCodeObject1(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_CLASSIF);
		j.setJoinedIdObject1(idClss);
		
		return j;
	}
	
	
	
	public TreeHolder toTreeHolder() {
		
		TreeHolder th = new TreeHolder();
		th.setIdParent(idClss);
		th.setCodeObject(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION);
		th.setIdObject(id);
		
		th.setActive((status!=null && status.intValue() == NSIConstants.CODE_ZNACHENIE_VER_STATUS_ACTIVE?true:false));
		
		if (id.intValue() == 3397) {
			System.out.println("Test");
			//Класификацията трябва да е 179
		}
		
		Iterator<Entry<Integer, VersionLang>> it = langMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, VersionLang> entry = it.next();
			VersionLang value = entry.getValue();
			TreeHolderLang thl = new TreeHolderLang();
			thl.setIdent(value.getIdent().replace("\n", ""));
			thl.setName(value.getTitle().replace("\n", ""));
			thl.setLang(value.getLang());
			
			th.getLangMap().put(thl.getLang(), thl);
		}
		
		return th;
		
	}


	public Integer getFinalized() {
		return finalized;
	}


	public void setFinalized(Integer finalized) {
		this.finalized = finalized;
	}
	
	public boolean getPublicated() {
		if (finalized==null || finalized==Constants.CODE_ZNACHENIE_NE) {
			publicated=false;
		}else {
			publicated=true;
		}
		return publicated;
	}


	public void setPublicated(boolean publicated) {
		this.publicated = publicated;
		if (publicated) {
			finalized=Constants.CODE_ZNACHENIE_DA;			
		}else {
			finalized=Constants.CODE_ZNACHENIE_NE;
		}
	}
}
