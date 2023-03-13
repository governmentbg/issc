package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;

/**  @author yoncho */
@Named("exportRalacii")
@ViewScoped
public class ExportRalacii extends IndexUIbean implements Serializable {


	private static final long serialVersionUID = -4926732144779835760L;
	static final Logger LOGGER = LoggerFactory.getLogger(ExportRalacii.class);
	
	private List<String> orderItems;
    private String razdelitel=":";
    private String[] selectedItems;
    private String headersAsString="Код източник,Текст източник,Код цел,Текст цел,Характер на промяната";
    
    private boolean generate;
    
    private List<SystemClassif> symbols;
    
    private List<SystemClassif> typeRelation;
    private List<SystemClassif> selectedTypeRelation;
    private String typeRel;
    
    private List<SystemClassif> typeChangeRelation;
    private List<SystemClassif> selectedTypeChangeRelation;
    private String changeRel;
    
    private Integer idObj;
    private Integer lang;
    private String fName="";
    
    
    
	@PostConstruct
	public void initData() {
		LOGGER.info("INIT exportRalacii....");
		CorespTableBean mainBean = (CorespTableBean) JSFUtils.getManagedBean("corespTableBean");
		if (mainBean != null) {
			fName=mainBean.getCorespTableLang().getIdent();
		}

		
		lang = mainBean.getLang();
		if (lang == null) {
			lang = NSIConstants.CODE_DEFAULT_LANG;
		}
		selectedTypeRelation=new ArrayList<SystemClassif>();
		selectedTypeChangeRelation=new ArrayList<SystemClassif>();
		loadHeadersInVersion();
	}
	
	
	
	public void loadHeadersInVersion() {
		try {
			symbols=getSystemData()
					.getSysClassification(NSIConstants.CODE_CLASSIF_EXPLANATION, new Date(), lang);
			
			typeRelation=getSystemData()
					.getSysClassification(NSIConstants.CODE_CLASSIF_RELATION_TYPE, new Date(), lang);

			typeChangeRelation=getSystemData()
					.getSysClassification(NSIConstants.CODE_CLASSIF_CHANGE_TYPE, new Date(), lang);

		} catch (DbErrorException e) { // тук не се затваря сесия h2
			LOGGER.error("Грешка при разкодиране на налични атрибути за класификацията", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					"Грешка при разкодиране на налични атрибути за класификацията");
		}
	}
    
	public ExportRalacii() {
            orderItems = new ArrayList<>();
            loadAttributesLang();
        	selectedItems=new  String[orderItems.size()] ;
    }
	
	public void loadAttributesLang() {
		 if (lang == null) {
 			lang = NSIConstants.CODE_DEFAULT_LANG;
 		}
        if( lang == NSIConstants.CODE_DEFAULT_LANG) {
     	   orderItems.add("Код източник");
     	   orderItems.add("Текст източник");
           orderItems.add("Код цел");
           orderItems.add("Текст цел");
           orderItems.add("Характер на промяната");
           headersAsString="";
        }else {
     	   orderItems.add("Code source");
     	   orderItems.add("Text source");
           orderItems.add("Code target");
           orderItems.add("Text target");
           orderItems.add("Change character");
           headersAsString="";
        }
	}
	
//	public void updateSelected() {
//		headersAsString="";
//		for(String item:selectedItems) {
//			headersAsString+=item+",";
//		}
//		//mahame spodlednata zapetaq
//		headersAsString=headersAsString.substring(0,headersAsString.length()-1);
//		LOGGER.info("updateSelected");
//		LOGGER.info("headersAsString  "+headersAsString);
//		
//    }
	  
	/**
	 * Check if the separator is valid
	 */
	public String actionRazdl() {
		return "";
	}
	
	
	public void changeLang() {

		UserData ud = (UserData) getUserData();
		ud.setCurrentLang(lang.intValue()); 
		
		// разкодирането ползва currentLang
		//listAttr= new ArrayList<Integer>();
		//loadHeadersInVersion(listAttr, idObj);
		 orderItems = new ArrayList<>();
		 loadAttributesLang();
		 
		// разкодирането ползва currentLang
		 loadHeadersInVersion();
		
	}


	public List<String> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<String> orderItems) {
		this.orderItems = orderItems;
	}

	public String getRazdelitel() {
		return razdelitel;
	}

	public void setRazdelitel(String razdelitel) {
		this.razdelitel = razdelitel;
	}

    public String[] getSelectedItems() {
		return selectedItems;
	}

	public void setSelectedItems(String[] selectedItems) {
		ArrayList<String> oldSelection=new ArrayList<String>();
		ArrayList<String> newSelection=new ArrayList<String>();
		
		for(String s: headersAsString.split(",")){
			oldSelection.add(s);
			
		}
		
		for(String s:selectedItems) {
			newSelection.add(s);
		}
		
		//we added the new elements
		for(String item:selectedItems) {
			if(!oldSelection.contains(item)) {
				oldSelection.add(item);//we have added the new element
			}
		}
		//we have removed the old elements
		List<String> deletedElements=new ArrayList<String>();
		for(String el:oldSelection) {
			if(!newSelection.contains(el)) {
				deletedElements.add(el);//if element is removed we removed it too
			}
		}
		for(String del:deletedElements) {
			oldSelection.remove(del);
		}
		
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<oldSelection.size();i++) {
			sb.append(oldSelection.get(i)).append(",");
		}
		
		//we remove the last comma
		sb.deleteCharAt(sb.length()-1);
		headersAsString=sb.toString();
		
		LOGGER.info("headersAsString  "+headersAsString);
		this.selectedItems = selectedItems;
	}
	
	public void checkType(SelectEvent<SystemClassif> event) {
		SystemClassif sc=(SystemClassif)event.getSource();
		selectedTypeRelation.add(sc);
	    System.out.println("in checkType"+sc.getTekst());
	}
	
	public void checkChange(UnselectEvent<SystemClassif> event) {
		SystemClassif sc=(SystemClassif)event.getSource();
		//selectedTypeChangeRelation.remove(sc);
	    System.out.println("in checkChange" +sc.getTekst());
	}
	
	public void checkType(UnselectEvent<SystemClassif> event) {
		SystemClassif sc=(SystemClassif)event.getSource();
		//selectedTypeRelation.remove(sc);
	    System.out.println("in checkType"+sc.getTekst());
	}
	

    public String getHeadersAsString() {
		return headersAsString;
	}

	public void setHeadersAsString(String headersAsString) {
		this.headersAsString = headersAsString;
	}
	
	public boolean isGenerate() {
		return generate;
	}


	public void setGenerate(boolean generate) {
		this.generate = generate;
	}
	
	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public List<SystemClassif> getSymbols() {
		return symbols;
	}



	public void setSymbols(List<SystemClassif> symbols) {
		this.symbols = symbols;
	}



	public List<SystemClassif> getTypeRelation() {
		return typeRelation;
	}



	public void setTypeRelation(List<SystemClassif> typeRelation) {
		this.typeRelation = typeRelation;
	}



	public List<SystemClassif> getTypeChangeRelation() {
		return typeChangeRelation;
	}



	public void setTypeChangeRelation(List<SystemClassif> typeChangeRelation) {
		this.typeChangeRelation = typeChangeRelation;
	}



	public List<SystemClassif> getSelectedTypeRelation() {
		return selectedTypeRelation;
	}



	public void setSelectedTypeRelation(List<SystemClassif> selectedTypeRelation) {
		this.selectedTypeRelation = selectedTypeRelation;
		LOGGER.info("selectedTypeRelation "+selectedTypeRelation.size());
		StringBuilder sb = new StringBuilder();
		if(!selectedTypeRelation.isEmpty()) {
			for (SystemClassif sc : selectedTypeRelation) {
				sb.append(sc.getCode()).append(",");
			}
		}

		if(sb.length()>0) {
			sb.deleteCharAt(sb.length() - 1);
			setTypeRel(sb.toString());
		}
		LOGGER.info("typeRel "+typeRel);
	}



	public List<SystemClassif> getSelectedTypeChangeRelation() {
		return selectedTypeChangeRelation;
	}



	public void setSelectedTypeChangeRelation(List<SystemClassif> selectedTypeChangeRelation) {
		LOGGER.info("selectedTypeChangeRelation "+selectedTypeChangeRelation.size());
		this.selectedTypeChangeRelation = selectedTypeChangeRelation;
		StringBuilder sb = new StringBuilder();
		if(!selectedTypeChangeRelation.isEmpty()) {
			for (SystemClassif sc : selectedTypeChangeRelation) {
				sb.append(sc.getCode()).append(",");
			}
		}

		if(sb.length()>0) {
			sb.deleteCharAt(sb.length() - 1);
			setChangeRel(sb.toString());
		}
		LOGGER.info("changeRel "+changeRel);
	}


	public String getTypeRel() {
		return typeRel;
	}



	public void setTypeRel(String typeRel) {
		this.typeRel = typeRel;
	}



	public String getChangeRel() {
		return changeRel;
	}



	public void setChangeRel(String changeRel) {
		this.changeRel = changeRel;
	}



	public Integer getIdObj() {
		return idObj;
	}

	public void setIdObj(Integer idObj) {
		this.idObj = idObj;
	}



	public String getfName() {
		return fName;
	}



	public void setfName(String fName) {
		this.fName = fName;
	}


}
