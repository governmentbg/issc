package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.Query;

import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dao.SchemeDAO;
import com.ib.nsiclassif.db.dto.PositionLang;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.SchemeItem;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
@ViewScoped
@Named("testTree")
public class TestTree implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8554500763845184227L;

	private TreeNode selected;// = "";
	private TreeNode root = new DefaultTreeNode("aaa", null);
	private TreeNode rootV = new DefaultTreeNode("aaa", null);

	private TreeNode selectedNodeV;

	private Integer versionId=1;
	
	private List<SchemeItem> positions;
	private SchemeItem selecteItem;
	private PositionS selectedPos;
	private List<SchemeItem> parentPossitions= new ArrayList<SchemeItem>();
	SchemeDAO dao;
	PositionSDAO posDao;

	private int typeOfNew;
	@PostConstruct
	public void init() {
		
		posDao= new PositionSDAO(ActiveUser.DEFAULT);
		/////////// LOAD All tree V Start
//		Date start = new Date();
//		rootV = loadTree(10);
//		Date end = new Date();
//		System.out.println("=== Time spent --->" + (end.getTime() - start.getTime()) + " ms.");
////        if (rootV!=null) {
////        	System.out.println("=== memory root -->"+ObjectSizeFetcher.getObjectSize(rootV));
////        }
//		//////////////
//		try {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			oos.writeObject(rootV);
//			oos.close();
//			System.out.println("=== memory rootV -->" + baos.size() / 1024 + " kb.");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
/////////// LOAD All tree V End

		
		
		/*
		 * root = new DefaultTreeNode("Root", null);
		 * 
		 * addSubTree(versionId, root,0);
		 * 
		 */
		
		
		try {
			SchemeDAO dao = new SchemeDAO(ActiveUser.DEFAULT);
			positions = dao.loadScheme(1, 0,1);
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
		
		
	}

	public void actionGetChildren(SchemeItem pos) {
		System.out.println("Selected pos:"+pos.getCode());
		parentPossitions.add(pos);
		try {
			SchemeDAO dao = new SchemeDAO(ActiveUser.DEFAULT);
			positions = dao.loadScheme(1, pos.getId(),1);
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}

	}
	
	public void actionSelectParent(int index) {
		System.out.println("actionSelectParent:"+index);
		SchemeItem pos = parentPossitions.get(index);
		try {
			dao = new SchemeDAO(ActiveUser.DEFAULT);
			positions = dao.loadScheme(1, pos.getIdParent(),1);
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
		parentPossitions.subList(index, parentPossitions.size()).clear();
	}
	
	public void actionDeletePos() {
		
	}
	
	/**
	 * @param type:къде (NSIConstants  --> CODE_ZNACHENIE_INSERT_BEFORE, CODE_ZNACHENIE_INSERT_AFTER, CODE_ZNACHENIE_INSERT_AS_CHILD)
	 */
	public void actionNewPos(int type) {
		System.out.println("Create new position as[1-before,2-after,3-child:"+type);
		System.out.println("parent is:"+getSelectedPos().getId());
		this.typeOfNew=type;
		PositionS p = new PositionS();
		//Tuk prawq prostotii za da mi e po-lesno pri zapisa
//		p.setSchemeItem(new SchemeItem());
//		if (type==3) {
//			p.getSchemeItem().setIdParent(selecteItem.getId());
//		}else {
//			p.getSchemeItem().setIdParent(selecteItem.getIdParent());
//		}
		
		p.setLevelNumber(type==3?selecteItem.getLevelNumber()+1:selecteItem.getLevelNumber());
		
		PositionLang lang = new PositionLang();
		lang.setLang(1);
		lang.setPosition(p);
		p.getLangMap().put(1, lang);
		setSelectedPos(p);
	}
	
	public void actionSave() {
		PositionS selectedPos2 = getSelectedPos();
		System.out.println(selectedPos2);
		
		try {
			JPA.getUtil().begin();
			//Update
			if (selectedPos2.getId()!=null) {
				posDao.save(selectedPos2);
			//New insert
			}else{
				System.out.println("=== its new type:"+typeOfNew);
				System.out.println("selectedItem id:"+selecteItem.getId());
				System.out.println(selectedPos2);
				posDao.saveSchemePosition(selectedPos2, typeOfNew, selecteItem.getId(), versionId);
			}
			JPA.getUtil().commit();
			//Refresh
			positions = dao.loadScheme(1, selectedPos2.getIdParent(),1);
			//if position is new and item is new children add current in stack
			if (typeOfNew==3) {
				parentPossitions.add(selecteItem);
				//select new as cyrrent
				selecteItem=positions.get(0);
				typeOfNew=0;
			}else if (typeOfNew!=0) {
				//select new as current
				selecteItem=positions.get(0);
			}
			
		} catch (DbErrorException e) {
			JPA.getUtil().rollback();
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
//		
	}
	
	private TreeNode loadTree(Integer vesrionId) {
		SchemeDAO dao = new SchemeDAO(ActiveUser.DEFAULT);
		TreeNode node = new DefaultTreeNode();
		try {

			// Integer vesrionId = 10;

			String sql = " from SchemeItem where versionId = :ViD order by levelNumber";

			Query q = JPA.getUtil().getEntityManager().createQuery(sql);
			q.setParameter("ViD", vesrionId);

			Date start = new Date();

			ArrayList<SchemeItem> scheme = (ArrayList<SchemeItem>) q.getResultList();
			Date end = new Date();
			System.out.println("=== Get data from DB time spent --->" + (end.getTime() - start.getTime()) + " ms.");

			start = new Date();
			node = dao.loadTreeData3(scheme, null, false, false, null, null);
			end = new Date();
			System.out.println("=== BuildTree time spent --->" + (end.getTime() - start.getTime()) + " ms.");

		} catch (Exception e) {
			e.printStackTrace();

		}
		return node;

	}

	public void onNodeExpand(NodeExpandEvent event) {
//        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Expanded", event.getTreeNode().toString());
//        FacesContext.getCurrentInstance().addMessage(null, message);
		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Expand", event.getTreeNode().toString());
		TreeNode currentTmpNode = event.getTreeNode();
//		п
		if (currentTmpNode.getChildren()!=null && currentTmpNode.getChildren().size()==1 && currentTmpNode.getChildren().get(0).getData().equals(-1)) {
			currentTmpNode.getChildren().clear();
			addSubTree(versionId,currentTmpNode,((SchemeItem)currentTmpNode.getData()).getId());
		}
		
		TreeNode treeNode = currentTmpNode;
		
		System.out.println("--- rowKey:"+treeNode.getRowKey());
		
		System.out.println("--Parent rowKey:"+treeNode.getParent().getRowKey());
		
//		event.getTreeNode().getChildren().add(new DefaultTreeNode("c1"));
//		event.getTreeNode().getChildren().add(new DefaultTreeNode("c2"));

	}

	public void test() {
		SchemeItem data = (SchemeItem) root.getChildren().get(0).getData();
		data.setCode("1111");
		System.out.println((SchemeItem) root.getChildren().get(0).getData());
		root.getChildren().add(0, new DefaultTreeNode(data));
		PrimeFaces.current().ajax().update("testForm:testTree:0");
		Ajax.update("testForm:testTree");
	}
	/**
	 * Зарежда сдецата на конкретен нод. Само Преките наследници
	 * Важно е да се знае, че ако нода има деца, се слага един служебен с дата="-1", за да може да се експандва дървото
	 * После при еьпанд се проверява и ако го има това -1 се мха и се зареждат истинските
	 * @param versionId
	 * @param currentTmpNode
	 * @param id - всички деца на това ид се добавят
	 */
	private void addSubTree(Integer versionId,TreeNode currentTmpNode,Integer id) {
		try {
			//SchemeItem data = (SchemeItem) currentTmpNode.getData();
			SchemeDAO dao = new SchemeDAO(ActiveUser.DEFAULT);
//			System.out.println("id="+data.getId());
			List<SchemeItem> items = dao.loadScheme(1, id,1);
			
			for (Iterator iterator = items.iterator(); iterator.hasNext();) {
				SchemeItem schemeItem = (SchemeItem) iterator.next();
				DefaultTreeNode node = new DefaultTreeNode(schemeItem, currentTmpNode);
				if (schemeItem.getIndChild().equals(1)) {
					node.getChildren().add(new DefaultTreeNode(-1));
				}
			}
			
			
			
			
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}

	public void onNodeSelect(NodeSelectEvent event) {
//	        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", event.getTreeNode().toString());
//	        FacesContext.getCurrentInstance().addMessage(null, message);
		TreeNode treeNode = event.getTreeNode();
		setSelected(treeNode);
		System.out.println("--- rowKey:"+treeNode.getRowKey());
	}

	public void onNodeSelectV(NodeSelectEvent event) {
//        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", event.getTreeNode().toString());
//        FacesContext.getCurrentInstance().addMessage(null, message);
		setSelectedNodeV(event.getTreeNode());

	}

	public TreeNode getSelectedNodeV() {
		return selectedNodeV;
	}

	public void setSelectedNodeV(TreeNode selectedNode) {
		this.selectedNodeV = selectedNode;
	}

	public TreeNode getRoot() {
		return root;
	}

	public void setRoot(TreeNode root) {
		this.root = root;
	}

	public TreeNode getSelected() {
		return selected;
	}

	public void setSelected(TreeNode selected) {
		this.selected = selected;
	}

	public TreeNode getRootV() {
		return rootV;
	}

	public void setRootV(TreeNode rootV) {
		this.rootV = rootV;
	}

	public List getPositions() {
		return positions;
	}

	public void setPositions(List positions) {
		this.positions = positions;
	}

	public PositionS getSelectedPos() {
		return selectedPos;
	}

	public void setSelectedPos(PositionS selectedPos) {
		this.selectedPos = selectedPos;
	}

	public List<SchemeItem> getParentPossitions() {
		return parentPossitions;
	}

	public void setParentPossitions(List<SchemeItem> parentPossitions) {
		this.parentPossitions = parentPossitions;
	}

	public SchemeItem getSelecteItem() {
		return selecteItem;
	}

	public void setSelecteItem(SchemeItem selecteItem) {
		this.selecteItem = selecteItem;
		try {
			selectedPos=posDao.findById(selecteItem.getId());
		}catch (Exception e) {
			e.printStackTrace();
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}
	}
}
