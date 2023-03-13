package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Transient;

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

@SqlResultSetMapping(name = "loadSchemeNew", classes = {
		@ConstructorResult(targetClass = PositionS.class, columns = { 
				@ColumnResult(name = "ID", type = Integer.class), 
				@ColumnResult(name = "CODE", type = String.class),
				@ColumnResult(name = "DEFF_NAME", type = String.class), 
				@ColumnResult(name = "VERSION_ID", type = Integer.class),				
				@ColumnResult(name = "ID_PREV", type = Integer.class), 
				@ColumnResult(name = "ID_PARENT", type = Integer.class), 
				@ColumnResult(name = "LEVEL_NUMBER", type = Integer.class),
				@ColumnResult(name = "IND_CHILD", type = Integer.class) 
	}) })


@Entity
@Table(name = "POSITION_SCHEME")
@Cacheable
public class PositionS extends TrackableEntity implements AuditExt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937873371067755831L;
	
	@SequenceGenerator(name = "Position", sequenceName = "SEQ_POSITION", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "Position")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	
	@Column(name = "CODE")
	@JournalAttr(label = "code", defaultText = "Код")
	private String code;
	
	@Column(name = "CODE_FULL")
	@JournalAttr(label = "codeFull", defaultText = "Пълен код")
	private String codeFull;
	
	@Column(name = "CODE_SEPARATE")
	@JournalAttr(label = "codeSeparate", defaultText = "Код с разделители")
	private String codeSeparate;
	
	@Column(name = "CODE_TYPE")
	@JournalAttr(label = "codeType", defaultText = "Тип на кода", classifID = "" + NSIConstants.CODE_CLASSIF_POSITION_CODE_TYPE )
	private Integer codeType;
	
	@Column(name = "STATUS")
	@JournalAttr(label = "status", defaultText = "Статус", classifID = "" + NSIConstants.CODE_CLASSIF_POSITION_STATUS )
	private Integer status;
		
	@Column(name = "LEVEL_NUMBER")
	@JournalAttr(label = "levelNumber", defaultText = "Номер на ниво")
	private Integer levelNumber;
	
	@Column(name = "ID_PREV")
	@JournalAttr(label = "idPrev", defaultText = "ИД на предишен елемент")
	private Integer idPrev;
	
	@Column(name = "ID_PARENT")
	@JournalAttr(label = "idParent", defaultText = "Брой символи в кода")
	private Integer idParent;
	
	@Column(name = "IND_CHILD")
	@JournalAttr(label = "indChild", defaultText = "Индикатор за наследници")
	private Integer indChild;
	
	@Column(name = "VERSION_ID")
	@JournalAttr(label = "versionId", defaultText = "ИД на версия")
	private Integer versionId;
	
	@Transient
	private String name;
	
	@Transient
	private Integer idMigr;
	
	@JournalAttr(label = "langMap", defaultText = "Езикови атрибути")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL,  orphanRemoval = true,targetEntity = PositionLang.class)
	@JoinColumn(name = "POSITION_ID", referencedColumnName = "ID", nullable = false,insertable = false,updatable = false)
	@MapKey(name = "lang")
	private Map<Integer,PositionLang> langMap = new HashMap<Integer,PositionLang>();
	
	
	@JournalAttr(label = "units", defaultText = "Мерна единицa")
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true,  mappedBy = "position")	
	private List<PositionUnits> units = new ArrayList<PositionUnits>();
	
	
	public PositionS() {
		super();		
	}
	
	public PositionS(Integer id, String code, String deffName, Integer versionId, Integer idPrev, Integer idParent, Integer levelNumber, Integer indChild) {
		super();
		this.id = id;
		this.code = code; 
		this.name = deffName;
		this.versionId = versionId;		
		this.idPrev = idPrev;
		this.idParent = idParent;
		this.levelNumber = levelNumber;
		this.indChild = indChild;
		
	}
	
	public PositionS(Integer id,Integer idParent, String code, String deffName) {
		super();
		this.id = id;
		this.idParent = idParent;
		this.code = code; 
		this.name = deffName;
	}

	@Override
	public String getIdentInfo() throws DbErrorException {
		String identInfo = "Id: "+this.id; 
		if (getLangMap() != null && getLangMap().size() > 0) {
			PositionLang deffLang = getLangMap().get(NSIConstants.CODE_DEFAULT_LANG) ;
			if (deffLang == null) {
				deffLang = getLangMap().entrySet().iterator().next().getValue();
			}
			identInfo = deffLang.getOffitialTitile();
		}
		
		return identInfo;
	}
	


	@Override
	public Integer getCodeMainObject() {		
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_POSITION;
	}


	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal j = new SystemJournal();
		j.setCodeObject(getCodeMainObject());
		j.setIdObject(getId());
		j.setIdentObject(getIdentInfo());
		return j;
	}





	public Integer getId() {
		return id;
	}





	public void setId(Integer id) {
		this.id = id;
	}





	public String getCode() {
		return code;
	}





	public void setCode(String code) {
		this.code = code;
	}





	public String getCodeFull() {
		return codeFull;
	}





	public void setCodeFull(String codeFull) {
		this.codeFull = codeFull;
	}





	public String getCodeSeparate() {
		return codeSeparate;
	}





	public void setCodeSeparate(String codeSeparate) {
		this.codeSeparate = codeSeparate;
	}





	public Integer getCodeType() {
		return codeType;
	}





	public void setCodeType(Integer codeType) {
		this.codeType = codeType;
	}





	public Integer getStatus() {
		return status;
	}





	public void setStatus(Integer status) {
		this.status = status;
	}





	public Integer getLevelNumber() {
		return levelNumber;
	}





	public void setLevelNumber(Integer levelNumber) {
		this.levelNumber = levelNumber;
	}





	public Map<Integer, PositionLang> getLangMap() {
		return langMap;
	}





	public void setLangMap(Map<Integer, PositionLang> langMap) {
		this.langMap = langMap;
	}





	public List<PositionUnits> getUnits() {
		return units;
	}





	public void setUnits(List<PositionUnits> units) {
		this.units = units;
	}

	

	@Override
	public String toString() {
		return "Position [id=" + id + ", code=" + code + ", codeFull=" + codeFull + ", codeSeparate=" + codeSeparate
				+ ", codeType=" + codeType + ", status=" + status + ", levelNumber=" + levelNumber + ", langMap=" + langMap + ", units=" + units + "]";
	}

	public Integer getIdPrev() {
		return idPrev;
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

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj)  return true;
		
		if(obj == null || obj.getClass()!= this.getClass()) return false;
		
		if(this.id!=null &&  ((PositionS)obj).getId() !=null && (this.id.intValue() == ((PositionS)obj).getId().intValue())) return true;
		
		return false;
    }

	public Integer getIdMigr() {
		return idMigr;
	}

	public void setIdMigr(Integer idMigr) {
		this.idMigr = idMigr;
	}
	
	
}
