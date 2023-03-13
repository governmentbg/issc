package com.ib.nsiclassif.db.dto;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.ib.indexui.system.Constants;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.TrackableEntity;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Метаданни на Класификация
 *
 * @author dessy
 */
@Entity
@Table(name = "classification")
public class Classification extends TrackableEntity implements AuditExt{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4937970471067755831L;
	
	@SequenceGenerator(name = "Classification", sequenceName = "SEQ_CLASSIFICATION", allocationSize = 1)
	@Id
	@GeneratedValue(strategy = SEQUENCE, generator = "Classification")
	@Column(name = "ID", unique = true, nullable = false)
	private Integer id;
	
	@Column(name = "CLASS_TYPE")
	@JournalAttr(label = "classType", defaultText = "Тип класификация", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_TYPE)
	private Integer classType;
	
	@Column(name = "CLASS_UNIT")
	@JournalAttr(label = "classUnit", defaultText = "Класификационна единица", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_UNIT)
	private Integer classUnit;
	
	@Column(name = "FAMILY")
	@JournalAttr(label = "family", defaultText = "Семейство/Подсемейство", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_FAMILY)
	private Integer family;
	
	@Column(name = "FINALIZED")
	@JournalAttr(label = "finaliezd", defaultText = "Публикувана на сайта", classifID = "" + NSIConstants.CODE_CLASSIF_DANE)
	private Integer finalized;
	
	@Transient
	private boolean publicated;
	
		
	@JournalAttr(label = "langMap", defaultText = "Езикови атрибути")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL,  orphanRemoval = true,targetEntity = ClassificationLang.class)
	@JoinColumn(name = "CLASSIFICATION_ID", referencedColumnName = "ID", nullable = false,insertable = false,updatable = false)
	@MapKey(name = "lang")
	private Map<Integer,ClassificationLang> langMap = new HashMap<Integer,ClassificationLang>();
	
	@JournalAttr(label = "attributes", defaultText = "Атрибути на позициите")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "classif")	
	private List<ClassificationAttributes> attributes = new ArrayList<ClassificationAttributes>();
	
	
	public Classification() {
		super();		
	}	
	
	@Override
	public Integer getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the classType
	 */
	public Integer getClassType() {
		return classType;
	}

	/**
	 * @param classType the classType to set
	 */
	public void setClassType(Integer classType) {
		this.classType = classType;
	}

	/**
	 * @return the classUnit
	 */
	public Integer getClassUnit() {
		return classUnit;
	}

	/**
	 * @param classUnit the classUnit to set
	 */
	public void setClassUnit(Integer classUnit) {
		this.classUnit = classUnit;
	}

	/**
	 * @return the family
	 */
	public Integer getFamily() {
		return family;
	}

	/**
	 * @param family the family to set
	 */
	public void setFamily(Integer family) {
		this.family = family;
	}	
	
	@Override
	public String getIdentInfo() throws DbErrorException {
		String identInfo = "Id: "+this.id; 
		if (getLangMap() != null && getLangMap().size() > 0) {
			ClassificationLang deffLang = getLangMap().get(NSIConstants.CODE_DEFAULT_LANG) ;
			if (deffLang == null) {
				deffLang = getLangMap().entrySet().iterator().next().getValue();
			}
			identInfo = deffLang.getIdent();
		}
		
		return identInfo;
	}

	@Override
	public Integer getCodeMainObject() {		
		return NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_CLASSIF;
	}	

	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal j = new SystemJournal();
		j.setCodeObject(getCodeMainObject());
		j.setIdObject(getId());
		j.setIdentObject(getIdentInfo());
		return j;
	}

	

	public Map<Integer, ClassificationLang> getLangMap() {
		return langMap;
	}

	public void setLangMap(Map<Integer, ClassificationLang> langMap) {
		this.langMap = langMap;
	}

	public List<ClassificationAttributes> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ClassificationAttributes> attributes) {
		this.attributes = attributes;
	}

	
	public TreeHolder toTreeHolder() {
		
		TreeHolder th = new TreeHolder();
		th.setIdParent(null);
		th.setCodeObject(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_CLASSIF);
		th.setIdObject(id);
		Iterator<Entry<Integer, ClassificationLang>> it = langMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, ClassificationLang> entry = it.next();
			ClassificationLang value = entry.getValue();
			TreeHolderLang thl = new TreeHolderLang();
			if (value != null && value.getIdent() != null) {
				thl.setIdent(value.getIdent().replace("\n", ""));
			}else {
				thl.setIdent("*No Translation!");
			}
			if (value != null && value.getNameClassif() != null) {
				thl.setName(value.getNameClassif().replace("\n", ""));
			}else {
				thl.setName("*No Translation!");
			}
			thl.setLang(value.getLang());
			th.getLangMap().put(thl.getLang(), thl);
		}
		
		return th;
		
	}

	public Integer getFinalized() {
		return finalized;
	}

	public void setFinalized(Integer finalized) {
		this.finalized = finalized;
	}
	
	public boolean getPublicated() {
		if (finalized==null || finalized==Constants.CODE_ZNACHENIE_NE) {
			publicated=false;
		}else {
			publicated=true;
		}
		return publicated;
	}


	public void setPublicated(boolean publicated) {
		this.publicated = publicated;
		if (publicated) {
			finalized=Constants.CODE_ZNACHENIE_DA;			
		}else {
			finalized=Constants.CODE_ZNACHENIE_NE;
		}
	}
	
}
