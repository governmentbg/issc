package com.ib.nsiclassif.db.dto;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ib.system.db.AuditExt;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Въпроси от добра класификационна практика
 * 
 */
@Entity
@Table(name = "question")
public class Question extends TrackableEntity implements AuditExt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3575209697774300855L;

	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "ask_e_mail")
	private String askEmail;

	@Column(name = "ask_date")
	private Date askDate;
	
	@Column(name = "answer_e_mail")
	private String answerEmail;
	
	@Column(name = "answer_date")
	private Date answerDate;
	

	
	
	@Override
	public Integer getCodeMainObject() {
		return null;
	}

	@Override
	public Object getId() {
		return null;
	}

	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		return null;
	}
	
	
	
	
	public String getAskEmail() {
		return askEmail;
	}

	public void setAskEmail(String askEmail) {
		this.askEmail = askEmail;
	}

	public Date getAskDate() {
		return askDate;
	}

	public void setAskDate(Date askDate) {
		this.askDate = askDate;
	}

	public String getAnswerEmail() {
		return answerEmail;
	}

	public void setAnswerEmail(String answerEmail) {
		this.answerEmail = answerEmail;
	}

	public Date getAnswerDate() {
		return answerDate;
	}

	public void setAnswerDate(Date answerDate) {
		this.answerDate = answerDate;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	

}
