package com.ib.nsiclassif.db.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;

/**
 * DAO for {@link Position}
 *
 * @author mamun
 */
public class PositionDAO extends AbstractDAO<PositionS> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(PositionDAO.class);
	
	SchemeDAO sdao = null; 

	/** @param user */
	public PositionDAO(ActiveUser user) {
		super(user);	
		sdao = new SchemeDAO(user);
	}

	
//	/** Записва нова позиция преди/след/като дете на елемент от схема
//	 * @param position Позиция
//	 * @param typeAction - къде (NSIConstants  --> CODE_ZNACHENIE_INSERT_BEFORE, CODE_ZNACHENIE_INSERT_AFTER, CODE_ZNACHENIE_INSERT_AS_CHILD)
//	 * @param idSelectedSchemeItem Избран елемент от схемата
//	 * @return записана позиция
//	 * @throws DbErrorException 
//	 */
//	public Position saveSchemePosition(Position position, Integer typeAction, Integer idSelectedSchemeItem, Integer versionId) throws DbErrorException {
//		
//		if (position == null || position.getId() != null || position.getSchemeItem() != null || (typeAction == null && idSelectedSchemeItem != null) || (idSelectedSchemeItem == null && typeAction != null)  || versionId == null) {
//			LOGGER.error("position="+position);
//			LOGGER.error("typeAction="+typeAction);
//			LOGGER.error("idSelectedSchemeItem="+idSelectedSchemeItem);
//			LOGGER.error("versionId="+versionId);
//			if (position != null) {
//				LOGGER.error("position.getId="+position.getId());
//				LOGGER.error("position.getSchemeItem="+position.getSchemeItem());
//			}
//			throw new InvalidParameterException("Невалидни входни параметри за savePosition");
//		}
//		
//		try {
//			
//			position = save(position);
//			String name = "Не е въведено";
//			 PositionLang pLang = position.getLangMap().get(NSIConstants.CODE_DEFAULT_LANG);
//			 if (pLang != null && pLang.getOffitialTitile() != null && ! pLang.getOffitialTitile().trim().isEmpty()) {
//				 name = pLang.getOffitialTitile();
//			 }else {
//				 if (position.getLangMap().size() > 0) {
//					 pLang = position.getLangMap().entrySet().iterator().next().getValue();
//					 if (pLang != null && pLang.getOffitialTitile() != null && ! pLang.getOffitialTitile().trim().isEmpty()) {
//						 name = pLang.getOffitialTitile();
//					 }
//					 
//				 }
//			 }
//					 
//					 
//			if (idSelectedSchemeItem == null || idSelectedSchemeItem.equals(0))	{
//				
//				/** CASE 1 - Първо значение в схемата */
//				LOGGER.debug("savePosition - CASE 1");
//				
//				SchemeItem newItem = new SchemeItem();
//				newItem.setCode(position.getCode());
//				newItem.setDeffName(name);
//				newItem.setIdParent(0);
//				newItem.setIdPrev(0);
//				newItem.setIndChild(0);
//				newItem.setLevelNumber(1);
//				newItem.setPositionId(position.getId());
//				newItem.setVersionId(versionId);
//				
//				newItem = sdao.save(newItem);
//				position.setSchemeItem(newItem);
//				
//				
//			}else {
//			
//			
//				if (typeAction.equals(NSIConstants.CODE_ZNACHENIE_INSERT_AFTER)) {
//					
//					SchemeItem nextItem = sdao.findByIdPrev(idSelectedSchemeItem);
//					if (nextItem != null) {
//						
//						/** CASE 2 - Вмъкване след */
//						LOGGER.debug("savePosition - CASE 2");
//					
//						SchemeItem newItem = new SchemeItem();
//						newItem.setCode(position.getCode());
//						newItem.setDeffName(name);
//						newItem.setIdParent(nextItem.getIdParent());
//						newItem.setIdPrev(idSelectedSchemeItem);
//						newItem.setIndChild(0);
//						newItem.setLevelNumber(nextItem.getLevelNumber());
//						newItem.setPositionId(position.getId());
//						newItem.setVersionId(nextItem.getVersionId());
//						
//						newItem = sdao.save(newItem);
//						position.setSchemeItem(newItem);
//						
//						nextItem.setIdPrev(newItem.getId());
//						
//						nextItem = sdao.save(nextItem);
//					}else {							
//						/** CASE 3 - Вмъкване след на последно значение */
//						LOGGER.debug("savePosition - CASE 3");
//						
//						SchemeItem selItem = sdao.findById(idSelectedSchemeItem);
//						
//						SchemeItem newItem = new SchemeItem();
//						newItem.setCode(position.getCode());
//						newItem.setDeffName(name);
//						newItem.setIdParent(selItem.getIdParent());
//						newItem.setIdPrev(idSelectedSchemeItem);
//						newItem.setIndChild(0);
//						newItem.setLevelNumber(selItem.getLevelNumber());
//						newItem.setPositionId(position.getId());
//						newItem.setVersionId(selItem.getVersionId());
//						
//						newItem = sdao.save(newItem);
//						position.setSchemeItem(newItem);
//						
//						
//					}
//				}
//				
//				if (typeAction.equals(NSIConstants.CODE_ZNACHENIE_INSERT_BEFORE)) {
//					
//					/** CASE 4 - Вмъкване преди */
//					LOGGER.debug("savePosition - CASE 4");
//					
//					SchemeItem selItem = sdao.findById(idSelectedSchemeItem);
//					if (selItem != null) {
//					
//						SchemeItem newItem = new SchemeItem();
//						newItem.setCode(position.getCode());
//						newItem.setDeffName(name);
//						newItem.setIdParent(selItem.getIdParent());
//						newItem.setIdPrev(selItem.getIdPrev());
//						newItem.setIndChild(0);
//						newItem.setLevelNumber(selItem.getLevelNumber());
//						newItem.setPositionId(position.getId());
//						newItem.setVersionId(selItem.getVersionId());
//						
//						newItem = sdao.save(newItem);
//						position.setSchemeItem(newItem);
//						
//						selItem.setIdPrev(newItem.getId());
//						
//						selItem = sdao.save(selItem);
//					}else {							
//						throw new DbErrorException("Повреденa схема ма версия с id = " + versionId + " Липсва значение с id = " + idSelectedSchemeItem);
//					}
//				}
//				
//				
//				if (typeAction.equals(NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD)) {
//					
//					SchemeItem firstItem = sdao.findFirstChild(idSelectedSchemeItem);
//					if (firstItem == null) {
//
//						/** CASE 5 - Запис на първо дете */
//						LOGGER.debug("savePosition - CASE 5");
//												
//						SchemeItem parItem = sdao.findById(idSelectedSchemeItem);
//						if (parItem != null) {						
//							
//							SchemeItem newItem = new SchemeItem();
//							newItem.setCode(position.getCode());
//							newItem.setDeffName(name);
//							newItem.setIdParent(idSelectedSchemeItem);
//							newItem.setIdPrev(0);
//							newItem.setIndChild(0);
//							newItem.setLevelNumber(parItem.getLevelNumber()+1);
//							newItem.setPositionId(position.getId());
//							newItem.setVersionId(versionId);
//							
//							newItem = sdao.save(newItem);
//							position.setSchemeItem(newItem);
//							
//							parItem.setIndChild(1);
//							
//							parItem = sdao.save(parItem);
//							
//						}else {							
//							throw new DbErrorException("Повреденa схема ма версия с id = " + versionId + " Липсва значение с id = " + idSelectedSchemeItem);
//						}
//					}else {
//
//						/** CASE 6 - Запис на дете - слагаме го на първо място */
//						LOGGER.debug("savePosition - CASE 6");
//						
//						SchemeItem newItem = new SchemeItem();
//						newItem.setCode(position.getCode());
//						newItem.setDeffName(name);
//						newItem.setIdParent(firstItem.getIdParent());
//						newItem.setIdPrev(firstItem.getIdPrev());
//						newItem.setIndChild(0);
//						newItem.setLevelNumber(firstItem.getLevelNumber());
//						newItem.setPositionId(position.getId());
//						newItem.setVersionId(firstItem.getVersionId());
//						
//						newItem = sdao.save(newItem);
//						position.setSchemeItem(newItem);
//						
//						firstItem.setIdPrev(newItem.getId());
//						
//						firstItem = sdao.save(firstItem);
//					}
//					
//				}
//			}
//			
//			
//		}catch (DbErrorException e) {
//			throw e;
//		}catch (Exception e) {
//			
//		}
//		
//		
//		
//		
//		return position;
//	}
//	
//	/** Променя данни за позиция (без изместване в схемата)
//	 * @param position Позиция
//	 * @return променена позиция
//	 * @throws DbErrorException 
//	 */
//	public Position updateSchemePosition(Position position) throws DbErrorException {	
//		
//		
//		
//		if (position == null || position.getSchemeItem() == null || position.getId() == null || position.getSchemeItem().getId() == null) {
//			throw new InvalidParameterException("Невалидни входни параметри за updatePosition");
//		}
//		
//		position = save(position);
//		
//		String name = "Не е въведено";
//		 PositionLang pLang = position.getLangMap().get(NSIConstants.CODE_DEFAULT_LANG);
//		 if (pLang != null && pLang.getOffitialTitile() != null && ! pLang.getOffitialTitile().trim().isEmpty()) {
//			 name = pLang.getOffitialTitile();
//		 }else {
//			 if (position.getLangMap().size() > 0) {
//				 pLang = position.getLangMap().entrySet().iterator().next().getValue();
//				 if (pLang != null && pLang.getOffitialTitile() != null && ! pLang.getOffitialTitile().trim().isEmpty()) {
//					 name = pLang.getOffitialTitile();
//				 }
//				 
//			 }
//		 }
//		 
//		 if (! position.getSchemeItem().getCode().equals(position.getCode()) || ! position.getSchemeItem().getDeffName().equals(name)) {
//			 position.getSchemeItem().setCode(position.getCode());
//			 position.getSchemeItem().setDeffName(name);
//			 position.setSchemeItem(sdao.save(position.getSchemeItem()));
//		 }
//		
//		
//		return position;
//	}
//	
//	
//	/**
//	 * @param idcurrentSchemeItem Елемен от схемата, който ще местим
//	 * @param typeAction - къде (NSIConstants  --> CODE_ZNACHENIE_INSERT_BEFORE, CODE_ZNACHENIE_INSERT_AFTER, CODE_ZNACHENIE_INSERT_AS_CHILD)
//	 * @param idSelectedSchemeItem Избран елемент от схемата
//	 */
//	public void moveSchemePosition(Integer idcurrentSchemeItem, Integer typeAction, Integer idSelectedSchemeItem) {
//		//TODO
//	}
//	
//	
//	
//	/**Зарежа позиция и елемент от схема по ид на елемент от схема
//	 * @param schemeId системен идентификатор на елемент от схема
//	 * @return Позиция със заредена йерархия
//	 * @throws DbErrorException грешка при работа с БД
//	 */
//	public Position loadSchemePosition(Integer schemeId) throws DbErrorException {
//		
//		SchemeItem sItem = sdao.findById(schemeId);
//		if (sItem != null) {
//			if (sItem.getPositionId() != null) {
//				Position p = findById(sItem.getPositionId());
//				if (p != null) {
//					p.setSchemeItem(sItem);
//					return p;
//				}else {
//					throw new DbErrorException("Намерен е елемент на схема с изтрита позиция !!!! schemeId= " + schemeId + ", positionId="+ sItem.getPositionId());
//				}
//			}else {
//				throw new DbErrorException("Намерен е елемент на схема без позиция !!!! schemeId = " + schemeId);
//			}
//		}else {
//			return null;
//		}
//		
//	}
//	
//	
//	/** Изтрива позиция от схема
//	 * @param position позиция с елемет от схема в него
//	 * @throws DbErrorException грешка при работа с БД
//	 * @throws ObjectInUseException позиция или децата и се използват и в други обекти
//	 */
//	@SuppressWarnings("unchecked")
//	public void deleteSchemePosition(Position position) throws DbErrorException, ObjectInUseException {
//		
//		if (position == null) {
//			return;
//		}
//		
//		
//		
//		Query q = JPA.getUtil().getEntityManager().createNativeQuery("select id from SCHEME where POSITION_ID = :IDP");
//		q.setParameter("IDP", position.getId());
//		ArrayList<Object> ids = (ArrayList<Object>) q.getResultList();
//		
//		if (ids.size() == 1) {
//			delete(position);
//		}
//		
//		
//		
//		sdao.delete(position.getSchemeItem());
//		
//		
//		List<SchemeItem> children = sdao.loadScheme(position.getSchemeItem().getVersionId(), position.getSchemeItem().getId(), NSIConstants.CODE_DEFAULT_LANG);
//		for (SchemeItem child : children) {
//			Position p = findById(child.getPositionId());
//			p.setSchemeItem(child);
//			deleteSchemePosition(p);
//		}
//		
//		
//		
//	}
//	
//	
//	
//	@SuppressWarnings("unchecked")
//	public List<Integer> loadPositionAttr(Integer idClassif) throws DbErrorException, ObjectInUseException{
//		
//		if(idClassif==null) {
//			throw new InvalidParameterException("Невалиден идентификатор на класификация!");
//		}
//		
//		Query q = JPA.getUtil().getEntityManager().createNativeQuery("SELECT CODE_ATTRIB FROM CLASSIFICATION_ATTRIBUTES WHERE CLASSIFICATION_ID =:IDC");
//		q.setParameter("IDC", idClassif);
//		
//		return q.getResultList();
//	}
}
