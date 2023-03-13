package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.Date;
/*import java.util.HashMap;
import java.util.Map;*/

//import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
/*import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;*/
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/*import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;*/

import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;
import com.ib.nsiclassif.system.NSIConstants;


@Entity
@Table(name = "PUBLICATION")

public class Publication extends TrackableEntity implements AuditExt {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6743256342493374985L;
	

	/**
	 * Основен java Entity клас на публикациите 
	 */
	
	@SequenceGenerator(name = "Publication", sequenceName = "SEQ_PUBLICATION", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "Publication")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
		
	
	@Column(name = "SECTION")
	@JournalAttr(label = "section", defaultText = "Секция", classifID = "" + NSIConstants.CODE_SYSCLASS_SECT_PUBL)
	private Integer section;        
	
	@Column(name = "DATE_FROM")
	@JournalAttr(label = "dateFrom", defaultText = "Дата на секцията", dateMask = "dd.MM.yyyy")
	private Date dateFrom;   
	
	@Column(name = "DATE_TO")
	@JournalAttr(label = "dateTo", defaultText = "Крайна дата на секцията", dateMask = "dd.MM.yyyy")
	private Date dateTo; 

	@Column(name = "IMAGE_PUB")
	@JournalAttr(label = "imagePub", defaultText = "Контент на изображение към секцията")
	private byte[] 	imagePub;      
	
	@Column(name = "NOTE")
	@JournalAttr(label = "note", defaultText = "Други коментари към секцията")
	private String  note;
	
	@Column(name = "home_page")
	@JournalAttr(label = "homePage", defaultText = "Локация начална/друга страница на сайта")
	private Integer homePage;       
	
	@Column(name = "new_tab")
	@JournalAttr(label = "newTab", defaultText = "Отваряне в друга страница на сайта")
	private Integer newTab;         
	
	@Column(name = "link_page")
	@JournalAttr(label = "linkPage", defaultText = "Линк към страница от менюто")
	private String linkPage;      
	

	@Column(name = "TYPE_PUB")
	@JournalAttr(label = "typePub", defaultText = "Тип на публикацията", classifID = "" + NSIConstants.CODE_SYSCLASS_PUBL_TYPE)
	private Integer typePub;     
	
		
	public Publication() {
		super();
	}

	@Override
	public Integer getCodeMainObject() {
		// TODO Auto-generated method stub
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_PUBLICATION;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getSection() {
		return section;
	}

	public void setSection(Integer section) {
		this.section = section;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}


	public byte[] getImagePub() {
		return imagePub;
	}

	public void setImagePub(byte[] imagePub) {
		this.imagePub = imagePub;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}


	public Integer getTypePub() {
		return typePub;
	}

	public void setTypePub(Integer typePub) {
		this.typePub = typePub;
	}
	
	/**
	 * @return the homePage
	 */
	public Boolean getHomePageB() {
		if (this.getHomePage()==null || this.getHomePage().equals(2L)) {
			return false;
		}else{
			return true;
		}		
	}

	/**
	 * @param homePage the homePage to set
	 */
	public void setHomePageB(Boolean homePage) {
		if (homePage) {
			this.setHomePage(1);
		}else{
			this.setHomePage(2);
		}	
	}

	/**
	 * @return the homePage
	 */
	public Boolean getNewTabB() {
		if (this.getNewTab()==null || this.getNewTab().equals(2)) {
			return false;
		}else{
			return true;
		}		
	}

	/**
	 * @param homePage the homePage to set
	 */
	public void setNewTabB(Boolean newTab) {
		if (newTab) {
			this.setNewTab(1);
		}else{
			this.setNewTab(2);
		}	
	}

	/**
	 * @return the homePage
	 */
	public Integer getHomePage() {
		return homePage;
	}

	/**
	 * @param homePage the homePage to set
	 */
	public void setHomePage(Integer homePage) {
		this.homePage = homePage;
	}

	/**
	 * @return the newTab
	 */
	public Integer getNewTab() {
		return newTab;
	}

	/**
	 * @param newTab the newTab to set
	 */
	public void setNewTab(Integer newTab) {
		this.newTab = newTab;
	}

	/**
	 * @return the linkPage
	 */
	public String getLinkPage() {
		return linkPage;
	}

	/**
	 * @param linkPage the linkPage to set
	 */
	public void setLinkPage(String linkPage) {
		this.linkPage = linkPage;
	}

	
	@Override
	public String getIdentInfo() throws DbErrorException {
		String identInfo = "Id: "+this.id; 
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
