package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "CLASSIFICATION_LANG")

public class ClassificationLang implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -4937978471767755831L;
	
	@SequenceGenerator(name = "ClassificationLang", sequenceName = "SEQ_CLASSIFICATION_LANG", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "ClassificationLang")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
	
	@JournalAttr(label="lang",defaultText = "Език",  classifID = "" + NSIConstants.CODE_CLASSIF_LANG)
	@Column(name = "lang")
	private Integer lang;
		
	
	@ManyToOne(targetEntity = Classification.class)
	@JoinColumn(name = "CLASSIFICATION_ID",nullable = false)
	private Classification classif;
	
	@Column(name = "IDENT")
	@JournalAttr(label = "ident", defaultText = "Идентификатор")
	private String ident;
	
	@Column(name = "NAME_CLASSIF")
	@JournalAttr(label = "nameClassif", defaultText = "Име")
	private String nameClassif;
	
	@Column(name = "DESCRIPTION")
	@JournalAttr(label = "description", defaultText = "Общо описание")
	private String description;
	
	@Column(name = "COMMENT")
	@JournalAttr(label = "comment", defaultText = "Коментар")
	private String comment;
	
	@Column(name = "NEWS")
	@JournalAttr(label = "news", defaultText = "Актуални новини")
	private String news;
	
	@Column(name = "AREA")
	@JournalAttr(label = "area", defaultText = "Области на приложение")
	private String area;
	
	@Override
	public ClassificationLang clone() throws CloneNotSupportedException {
		ClassificationLang c = (ClassificationLang) super.clone();
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
	public Classification getClassif() {
		return classif;
	}

	
	public void setClassif(Classification classif) {
		this.classif = classif;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getNews() {
		return news;
	}

	public void setNews(String news) {
		this.news = news;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getNameClassif() {
		return nameClassif;
	}

	public void setNameClassif(String nameClassif) {
		this.nameClassif = nameClassif;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}


	
	
	
}
