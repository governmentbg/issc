package com.ib.nsiclassif.search;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.Constants;
import com.ib.nsiclassif.system.SystemData;
import com.ib.system.db.JPA;

public class CorespTableSearch {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CorespTableSearch.class);
	
	private static SystemData sd;
	
	/** @throws Exception */
	@BeforeClass
	public static void setUp() throws Exception {
		JPA.getUtil();
		//sd = new SystemData();
		
	}

	/** */
	@Test
	public void testBuildQueryComp() {
		try {
			CorespTablesSearch search = new CorespTablesSearch();
			search.setLang(2);
			
			//search.setVersionId(10);
			search.setClassificationId(1);

			search.buildQuery();

			Query q = JPA.getUtil().getEntityManager().createNativeQuery(search.getSqlCount());
			Iterator<Entry<String, Object>> it = search.getSqlParameters().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				q.setParameter(entry.getKey(), entry.getValue());
			}
			
			
			Integer count= (Integer) q.getSingleResult();
			System.out.println("Count: " + count);
			
			q = JPA.getUtil().getEntityManager().createNativeQuery(search.getSql());
			it = search.getSqlParameters().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				q.setParameter(entry.getKey(), entry.getValue());
			}
			
			
			@SuppressWarnings("unchecked")
			List<Object[]> result = q.getResultList();
			System.out.println("FoundRows: " + result.size());
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(e.getMessage(), e);
			fail(e.getMessage());
			
		}
	}

}
