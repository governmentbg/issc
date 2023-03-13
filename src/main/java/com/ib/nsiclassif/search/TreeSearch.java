package com.ib.nsiclassif.search;

import java.util.List;

import org.primefaces.model.DefaultTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.TreeHolder;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.exceptions.DbErrorException;

public class TreeSearch {
	
	static final Logger LOGGER = LoggerFactory.getLogger(TreeSearch.class);

	public TreeHolder allCV = new TreeHolder();
	public DefaultTreeNode fullTree = new DefaultTreeNode(new TreeHolder(), null);
	
	private boolean activeVersion;
	
	private Integer lang;
	
	public TreeSearch() {

	}

	public void init() throws DbErrorException {
		
		if (this.lang == null) {
			this.lang = NSIConstants.CODE_DEFAULT_LANG;
		}

		allCV = new TreeHolder();
		
		//извличат се само готовите за публикуване на сайта 
		List<Classification> allClass = new ClassificationDAO(ActiveUser.DEFAULT).findAllSiteNew(lang);
		List<Version> allVers = new VersionDAO(ActiveUser.DEFAULT).findAllSiteNew();
		List<CorespTable> allTables = new CorespTableDAO(ActiveUser.DEFAULT).findAllSiteNew();
		
		
		for (Classification c : allClass) {	
			//if (c.getFinalized() != null && c.getFinalized() == Constants.CODE_ZNACHENIE_DA) { //(добавено от С.Арнаудова)				
				TreeHolder thc = c.toTreeHolder();
				
				for (Version v : allVers) {
					//if (v.getFinalized() != null && v.getFinalized() == Constants.CODE_ZNACHENIE_DA) { 	
						
						if (v.getIdClss().equals(c.getId())) {
							TreeHolder thv = v.toTreeHolder();
							thc.getChilddren().add(thv);
							
							for (CorespTable table : allTables) {							
								//if (table.getFinalized() != null && table.getFinalized() == Constants.CODE_ZNACHENIE_DA) {								
									if (table.getIdVersSource().equals(v.getId()) || table.getIdVersTarget().equals(v.getId())) {
										TreeHolder tht = table.toTreeHolder();
										thv.getChilddren().add(tht);
									}
								//}
							}
						}
					//}
				}
				
				allCV.getChilddren().add(thc);
			//}
		}

		fullTree = buildTree(null, null, allCV, null);

	}

	public DefaultTreeNode buildTree(String ident, String nameC, Integer lang) {
		if (ident == null && nameC == null) {
			return fullTree;
		} else {
			DefaultTreeNode result = buildTree(ident, nameC, allCV, lang);
			if (result == null) {
				result = new DefaultTreeNode(new TreeHolder());
			}
			return result;
		}
	}

	private DefaultTreeNode buildTree(String ident, String nameC, TreeHolder rootTh, Integer lang) {

		DefaultTreeNode root = new DefaultTreeNode(rootTh, null);
		for (TreeHolder tekTh : rootTh.getChilddren()) {
			DefaultTreeNode child = buildTree(ident, nameC, tekTh, lang);
			if (child != null) {
				root.getChildren().add(child);
			}
		}
		
		if (activeVersion && rootTh.getCodeObject() != null && rootTh.getCodeObject() == NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION) {
			// да се показват само активните версии на класификациите
			if (rootTh.isActive() && (root.getChildCount() > 0 || rootTh.containsText2(ident, nameC, lang))) {

				if ( (ident != null && !ident.isEmpty()) || (nameC != null && !nameC.isEmpty()) ) {
					root.setExpanded(true);
				}

				return root;
			} else {
				return null;
			}
		} else {
			// всички публикувани на сайта
			if (root.getChildCount() > 0 || rootTh.containsText2(ident, nameC, lang)) {

				if ( (ident != null && !ident.isEmpty()) || (nameC != null && !nameC.isEmpty()) ) {
					root.setExpanded(true);
				}

				return root;
			} else {
				return null;
			}
		}
		
		

	}

	public TreeHolder getAllCV() {
		return allCV;
	}

	public void setAllCV(TreeHolder allCV) {
		this.allCV = allCV;
	}

	public DefaultTreeNode getFullTree() {
		return fullTree;
	}

	public void setFullTree(DefaultTreeNode fullTree) {
		this.fullTree = fullTree;
	}
	

	public boolean isActiveVersion() {
		return activeVersion;
	}

	public void setActiveVersion(boolean activeVersion) {
		this.activeVersion = activeVersion;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

}
