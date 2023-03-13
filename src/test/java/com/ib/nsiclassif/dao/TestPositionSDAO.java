package com.ib.nsiclassif.dao;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dto.PositionLang;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.PositionUnits;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class TestPositionSDAO {
	
	
	
	@Test
	public void testSaveNew() {
		
		PositionSDAO dao = new PositionSDAO(ActiveUser.DEFAULT);
		
		try {
			
			PositionS p = new PositionS();
			p.setCode("1");
			p.setCodeFull("2");
			p.setCodeSeparate("3");
			p.setCodeType(4);
			p.setLevelNumber(5);
			p.setIdParent(7);
			p.setIdPrev(8);
			p.setIndChild(9);
			
			JPA.getUtil().begin();
			
			
			
			PositionLang lang = new PositionLang();
			lang.setLang(1);
			lang.setPosition(p);
			
			lang.setAlsoIncludes("alse");
			lang.setAlternativeNames("alternative");
			lang.setComment("comment");
			lang.setExcludes("excludes");
			lang.setIncludes("includes");
			lang.setLongTitle("long");
			lang.setOffitialTitile("off");
			lang.setRules("rules");
			lang.setShortTitle("short");
			
			p.getLangMap().put(1, lang);
			
			
			PositionUnits pu = new PositionUnits();
			pu.setUnit(111);
			pu.setPosition(p);
			pu.setTypeUnit(444);
			p.getUnits().add(pu);
			
			p = dao.save(p);
			
			JPA.getUtil().commit();
			
			JPA.getUtil().begin();
			
			dao.delete(p);
			
			JPA.getUtil().commit();
			
			
			
			
			
		} catch (Exception e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSavePosInscheme(){
		
		PositionSDAO dao = new PositionSDAO(ActiveUser.DEFAULT);
		
		Integer versionId = 2128; 
		
		try {
			
			
			
			JPA.getUtil().begin();
			
			
			
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION_UNITS where POSITION_ID in (select ID from POSITION_SCHEME where VERSION_ID = 2128 )").executeUpdate();
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE  from POSITION_LANG where POSITION_ID in (select ID from POSITION_SCHEME where VERSION_ID = 2128 )").executeUpdate();
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION_SCHEME where VERSION_ID = 2128 ").executeUpdate();
			
			JPA.getUtil().commit();
			JPA.getUtil().begin();
			
			
			PositionS p = new PositionS();
			p.setCode("1");
			p.setCodeFull("1");
			p.setCodeSeparate("1");
			p.setCodeType(1);
			p.setLevelNumber(1);
			
			PositionLang lang = new PositionLang();
			lang.setLang(1);
			lang.setPosition(p);
						
			lang.setOffitialTitile("Root position");
			p.getLangMap().put(1, lang);
			
			p = dao.saveSchemePosition(p, null, null, versionId);
			assertNotNull(p.getId());
			
			JPA.getUtil().commit();
			JPA.getUtil().begin();
			
			
			PositionS p10 = new PositionS();
			p10.setCode("1.10");
			p10.setCodeFull("1.10");
			p10.setCodeSeparate("1.10");
			p10.setCodeType(1);
			p10.setLevelNumber(2);
			
			PositionLang lang10 = new PositionLang();
			lang10.setLang(1);
			lang10.setPosition(p10);						
			lang10.setOffitialTitile("Дете 10");
			p10.getLangMap().put(1, lang10);
			
			p10 = dao.saveSchemePosition(p10, NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD, p.getId(), versionId);
			assertNotNull(p10.getId());
			
			
			JPA.getUtil().commit();
			JPA.getUtil().begin();
			
			PositionS p5 = new PositionS();
			p5.setCode("1.05");
			p5.setCodeFull("1.05");
			p5.setCodeSeparate("1.05");
			p5.setCodeType(1);
			p5.setLevelNumber(2);
			
			PositionLang lang5 = new PositionLang();
			lang5.setLang(1);
			lang5.setPosition(p5);						
			lang5.setOffitialTitile("Дете 5");
			p5.getLangMap().put(1, lang5);
			
			p5 = dao.saveSchemePosition(p5, NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD, p.getId(), versionId);
			assertNotNull(p5.getId());
			
			JPA.getUtil().commit();
			JPA.getUtil().begin();
			
			PositionS p3 = new PositionS();
			p3.setCode("1.03");
			p3.setCodeFull("1.03");
			p3.setCodeSeparate("1.03");
			p3.setCodeType(1);
			p3.setLevelNumber(2);
			
			PositionLang lang3 = new PositionLang();
			lang3.setLang(1);
			lang3.setPosition(p3);						
			lang3.setOffitialTitile("Дете 3");
			p3.getLangMap().put(1, lang3);
			
			p3 = dao.saveSchemePosition(p3, NSIConstants.CODE_ZNACHENIE_INSERT_BEFORE, p5.getId(), versionId);
			assertNotNull(p3.getId());
			
			JPA.getUtil().commit();
			JPA.getUtil().begin();
			
			
			PositionS p4 = new PositionS();
			p4.setCode("1.04");
			p4.setCodeFull("1.04");
			p4.setCodeSeparate("1.04");
			p4.setCodeType(1);
			p4.setLevelNumber(2);
			
			PositionLang lang4 = new PositionLang();
			lang4.setLang(1);
			lang4.setPosition(p4);						
			lang4.setOffitialTitile("Дете 4");
			p4.getLangMap().put(1, lang4);
			
			p4 = dao.saveSchemePosition(p4, NSIConstants.CODE_ZNACHENIE_INSERT_BEFORE, p5.getId(), versionId);
			assertNotNull(p4.getId());
			
			JPA.getUtil().commit();
			JPA.getUtil().begin();
			
			PositionS p6 = new PositionS();
			p6.setCode("1.06");
			p6.setCodeFull("1.06");
			p6.setCodeSeparate("1.06");
			p6.setCodeType(1);
			p6.setLevelNumber(2);
			
			PositionLang lang6 = new PositionLang();
			lang6.setLang(1);
			lang6.setPosition(p6);						
			lang6.setOffitialTitile("Дете 6");
			p6.getLangMap().put(1, lang6);
			
			p6 = dao.saveSchemePosition(p6, NSIConstants.CODE_ZNACHENIE_INSERT_AFTER, p5.getId(), versionId);
			assertNotNull(p6.getId());
			
			PositionS p11 = new PositionS();
			p11.setCode("1.11");
			p11.setCodeFull("1.11");
			p11.setCodeSeparate("1.11");
			p11.setCodeType(1);
			p11.setLevelNumber(2);
			
			PositionLang lang11 = new PositionLang();
			lang11.setLang(1);
			lang11.setPosition(p11);						
			lang11.setOffitialTitile("Дете 11");
			p11.getLangMap().put(1, lang11);
			
			p11 = dao.saveSchemePosition(p11, NSIConstants.CODE_ZNACHENIE_INSERT_AFTER, p10.getId(), versionId);
			assertNotNull(p11.getId());
			
			JPA.getUtil().commit();
			JPA.getUtil().begin();
			
			
			PositionS p55 = new PositionS();
			p55.setCode("1.05.011");
			p55.setCodeFull("1.05.011");
			p55.setCodeSeparate("1.05.011");
			p55.setCodeType(1);
			p55.setLevelNumber(3);
			
			PositionLang lang55 = new PositionLang();
			lang55.setLang(1);
			lang55.setPosition(p55);						
			lang55.setOffitialTitile("Унуче");
			p55.getLangMap().put(1, lang55);
			
			p55 = dao.saveSchemePosition(p55, NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD, p5.getId(), versionId);
			assertNotNull(p55.getId());
			
			JPA.getUtil().commit();
			JPA.getUtil().begin();
			
			p55.setCode("1.05.01");
			p55.setCodeFull("1.05.01");
			p55.setCodeSeparate("1.05.01");
			
			p55 = dao.save(p55);
			
			p55.getLangMap().get(1).setOffitialTitile("Внуче 5.1");
			
			p55 = dao.save(p55);
			
			JPA.getUtil().commit();
			
			
			ArrayList<PositionS> scheme = (ArrayList<PositionS>) JPA.getUtil().getEntityManager().createQuery("from PositionS where versionId = :VID").setParameter("VID", versionId).getResultList();
			System.out.println(scheme.size());
			
			dao.doSortSchemePrev(scheme);
			
			for (PositionS tek : scheme) {
				for (int i = 0; i < tek.getLevelNumber(); i++) {
					System.out.print("\t");					
				}
				System.out.println(tek.getCode() + " " + tek.getLangMap().get(1).getOffitialTitile() );				
			}
			
			
			System.out.println("---------------------------------- Load Start --------------------------------------");
			List<PositionS> schemeNew = dao.loadScheme(versionId, null, 1,1,100) ;
			System.out.println("---------------------------------- Load End --------------------------------------");
			List<Object[]> r = dao.decodePositionByVersionCode(versionId, "1.05.01", 1);
			System.out.println("****************************" + r.get(0)[1]);
			
			JPA.getUtil().begin();
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION_UNITS where POSITION_ID in (select ID from POSITION_SCHEME where VERSION_ID = 2128 )").executeUpdate();
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE  from POSITION_LANG where POSITION_ID in (select ID from POSITION_SCHEME where VERSION_ID = 2128 )").executeUpdate();
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION_SCHEME where VERSION_ID = 2128 ").executeUpdate();
			
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
	public void testDeletePosInscheme(){
		
		PositionSDAO dao = new PositionSDAO(ActiveUser.DEFAULT);
		
		Integer versionId = 2128; 
		
		try {
			
			
			
			JPA.getUtil().begin();
			
			
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION_UNITS where POSITION_ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE  from POSITION_LANG where POSITION_ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE from POSITION where ID in (select POSITION_ID from scheme where VERSION_ID = 2128 )").executeUpdate();
			JPA.getUtil().getEntityManager().createNativeQuery("DELETE FROM SCHEME WHERE VERSION_ID = 2128").executeUpdate();
			
			
			PositionS p = new PositionS();
			p.setCode("1");
			p.setCodeFull("1");
			p.setCodeSeparate("1");
			p.setCodeType(1);
			p.setLevelNumber(1);
			
			PositionLang lang = new PositionLang();
			lang.setLang(1);
			lang.setPosition(p);
						
			lang.setOffitialTitile("Root position");
			p.getLangMap().put(1, lang);
			
			p = dao.saveSchemePosition(p, null, null, versionId);
			assertNotNull(p.getId());
			
			
			PositionS p10 = new PositionS();
			p10.setCode("1.10");
			p10.setCodeFull("1.10");
			p10.setCodeSeparate("1.10");
			p10.setCodeType(1);
			p10.setLevelNumber(2);
			
			PositionLang lang10 = new PositionLang();
			lang10.setLang(1);
			lang10.setPosition(p10);						
			lang10.setOffitialTitile("Дете 10");
			p10.getLangMap().put(1, lang10);
			
			p10 = dao.saveSchemePosition(p10, NSIConstants.CODE_ZNACHENIE_INSERT_AS_CHILD, p.getId(), versionId);
			assertNotNull(p10.getId());
			
			dao.delete(p);
			
			
			
			
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
	public void testLoadScheme() {
		
		try {
			PositionSDAO dao = new PositionSDAO(ActiveUser.DEFAULT);
			
			//List<SchemeItem> items = dao.findItemChildren(1, 892487);
			List<PositionS> items = dao.loadScheme(1, null, 1,1,100);
			
			for (PositionS item : items) {
				for (int i = 0; i < item.getLevelNumber(); i++) {
					System.out.print("\t");
				}
				System.out.println(item.getCode() + "\t" + item.getName());
			}
			
			
			
			System.out.println(items.size());
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
	}
	
	
}
