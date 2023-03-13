package com.ib.nsiclassif.dao;

public class TestPositionDAO {
	
	
	
//	@Test
//	public void testSaveNew() {
//		
//		PositionDAO dao = new PositionDAO(ActiveUser.DEFAULT);
//		
//		try {
//			
//			Position p = new Position();
//			p.setCode("1");
//			p.setCodeFull("2");
//			p.setCodeSeparate("3");
//			p.setCodeType(4);
//			p.setLevelNumber(5);
//			
//			JPA.getUtil().begin();
//			
//			
//			
//			PositionLang lang = new PositionLang();
//			lang.setLang(1);
//			lang.setPosition(p);
//			
//			lang.setAlsoIncludes("alse");
//			lang.setAlternativeNames("alternative");
//			lang.setComment("comment");
//			lang.setExcludes("excludes");
//			lang.setIncludes("includes");
//			lang.setLongTitle("long");
//			lang.setOffitialTitile("off");
//			lang.setRules("rules");
//			lang.setShortTitle("short");
//			
//			p.getLangMap().put(1, lang);
//			
//			
//			PositionUnits pu = new PositionUnits();
//			pu.setUnit(111);
//			pu.setPosition(p);
//			p.getUnits().add(pu);
//			
//			p = dao.save(p);
//			
//			JPA.getUtil().commit();
//			
//			JPA.getUtil().begin();
//			
//			dao.delete(p);
//			
//			JPA.getUtil().commit();
//			
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
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testSavePosInscheme(){
//		
//		PositionDAO dao = new PositionDAO(ActiveUser.DEFAULT);
//		
//		Integer versionId = 2128; 
//		
//		try {
//			
//			
//			
//			JPA.getUtil().begin();
//			
//			
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION_UNITS where POSITION_ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE  from POSITION_LANG where POSITION_ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION where ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE FROM SCHEME WHERE VERSION_ID = 2128").executeUpdate();
//			
//			
//			Position p = new Position();
//			p.setCode("1");
//			p.setCodeFull("1");
//			p.setCodeSeparate("1");
//			p.setCodeType(1);
//			p.setLevelNumber(1);
//			
//			PositionLang lang = new PositionLang();
//			lang.setLang(1);
//			lang.setPosition(p);
//						
//			lang.setOffitialTitile("Root position");
//			p.getLangMap().put(1, lang);
//			
//			p = dao.saveSchemePosition(p, null, null, versionId);
//			assertNotNull(p.getSchemeItem());
//			
//			
//			Position p10 = new Position();
//			p10.setCode("1.10");
//			p10.setCodeFull("1.10");
//			p10.setCodeSeparate("1.10");
//			p10.setCodeType(1);
//			p10.setLevelNumber(2);
//			
//			PositionLang lang10 = new PositionLang();
//			lang10.setLang(1);
//			lang10.setPosition(p10);						
//			lang10.setOffitialTitile("Дете 10");
//			p10.getLangMap().put(1, lang10);
//			
//			p10 = dao.saveSchemePosition(p10, NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD, p.getSchemeItem().getId(), versionId);
//			assertNotNull(p10.getSchemeItem());
//			
//			
//			Position p5 = new Position();
//			p5.setCode("1.05");
//			p5.setCodeFull("1.05");
//			p5.setCodeSeparate("1.05");
//			p5.setCodeType(1);
//			p5.setLevelNumber(2);
//			
//			PositionLang lang5 = new PositionLang();
//			lang5.setLang(1);
//			lang5.setPosition(p10);						
//			lang5.setOffitialTitile("Дете 5");
//			p5.getLangMap().put(1, lang5);
//			
//			p5 = dao.saveSchemePosition(p5, NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD, p.getSchemeItem().getId(), versionId);
//			assertNotNull(p5.getSchemeItem());
//			
//			Position p3 = new Position();
//			p3.setCode("1.03");
//			p3.setCodeFull("1.03");
//			p3.setCodeSeparate("1.03");
//			p3.setCodeType(1);
//			p3.setLevelNumber(2);
//			
//			PositionLang lang3 = new PositionLang();
//			lang3.setLang(1);
//			lang3.setPosition(p10);						
//			lang3.setOffitialTitile("Дете 3");
//			p3.getLangMap().put(1, lang3);
//			
//			p3 = dao.saveSchemePosition(p3, NSIConstants.CODE_ZNACHENIE_INSERT_BEFORE, p5.getSchemeItem().getId(), versionId);
//			assertNotNull(p3.getSchemeItem());
//			
//			
//			Position p4 = new Position();
//			p4.setCode("1.04");
//			p4.setCodeFull("1.04");
//			p4.setCodeSeparate("1.04");
//			p4.setCodeType(1);
//			p4.setLevelNumber(2);
//			
//			PositionLang lang4 = new PositionLang();
//			lang4.setLang(1);
//			lang4.setPosition(p10);						
//			lang4.setOffitialTitile("Дете 4");
//			p4.getLangMap().put(1, lang4);
//			
//			p4 = dao.saveSchemePosition(p4, NSIConstants.CODE_ZNACHENIE_INSERT_BEFORE, p5.getSchemeItem().getId(), versionId);
//			assertNotNull(p4.getSchemeItem());
//			
//			Position p6 = new Position();
//			p6.setCode("1.06");
//			p6.setCodeFull("1.06");
//			p6.setCodeSeparate("1.06");
//			p6.setCodeType(1);
//			p6.setLevelNumber(2);
//			
//			PositionLang lang6 = new PositionLang();
//			lang6.setLang(1);
//			lang6.setPosition(p10);						
//			lang6.setOffitialTitile("Дете 6");
//			p6.getLangMap().put(1, lang6);
//			
//			p6 = dao.saveSchemePosition(p6, NSIConstants.CODE_ZNACHENIE_INSERT_AFTER, p5.getSchemeItem().getId(), versionId);
//			assertNotNull(p6.getSchemeItem());
//			
//			Position p11 = new Position();
//			p11.setCode("1.11");
//			p11.setCodeFull("1.11");
//			p11.setCodeSeparate("1.11");
//			p11.setCodeType(1);
//			p11.setLevelNumber(2);
//			
//			PositionLang lang11 = new PositionLang();
//			lang11.setLang(1);
//			lang11.setPosition(p10);						
//			lang11.setOffitialTitile("Дете 11");
//			p11.getLangMap().put(1, lang11);
//			
//			p11 = dao.saveSchemePosition(p11, NSIConstants.CODE_ZNACHENIE_INSERT_AFTER, p10.getSchemeItem().getId(), versionId);
//			assertNotNull(p11.getSchemeItem());
//			
//			
//			Position p55 = new Position();
//			p55.setCode("1.05.011");
//			p55.setCodeFull("1.05.011");
//			p55.setCodeSeparate("1.05.011");
//			p55.setCodeType(1);
//			p55.setLevelNumber(3);
//			
//			PositionLang lang55 = new PositionLang();
//			lang55.setLang(1);
//			lang55.setPosition(p10);						
//			lang55.setOffitialTitile("Унуче");
//			p55.getLangMap().put(1, lang55);
//			
//			p55 = dao.saveSchemePosition(p55, NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD, p5.getSchemeItem().getId(), versionId);
//			assertNotNull(p55.getSchemeItem());
//			
//			
//			p55.setCode("1.05.01");
//			p55.setCodeFull("1.05.01");
//			p55.setCodeSeparate("1.05.01");
//			
//			p55 = dao.updateSchemePosition(p55);
//			
//			p55.getLangMap().get(1).setOffitialTitile("Внуче 5.1");
//			
//			p55 = dao.updateSchemePosition(p55);
//			
//			JPA.getUtil().commit();
//			
//			
//			ArrayList<SchemeItem> scheme = (ArrayList<SchemeItem>) JPA.getUtil().getEntityManager().createQuery("from SchemeItem where versionId = :VID").setParameter("VID", versionId).getResultList();
//			System.out.println(scheme.size());
//			
//			SchemeDAO sdao = new SchemeDAO(ActiveUser.DEFAULT);
//			
//			sdao.doSortClassifPrev(scheme);
//			
//			for (SchemeItem tek : scheme) {
//				for (int i = 0; i < tek.getLevelNumber(); i++) {
//					System.out.print("\t");					
//				}
//				System.out.println(tek.getCode() + " " + tek.getDeffName() );				
//			}
//			
//			JPA.getUtil().begin();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION_UNITS where POSITION_ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE  from POSITION_LANG where POSITION_ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION where ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE FROM SCHEME WHERE VERSION_ID = 2128").executeUpdate();
//			JPA.getUtil().commit();
//			
//		} catch (Exception e) {
//			JPA.getUtil().rollback();
//			e.printStackTrace();
//			fail();
//		}finally {
//			JPA.getUtil().closeConnection();
//		}
//		
//	}
//	
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testDeletePosInscheme(){
//		
//		PositionDAO dao = new PositionDAO(ActiveUser.DEFAULT);
//		
//		Integer versionId = 2128; 
//		
//		try {
//			
//			
//			
//			JPA.getUtil().begin();
//			
//			
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION_UNITS where POSITION_ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE  from POSITION_LANG where POSITION_ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION where ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
//			JPA.getUtil().getEntityManager().createNativeQuery("DELETE FROM SCHEME WHERE VERSION_ID = 2128").executeUpdate();
//			
//			
//			Position p = new Position();
//			p.setCode("1");
//			p.setCodeFull("1");
//			p.setCodeSeparate("1");
//			p.setCodeType(1);
//			p.setLevelNumber(1);
//			
//			PositionLang lang = new PositionLang();
//			lang.setLang(1);
//			lang.setPosition(p);
//						
//			lang.setOffitialTitile("Root position");
//			p.getLangMap().put(1, lang);
//			
//			p = dao.saveSchemePosition(p, null, null, versionId);
//			assertNotNull(p.getSchemeItem());
//			
//			
//			Position p10 = new Position();
//			p10.setCode("1.10");
//			p10.setCodeFull("1.10");
//			p10.setCodeSeparate("1.10");
//			p10.setCodeType(1);
//			p10.setLevelNumber(2);
//			
//			PositionLang lang10 = new PositionLang();
//			lang10.setLang(1);
//			lang10.setPosition(p10);						
//			lang10.setOffitialTitile("Дете 10");
//			p10.getLangMap().put(1, lang10);
//			
//			p10 = dao.saveSchemePosition(p10, NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD, p.getSchemeItem().getId(), versionId);
//			assertNotNull(p10.getSchemeItem());
//			
//			dao.deleteSchemePosition(p);
//			
//			
//			
//			
//			JPA.getUtil().commit();
//			
//		} catch (Exception e) {
//			JPA.getUtil().rollback();
//			e.printStackTrace();
//			fail();
//		}finally {
//			JPA.getUtil().closeConnection();
//		}
//		
//	}
	
	
	
}
