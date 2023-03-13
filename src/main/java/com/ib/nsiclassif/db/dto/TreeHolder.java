package com.ib.nsiclassif.db.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TreeHolder {
	
	Integer codeObject;
	Integer idObject;
	Integer indChild;
	Integer idParent;
	Integer idParent2;
	
	boolean active;
	
	List<TreeHolder> childdren = new ArrayList<TreeHolder>();
	
	HashMap<Integer, TreeHolderLang> langMap = new HashMap<Integer, TreeHolderLang>();
	

	public Integer getCodeObject() {
		return codeObject;
	}

	public void setCodeObject(Integer codeObject) {
		this.codeObject = codeObject;
	}

	public Integer getIdObject() {
		return idObject;
	}

	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}

	

	public Integer getIndChild() {
		return indChild;
	}

	public void setIndChild(Integer indChild) {
		this.indChild = indChild;
	}

	public Integer getIdParent() {
		return idParent;
	}

	public void setIdParent(Integer idParent) {
		this.idParent = idParent;
	}

	public HashMap<Integer, TreeHolderLang> getLangMap() {
		return langMap;
	}

	public void setLangMap(HashMap<Integer, TreeHolderLang> langMap) {
		this.langMap = langMap;
	}
	

	public List<TreeHolder> getChilddren() {
		return childdren;
	}

	public void setChilddren(List<TreeHolder> childdren) {
		this.childdren = childdren;
	}

	public Integer getIdParent2() {
		return idParent2;
	}

	public void setIdParent2(Integer idParent2) {
		this.idParent2 = idParent2;
	}
	
	
	public boolean containsText(String searchString, Integer lang) {
		
		if (searchString == null || searchString.trim().isEmpty()) {
			return true;
		}
		
		TreeHolderLang langValue = getLangMap().get(lang);
		if (langValue == null) {
			return false;
		}
		
		String textToSearch = "";
		if (langValue.getIdent() != null) {
			textToSearch += " " + langValue.getIdent();  
		}
		if (langValue.getName() != null) {
			textToSearch += " " + langValue.getName();  
		}
		textToSearch = textToSearch.toUpperCase();
		
		String[] words = searchString.toUpperCase().split(" ");
		for (String word : words) {
			if (!textToSearch.contains(word)) {
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Копие на containsText(String searchString, Integer lang), но търси по 
	 * идентификатор и наименование
	 * 
	 */
	public boolean containsText2(String ident, String nameC, Integer lang) {

		if ( (ident == null || ident.trim().isEmpty()) & (nameC == null || nameC.trim().isEmpty())) {
			return true;
		}
		
		TreeHolderLang langValue = getLangMap().get(lang);
		if (langValue == null) {
			return false;
		}
		
		String textToSearch = "";
		if ( (ident != null && !ident.isEmpty()) | langValue.getIdent() != null) {
			textToSearch += " " + langValue.getIdent();  
		}
		if ( (nameC != null && !nameC.isEmpty()) | langValue.getName() != null) {
			textToSearch += " " + langValue.getName();  
		}
		textToSearch = textToSearch.toUpperCase();
		
		String searchString = "";
		if (ident != null && !ident.isEmpty()) {
			searchString += ident;
		}
		if (nameC != null && !nameC.isEmpty()) {
			searchString += nameC;
		}
		
		System.out.println("searchstring" + searchString);
		String[] words = searchString.toUpperCase().split(" ");
		for (String word : words) {
			if (!textToSearch.contains(word)) {
				return false;
			}
		}
		
		return true;
	}
	
	

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	
	
}
