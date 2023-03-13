package com.ib.nsiclassif.dao;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class TestVersionDAO {
	
	
	
	@Test
	public void testSaveNew() {
		
		try {
			
			ArrayList<Object[]> result = new VersionDAO(ActiveUser.DEFAULT).getClassifVersions(96, 1);
			System.out.println(result.size());
			
		} catch (DbErrorException e) {
			
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
	}
	
	@Test
	public void testCopyVersion() {
		
		try {
			
			JPA.getUtil().begin();
			//new VersionDAO(ActiveUser.DEFAULT).copyVersionScheme(1, 2128, false);
			//new VersionDAO(ActiveUser.DEFAULT).copyVersion(1194);
			JPA.getUtil().commit();
			
			
		} catch (DbErrorException e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
	}
	
	
	@Test
	public void testdecodeVersionIdent() {
		
		try {
			
			JPA.getUtil().begin();
			
			System.out.println(new VersionDAO(ActiveUser.DEFAULT).decodeVersionIdent(10, 1));
			
			JPA.getUtil().rollback();
			
			
		} catch (DbErrorException e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
	}
	
	
	@Test
	public void testdecodeAllVersionList() {
		
		try {
			
			JPA.getUtil().begin();
			
			ArrayList<Object[]> all = new VersionDAO(ActiveUser.DEFAULT).getAllVesrionsList(2);
			System.out.println(all.size());
			JPA.getUtil().rollback();
			
			
		} catch (DbErrorException e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
	}
	
	
}
