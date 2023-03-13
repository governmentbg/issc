package com.ib.nsiclassif.db.dto;

import static com.ib.indexui.system.Constants.CODE_CLASSIF_STATUS_POTREB;
import static com.ib.indexui.system.Constants.CODE_CLASSIF_TIP_POTREB;
import static com.ib.indexui.system.Constants.CODE_ZNACHENIE_JOURNAL_USER;
import static com.ib.system.SysConstants.CODE_CLASSIF_LANG;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

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
import com.ib.system.utils.DateUtils;

/**
 * AdmUser Entity
 *
 * Автор Г.Белев, адаптиран за НСИ - Стоян
 */
@Entity
@Table(name = "ADM_USERS")

public class AdmUser extends TrackableEntity implements Cloneable, AuditExt {

	/**  */
	private static final long serialVersionUID = -545631164407428931L;

	@SequenceGenerator(name = "AdmUsers", sequenceName = "SEQ_ADM_USERS", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AdmUsers")
	@JournalAttr(label = "id", defaultText = "Системен идентификатор", isId = "true")
	@Column(name = "USER_ID")
	private Integer id;

	@Column(name = "USERNAME")
	@JournalAttr(label = "username", defaultText = "Потребителско име")
	private String username; // logid - потребителят се въвежда само ако username го има в активната директория и е активен

	@Column(name = "PASSWORD")
	private String password; // pass - - не се използув - не се въвежда парола в базата - връзката е по username от активната директория        -  

	@Column(name = "EMAIL")
	@JournalAttr(label = "email", defaultText = "Емейл")
	private String email;                   // Взима се от активната директория 

	@Column(name = "USER_TYPE")
	@JournalAttr(label = "userType", defaultText = "Тип потребител", classifID = "" + Constants.CODE_CLASSIF_BUSINESS_ROLE)
	private Integer userType; // Тип (роля) на потребител  - администратор, класиф. експерт, експерт - избира  се от класификация 4 - бизнес роли

	@Column(name = "NAMES")
	@JournalAttr(label = "names", defaultText = "Имена")
	private String names; // Трите имена - взимат се от активната директория

	@Column(name = "LANG")
	@JournalAttr(label = "lang", defaultText = "Език", classifID = "" + SysConstants.CODE_CLASSIF_LANG)
	private Integer lang; // Език за работа - въвежда се винаги cuurentLang сега

	@Column(name = "STATUS")
	@JournalAttr(label = "status", defaultText = "Статус", classifID = "" + Constants.CODE_CLASSIF_STATUS_POTREB)
	private Integer status; // Статус - актуален / не

	@Column(name = "STATUS_DATE")
	@JournalAttr(label = "statusDate", defaultText = "Дата на статус")
	private Date statusDate; // Дата на установяване на статуса неактуален
	
	@Column(name = "USER_TEL")
	private String userTel;                     // Телефони - въвеждат се разделени с ;
	
	@Column(name = "USER_ADM_STR")
	private Integer userAdmStr;                       // Връзка с адм. структура
	
	@Column(name = "USER_BLOCK")
	private Integer userBlock;                     // За блокиране при многократен неправилен вход - 1 - блокиран ;  null - свободен за работо   
	
	/** Зададени роли на потребителя */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JournalAttr(label = "userRoles", defaultText = "Право")
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false)
	private List<AdmUserRole> userRoles;

	/** Зададени групи на потребителя */
	@ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinTable(name = "ADM_USER_GROUP", joinColumns = { @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", nullable = false, updatable = false) }, inverseJoinColumns = {
		@JoinColumn(name = "GROUP_ID", referencedColumnName = "GROUP_ID", nullable = false, updatable = false) })
	private List<AdmGroup> userGroups;
	
//	// Брой опити за влизане
//	@Column(name = "LOGIN_ATTEMPTS")
//	@JournalAttr(label = "loginAttempts", defaultText = "Брой опити")
//	private Integer loginAttempts;
//
//	// Дата на последна смяна на парола
//	@Column(name = "PASS_LAST_CHANGE")
//	@JournalAttr(label = "passLastChange", defaultText = "Дата на последна смяна на парола")
//	private Date passLastChange;
//
//	@Column(name = "PASS_IS_NEW")
//	private Boolean passIsNew;

//	@Column(name = "CONFIRMED")
//	private Boolean confirmed; // сетва се на TRUE при първи успешен логин.
//
//	/** дава идентификация за ИД на служител. използва се за да се зададе като ИД на потребителя при нов запис */
//	@Transient
//	private transient Integer referentId;
//
	@Transient
	@JournalAttr(label = "xmlGroups", defaultText = "Групи", codeObject = Constants.CODE_ZNACHENIE_JOURNAL_GROUPUSER)
	private List<Integer> xmlGroups;

	@Transient
	private Map<Integer, Map<Integer, Boolean>> accessValues;
 
	/** */
	public AdmUser() {
		super();
	}

	/**
	 * @param id
	 * @param username
	 * @param names
	 * @param status
	 * @param statusDate
	 * @param statusExplain
	 */
	public AdmUser(Integer id, String username, String names, Integer status, Date statusDate) {
		this.id = id;
		this.username = username;
		this.names = names;
		this.status = status;
		this.statusDate = statusDate;
//		this.statusExplain = statusExplain;
	}

	/** */
	@Override
	public AdmUser clone() throws CloneNotSupportedException {
		return (AdmUser) super.clone();
	}

	/** @return the accessValues */
	@XmlTransient
	public Map<Integer, Map<Integer, Boolean>> getAccessValues() {
		return this.accessValues;
	}

	/**
	 * @see com.ib.system.db.PersistentEntity#getCodeMainObject()
	 */
	@Override
	public Integer getCodeMainObject() {
		return CODE_ZNACHENIE_JOURNAL_USER;
	}

	/**
	 * @return the id
	 */
	@Override
	public Integer getId() {
		return this.id;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return this.username;
	}
	
	/** */
	@Override
	public String getIdentInfo() throws DbErrorException {
		return this.username;
	}

	/**
	 * @return the lang
	 */
	public Integer getLang() {
		return this.lang;
	}
	

	/**
	 * @return the names
	 */
	public String getNames() {
		return this.names;
	}


	/**
	 * @return the password
	 */
	public String getPassword() {
		return this.password;
	}

		
	/**
	 * @return the userType
	 */
	public Integer getUserType() {
		return this.userType;
	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return this.status;
	}

	/**
	 * @return the statusDate
	 */
	public Date getStatusDate() {
		return this.statusDate;
	}

//	/**
//	 * @return the statusExplain
//	 */
//	public String getStatusExplain() {
//		return this.statusExplain;
//	}
	
	/**
	 * @return the email
	 */
	public String getEmail() {
		return this.email;
	}
	
/**
 * 
 * @return userTel
 */
	public String getUserTel() {
		return userTel;
	}

	/**
	 * @return  userAdmStr
	 */
	public Integer getUserAdmStr() {
		return userAdmStr;
	}
	
	/**
	 * @return  userBlock
	 */
	public Integer getUserBlock() {
		return userBlock;
	}


	/**
	 * @return the userGroups
	 */
	@XmlTransient
	public List<AdmGroup> getUserGroups() {
		if (this.userGroups == null) {
			this.userGroups = new ArrayList<>();
		}
		return this.userGroups;
	}

	/**
	 * @return the userRoles
	 */
	public List<AdmUserRole> getUserRoles() {
		if (this.userRoles == null) {
			this.userRoles = new ArrayList<>();
		}
		return this.userRoles;
	}
	/** */
	@Override
	public List<Object[]> printObject(BaseSystemData sd, Integer langArg, Date date) throws DbErrorException {
		List<Object[]> result = new ArrayList<>();

		result.add(new Object[] { null, "ИД Потребител", "USER_ID", this.id });
		result.add(new Object[] { null, "Потребителско име", "USERNAME", this.username });
		result.add(new Object[] { null, "Електронна поща", "EMAIL", this.email });
		result.add(new Object[] { null, "Тип потребител", "USER_TYPE", sd.decodeItem(CODE_CLASSIF_TIP_POTREB, this.userType, langArg, date) });
		result.add(new Object[] { null, "Имена", "NAMES", this.names });
		result.add(new Object[] { null, "Език за работа", "LANG", sd.decodeItem(CODE_CLASSIF_LANG, this.lang, langArg, date) });
		result.add(new Object[] { null, "Статус", "STATUS", sd.decodeItem(CODE_CLASSIF_STATUS_POTREB, this.status, langArg, date) });
		result.add(new Object[] { null, "Дата на статусa", "STATUS_DATE", DateUtils.printDateFull(this.statusDate) });
//		result.add(new Object[] { null, "Статус причина", "statusExplain", this.statusExplain });
		result.add(new Object[] { null, "Телефони", "USER_TEL", this.userTel});

		StringBuilder sb = new StringBuilder();
		if (this.userRoles != null && !this.userRoles.isEmpty()) {
			Set<Integer> classif = new HashSet<>();
			for (AdmUserRole role : this.userRoles) {
				if (classif.add(role.getCodeClassif())) {
					sb.append(sd.getNameClassification(role.getCodeClassif(), langArg) + "</br>");
				}
				sb.append("&emsp;" + sd.decodeItem(role.getCodeClassif(), role.getCodeRole(), langArg, date) + "</br>");
			}
		}
		result.add(new Object[] { null, "Индивидуален достъп", "ADM_USER_ROLES", sb.length() > 0 ? sb.toString() : null });

		sb.setLength(0);
		if (this.userGroups != null && !this.userGroups.isEmpty()) {
			for (AdmGroup group : this.userGroups) {
				sb.append(group.getGroupName() + " (" + group.getId() + ")</br>");
			}
		}
		result.add(new Object[] { null, "Участие в групи", "ADM_USER_GROUP", sb.length() > 0 ? sb.toString() : null });

		result.addAll(super.printObject(sd, langArg, date));
		return result;
	}

 /**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	
	/**
	 * @param names the names to set
	 */
	public void setNames(String names) {
		this.names = names;
	}

	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @param userType the userType to set
	 */
	public void setUserType(Integer userType) {
		this.userType = userType;
	}
	
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	
	/**
	 * @param lang the lang to set
	 */
	public void setLang(Integer lang) {
		this.lang = lang;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * @param statusDate the statusDate to set
	 */
	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}

//	/**
//	 * @param statusExplain the statusExplain to set
//	 */
//	public void setStatusExplain(String statusExplain) {
//		this.statusExplain = statusExplain;
//	}
	
	/**
	 * @param userTel the userTel to set
	 */
	public void setUserTel(String userTel) {
		this.userTel = userTel;
	}

	/**
	 * @param userAdmStr the userAdmStr to set
	 */
	public void setUserAdmStr(Integer userAdmStr) {
		this.userAdmStr = userAdmStr;
	}
	
	/**
	 * @param userBlockr the userBlock to set
	 */
	public void setUserBlock(Integer userBlock) {
		this.userBlock = userBlock;
	}

	
	/** @param accessValues the accessValues to set */
	public void setAccessValues(Map<Integer, Map<Integer, Boolean>> accessValues) {
		this.accessValues = accessValues;
	}

	/**
	 * @param userGroups the userGroups to set
	 */
	public void setUserGroups(List<AdmGroup> userGroups) {
		this.userGroups = userGroups;
	}

	
	/**
	 * @param userRoles the userRoles to set
	 */
	public void setUserRoles(List<AdmUserRole> userRoles) {
		this.userRoles = userRoles;
	}

			
	/** */
	@Override
	public PersistentEntity toAuditSerializeObject(String unitName) {
		AdmUser obj;
		try {
			obj = clone();
		} catch (CloneNotSupportedException e) {
			return this;
		}

		JPA jpa = JPA.getUtil(unitName);

		obj.userRoles = this.userRoles != null && jpa.isLoaded(this.userRoles) ? new ArrayList<>(this.userRoles) : null;
		obj.userGroups = this.userGroups != null && jpa.isLoaded(this.userGroups) ? new ArrayList<>(this.userGroups) : null;

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
	
	/** @return the xmlGroups */
	public List<Integer> getXmlGroups() {
		this.xmlGroups = null;
		if (this.userGroups != null && !this.userGroups.isEmpty()) {
			this.xmlGroups = new ArrayList<>();

			for (AdmGroup group : this.userGroups) {
				this.xmlGroups.add(group.getId());
			}
		}
		return this.xmlGroups;
	}
	
	/**
	 * Идеята тука е да се създава потребител с конкретно ИД, което не се взима през SEQ. И когато има система, която ИД-то на
	 * усера се задава външно в нея се прави следното:<br>
	 * Създава се файл /src/main/resources/META-INF/orm.xml<br>
	 * В него се слага следното<br>
	 * 
	<entity-mappings>
		<entity class="com.ib.indexui.db.dto.AdmUser" name="AdmUser">
			<attributes>
				<id name="id">
					<column name="USER_ID" />
				</id>
			</attributes>
		</entity>
	</entity-mappings>
	 *
	 * и по този начин текущия генериран SEQ с анотацията се припокрива и нещата сработват.
	 */
//	@PrePersist
//	public void prePersist() {
//		if (this.referentId != null) {
//			this.id = this.referentId;
//		}
//	}
}