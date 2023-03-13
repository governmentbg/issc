package com.ib.nsiclassif.dao;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.ib.nsiclassif.db.dao.RelationDAO;
import com.ib.nsiclassif.db.dto.Relation;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class TestRelationDAO {
	
	
	
	@Test
	public void testSaveNew() {
		
		RelationDAO dao = new RelationDAO(ActiveUser.DEFAULT);
		
		try {
			
			Relation r = new Relation();
			
			//r.setChangeType(1);
			r.setIdTable(2);
			r.setSourceCode("3");
			r.setTargetCode("4");
			//r.setRelationType(5);
			//r.setWeigthSource(6.0);
			//r.setWeigthTarget(7.0);			
			
			
			
			
			
			JPA.getUtil().begin();
			
			
			
//			RelationLang lang = new RelationLang();
//			lang.setLang(1);
//			lang.setRelation(r);
//			
//			
//			lang.setComment("comment");
//			
//			
//			r.getLangMap().put(1, lang);
//			
			
			
			
			r = dao.save(r);
			
			JPA.getUtil().commit();
			
			JPA.getUtil().begin();
			
			//dao.delete(r);
			
			JPA.getUtil().commit();
			
			
			
			
			
		} catch (Exception e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
	}
	
	@Test
	public void testGenerateRelations() {
		
		try {
			
			JPA.getUtil().begin();
			new RelationDAO(ActiveUser.DEFAULT).generateRelationsHist(1365, 2184, 2196, true, true, true);
			
			JPA.getUtil().commit();
			
			
		} catch (DbErrorException e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
	}
	
	
	
	
	
}
