package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.text.SimpleDateFormat;
import java.util.Date;
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
 * Документи към обект
 *
 * @author мамун
 */
@Entity
@Table(name = "OBJECT_DOCS")
public class ObjectDocs extends TrackableEntity implements AuditExt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937876661999755831L;
	
	@SequenceGenerator(name = "ObjectDocs", sequenceName = "SEQ_OBJECT_DOCS", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "ObjectDocs")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "CODE_OBJECT")
	//@JournalAttr(label = "codeObject", defaultText = "Код на обект")
	private Integer codeObject;
	
	@Column(name = "ID_OBJECT")
	//@JournalAttr(label = "idObject", defaultText = "ИД на обект")
	private Integer idObject;
	
	@Column(name = "RN_DOC")
	@JournalAttr(label = "rnDoc", defaultText = "Номер на документа")
	private String rnDoc;
	
	@Column(name = "DAT_DOC")
	@JournalAttr(label = "datDoc", defaultText = "Дата на документа")
	private Date datDoc;
	
	
	@Column(name = "TYPE")
	@JournalAttr(label = "type", defaultText = "Вид на документа", classifID = "" + NSIConstants.CODE_CLASSIF_DOC_TYPE)
	private Integer type;
		
	@Column(name = "PUBL")
	@JournalAttr(label = "publ", defaultText = "За публикуване", classifID = "" + NSIConstants.CODE_CLASSIF_DANE)
	private Integer publ;
	
	@JournalAttr(label = "langMap", defaultText = "Езикови атрибути")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL,  orphanRemoval = true,targetEntity = ObjectDocsLang.class)
	@JoinColumn(name = "OBJECT_DOCS_ID", referencedColumnName = "ID", nullable = false,insertable = false,updatable = false)
	@MapKey(name = "lang")
	private Map<Integer,ObjectDocsLang> langMap = new HashMap<Integer,ObjectDocsLang>();
	
	
	
	
	public ObjectDocs() {
		super();		
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	@Override
	public String getIdentInfo() throws DbErrorException {	
		
		String identInfo = "Id: "+this.id; 
		if (rnDoc != null && ! rnDoc.isEmpty()) {
			identInfo = rnDoc;
			if (datDoc != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				identInfo += "\\" + sdf.format(datDoc);
			}
		}
		
		return identInfo;
	}



	@Override
	public Integer getCodeMainObject() {		
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_OBJECT_DOC;
	}


	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal j = new SystemJournal();
		j.setCodeObject(getCodeMainObject());
		j.setIdObject(getId());
		j.setIdentObject(getIdentInfo());
		j.setJoinedCodeObject1(codeObject);
		j.setJoinedIdObject1(idObject);
		return j;
	}


	public Integer getCodeObject() {
		return codeObject;
	}


	public void setCodeObject(Integer codeObject) {
		this.codeObject = codeObject;
	}


	public Integer getIdObject() {
		return idObject;
	}


	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}


	public String getRnDoc() {
		return rnDoc;
	}


	public void setRnDoc(String rnDoc) {
		this.rnDoc = rnDoc;
	}


	public Date getDatDoc() {
		return datDoc;
	}


	public void setDatDoc(Date datDoc) {
		this.datDoc = datDoc;
	}


	public Integer getType() {
		return type;
	}


	public void setType(Integer type) {
		this.type = type;
	}


	public Integer getPubl() {
		return publ;
	}


	public void setPubl(Integer publ) {
		this.publ = publ;
	}


	public Map<Integer, ObjectDocsLang> getLangMap() {
		return langMap;
	}


	public void setLangMap(Map<Integer, ObjectDocsLang> langMap) {
		this.langMap = langMap;
	}



	
	
	
	
	
}
