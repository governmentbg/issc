package  com.ib.nsiclassif.db.dao;

import com.ib.system.utils.SearchUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.system.db.SelectMetadata;
import com.ib.nsiclassif.db.dto.Publication;

/**
 * @author ivanc
 *
 */

public class PublicationDAO extends AbstractDAO<Publication> {

	static final Logger LOGGER = LoggerFactory.getLogger(PublicationDAO.class);
	
	public PublicationDAO (ActiveUser user){
		
		super(user);		
	}
	
	/**Изгражда sql за извличане от БД на обекти/публикации по зададен филтър. Задължително включва/връща и ид. на обектите. 
	 * @param Date - dateFrom - начална дата на публикацията 
	 * @param Date - dateTo - крайна дата на публикацията
	 * @param List<Integer> codeSections - код на избраните секции
	 * @return SelectMetadata - sql със сетнати параметри. 
	 */
	public SelectMetadata findPublFilter(Date dateFrom, Date dateTo, List<Integer> codeSections, String title, Integer lang, Boolean nexist){
		String dialect = JPA.getUtil().getDbVendorName();
		HashMap<String, Object> params = new HashMap<String, Object>();
		StringBuffer sql = new StringBuffer();
		List<String> whereStr = new ArrayList<String>();
		
		String selAnot=DialectConstructor.limitBigString(dialect, "pubLang.ANNOTATION", 300) +" A6 ";
		
		sql.append("SELECT DISTINCT pub.ID A0, pub.SECTION A1, pub.DATE_FROM A2, pub.DATE_TO A3, pub.IMAGE_PUB A4, pubLang.TITLE A5, " + selAnot);
		String fromStr = " FROM PUBLICATION pub LEFT JOIN PUBLICATION_LANG pubLang ON (pub.ID=pubLang.PUBLICATION_ID)";
	
		if(null!=codeSections && ! codeSections.isEmpty())
	    	whereStr.add("pub.SECTION IN ("+codeSections.toString().trim().substring(1, codeSections.toString().trim().length()-1)+")");
	    
	    if (null!=dateFrom && null!=dateTo){
	    	whereStr.add("pub.DATE_FROM BETWEEN "+DialectConstructor.convertDateOnlyToSQLString(dialect, dateFrom) +" AND "+ DialectConstructor.convertDateOnlyToSQLString(dialect, dateTo) +"");
	    	
	    	
	    }else{
			if (null!=dateFrom && null==dateTo){
				whereStr.add("pub.DATE_FROM >= "+DialectConstructor.convertDateOnlyToSQLString(dialect, dateFrom));
			}else if(null==dateFrom && null!=dateTo){
				whereStr.add("(pub.DATE_FROM <= "+ DialectConstructor.convertDateOnlyToSQLString(dialect, dateTo) +")");
			}
			
		}	
	    
	   	    
	    if(null!=title && !title.isEmpty()){
	    	title=title.replaceAll("'", "''");	
	    	
	    	String [] tit = title.trim().toUpperCase().split(" ");
	    	String parm = "( ";
	    	boolean addfirst = true;
	    	for (int i = 0; i < tit.length; i++) {
	    		if (null==tit[i] || tit[i].length()<3)
	    			continue;
	    		if(addfirst) {
	    			parm+="UPPER(pubLang.TITLE) LIKE '%"+tit[i]+"%'";
	    			addfirst = false;
	    		}else {
	    			parm+=" OR UPPER(pubLang.TITLE) LIKE '%"+tit[i]+"%'";	    		}
	    	}
	    	parm+=" )";
	    	whereStr.add(parm);
		}
	    
	    if (null!=lang) {	    
		    if (nexist) {
		    	whereStr.add(" NOT EXISTS (SELECT pubLang.PUBLICATION_ID FROM PUBLICATION_LANG pubLang "+
		    			 " WHERE "+  
		    		     "pub.ID=pubLang.PUBLICATION_ID AND " +
		    		     "pubLang.lang = :codeLang)");
		    }else {
		    	whereStr.add("pubLang.LANG=:codeLang");
		    }
		    params.put("codeLang", lang); 
	    }

	    sql.append(fromStr);
	    
	    String strWhere=""; 
	    
		if (!whereStr.isEmpty()) {
			strWhere+=" WHERE ";
			for (int i = 0; i < whereStr.size(); i++) {	
				strWhere+=whereStr.get(i);
				if (i != (whereStr.size() - 1)) {
					strWhere+=" AND ";
				}
			}
		}
		
		sql.append(strWhere); 
		
		SelectMetadata smd = new SelectMetadata();
		smd.setSql(sql.toString());
		smd.setSqlCount("SELECT COUNT(distinct pub.ID) as counter  "+fromStr+strWhere);
		smd.setSqlParameters(params);
//		smd.setResultSetMapping("TestIvanCint");
		return smd;
	}
	
	
	/**Изгражда sql за извличане от БД на обекти/публикации по зададен филтър. Задължително включва/връща и ид. на обектите. 
	 * @param Date - dateFrom - начална дата на публикацията 
	 * @param Date - dateTo - крайна дата на публикацията
	 * @param Integer codeSection - код на секция
	 * @return SelectMetadata - sql със сетнати параметри. 
	 */
	public SelectMetadata findPublExtFilter(Date dateFrom, Date dateTo, Integer codeSection, String title, Integer lang, Date currentDate){
		String dialect = JPA.getUtil().getDbVendorName();
		HashMap<String, Object> params = new HashMap<String, Object>();
		StringBuffer sql = new StringBuffer();
		List<String> whereStr = new ArrayList<String>();
		
		String selAnot=DialectConstructor.limitBigString(dialect, "pubLang.ANNOTATION", 300) +" A2 ";
				
		sql.append("SELECT distinct pub.ID A0, pubLang.TITLE A1, "+ selAnot+ ", pub.IMAGE_PUB A3, pub.DATE_FROM A4, pubLang.URL_PUB A5, pub.type_pub A6, pub.link_page A7 ");
//		sql.append("SELECT distinct pub.ID A0, pubLang.TITLE A1, ");
		
		String fromStr = " FROM PUBLICATION pub JOIN PUBLICATION_LANG pubLang ON (pub.ID=pubLang.PUBLICATION_ID and pubLang.LANG=:lang)";
		
		
		if(null!=codeSection){
	    	whereStr.add("pub.SECTION=:codeSection");
	    	params.put("codeSection", codeSection); 
		}
	    
	    	    
	    if (null!=dateFrom && null!=dateTo){
	    	whereStr.add("((pub.DATE_FROM BETWEEN "+ DialectConstructor.convertDateOnlyToSQLString(dialect, dateFrom)+" AND "+ DialectConstructor.convertDateOnlyToSQLString(dialect, dateTo) +") AND (pub.DATE_TO IS NULL OR pub.DATE_TO >= "+ DialectConstructor.convertDateOnlyToSQLString(dialect, currentDate) +"))");
		}else{
			if (null!=dateFrom && null==dateTo){
				whereStr.add("(pub.DATE_FROM >="+ DialectConstructor.convertDateOnlyToSQLString(dialect, dateFrom) + " AND (pub.DATE_TO IS NULL OR pub.DATE_TO>= "+ DialectConstructor.convertDateOnlyToSQLString(dialect, currentDate)+"))" );
			}else if(null==dateFrom && null!=dateTo){
				whereStr.add("(pub.DATE_FROM <= " + DialectConstructor.convertDateOnlyToSQLString(dialect, dateTo)+" AND (pub.DATE_TO IS NULL OR (pub.DATE_TO >="+DialectConstructor.convertDateOnlyToSQLString(dialect, currentDate)+")))");
			}else {
				whereStr.add("(pub.DATE_TO IS NULL OR pub.DATE_TO >="+DialectConstructor.convertDateOnlyToSQLString(dialect, currentDate)+")");
			}
		}	
	    whereStr.add("(pub.DATE_FROM <="+ DialectConstructor.convertDateOnlyToSQLString(dialect, currentDate)+")");
	   	    
	    if(null!=title && !title.isEmpty()){
	    	title=title.replaceAll("'", "''");	
	    	
	    	String [] tit = title.trim().toUpperCase().split(" ");
	    	String parm = "( ";
	    	boolean addfirst = true;
	    	for (int i = 0; i < tit.length; i++) {
	    		if (null==tit[i] || tit[i].length()<3)
	    			continue;
	    		if(addfirst) {
	    			parm+="UPPER(pubLang.TITLE) LIKE '%"+tit[i]+"%'";
	    			addfirst = false;
	    		}else {
	    			parm+=" OR UPPER(pubLang.TITLE) LIKE '%"+tit[i]+"%'";
	    		}
	    	}
	    	parm+=" )";
	    	whereStr.add(parm);
		}
	    
	    if(null!=lang) {
	    	params.put("lang", lang); 
		}
			
	    sql.append(fromStr);
	    
	    String strWhere=""; 
	    
		if (!whereStr.isEmpty()) {
			strWhere+=" WHERE ";
			for (int i = 0; i < whereStr.size(); i++) {	
				strWhere+=whereStr.get(i);
				if (i != (whereStr.size() - 1)) {
					strWhere+=" AND ";
				}
			}
		}
		sql.append(strWhere); 
//		sql.append(strWhere + " ORDER BY pub.DATE_FROM DESC"); 
		
		SelectMetadata smd = new SelectMetadata();
		smd.setSql(sql.toString());
		smd.setSqlCount("SELECT COUNT(distinct pub.ID) as counter  "+fromStr+strWhere);
		smd.setSqlParameters(params);
//		smd.setResultSetMapping("TestIvanCext");
		
		return smd;
	}

		
	/** Извлича данни от БД по зададен sql и атрибут за сортиране на данните
	 * @param smd - sql за избор на данни 
	 * @param defaultSortColumn - колона от таблица в базата данни, по която се сортират данните
	 * @return
	 * @throws DbErrorException
	 */
	public LazyDataModelSQL2Array newLazyDataModel (SelectMetadata smd, String defaultSortColumn ) throws DbErrorException { 
		   
		if (smd == null) return null;
		 LazyDataModelSQL2Array list = null;
//		try {
			/*smd.setCachable(true);
			smd.setCacheRegion("queryTestKG");*/
			//smd.setResultSetMapping("TestIvanC");
			
			/*smd.setCachable(true);
			smd.setCacheRegion("queryTestKG");*/
			
			
			list = new LazyDataModelSQL2Array(smd, defaultSortColumn);  
				
//		} catch (DbErrorException e) { 
//			LOGGER.error(e.getMessage(), e);
//			throw new DbErrorException ("Грешка при търсене на записи от БД!" +e.getCause().getMessage(), e);
//		}
	
		return list;
	}
	
	
	/**
	 * 
	 * @param classifCode
	 * @param roleCode
	 * @return
	 * @throws DbErrorException
	 */
	/*@SuppressWarnings("unchecked")
	public List<Object[]> findAdminData(Integer classifCode, Integer roleCode) throws DbErrorException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT au.names, au.email, au.phone ");
		sb.append("FROM adm_users au ");
		sb.append("JOIN adm_user_roles aur ON ");
		sb.append("aur.user_id = au.user_id AND aur.code_classif = ? and aur.code_role = ?");
		
		try{
			Query query = createNativeQuery(sb.toString()); 
			query.setParameter(1, classifCode);
			query.setParameter(2, roleCode);
			
			return (List<Object[]>) query.getResultList();
		
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new DbErrorException("Възникна грешка при четене на данни от БД", e);
		}
	}*/
	
	
	/** Извлича от БД на EMAIL на администратора, по неговия код на службата
	 * @param orgCode - код на службата
	 * @return
	 * @throws DbErrorException
	 */
	/*@SuppressWarnings("unchecked")
	public List<Object[]> findAdminEmailByOrgCode(Integer orgCode)  throws DbErrorException {
		
		String sqlSel="SELECT au.USER_ID, au.EMAIL FROM ADM_USERS au WHERE au.ORG_CODE = ? ";
			
			try{
				Query query = createNativeQuery(sqlSel); 

				query.setParameter(1, orgCode);
			
				return query.getResultList();
			
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new DbErrorException("Възникна грешка при четене на данни от БД", e);
			}
	
	}*/
	/** Проверка за брой записи в една секция
	 * @param section - код на секцията
	 * @param section - език
	 * @return
	 * @throws DbErrorException
	 */
	
	@SuppressWarnings("unchecked")
	public Integer checkForSinglePublicInSect(Integer section, Integer lang) throws DbErrorException {
		try{
			/*
			 * String sql =
			 * "SELECT ID FROM PUBLICATION WHERE SECTION = ? AND DATE_TO is null"; //TODO
			 * Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			 * q.setParameter(1, section);
			 */
			String sql = "SELECT distinct pub.ID "
					+ "FROM PUBLICATION pub "
					+ "JOIN PUBLICATION_LANG pubLang ON "
					+ "(pub.ID=pubLang.PUBLICATION_ID and pub.DATE_TO is null and pubLang.LANG=? and pub.SECTION=?)";
								
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter(1,  lang);
			q.setParameter(2,  section);
			
			List<Object> rez = q.getResultList();
			
			if(rez!=null && rez.size()==1) {
				return SearchUtils.asInteger(rez.get(0));
			} else if(rez==null || rez.size()==0) {
				return -1;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new DbErrorException("Възникна грешка при четене на данни от БД checkCountPublicinSection", e);
		}
		return null;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public Integer checkForSinglePublInSectNoLang(Integer section, Integer lang) throws DbErrorException {
		try{
			
			String sql = "SELECT distinct pub.ID "
					+ "FROM PUBLICATION pub "
					+ "WHERE "
					+ "pub.DATE_TO is null and pubLang.LANG=? and pub.SECTION=?";
								
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter(1,  lang);
			q.setParameter(2,  section);
			
			List<Object> rez = q.getResultList();
			
			if(rez!=null) {
				return SearchUtils.asInteger(rez.get(0));
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new DbErrorException("Възникна грешка при четене на данни от БД checkCountPublicinSection", e);
		}
		
	}
	
	/*List<SystemClassif> getChildrenOnNextLevel(int codeClassif, int codeParent, int lang, Date date) throws DbErrorException {
		LOGGER.debug("getChildrenOnNextLevel(codeClassif={},codeParent={},lang={},date={})", codeClassif, codeParent, lang, date);

		List<SystemClassif> classif = new ArrayList<>();

//		List<Object> res = null;
		ResultSet res = null;
		PreparedStatement prepared = null;
		
		try {
			java.sql.Date startDate = toSqlStartDate(date);

			String sql = "SELECT CODE, CODE_PREV, TEKST, LEVEL_NUMBER, CODE_EXT, DOP_INFO" //
				+ " FROM SYSTEM_CLASSIF" //
				+ " WHERE CODE_PARENT = ? AND CODE_CLASSIF = ? and LANG = ? AND DATE_OT <= ? AND DATE_DO > ?" //
				+ " ORDER BY LEVEL_NUMBER";
			
			JPA.getUtil().getEntityManager().getTransaction().
			prepared = getConnection().prepareStatement(sql);
			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			
			q.setParameter(1, codeParent);
			q.setParameter(2, codeClassif);
			q.setParameter(3, lang);
			q.setParameter(4, startDate);
			q.setParameter(5, startDate);

			res = q.getResultList();
			for (Object item : res) {
				classif.add(new SystemClassif(codeClassif, item.getInt(1), item.getInt(2), codeParent, item.getString(3), item.getInt(4), item.getString(5), item.getString(6)));
			}
			
			while (res.iterator()) {
				classif.add(new SystemClassif(codeClassif, rs.getInt(1), rs.getInt(2), codeParent, rs.getString(3), rs.getInt(4), rs.getString(5), rs.getString(6)));
			}
		} catch (Exception e) {
			throw new DbErrorException("Грешка при извличане на класификация от H2 базата !", e);
		} finally {
			silentClose(rs);
			silentClose(prepared);
		}
		LOGGER.debug("getChildrenOnNextLevel return classification with size:{}", classif.size());
		return classif;
	}
	
	protected java.sql.Date toSqlStartDate(Date date) {
		if (date == null) {
			return new java.sql.Date(System.currentTimeMillis());
		}
		return new java.sql.Date(date.getTime());
	}*/
	
}
