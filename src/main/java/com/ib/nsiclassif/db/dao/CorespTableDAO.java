package com.ib.nsiclassif.db.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.Constants;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.CorespTableLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.SearchUtils;

/**
 * DAO for {@link CorespTable}
 *
 * @author mamun
 */
public class CorespTableDAO extends AbstractDAO<CorespTable> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(CorespTableDAO.class);

	/** @param user */
	public CorespTableDAO(ActiveUser user) {
		super(user);
		// TODO Auto-generated constructor stub
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public  List<CorespTable> findAllSite() throws DbErrorException {
		LOGGER.debug("Търсят се всички обекти от тип:{}", "CorespTable");
		try {
			Query q = JPA.getUtil().getEntityManager().createQuery("from CorespTable where finalized = :FIN order by dateReg DESC");
			q.setParameter("FIN", Constants.CODE_ZNACHENIE_DA);
			
			return  q.getResultList();
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане кореспондиращи таблици сайт", e);
			throw new DbErrorException(e);
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	public  List<CorespTable> findAllSiteNew() throws DbErrorException {
		LOGGER.debug("Търсят се всички обекти от тип:{}", "CorespTable");
		try {
			
			
			HashMap<Integer, CorespTable> tableMap = new HashMap<Integer, CorespTable>();
			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select c.ID, LANG, IDENT, NAME, ID_VERS_SOURCE, ID_VERS_TARGET, DATE_REG from TABLE_CORRESP c join TABLE_CORRESP_LANG cl  on c.ID = cl.TABLE_CORRESP_ID where FINALIZED = 1 ");
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
			for (Object[] row : result) {
				String ident = SearchUtils.asString(row[2]);
				String name = SearchUtils.asString(row[3]);
				Integer cId = SearchUtils.asInteger(row[0]);
				Integer llang = SearchUtils.asInteger(row[1]);
								
				Integer vSource = SearchUtils.asInteger(row[4]);
				Integer vTarget = SearchUtils.asInteger(row[5]);
				Date datReg = SearchUtils.asDate(row[6]);
				
				CorespTable table = tableMap.get(cId);
				if (table == null) {
					//Нова таблица 
					table = new CorespTable();
					table.setId(cId);
					table.setLangMap(new HashMap<Integer,CorespTableLang>());
					table.setIdVersSource(vSource);
					table.setIdVersTarget(vTarget);
					table.setDateReg(datReg);
					
					CorespTableLang cl = new CorespTableLang();
					cl.setLang(llang);
					cl.setCorespTable(table);
					cl.setIdent(ident);
					cl.setName(name);
					table.getLangMap().put(llang, cl);
					
					tableMap.put(cId, table);
					
				}else {
					CorespTableLang cl = new CorespTableLang();
					cl.setLang(llang);
					cl.setCorespTable(table);
					cl.setIdent(ident);
					cl.setName(name);
					table.getLangMap().put(llang, cl);
					
				}
				
			}
			
			List<CorespTable> tableList = new ArrayList<CorespTable>();
			Collections.sort(tableList, new SiteComparator());
			tableList.addAll(tableMap.values());
						
			return tableList;
			
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане таблици сайт", e);
			throw new DbErrorException(e);
		}
	}
	
	
	 
	
	
	
	
	
	
	
	
	
	
    
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<CorespTable> findCorespTableForCopy(Integer idVersion) throws DbErrorException{
		
		try {
			Query q = JPA.getUtil().getEntityManager().createQuery("from CorespTable where idVersSource = :IDV or idVersTarget = :IDV");
			q.setParameter("IDV", idVersion);
			
			
			return (ArrayList<CorespTable>) q.getResultList();
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане кореспондиращи таблици към версия", e);
			throw new DbErrorException("Грешка при извличане кореспондиращи таблици към версия", e);
		}
		
		
	}
	
	
	/** Копиране на релациите от една таблица на друг
     * @param fromIdVersion от версия
     * @param toIdVersion към версия
     * @param withCorespTables дали да се копират кореспондиращите таблици
     * @throws DbErrorException грешка при работа с БД
     */
    public void copyTableRelations(Integer fromIdtable, Integer toIdTable) throws DbErrorException {
    	try {
    		
    		Date datAction = new Date();
    		
    		//Изтрива темп таблицата за тази версията, която наливаме
    		Query q = JPA.getUtil().getEntityManager().createNativeQuery("DELETE FROM TEMP_COPY_RELATIONS WHERE ID_TABLE = :IDNEW");
    		q.setParameter("IDNEW", toIdTable);
    		int count = q.executeUpdate();
    		System.out.println("DELETE FROM TEMP_COPY_RELATIONS - Брой редове: " + count);
    		
    		//Прави съответствията в темп танлицата
    		q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO TEMP_COPY_RELATIONS (ID_TABLE, ID_OLD, ID_NEW)  SELECT :IDNEW, RELATION.ID, next value for SEQ_RELATION FROM RELATION WHERE ID_TABLE = :IDOLD");
    		q.setParameter("IDNEW", toIdTable);
    		q.setParameter("IDOLD", fromIdtable);    		
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO TEMP_COPY_RELATIONS - Брой редове: " + count);
    		
    		//Запис на позиции
    		String sql = "INSERT INTO RELATION(ID, ID_TABLE, SOURCE_CODE, TARGET_CODE, WEIGTH_SOURCE, WEIGTH_TARGET, RELATION_TYPE, CHANGE_TYPE, DATE_REG, USER_REG ) "
    				+ "    						SELECT TEMP_COPY_RELATIONS.ID_NEW, TEMP_COPY_RELATIONS.ID_TABLE, r1.SOURCE_CODE, r1.TARGET_CODE, r1.WEIGTH_SOURCE, r1.WEIGTH_TARGET, r1.RELATION_TYPE, r1.CHANGE_TYPE, :DAT, :USERID  "
    				+ "    					FROM RELATION r1 "
    				+ "    				             JOIN TEMP_COPY_RELATIONS ON   r1.ID = TEMP_COPY_RELATIONS.ID_OLD "
    				+ "    					WHERE TEMP_COPY_RELATIONS.ID_TABLE = :IDNEW";
    		q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
    		q.setParameter("IDNEW", toIdTable);    		
    		q.setParameter("USERID", getUserId());
    		q.setParameter("DAT", datAction);    		
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO RELATION Брой редове: " + count);
    		
    		//Запис на езикови атрибути
    		sql = "INSERT INTO RELATION_LANG (ID, RELATION_ID, LANG, COMMENT) \r\n"
    				+ "    SELECT next value for SEQ_RELATION_LANG, TEMP_COPY_RELATIONS.ID_NEW, LANG, COMMENT 	\r\n"
    				+ "        FROM RELATION_LANG JOIN TEMP_COPY_RELATIONS ON   RELATION_LANG.RELATION_ID = TEMP_COPY_RELATIONS.ID_OLD where TEMP_COPY_RELATIONS.ID_TABLE = :IDNEW";
    		q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
    		q.setParameter("IDNEW", toIdTable);
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO RELATION_LANG - Брой редове: " + count);
    		
    		//Журналиране на позиции
    		q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO SYSTEM_JOURNAL (ID, DATE_ACTION, ID_USER, CODE_ACTION, CODE_OBJECT, ID_OBJECT)    SELECT next value for SEQ_SYSTEM_JOURNAL, getdate(), -1, 1, 99, ID_NEW FROM TEMP_COPY_RELATIONS where ID_NEW > 0 AND ID_TABLE = :IDNEW");
    		q.setParameter("IDNEW", toIdTable);
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO SYSTEM_JOURNAL - Брой редове: " + count);
    		
  
    		
    	} catch (Exception e) {
			LOGGER.error("Грешка при копиране на позиции на таблица");
			throw new DbErrorException("Грешка при копиране на позиции на таблица", e);
		}
    	
    }
    
    
    /** Генерира ревърсивна таблица по id на таблица
     * @param idTable - id на таблица, от която се копита
     * @return новосъздаденат таблциа
     * @throws DbErrorException
     */
    public CorespTable createReverseTable(Integer idTable) throws DbErrorException {
    	
    	CorespTable source = findById(idTable);
    	
    	CorespTable newTable = new  CorespTable();
    	newTable.setIdVersSource(source.getIdVersTarget());
    	newTable.setIdVersTarget(source.getIdVersSource());
    	newTable.setPath(getUnitName());
    	newTable.setRelationsCount(source.getRelationsCount());
    	newTable.setRelationType(source.getRelationType());
    	newTable.setSourcePosCount(source.getTargetPosCount());
    	newTable.setStatus(idTable);
    	newTable.setTableType(source.getTableType());
    	newTable.setTargetPosCount(source.getSourcePosCount());
    	
    	Iterator<Entry<Integer, CorespTableLang>> it = source.getLangMap().entrySet().iterator();
    	while (it.hasNext()) {
    		Entry<Integer, CorespTableLang> entry = it.next();
    		CorespTableLang sl = entry.getValue();
    		CorespTableLang tl = new CorespTableLang();
    		tl.setComment(sl.getComment());
    		tl.setCorespTable(newTable);
    		tl.setIdent(sl.getIdent() + " (reverse copy)");
    		tl.setLang(sl.getLang());
    		tl.setName(sl.getName()  + " (reverse copy)");
    		tl.setRegion(sl.getRegion());
    		newTable.getLangMap().put(tl.getLang(), tl);
    	}
    	
    	newTable = save(newTable);
    	
    	try {
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO RELATION(ID, ID_TABLE, SOURCE_CODE, TARGET_CODE, EXPLANATION, DATE_REG, USER_REG, DATE_LAST_MOD, USER_LAST_MOD) "
					+ "    SELECT next value for SEQ_RELATION, :IDT, TARGET_CODE, SOURCE_CODE, EXPLANATION, DATE_REG, USER_REG, DATE_LAST_MOD, USER_LAST_MOD FROM RELATION where ID_TABLE = :IDS");
			q.setParameter("IDS", source.getId());
			q.setParameter("IDT", newTable.getId());
			q.executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Грешка при копиране на релации на таблица !", e);
			throw new DbErrorException("Грешка при копиране на релации на таблица !", e);
		}
    	
    	return newTable;
    }
    
    
    
    @SuppressWarnings("unchecked")
	public String decodeCorrespTableIdent(Integer correspTableId, Integer lang) throws DbErrorException {
    	
    	try {
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select IDENT, LANG from TABLE_CORRESP_LANG where TABLE_CORRESP_ID = :correspTableId and (LANG = :lang or LANG = :defflang)");
			q.setParameter("correspTableId", correspTableId);
			q.setParameter("lang", lang);
			q.setParameter("defflang", NSIConstants.CODE_DEFAULT_LANG);
			
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
			String defident = "";
			String ident = "";
			for (Object[] row : result) {
				Integer l = SearchUtils.asInteger(row[1]);
				String t = SearchUtils.asString(row[0]);
				if (l.equals(lang)) {
					ident = t;
				}
				if (l.equals(NSIConstants.CODE_DEFAULT_LANG)) {
					defident = t;
				}
			}
			if (ident != null && !ident.isEmpty()) {
				return ident;
			}else {
				return defident;
			}
    	  } catch (Exception e) {
  			LOGGER.error("Грешка декодиране на идентификатор на кореспондираща таблица", e);
  			throw new DbErrorException("Грешка декодиране на идентификатор на кореспондираща таблица", e);
  		}
    	
    }
    
    
    @Override
	public void delete(CorespTable entity) throws DbErrorException, ObjectInUseException {
    	
    	ObjectDocsDAO docdao = new ObjectDocsDAO(getUser());
    	ArrayList<Object[]> docs = docdao.findObjectDocsNative(entity.getCodeMainObject(), entity.getId(), NSIConstants.CODE_DEFAULT_LANG);
    	for (Object[] tek : docs){
    		Integer id = SearchUtils.asInteger(tek[0]);
    		docdao.deleteById(id);
    	}
    	
    	ObjectUsersDAO userdao = new ObjectUsersDAO(getUser());
    	ArrayList<Object[]> users = userdao.findObjectUsersNative(entity.getCodeMainObject(), entity.getId(), NSIConstants.CODE_DEFAULT_LANG);
    	for (Object[] tek : users){
    		Integer id = SearchUtils.asInteger(tek[0]);
    		userdao.deleteById(id);
    	}
    	
    	
    	try {
			JPA.getUtil().getEntityManager().createNativeQuery("delete from RELATION where ID_TABLE = :IDT").setParameter("IDT", entity.getId()).executeUpdate();
		} catch (Exception e) {
			LOGGER.error("Грешка при изтриване на релации към таблица!", e);
			throw new DbErrorException("Грешка при изтриване на релации към таблица!", e);
		}
    	
    	
    	super.delete(entity);
    	
    }
	
    @SuppressWarnings("unchecked")
   	public ArrayList<Object[]> getCorespTablesByVersions(Integer ver) throws DbErrorException{
       	
       	try {
       		StringBuffer SQL = new StringBuffer();
       		SQL.append("SELECT ");
       		SQL.append("    t.id, ");
       		SQL.append("    t.ID_VERS_SOURCE, ");
       		SQL.append("    ( ");
       		SQL.append("        SELECT ");
       		SQL.append("            ISNULL(vl.IDENT, '') ");
       		SQL.append("        FROM ");
       		SQL.append("            VERSION_LANG vl ");
       		SQL.append("        WHERE ");
       		SQL.append("            vl.VERSION_ID = t.ID_VERS_SOURCE ");
       		SQL.append("        AND vl.LANG = :DEFLANG ) verSourceName, ");
       		SQL.append("    t.ID_VERS_TARGET, ");
       		SQL.append("    ( ");
       		SQL.append("        SELECT ");
       		SQL.append("            ISNULL(vl.IDENT, '') ");
       		SQL.append("        FROM ");
       		SQL.append("            VERSION_LANG vl ");
       		SQL.append("        WHERE ");
       		SQL.append("            vl.VERSION_ID =t.ID_VERS_TARGET ");
       		SQL.append("        AND vl.LANG = :DEFLANG) verTargetName ");
       		SQL.append(" FROM ");
       		SQL.append("    TABLE_CORRESP t ");
       		SQL.append(" WHERE ");
       		SQL.append("    t.ID_VERS_SOURCE=:idVer");
   			
   			
   			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
   			q.setParameter("idVer", ver);
   			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
   			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
   			
   			return result;
   		} catch (Exception e) {
   			LOGGER.error("Грешка при извличане на версии на класификация", e);
   			throw new DbErrorException("Грешка при извличане на версии на класификация", e);
   		}
       	
       }
   	
    /**Добавен на 28.10.22г.
     * Използва се от сайта са извличане на ПУБЛИКУВАНИТЕ коресп. таблици към версия
     * */
  	@SuppressWarnings("unchecked")
  	public ArrayList<Object[]> getPublishedTables(Integer idVersion, Integer lang) throws DbErrorException {

  		try {
  			String sql = "select TABLE_CORRESP.id, isnull(ctl.ident, deff.ident) ident, isnull(ctl.NAME, deff.NAME) name, ID_VERS_SOURCE, ID_VERS_TARGET  from TABLE_CORRESP "
  					+ " left outer join TABLE_CORRESP_LANG ctl on TABLE_CORRESP.id = ctl.TABLE_CORRESP_ID and ctl.lang = :LANG"
  					+ " left outer join TABLE_CORRESP_LANG deff on TABLE_CORRESP.id = deff.TABLE_CORRESP_ID and deff.lang = :DEFLANG"
  					+ " where TABLE_CORRESP.finalized = :finalized AND ID_VERS_SOURCE = :idVersion or ID_VERS_TARGET = :idVersion ORDER BY DATE_REG DESC";

  			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
  			q.setParameter("LANG", lang);
  			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
  			q.setParameter("idVersion", idVersion);
  			q.setParameter("finalized", NSIConstants.CODE_ZNACHENIE_DA);
  			
  			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();

  			return result;
  		} catch (Exception e) {
  			LOGGER.error("Грешка при извличане на кореспондиращи таблици към версия", e);
  			throw new DbErrorException("Грешка при извличане на кореспондиращи таблици към версия", e);
  		}

  	}
  	
  	class SiteComparator implements Comparator<CorespTable> {
	    
		@Override
		public int compare(CorespTable o1, CorespTable o2) {
			
			if (o1 == null && o2 != null) {
				return 1;
			}
			
			if (o1 != null && o2 == null) {
				return -1;
			}
			
			if (o1 == null && o2 == null) {
				return 0;
			}
			
			//-------------------
			
			if (o1.getDateReg() == null && o2.getDateReg() != null) {
				return 1;
			}
			
			if (o1.getDateReg() != null && o2.getDateReg() == null) {
				return -1;
			}
			
			if (o1.getDateReg() == null && o2.getDateReg() == null) {
				return 0;
			}
			
			
			
			return -o1.getDateReg().compareTo(o2.getDateReg());
			
			
		}
  	}



}
