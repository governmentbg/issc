package com.ib.nsiclassif.dao;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import com.ib.nsiclassif.db.dao.ObjectUsersDAO;
import com.ib.nsiclassif.db.dto.ObjectUsers;
import com.ib.nsiclassif.db.dto.ObjectUsersLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class TestOUDAO {
	
	
	
	@Test
	public void testSaveNew() {
		
		try {
			
			JPA.getUtil().begin();
			
			ObjectUsers ou = new ObjectUsers();
			ou.setCodeLice(1);
			ou.setCodeObject(2);
			ou.setIdObject(3);
			ou.setRole(4);
			ou.setRoleDate(new Date());
			
			ObjectUsersLang lang = new ObjectUsersLang();
			lang.setLang(1);
			lang.setRoleComment("comment");
			lang.setObjectUsers(ou);
			
			ou.getLangMap().put(1, lang);
			
			
			new ObjectUsersDAO(ActiveUser.DEFAULT).save(ou);
			
			
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
	public void findObjectUsers(){
		
		try {
			//ArrayList<ObjectUsers> list = new ObjectUsersDAO(ActiveUser.DEFAULT).findObjectUsers(91, 96);
			
			
			ArrayList<Object[]> list = new ObjectUsersDAO(ActiveUser.DEFAULT).findObjectUsersNative(NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION, 1, 2);
			
			System.out.println("Намерени: " + list.size());
		} catch (DbErrorException e) {			
			e.printStackTrace();
			fail();
		}
	}
	
	
	
	
	
}
