package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Cacheable;
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
@Table(name = "POSITION_LANG")
@Cacheable
public class PositionLang implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -4937978471767755441L;
	
	@SequenceGenerator(name = "PositionLang", sequenceName = "SEQ_POSITION_LANG", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "PositionLang")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
	
	@JournalAttr(label="lang",defaultText = "Език",  classifID = "" + NSIConstants.CODE_CLASSIF_LANG)
	@Column(name = "lang")
	private Integer lang;
		
	
	@ManyToOne(targetEntity = PositionS.class)
	@JoinColumn(name = "POSITION_ID",nullable = false)
	private PositionS position;
	
	@Column(name = "OFFICIAL_TITLE")
	@JournalAttr(label = "offitialTitile", defaultText = "Наименование ")
	private String offitialTitile;
	
	@Column(name = "LONG_TITLE")
	@JournalAttr(label = "LongTitle", defaultText = "Стандартно дълго наименование")
	private String LongTitle;
	
	@Column(name = "SHORT_TITLE")
	@JournalAttr(label = "shortTitle", defaultText = "Стандартно кратко наименование")
	private String shortTitle;
	
	@Column(name = "ALTERNATE_TITLES")
	@JournalAttr(label = "alternativeNames", defaultText = "Алтернативни наименования")
	private String alternativeNames;
	
	@Column(name = "COMMENT")
	@JournalAttr(label = "comment", defaultText = "Коментар")
	private String comment;
	
	@Column(name = "INCLUDES")
	@JournalAttr(label = "includes", defaultText = "Обяснителни бележки - Включва")
	private String includes;
	
	@Column(name = "ALSO_INCLUDES")
	@JournalAttr(label = "alsoIncludes", defaultText = "Обяснителни бележки - Включва още")
	private String alsoIncludes;
	
	@Column(name = "EXCLUDES")
	@JournalAttr(label = "excludes", defaultText = "Обяснителни бележки - Не включва")
	private String excludes;
	
	@Column(name = "RULES")
	@JournalAttr(label = "rules", defaultText = "Обяснителни бележки - Правила")
	private String rules;
	
	
	@Column(name = "STAT_POKAZATEL")
	@JournalAttr(label = "pokazatel", defaultText = "Статистически показател")
	private String statPokazatel;
	
	
	@Column(name = "PREPRATKA")
	@JournalAttr(label = "prepratka", defaultText = "Препратка")
	private String prepratka;
	
	@Override
	public PositionLang clone() throws CloneNotSupportedException {
		PositionLang c = (PositionLang) super.clone();
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
	public PositionS getPosition() {
		return position;
	}


	public void setPosition(PositionS position) {
		this.position = position;
	}


	public String getOffitialTitile() {
		return offitialTitile;
	}


	public void setOffitialTitile(String offitialTitile) {
		this.offitialTitile = offitialTitile;
	}


	public String getLongTitle() {
		return LongTitle;
	}


	public void setLongTitle(String longTitle) {
		LongTitle = longTitle;
	}


	public String getShortTitle() {
		return shortTitle;
	}


	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}


	public String getAlternativeNames() {
		return alternativeNames;
	}


	public void setAlternativeNames(String alternativeNames) {
		this.alternativeNames = alternativeNames;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public String getIncludes() {
		return includes;
	}


	public void setIncludes(String includes) {
		this.includes = includes;
	}


	public String getAlsoIncludes() {
		return alsoIncludes;
	}


	public void setAlsoIncludes(String alsoIncludes) {
		this.alsoIncludes = alsoIncludes;
	}


	public String getExcludes() {
		return excludes;
	}


	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}


	public String getRules() {
		return rules;
	}


	public void setRules(String rules) {
		this.rules = rules;
	}


	public String getStatPokazatel() {
		return statPokazatel;
	}


	public void setStatPokazatel(String statPokazatel) {
		this.statPokazatel = statPokazatel;
	}


	public String getPrepratka() {
		return prepratka;
	}


	public void setPrepratka(String prepratka) {
		this.prepratka = prepratka;
	}


	@Override
	public String toString() {
		return "PositionLang [id=" + id + ", lang=" + lang + ", positionId=" + position.getId() + ", offitialTitile="
				+ offitialTitile + ", LongTitle=" + LongTitle + ", shortTitle=" + shortTitle + ", alternativeNames="
				+ alternativeNames + ", comment=" + comment + ", includes=" + includes + ", alsoIncludes="
				+ alsoIncludes + ", excludes=" + excludes + ", rules=" + rules + "]";
	}

	
	

	

	
	
	
	
}
