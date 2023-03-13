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
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Метаданни на Кореспондираща таблица
 *
 * @author мамун
 */
@Entity
@Table(name = "TABLE_CORRESP")
public class CorespTable extends TrackableEntity implements AuditExt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937330471067755837L;
	
	@SequenceGenerator(name = "CorespTable", sequenceName = "SEQ_TABLE_CORRESP", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "CorespTable")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	
	@Column(name = "ID_VERS_SOURCE")
	@JournalAttr(label = "idVersSource", defaultText = "Версия източник" )
	private Integer idVersSource;
		
	@Column(name = "ID_VERS_TARGET")
	@JournalAttr(label = "idVersTarget", defaultText = "Версия цел")
	private Integer idVersTarget;
	
	@Column(name = "TABLE_TYPE")
	@JournalAttr(label = "tableType", defaultText = "Тип на таблицата", classifID = "" + NSIConstants.CODE_CLASSIF_CORESP_TYPE)
	private Integer tableType;
	
	@Column(name = "STATUS")
	@JournalAttr(label = "status", defaultText = "Статус", classifID = "" + NSIConstants.CODE_CLASSIF_CORESP_STATUS)
	private Integer status;
	
	@Column(name = "RELATIONS_COUNT")
	@JournalAttr(label = "relationsCount", defaultText = "Брой релации")
	private Integer relationsCount;
	
	@Column(name = "SOURCE_POS_COUNT")
	@JournalAttr(label = "sourcePosCount", defaultText = "Брой позиции - източници ")
	private Integer sourcePosCount;
	
	@Column(name = "TARGET_POS_COUNT")
	@JournalAttr(label = "targetPosCount", defaultText = "Брой позиции - цели")
	private Integer targetPosCount;
	
	@Column(name = "PATH")
	@JournalAttr(label = "path", defaultText = "Пътека за индиректни таблици")
	private String path;
	
	@Column(name = "RELATION_TYPE")
	@JournalAttr(label = "relationType", defaultText = "Режим на свързване", classifID = "" + NSIConstants.CODE_CLASSIF_CORESP_RELATION_TYPE )
	private Integer relationType;
	
	@Column(name = "DATE_REG")
	@JournalAttr(label = "dateReg", defaultText = "Дата на регистрация", dateMask = "dd.MM.yyyy")
	private Date dateReg;
	
	@Column(name = "FINALIZED")
	@JournalAttr(label = "finaliezd", defaultText = "Публикувана на сайта", classifID = "" + NSIConstants.CODE_CLASSIF_DANE)
	private Integer finalized;
	
	@Transient
	private boolean publicated;
	
	
	@JournalAttr(label = "langMap", defaultText = "Езикови атрибути")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL,  orphanRemoval = true,targetEntity = CorespTableLang.class)
	@JoinColumn(name = "TABLE_CORRESP_ID", referencedColumnName = "ID", nullable = false,insertable = false,updatable = false)
	@MapKey(name = "lang")
	private Map<Integer,CorespTableLang> langMap = new HashMap<Integer,CorespTableLang>();
	
	
	public CorespTable() {
		super();		
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	


	@Override
	public Integer getCodeMainObject() {		
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_CORESP_TABLE;
	}


	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal j = new SystemJournal();
		j.setCodeObject(getCodeMainObject());
		j.setIdObject(getId());
		j.setIdentObject(getIdentInfo());
		
		j.setJoinedCodeObject1(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION);
		j.setJoinedIdObject1(idVersSource);		
		j.setJoinedCodeObject2(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION);
		j.setJoinedIdObject2(idVersTarget);
		return j;
	}


	public Integer getIdVersSource() {
		return idVersSource;
	}


	public void setIdVersSource(Integer idVersSource) {
		this.idVersSource = idVersSource;
	}


	public Integer getIdVersTarget() {
		return idVersTarget;
	}


	public void setIdVersTarget(Integer idVersTarget) {
		this.idVersTarget = idVersTarget;
	}


	public Integer getTableType() {
		return tableType;
	}


	public void setTableType(Integer tableType) {
		this.tableType = tableType;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public Integer getRelationsCount() {
		return relationsCount;
	}


	public void setRelationsCount(Integer relationsCount) {
		this.relationsCount = relationsCount;
	}


	public Integer getSourcePosCount() {
		return sourcePosCount;
	}


	public void setSourcePosCount(Integer sourcePosCount) {
		this.sourcePosCount = sourcePosCount;
	}


	public Integer getTargetPosCount() {
		return targetPosCount;
	}


	public void setTargetPosCount(Integer targetPosCount) {
		this.targetPosCount = targetPosCount;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public Integer getRelationType() {
		return relationType;
	}


	public void setRelationType(Integer relationType) {
		this.relationType = relationType;
	}


	public Map<Integer, CorespTableLang> getLangMap() {
		return langMap;
	}


	public void setLangMap(Map<Integer, CorespTableLang> langMap) {
		this.langMap = langMap;
	}
	
	public TreeHolder toTreeHolder() {
		
		TreeHolder th = new TreeHolder();
		th.setIdParent(idVersSource);
		th.setIdParent2(idVersTarget);
		th.setCodeObject(NSIConstants.CODE_ZNACHENIE_JOURNAL_CORESP_TABLE);
		th.setIdObject(id);
		Iterator<Entry<Integer, CorespTableLang>> it = langMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, CorespTableLang> entry = it.next();
			CorespTableLang value = entry.getValue();
			TreeHolderLang thl = new TreeHolderLang();
			thl.setIdent(value.getIdent().replace("\n", ""));
			thl.setName(value.getName().replace("\n", ""));
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
