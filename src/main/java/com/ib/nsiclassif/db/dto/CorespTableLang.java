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
@Table(name = "TABLE_CORRESP_LANG")

public class CorespTableLang implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -4937978471767755855L;
	
	@SequenceGenerator(name = "CorespTableLang", sequenceName = "SEQ_TABLE_CORRESP_LANG", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "CorespTableLang")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
	
	@JournalAttr(label="lang",defaultText = "Език",  classifID = "" + NSIConstants.CODE_CLASSIF_LANG)
	@Column(name = "lang")
	private Integer lang;
		
	
	@ManyToOne(targetEntity = CorespTable.class)
	@JoinColumn(name = "TABLE_CORRESP_ID",nullable = false)
	private CorespTable corespTable;
	
	@Column(name = "IDENT")
	@JournalAttr(label = "ident", defaultText = "Идентификатор")
	private String ident;
	
	@Column(name = "NAME")
	@JournalAttr(label = "name", defaultText = "Наименование")
	private String name;
	
	@Column(name = "REGION")
	@JournalAttr(label = "region", defaultText = "Област на приложение")
	private String region;
	
	@Column(name = "COMMENT")
	@JournalAttr(label = "comment", defaultText = "Коментар")
	private String comment;
	
	@Override
	public CorespTableLang clone() throws CloneNotSupportedException {
		CorespTableLang c = (CorespTableLang) super.clone();
		return c;
	}
	

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
	public CorespTable getCorespTable() {
		return corespTable;
	}


	public void setCorespTable(CorespTable corespTable) {
		this.corespTable = corespTable;
	}


	public String getIdent() {
		return ident;
	}


	public void setIdent(String ident) {
		this.ident = ident;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getRegion() {
		return region;
	}


	public void setRegion(String region) {
		this.region = region;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}

	
	

	
	
	

	

	
	
	
	
}
