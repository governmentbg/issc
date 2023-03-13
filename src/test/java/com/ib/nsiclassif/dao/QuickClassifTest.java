package com.ib.nsiclassif.dao;

import java.util.List;

import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.TreeHolder;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class QuickClassifTest {

	public static void main(String[] args) {
		
			
			// Load
			try {
				List<Classification> all = new ClassificationDAO(ActiveUser.DEFAULT).findAllSite(1);
				
				TreeHolder th = all.get(0).toTreeHolder();
				System.out.println(th.getLangMap().size());
				
				th.getLangMap().get(100).getName();
				
				
			} catch (DbErrorException e) {			
				e.printStackTrace();			
			}finally {
				JPA.getUtil().closeConnection();
			}
			
		
	}

}
