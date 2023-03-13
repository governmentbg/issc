package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.HashMap;
import java.util.Map;

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

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Метаданни на Ниво
 *
 * @author мамун
 */
@Entity
@Table(name = "LEVEL")
public class Level extends TrackableEntity implements AuditExt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937870471067755831L;
	
	@SequenceGenerator(name = "Level", sequenceName = "SEQ_LEVEL", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "Level")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "LEVEL_NUMBER")
	@JournalAttr(label = "levelNumber", defaultText = "Номер на ниво")
	private Integer levelNumber;
	
	@Column(name = "VERSION_ID")
	@JournalAttr(label = "versionId", defaultText = "ИД на версия")
	private Integer versionId;
	
	@Column(name = "LEVEL_NAME")
	@JournalAttr(label = "levelName", defaultText = "Име на ниво", classifID = "" + NSIConstants.CODE_CLASSIF_LEVEL_NAME)
	private Integer levelName;
	
	@Column(name = "SYMBOL_COUNT")
	@JournalAttr(label = "symbolCount", defaultText = "Брой символи в кода")
	private Integer symbolCount;
	
	@Column(name = "CODE_TYPE")
	@JournalAttr(label = "codeType", defaultText = "Тип на кода", classifID = "" + NSIConstants.CODE_CLASSIF_LEVEL_CODE_TYPE)
	private Integer codeType;
	
	@Column(name = "MASK_REAL")
	@JournalAttr(label = "maskReal", defaultText = "Формат на кода ")
	private String maskReal;
	
	@Column(name = "MASK_INHERIT")
	@JournalAttr(label = "maskInherit", defaultText = "Маска на наследяване на код")
	private String maskInherit;
	
	@Column(name = "POSITION_COUNT")
	@JournalAttr(label = "positionCount", defaultText = "Брой позиции")
	private Integer positionCount;
	
	
	
	@JournalAttr(label = "langMap", defaultText = "Езикови атрибути")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL,  orphanRemoval = true,targetEntity = LevelLang.class)
	@JoinColumn(name = "LEVEL_ID", referencedColumnName = "ID", nullable = false,insertable = false,updatable = false)
	@MapKey(name = "lang")
	private Map<Integer,LevelLang> langMap = new HashMap<Integer,LevelLang>();
	
	
	public Level() {
		super();		
	}
	



	public Level(Integer id, Integer levelNumber, Integer levelName) {
		super();
		this.id = id;
		this.levelNumber = levelNumber;
		this.levelName = levelName;
	}




	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	


	public Integer getLevelNumber() {
		return levelNumber;
	}


	public void setLevelNumber(Integer levelNumber) {
		this.levelNumber = levelNumber;
	}


	public Integer getVersionId() {
		return versionId;
	}


	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}


	public Integer getLevelName() {
		return levelName;
	}


	public void setLevelName(Integer levelName) {
		this.levelName = levelName;
	}


	public Integer getSymbolCount() {
		return symbolCount;
	}


	public void setSymbolCount(Integer symbolCount) {
		this.symbolCount = symbolCount;
	}


	public Integer getCodeType() {
		return codeType;
	}


	public void setCodeType(Integer codeType) {
		this.codeType = codeType;
	}


	public String getMaskReal() {
		return maskReal;
	}


	public void setMaskReal(String maskReal) {
		this.maskReal = maskReal;
	}


	public String getMaskInherit() {
		return maskInherit;
	}


	public void setMaskInherit(String maskInherit) {
		this.maskInherit = maskInherit;
	}


	public Integer getPositionCount() {
		return positionCount;
	}


	public void setPositionCount(Integer positionCount) {
		this.positionCount = positionCount;
	}


	public Map<Integer, LevelLang> getLangMap() {
		return langMap;
	}


	public void setLangMap(Map<Integer, LevelLang> langMap) {
		this.langMap = langMap;
	}
	
	@Override
	public String getIdentInfo() throws DbErrorException {	
		
		return "Ниво " + this.getLevelNumber();
	}


	@Override
	public Integer getCodeMainObject() {		
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_LEVEL;
	}


	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal j = new SystemJournal();
		j.setCodeObject(getCodeMainObject());
		j.setIdObject(getId());
		j.setIdentObject(getIdentInfo());
		
		j.setJoinedCodeObject1(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_LEVEL);
		j.setJoinedIdObject1(versionId);
		return j;
	}
	
	
	@Override
	public String toString() {
	    return "Level[id=" + id + "]";
	}
	
	
	
	
}
