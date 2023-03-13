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
@Table(name = "POSITION_UNITS")

public class PositionUnits implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -4937972671767755831L;
	
	@SequenceGenerator(name = "PositionUnits", sequenceName = "SEQ_POSITION_UNITS", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "PositionUnits")
	@Column(name = "ID", unique = true, nullable = false)
	@JournalAttr(label="id",defaultText = "Системен идентификатор", isId = "true")
	private Integer id;
		
	
	@ManyToOne(targetEntity = PositionS.class)
	@JoinColumn(name = "POSITION_ID",nullable = false)
	private PositionS position;
	
	@Column(name = "UNIT")
	@JournalAttr(label = "unit", defaultText = "Мерна единица", classifID = "" + NSIConstants.CODE_CLASSIF_UNITS )
	private Integer unit;
	

	@Column(name = "TYPE_UNIT")
	@JournalAttr(label = "typeUnit", defaultText = "Национална/Межународна", classifID = ""+NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES )
	private Integer typeUnit;

	public Integer getTypeUnit() {
		return typeUnit;
	}

	public void setTypeUnit(Integer typeUnit) {
		this.typeUnit = typeUnit;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@XmlTransient
	public PositionS getPosition() {
		return position;
	}

	public void setPosition(PositionS position) {
		this.position = position;
	}

	public Integer getUnit() {
		return unit;
	}

	public void setUnit(Integer unit) {
		this.unit = unit;
	}

	

	
	
	

	
	
	
}
