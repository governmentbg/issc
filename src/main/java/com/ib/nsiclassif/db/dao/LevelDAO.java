package com.ib.nsiclassif.db.dao;

import java.security.InvalidParameterException;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.Level;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectNotFoundException;


/**
 * DAO for {@link Level}
 *
 * @author mamun
 */
public class LevelDAO extends AbstractDAO<Level> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(LevelDAO.class);

	/** @param user */
	public LevelDAO(ActiveUser user) {
		super(user);
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	public List<Level> levelsInVersionList(Integer versionId) throws DbErrorException {
		try {

			Query q = JPA.getUtil().getEntityManager().createQuery("from Level where versionId = :versionId order by level_number");
			q.setParameter("versionId", versionId);

			return (List<Level>) q.getResultList();
			
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на нива във версия", e);
			throw new DbErrorException("Грешка при извличане на нива във версия", e);
		}

	}
	
	public String findMask(Integer verId, Integer levelNumber) throws DbErrorException, ObjectNotFoundException  {
		
		try {
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select mask_real MASK from level where version_id=:id and level_number = :ln");
			q.setParameter("id", verId);
			q.setParameter("ln", levelNumber);
			
			String mask = (String) q.getSingleResult();
			
			
			
			return mask;
		}catch (NoResultException e) {
			throw new ObjectNotFoundException("Няма описано ниво с номер " + levelNumber+ " за тази версия !");
		}  catch (Exception e) {
			LOGGER.error("Грешка при извличане на ниво по ид версия и ниво на нивото", e);
			throw new DbErrorException("Грешка при извличане на ниво по ид версия и ниво на нивото", e);
		}
	}

}
