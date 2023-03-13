package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.JournalAttr;


@SqlResultSetMapping(
		name="KrasiGResultSet",
		columns={@ColumnResult(name="CODE_ATTRIB",type=Integer.class)})

@Entity
@Table(name = "CLASSIFICATION_ATTRIBUTES")

public class ClassificationAttributes implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -4937972271767755831L;
	
	@SequenceGenerator(name = "ClassificationAttributes", sequenceName = "SEQ_CLASSIFICATION_ATTRIBUTES", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "ClassificationAttributes")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
		
	
	@ManyToOne(targetEntity = Classification.class)
	@JoinColumn(name = "CLASSIFICATION_ID",nullable = false)
	private Classification classif;
	
	@Column(name = "CODE_ATTRIB")
	@JournalAttr(label = "codeAttrib", defaultText = "Атрибут", classifID = "" + NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES)
	private Integer codeAttrib;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	

	@XmlTransient
	public Classification getClassif() {
		return classif;
	}

	public void setClassif(Classification classif) {
		this.classif = classif;
	}

	public Integer getCodeAttrib() {
		return codeAttrib;
	}

	public void setCodeAttrib(Integer codeAttrib) {
		this.codeAttrib = codeAttrib;
	}
	
	
		

	
	
	
}
