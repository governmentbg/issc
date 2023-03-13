package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;
import java.util.Date;

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
@Table(name = "VERSION_LANG")
public class VersionLang implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -4937978471767755831L;
	
	@SequenceGenerator(name = "VersionLang", sequenceName = "SEQ_VERSION_LANG", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "VersionLang")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
	
	@JournalAttr(label="lang",defaultText = "Език",  classifID = "" + NSIConstants.CODE_CLASSIF_LANG)
	@Column(name = "lang")
	private Integer lang;
		
	
	@ManyToOne(targetEntity = Version.class)
	@JoinColumn(name = "VERSION_ID",nullable = false)
	private Version version;
	
	@Column(name = "IDENT")
	@JournalAttr(label = "ident", defaultText = "Идентификатор")
	private String ident;
	
	@Column(name = "TITLE")
	@JournalAttr(label = "TITLE", defaultText = "Наименование")
	private String title;
	
	@Column(name = "DESCRIPTION")
	@JournalAttr(label = "DESCRIPTION", defaultText = "Описание")
	private String description;
	
	@Column(name = "METHODOLOGY")
	@JournalAttr(label = "METHODOLOGY", defaultText = "Методология")
	private String methodology;
	
	@Column(name = "COMMENT")
	@JournalAttr(label = "COMMENT", defaultText = "Коментар")
	private String comment;
	
	@Column(name = "NEWS")
	@JournalAttr(label = "NEWS", defaultText = "Актуални новини")
	private String news;
	
	@Column(name = "LEGALBASE")
	@JournalAttr(label = "LEGALBASE", defaultText = "Правно основание")
	private String legalbase;
	
	@Column(name = "PUBLICATIONS")
	@JournalAttr(label = "PUBLICATIONS", defaultText = "Публикации")
	private String publications;
	
	@Column(name = "AREAS")
	@JournalAttr(label = "AREAS", defaultText = "Области на приложение")
	private String areas;
	
	@Column(name = "POD")
	@JournalAttr(label = "pod", defaultText = "Наименование на институция")
	private String pod;
	
	@Column(name = "POD_URL")
	@JournalAttr(label = "podUrl", defaultText = "Наименование на институция - линк")
	private String podUrl;
	
	@Column(name = "RESUOURCE_OPENDATA_KEY")
	@JournalAttr(label = "resourceOpenDataKey", defaultText = "Ключ на ресурса в opendata")
	private String resourceOpenDataKey;
	
	@Column(name = "OPENDATA_LAST_SENT")
	@JournalAttr(label = "openDataLastSent", defaultText = "Дата на последно обновяване в opendata")
	private Date openDataLastSent;
	
	@Override
	public VersionLang clone() throws CloneNotSupportedException {
		VersionLang c = (VersionLang) super.clone();
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
	public Version getVersion() {
		return version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMethodology() {
		return methodology;
	}

	public void setMethodology(String methodology) {
		this.methodology = methodology;
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

	public String getLegalbase() {
		return legalbase;
	}

	public void setLegalbase(String legalbase) {
		this.legalbase = legalbase;
	}

	public String getPublications() {
		return publications;
	}

	public void setPublications(String publications) {
		this.publications = publications;
	}

	public String getAreas() {
		return areas;
	}

	public void setAreas(String areas) {
		this.areas = areas;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getPod() {
		return pod;
	}

	public void setPod(String pod) {
		this.pod = pod;
	}

	public String getPodUrl() {
		return podUrl;
	}

	public void setPodUrl(String podUrl) {
		this.podUrl = podUrl;
	}

	public String getResourceOpenDataKey() {
		return resourceOpenDataKey;
	}

	public void setResourceOpenDataKey(String resourceOpenDataKey) {
		this.resourceOpenDataKey = resourceOpenDataKey;
	}

	public Date getOpenDataLastSent() {
		return openDataLastSent;
	}

	public void setOpenDataLastSent(Date openDataLastSent) {
		this.openDataLastSent = openDataLastSent;
	}

	
	
	
	
}
