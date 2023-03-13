package com.ib.nsiclassif.db.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.SchemeItem;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.UnexpectedResultException;

/**
 * DAO for {@link SchemeItem}
 *
 * @author mamun
 */
public class SchemeDAO extends AbstractDAO<SchemeItem> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(SchemeDAO.class);

	/** @param user */
	public SchemeDAO(ActiveUser user) {
		super(user);
		// TODO Auto-generated constructor stub
	}

	
	
	/**
	 * Сортира ги по код на предходен
	 *
	 * @param scheme
	 * @throws UnexpectedResultException
	 */
	public void doSortClassifPrev(List<SchemeItem> scheme) throws UnexpectedResultException {
		if (scheme == null || scheme.isEmpty()) {
			return;
		}

		List<SchemeItem> sorted = new ArrayList<>();
		
		recLoadNivo(scheme, 0, 0, sorted);
		

		if (!scheme.isEmpty()) {
			StringBuilder codes = new StringBuilder();

			for (Iterator<SchemeItem> iterator = scheme.iterator(); iterator.hasNext();) {
				SchemeItem sci = iterator.next();

				codes.append(",");
				codes.append(sci.getId());
			}
			throw new UnexpectedResultException("Повреденa схема ма версия с id = " + scheme.get(0).getVersionId() + ". Останали значения:" + scheme.size() + " codes:[" + codes.toString() + "]");
		}

		scheme.clear();
		scheme.addAll(sorted);
	}
	
	
	private static void recLoadNivo(List<SchemeItem> scheme, int prev, int parent, List<SchemeItem> sorted) {
		SchemeItem poreden = null;

		for (int i = 0; i < scheme.size(); i++) {
			SchemeItem tek = scheme.get(i);
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
	 * Ето го и поредният метод за зареждане на дървета !!!!! Това е нова (вер. 2) на стария ТУРБО метод
	 * {@link #loadTree2(ArrayList,title)} следователно това е ХИПЕР-ТУРБО метод за изграждане на дървета от системна
	 * клсификация.<br>
	 * Предишната версия не отразяваше коректно подредбата в рамките на ено ниво. Сега всичко трябва да е наред.
	 * <p>
	 * 1. НАИ-ВАЖНО-то нещо което трябва да се знае е: <br>
	 * <b>НЕ МЕ ПИТАЙТЕ КАК РАБОТИ. Отдавна съм забравил!!!!!!!!!!!!</b> <br>
	 * </p>
	 * <p>
	 * 2. Проведени са следните тестове: <br>
	 * &nbsp;&nbsp;&nbsp; зареждане на 100 дървета (класификация с мену-то на системите (31 позиции) и резултатите са
	 * следните:<br>
	 * loadTreeData(метода от АИС ПАРЛАМЕНТ) 4s <br>
	 * loadTreeData2 ~0.5s <br>
	 * loadTreeData3 ~0.5s <br>
	 * (с 2 думи: "Mоя .... е най-голям"
	 * </p>
	 *
	 * @param arrTree       - списък от елементите на дървото от тип SystemClassif
	 * @param title         - стринг (не се използва)
	 * @param sortByName    -true/false дали елементите в дървото да са подредени по азбучен ред или по реда на въвеждане
	 * @param expanded      - true/false дали дървото да е отворено или затворено при първоначално показване
	 * @param readOnlyCodes - списък на кодове на елементи ,които да са само за разглеждане в дървото (не позволява да се избират)
	 * @param readOnlyType  - стринг , ако е подаден този параметър трябва да се дефинира treeNode от такъв тип
	 * @return
	 */
	public TreeNode loadTreeData3(List<SchemeItem> arrTree, String title, boolean sortByName, boolean expanded, List<Integer> readOnlyCodes, String readOnlyType) {
		return loadTreeData(arrTree, sortByName, DefaultTreeNode.class, null, expanded, readOnlyCodes, readOnlyType);
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

		if (tData.getData() != null && ((SchemeItem) tData.getData()).getId() == key) {
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
	
	/**
	 * @param arrTree       - списък от елементите на дървото от тип SystemClassif
	 * @param title         - стринг (не се използва)
	 * @param sortByName    -true/false дали елементите в дървото да са подредени по азбучен ред или по реда на въвеждане
	 * @param arg           - име на клас . Да знаем дали дървото да се изгради като стандартно или с чекбоксове
	 * @param expanded      - true/false дали дървото да е отворено или затворено при първоначално показване
	 * @param readOnlyCodes - списък на кодове на елементи ,които да са само за разглеждане в дървото (не позволява да се избират)
	 * @param readOnlyType  - стринг , ако е подаден този параметър трябва да се дефинира treeNode от такъв тип
	 * @return
	 */
	private TreeNode loadTreeData(List<SchemeItem> arrTree, boolean sortByName, Class<?> arg, List<Integer> selected, boolean expanded, List<Integer> readOnlyCodes, String readOnlyType) {
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
				doSortClassifPrev(arrTree);
				// Collections.sort(arrTree, new SortByLevel_CodePrev());
			}
			for (int i = 0; i < arrTree.size(); i++) {
				SchemeItem classItem = arrTree.get(i);

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
						SchemeItem classItem2 = arrTree.get(j);
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
	 * Този компаратор се използва за да сортираме системна класификация по ниво и код текст
	 *
	 * @author krasi
	 */
	private class SortByLevelTekst implements Comparator<SchemeItem>, Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 3621707321119040557L;

		/** @see Comparator#compare(Object, Object) */
		@Override
		public final int compare(SchemeItem a, SchemeItem b) {
			if (a.getLevelNumber() == b.getLevelNumber()) {
				return a.getDeffName().compareTo(b.getDeffName());
			}
			return a.getLevelNumber() - b.getLevelNumber();
		}
	}
	
	
	/** Връща списък на наследници на едно значение от схемата (дървото) на една версия
	 * @param idVersion - системен идентификатор на версия
	 * @param idItem и системен идентификатор на значение
	 * @return Списък от деца;
	 * @throws DbErrorException
	 * 
	 * @deprecated  
	 *              use {@link #loadScheme()} :
	 */
	@SuppressWarnings("unchecked")
	@Deprecated 
	public List<SchemeItem> findItemChildren(Integer idVersion, Integer idItem) throws DbErrorException{
		
		try {
			Query q = JPA.getUtil().getEntityManager().createQuery("from SchemeItem where versionId = :VID and idParent = :IDP");
			q.setParameter("VID", idVersion);
			q.setParameter("IDP", idItem);
			
			List<SchemeItem> items = (List<SchemeItem>) q.setHint("org.hibernate.cacheable", true).getResultList();
			LOGGER.debug("Start sort level");
			//doSortClassifPrev(items);
			sortSchemeLevelByPrev(items);
			LOGGER.debug("End sort level");
			
			return items; 
			
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на деца на елемент", e);
			throw new DbErrorException("Грешка при извличане на деца на елемент", e);
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
	public List<SchemeItem> loadScheme(Integer idVersion, Integer idIParent, Integer lang) throws DbErrorException{
		
		try {
			if (idVersion == null) {
				
			}
			
			String sql = "select SCHEME.ID, SCHEME.CODE, isnull(pl.OFFICIAL_TITLE, pdeff.OFFICIAL_TITLE + ' *') DEFF_NAME, VERSION_ID, SCHEME.POSITION_ID, ID_PREV, ID_PARENT, LEVEL_NUMBER, IND_CHILD  "
					+ " from SCHEME "
					+ "    left outer join POSITION_LANG pl on  pl.POSITION_ID =  SCHEME.POSITION_ID and pl.lang = :LANG "
					+ "    left outer join POSITION_LANG pdeff on  pdeff.POSITION_ID =  SCHEME.POSITION_ID and pdeff.lang = :DEFLANG "
					+ " where VERSION_ID = :VID" ;
			
			if (idIParent != null) {
				sql += " and id_parent = :IDP ";
			}
			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql, "loadScheme");
			q.setParameter("VID", idVersion);
			q.setParameter("LANG", lang);
			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
			
			if (idIParent != null) {
				q.setParameter("IDP", idIParent);
			}
			
			List<SchemeItem> scheme =  q.getResultList();
			
			if (idIParent != null) {
				sortSchemeLevelByPrev(scheme);
			}else {
				doSortClassifPrev(scheme);
			}
			
			return scheme;
			
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на схема с vestionId = " + idVersion, e);
			throw new DbErrorException("Грешка при извличане на схема с vestionId = " + idVersion, e);
		}
	}
	
	
	
	
	
	
	private void sortSchemeLevelByPrev(List<SchemeItem> items) throws UnexpectedResultException {
		
		List<SchemeItem> sorted = new ArrayList<SchemeItem>();
		boolean hasChange = true;
		Integer tekPrev = 0;		
		while (items.size() > 0 && hasChange) {
			hasChange = false;
			SchemeItem found = null;
			for (SchemeItem tek : items) {
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



	@SuppressWarnings("unchecked")
	public SchemeItem findByIdPrev(Integer id) throws DbErrorException {
		
		ArrayList<SchemeItem> rows = new ArrayList<SchemeItem>();
		try {
			rows = new ArrayList<SchemeItem>();

			if (id == null || id.equals(0)) {
				return null;
			}
			
			Query q = JPA.getUtil().getEntityManager().createQuery(" from SchemeItem where idPrev = :IDP");
			q.setParameter("IDP", id);
			
			rows = (ArrayList<SchemeItem>) q.getResultList();
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
	public SchemeItem findFirstChild(Integer id) throws DbErrorException {
		
		ArrayList<SchemeItem> rows = new ArrayList<SchemeItem>();
		try {
			rows = new ArrayList<SchemeItem>();

			if (id == null || id.equals(0)) {
				return null;
			}
			
			Query q = JPA.getUtil().getEntityManager().createQuery(" from SchemeItem where idParent = :IDP and idPrev = :IDPTV");
			q.setParameter("IDP", id);
			q.setParameter("IDPTV", 0);
			
			rows = (ArrayList<SchemeItem>) q.getResultList();
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
			LOGGER.error("Грешка при извличане на схема с vestionId = " + idVersion, e);
			throw new DbErrorException("Грешка при извличане на схема с vestionId = " + idVersion, e);
		}
	}
	
	

}
