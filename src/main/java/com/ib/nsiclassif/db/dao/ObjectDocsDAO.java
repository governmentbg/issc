package com.ib.nsiclassif.db.dao;

import java.util.ArrayList;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.ObjectDocs;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

/**
 * DAO for {@link ObjectDocs}
 *
 * @author mamun
 */
public class ObjectDocsDAO extends AbstractDAO<ObjectDocs> {
	
	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectDocsDAO.class);

	/** @param user */
	public ObjectDocsDAO(ActiveUser user) {
		super(user);
		// TODO Auto-generated constructor stub
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<ObjectDocs> findObjectDocsForCopy(Integer codeObject, Integer idObject) throws DbErrorException{
		
		try {
			Query q = JPA.getUtil().getEntityManager().createQuery("from ObjectDocs where codeObject = :CO and idObject = :IO");
			q.setParameter("CO", codeObject);
			q.setParameter("IO", idObject);
			
			return (ArrayList<ObjectDocs>) q.getResultList();
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на документи към обект", e);
			throw new DbErrorException("Грешка при извличане на документи към обект", e);
		}
		
		
	}
	
	
	/** Метод за връщане на документите към обект на даден език
	 * @param codeObject - Код на обект
	 * @param idObject - Системен идентификатор на обект
	 * @param lang - език
	 * @return Списък документи
	 * @throws DbErrorException - грешка при работа с БД
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Object[]> findObjectDocsNative(Integer codeObject, Integer idObject, Integer lang) throws DbErrorException{
		
		try {
			
			String sql = "SELECT OBJECT_DOCS.ID, OBJECT_DOCS.RN_DOC, OBJECT_DOCS.DAT_DOC, OBJECT_DOCS.TYPE, OBJECT_DOCS.PUBL, SUBSTRING (ISNULL(lang1.ANOT, deff.ANOT),1,100) ANOT FROM OBJECT_DOCS "
					+ "    left outer join  OBJECT_DOCS_LANG lang1 on OBJECT_DOCS.id = lang1.OBJECT_DOCS_ID and lang1.lang = :LANG  "
					+ "    left outer join  OBJECT_DOCS_LANG deff  on OBJECT_DOCS.id = deff.OBJECT_DOCS_ID and deff.lang = :DEFLANG "
					+ "					 		where OBJECT_DOCS.CODE_OBJECT = :CO and OBJECT_DOCS.ID_OBJECT = :IO";
			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter("CO", codeObject);
			q.setParameter("IO", idObject);
			
			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
			if (lang == null) {
				q.setParameter("LANG", NSIConstants.CODE_DEFAULT_LANG);
			}else {
				q.setParameter("LANG", lang);
			}
			
			
			return (ArrayList<Object[]>) q.getResultList();
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на документи към обект", e);
			throw new DbErrorException("Грешка при извличане на документи към обект", e);
		}
		
		
	}
	

}
