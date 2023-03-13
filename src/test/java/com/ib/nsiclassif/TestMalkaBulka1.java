package com.ib.nsiclassif;

import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.Constants;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class TestMalkaBulka1 {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMalkaBulka1.class);

	public static void main(String[] args) {
		
		try {
			List<Classification> result = findAllSite();
			System.out.println(result.size());
			
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public  static List<Classification> findAllSite() throws DbErrorException {
		LOGGER.debug("Търсят се всички обекти от тип:{}", "Classification");
		try {
			Query q = JPA.getUtil().getEntityManager().createQuery("select c from Classification c join fetch ClassificationLang cl on cl.classif.id = c.id where c.finalized = :FIN order by cl.ident ASC");
			q.setParameter("FIN", Constants.CODE_ZNACHENIE_DA);
			
			return q.getResultList();
			
			 
			
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане класификации сайт", e);
			throw new DbErrorException(e);
		}
	}

}
