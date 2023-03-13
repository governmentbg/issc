package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Метаданни на Схема
 *
 * @author мамун
 */

@SqlResultSetMapping(name = "loadScheme", classes = {
			@ConstructorResult(targetClass = SchemeItem.class, columns = { 
					@ColumnResult(name = "ID", type = Integer.class), 
					@ColumnResult(name = "CODE", type = String.class),
					@ColumnResult(name = "DEFF_NAME", type = String.class), 
					@ColumnResult(name = "VERSION_ID", type = Integer.class),
					@ColumnResult(name = "POSITION_ID", type = Integer.class),
					@ColumnResult(name = "ID_PREV", type = Integer.class), 
					@ColumnResult(name = "ID_PARENT", type = Integer.class), 
					@ColumnResult(name = "LEVEL_NUMBER", type = Integer.class),
					@ColumnResult(name = "IND_CHILD", type = Integer.class) 
		}) })


@Entity
@Table(name = "SCHEME")
public class SchemeItem extends TrackableEntity implements AuditExt{
	
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937870471022255831L;
	
	@SequenceGenerator(name = "SchemeItem", sequenceName = "SEQ_SCHEME", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "SchemeItem")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "VERSION_ID")
	@JournalAttr(label = "versionId", defaultText = "ИД на версия")
	private Integer versionId;
	
	
	@Column(name = "LEVEL_NUMBER")
	@JournalAttr(label = "levelNumber", defaultText = "Номер на ниво")
	private Integer levelNumber;
	
	@Column(name = "CODE")
	@JournalAttr(label = "code", defaultText = "Код")
	private String code;
	
	@Column(name = "DEFF_NAME")
	@JournalAttr(label = "deffName", defaultText = "Наименование")
	private String deffName;
	
	@Column(name = "POSITION_ID")
	@JournalAttr(label = "positionId", defaultText = "ИД на позиция")
	private Integer positionId;
		
	@Column(name = "ID_PREV")
	@JournalAttr(label = "idPrev", defaultText = "ИД на предишен елемент")
	private Integer idPrev;
	
	@Column(name = "ID_PARENT")
	@JournalAttr(label = "idParent", defaultText = "Брой символи в кода")
	private Integer idParent;
	
	@Column(name = "IND_CHILD")
	@JournalAttr(label = "indChild", defaultText = "Индикатор за наследници")
	private Integer indChild;
	
	
	
	public SchemeItem() {
		super();		
	}
	
	public SchemeItem(Integer id, String code, String deffName, Integer versionId, Integer positionId, Integer idPrev, Integer idParent, Integer levelNumber, Integer indChild) {
		super();
		this.id = id;
		this.code = code; 
		this.deffName = deffName;
		this.versionId = versionId;
		this.positionId = positionId;
		this.idPrev = idPrev;
		this.idParent = idParent;
		this.levelNumber = levelNumber;
		this.indChild = indChild;
		
	}
	
	public SchemeItem(Integer id) {
		super();
		this.id = id;
		
	}
	


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	@Override
	public String getIdentInfo() throws DbErrorException {
		return this.code;
	}


	@Override
	public Integer getCodeMainObject() {		
		//return NSIConstants.CODE_ZNACHENIE_JOURNAL_SCHEME_ITEM;
		return null;
	}


	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal j = new SystemJournal();
		j.setCodeObject(getCodeMainObject());
		j.setIdObject(getId());
		j.setIdentObject(getIdentInfo());
		return j;
	}


	public Integer getVersionId() {
		return versionId;
	}


	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}


	public Integer getLevelNumber() {
		return levelNumber;
	}


	public void setLevelNumber(Integer levelNumber) {
		this.levelNumber = levelNumber;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getDeffName() {
		return deffName;
	}


	public void setDeffName(String deffName) {
		this.deffName = deffName;
	}


	public Integer getPositionId() {
		return positionId;
	}


	public void setPositionId(Integer positionId) {
		this.positionId = positionId;
	}


	public Integer getIdPrev() {
		return this.idPrev;
	}


	public void setIdPrev(Integer idPrev) {
		this.idPrev = idPrev;
	}


	public Integer getIdParent() {
		return idParent;
	}


	public void setIdParent(Integer idParent) {
		this.idParent = idParent;
	}


	public Integer getIndChild() {
		return indChild;
	}


	public void setIndChild(Integer indChild) {
		this.indChild = indChild;
	}
	
	
	
	
	
}
