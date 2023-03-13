package com.ib.nsiclassif.activeDir;

import static org.junit.Assert.fail;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.junit.Test;

import com.ib.nsiclassif.db.dao.AdmUserDAO;
import com.ib.nsiclassif.db.dto.AdmUser;
import com.ib.nsiclassif.ldap.ActiveDirectory;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.exceptions.DbErrorException;

public class ActiveDirTest {
	
 
	
	@Test
	public void findUser() throws NamingException {
		String username = "testuser";
		String password = "Alab@la";
		ActiveDirectory ac=new ActiveDirectory();
		
		DirContext context=null;
		
		try {
			context=ac.createContext(username, password);
		} catch (DbErrorException e) {
			e.printStackTrace();
			fail();
		}
		
		if (context == null) {     // Въведеният потребител фигурира в Ldap
			fail();
		} else {
		
		
			AdmUser user1;
			try {
				user1 = ac.searchUserInAktDir(context,username,password,"sAMAccountName");
				if (user1==null) {
					fail();
				}
			} catch (DbErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				context.close();
			}
		}
		
	}
}

