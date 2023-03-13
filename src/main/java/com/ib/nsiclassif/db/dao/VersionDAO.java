package com.ib.nsiclassif.db.dao;

import static com.ib.system.utils.SearchUtils.asInteger;

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
import com.ib.nsiclassif.db.dto.Level;
import com.ib.nsiclassif.db.dto.LevelLang;
import com.ib.nsiclassif.db.dto.ObjectUsers;
import com.ib.nsiclassif.db.dto.ObjectUsersLang;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.db.dto.VersionLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.SearchUtils;

/**
 * DAO for {@link Version}
 *
 * @author mamun
 */
public class VersionDAO extends AbstractDAO<Version> {
	
	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(VersionDAO.class);

	/** @param user */
	public VersionDAO(ActiveUser user) {
		super(user);
		// TODO Auto-generated constructor stub
	}
	
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public  List<Version> findAllSite() throws DbErrorException {
		LOGGER.debug("Търсят се всички обекти от тип:{}", "Version");
		try {
			Query q = JPA.getUtil().getEntityManager().createQuery("from Version where finalized = :FIN order by releaseDate DESC");
			q.setParameter("FIN", Constants.CODE_ZNACHENIE_DA);
			
			return  q.getResultList();
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане версии сайт", e);
			throw new DbErrorException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public  List<Version> findAllSiteNew() throws DbErrorException {
		LOGGER.debug("Търсят се всички обекти от тип:{}", "Version");
		try {
			
			
			HashMap<Integer, Version> versionMap = new HashMap<Integer, Version>();
			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select v.ID, vl.LANG, vl.IDENT, vl.TITLE, v.ID_CLASS, v.RELEASE_DATE, v.STATUS from VERSION v join VERSION_LANG vl  on v.ID = vl.VERSION_ID where FINALIZED = 1");
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
			for (Object[] row : result) {
				
				Integer vId = SearchUtils.asInteger(row[0]);
				String ident = SearchUtils.asString(row[2]);
				String name = SearchUtils.asString(row[3]);
				
				Integer llang = SearchUtils.asInteger(row[1]);
				
				Integer idClass = SearchUtils.asInteger(row[4]);
				
				Date relDate = SearchUtils.asDate(row[5]);
				
				Integer status = SearchUtils.asInteger(row[6]);
				
				
				Version version = versionMap.get(vId);
				if (version == null) {
					//Нова версия 
					version = new Version();
					version.setId(vId);
					version.setLangMap(new HashMap<Integer,VersionLang>());
					version.setIdClss(idClass);
					version.setReleaseDate(relDate);
					version.setStatus(status);
					
					
					VersionLang vl = new VersionLang();
					vl.setLang(llang);
					vl.setVersion(version);
					vl.setIdent(ident);					
					vl.setTitle(name);
					
					version.getLangMap().put(llang, vl);
					
					versionMap.put(vId, version);
					
				}else {
					VersionLang vl = new VersionLang();
					vl.setLang(llang);
					vl.setVersion(version);
					vl.setIdent(ident);
					vl.setTitle(name);
					version.getLangMap().put(llang, vl);
					
				}
				
			}
			
			List<Version> versionList = new ArrayList<Version>();
			versionList.addAll(versionMap.values());
			Collections.sort(versionList, new SiteComparator());			
			return versionList;
			
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане версии за сайт", e);
			throw new DbErrorException(e);
		}
	}
    
    
    @SuppressWarnings("unchecked")
	public ArrayList<Object[]> getClassifVersions(Integer idClass, Integer lang) throws DbErrorException{
    	
    	try {
			String sql = 	"select VERSION.ID,  ISNULL(vl.IDENT, deffv.IDENT) ident, ISNULL(vl.TITLE, deffv.TITLE) from VERSION  " 
					+ 		"    left outer join VERSION_LANG vl on VERSION.id = vl.VERSION_ID and vl.LANG = :LANG"
					+ 		"    left outer join VERSION_LANG deffv on VERSION.id = deffv.VERSION_ID and deffv.LANG = :DEFLANG"
					+ 		" WHERE id_class = :idc  ORDER BY RELEASE_DATE DESC ";
			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter("LANG", lang);
			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
			q.setParameter("idc", idClass);
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
			
			return result;
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на версии на класификация", e);
			throw new DbErrorException("Грешка при извличане на версии на класификация", e);
		}
    	
    }
    
    
    
    /** Създава нова версия-копие на дадена версия
     * @param idSourceVersion - системен идентификатор на версията, която копираме
     * @return изкопираната версия
     * @throws DbErrorException грешка при работа с БД
     */
    public Version copyVersion(Integer idSourceVersion, boolean copyLevels, boolean copyTables, boolean copyUsers, boolean copyScheme) throws DbErrorException {
    	
    	
    	try {
    		Version source = findById(idSourceVersion);
    		Version target = new Version();
    		
    		
    		target.setCopyright(source.getCopyright());
    		target.setDateReg(new Date());
    		target.setUserReg(getUserId());
    		target.setExpandedLevel(source.getExpandedLevel());
    		target.setIdClss(source.getIdClss());
    		target.setIdNextVer(null);
    		target.setIdPrevVer(idSourceVersion);
    		target.setLevelCount(source.getLevelCount());
    		target.setPositionCount(source.getPositionCount());
    		target.setRazprostranenie(source.getRazprostranenie());
    		target.setStatus(NSIConstants.CODE_ZNACHENIE_VER_STATUS_NEZAVARSHENA);
    		
    		Iterator<Entry<Integer, VersionLang>> it = source.getLangMap().entrySet().iterator();
    		while (it.hasNext()) {
    			Entry<Integer, VersionLang> entry = it.next();
    			Integer lang = entry.getKey();
    			VersionLang value = entry.getValue();
    			
    			VersionLang vLang = new VersionLang();
    			vLang.setAreas(value.getAreas());
    			vLang.setComment(value.getComment());
    			vLang.setDescription(value.getDescription());
    			vLang.setIdent(value.getIdent() + " (Copy)");
    			vLang.setLang(lang);
    			vLang.setLegalbase(value.getLegalbase());
    			vLang.setMethodology(value.getMethodology());
    			vLang.setNews(value.getNews());
    			vLang.setPod(value.getPod());
    			vLang.setPodUrl(value.getPodUrl());
    			vLang.setPublications(value.getPublications());
    			vLang.setTitle(value.getTitle() + " (Копие от " + new Date() + ")");
    			vLang.setVersion(target);
    			target.getLangMap().put(lang, vLang);
    		}
    		
    		target = save(target);
    		
    		
    		if (copyLevels) {
	    		Query q = JPA.getUtil().getEntityManager().createQuery("from Level where versionId = :versionId");
				q.setParameter("versionId", idSourceVersion);
				
				@SuppressWarnings("unchecked")			
				List<Level> levels = q.getResultList();
				LevelDAO ldao = new LevelDAO(getUser());
				for (Level level : levels) {
					Level newLevel = new Level();
					newLevel.setCodeType(level.getCodeType());
					newLevel.setLevelName(level.getLevelName());
					newLevel.setLevelNumber(level.getLevelNumber());
					newLevel.setMaskInherit(level.getMaskInherit());
					newLevel.setMaskReal(level.getMaskReal());
					newLevel.setPositionCount(level.getPositionCount());
					newLevel.setSymbolCount(level.getSymbolCount());
					newLevel.setVersionId(target.getId());
					
					Iterator<Entry<Integer, LevelLang>> itL = level.getLangMap().entrySet().iterator();
		    		while (itL.hasNext()) {
		    			Entry<Integer, LevelLang> entry = itL.next();
		    			Integer lang = entry.getKey();
		    			LevelLang value = entry.getValue();
		    			
		    			LevelLang nLang = new LevelLang();
		    			nLang.setDescription(value.getDescription());
		    			nLang.setLang(value.getLang());
		    			nLang.setLevel(newLevel);
		    			newLevel.getLangMap().put(lang, nLang);
		    		}
					
					
					ldao.save(newLevel);
				}
    		}
			
    		if (copyUsers) {
			ObjectUsersDAO udao = new ObjectUsersDAO(getUser());
				List<ObjectUsers> users = udao.findObjectUsersForCopy(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION, idSourceVersion);
				for (ObjectUsers user : users) {
					ObjectUsers newUser = new ObjectUsers();
					newUser.setCodeLice(user.getCodeLice());
					newUser.setCodeObject(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION);
					newUser.setIdObject(target.getId());
					newUser.setRole(user.getRole());
					newUser.setRoleDate(user.getRoleDate());
					Iterator<Entry<Integer, ObjectUsersLang>> itL = user.getLangMap().entrySet().iterator();
		    		while (itL.hasNext()) {
		    			Entry<Integer, ObjectUsersLang> entry = itL.next();
		    			Integer lang = entry.getKey();
		    			ObjectUsersLang value = entry.getValue();
		    			
		    			ObjectUsersLang nLang = new ObjectUsersLang();	    			
		    			nLang.setLang(value.getLang());
		    			nLang.setRoleComment(value.getRoleComment());
		    			nLang.setObjectUsers(newUser);
		    			newUser.getLangMap().put(lang, nLang);
		    		}
		    		udao.save(newUser);
					
				}
    		}
			
    		if (copyTables) {
				CorespTableDAO cdao = new CorespTableDAO(getUser());
				ArrayList<CorespTable> tables = cdao.findCorespTableForCopy(idSourceVersion);
				for (CorespTable table : tables) {
					CorespTable newTable = new CorespTable();
					
					if (idSourceVersion.equals(table.getIdVersSource())) {
						newTable.setIdVersSource(target.getId());
						newTable.setIdVersTarget(table.getIdVersTarget());
					}else {
						newTable.setIdVersSource(table.getIdVersSource());
						newTable.setIdVersTarget(target.getId());
					}
					
					//newTable.setIdVersSource(idSourceVersion);
					//newTable.setIdVersTarget(idSourceVersion);
					newTable.setPath(table.getPath());
					newTable.setRelationsCount(table.getRelationsCount());
					newTable.setRelationType(table.getRelationsCount());
					newTable.setSourcePosCount(table.getSourcePosCount());
					newTable.setStatus(table.getStatus());
					newTable.setTableType(table.getTableType());
					newTable.setTargetPosCount(table.getTargetPosCount());
					
					Iterator<Entry<Integer, CorespTableLang>> itC = table.getLangMap().entrySet().iterator();
		    		while (itC.hasNext()) {
		    			Entry<Integer, CorespTableLang> entry = itC.next();
		    			Integer lang = entry.getKey();
		    			CorespTableLang value = entry.getValue();
		    			
		    			CorespTableLang nLang = new CorespTableLang();	    			
		    			nLang.setLang(value.getLang());
		    			nLang.setComment(value.getComment());
		    			nLang.setIdent(value.getIdent());
		    			nLang.setName(value.getName()+ " (Копие от " + new Date() + ")");
		    			nLang.setRegion(value.getRegion());
		    			nLang.setCorespTable(newTable);
		    			newTable.getLangMap().put(lang, nLang);
		    		}
		    		newTable = cdao.save(newTable);
		    		
		    		
		    		copyTableRelations(table.getId(), newTable.getId());
					
				}
    		}
    		
    		if (copyScheme) {
    			copyVersionScheme(idSourceVersion, target.getId());
    		}
    		
    		return target;
    		
    		
    	} catch (DbErrorException e) {
    		throw e;
    	} catch (Exception e) {
			LOGGER.error("Грешка при копиране на версии на класификация", e);
			throw new DbErrorException("Грешка при копиране на версии на класификация", e);
		}
    	
    	
    	
    }
    
    
    
    /** Копиране на схема от една версия в друга
     * @param fromIdVersion от версия
     * @param toIdVersion към версия
     * @param withCorespTables дали да се копират кореспондиращите таблици
     * @throws DbErrorException грешка при работа с БД
     */
    private void copyVersionScheme(Integer fromIdVersion, Integer toIdVersion) throws DbErrorException {
    	try {
    		
    		Date datAction = new Date();
    		
    		//Изтрива темп таблицата за тази версията, която наливаме
    		Query q = JPA.getUtil().getEntityManager().createNativeQuery("DELETE FROM TEMP_COPY_VERSION WHERE ID_VERSION = :IDNEW");
    		q.setParameter("IDNEW", toIdVersion);
    		int count = q.executeUpdate();
    		System.out.println("DELETE FROM TEMP_COPY_VERSION - Брой редове: " + count);
    		
    		//Прави нулев ред в съоветствията заради id_prev, id_parent = 0
    		q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO TEMP_COPY_VERSION(ID_VERSION, ID_OLD, ID_NEW) VALUES(:IDNEW, 0, 0)");
    		q.setParameter("IDNEW", toIdVersion);    		
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO TEMP_COPY_VERSION - Брой редове: " + count);
    		
    		//Прави съответствията в темп танлицата
    		q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO TEMP_COPY_VERSION (ID_VERSION, ID_OLD, ID_NEW)  SELECT :IDNEW, ID, next value for SEQ_POSITION  FROM POSITION_SCHEME WHERE VERSION_ID = :IDOLD");
    		q.setParameter("IDNEW", toIdVersion);
    		q.setParameter("IDOLD", fromIdVersion);    		
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO TEMP_COPY_VERSION - Брой редове: " + count);
    		
    		//Запис на позиции
    		String sql = "INSERT INTO POSITION_SCHEME(ID, VERSION_ID, CODE, CODE_FULL, CODE_SEPARATE, CODE_TYPE, STATUS, LEVEL_NUMBER, ID_PREV, ID_PARENT, IND_CHILD, DATE_REG, USER_REG, DATE_LAST_MOD, USER_LAST_MOD) "
    				+ "		SELECT TC1.ID_NEW, :IDNEW, POSITION_SCHEME.CODE, POSITION_SCHEME.CODE_FULL, POSITION_SCHEME.CODE_SEPARATE, POSITION_SCHEME.CODE_TYPE, POSITION_SCHEME.STATUS, POSITION_SCHEME.LEVEL_NUMBER, TC2.ID_NEW, TC3.ID_NEW, IND_CHILD  , :DAT, :USERID, null, null "
    				+ "		FROM POSITION_SCHEME "
    				+ "	             JOIN TEMP_COPY_VERSION TC1 ON   POSITION_SCHEME.ID = TC1.ID_OLD "
    				+ "	             JOIN TEMP_COPY_VERSION TC2 ON   POSITION_SCHEME.ID_PREV = TC2.ID_OLD "
    				+ "	             JOIN TEMP_COPY_VERSION TC3 ON   POSITION_SCHEME.ID_PARENT = TC3.ID_OLD "
    				+ "	WHERE POSITION_SCHEME.VERSION_ID = :IDOLD AND "
    				+ " TC1.ID_VERSION = :IDNEW AND "
    				+ " TC2.ID_VERSION = :IDNEW AND "
    				+ " TC3.ID_VERSION = :IDNEW "    				;
    		
    		q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
    		q.setParameter("IDNEW", toIdVersion);
    		q.setParameter("IDOLD", fromIdVersion);
    		q.setParameter("USERID", getUserId());
    		q.setParameter("DAT", datAction);    		
    		count = q.executeUpdate();
    		System.out.println("Брой редове: " + count);
    		
    		
    		//Запис на измерителни единици
    		sql = "INSERT INTO POSITION_UNITS(ID, POSITION_ID, UNIT, TYPE_UNIT) "
    				+ "SELECT next value for SEQ_POSITION_UNITS, TEMP_COPY_VERSION.ID_NEW, UNIT, TYPE_UNIT "
    				+ "	FROM POSITION_UNITS JOIN TEMP_COPY_VERSION ON   POSITION_UNITS.POSITION_ID = TEMP_COPY_VERSION.ID_OLD where TEMP_COPY_VERSION.ID_VERSION = :IDNEW";
    		q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
    		q.setParameter("IDNEW", toIdVersion);
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO POSITION_UNITS - Брой редове: " + count);
    		
    		
    		//Запис на езикови атрибути
    		sql = "INSERT INTO POSITION_LANG(ID, POSITION_ID, LANG, OFFICIAL_TITLE, LONG_TITLE, SHORT_TITLE, ALTERNATE_TITLES, COMMENT, INCLUDES, ALSO_INCLUDES, EXCLUDES, RULES) 	"
    				+ "SELECT next value for SEQ_POSITION_LANG, TEMP_COPY_VERSION.ID_NEW, LANG, OFFICIAL_TITLE, LONG_TITLE, SHORT_TITLE, ALTERNATE_TITLES, COMMENT, INCLUDES, ALSO_INCLUDES, EXCLUDES, RULES "
    				+ "	FROM POSITION_LANG JOIN TEMP_COPY_VERSION ON   POSITION_LANG.POSITION_ID = TEMP_COPY_VERSION.ID_OLD where TEMP_COPY_VERSION.ID_VERSION = :IDNEW";
    		q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
    		q.setParameter("IDNEW", toIdVersion);
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO POSITION_LANG - Брой редове: " + count);
    		
    		
    		
    		
    		
    		//Журналиране на позиции
    		q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO SYSTEM_JOURNAL (ID, DATE_ACTION, ID_USER, CODE_ACTION, CODE_OBJECT, ID_OBJECT)    SELECT next value for SEQ_SYSTEM_JOURNAL, getdate(), -1, 1, 97, ID_NEW FROM TEMP_COPY_VERSION where ID_NEW > 0 AND ID_VERSION = :IDNEW");
    		q.setParameter("IDNEW", toIdVersion);
    		count = q.executeUpdate();
    		System.out.println("INSERT INTO SYSTEM_JOURNAL - Брой редове: " + count);
    		
    		
    		

    		
    		
    		
    		
    		
    		
    	} catch (Exception e) {
			LOGGER.error("Грешка при копиране на версии на класификация", e);
			throw new DbErrorException("Грешка при копиране на версии на класификация", e);
		}
    	
    }
    
    
    
	private void copyTableRelations(Integer idSourceTbale, Integer idTagetTable) throws DbErrorException {
			
		try {	
			String sqlInsert = "INSERT INTO RELATION (ID, ID_TABLE, SOURCE_CODE, TARGET_CODE, EXPLANATION, DATE_REG, USER_REG) 	 " +
			 " select next value for SEQ_RELATION, "+idTagetTable+", SOURCE_CODE, TARGET_CODE, EXPLANATION, GETDATE(), " + getUserId() + " from relation  where ID_TABLE = " + idSourceTbale ;
			System.out.println(sqlInsert);
			JPA.getUtil().getEntityManager().createNativeQuery(sqlInsert).executeUpdate();
			
	    } catch (Exception e) {
			LOGGER.error("Грешка при копиране на версии на класификация", e);
			throw new DbErrorException("Грешка при копиране на релации на таблица ", e);
		}
		
		
		
		
		
	}
	
    
    @SuppressWarnings("unchecked")
	public String decodeVersionIdent(Integer versionId, Integer lang) throws DbErrorException {
    	
    	try {
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select IDENT, LANG from VERSION_LANG where VERSION_ID = :versionId and (LANG = :lang or LANG = :defflang)");
			q.setParameter("versionId", versionId);
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
  			LOGGER.error("Грешка декодиране на идентификатор на версия", e);
  			throw new DbErrorException("Грешка декодиране на идентификатор на версия ", e);
  		}
    	
    }
    
    
    /** Изкарва списък на всички версии с класификациите им     * 
     * @param lang език
     * @return
     * @throws DbErrorException
     */
    @SuppressWarnings("unchecked")
	public ArrayList<Object[]> getAllVesrionsList(Integer lang) throws DbErrorException{
    	String sql = "SELECT "
    			+ "    version.id, "
    			+ "    isnull(vl.IDENT, deffl.IDENT) vident, "
    			+ "    isnull(cl.IDENT, deffcl.IDENT) cident "
    			+ " FROM  "
    			+ "    version "
    			+ "    left outer join  VERSION_LANG vl ON   version.ID=vl.VERSION_ID AND vl.LANG= :LANG "
    			+ "    left outer join  VERSION_LANG deffl ON   version.ID=deffl.VERSION_ID AND deffl.LANG= :DEFF "
    			+ "    left outer join  CLASSIFICATION_LANG cl ON       version.ID_CLASS=cl.CLASSIFICATION_ID   AND cl.LANG= :LANG "
    			+ "    left outer join  CLASSIFICATION_LANG deffcl ON   version.ID_CLASS=deffcl.CLASSIFICATION_ID AND deffcl.LANG= :DEFF "
    			+ " order by cident, vident";
    	
    	
    			try {
					Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
					q.setParameter("LANG", lang);
					q.setParameter("DEFF", NSIConstants.CODE_DEFAULT_LANG);
					
					return (ArrayList<Object[]>) q.getResultList();
				} catch (Exception e) {
					LOGGER.error("Грешка извличане на списък от версии", e);
		  			throw new DbErrorException("Грешка извличане на списък от версии", e);
				}
    }
    
	
	/** Разкодира наименование на версия и идентификатор     * 
     * @param idVersion идентификатор на версия
     * @param lang език
     * @return String
     * @throws DbErrorException
     */
	@SuppressWarnings("unchecked")
	public String decodeVersionIdentName(Integer idVersion, Integer lang) throws DbErrorException{
    	
    	try {
			String sql = 	"select ISNULL(vl.IDENT, deffv.IDENT) ident, ISNULL(vl.TITLE, deffv.TITLE) from VERSION  " 
					+ 		"    left outer join VERSION_LANG vl on VERSION.id = vl.VERSION_ID and vl.LANG = :LANG"
					+ 		"    left outer join VERSION_LANG deffv on VERSION.id = deffv.VERSION_ID and deffv.LANG = :DEFLANG"
					+ 		" WHERE VERSION.ID = :idv    ";
			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter("LANG", lang);
			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
			q.setParameter("idv", idVersion);
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
			
			if(result.size()>0) {
				return  result.get(0)[0]+" / "+result.get(0)[1];
			}
			
			return null;
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на версии на класификация", e);
			throw new DbErrorException("Грешка при извличане на версии на класификация", e);
		}
    	
    }
	
	
	@Override
	public void delete(Version entity) throws DbErrorException, ObjectInUseException {
		
		
		Integer cnt;

		try {
			cnt = asInteger( 
				createNativeQuery("select count(id) as cnt from TABLE_CORRESP where ID_VERS_SOURCE = ?1 or ID_VERS_TARGET = ?2") //
					.setParameter(1, entity.getId()) //
					.setParameter(2, entity.getId()) //
					.getResultList().get(0));
			if (cnt != null && cnt.intValue() > 0) {
				throw new ObjectInUseException("Версия "+entity.getIdentInfo()+" има създадени кореспондиращи таблици и не може да бъде изтрита!");
			}
		} catch (ObjectInUseException e) {
			throw e; // за да не се преопакова
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на свързани обекти към версия!", e);
		}

		
    	
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
			JPA.getUtil().getEntityManager().createNativeQuery("delete from POSITION_LANG where POSITION_ID in (select id from POSITION_SCHEME where VERSION_ID = :IDV)").setParameter("IDV", entity.getId()).executeUpdate();
			
			JPA.getUtil().getEntityManager().createNativeQuery("delete from POSITION_UNITS where POSITION_ID in (select id from POSITION_SCHEME where VERSION_ID = :IDV)").setParameter("IDV", entity.getId()).executeUpdate();
			
			JPA.getUtil().getEntityManager().createNativeQuery("delete from POSITION_SCHEME where VERSION_ID = :IDV").setParameter("IDV", entity.getId()).executeUpdate();
			
			JPA.getUtil().getEntityManager().createNativeQuery("delete from LEVEL_LANG where LEVEL_ID in (select id from LEVEL where VERSION_ID = :IDV)").setParameter("IDV", entity.getId()).executeUpdate();
			
			JPA.getUtil().getEntityManager().createNativeQuery("delete from LEVEL where VERSION_ID = :IDV").setParameter("IDV", entity.getId()).executeUpdate();
			
			
		} catch (Exception e) {
			LOGGER.error("Грешка при изтриване на позиции към версия!", e);
			throw new DbErrorException("Грешка при изтриване на релации към таблица!", e);
		}
    	
    	
    	super.delete(entity);
    	
    }
	
	 /**Добавен на 28.10.22г.
     *  Използва се от сайта са извличане на ПУБЛИКУВАНИТЕ версии към класификация
     * */
	@SuppressWarnings("unchecked")
	public ArrayList<Object[]> getPublishedVersions(Integer idClass, Integer lang) throws DbErrorException {

		try {
			String sql = "select VERSION.ID,  ISNULL(vl.IDENT, deffv.IDENT) ident, ISNULL(vl.TITLE, deffv.TITLE) from VERSION  "
					+ "    left outer join VERSION_LANG vl on VERSION.id = vl.VERSION_ID and vl.LANG = :LANG"
					+ "    left outer join VERSION_LANG deffv on VERSION.id = deffv.VERSION_ID and deffv.LANG = :DEFLANG"
					+ " WHERE id_class = :idc  and VERSION.FINALIZED = :finalized ORDER BY RELEASE_DATE DESC ";

			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter("LANG", lang);
			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
			q.setParameter("idc", idClass);
			q.setParameter("finalized", NSIConstants.CODE_ZNACHENIE_DA);
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();

			return result;
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на версии на класификация", e);
			throw new DbErrorException("Грешка при извличане на версии на класификация", e);
		}

	}
	
	
	class SiteComparator implements Comparator<Version> {
	    
		@Override
		public int compare(Version o1, Version o2) {
			
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
			
			if (o1.getReleaseDate() == null && o2.getReleaseDate() != null) {
				return 1;
			}
			
			if (o1.getReleaseDate() != null && o2.getReleaseDate() == null) {
				return -1;
			}
			
			if (o1.getReleaseDate() == null && o2.getReleaseDate() == null) {
				return 0;
			}
			
			
			
			return -o1.getReleaseDate().compareTo(o2.getReleaseDate());
			
			
		}
 }
	
}
