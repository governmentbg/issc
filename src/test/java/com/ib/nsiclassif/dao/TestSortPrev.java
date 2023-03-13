package com.ib.nsiclassif.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.UnexpectedResultException;

public class TestSortPrev {

//	public static void main(String[] args) {
//
//		try {
//			Integer versionId = 6458; //1815, 6458
//			System.out.println("1 --------------------------------------");
//			ArrayList<PositionS> scheme = (ArrayList<PositionS>) JPA.getUtil().getEntityManager().createQuery("from PositionS where versionId = :VID").setParameter("VID", versionId).getResultList();
//			System.out.println(scheme.size());
//			System.out.println("2 --------------------------------------");
//			new PositionSDAO(null).doSortSchemePrevNew(scheme);
//			System.out.println("3 --------------------------------------");
//			for (PositionS tek : scheme) {
//				for (int i = 0; i < tek.getLevelNumber(); i++) {
//					System.out.print("\t");					
//				}
//				System.out.println(tek.getCode() + " " + tek.getLangMap().get(1).getOffitialTitile() );				
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("4 --------------------------------------");
//		
//	}
	
	
	
	
	public static void main(String[] args) {

		try {
			Integer versionId = 6458; 
			System.out.println("1 --------------------------------------");
			List<Object[]> result = new PositionSDAO(ActiveUser.DEFAULT).loadPositionsForExport(versionId, new HashMap <Integer,Boolean>(),true);
			System.out.println(result.size());
			//System.out.println("2 --------------------------------------");
			new PositionSDAO(null).doSortSchemePrevAsObj(result, versionId);
			for ( Object[] tek : result) {
				
				System.out.println(tek[3] );				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("3 --------------------------------------");
		
	}
	
	

}
