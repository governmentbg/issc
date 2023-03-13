package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.UnexpectedResultException;

@Named
@ViewScoped
public class ClassificationEdit extends IndexUIbean implements Serializable {

	/**
	 * Въвеждане / актуализация на метаданни на класификация
	 * 
	 */
	private static final long serialVersionUID = -5230588512749055546L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationEdit.class);
	
	private Date decodeDate = new Date();
	
	private List<SelectItem> posAttrList;
	
	/** 
	 * 
	 * 
	 **/
	@PostConstruct
	public void initData() {
		
		LOGGER.debug("PostConstruct - ClassificationEdit!!!");
		
		try {
			
			this.posAttrList = createItemsList(false, NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, this.decodeDate, null);
		
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане атрибути на позиции!", e);
		
		} catch (UnexpectedResultException e) {
			LOGGER.error("Грешка при зареждане атрибути на позиции!", e);
		}
	}
	
/******************************************************* GET & SET *******************************************************/	
	
	public Date getDecodeDate() {
		return new Date(decodeDate.getTime()) ;
	}

	public void setDecodeDate(Date decodeDate) {
		this.decodeDate = decodeDate != null ? new Date(decodeDate.getTime()) : null;
	}	

	public List<SelectItem> getPosAttrList() {
		return posAttrList;
	}

	public void setPosAttrList(List<SelectItem> posAttrList) {
		this.posAttrList = posAttrList;
	}

}
