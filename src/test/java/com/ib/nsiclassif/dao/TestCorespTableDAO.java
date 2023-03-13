package com.ib.nsiclassif.dao;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.CorespTableLang;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class TestCorespTableDAO {
	
	
	
	@Test
	public void testSaveNew() {
		
		CorespTableDAO dao = new CorespTableDAO(ActiveUser.DEFAULT);
		
		try {
			
			CorespTable ct = new CorespTable();
			
			ct.setIdVersSource(1);
			ct.setIdVersTarget(2);
			ct.setPath("3");
			ct.setRelationsCount(4);
			ct.setRelationType(5);
			ct.setSourcePosCount(6);
			ct.setStatus(7);
			ct.setTableType(8);
			ct.setTargetPosCount(9);
						
			
			
			
			
			
			JPA.getUtil().begin();
			
			
			
			CorespTableLang lang = new CorespTableLang();
			lang.setLang(1);
			lang.setCorespTable(ct);
			
			lang.setIdent("ident");
			lang.setName("name");
			lang.setRegion("region");
			lang.setComment("comment");
			
			
			ct.getLangMap().put(1, lang);
			
			
			
			
			ct = dao.save(ct);
			
			JPA.getUtil().commit();
			
			JPA.getUtil().begin();
			
			dao.delete(ct);
			
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
	public void testCopyRelations() {
		
		try {
			
			JPA.getUtil().begin();
			new CorespTableDAO(ActiveUser.DEFAULT).copyTableRelations(829, 222);
			
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
