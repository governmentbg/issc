package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.LevelDAO;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dto.Level;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.SchemeItem;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;

/**  @author yoncho */
@Named("exportVersii")
@ViewScoped
public class ExportVersii extends IndexUIbean implements Serializable {

	private static final long serialVersionUID = 7088380864284134879L;
	static final Logger LOGGER = LoggerFactory.getLogger(ExportVersii.class);
	
	private List<String> orderItems;
    private String razdelitel=",";
    private String headerCodes;
    private boolean generate;
    
    private DualListModel<SystemClassif> posAttrList;
    private List<Integer> listAttr;
    private Integer lang;
    private List<Level> selectedLevels;
    private List<Level> availableLevels;
    
	private TreeNode rootS = new DefaultTreeNode("aaa", null);
	private TreeNode[] selectedNodes;
	
    
    private Integer idObj;
    private Integer versionId;

    
    private String intervals;
    
    private String begs="";
    private String ends="";
    private String levels="";
    private String nodeCodes="";
    private String fName="";
    private boolean units=true;
    
    
	public ExportVersii() {

	}

	@PostConstruct
	public void initData() {
		// //LOGGER.info("INIT ExportVersii....");
		VersionEditBean mainBean = (VersionEditBean) JSFUtils.getManagedBean("versionEdit");
		if (mainBean != null && !Objects.equals(mainBean.getVersionId(), this.versionId)) {
			fName=mainBean.getVersion().getLangMap().get(NSIConstants.CODE_DEFAULT_LANG).getIdent();

			versionId = mainBean.getVersionId();
			lang = mainBean.getLang();
			if (lang == null) {
				lang = NSIConstants.CODE_DEFAULT_LANG;
			}
			Version version = mainBean.getVersion();

			posAttrList = new DualListModel<>();

			// зареждаме нивата за избор
			if (versionId != null) {
				loadLevelsInVersionList(versionId);
				selectedLevels = new ArrayList<Level>();

				StringBuilder sb = new StringBuilder();
				if(!selectedLevels.isEmpty()) {
					for (Level l : selectedLevels) {
						sb.append(l.getLevelNumber()).append(",");
					}
					if(sb.length()>0) {
						sb.deleteCharAt(sb.length() - 1);
						this.levels = sb.toString();
					}
				}
				
			
			}

			if (version == null) {
				// трябва ни versionEdit.version.id, за да извлечем всички атрибути на версията
				// на класификацията в listAttr
				idObj = 1;

			} else {
				idObj = mainBean.getVersion().getId();
				listAttr = new ArrayList<Integer>();
				loadHeadersInVersion(listAttr, idObj);
			}

		}
		// зареждаме дървото
		rootS = new DefaultTreeNode("Root", null);
		addSubTree(rootS, 0);
		// PrimeFaces.current().ajax().update(":tabPositions2:positionPanel2");

	}
	
	private void loadLevelsInVersionList(Integer versionId) {
		try {
			JPA.getUtil().runWithClose(() -> setAvailableLevels(new LevelDAO(getUserData()).levelsInVersionList(versionId)));

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на нива във версия!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}
	
	public void loadHeadersInVersion(List<Integer> listAttr, Integer idObj) {
		try {
			listAttr = new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idObj);
			posAttrList = new DualListModel<>(getSystemData()
					.getSysClassification(NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, new Date(), lang),
					new ArrayList<SystemClassif>());

			StringBuilder sb =new StringBuilder();
			// тук ще видим, кой атрибути не се срещат в класификацията
			List<SystemClassif> missingAttr = new ArrayList<>();
			for (int i = 0; i < posAttrList.getSource().size(); i++) {
				SystemClassif sc = posAttrList.getSource().get(i);
				if (!listAttr.contains(sc.getCode())) {
					missingAttr.add(sc);
				}else {
					sb.append(sc.getCode()).append(",");
				}
			}
			
			//we remove the last comma
			sb.deleteCharAt(sb.length()-1);
			setHeaderCodes( sb.toString());

			// махаме излишните атрибути от версията на класификацията
			for (SystemClassif attr : missingAttr) {
				posAttrList.getSource().remove(attr);
			}
			
			if (posAttrList.getTarget().size() > 0) {
				////LOGGER.info("posAttrList.getTarget()");
			}

			for (int i = 0; i < posAttrList.getTarget().size(); i++) {
				//LOGGER.info("i" + posAttrList.getTarget().get(i));
			}
		} catch (DbErrorException e) { // тук не се затваря сесия h2
			LOGGER.error("Грешка при разкодиране на налични атрибути за класификацията", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					"Грешка при разкодиране на налични атрибути за класификацията");
		}
	}
	
	
	public void onTransfer(TransferEvent event) {
		StringBuilder sb =new StringBuilder();
		List<SystemClassif> target= posAttrList.getTarget();
		for(SystemClassif sc:target) {
			sb.append(sc.getCode()).append(",");
		}
		//we remove the last comma
		sb.deleteCharAt(sb.length()-1);
		setHeaderCodes( sb.toString());
		

//        FacesMessage msg = new FacesMessage();
//        msg.setSeverity(FacesMessage.SEVERITY_INFO);
//        msg.setSummary("headerCodes");
//        msg.setDetail(headerCodes);
//
//        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
	
	
	   public void displaySelectedMultiple(TreeNode[] nodes) {
//		   List<Integer> levels = new ArrayList<Integer>();
//		   for(Level l:selectedLevels ) {
//			   levels.add(l.getLevelNumber());
//		   }
	        if (nodes != null && nodes.length > 0) {
	            StringBuilder sb = new StringBuilder();

	            for (TreeNode node : nodes) {
	            	//System.out.println(node.getData());
	            	SchemeItem item=((SchemeItem)node.getData());
	            	
	            	// item has the selected level or is in interval PLUS selected level
//	            	if(levels.contains(item.getLevelNumber())) {
//	            		if(!intervals.isEmpty()) {
//	            			ArrayList<Integer> intervals=(ArrayList<Integer>) transformIntervals();
//		            		if(intervals.contains(item.getCode())){
//		            			sb.append(item.getId());
//		    	                sb.append("<br />");
//		            		}
//	            		}else {
//	            			sb.append(item.getId());
//	    	                sb.append("<br />");
//	            		}
//	            		
//	            	}

	            	sb.append(item.getCode());
	                sb.append(",");
	            }
	            setNodeCodes(sb.toString());

//	            FacesMessage msg = new FacesMessage();
//	            msg.setSeverity(FacesMessage.SEVERITY_INFO);
//	            msg.setSummary("elements");
//	            msg.setDetail(sb.toString());
//
//	            FacesContext.getCurrentInstance().addMessage(null, msg);
	    		PrimeFaces.current().ajax().update("ExportMenuDown");
	    		PrimeFaces.current().ajax().update("ExportMenuUp");
	        }
	    }
	
		/**
		 * Check if the separator and intervals are valid
		 */
		public String actionRazdl() {
			/*
			 * //System.out.println("interval "+intervals); if(intervals.trim().isEmpty()) {
			 * JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
			 * "Зададените интервали не са валидират по зададени патърн, моля въведете интервала отново"
			 * ); }else { String [] inters= this.intervals.split(";"); StringBuilder sb1=new
			 * StringBuilder(); StringBuilder sb2=new StringBuilder(); if(inters.length>0) {
			 * for (String s:inters) { String [] vals=s.split(this.razdelitel);
			 * //System.out.println(Arrays.toString(vals)); // if(vals[0].isEmpty() ||
			 * vals[0].isEmpty()) { //
			 * JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
			 * "Зададените интервали не са валидират по зададени патърн, моля въведете интервала отново"
			 * ); // } if(vals.length>0) { sb1.append(vals[0]).append(",") ;
			 * sb2.append(vals[1]).append(",") ; }
			 * 
			 * //interate between all code and put all in the interval or have query for all
			 * codes between two intervals }
			 * 
			 * //we remove the last comma if(sb1.length()>0) {
			 * sb1.deleteCharAt(sb1.length()-1); setBegs(sb1.toString()); }
			 * 
			 * if(sb2.length()>0) { sb2.deleteCharAt(sb2.length()-1);
			 * setEnds(sb2.toString()); } }
			 * 
			 * }
			 */
		
			
			return "";
		}
	
	public void changeLang() {

		UserData ud = (UserData) getUserData();
		ud.setCurrentLang(lang.intValue()); 
		
		// разкодирането ползва currentLang
		listAttr= new ArrayList<Integer>();
		loadHeadersInVersion(listAttr, idObj);
		
		//сменяме езика на дървото
		rootS = new DefaultTreeNode("Root", null);
		addSubTree(rootS,0);
	}
    
	
	public void onNodeExpand(NodeExpandEvent event) {
		TreeNode currentTmpNode = event.getTreeNode();
		if (currentTmpNode.getChildren()!=null && currentTmpNode.getChildren().size()==1 && ((SchemeItem)currentTmpNode.getChildren().get(0).getData()).getId()==-1) {
			currentTmpNode.getChildren().clear();
			addSubTree(currentTmpNode,((SchemeItem)currentTmpNode.getData()).getId());
		}
	}
	 public void onNodeChecked(NodeExpandEvent event) {
			StringBuilder sb=new StringBuilder();
			//we added the new elements
//			if(!selectedNodes.equals(null)) {
//				for(TreeNode node:selectedNodes) {
//					SchemeItem item=(SchemeItem) node.getData();
//					sb.append(item.getCode()).append("<br/>");
//				}
//				this.nodeCodes=sb.toString();
//				FacesMessage msg = new FacesMessage();
//		        msg.setSeverity(FacesMessage.SEVERITY_INFO);
//		        msg.setSummary("nodeCodes");
//		        msg.setDetail(nodeCodes);
//			}
	 }
	
	/**
	 * Зарежда децата на конкретен позицията. Само Преките наследници
	 * Важно е да се знае, че ако позицията има деца, се слага един служебен с дата SchemeItem с ид "-1", за да може да се отваря дървото
	 * После при отваряне се проверява и ако го има SchemeItem с ид -1 се маха и се зареждат истинските
	 * @param versionId
	 * @param currentTmpNode
	 * @param id - всички деца на това ид се добавят
	 */
	private void addSubTree(TreeNode currentTmpNode,Integer id) {
		try {
			//Date start = new Date();
			//SchemeItem data = (SchemeItem) currentTmpNode.getData();
			
//			//System.out.println("id="+data.getId());
			List<PositionS> items = new PositionSDAO(ActiveUser.DEFAULT).loadScheme(versionId, id,lang,null,0);
			
			for (Iterator iterator = items.iterator(); iterator.hasNext();) {
				PositionS pos = (PositionS) iterator.next();
				SchemeItem schemeItem= new SchemeItem(pos.getId(),pos.getCode(),pos.getName() ,pos.getVersionId(), pos.getId() ,pos.getIdPrev(), pos.getIdParent(),pos.getLevelNumber(), pos.getIndChild());
						//ID_CLASSIF, FORM, versionId, id, id, id, versionId, id
				DefaultTreeNode node = new DefaultTreeNode(schemeItem, currentTmpNode);
				if (schemeItem.getIndChild().equals(1)) {
					node.getChildren().add(new DefaultTreeNode(new SchemeItem(-1)));
				}
			}
			////LOGGER.info("=== Get data from DB time spent --->" + (end.getTime() - start.getTime()) + " ms.");
		} catch (DbErrorException e) {
			LOGGER.error("Грешка при зареждане на дървото с позиции addSubTree !", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG),e.getMessage());
		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	
    
	
	
	public String proccess(String format) {
		StringBuilder res=new StringBuilder("downloadFile?");
		if(idObj!=null) {
			res.append("idObj="+idObj).append("&amp;");
		}
		res.append("typeObj=version&amp;");
		if(headerCodes.isBlank()) {
			res.append("headers="+headerCodes).append("&amp;");
		}
		if(lang!=null) {
			res.append("lang="+lang).append("&amp;");
		}
		if(razdelitel.isBlank()) {
			res.append("razd="+razdelitel).append("&amp;");
		}
		if(!selectedLevels.isEmpty()) {
			//generating levels
			StringBuilder sb = new StringBuilder();
			for (Level l : selectedLevels) {
				sb.append(l.getLevelNumber()).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			this.levels = sb.toString();
			
			res.append("levels="+levels).append("&amp;");
		}
		if(!begs.isBlank()) {
			res.append("begs="+begs).append("&amp;");
		}
		if(!ends.isBlank()) {
			res.append("ends="+ends).append("&amp;");
		}
		if(null!=selectedNodes && selectedNodes.length>0) {
			res.append("nodeCodes="+nodeCodes).append("&amp;");
		}
		res.append("format="+format);
		//LOGGER.info(res.toString());
		return res.toString();
	}
    
    /*==============================================================================================================================================
     * 												Geters and setters
     ==============================================================================================================================================*/


	public List<String> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<String> orderItems) {
		this.orderItems = orderItems;
	}

	public String getHeaderCodes() {
		return headerCodes;
	}


	public void setHeaderCodes(String headerCodes) {
		this.headerCodes = headerCodes;
	}


	public String getRazdelitel() {
		return razdelitel;
	}

	public void setRazdelitel(String razdelitel) {
		this.razdelitel = razdelitel;
		//System.out.println("interval get "+intervals);
//		if(intervals.trim().isEmpty()) {
//			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Зададените интервали не са валидират по зададени патърн, моля въведете интервала отново");
//		}else {
			String [] inters= this.intervals.split(";");
			//System.out.println("inters "+Arrays.toString(inters));
			StringBuilder sb1=new StringBuilder();
			StringBuilder sb2=new StringBuilder();
			ArrayList<String> codes = new ArrayList<>();
			int numCodes=0;
			if(inters.length>0 &&!intervals.trim().isEmpty()) {
				for (String s:inters) {
					String [] vals=s.split(this.razdelitel);
					//System.out.println("vals"+Arrays.toString(vals));
					if(vals.length==1) {
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Използваният разделител в интервала не съвпада с въведения такъв !");
					}else {
					if(vals.length>0) {
						sb1.append(vals[0]).append(",") ;
						sb2.append(vals[1]).append(",") ;
						codes.add(vals[0]);
						codes.add(vals[1]);
						numCodes+=2;
					}
					//System.out.println(sb1.toString());
					//System.out.println(sb2.toString());
					//interate between all code and put all in the interval or have query for all codes between two intervals
					}
				}
				
				//we remove the last comma
				if(sb1.length()>0) {
					sb1.deleteCharAt(sb1.length()-1);
					setBegs(sb1.toString());
				}
				
				if(sb2.length()>0) {
				sb2.deleteCharAt(sb2.length()-1);
				setEnds(sb2.toString());
				}
				
				//System.out.println("Begs "+begs+" End "+ends);
			}
			int checkCodes=0;
			try {
				checkCodes=new PositionSDAO(ActiveUser.DEFAULT).checkCodeExists(versionId, codes);
			} catch (DbErrorException e) {
				LOGGER.error("Грешка при проверка дали списък от кодове съществува в релация!");
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG),e.getMessage());
			}
			if(numCodes!=checkCodes) {
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Проверете наново въведените кодове за интервали, имате грешка в някой от кодовете!");
			}
//		}
	}


	public boolean isGenerate() {
		return generate;
	}

	public void setGenerate(boolean generate) {
		this.generate = generate;
	}

	public DualListModel<SystemClassif> getPosAttrList() {
		return posAttrList;
	}

	public void setPosAttrList(DualListModel<SystemClassif> posAttrList) {
		this.posAttrList = posAttrList;
	}

	public List<Level> getSelectedLevels() {
		return selectedLevels;
	}

	public void setSelectedLevels(List<Level> selectedLevels) {
		this.selectedLevels = selectedLevels;
		StringBuilder sb = new StringBuilder();
		for (Level l : selectedLevels) {
			sb.append(l.getLevelNumber()).append(",");
		}
		if(sb.length()>0) {
			sb.deleteCharAt(sb.length() - 1);
			this.levels=sb.toString();
		}

	}

	public Integer getIdObj() {
		return idObj;
	}

	public void setIdObj(Integer idObj) {
		this.idObj = idObj;
	}

	public List<Integer> getListAttr() {
		return listAttr;
	}

	public void setListAttr(List<Integer> listAttr) {
		this.listAttr = listAttr;
	}

	public List<Level> getAvailableLevels() {
		return availableLevels;
	}

	public void setAvailableLevels(List<Level> availableLevels) {
		this.availableLevels = availableLevels;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}
	
    public TreeNode getRootS() {
		return rootS;
	}

	public void setRootS(TreeNode rootS) {
		this.rootS = rootS;
	}

	public TreeNode[] getSelectedNodes() {
		return selectedNodes;
	}


	public void setSelectedNodes(TreeNode[] selectedNodes) {
		this.selectedNodes = selectedNodes;
//		StringBuilder sb=new StringBuilder();
//		if(null!=selectedNodes) {
//			for(TreeNode node:selectedNodes) {
//				SchemeItem item=(SchemeItem) node.getData();
//				//System.out.println("node.getData() "+item.getCode());
//				sb.append(item.getCode()).append(",");
//			}
//			setNodeCodes(sb.toString());
//		}
		PrimeFaces.current().ajax().update("ExportMenuDown");
		PrimeFaces.current().ajax().update("ExportMenuUp");
		
	}


	public String getNodeCodes() {
		return nodeCodes;
	}


	public void setNodeCodes(String nodeCodes) {
		this.nodeCodes = nodeCodes;
	}


	public String getfName() {
		return fName;
	}

	public void setfName(String fName) {
		this.fName = fName;
	}

	public boolean isUnits() {
		return units;
	}

	public void setUnits(boolean units) {
		LOGGER.info("in set units");
		this.units = units;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}


	public String getIntervals() {
		return intervals;
	}


	public void setIntervals(String intervals) {
		this.intervals = intervals;
	}


	public String getBegs() {
		return begs;
	}


	public void setBegs(String begs) {
		this.begs = begs;
	}


	public String getEnds() {
		return ends;
	}


	public void setEnds(String ends) {
		this.ends = ends;
	}


	public String getLevels() {
		return levels;
	}


	public void setLevels(String levels) {
		this.levels = levels;
	}


}
