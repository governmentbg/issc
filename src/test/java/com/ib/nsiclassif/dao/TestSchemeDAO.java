package com.ib.nsiclassif.dao;

public class TestSchemeDAO {
	
	
//	
//	@Test
//	public void testSaveNew() {
//		
//		try {
//			SchemeDAO dao = new SchemeDAO(ActiveUser.DEFAULT);
//			
//			SchemeItem item = new SchemeItem();
//			item.setCode("1");
//			item.setDateReg(new Date());
//			item.setDeffName("2");			
//			item.setIdParent(4);
//			item.setIdPrev(5);
//			item.setLevelNumber(6);
//			item.setPositionId(7);
//			item.setVersionId(8);
//			item.setUserReg(9);
//			
//			JPA.getUtil().begin();
//			
//			item = dao.save(item);
//			
//			JPA.getUtil().commit();
//			JPA.getUtil().begin();
//			
//			dao.delete(item);
//			
//			JPA.getUtil().commit();
//			
//			
//			
//			
//		} catch (Exception e) {
//			JPA.getUtil().rollback();
//			e.printStackTrace();
//			fail();
//		}finally {
//			JPA.getUtil().closeConnection();
//		}
//		
//		
//	}
//	
//	
//	@Test
//	public void testBuildTree() {
//		
//		Integer versionId = 2128;
//		
//	
//		
//		try {
//			
//			ArrayList<SchemeItem> scheme = (ArrayList<SchemeItem>) JPA.getUtil().getEntityManager().createQuery("from SchemeItem where versionId = :VID").setParameter("VID", versionId).getResultList();
//			System.out.println(scheme.size());
//			
//			SchemeDAO dao = new SchemeDAO(ActiveUser.DEFAULT);
//			
//			dao.doSortClassifPrev(scheme);
//			
//			for (SchemeItem tek : scheme) {
//				for (int i = 0; i < tek.getLevelNumber(); i++) {
//					System.out.print("\t");					
//				}
//				System.out.println(tek.getCode() + " " + tek.getDeffName() + "\t-\t" + tek.getLevelNumber());				
//			}
//			
//		} catch (Exception e) {			
//			e.printStackTrace();
//			fail();
//		}finally {
//			JPA.getUtil().closeConnection();
//		}
//		
//	}
//	
//	
//	@Test
//	public void testGetChildren() {
//		
//		try {
//			SchemeDAO dao = new SchemeDAO(ActiveUser.DEFAULT);
//			
//			List<SchemeItem> items = dao.findItemChildren(1, 892487);
//			
//			for (SchemeItem item : items) {
//				System.out.println(item.getCode() + "\t" + item.getDeffName());
//			}
//			
//			
//			
//			System.out.println(items.size());
//		} catch (DbErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			fail();
//		}
//		
//	}
//	
//	
//	@Test
//	public void testLoadScheme() {
//		
//		try {
//			SchemeDAO dao = new SchemeDAO(ActiveUser.DEFAULT);
//			
//			//List<SchemeItem> items = dao.findItemChildren(1, 892487);
//			List<SchemeItem> items = dao.loadScheme(1, null, 1);
//			
//			for (SchemeItem item : items) {
//				System.out.println(item.getCode() + "\t" + item.getDeffName());
//			}
//			
//			
//			
//			System.out.println(items.size());
//		} catch (DbErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			fail();
//		}
//		
//	}
//	
//	
	
}
