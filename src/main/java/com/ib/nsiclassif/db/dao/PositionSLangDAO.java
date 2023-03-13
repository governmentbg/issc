package com.ib.nsiclassif.db.dao;

import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.PositionLang;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;


/**
 *  DAO for {@link PositionLang}
 *  
 * @author s.marinov
 *
 */
public class PositionSLangDAO {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(PositionSLangDAO.class);
	
	

	/** @param user */
	public PositionSLangDAO(ActiveUser user) {
		
	}
	
	@SuppressWarnings("unchecked")
	public List<PositionLang> findByIdPosition(Integer idPos) throws DbErrorException {
		
		try {

			Query query = JPA.getUtil().getEntityManager().createQuery("select a FROM PositionLang as a, PositionS as p  WHERE а.position.id = p.id and p.versionId=:idV ");

			query.setParameter("idV", idPos);

			List<PositionLang> posLangs = query.getResultList();
			for (int i = 0; i < posLangs.size(); i++) {
				System.out.println(posLangs.get(i).getOffitialTitile());
			}
			
			return posLangs;
			
		} catch (Exception e) {
			throw new DbErrorException("Възникна грешка при извличане на езиковите версии на публикация!!!", e);
		}
		
	}	

	 
}





