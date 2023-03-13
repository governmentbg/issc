package com.ib.nsiclassif.db.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.AdmUser;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.Relation;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.SearchUtils;

/**
 * DAO for {@link CorespTable}
 *
 * @author mamun
 */
public class RelationDAO extends AbstractDAO<Relation> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(RelationDAO.class);

	/** @param user */
	public RelationDAO(ActiveUser user) {
		super(user);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Намира релациите на кореспондираща таблица.
	 *
	 * @param username
	 * @return
	 */
	public Relation findByIdCorespTable(Integer idTable) {
		Query query = getEntityManager().createQuery("FROM Relation WHERE idTable = :idTable") //
			.setParameter("idTable", idTable);

		@SuppressWarnings("unchecked")
		List<Relation> relList = query.getResultList();

		if (relList.isEmpty()) {
			return null;
		}
		return relList.get(0);
	}
	/**
	 * Намира релациите на кореспондираща таблица.
	 *
	 * @param id_cores_table, code_source, code_target
	 * @return
	 * @throws DbErrorException 
	 */
	public List<Relation> getRelationsByCodeSourceTarget(Integer idTable, String codeSource, String codeTarget) throws DbErrorException {
		
		try {
			if (codeSource==null || codeSource.trim().isEmpty()) {
				codeSource=null;			
			}
			if (codeTarget==null || codeTarget.trim().isEmpty()) {
				codeTarget=null;			
			}
			StringBuffer SQL = new StringBuffer();
//			SQL.append(" select distinct a from(");
			
//				SQL.append(" SELECT DISTINCT ");
//				SQL.append("    r.id, ");
//				SQL.append("    r.sourceCode, ");
//				SQL.append("    r.targetCode ");
				SQL.append(" FROM ");
				SQL.append("    Relation r ");
				SQL.append(" WHERE ");
				SQL.append("    r.idTable=:idTable ");
			
			if (codeSource!=null && codeTarget!=null && !codeSource.isEmpty() && !codeTarget.isEmpty()) {
				SQL.append(" AND (r.sourceCode=:codeS or r.targetCode=:codeT)");	
			}else {
				if (codeSource!=null && !codeSource.isEmpty()) {		
					SQL.append(" AND r.sourceCode=:codeS ");	
				}else {
					if (codeTarget!=null && !codeTarget.isEmpty()) {	
						SQL.append(" AND r.targetCode=:codeT");
					}
				}
			}
			
//			if (codeSource!=null && codeTarget!=null) {
//				SQL.append(" UNION ALL ");	
//			}
//			
//			if (codeTarget!=null) {
//				SQL.append(" SELECT DISTINCT ");
//				SQL.append("    r.id, ");
//				SQL.append("    r.sourceCode, ");
//				SQL.append("    r.targetCode ");
//				SQL.append(" FROM ");
//				SQL.append("    Relation r ");
//				SQL.append(" WHERE ");
//				SQL.append("    r.idTable=:idTable ");
//				SQL.append(" AND r.targetCode=:codeT");
//			}
//			SQL.append(" ) a");
			Query q = getEntityManager().createQuery(SQL.toString());
			q.setParameter("idTable", idTable);
			if (codeSource!=null) {
				q.setParameter("codeS", codeSource);
			}
			if (codeTarget!=null) {
				q.setParameter("codeT", codeTarget);
			}
			 
 
			List<Relation> result = q.getResultList();
			
			return result;
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на релации между позиции на кореспондираща таблица!");
			throw new DbErrorException("Грешка при извличане на релации между позиции на кореспондираща таблица!", e);
		}
    	
	}

	
	public void generateRelationsHist(Integer idTable, Integer idVerSource, Integer idVerTarget, boolean oneToOne, boolean oneToZero, boolean zeroToOne) {
		Date dat=new Date();
		if (oneToOne || zeroToOne || oneToZero) {
			
			
			if (oneToOne) {
				
				StringBuffer SQL = new StringBuffer();
				SQL.append("INSERT ");
				SQL.append(" INTO ");
				SQL.append("    RELATION ");
				SQL.append("    ( ");
				SQL.append("        ID, ");
				SQL.append("        ID_TABLE, ");
				SQL.append("        SOURCE_CODE, ");
				SQL.append("        TARGET_CODE, ");
				SQL.append("        DATE_REG,");
				SQL.append("        USER_REG ");
				SQL.append("    ) ");
				SQL.append(" SELECT ");
				SQL.append("    NEXT value FOR SEQ_RELATION , ");
				SQL.append("    :idTable,");
				SQL.append("    s.code, ");
				SQL.append("    s.code, ");
				SQL.append("    :datReg, ");
				SQL.append("    :userReg ");
				SQL.append(" FROM ");
				SQL.append("    POSITION_SCHEME s ");
				SQL.append(" WHERE ");
				SQL.append("    s.VERSION_ID=:idVerSource ");
				SQL.append(" AND s.code IN ");
				SQL.append("    ( ");
				SQL.append("        SELECT ");
				SQL.append("            s1.code ");
				SQL.append("        FROM ");
				SQL.append("            POSITION_SCHEME s1 ");
				SQL.append("        WHERE ");
				SQL.append("            s1.VERSION_ID=:idVerTarget)");
				 
				Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
				//Прави съответствията 
				q.setParameter("idTable", idTable);
				q.setParameter("idVerSource", idVerSource);    		
				q.setParameter("idVerTarget", idVerTarget);    		
				q.setParameter("datReg", dat);    		
				q.setParameter("userReg", getUserId());    		
				q.executeUpdate();
			}
			if (oneToZero) {
				
				StringBuffer SQL = new StringBuffer();
				SQL.append("INSERT ");
				SQL.append(" INTO ");
				SQL.append("    RELATION ");
				SQL.append("    ( ");
				SQL.append("        ID, ");
				SQL.append("        ID_TABLE, ");
				SQL.append("        SOURCE_CODE, ");
				SQL.append("        DATE_REG,");
				SQL.append("        USER_REG ");
				SQL.append("    ) ");
				SQL.append(" SELECT ");
				SQL.append("    NEXT value FOR SEQ_RELATION , ");
				SQL.append("    :idTable,");
				SQL.append("    s.code, ");
				SQL.append("    :datReg, ");
				SQL.append("    :userReg ");
				SQL.append(" FROM ");
				SQL.append("    POSITION_SCHEME s ");
				SQL.append(" WHERE ");
				SQL.append("    s.VERSION_ID=:idVerSource ");
				SQL.append(" AND s.code not IN ");
				SQL.append("    ( ");
				SQL.append("        SELECT ");
				SQL.append("            s1.code ");
				SQL.append("        FROM ");
				SQL.append("            POSITION_SCHEME s1 ");
				SQL.append("        WHERE ");
				SQL.append("            s1.VERSION_ID=:idVerTarget)");
				
				Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
				//Прави съответствията 
				q.setParameter("idTable", idTable);
				q.setParameter("idVerSource", idVerSource);    		
				q.setParameter("idVerTarget", idVerTarget);    		
				q.setParameter("datReg", dat);    		
				q.setParameter("userReg", getUserId());    		
				q.executeUpdate();
			}
			if (zeroToOne) {
				
				StringBuffer SQL = new StringBuffer();
				SQL.append("INSERT ");
				SQL.append(" INTO ");
				SQL.append("    RELATION ");
				SQL.append("    ( ");
				SQL.append("        ID, ");
				SQL.append("        ID_TABLE, ");
				SQL.append("        TARGET_CODE, ");
				SQL.append("        DATE_REG,");
				SQL.append("        USER_REG ");
				SQL.append("    ) ");
				SQL.append(" SELECT ");
				SQL.append("    NEXT value FOR SEQ_RELATION , ");
				SQL.append("    :idTable,");
				SQL.append("    s.code, ");
				SQL.append("    :datReg, ");
				SQL.append("    :userReg ");
				SQL.append(" FROM ");
				SQL.append("    POSITION_SCHEME s ");
				SQL.append(" WHERE ");
				SQL.append("    s.VERSION_ID=:idVerTarget");
				SQL.append(" AND s.code not IN ");
				SQL.append("    ( ");
				SQL.append("        SELECT ");
				SQL.append("            s1.code ");
				SQL.append("        FROM ");
				SQL.append("            POSITION_SCHEME s1 ");
				SQL.append("        WHERE ");
				SQL.append("            s1.VERSION_ID=:idVerSource)");
				
				Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
				//Прави съответствията 
				q.setParameter("idTable", idTable);
				q.setParameter("idVerSource", idVerSource);    		
				q.setParameter("idVerTarget", idVerTarget);    		
				q.setParameter("datReg", dat);    		
				q.setParameter("userReg", getUserId());    		
				q.executeUpdate();
			}
			String identObj="Генериране на релации на историческа таблица: ";
			if (oneToOne) {
				identObj+="едно към едно";
			}
			if (oneToZero) {
				if (oneToOne) {
					if (zeroToOne) {
						identObj+=", ";
					}else {
						identObj+=" и ";
					}
					
				}
				identObj+="едно към нула";
			}
			if (zeroToOne) {
				if (oneToOne || oneToZero) {
					identObj+=" и ";
				}
				identObj+="нула към едно";
			}
			identObj+=".";
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO SYSTEM_JOURNAL (ID, DATE_ACTION, ID_USER, CODE_ACTION, CODE_OBJECT, ID_OBJECT, IDENT_OBJECT)    "
					+ "VALUES( next value for SEQ_SYSTEM_JOURNAL, getdate(), :idUser, 2, "+NSIConstants.CODE_ZNACHENIE_JOURNAL_CORESP_TABLE+", :idTable, :identObj)");
    		q.setParameter("idUser", getUserId());
    		q.setParameter("idTable", idTable);
    		q.setParameter("identObj", identObj);
    		q.executeUpdate();
		}
		
	}

	
	public List<Object[]> loadRelationsForExport(Integer idTable) throws DbErrorException{
		try {
			StringBuffer SQL = new StringBuffer();
			SQL.append("SELECT DISTINCT ");
			SQL.append("    r.id, ");
			SQL.append("    r.SOURCE_CODE, ");
			
			SQL.append("    r.TARGET_CODE, ");
			SQL.append("    (select s.CODE_EXT from SYSTEM_CLASSIF s where s.CODE_CLASSIF=512 and s.code =r.EXPLANATION ) expl ");
			SQL.append(" FROM ");
			SQL.append("    RELATION r ");
			SQL.append(" WHERE ");
			SQL.append("    r.ID_TABLE=:idTable");
			SQL.append("    order by r.id");
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
			q.setParameter("idTable", idTable);
	
			
			
			return q.getResultList();
		} catch (Exception e) {
			throw new DbErrorException(e);
		}
	}
	
	public CorespTable changeCorespTableBroiRelacii(CorespTable ct) {
		StringBuffer SQL = new StringBuffer();
		SQL.append(" SELECT ");
		SQL.append("    COUNT (DISTINCT r.id) id, ");
		SQL.append("    COUNT (DISTINCT r.SOURCE_CODE) sc, ");
		SQL.append("    COUNT (DISTINCT r.TARGET_CODE) tc");
		SQL.append(" FROM ");
		SQL.append("    RELATION r ");
		SQL.append(" WHERE ");
		SQL.append("    r.ID_TABLE=:idTable1");
		
		Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
		q.setParameter("idTable1", ct.getId());
		Object[] rez=(Object[]) q.getSingleResult();
		
		if (rez!=null) {
			if (rez[0]!=null) {
				ct.setRelationsCount(SearchUtils.asInteger(rez[0]));
			}
			if (rez[1]!=null) {
				ct.setSourcePosCount(SearchUtils.asInteger(rez[1]));
			}
			if (rez[2]!=null) {
				ct.setTargetPosCount(SearchUtils.asInteger(rez[2]));
			}
		}
		
		return ct;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> findLastChanges() {
		
		
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("    DISTINCT 'v', ");
		sql.append("    v.ID ");
		sql.append(" FROM ");
		sql.append("    VERSION v ");
		sql.append(" WHERE ");
		sql.append("    v.DATE_LAST_MOD IS NOT NULL ");
		sql.append(" AND v.FINALIZED=1 ");
		sql.append(" AND v.DATE_LAST_MOD> ");
		sql.append("    (   SELECT ");
		sql.append("            so.OPTION_VALUE ");
		sql.append("        FROM ");
		sql.append("            system_options so ");
		sql.append("        WHERE ");
		sql.append("            so.OPTION_LABEL='nsi_last_time_exports_check') ");
		sql.append(" ");
		sql.append(" UNION ");
		sql.append(" ");
		sql.append(" SELECT ");
		sql.append("    DISTINCT 'c', ");
		sql.append("    t.ID ");
		sql.append(" FROM ");
		sql.append("    TABLE_CORRESP t ");
		sql.append(" WHERE ");
		sql.append("    t.FINALIZED=1 ");
		sql.append(" AND t.DATE_LAST_MOD IS NOT NULL ");
		sql.append(" AND t.DATE_LAST_MOD> ");
		sql.append("    (   SELECT ");
		sql.append("            so.OPTION_VALUE ");
		sql.append("        FROM ");
		sql.append("            system_options so ");
		sql.append("        WHERE ");
		sql.append("            so.OPTION_LABEL='nsi_last_time_exports_check')");
				
		Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql.toString());
		 
		
		return q.getResultList();
		
	}
	
	
	
	

}
