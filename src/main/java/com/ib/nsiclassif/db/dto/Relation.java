package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Метаданни на релация
 *
 * @author мамун
 */
@Entity
@Table(name = "RELATION")
public class Relation extends TrackableEntity implements AuditExt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937970488067755831L;

	@SequenceGenerator(name = "Relation", sequenceName = "SEQ_RELATION", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "Relation")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;

	@Column(name = "ID_TABLE")
	@JournalAttr(label = "idTable", defaultText = "Кореспондираща таблица")
	private Integer idTable;

	@Column(name = "SOURCE_CODE")
	@JournalAttr(label = "sourceCode", defaultText = "Код на позиция – източник")
	private String sourceCode;

	@Column(name = "TARGET_CODE")
	@JournalAttr(label = "targetCode", defaultText = "Код на позиция – цел")
	private String targetCode;

	@Column(name = "EXPLANATION")
	@JournalAttr(label = "explanation", defaultText = "Пояснение", classifID = "" + NSIConstants.CODE_CLASSIF_EXPLANATION)
	private Integer explanation;
 

	public Relation() {
		super();
	}

	@Override
	public Integer getCodeMainObject() {
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_RELATION;
	}

	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal j = new SystemJournal();
		j.setCodeObject(getCodeMainObject());
		j.setIdObject(getId());
		j.setIdentObject(getIdentInfo());
		j.setJoinedIdObject1(getIdTable());
		j.setJoinedCodeObject1(NSIConstants.CODE_ZNACHENIE_JOURNAL_CORESP_TABLE);
		return j;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdTable() {
		return idTable;
	}

	public void setIdTable(Integer idTable) {
		this.idTable = idTable;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getTargetCode() {
		return targetCode;
	}

	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	public Integer getExplanation() {
		return explanation;
	}

	public void setExplanation(Integer explanation) {
		this.explanation = explanation;
	}

}
