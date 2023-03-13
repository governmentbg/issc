package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
import javax.persistence.Id;
/*import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;*/
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;
import com.ib.nsiclassif.system.NSIConstants;

@Entity
@Table(name = "PUBLICATION_LANG")
public class PublicationLang  extends TrackableEntity  implements AuditExt {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4439647306940449702L;

	/**
	 * java Entity клас на езиковите версии на публикациите 
	 */

		
	@SequenceGenerator(name = "PublicationLang", sequenceName = "SEQ_PUBLICATION_LANG", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "PublicationLang")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;

	@Column(name = "PUBLICATION_ID")
	private Integer pubId;    
	
	@Column(name = "LANG")
	@JournalAttr(label = "lang", defaultText = "Езикова версия", classifID = "" + NSIConstants.CODE_CLASSIF_LANG)
	private Integer lang; 
	
	@Column(name = "TITLE")
	@JournalAttr(label = "title", defaultText = "Заглавие")
	private String title;   
	
	@Column(name = "ANNOTATION")
	@JournalAttr(label = "annotation", defaultText = "Анотация")
	private String  annotation; 

	@Column(name = "FULL_TEXT")
	@JournalAttr(label = "fullText", defaultText = "Пълен текст")
	private String 	fullText;    

	@Column(name = "OTHER_INFO")
	@JournalAttr(label = "otherInfo", defaultText = "Коментар")
	private String  otherInfo; 

	@Column(name = "URL_PUB")
	@JournalAttr(label = "urlPub", defaultText = "URL Видео")
	private String  urlPub;  
	
	@Column(name = "AUTHOR")
	@JournalAttr(label = "author", defaultText = "Автори")
	private String  author;  

	@Override
	public Integer getCodeMainObject() {
		// TODO Auto-generated method stub
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_PUBLICATION_LANG;
	}
	
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrlPub() {
		return urlPub;
	}

	public void setUrlPub(String urlPub) {
		this.urlPub = urlPub;
	}

	public String getOtherInfo() {
		return otherInfo;
	}

	public void setOtherInfo(String otherInfo) {
		this.otherInfo = otherInfo;
	}

	public String getFullText() {
		return fullText;
	}

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getPubId() {
		return pubId;
	}

	public void setPubId(Integer pubId) {
		this.pubId = pubId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}


	@Override
	public String getIdentInfo() throws DbErrorException {
		String identInfo = "Id: "+this.id; 
		if (getTitle() != null && ! getTitle().isEmpty()) 
			identInfo+=" " +getTitle().trim();
		return identInfo;
	}
	
	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal sj = new SystemJournal();
		sj.setCodeObject(getCodeMainObject());
		sj.setIdObject(getId());
		sj.setIdentObject(getIdentInfo());		
		
		return sj;
	}
	


}
