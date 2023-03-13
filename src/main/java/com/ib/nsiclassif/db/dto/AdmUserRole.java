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
 * AdmUserRoles Entity
 */
@Entity
@Table(name = "ADM_USER_ROLES")
public class AdmUserRole implements Serializable {

	private static final long serialVersionUID = -545631164407428931L;

	@SequenceGenerator(name = "UserRoles", sequenceName = "SEQ_ADM_USER_ROLES", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "UserRoles")
	@Column(name = "ROLE_ID")
	@JournalAttr(label = "id", defaultText = "Системен идентификатор", isId = "true")
	private Integer id; // id

	@Column(name = "USER_ID", insertable = false, updatable = false)
	private Integer userId; // id User

	@Column(name = "CODE_CLASSIF")
	@JournalAttr(label = "codeClassif", defaultText = "Класификация", classifID = "-1")
	private Integer codeClassif; // Код класификация

	@Column(name = "CODE_ROLE")
	@JournalAttr(label = "codeRole", defaultText = "Наименование", classifField = "codeClassif")
	private Integer codeRole; // Код на ролята от класификацията

	/**  */
	public AdmUserRole() {
		super();
	}

	/**
	 * @param codeClassif
	 * @param codeRole
	 */
	public AdmUserRole(Integer codeClassif, Integer codeRole) {
		this.codeClassif = codeClassif;
		this.codeRole = codeRole;
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
	 * @return the id
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * @return the userId
	 */
	public Integer getUserId() {
		return this.userId;
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
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
  
	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}