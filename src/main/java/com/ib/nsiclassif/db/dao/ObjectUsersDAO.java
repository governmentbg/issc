package com.ib.nsiclassif.db.dao;

import java.util.ArrayList;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.ObjectUsers;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

/**
 * DAO for {@link ObjectUsers}
 *
 * @author mamun
 */
public class ObjectUsersDAO extends AbstractDAO<ObjectUsers> {
	
	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectUsersDAO.class);

	/** @param user */
	public ObjectUsersDAO(ActiveUser user) {
		super(user);
		// TODO Auto-generated constructor stub
	}
	
	
	
	public ArrayList<ObjectUsers> findObjectUsersForCopy(Integer codeObject, Integer idObject) throws DbErrorException{
		
		try {
			Query q = JPA.getUtil().getEntityManager().createQuery("from ObjectUsers where codeObject = :CO and idObject = :IO");
			q.setParameter("CO", codeObject);
			q.setParameter("IO", idObject);
			
			return (ArrayList<ObjectUsers>) q.getResultList();
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане на потребители към обект", e);
			throw new DbErrorException("Грешка при извличане на потребители към обект", e);
		}
		
		
	}
	
	
	/** Метод за връщане на потребителите (демек служителите) към обект на даден език
	 * @param codeObject - Код на обект
	 * @param idObject - Системен идентификатор на обект
	 * @param lang - език
	 * @return Списък потребители
	 * @throws DbErrorException - грешка при работа с БД
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Object[]> findObjectUsersNative(Integer codeObject, Integer idObject, Integer lang) throws DbErrorException{
		
		try {
			
			String sql = "SELECT OBJECT_USERS.ID, OBJECT_USERS.CODE_OBJECT, OBJECT_USERS.ID_OBJECT, OBJECT_USERS.CODE_LICE, OBJECT_USERS.ROLE, OBJECT_USERS.ROLE_DATE, OBJECT_USERS.DATE_REG, OBJECT_USERS.USER_REG, OBJECT_USERS.DATE_LAST_MOD, OBJECT_USERS.USER_LAST_MOD, SUBSTRING (ISNULL(lang1.ROLE_COMMENT, deff.ROLE_COMMENT+' *'),1,100) COMMENT FROM OBJECT_USERS " +
					 			" left outer join  OBJECT_USERS_LANG lang1 on OBJECT_USERS.id = lang1.OBJECT_USERS_ID and lang1.lang = :LANG " + 
					 			" left outer join  OBJECT_USERS_LANG deff on OBJECT_USERS.id = deff.OBJECT_USERS_ID and deff.lang = :DEFLANG " + 
					 		" where OBJECT_USERS.CODE_OBJECT = :CO and OBJECT_USERS.ID_OBJECT = :IO";
			
			
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
			LOGGER.debug("Грешка при извличане на потребители към обект", e);
			throw new DbErrorException("Грешка при извличане на потребители към обект", e);
		}
		
		
	}

}
