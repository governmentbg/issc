package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

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
 * Потребители към обект
 *
 * @author мамун
 */
@Entity
@Table(name = "OBJECT_USERS")
public class ObjectUsers extends TrackableEntity implements AuditExt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937876661067755831L;
	
	@SequenceGenerator(name = "ObjectUsers", sequenceName = "SEQ_OBJECT_USERS", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "ObjectUsers")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "CODE_OBJECT")
	//@JournalAttr(label = "codeObject", defaultText = "Код на обект")
	private Integer codeObject;
	
	@Column(name = "ID_OBJECT")
	//@JournalAttr(label = "idObject", defaultText = "ИД на обект")
	private Integer idObject;
	
	@Column(name = "CODE_LICE")
	@JournalAttr(label = "codeLice", defaultText = "Лице", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_SLUJBI_LICA)
	private Integer codeLice;
		
	@Column(name = "ROLE")
	@JournalAttr(label = "role", defaultText = "Роля", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_TYPE)
	private Integer role;
	
	@Column(name = "ROLE_DATE")
	@JournalAttr(label = "roleDate", defaultText = "Начална дата")
	private Date roleDate;
	
	
	
	@JournalAttr(label = "langMap", defaultText = "Езикови атрибути")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL,  orphanRemoval = true,targetEntity = ObjectUsersLang.class)
	@JoinColumn(name = "OBJECT_USERS_ID", referencedColumnName = "ID", nullable = false,insertable = false,updatable = false)
	@MapKey(name = "lang")
	private Map<Integer,ObjectUsersLang> langMap = new HashMap<Integer,ObjectUsersLang>();
	
	
	
	
	public ObjectUsers() {
		super();		
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	



	@Override
	public Integer getCodeMainObject() {		
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_OBJECT_USER;
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


	public Integer getCodeLice() {
		return codeLice;
	}


	public void setCodeLice(Integer codeLice) {
		this.codeLice = codeLice;
	}


	public Integer getRole() {
		return role;
	}


	public void setRole(Integer role) {
		this.role = role;
	}


	public Date getRoleDate() {
		return roleDate;
	}


	public void setRoleDate(Date roleDate) {
		this.roleDate = roleDate;
	}


	


	public Map<Integer, ObjectUsersLang> getLangMap() {
		return langMap;
	}


	public void setLangMap(Map<Integer, ObjectUsersLang> langMap) {
		this.langMap = langMap;
	}
	
	
	
	
	
}
