package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.JournalAttr;

@Entity
@Table(name = "OBJECT_DOCS_LANG")

public class ObjectDocsLang implements Serializable{
	
	private static final long serialVersionUID = -4937978471767333831L;
	
	@SequenceGenerator(name = "ObjectDocsLang", sequenceName = "SEQ_OBJECT_DOCS_LANG", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "ObjectDocsLang")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
	
	@JournalAttr(label="lang",defaultText = "Език",  classifID = "" + NSIConstants.CODE_CLASSIF_LANG)
	@Column(name = "lang")
	private Integer lang;
		
	
	@ManyToOne(targetEntity = ObjectDocs.class)
	@JoinColumn(name = "OBJECT_DOCS_ID",nullable = false)
	private ObjectDocs objectDocs;
	
	@Column(name = "ANOT")
	@JournalAttr(label = "anot", defaultText = "Относно")
	private String anot;
	
	@Column(name = "DESCRIPTION")
	@JournalAttr(label = "description", defaultText = "Допълнителна информация")
	private String description;
	
	
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	@XmlTransient
	public ObjectDocs getObjectDocs() {
		return objectDocs;
	}

	public void setObjectDocs(ObjectDocs objectDocs) {
		this.objectDocs = objectDocs;
	}

	public String getAnot() {
		return anot;
	}

	public void setAnot(String anot) {
		this.anot = anot;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	
	
	
	
	

	
	
	
	
	
	
	

	
	
	
	
}
