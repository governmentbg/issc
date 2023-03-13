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
@Table(name = "RELATION_LANG_DROP_ME")

public class RelationLang implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -4937978471767755831L;
	
	@SequenceGenerator(name = "RelationLang", sequenceName = "SEQ_RELATION_LANG", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "RelationLang")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
	
	@JournalAttr(label="lang",defaultText = "Език",  classifID = "" + NSIConstants.CODE_CLASSIF_LANG)
	@Column(name = "lang")
	private Integer lang;
		
	
	@ManyToOne(targetEntity = Level.class)
	@JoinColumn(name = "RELATION_ID",nullable = false)
	private Relation relation;
	
	@Column(name = "COMMENT")
	@JournalAttr(label = "comment", defaultText = "Коментар")
	private String comment;
	
	@Override
	public RelationLang clone() throws CloneNotSupportedException {
		RelationLang c = (RelationLang) super.clone();
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
	public Relation getRelation() {
		return relation;
	}


	public void setRelation(Relation relation) {
		this.relation = relation;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}

	
	

	
	

	

	
	
	
	
}
