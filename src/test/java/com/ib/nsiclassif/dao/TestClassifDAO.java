package com.ib.nsiclassif.dao;

import static org.junit.Assert.fail;

import org.junit.Test;

import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.ClassificationAttributes;
import com.ib.nsiclassif.db.dto.ClassificationLang;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;

public class TestClassifDAO {
	
	
	
	@Test
	public void testSaveNew() {
		
		ClassificationDAO dao = new ClassificationDAO(ActiveUser.DEFAULT);
		
		try {
			JPA.getUtil().begin();
			
			
			Classification c = new Classification();
			c.setClassType(1);
			c.setClassUnit(2);
			c.setFamily(3);
			
			
			
			ClassificationLang lang = new ClassificationLang();
			lang.setLang(1);
			lang.setClassif(c);
			
			lang.setComment("comment");
			lang.setDescription("desc");
			lang.setIdent("ident");
			lang.setNameClassif("name");
			lang.setNews("news");
			
			
			c.getLangMap().put(1, lang);
			
			
			ClassificationAttributes ca = new ClassificationAttributes();
			ca.setCodeAttrib(1);
			ca.setClassif(c);
			
			c.getAttributes().add(ca);
			
			c = dao.save(c);
			
			JPA.getUtil().commit();
			
			JPA.getUtil().begin();
			
			dao.delete(c);
			
			JPA.getUtil().commit();
			
			
			
			
			
		} catch (Exception e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
	}
	
	
	
	
	
}
