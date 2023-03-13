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
@Table(name = "OBJECT_USERS_LANG")

public class ObjectUsersLang implements Serializable{
	
	private static final long serialVersionUID = -4937978471767755831L;
	
	@SequenceGenerator(name = "ObjectUsersLang", sequenceName = "SEQ_OBJECT_USERS_LANG", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "ObjectUsersLang")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
	
	@JournalAttr(label="lang",defaultText = "Език",  classifID = "" + NSIConstants.CODE_CLASSIF_LANG)
	@Column(name = "lang")
	private Integer lang;
		
	
	@ManyToOne(targetEntity = ObjectUsers.class)
	@JoinColumn(name = "OBJECT_USERS_ID",nullable = false)
	private ObjectUsers objectUsers;
	
	@Column(name = "ROLE_COMMENT")
	@JournalAttr(label = "roleComment", defaultText = "Коментар")
	private String roleComment;
	
	
	

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
	public ObjectUsers getObjectUsers() {
		return objectUsers;
	}

	public void setObjectUsers(ObjectUsers objectUsers) {
		this.objectUsers = objectUsers;
	}

	public String getRoleComment() {
		return roleComment;
	}

	public void setRoleComment(String roleComment) {
		this.roleComment = roleComment;
	}

	
	
	
	
	
	
	

	
	
	
	
}
