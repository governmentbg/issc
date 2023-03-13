package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.PositionUnits;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.Base64;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.StringUtils;

@Named
@RequestScoped
public class Position extends IndexUIbean implements Serializable {

	/**
	 * Позиции сайт
	 * 
	 */
	private static final long serialVersionUID = 4955665421466533568L;	
	static final Logger LOGGER = LoggerFactory.getLogger(Position.class);
	
	private Integer lang;
	private Integer idVersion;
	private Integer parent = 0;
	
	private TreeNode positionTree = new DefaultTreeNode("aaa", null);
	
	private List<PositionS> positionsTable;
	private List<PositionS> parentPossitions= new ArrayList<PositionS>();
	
	private PositionS selectedNode;
	private Integer positionId;
	
	private Map <Integer,Boolean> schemePosAttr = new HashMap<Integer, Boolean>();
	
	private PositionSDAO posDao;
	
	private String searchString = "";
	
	/*Брой на намерените позиции*/
	private int maxPage;
	private int resultSize=0;
    private int page = 1;
	private int rows= 15;
    private String pageInfo;
	
    private String panelInfo;
    
    private String levelsPage;
    private Map<Integer ,Integer>    mapPages;
    
    private String unitNac;
    private String unitEU;
    
   //част от текст по който ще се търси позиция във версията
  	private String searchText;
  	private List<PositionS> positionsSearch;
  	
  	private boolean showSearchData;
  	
  	private PositionS selectedItemSearch;
  	
  	private boolean optionFindCod;
  	private boolean optionFindName;
  	
	@PostConstruct
	public void initData() {
		System.out.println("Position init: ");
		
		posDao = new PositionSDAO(ActiveUser.DEFAULT);
		
		selectedNode = new PositionS();
		positionId = -1;
		
		unitNac = "";
		unitEU = "";
		
		optionFindCod = true;
		optionFindName = true;
		
		try {
			
			if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idObj"))) {
				idVersion= Integer.valueOf(JSFUtils.getRequestParameter("idObj"));
				
				
				lang = NSIConstants.CODE_DEFAULT_LANG;
				
				if (JSFUtils.getRequestParameter("lang") != null && !"".equals(JSFUtils.getRequestParameter("lang"))) {
					try { 
						lang = Integer.valueOf(JSFUtils.getRequestParameter("lang"));
						if(lang<0) { lang = NSIConstants.CODE_DEFAULT_LANG;}
					} catch (Exception e) {
						lang = NSIConstants.CODE_DEFAULT_LANG;
					}
				}
		
				if (JSFUtils.getRequestParameter("parent") != null && !"".equals(JSFUtils.getRequestParameter("parent"))) {
					try {
						parent = Integer.valueOf(JSFUtils.getRequestParameter("parent"));
						if(parent<0) { parent=0;}
					} catch (Exception e) {
						parent=0;
					}
				}
				
				if (JSFUtils.getRequestParameter("page") != null && !"".equals(JSFUtils.getRequestParameter("page"))) {
					try {
						page = Integer.valueOf(JSFUtils.getRequestParameter("page"));
						if(page<1) { page=1;}
					} catch (Exception e) {
						page=1;
					}
				}
				
				loadPositionsTbl(idVersion, parent);
				
				//----------------------------------------------------------
				levelsPage = JSFUtils.getRequestParameter("levels_page");
				
				mapPages = new HashMap<Integer, Integer>();
				if(levelsPage==null || levelsPage.trim().isEmpty()) {
					mapPages.put(parent, page);
				} else {
					
					levelsPage = new String(Base64.decode(levelsPage));
					//System.out.println("levelsPage decode -> "+levelsPage);
					mapPages = convertToMap(levelsPage);
					
					if(mapPages.containsKey(parent)) {
						mapPages.replace(parent, page);
					} else {
						mapPages.put(parent, page);
					}
					
				}
				
				levelsPage = mapPages.toString(); 
				levelsPage = Base64.encodeBytes(levelsPage.getBytes() ,Base64.DONT_BREAK_LINES);
			
				//System.out.println("levelsPage encodeBytes-> "+levelsPage);
				
								
				//System.out.println("parent--> "+JSFUtils.getRequestParameter("parent"));
				//System.out.println("parent1--> "+parent);
				
				if (JSFUtils.getRequestParameter("position") != null && !"".equals(JSFUtils.getRequestParameter("position"))) {
					
					try {
						positionId = Integer.valueOf(JSFUtils.getRequestParameter("position"));
						
						selectedNode =posDao.findById(positionId); 
						
						if(selectedNode == null) {
							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Не е намерена позиция с ид:"+positionId+" !");
							LOGGER.error("Не е намерена позиция с ид:"+positionId+" !");
						} else {
							searchString=JSFUtils.getRequestParameter("search_text");
							
							if (searchString!=null && !searchString.isBlank()) {
								//imame izbrano markirame vizualno s bold po search text
								if (selectedNode.getLangMap().get(getLang()).getOffitialTitile()!=null) {
									selectedNode.getLangMap().get(getLang()).setOffitialTitile(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getOffitialTitile(), searchString, "<b>", "</b>"));
								}
								if (selectedNode.getLangMap().get(getLang()).getLongTitle()!=null) {
									selectedNode.getLangMap().get(getLang()).setLongTitle(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getLongTitle(), searchString, "<b>", "</b>"));
								}
								if (selectedNode.getLangMap().get(getLang()).getShortTitle()!=null) {
									selectedNode.getLangMap().get(getLang()).setShortTitle(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getShortTitle(), searchString, "<b>", "</b>"));
								}
								if (selectedNode.getLangMap().get(getLang()).getAlternativeNames()!=null) {
									selectedNode.getLangMap().get(getLang()).setAlternativeNames(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getAlternativeNames(), searchString, "<b>", "</b>"));
								}
								if (selectedNode.getLangMap().get(getLang()).getIncludes()!=null) {
									selectedNode.getLangMap().get(getLang()).setIncludes(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getIncludes(), searchString, "<b>", "</b>"));
								}
								if (selectedNode.getLangMap().get(getLang()).getAlsoIncludes()!=null) {
									selectedNode.getLangMap().get(getLang()).setAlsoIncludes(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getAlsoIncludes(), searchString, "<b>", "</b>"));
								}
								if (selectedNode.getLangMap().get(getLang()).getExcludes()!=null) {
									selectedNode.getLangMap().get(getLang()).setExcludes(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getExcludes(), searchString, "<b>", "</b>"));
								}
								if (selectedNode.getLangMap().get(getLang()).getRules()!=null) {
									selectedNode.getLangMap().get(getLang()).setRules(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getRules(), searchString, "<b>", "</b>"));
								}
								if (selectedNode.getLangMap().get(getLang()).getComment()!=null) {
									selectedNode.getLangMap().get(getLang()).setComment(StringUtils.markingSearchText(selectedNode.getLangMap().get(getLang()).getComment(), searchString, "<b>", "</b>"));
								}
							}
							
							
							List<PositionUnits> units = selectedNode.getUnits();
							if(units!=null && !units.isEmpty()) {
								
								for(PositionUnits unit:units) {
									SystemClassif itemTarget = getSystemData().decodeItemLite(NSIConstants.CODE_CLASSIF_UNITS, unit.getUnit(), lang, selectedNode.getDateLastMod(), false);
									if(unit.getTypeUnit().intValue() == NSIConstants.CODE_ZNACHENIE_NACIONALNA) {
										unitNac += itemTarget.getTekst()+" <br/>";
									} else {
										unitEU += itemTarget.getTekst()+" <br/>";
									}
								}
							}
							
							
						} 
						parentPossitions.clear();
						List<Object[]> listParent = posDao.loadParentsPosition(positionId, lang);
						for(Object[] item :listParent) { 
							parentPossitions.add(new PositionS(SearchUtils.asInteger(item[0]), SearchUtils.asInteger(item[1]) ,SearchUtils.asString(item[2]),SearchUtils.asString(item[3])));
						}
					
					} catch (Exception e) {
						LOGGER.error("Грешка при зареждане на позиция с id :"+JSFUtils.getRequestParameter("position"), e);
					} 
					
				} else {
					if(parent>0) {

						List<Object[]> listParent = posDao.loadParentsPosition(parent, lang);
						for(Object[] item :listParent) { 
							parentPossitions.add(new PositionS(SearchUtils.asInteger(item[0]), SearchUtils.asInteger(item[1]) ,SearchUtils.asString(item[2]),SearchUtils.asString(item[3])));
						}
					}
				}	
				
				//попълване на информативният панел
				panelInfo =  new VersionDAO(ActiveUser.DEFAULT).decodeVersionIdentName(idVersion, lang);
			} 

			
			
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на данни за класификация!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} finally {
			JPA.getUtil().closeConnection();
		}

	}
	
	public void actionChangeLang() {

		
	}	
	
	private void loadPositionsTbl(Integer versionId,Integer parentId ) {
		parent = parentId;
		try {
			positionsTable = posDao.loadScheme(versionId, parentId,lang, null , 0);
			
			List<Integer> listAttr = posDao.loadPositionAttr(idVersion);
			
			schemePosAttr.clear();
			
			for(Integer attr : listAttr) {
				schemePosAttr.put(attr, Boolean.TRUE);
			}
			
			
			//resultSize = posDao.loadSchemeCount(versionId, parentId);
			resultSize = positionsTable.size();
			
			//само когато са повече от максималното за показване
			if (resultSize > rows){ 
				
				float t = resultSize;
                float z = rows;
				
				//проверяваме дали идва от екрана за търсене
				searchString=JSFUtils.getRequestParameter("search_text");
				
				if (searchString!=null && !searchString.isBlank()) {
					// идва от екрана за търсене
					//трябва да намерим позицията на коя страница е
					
					if (JSFUtils.getRequestParameter("position") != null && !"".equals(JSFUtils.getRequestParameter("position"))) {
						positionId = Integer.valueOf(JSFUtils.getRequestParameter("position"));
						int posNumberList = 1;
						for(PositionS ps: positionsTable) {
							
							if(ps.getId().equals(positionId)) {
								break;
							}
							
							posNumberList++;
							
						}
						
						if(posNumberList>rows) {
							float posNumberListFl = posNumberList;
							page = (int)Math.round(Math.ceil(posNumberListFl/z));
						}else {
							page=1;
						}
					} 
				} 
				
				double rez =  Math.ceil(t/z);
                
                maxPage = (int)Math.round(rez);
				
                if(page > maxPage) {page = maxPage;}
                
				int fromRow = (page-1)*rows;
				int toRow = fromRow+rows;
				
				if(fromRow<0) {fromRow = 0;}
				if(toRow >=resultSize ) {toRow = resultSize;}
                
                pageInfo = "Страница: "+page+"/"+maxPage;
				
               // System.out.println("fromRow "+fromRow);
               // System.out.println("toRow "+toRow);
				//System.out.println("resultSize "+resultSize);
                
				if(fromRow == toRow) { //sub list-а връща празен масив
					PositionS item = positionsTable.get(fromRow);
					positionsTable.clear();
					positionsTable.add(item);
				} else {
					positionsTable = positionsTable.subList(fromRow,toRow);
				}
				
            }
			
			
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на дървото с позиции loadPositionsTbl !", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	
	//--------------------------- metodi za dyrwo --------------------------------------------------------------------------------------------
	
//	private void loadTreePosition() {
//		
//		positionTree = new DefaultTreeNode("Root", null);
//		
//		addSubTree(idVersion, positionTree,0);
//		
//		
//		try {
//			List<Integer> listAttr = new ArrayList<Integer>();
//			
//			listAttr = posDao.loadPositionAttr(idVersion);
//			
//			schemePosAttr.clear();
//			
//			for(Integer attr : listAttr) {
//				schemePosAttr.put(attr, Boolean.TRUE);
//			}
//			
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при зареждане на схема на атрибути на позиция!!", e);
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при зареждане на схема на атрибути на позиция!");
//		} finally {
//			JPA.getUtil().closeConnection();
//		}
//		
//	}
//	
//	
//	
//	/**
//	 * Зарежда сдецата на конкретен нод. Само Преките наследници
//	 * Важно е да се знае, че ако нода има деца, се слага един служебен с дата="-1", за да може да се експандва дървото
//	 * После при еьпанд се проверява и ако го има това -1 се мха и се зареждат истинските
//	 * @param versionId
//	 * @param currentTmpNode
//	 * @param id - всички деца на това ид се добавят
//	 */
//	private void addSubTree(Integer versionId,TreeNode currentTmpNode,Integer id) {
//		try {
//			List<PositionS> items = posDao.loadScheme(versionId, id,lang);
//			
//			for (Iterator<PositionS> iterator = items.iterator(); iterator.hasNext();) {
//				PositionS schemeItem = iterator.next();
//				DefaultTreeNode node = new DefaultTreeNode(schemeItem, currentTmpNode);
//				if (schemeItem.getIndChild().equals(1)) {
//					node.getChildren().add(new DefaultTreeNode(-1));
//				}
//			}
//			
//		} catch (DbErrorException e) {
//			LOGGER.error("Грешка при зареждане на дървото с позиции addSubTree !", e);
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG),e.getMessage());
//		} finally {
//			JPA.getUtil().closeConnection();
//		}
//	}
//	
//
//	public void onNodeExpand(NodeExpandEvent event) {
//		TreeNode currentTmpNode = event.getTreeNode();
//		if (currentTmpNode.getChildren()!=null && currentTmpNode.getChildren().size()==1 && currentTmpNode.getChildren().get(0).getData().equals(-1)) {
//			currentTmpNode.getChildren().clear();
//			addSubTree(idVersion,currentTmpNode,((PositionS)currentTmpNode.getData()).getId());
//		}
//	}
//	
//	public void onNodeSelected(NodeSelectEvent event) {
//		selectedNode = (PositionS)event.getTreeNode().getData();
//		positionId = selectedNode.getId();
//		try {
//			selectedNode =posDao.findById(selectedNode.getId()); 
//			
//			if(selectedNode == null) {
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане на позиция с ид:"+selectedNode.getId()+" !");
//			} 
//			
//		}catch (Exception e) {
//			LOGGER.error("Грешка при зареждане от базата на позиция с ид:"+selectedNode.getId()+" !", e);
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане от базата на позиция с ид:"+selectedNode.getId()+" !", e.getMessage());
//		}finally {
//			JPA.getUtil().closeConnection();
//		}
//	}
//	
	
	//--------------------------------------------------------------------------------------------------------------------------------------------------
		
	
	public Integer getLang() {
		//System.out.println("classif getLang:"+lang);
		return lang;
	}

	public void setLang(Integer lang) {
		//System.out.println("classif setLang:"+lang);
		this.lang = lang;
	}	

	public Integer getIdVersion() {
		return idVersion;
	}

	public void setIdVersion(Integer idVersion) {
		this.idVersion = idVersion;
	}

	public TreeNode getPositionTree() {
		return positionTree;
	}

	public void setPositionTree(TreeNode positionTree) {
		this.positionTree = positionTree;
	}

	public PositionS getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(PositionS selectedNode) {
		this.selectedNode = selectedNode;
	}

	public Map<Integer, Boolean> getSchemePosAttr() {
		return schemePosAttr;
	}

	public void setSchemePosAttr(Map<Integer, Boolean> schemePosAttr) {
		this.schemePosAttr = schemePosAttr;
	}

	public Integer getPositionId() {
		return positionId;
	}

	public void setPositionId(Integer positionId) {
		this.positionId = positionId;
	}

	public List<PositionS> getPositionsTable() {
		return positionsTable;
	}

	public void setPositionsTable(List<PositionS> positionsTable) {
		this.positionsTable = positionsTable;
	}

	public List<PositionS> getParentPossitions() {
		return parentPossitions;
	}

	public void setParentPossitions(List<PositionS> parentPossitions) {
		this.parentPossitions = parentPossitions;
	}

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getPage() {
		//System.out.println("getPage: "+page);
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(String pageInfo) {
		this.pageInfo = pageInfo;
	}

	public String getPanelInfo() {
		return panelInfo;
	}

	public void setPanelInfo(String panelInfo) {
		this.panelInfo = panelInfo;
	}

	public String getLevelsPage() {
		return levelsPage;
	}

	public void setLevelsPage(String levelsPage) {
		this.levelsPage = levelsPage;
	}
	
	private Map<Integer, Integer> convertToMap(String value) {
				  
	        // New HashMap obj
	        Map<Integer, Integer> hashMap  = new HashMap<Integer, Integer>();
	        try {
	        	value = value.replace("{", "");
		        value = value.replace("}", "");
		        
		        // split the String by a comma
		        String parts[] = value.split(",");
		  
		        // iterate the parts and add them to a HashMap
		        for (String part : parts) {
		  
		            // split the student data by colon to get the
		            // name and roll number
		            String stuData[] = part.split("=");
		  
		            String stuRollNo = stuData[0].trim();
		            String stuName = stuData[1].trim();
		  
		            // Add to map
		            hashMap.put(Integer.valueOf(stuRollNo), Integer.valueOf(stuName));
		        }
	        } catch (Exception e) {
				LOGGER.error("Schupen map za straniciraneto :"+value, e);
				hashMap.put(0, 0);
			}
	        return hashMap;
	}

	public Map<Integer, Integer> getMapPages() {
		return mapPages;
	}

	public void setMapPages(Map<Integer, Integer> mapPages) {
		this.mapPages = mapPages;
	}

	public int getResultSize() {
		return resultSize;
	}

	public void setResultSize(int resultSize) {
		this.resultSize = resultSize;
	}

	public int getMaxPage() {
		return maxPage;
	}

	public void setMaxPage(int maxPage) {
		this.maxPage = maxPage;
	}

	public String getUnitNac() {
		return unitNac;
	}

	public void setUnitNac(String unitNac) {
		this.unitNac = unitNac;
	}

	public String getUnitEU() {
		return unitEU;
	}

	public void setUnitEU(String unitEU) {
		this.unitEU = unitEU;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public List<PositionS> getPositionsSearch() {
		return positionsSearch;
	}

	public void setPositionsSearch(List<PositionS> positionsSearch) {
		this.positionsSearch = positionsSearch;
	}

	public boolean isShowSearchData() {
		return showSearchData;
	}

	public void setShowSearchData(boolean showSearchData) {
		this.showSearchData = showSearchData;
	}

	public PositionS getSelectedItemSearch() {
		return selectedItemSearch;
	}

	public void setSelectedItemSearch(PositionS selectedItemSearch) {
		this.selectedItemSearch = selectedItemSearch;
	}

	public boolean isOptionFindCod() {
		return optionFindCod;
	}

	public void setOptionFindCod(boolean optionFindCod) {
		this.optionFindCod = optionFindCod;
	}
	
	public void actionSearchPosition() {
 		
 		System.out.println("find code:"+optionFindCod);
 		System.out.println("find name:"+optionFindName);
 		
 		//TODO
 		showSearchData = false;
 		selectedItemSearch=null;
 		
 		if (searchText==null || searchText.trim().isEmpty()) {
 			// 
 			JSFUtils.addMessage("position:searchText", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages,"general.pleasInsertText"));
 		} else {
 			
 			//
 			try {
 				positionsSearch = posDao.searchPositionsVersion(searchText, lang, idVersion, optionFindCod,optionFindName);
 				
 				if(positionsSearch!=null && !positionsSearch.isEmpty()) {
 					showSearchData = true;
 				} else {
 					JSFUtils.addMessage("position:searchText", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages,"general.notFoundResult"));
 				}
 			} catch (Exception e) {
 				LOGGER.error("Грешка при зареждане на дървото с позиции loadPositionsTbl !", e);
 				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
 			} finally {
 				JPA.getUtil().closeConnection();
 			}
 		}
 	}
 	
 	public void actionSearchPositionClear() {
 		showSearchData = false;
 		searchText = "";
 		positionsSearch = new ArrayList<PositionS>();
 		selectedItemSearch=null;
 	}

	public boolean isOptionFindName() {
		return optionFindName;
	}

	public void setOptionFindName(boolean optionFindName) {
		this.optionFindName = optionFindName;
	}
}
