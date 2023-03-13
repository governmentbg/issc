package com.ib.nsiclassif.dao;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import com.ib.nsiclassif.db.dao.ObjectDocsDAO;
import com.ib.nsiclassif.db.dto.ObjectDocs;
import com.ib.nsiclassif.db.dto.ObjectDocsLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class TestObjectDocsDAO {
	
	
	
	@Test
	public void testSaveNew() {
		
		try {
			
			JPA.getUtil().begin();
			
			ObjectDocs doc = new ObjectDocs();
			
			doc.setCodeObject(2);
			doc.setIdObject(3);
			doc.setRnDoc("111-11-1");
			doc.setDatDoc(new Date());
			doc.setType(44);
			doc.setPubl(1);
			
			
			ObjectDocsLang lang = new ObjectDocsLang();
			lang.setLang(1);
			lang.setAnot("anot");
			lang.setDescription("dopInfo");
			lang.setObjectDocs(doc);
			
			doc.getLangMap().put(1, lang);
			
			
			new ObjectDocsDAO(ActiveUser.DEFAULT).save(doc);
			
			
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
	public void findObjectDocs(){
		
		try {
			ArrayList<Object[]> list = new ObjectDocsDAO(ActiveUser.DEFAULT).findObjectDocsNative(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION, 1, 2);
			System.out.println("Намерени: " + list.size());
		} catch (DbErrorException e) {			
			e.printStackTrace();
			fail();
		}
	}
	
	
	
	
	
}
