package com.ib.nsiclassif.db.dao;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.hibernate.jpa.QueryHints;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.SchemeItem;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.BaseSystemData;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.db.SelectMetadata;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.exceptions.UnexpectedResultException;
import com.ib.system.utils.SearchUtils;


/**
 * DAO for {@link Position}
 *
 * @author mamun
 */
public class PositionSDAO extends AbstractDAO<PositionS> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(PositionSDAO.class);
	
	

	/** @param user */
	public PositionSDAO(ActiveUser user) {
		super(user);
	}

	
	/** Записва нова позиция преди/след/като дете на елемент от схема
	 * @param position Позиция
	 * @param typeAction - къде (NSIConstants  --> CODE_ZNACHENIE_INSERT_BEFORE, CODE_ZNACHENIE_INSERT_AFTER, CODE_ZNACHENIE_INSERT_AS_CHILD)
	 * @param idSelectedSchemeItem Избран елемент от схемата
	 * @return записана позиция
	 * @throws DbErrorException 
	 */
	public PositionS saveSchemePosition(PositionS position, Integer typeAction, Integer idSelectedPosition, Integer versionId) throws DbErrorException {
		
		if (position == null || position.getId() != null ||  (typeAction == null && idSelectedPosition != null) || (idSelectedPosition == null && typeAction != null)  || versionId == null) {
			LOGGER.error("position="+position);
			LOGGER.error("typeAction="+typeAction);
			LOGGER.error("idSelectedPosition="+idSelectedPosition);
			LOGGER.error("versionId="+versionId);
			if (position != null) {
				LOGGER.error("position.getId="+position.getId());				
			}
			throw new InvalidParameterException("Невалидни входни параметри за savePosition");
		}
		
		try {
			
			
					 
					 
			if (idSelectedPosition == null || idSelectedPosition.equals(0))	{
				
				/** CASE 1 - Първо значение в схемата */
				LOGGER.debug("savePosition - CASE 1");
				
				
				position.setIdParent(0);
				position.setIdPrev(0);
				position.setIndChild(0);
				position.setLevelNumber(1);				
				position.setVersionId(versionId);
				
				position = save(position);
				
				
				
			}else {
			
			
				if (typeAction.equals(NSIConstants.CODE_ZNACHENIE_INSERT_AFTER)) {
					
					PositionS nextItem = findByIdPrev(idSelectedPosition);
					if (nextItem != null) {
						
						/** CASE 2 - Вмъкване след */
						LOGGER.debug("savePosition - CASE 2");
					
						
						
						position.setIdParent(nextItem.getIdParent());
						position.setIdPrev(idSelectedPosition);
						position.setIndChild(0);
						position.setLevelNumber(nextItem.getLevelNumber());						
						position.setVersionId(nextItem.getVersionId());
						
						position = save(position);
						
						nextItem.setIdPrev(position.getId());
						
						nextItem = save(nextItem);
					}else {							
						/** CASE 3 - Вмъкване след на последно значение */
						LOGGER.debug("savePosition - CASE 3");
						
						PositionS selItem = findById(idSelectedPosition);
						
						position.setIdParent(selItem.getIdParent());
						position.setIdPrev(idSelectedPosition);
						position.setIndChild(0);
						position.setLevelNumber(selItem.getLevelNumber());						
						position.setVersionId(selItem.getVersionId());
						
						position = save(position);
						
						
						
					}
				}
				
				if (typeAction.equals(NSIConstants.CODE_ZNACHENIE_INSERT_BEFORE)) {
					
					/** CASE 4 - Вмъкване преди */
					LOGGER.debug("savePosition - CASE 4");
					
					PositionS selItem = findById(idSelectedPosition);
					if (selItem != null) {
						
						position.setIdParent(selItem.getIdParent());
						position.setIdPrev(selItem.getIdPrev());
						position.setIndChild(0);
						position.setLevelNumber(selItem.getLevelNumber());						
						position.setVersionId(selItem.getVersionId());
						
						position = save(position);
						
						
						selItem.setIdPrev(position.getId());
						
						selItem = save(selItem);
					}else {							
						throw new DbErrorException("Повреденa схема ма версия с id = " + versionId + " Липсва значение с id = " + idSelectedPosition);
					}
				}
				
				
				if (typeAction.equals(NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD)) {
					
					PositionS firstItem = findFirstChild(idSelectedPosition);
					if (firstItem == null) {

						/** CASE 5 - Запис на първо дете */
						LOGGER.debug("savePosition - CASE 5");
												
						PositionS parItem = findById(idSelectedPosition);
						if (parItem != null) {						
							
							
							position.setIdParent(idSelectedPosition);
							position.setIdPrev(0);
							position.setIndChild(0);
							position.setLevelNumber(parItem.getLevelNumber()+1);							
							position.setVersionId(versionId);
							
							position = save(position);
							
							
							parItem.setIndChild(1);							
							parItem = save(parItem);
							
						}else {							
							throw new DbErrorException("Повреденa схема ма версия с id = " + versionId + " Липсва значение с id = " + idSelectedPosition);
						}
					}else {

						/** CASE 6 - Запис на дете - слагаме го на първо място */
						LOGGER.debug("savePosition - CASE 6");
						
						
						position.setIdParent(firstItem.getIdParent());
						position.setIdPrev(firstItem.getIdPrev());
						position.setIndChild(0);
						position.setLevelNumber(firstItem.getLevelNumber());						
						position.setVersionId(firstItem.getVersionId());
						
						position = save(position);
						
						
						firstItem.setIdPrev(position.getId());						
						firstItem = save(firstItem);
					}
					
				}
			}
			
			
		}catch (DbErrorException e) {
			throw e;
		}catch (Exception e) {
			
		}
		
		
		
		
		return position;
	}
	
	
	/**
	 * @param idcurrentSchemeItem Елемен от схемата, който ще местим
	 * @param typeAction - къде (NSIConstants  --> CODE_ZNACHENIE_INSERT_BEFORE, CODE_ZNACHENIE_INSERT_AFTER, CODE_ZNACHENIE_INSERT_AS_CHILD)
	 * @param idSelectedSchemeItem Избран елемент от схемата
	 */
	public void moveSchemePosition(Integer idcurrentSchemeItem, Integer typeAction, Integer idSelectedSchemeItem) {
		//TODO
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Integer> loadPositionAttr(Integer idVersion) throws DbErrorException, InvalidParameterException{
		
		if(idVersion==null) {
			throw new InvalidParameterException("Невалиден идентификатор на класификация!");
		}
		
		String sql = "SELECT CA.CODE_ATTRIB FROM CLASSIFICATION_ATTRIBUTES CA JOIN VERSION V ON CA.CLASSIFICATION_ID = V.ID_CLASS  WHERE V.ID =:IDV";
		Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql,"KrasiGResultSet");
		q.setHint(QueryHints.HINT_CACHEABLE, true);
		q.setHint(QueryHints.HINT_READONLY, true);
		q.setHint(QueryHints.HINT_CACHE_REGION, "queryTestKG");
		q.setParameter("IDV", idVersion);
		
		return q.getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	private PositionS findByIdPrev(Integer id) throws DbErrorException {
		
		ArrayList<PositionS> rows = new ArrayList<PositionS>();
		try {
			

			if (id == null || id.equals(0)) {
				return null;
			}
			
			Query q = JPA.getUtil().getEntityManager().createQuery(" from PositionS where idPrev = :IDP");
			q.setParameter("IDP", id);
			
			rows = (ArrayList<PositionS>) q.getResultList();
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на елемент по id на предходен", e);
			throw new DbErrorException("Грешка при извличане на елемент по id на предходен", e);
		}
		
		
		if (rows.size() == 0) {
			return null;
		}
		
		if (rows.size() > 1) {
			throw new DbErrorException("Счупена схема на версия с id="+rows.get(0).getVersionId() + ". "+rows.size()+" значения се намират след елемент с id="+id);
		}
		
		return rows.get(0);
		
	}
	
	
	@SuppressWarnings("unchecked")
	public PositionS findFirstChild(Integer id) throws DbErrorException {
		
		ArrayList<PositionS> rows = new ArrayList<PositionS>();
		try {
			

			if (id == null || id.equals(0)) {
				return null;
			}
			
			Query q = JPA.getUtil().getEntityManager().createQuery(" from PositionS where idParent = :IDP and idPrev = :IDPTV");
			q.setParameter("IDP", id);
			q.setParameter("IDPTV", 0);
			
			rows = (ArrayList<PositionS>) q.getResultList();
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на елемент по id на родител", e);
			throw new DbErrorException("Грешка при извличане на елемент по id на родител", e);
		}
		
		
		if (rows.size() == 0) {
			return null;
		}
		
		if (rows.size() > 1) {
			throw new DbErrorException("Счупена схема на версия с id="+rows.get(0).getVersionId() + ". "+rows.size()+" значения се намират след елемент с id="+id);
		}
		
		return rows.get(0);
		
	}
	
	
	
	/**
	 * Сортира ги по код на предходен
	 *
	 * @param scheme
	 * @throws UnexpectedResultException
	 */
	public void doSortSchemePrev(List<PositionS> scheme) throws UnexpectedResultException {
		if (scheme == null || scheme.isEmpty()) {
			return;
		}

		List<PositionS> sorted = new ArrayList<PositionS>();
		
		recLoadNivo(scheme, 0, 0, sorted);
		

		if (!scheme.isEmpty()) {
			StringBuilder codes = new StringBuilder();

			for (Iterator<PositionS> iterator = scheme.iterator(); iterator.hasNext();) {
				PositionS sci = iterator.next();

				codes.append(",");
				codes.append(sci.getId());
			}
			throw new UnexpectedResultException("Повреденa схема ма версия с id = " + scheme.get(0).getVersionId() + ". Останали значения:" + scheme.size() + " codes:[" + codes.toString() + "]");
		}

		scheme.clear();
		scheme.addAll(sorted);
	}
	
	private static void recLoadNivo(List<PositionS> scheme, int prev, int parent, List<PositionS> sorted) {
		PositionS poreden = null;

		for (int i = 0; i < scheme.size(); i++) {
			PositionS tek = scheme.get(i);
			//System.out.println(tek.getId() + " " + tek.getName());
			if (tek.getIdParent() == parent && tek.getIdPrev() == prev) {
				poreden = tek;
				break;
			}
		}
		if (poreden != null) {
			sorted.add(poreden);			
			scheme.remove(poreden);
			
			
			recLoadNivo(scheme, 0, poreden.getId(), sorted);
			recLoadNivo(scheme, poreden.getId(), parent, sorted);
		}
	}
	
	
	
	
	/**
	 * Метод за изграждане на дърво на схема на версия	
	 *
	 * @param arrTree       - списък от елементите на дървото от тип PositionS
	 * @param title         - стринг (не се използва)
	 * @param sortByName    -true/false дали елементите в дървото да са подредени по азбучен ред или по реда на въвеждане
	 * @param expanded      - true/false дали дървото да е отворено или затворено при първоначално показване
	 * @param readOnlyCodes - списък на кодове на елементи ,които да са само за разглеждане в дървото (не позволява да се избират)
	 * @param readOnlyType  - стринг , ако е подаден този параметър трябва да се дефинира treeNode от такъв тип
	 * @return
	 */
	public TreeNode loadTreeData3(List<PositionS> arrTree, String title, boolean sortByName, boolean expanded, List<Integer> readOnlyCodes, String readOnlyType) {
		return loadTreeData(arrTree, sortByName, DefaultTreeNode.class, null, expanded, readOnlyCodes, readOnlyType);
	}
	
	/**
	 * @param arrTree       - списък от елементите на дървото от тип PositionS
	 * @param title         - стринг (не се използва)
	 * @param sortByName    -true/false дали елементите в дървото да са подредени по азбучен ред или по реда на въвеждане
	 * @param arg           - име на клас . Да знаем дали дървото да се изгради като стандартно или с чекбоксове
	 * @param expanded      - true/false дали дървото да е отворено или затворено при първоначално показване
	 * @param readOnlyCodes - списък на кодове на елементи ,които да са само за разглеждане в дървото (не позволява да се избират)
	 * @param readOnlyType  - стринг , ако е подаден този параметър трябва да се дефинира treeNode от такъв тип
	 * @return
	 */
	private TreeNode loadTreeData(List<PositionS> arrTree, boolean sortByName, Class<?> arg, List<Integer> selected, boolean expanded, List<Integer> readOnlyCodes, String readOnlyType) {
		try {

			if (selected == null) {
				selected = new ArrayList<>();
			}

			if (readOnlyCodes == null) {
				readOnlyCodes = new ArrayList<>();
			}

			TreeNode tData = (TreeNode) arg.newInstance();

			if (sortByName) {
				Collections.sort(arrTree, new SortByLevelTekst());
			} else {
				doSortSchemePrev(arrTree);
				// Collections.sort(arrTree, new SortByLevel_CodePrev());
			}
			for (int i = 0; i < arrTree.size(); i++) {
				PositionS classItem = arrTree.get(i);

				TreeNode parent = findNode(tData, classItem.getIdParent());
				// System.out.println();

				if (parent == null) {

					parent = tData;

				}

				parent.setExpanded(expanded);

				if (parent instanceof CheckboxTreeNode) {

					TreeNode tmpData = new CheckboxTreeNode(classItem, parent);

					if (tmpData.getData() != null) {
						int code = ((SchemeItem) tmpData.getData()).getId();

						if (selected.contains(Integer.valueOf(code))) {
							tmpData.setSelected(true);
							// tmpData.setExpanded(true);
						} else {
							tmpData.setSelected(false);
						}

						if (readOnlyCodes.contains(Integer.valueOf(code))) {
							tmpData.setSelectable(false);
							if (readOnlyType != null && !readOnlyType.isEmpty()) {
								tmpData.setType(readOnlyType);
							}
						}

					}

				} else {
					TreeNode tmpData = new DefaultTreeNode(classItem, parent);
					if (readOnlyCodes.contains(((SchemeItem) tmpData.getData()).getId())) {
						tmpData.setSelectable(false);
						if (readOnlyType != null && !readOnlyType.isEmpty()) {
							tmpData.setType(readOnlyType);
						}
					}
				}
				// DefaultTreeNode tmpData = new DefaultTreeNode(classItem,parent);

				if (!sortByName) {
					int tmpCode = classItem.getId();
					for (int j = i + 1; j < arrTree.size(); j++) {
						PositionS classItem2 = arrTree.get(j);
						// Ako e stignat kraja na nivoto - restartirame
						if (classItem2.getLevelNumber() != classItem.getLevelNumber()) {
							break;
						}
						// Ako e nameren sledvashtija - go dobavjame
						if (classItem2.getIdPrev() == tmpCode) {

							if (parent instanceof CheckboxTreeNode) {

								TreeNode tmpData = new CheckboxTreeNode(classItem2, parent);

								if (tmpData.getData() != null) {
									int code = ((SchemeItem) tmpData.getData()).getId();

									if (selected.contains(Integer.valueOf(code))) {
										tmpData.setSelected(true);
										// tmpData.setExpanded(true);
									} else {
										tmpData.setSelected(false);
									}

									if (readOnlyCodes.contains(Integer.valueOf(code))) {
										tmpData.setSelectable(false);
										if (readOnlyType != null && !readOnlyType.isEmpty()) {
											tmpData.setType(readOnlyType);
										}
									}
								}

							} else {
								TreeNode tmpData = new DefaultTreeNode(classItem2, parent);

								if (readOnlyCodes.contains(((SchemeItem) tmpData.getData()).getId())) {
									tmpData.setSelectable(false);
									if (readOnlyType != null && !readOnlyType.isEmpty()) {
										tmpData.setType(readOnlyType);
									}
								}
							}
							tmpCode = classItem2.getId();
							arrTree.remove(j);
							j = i + 1;

						}
					}
				}

			}

			return tData;

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}

		return null;
	}
	
	/**
	 * Този компаратор се използва за да сортираме системна класификация по ниво и код 
	 *
	 * @author krasi
	 */
	private class SortByLevelTekst implements Comparator<PositionS>, Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 3621707321119040557L;

		/** @see Comparator#compare(Object, Object) */
		@Override
		public final int compare(PositionS a, PositionS b) {
			if (a.getLevelNumber() == b.getLevelNumber()) {
				return a.getCode().compareTo(b.getCode());
			}
			return a.getLevelNumber() - b.getLevelNumber();
		}
	}
	
	/**
	 * Рекурсивен метод за търсене на на възел в дърво по заданен код Малко е смотано че се извършват доста итерации. Добре би
	 * било да се пооптимизира някак
	 *
	 * @param tData
	 * @param key
	 * @return TreeNode ако го намери NULL ако го няма
	 */
	private TreeNode findNode(TreeNode tData, int key) {

		if (tData.getData() != null && ((PositionS) tData.getData()).getId() == key) {
			return tData;
		}

		if (!tData.isLeaf()) {

			for (TreeNode aa : tData.getChildren()) {

				if (((SchemeItem) aa.getData()).getId() == key) {
					return aa;
				} else if (!aa.isLeaf()) {
					TreeNode tmpTreeNode = findNode(aa, key);
					if (tmpTreeNode != null) {
						return tmpTreeNode;
					}
				}
			}

		}
		return null;
	}
	
	
	
	public List<Object[]> decodePositionByVersionCode(Integer idVersion, String code, Integer lang) throws DbErrorException {
		
		
		try {
			 
			
			StringBuffer SQL = new StringBuffer();
				SQL.append("SELECT ");
				SQL.append(" s.ID, ");
				SQL.append("    ISNULL(pl.OFFICIAL_TITLE, pdeff.OFFICIAL_TITLE + ' *') DEFF_NAME ");
				SQL.append(" FROM ");
				SQL.append("    POSITION_SCHEME s ");
				SQL.append(" LEFT OUTER JOIN POSITION_LANG pl ON pl.POSITION_ID = s.ID AND pl.lang = :LANG ");
				SQL.append(" LEFT OUTER JOIN POSITION_LANG pdeff ON pdeff.POSITION_ID = s.ID AND pdeff.lang = :DEFLANG ");
				SQL.append(" WHERE ");
				SQL.append("    s.VERSION_ID = :versionId ");
				SQL.append(" AND s.CODE=:sourceCode");
			
			 
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
				q.setParameter("versionId", idVersion);
				q.setParameter("LANG", lang);
				q.setParameter("sourceCode", code);
				q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
			
			 
			
		 
			
			return q.getResultList();
			
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на име на позиция от vestionId = " + idVersion, e);
			throw new DbErrorException("Грешка при извличане на име на позиция от vestionId = " + idVersion, e);
		}
	}
	
	
	/** Изгражда схема на версия или  ниво от версия на зададен език
	 * @param idVersion - системен идентификатор на версия
	 * @param idIParent - системен идентификатор на родител
	 * @param lang - език 
	 * @return списък от значения, подреден по предходен в нивото
	 * @throws DbErrorException - грешка при работа с БД
	 */
	@SuppressWarnings("unchecked")
	public List<PositionS> loadScheme(Integer idVersion, Integer idIParent, Integer lang ,Integer fromRow ,int maxResult) throws DbErrorException{
		
		try {
			if (idVersion == null) {
				
			}
			
			String sql = "select POSITION_SCHEME.ID as ID, POSITION_SCHEME.CODE as CODE, isnull(pl.OFFICIAL_TITLE, pdeff.OFFICIAL_TITLE + ' *') as DEFF_NAME, VERSION_ID as VERSION_ID, ID_PREV as ID_PREV, ID_PARENT as ID_PARENT, LEVEL_NUMBER as LEVEL_NUMBER, IND_CHILD as IND_CHILD "
					+ " from POSITION_SCHEME "
					+ "    left outer join POSITION_LANG pl on  pl.POSITION_ID =  POSITION_SCHEME.ID and pl.lang = :LANG "
					+ "    left outer join POSITION_LANG pdeff on  pdeff.POSITION_ID =  POSITION_SCHEME.ID and pdeff.lang = :DEFLANG "
					+ " where VERSION_ID = :VID" ;
			
			if (idIParent != null) {
				sql += " and id_parent = :IDP ";
			}
			sql += " ORDER BY CODE ";
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql, "loadSchemeNew");
			q.setParameter("VID", idVersion);
			q.setParameter("LANG", lang);
			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
			
			if (idIParent != null) {
				q.setParameter("IDP", idIParent);
			}
			
			if(fromRow!=null && fromRow.intValue()>=0) {
				q.setFirstResult(fromRow);
				q.setMaxResults(maxResult);
			}
			
			List<PositionS> scheme =  q.getResultList();
			
			if (idIParent != null) {
				sortSchemeLevelByPrev(scheme);
			}else {
				doSortSchemePrev(scheme);
			}
			
			return scheme;
			
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на схема с vestionId = " + idVersion, e);
			throw new DbErrorException("Грешка при извличане на схема с vestionId = " + idVersion, e);
		}
	}
	
	
	public Integer loadSchemeCount(Integer idVersion, Integer idIParent) throws DbErrorException{
		
		try {
			String sql = "select count(ID) from POSITION_SCHEME where VERSION_ID = :VID ";
			
			if (idIParent != null) {
				sql += " and id_parent = :IDP ";
			}
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter("VID", idVersion);
			
			if (idIParent != null) {
				q.setParameter("IDP", idIParent);
			}
			
			Object count =  q.getSingleResult();
			
			
			
			return SearchUtils.asInteger(count);
		
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на броят позиции в схема с vestionId = " + idVersion, e);
			throw new DbErrorException("Грешка при извличане на броят позиции в схема с vestionId = " + idVersion, e);
		}
	}
	
	
	private void sortSchemeLevelByPrev(List<PositionS> items) throws UnexpectedResultException {
		
		List<PositionS> sorted = new ArrayList<PositionS>();
		boolean hasChange = true;
		Integer tekPrev = 0;		
		while (items.size() > 0 && hasChange) {
			hasChange = false;
			PositionS found = null;
			for (PositionS tek : items) {
				if (tek.getIdPrev().equals(tekPrev)) {
					sorted.add(tek);
					hasChange = true;
					found = tek;
					tekPrev = tek.getId();
					break;
				}
			}
			if (found != null) {
				items.remove(found);
			}
		}
		if (items.size() > 0) {
			throw new UnexpectedResultException("Повреденa схема ма версия с id = " + items.get(0).getVersionId() + ". Останали значения:" + items.size() + " codes:[" + items.toString() + "]");
		}else {
			items.clear();
			items.addAll(sorted);
		}
		
	}
	
	
	
	/**Проверка на код на позиция по зададена маска.
     * @param code - код на позиция
     * @param mask - маска
     * @return true ако кода съответства на маската или false при несъответствие
     */
    public boolean checkCode(String code, String mask) {
        
        if(code==null) return false;
        if(mask==null) return false;
        if(mask.length()<1) return false;
        if(mask.indexOf("*") != -1)   return true;
        if(code.length()!=mask.length()) return false;
        for(int i=0;i<mask.length();i++) {
            switch(mask.toCharArray()[i]) {
                case 'X': break;
                case 'x': break;
                case '9': if(!Character.isDigit(code.toCharArray()[i])) return false; break ;
                case 'C': if(!Character.isLetterOrDigit(code.toCharArray()[i])) return false; break ;
                case 'L': if(!Character.isUpperCase(code.toCharArray()[i])) return false; break ;            
                case 'l': if(!Character.isLowerCase(code.toCharArray()[i])) return false; break ;                  
                case 'K': if((code.toCharArray()[i]>='А')&&(code.toCharArray()[i]<='Я') ) break; else return false;
                case 'k': if((code.toCharArray()[i]>='а')&&(code.toCharArray()[i]<='я') ) break; else return false;            
                default: if(mask.toCharArray()[i]!=code.toCharArray()[i]) return false;
            }                                  
        }
        return true;
    }
    
    
    
    /**Проверява по зададен код и ид на версията дали има вече въведен такъв код ,и дали кода е променен при update
     * @param verId системен идентификатор на версия
     * @param codePosition код на позиция
     * @param idPosition ид на позиция
     * @return true/false 
     * @throws exception.InvalidParameterException грешни входни параметри
     * @throws exception.DbErrorException грешка при работа с БД
     */
    public boolean checkCodeByIdPosition (Integer verId, String codePosition ,Integer idPosition) throws InvalidParameterException, DbErrorException{
    	
       // Правим проверка за коректност на входните параметри !
       if (verId == null || codePosition == null)
           throw new InvalidParameterException("Подаден е параметър с null стойност");

       if (codePosition==null || codePosition == "")
           throw new InvalidParameterException("Подаден е параметър с некоректна стойност (id = " + verId + " , codePosition = " + codePosition);
       

       try {
           String textQuery = "Select ID From POSITION_SCHEME  Where VERSION_ID=:id and CODE = :code";
           Query q = JPA.getUtil().getEntityManager().createNativeQuery(textQuery);
           q.setParameter("id", verId);
           q.setParameter("code", codePosition);
           
           @SuppressWarnings("unchecked")
		   List<Object> rezList = q.getResultList();
           
           if ((rezList.size() == 0)) 
               return false;
               
           if(idPosition != null){    
               Integer itemId = SearchUtils.asInteger(rezList.get(0));
               if(idPosition.intValue() == itemId.intValue())
                   return false;
               else
                   return true;
           }else{
               return true;
           }
       } catch (Exception e) {
           throw new DbErrorException("Грешка при извличане на обект от БД",e);
       }
       
   }
    
    
    @SuppressWarnings("unchecked")
	public List<Object[]> loadParentsPosition(Integer idPosition, Integer lang) throws DbErrorException ,InvalidParameterException {
		
		if(idPosition==null) {
			throw new InvalidParameterException("Невалиден идентификатор на позиция!");
		}
		
		try {
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(""
					+ "WITH get_parents_pos "
					+ "AS ( "
					+ "      SELECT PS.ID, PS.ID_PARENT, PS.CODE, PL.OFFICIAL_TITLE  "
					+ "      FROM POSITION_SCHEME PS JOIN POSITION_LANG PL ON PS.ID = PL.POSITION_ID AND PL.LANG=:L "
					+ "      WHERE  PS.ID =:IDP "
					+ "UNION ALL "
					+ "      SELECT PS.ID, PS.ID_PARENT, PS.CODE, PL.OFFICIAL_TITLE "
					+ "      FROM POSITION_SCHEME PS JOIN POSITION_LANG PL ON PS.ID = PL.POSITION_ID AND PL.LANG=:L "
					+ "      INNER JOIN  get_parents_pos GPS ON PS.ID = GPS.ID_PARENT  "
					+ ") "
					+ "SELECT  * FROM   get_parents_pos order by ID_PARENT");
			
			q.setParameter("IDP", idPosition);
			q.setParameter("L", lang);
			
			return q.getResultList();
		} catch (Exception e) {
	           throw new DbErrorException("Грешка при извличане на обект от БД",e);
	    }
	       
	}
	  
    /**
     * 
     * @param idVersion
     * posAttr - по това се определя кои колони ни трябват реално.
     * useAttributes - това казва дали искаме да ограничаваме атрибутите - когато правим обикновенни файлове true/ когато правим SDMX или нещо друго което изисква PositionS после - false 
     * 
     * @return list with only what is selected as attribute from the classification
     * 	p.id,
     *  p.ID_PREV,
	    p.ID_PARENT,	    
	    ---- gornite za sortiraneto
	    ---- nadolu shte se vadqt v zavisimost ot atributa - vsi4ko tova e nujno za da moje da polzvame edna i sashti DownloadFile class
	    p.code,
	    p.code_full,
	    p.CODE_SEPARATE,
	    p.CODE_TYPE,
	    p.STATUS,
	    p.LEVEL_NUMBER,
	    
     * @throws DbErrorException 
     * 
     */
    public List<Object[]> loadPositionsForExport(Integer idVersion, Map <Integer,Boolean> posAttr, boolean useAttributes) throws DbErrorException{
    	try{
    		StringBuffer SQL = new StringBuffer();
	    	SQL.append("SELECT ");
	    	SQL.append("    p.id, ");
	    	SQL.append("    p.ID_PREV, ");
	    	SQL.append("    p.ID_PARENT, ");
	    	SQL.append("    p.code ");
	    	if (useAttributes) {
	    		if (posAttr.containsKey(2)) {
		    		SQL.append("   , p.code_full ");	
				}
		    	if (posAttr.containsKey(3)) {
		    		SQL.append("   , p.CODE_SEPARATE ");	
		    	}
		    	if (posAttr.containsKey(4)) {
		    		SQL.append("   , p.CODE_TYPE ");	
		    	}
		    	if (posAttr.containsKey(5)) {
		    		SQL.append("   , p.STATUS ");	
		    	}
		    	if (posAttr.containsKey(6)) {
		    		SQL.append("   , p.LEVEL_NUMBER ");	
		    	}
			}else {
		    		SQL.append("   , p.code_full ");	
		    		SQL.append("   , p.CODE_SEPARATE ");	
		    		SQL.append("   , p.CODE_TYPE ");	
		    		SQL.append("   , p.STATUS ");	
		    		SQL.append("   , p.LEVEL_NUMBER ");	
			}
	    	
	    	
	    	
	    	SQL.append(" FROM ");
	    	SQL.append("    POSITION_SCHEME p ");
	    	SQL.append(" WHERE ");
	    	SQL.append("    p.VERSION_ID=:idVersion");
	    	SQL.append(" order by p.ID asc");
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
			q.setParameter("idVersion", idVersion);
			
			return q.getResultList(); 
	    } catch (Exception e) {
			throw new DbErrorException(e);
		}
	}
    
    /**
     * 
     * @param idVersion
     * @return map with default lang (BG) + selected one
     * map: HashMap<LANG, HashMap<POSITION_ID, Object[]>>
     * where Object[] is:
     *  l.ID,
     * 	l.POSITION_ID,
	    l.LANG,
	    l.OFFICIAL_TITLE,
	    l.LONG_TITLE,
	    l.SHORT_TITLE,
	    l.ALTERNATE_TITLES,
	    l.COMMENT,
	    l.INCLUDES,
	    l.ALSO_INCLUDES,
	    l.EXCLUDES,
	    l.RULES,
	    l.PREPRATKA,
	    l.STAT_POKAZATEL
     * @throws DbErrorException 
     */
    public List<Object[]>  loadPositionsLangsForExport(Integer idVersion, Integer lang,  Map <Integer,Boolean> posAttr, boolean useAttributes) throws DbErrorException{
    	try {
    		
    		boolean langSecond=false;
    		if (lang==null || lang!=NSIConstants.CODE_LANG_BG) {
    			langSecond=true;
			}
			StringBuffer SQL = new StringBuffer();
			SQL.append("SELECT ");
			SQL.append("    l.ID, ");
			SQL.append("    l.POSITION_ID, ");
			SQL.append("    l.LANG ");
			if (useAttributes) {
				if (posAttr.containsKey(11)) {
					SQL.append("   , ISNULL(l.OFFICIAL_TITLE, '') OFFICIAL_TITLE");	
		    	}
				if (posAttr.containsKey(12)) {
					SQL.append("   , ISNULL(l.SHORT_TITLE, '') SHORT_TITLE");	
				}
				if (posAttr.containsKey(13)) {
					SQL.append("   , ISNULL(l.LONG_TITLE, '') LONG_TITLE");	
				}
				if (posAttr.containsKey(14)) {
					SQL.append("   , ISNULL(l.ALTERNATE_TITLES, '') ALTERNATE_TITLES");	
				}
				if (posAttr.containsKey(15)) {
					SQL.append("   , ISNULL(l.COMMENT, '') COMMENT");	
				}
				if (posAttr.containsKey(16)) {
					SQL.append("   , ISNULL(l.INCLUDES, '') INCLUDES");	
				}
				if (posAttr.containsKey(17)) {
					SQL.append("   , ISNULL(l.ALSO_INCLUDES, '') ALSO_INCLUDES");	
				}
				if (posAttr.containsKey(18)) {
					SQL.append("   , ISNULL(l.EXCLUDES, '') EXCLUDES");	
				}
				if (posAttr.containsKey(19)) {
					SQL.append("   , ISNULL(l.RULES, '') RULES");	
				}
				if (posAttr.containsKey(20)) {
					SQL.append("   , ISNULL(l.PREPRATKA, '') PREPRATKA");	
				}
				if (posAttr.containsKey(21)) {
					SQL.append("   , ISNULL(l.STAT_POKAZATEL, '') STAT_POKAZATEL");	
				}
			}else {
				SQL.append("   , ISNULL(l.OFFICIAL_TITLE, '') OFFICIAL_TITLE");	
				SQL.append("   , ISNULL(l.SHORT_TITLE, '') SHORT_TITLE");	
				SQL.append("   , ISNULL(l.LONG_TITLE, '') LONG_TITLE");	
				SQL.append("   , ISNULL(l.ALTERNATE_TITLES, '') ALTERNATE_TITLES");	
				SQL.append("   , ISNULL(l.COMMENT, '') COMMENT");	
				SQL.append("   , ISNULL(l.INCLUDES, '') INCLUDES");	
				SQL.append("   , ISNULL(l.ALSO_INCLUDES, '') ALSO_INCLUDES");	
				SQL.append("   , ISNULL(l.EXCLUDES, '') EXCLUDES");	
				SQL.append("   , ISNULL(l.RULES, '') RULES");	
				SQL.append("   , ISNULL(l.PREPRATKA, '') PREPRATKA");	
				SQL.append("   , ISNULL(l.STAT_POKAZATEL, '') STAT_POKAZATEL");	
			}
			
			 
			SQL.append(" FROM ");
			SQL.append("    POSITION_LANG l, ");
			SQL.append("    POSITION_SCHEME p ");
			SQL.append(" WHERE ");
			SQL.append("    l.POSITION_ID=p.id ");
			if (langSecond) {
				SQL.append(" AND l.LANG IN (:langBG,:secondLang) ");	
			}else {
				SQL.append(" AND l.LANG = :langBG ");
			}
			
			SQL.append(" AND p.VERSION_ID=:idVersion");
			SQL.append(" order by l.POSITION_ID asc,l.LANG desc");
			
			 
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
			q.setParameter("idVersion", idVersion);
			q.setParameter("langBG", NSIConstants.CODE_LANG_BG);
			if (langSecond) {
				q.setParameter("secondLang", lang);
			}
			
			return q.getResultList();
    	
	    	
	    	
	    	
	    	 
    	} catch (Exception e) {
			throw new DbErrorException(e);
		}
    }
    
    /**
     * 
     * @param idVersion
     * @return map: Map with (POSITION_ID, (TYPE_UNIT, String)) 
     * 
     * where String is decoded all:      
	    
	    u.unit
	    	     
     * @throws DbErrorException
     */
    public List<Object[]> loadPositionsUnitsForExport(Integer idVersion) throws DbErrorException{
    		StringBuffer SQL = new StringBuffer();
    		SQL.append("SELECT ");
    		SQL.append("    u.id, ");
    		SQL.append("    u.POSITION_ID, ");
    		SQL.append("    u.unit, ");
    		SQL.append("    u.TYPE_UNIT ");
    		SQL.append(" FROM ");
    		SQL.append("    POSITION_UNITS u, ");
    		SQL.append("    POSITION_SCHEME p ");
    		SQL.append(" WHERE ");
    		SQL.append("    p.id=u.POSITION_ID ");
    		SQL.append(" AND p.VERSION_ID=:idVersion");
    		
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
			q.setParameter("idVersion", idVersion);
			
			
			return q.getResultList();
			
	}
    
    
    public List<Object[]> loadPositionsUnitsForExport1(Integer idVersion) throws DbErrorException{
		StringBuffer SQL = new StringBuffer();
		SQL.append(" select u.position_id, min(t.units) as units, u.type_unit ");
		SQL.append(" from position_units u ");
		SQL.append(" left join position_scheme p on u.position_id = p.id ");
		SQL.append(" left join (  select 7 as UT,t1.*  from  ");
		SQL.append(" (select position_id, string_agg(unit,',') within group (order by unit) as units ");
		SQL.append(" from position_units  ");
		SQL.append(" group by position_id,type_unit having type_unit=7 ) as t1 ");
		SQL.append(" union ALL ");
		SQL.append(" select 8 as UT,t2.*  from (  select position_id, string_agg(unit,',') within group (order by unit) as units ");
		SQL.append(" from position_units  ");
		SQL.append(" group by position_id,type_unit having type_unit=8 ) as t2 ) as t on  t.position_id=u.position_id and t.UT=u.type_unit ");
		SQL.append(" where p.version_id =:idVersion  ");
		SQL.append(" group by u.position_id, u.TYPE_UNIT  ");
		SQL.append(" order by  u.TYPE_UNIT  ");
		
		Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
		q.setParameter("idVersion", idVersion);
		
		
		return q.getResultList();
		
}
    
    /**
     * 
     * @param units 
     * @return
     * @throws DbErrorException 
     */
    public HashMap<Integer, HashMap<Integer, String>> decodeUnitsAsMap(List<Object[]> units, BaseSystemData sd, Integer lang) throws DbErrorException{
    	try {
	    	HashMap<Integer, HashMap<Integer, String>> rez=new HashMap<Integer, HashMap<Integer,String>>();
			HashMap<Integer, String> unitMap=new HashMap<Integer, String>();
			
			Integer posId=null;
			String s7="";
			String s8="";
			if (units.size()>0) {
				if (SearchUtils.asInteger(units.get(0)[3])==7) {
					s7=sd.decodeItem(NSIConstants.CODE_CLASSIF_UNITS, SearchUtils.asInteger(units.get(0)[2]), lang, null);
				}
				if (SearchUtils.asInteger(units.get(0)[3])==8) {
					s8=sd.decodeItem(NSIConstants.CODE_CLASSIF_UNITS, SearchUtils.asInteger(units.get(0)[2]), lang, null);
				}
				posId=SearchUtils.asInteger(units.get(0)[1]);
			}
			for (int i = 1; i < units.size(); i++) {
				Integer tempInteger=SearchUtils.asInteger(units.get(i)[1]);
				if (!posId.equals(tempInteger)) {
					unitMap.put(7, s7);
					unitMap.put(8, s8);				
					rez.put(posId, unitMap);	
					
					posId=tempInteger;
					unitMap=new HashMap<Integer, String>();
					s7="";
					s8="";
				}
				if (SearchUtils.asInteger(units.get(i)[3])==7) {
					if (s7.length()>0) {
						s7+="; ";
					}
					s7+=sd.decodeItem(NSIConstants.CODE_CLASSIF_UNITS, SearchUtils.asInteger(units.get(i)[2]), lang, null);
				}
				if (SearchUtils.asInteger(units.get(i)[3])==8) {
					if (s8.length()>0) {
						s8+=";";
					}
					s8+=sd.decodeItem(NSIConstants.CODE_CLASSIF_UNITS, SearchUtils.asInteger(units.get(i)[2]), lang, null);
				}
				
			}
			if (posId!=null) {
				unitMap.put(7, s7);
				unitMap.put(8, s8);				
				rez.put(posId, unitMap);	
				
				unitMap=new HashMap<Integer, String>();
				s7="";
				s8="";
			}
			return rez;
			
	    } catch (Exception e) {
			throw new DbErrorException(e);
		}
		
    }
    
    
    /**
     * 
     * @param units 
     * @return
     * @throws DbErrorException 
     */
    public HashMap<Integer, HashMap<Integer, String>> decodeUnitsAsMap1(List<Object[]> units, BaseSystemData sd, Integer lang) throws DbErrorException{
    	try {
	    	HashMap<Integer, HashMap<Integer, String>> rez=new HashMap<Integer, HashMap<Integer,String>>();
			HashMap<Integer, String> unitMap=new HashMap<Integer, String>();
			
			Integer posId=null;
			String s7="";
			String s8="";
			if (units.size()>0) {
				for (int j = 0; j < units.size(); j++) {
					posId=SearchUtils.asInteger(units.get(j)[0]);
					unitMap=new HashMap<Integer, String>();
					s7="";
					s8="";
					if(rez.get(posId)!=null) {
						s7=rez.get(posId).get(7);
						s8=rez.get(posId).get(8);
					}
					
				if (SearchUtils.asInteger(units.get(j)[2])==7) {
					String[] localUnits=units.get(j)[1].toString().split(",");
					StringBuilder sb= new StringBuilder();
					for(int i=0;i<localUnits.length;i++) {
						sb.append(sd.decodeItem(NSIConstants.CODE_CLASSIF_UNITS,Integer.valueOf(localUnits[i]), lang, null)).append("+");
					}
					sb.deleteCharAt(sb.length() - 1);
					s7=sb.toString();
				}
				if (SearchUtils.asInteger(units.get(j)[2])==8) {
					String[] internationalUnits=units.get(j)[1].toString().split(",");
					StringBuilder sb= new StringBuilder();
					for(int i=0;i<internationalUnits.length;i++) {
						sb.append(sd.decodeItem(NSIConstants.CODE_CLASSIF_UNITS, Integer.valueOf(internationalUnits[i]), lang, null)).append("+");
					}
					sb.deleteCharAt(sb.length() - 1);
					s8=sb.toString();
				}
				unitMap.put(7, s7);
				unitMap.put(8, s8);				
				rez.put(posId, unitMap);
				//LOGGER.info("unitmap-->"+posId+"-- "+s7+"-- "+s8);

				unitMap=new HashMap<Integer, String>();
				s7="";
				s8="";
			}
			}

			return rez;
			
	    } catch (Exception e) {
			throw new DbErrorException(e);
		}
		
    }
    
    
    /**
	 * Сортира ги по код на предходен списък List<Object[]> scheme 
	 * scheme[0] = ID
	 * scheme[1] = ID_PREV
	 * scheme[2] = ID_PARENT
	 *
	 * @param scheme
	 * @throws UnexpectedResultException
	 */
	public void doSortSchemePrevAsObj(List<Object[]> scheme, Integer versionId) throws UnexpectedResultException {
		if (scheme == null || scheme.isEmpty()) {
			return;
		}

		List<Object[]> sorted = new ArrayList<Object[]>();
		
		recLoadNivoAsObjNew(scheme, 0, 0, sorted);
		

		if (!scheme.isEmpty()) {
			StringBuilder codes = new StringBuilder();

			for (Iterator<Object[]> iterator = scheme.iterator(); iterator.hasNext();) {
				Object[] sci = iterator.next();

				codes.append(",");
				codes.append(SearchUtils.asInteger(sci[0]));
			}
			throw new UnexpectedResultException("Повреденa схема ма версия с id = " + versionId + ". Останали значения:" + scheme.size() + " codes:[" + codes.toString() + "]");
		}

		scheme.clear();
		scheme.addAll(sorted);
	}
	
	private static void recLoadNivoAsObj(List<Object[]> scheme, int prev, int parent, List<Object[]> sorted) {
		Object[] poreden = null;

		for (int i = 0; i < scheme.size(); i++) {
			Object[] tek = scheme.get(i);
			if (SearchUtils.asInteger(tek[2]) == parent && SearchUtils.asInteger(tek[1]) == prev) {
				poreden = tek;
				break;
			}
		}
		if (poreden != null) {
			sorted.add(poreden);
			//System.out.println("1111111111111111111111111111111\t" + scheme.size());
			scheme.remove(poreden);
			//System.out.println(poreden[0]);
			//System.out.println("2222222222222222222222222222222\t" + scheme.size());
			
			recLoadNivoAsObj(scheme, 0, SearchUtils.asInteger(poreden[0]), sorted);
			recLoadNivoAsObj(scheme, SearchUtils.asInteger(poreden[0]), parent, sorted);
		}
	}
	
	
	/** Метода проверява дали позиция от дадена версия има релации в кореспондираща таблица
	 * @param idVer идентификатор на версия
	 * @param code код на позиция
	 * @throws DbErrorException грешка при работа с БД
	 */
	public boolean checkRelationPosition(Integer idVer , String code) throws DbErrorException {
		try {
			
			
			Query qs = JPA.getUtil().getEntityManager().createNativeQuery("SELECT COUNT(ID) FROM RELATION WHERE ID_TABLE IN (SELECT ID FROM TABLE_CORRESP WHERE ID_VERS_SOURCE =:IDVER) AND SOURCE_CODE =:CODE");
			qs.setParameter("IDVER", idVer);
			qs.setParameter("CODE" , code);
			
			Integer countS = SearchUtils.asInteger(qs.getSingleResult());
			
			if(countS!=null && countS.intValue()>0) {
				return true;
			} else {
				Query qt = JPA.getUtil().getEntityManager().createNativeQuery("SELECT COUNT(ID) FROM RELATION WHERE ID_TABLE IN (SELECT ID FROM TABLE_CORRESP WHERE ID_VERS_TARGET =:IDVER) AND TARGET_CODE =:CODE");
				qt.setParameter("IDVER", idVer);
				qt.setParameter("CODE" , code);
				Integer countT = SearchUtils.asInteger(qt.getSingleResult());
				
				if(countT!=null && countT.intValue()>0) {
					return true;
				} 
			}
			
			return false;
			
		} catch (Exception e) {
			LOGGER.error("Грешка при проверка на релации за позиция в кореспондираща таблица!");
			throw new DbErrorException("Грешка при проверка на релации за позиция в кореспондираща таблица!", e);
		}
	}
	
	
	
	/** Изтрива позиция от схема 
	 * @param position позиция с елемет от схема в него
	 * @throws DbErrorException грешка при работа с БД
	 * @throws ObjectInUseException позиция или децата и се използват и в други обекти
	 */
	public void deletePosition(PositionS position) throws DbErrorException, ObjectInUseException {
		
		if (position == null) {
			return;
		}
		
		// изтрива позицията и всичките и наследници
		deletePositionS(position);
		
		//ако има позиция след изтритата да я насоча към правилната IdPrev
		PositionS nextPos = findByIdPrev(position.getId()); 
		if(nextPos != null) {
			nextPos.setIdPrev(position.getIdPrev());
			save(nextPos);
		}
		
		 // Проверяваме дали нашият елемент е единствено дете (ако е, то трябва да ъпдейтнем инфото на родителя) 
		if(position.getIdParent()>0) {
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("SELECT COUNT(ID) FROM POSITION_SCHEME WHERE ID_PARENT =:PARENT AND VERSION_ID =:VERSION");
			q.setParameter("PARENT", position.getIdParent());
			q.setParameter("VERSION", position.getVersionId());
		 	Integer count = SearchUtils.asInteger(q.getSingleResult());
		 	
		 	if(count!=null && count.intValue()==0) {
		 		PositionS prenttPos = findById(position.getIdParent()); 
		 		prenttPos.setIndChild(0);
		 		save(prenttPos);
		 	}
		}
		
	}
	
	
	
	/** Изтрива позиция от схема (прехвърлен метод от PositionDAO)
	 * @param position позиция с елемет от схема в него
	 * @throws DbErrorException грешка при работа с БД
	 * @throws ObjectInUseException позиция или децата и се използват и в други обекти
	 */
	private void deletePositionS(PositionS position) throws DbErrorException, ObjectInUseException {
		
		if (position == null) {
			return;
		}
		
//		това не ни трябва има само една позиция
		
//		Query q = JPA.getUtil().getEntityManager().createNativeQuery("select id from SCHEME where POSITION_ID = :IDP");
//		q.setParameter("IDP", position.getId());
//		ArrayList<Object> ids = (ArrayList<Object>) q.getResultList();
//		
//		if (ids.size() == 1) {
//			delete(position);
//		}
		
		
		delete(position);
		
//		таблиците схема и позиции се обединиха 
//		sdao.delete(position.getSchemeItem())
		
//		не извличаме схемата а направо списъка с позиции
//		List<SchemeItem> children = sdao.loadScheme(position.getSchemeItem().getVersionId(), position.getSchemeItem().getId(), NSIConstants.CODE_DEFAULT_LANG);
		
		List<PositionS> children= loadScheme(position.getVersionId(), position.getId(), NSIConstants.CODE_DEFAULT_LANG, null, 0);
		
		for (PositionS child : children) {
//			Не е нужно да зареждаме схемата отново
//			Position p = findById(child.getPositionId());
//			p.setSchemeItem(child);
			
			deletePositionS(child);
		}
		
		
		
	}
	
	public SelectMetadata findPositionsByText(String textSearch, Integer lang, 
			boolean oficialTitle,
			boolean longTitle,
			boolean shortTitle,
			boolean alternateTitle,
			boolean include,
			boolean alsoInclude,
			boolean exclude,
			boolean rules,
			boolean comment) {
		

		HashMap<String, Object> params = new HashMap<String, Object>();
		
		StringBuffer SQLSelect = new StringBuffer();
			SQLSelect.append("SELECT DISTINCT ");
			SQLSelect.append("    pl.POSITION_ID, ");
			SQLSelect.append("    cl.IDENT clasifIdent, ");
			SQLSelect.append("    vl.IDENT versionIdent, ");
			SQLSelect.append("    ps.CODE, ");
			SQLSelect.append("    pl.OFFICIAL_TITLE, ");
			SQLSelect.append("    pl.LONG_TITLE, ");
			SQLSelect.append("    pl.SHORT_TITLE, ");
			SQLSelect.append("    pl.ALTERNATE_TITLES, ");
			SQLSelect.append("    pl.INCLUDES, ");
			SQLSelect.append("    pl.ALSO_INCLUDES, ");
			SQLSelect.append("    pl.EXCLUDES, ");
			SQLSelect.append("    pl.RULES, ");
			SQLSelect.append("    pl.COMMENT, ");
			SQLSelect.append("    v.id versionId, ");
			SQLSelect.append("    ps.id_parent, ");
			SQLSelect.append("    cl.CLASSIFICATION_ID ");
			
		StringBuffer SQL = new StringBuffer();
			
			SQL.append(" FROM ");
			SQL.append("    POSITION_LANG pl, ");
			SQL.append("    POSITION_SCHEME ps, ");
			SQL.append("    VERSION v, ");
			SQL.append("    VERSION_LANG vl, ");
			SQL.append("    CLASSIFICATION_LANG cl ");
			SQL.append(" WHERE ");
			SQL.append("    cl.CLASSIFICATION_ID=v.ID_CLASS ");
			SQL.append(" AND v.ID=ps.VERSION_ID ");
			SQL.append(" AND vl.VERSION_ID=v.id ");
			SQL.append(" AND pl.POSITION_ID=ps.ID ");
			SQL.append(" AND cl.LANG=:lang ");
			SQL.append(" AND vl.LANG=:lang ");
			SQL.append(" AND pl.LANG=:lang ");
			
			
			List<String> whereOr = new ArrayList<String>();
			
			if (oficialTitle) {
				whereOr.add("lower(pl.OFFICIAL_TITLE) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
			
			if (longTitle) {
				whereOr.add("lower(pl.LONG_TITLE) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
			
			if (shortTitle) {
				whereOr.add("lower(pl.SHORT_TITLE) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
			
			if (alternateTitle) {
				whereOr.add("lower(pl.ALTERNATE_TITLES) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
			
			if (include) {
				whereOr.add("lower(pl.INCLUDES) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
			
			if (alsoInclude) {
				whereOr.add("lower(pl.ALSO_INCLUDES) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
			
			if (exclude) {
				whereOr.add("lower(pl.EXCLUDES) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
			
			if (rules) {
				whereOr.add("lower(pl.RULES) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
			
			if (comment) {
				whereOr.add("lower(pl.COMMENT) LIKE '%"+textSearch.toLowerCase()+"%'");
			}
		
			
			
			if (!whereOr.isEmpty()) {
				SQL.append(" and ( ");
				for (int i = 0; i < whereOr.size(); i++) {	
					SQL.append(whereOr.get(i));
					if (i != (whereOr.size() - 1)) {
						SQL.append(" OR ");
					}
				}
				SQL.append(" ) ");
			} 
			 
			params.put("lang", lang);
		    
	    
	    
		 
		
		SelectMetadata smd = new SelectMetadata();
		smd.setSql(SQLSelect.toString()+SQL.toString());
		smd.setSqlCount("SELECT COUNT(pl.position_id) as counter  "+SQL.toString());
		smd.setSqlParameters(params);
//		smd.setResultSetMapping("TestIvanCint");
		return smd;
	}
	
	
	
	/**
	 * Сортира ги по код на предходен
	 *
	 * @param scheme
	 * @throws UnexpectedResultException
	 */
	public void doSortSchemePrevNew(List<PositionS> scheme) throws UnexpectedResultException {
		if (scheme == null || scheme.isEmpty()) {
			return;
		}

		List<PositionS> sorted = new ArrayList<PositionS>();
		
		recLoadNivoNew(scheme, 0, 0, sorted);
		

		if (!scheme.isEmpty()) {
			StringBuilder codes = new StringBuilder();

			for (Iterator<PositionS> iterator = scheme.iterator(); iterator.hasNext();) {
				PositionS sci = iterator.next();

				codes.append(",");
				codes.append(sci.getId());
			}
			throw new UnexpectedResultException("Повреденa схема ма версия с id = " + scheme.get(0).getVersionId() + ". Останали значения:" + scheme.size() + " codes:[" + codes.toString() + "]");
		}

		scheme.clear();
		scheme.addAll(sorted);
	}
	
	
	private static void recLoadNivoNew(List<PositionS> scheme, int prev, int parent, List<PositionS> sorted) {
		PositionS poreden = new PositionS();
		
		
		while (poreden != null) {
			poreden  = null;
			for (int i = 0; i < scheme.size(); i++) {
				PositionS tek = scheme.get(i);
				//System.out.println(tek.getId() + " " + tek.getName());
				if (tek.getIdParent() == parent && tek.getIdPrev() == prev) {
					poreden = tek;					
					break;
				}
			}
			
			if (poreden != null) {
				sorted.add(poreden);			
				scheme.remove(poreden);
				
				
				recLoadNivoNew(scheme, 0, poreden.getId(), sorted);
				prev = poreden.getId();
			}
			
		}		
	}
	
	private static void recLoadNivoAsObjNew(List<Object[]> scheme, int prev, int parent, List<Object[]> sorted) {
		Object[] poreden = new Object[20];
		
		
		while (poreden != null) {
			poreden  = null;
			for (int i = 0; i < scheme.size(); i++) {
				Object[] tek = scheme.get(i);
				if (SearchUtils.asInteger(tek[2]) == parent && SearchUtils.asInteger(tek[1]) == prev) {
					poreden = tek;
					break;
				}
			}
			
			if (poreden != null) {
				//System.out.println(poreden[3]);
				sorted.add(poreden);			
				scheme.remove(poreden);
				
				
				recLoadNivoAsObjNew(scheme, 0, SearchUtils.asInteger(poreden[0]), sorted);
				prev = (int) poreden[0];
			}
			
			
			
		}
	}
	
	/**
	 *  Метода търси по зададен стринг позиции от дадена кл.версия в екрана за позиции.
	 * @param textSearch (търси в код на позиция, наименование)
	 * @param idVersion  идентификатор на кл. версия
	 * @param lang код на език
	 *  @throws DbErrorException грешка при работа с Б
	 * */
	public List<PositionS> searchPositionsVersion(String textSearch, Integer lang ,Integer idVersion ,boolean findCod , boolean findName) throws DbErrorException{
		
		
		try {
			
			StringBuffer sql = new StringBuffer("SELECT POSITION_SCHEME.ID AS ID, POSITION_SCHEME.CODE AS CODE, PL.OFFICIAL_TITLE AS DEFF_NAME, "
					+ "   VERSION_ID AS VERSION_ID, ID_PREV AS ID_PREV, ID_PARENT AS ID_PARENT, LEVEL_NUMBER AS LEVEL_NUMBER, IND_CHILD AS IND_CHILD "
					+ "   FROM POSITION_SCHEME  JOIN POSITION_LANG PL ON  PL.POSITION_ID =  POSITION_SCHEME.ID AND PL.LANG = :LANG "
					+ "   WHERE VERSION_ID = :VID") ;
			
			
			
			List<String> params = new ArrayList<String>();
			if(findCod) {
				params.add(" UPPER(CODE) LIKE '%"+textSearch.toUpperCase()+"%' ");
			}
			if(findName) {
				params.add(" UPPER(PL.OFFICIAL_TITLE) LIKE '%"+textSearch.toUpperCase()+"%' ");
			}
			
			if(!params.isEmpty()) {
				sql .append(" AND ( ");
				int i=0;
				for(String p :params) {
					if(i>0) { 
						sql .append(" OR ");
					}
					sql .append(p);
					i++;
				}
				sql .append(" ) ");
			}
						
			sql .append( " ORDER BY CODE ");
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString(), "loadSchemeNew");
			q.setParameter("VID", idVersion);
			q.setParameter("LANG", lang);
			
			
			@SuppressWarnings("unchecked")
			List<PositionS> scheme =  q.getResultList();
					
			
			return scheme;
			
		} catch (Exception e) {
			LOGGER.error("Грешка при търсене на позиции!");
			throw new DbErrorException("Грешка при търсене на позиции!", e);
		}
	}
	
	/** Метода проверява дали зададените кодове на позиции съществуват в БД за дадена версия
	 * @param idVer идентификатор на версия
	 * @param codes Списък с кодове на позиция
	 * @throws DbErrorException грешка при работа с БД
	 */
	public Integer checkCodeExists(Integer idVer , ArrayList<String> codes) throws DbErrorException {
		Integer count=0;
		try {
			
			
			Query qs = JPA.getUtil().getEntityManager().createNativeQuery("Select count(id) from dbo.POSITION_SCHEME ps where VERSION_ID =:idVer and code in ( :codes)");
			qs.setParameter("idVer", idVer);
			qs.setParameter("codes", codes);
			
			count = SearchUtils.asInteger(qs.getSingleResult());
			
		} catch (Exception e) {
			LOGGER.error("Грешка при проверка дали списък от кодове съществува в релация!");
			throw new DbErrorException("Грешка при проверка дали списък от кодове съществува в релация!", e);
		}
		return count;
	}
	
    @SuppressWarnings("unchecked")
	public List<Object[]> loadRelationTextsExport(Integer idVersion, Integer lang) throws DbErrorException{
		StringBuffer SQL = new StringBuffer();
		SQL.append("select p.id,p.ID_PREV, p.ID_PARENT,p.code,l.OFFICIAL_TITLE ");
		SQL.append(" FROM ");
		SQL.append("    POSITION_SCHEME p, ");
		SQL.append("    POSITION_LANG l ");
		SQL.append(" WHERE ");
		SQL.append("    l.POSITION_ID=p.id ");
		SQL.append(" AND   l.lang=:lang ");
		SQL.append(" AND p.VERSION_ID=:idVersion");
		
		Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
		q.setParameter("idVersion", idVersion);
		q.setParameter("lang", lang);
		
		
		return q.getResultList();
		
}
	
}





