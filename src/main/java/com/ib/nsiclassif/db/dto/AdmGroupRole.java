package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.ib.system.db.JournalAttr;

/**
 * AdmGroupRoles Entity
 */
@Entity
@Table(name = "ADM_GROUP_ROLES")
public class AdmGroupRole implements Serializable {

	/** */
	private static final long serialVersionUID = 2342551115626418279L;

	@SequenceGenerator(name = "AdmGroupRoles", sequenceName = "SEQ_ADM_GROUP_ROLES", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "AdmGroupRoles")
	@Column(name = "ROLE_ID")
	@JournalAttr(label = "id", defaultText = "Системен идентификатор", isId = "true")
	private Integer id;

	@Column(name = "GROUP_ID", insertable = false, updatable = false)
	private Integer groupId;

	@Column(name = "CODE_CLASSIF")
	@JournalAttr(label = "codeClassif", defaultText = "Класификация", classifID = "-1")
	private Integer codeClassif;

	@Column(name = "CODE_ROLE")
	@JournalAttr(label = "codeRole", defaultText = "Наименование", classifField = "codeClassif")
	private Integer codeRole;

	/**  */
	public AdmGroupRole() {
		super();
	}

	/**
	 * @return the codeClassif
	 */
	public Integer getCodeClassif() {
		return this.codeClassif;
	}

	/**
	 * @return the codeRole
	 */
	public Integer getCodeRole() {
		return this.codeRole;
	}

	/**
	 * @return the groupId
	 */
	public Integer getGroupId() {
		return this.groupId;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * @param codeClassif the codeClassif to set
	 */
	public void setCodeClassif(Integer codeClassif) {
		this.codeClassif = codeClassif;
	}

	/**
	 * @param codeRole the codeRole to set
	 */
	public void setCodeRole(Integer codeRole) {
		this.codeRole = codeRole;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
}