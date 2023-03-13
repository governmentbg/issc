package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.utils.SearchUtils;

@Named
@RequestScoped
public class PositionDetail extends IndexUIbean implements Serializable {

	/**
	 * Позиции сайт
	 * 
	 */
	private static final long serialVersionUID = 4955665421466533568L;	
	static final Logger LOGGER = LoggerFactory.getLogger(PositionDetail.class);
	
	private Integer lang;
	
	
	private PositionS selectedNode;
	private Map <Integer,Boolean> schemePosAttr = new HashMap<Integer, Boolean>();
	private List<PositionS> parentPossitions= new ArrayList<PositionS>();
	
	
	private PositionSDAO posDao;
	
	@PostConstruct
	public void initData() {
		System.out.println("PositionDetail init");
		
		posDao = new PositionSDAO(ActiveUser.DEFAULT);
		
		selectedNode = new PositionS();
		Integer positionId = -1;
		parentPossitions = new ArrayList<PositionS>();
		
		try {
			
			
			
			if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idObj"))) {
				Integer idVersion= Integer.valueOf(JSFUtils.getRequestParameter("idObj"));
				
				if (JSFUtils.getRequestParameter("lang") != null && !"".equals(JSFUtils.getRequestParameter("lang"))) {
					setLang(Integer.valueOf(JSFUtils.getRequestParameter("lang")));					
				} else {
					setLang(NSIConstants.CODE_DEFAULT_LANG);
				}
		
				
				if (JSFUtils.getRequestParameter("position") != null && !"".equals(JSFUtils.getRequestParameter("position"))) {
					positionId = Integer.valueOf(JSFUtils.getRequestParameter("position"));
					try {
						selectedNode =posDao.findById(positionId); 
						
						if(selectedNode == null) {
							JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане на позиция с ид:"+selectedNode.getId()+" !");
						} else {
							List<Integer> listAttr = posDao.loadPositionAttr(idVersion);
							
							schemePosAttr.clear();
							
							for(Integer attr : listAttr) {
								schemePosAttr.put(attr, Boolean.TRUE);
							}
						}
						
						List<Object[]> listParent = posDao.loadParentsPosition(positionId, lang);
						for(Object[] item :listParent) {
							parentPossitions.add(new PositionS(SearchUtils.asInteger(item[0]), SearchUtils.asInteger(item[1]) ,SearchUtils.asString(item[2]),SearchUtils.asString(item[3])));
						}
						
					}catch (Exception e) {
						LOGGER.error("Грешка при зареждане от базата на позиция с ид:"+selectedNode.getId()+" !", e);
						JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при зареждане от базата на позиция с ид:"+selectedNode.getId()+" !", e.getMessage());
					}finally {
						JPA.getUtil().closeConnection();
					}
					
					
					
				}
				
					
				
			} 

			
			
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на данни за класификация!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

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

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public List<PositionS> getParentPossitions() {
		return parentPossitions;
	}

	public void setParentPossitions(List<PositionS> parentPossitions) {
		this.parentPossitions = parentPossitions;
	}

	
}
