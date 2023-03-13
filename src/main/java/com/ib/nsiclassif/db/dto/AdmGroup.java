package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import com.ib.nsiclassif.db.dto.AdmUser; 
import com.ib.indexui.system.Constants;
import com.ib.system.BaseSystemData;
import com.ib.system.SysConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JPA;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.PersistentEntity;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * AdmGroups Entity  
 * Автор г.Белев
 */
@Entity
@Table(name = "ADM_GROUPS")
public class AdmGroup extends TrackableEntity implements Cloneable, AuditExt {

	/** */
	private static final long serialVersionUID = 8328718883642909104L;

	@SequenceGenerator(name = "AdmGroups", sequenceName = "SEQ_ADM_GROUPS", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "AdmGroups")
	@Column(name = "GROUP_ID")
	@JournalAttr(label = "id", defaultText = "Системен идентификатор", isId = "true")
	private Integer id;

	@Column(name = "GROUP_NAME")
	@JournalAttr(label = "groupName", defaultText = "Име на група")
	private String groupName;

	@Column(name = "GROUP_COMMENT")
	@JournalAttr(label = "username", defaultText = "Описание на група")
	private String groupComment;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID", nullable = false)
	@JournalAttr(label = "groupRoles", defaultText = "Право")
	private List<AdmGroupRole> groupRoles;

	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(name = "ADM_USER_GROUP", joinColumns = { @JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID", nullable = false, updatable = false) }, inverseJoinColumns = {
		@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false, updatable = false) })
	private List<AdmUser> groupUsers;

	@Transient
	@JournalAttr(label = "xmlUsers", defaultText = "Участници", classifID = "" + SysConstants.CODE_CLASSIF_USERS)
	private List<Integer> xmlUsers;

	/** */
	public AdmGroup() {
		super();
	}

	/** @param id */
	public AdmGroup(Integer id) {
		this.id = id;
	}

	/** */
	@Override
	public AdmGroup clone() throws CloneNotSupportedException {
		return (AdmGroup) super.clone();
	}

	/** @see PersistentEntity#getCodeMainObject() */
	@Override
	public Integer getCodeMainObject() {
		return Constants.CODE_ZNACHENIE_JOURNAL_GROUPUSER;
	}

	/**
	 * @return the groupComment
	 */
	public String getGroupComment() {
		return this.groupComment;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * @return the groupRoles
	 */
	public List<AdmGroupRole> getGroupRoles() {
		if (this.groupRoles == null) {
			this.groupRoles = new ArrayList<>();
		}
		return this.groupRoles;
	}

	/**
	 * @return the groupUsers
	 */
	@XmlTransient
	public List<AdmUser> getGroupUsers() {
		if (this.groupUsers == null) {
			this.groupUsers = new ArrayList<>();
		}
		return this.groupUsers;
	}

	/**
	 * @return the id
	 */
	@Override
	public Integer getId() {
		return this.id;
	}

	/** */
	@Override
	public String getIdentInfo() throws DbErrorException {
		return this.groupName;
	}

	/** @return the xmlUsers */
	public List<Integer> getXmlUsers() {
		this.xmlUsers = null;
		if (this.groupUsers != null && !this.groupUsers.isEmpty()) {
			this.xmlUsers = new ArrayList<>();

			for (AdmUser user : this.groupUsers) {
				this.xmlUsers.add(user.getId());
			}
		}
		return this.xmlUsers;
	}

	/** */
	@Override
	public List<Object[]> printObject(BaseSystemData sd, Integer lang, Date date) throws DbErrorException {
		List<Object[]> result = new ArrayList<>();

		result.add(new Object[] { null, "ИД Група", "GROUP_ID", this.id });
		result.add(new Object[] { null, "Име на група", "GROUP_NAME", this.groupName });
		result.add(new Object[] { null, "Описание", "GROUP_COMMENT", this.groupComment });

		StringBuilder sb = new StringBuilder();
		if (this.groupRoles != null && !this.groupRoles.isEmpty()) {
			Set<Integer> classif = new HashSet<>();
			for (AdmGroupRole role : this.groupRoles) {
				if (classif.add(role.getCodeClassif())) {
					sb.append(sd.getNameClassification(role.getCodeClassif(), lang) + "</br>");
				}
				sb.append("&emsp;" + sd.decodeItem(role.getCodeClassif(), role.getCodeRole(), lang, date) + "</br>");
			}
		}
		result.add(new Object[] { null, "Права за достъп", "ADM_GROUP_ROLES", sb.length() > 0 ? sb.toString() : null });

		sb.setLength(0);
		if (this.groupUsers != null && !this.groupUsers.isEmpty()) {
			for (AdmUser user : this.groupUsers) {
				sb.append(user.getUsername() + " (" + user.getId() + ")</br>");
			}
		}
		result.add(new Object[] { null, "Участници в групата", "ADM_USER_GROUP", sb.length() > 0 ? sb.toString() : null });

		result.addAll(super.printObject(sd, lang, date));
		return result;
	}

	/**
	 * @param groupComment the groupComment to set
	 */
	public void setGroupComment(String groupComment) {
		this.groupComment = groupComment;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @param groupRoles the groupRoles to set
	 */
	public void setGroupRoles(List<AdmGroupRole> groupRoles) {
		this.groupRoles = groupRoles;
	}

	/**
	 * @param groupUsers the groupUsers to set
	 */
	public void setGroupUsers(List<AdmUser> groupUsers) {
		this.groupUsers = groupUsers;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/** @param xmlUsers the xmlUsers to set */
	public void setXmlUsers(List<Integer> xmlUsers) {
		this.xmlUsers = xmlUsers;
	}

	/** */
	@Override
	public PersistentEntity toAuditSerializeObject(String unitName) {
		AdmGroup obj;
		try {
			obj = clone();
		} catch (CloneNotSupportedException e) {
			return this;
		}

		JPA jpa = JPA.getUtil(unitName);

		obj.groupRoles = this.groupRoles != null && jpa.isLoaded(this.groupRoles) ? new ArrayList<>(this.groupRoles) : null;
		obj.groupUsers = this.groupUsers != null && jpa.isLoaded(this.groupUsers) ? new ArrayList<>(this.groupUsers) : null;

		return obj;
	}

	/** */
	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal dj = new SystemJournal();
		dj.setCodeObject(getCodeMainObject());
		dj.setIdObject(getId());
		dj.setIdentObject(getIdentInfo());
		return dj;
	}
}