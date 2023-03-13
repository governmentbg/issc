package com.ib.nsiclassif.dao;

import static org.junit.Assert.fail;

import javax.faces.application.FacesMessage;

import org.junit.Test;

import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.ClassificationAttributes;
import com.ib.nsiclassif.db.dto.ClassificationLang;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class TestClassDAO {
	
	private Classification classification = null;
	private ClassificationDAO classDAO = new ClassificationDAO(ActiveUser.DEFAULT);
	
	@Test
	public void testSaveNew() {
		
		try {
			ClassificationDAO dao = new ClassificationDAO(ActiveUser.DEFAULT);
			
			JPA.getUtil().begin();
			
			Classification classif = new Classification();
			
			classif.setClassType(1);
			classif.setClassUnit(2);
			classif.setFamily(3);
			
			ClassificationLang lang1 = new  ClassificationLang();
			lang1.setLang(1);
			lang1.setIdent("CENS_DISABILITY");
			lang1.setArea("Област");
			lang1.setComment("Коментар");
			lang1.setDescription("Описание");
			lang1.setNews("Новини");
			lang1.setClassif(classif);
			classif.getLangMap().put(1, lang1);
			
			ClassificationLang lang2 = new  ClassificationLang();
			lang2.setIdent("CENS_DISABILITY");
			lang2.setLang(2);
			lang2.setArea("Area");
			lang2.setComment("Comment");
			lang2.setDescription("Description");
			lang2.setNews("News");
			lang2.setClassif(classif);
			classif.getLangMap().put(2, lang2);
			
			ClassificationAttributes att1 = new ClassificationAttributes();
			att1.setCodeAttrib(1);
			att1.setClassif(classif);
			
			ClassificationAttributes att2 = new ClassificationAttributes();
			att2.setCodeAttrib(2);
			att2.setClassif(classif);
			
			classif.getAttributes().add(att1);
			classif.getAttributes().add(att2);
			
			
			dao.save(classif);
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
	public void testAddLang() {
		
		// Load
		try {
			classification = classDAO.findById(74);
		} catch (DbErrorException e) {			
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
		//Add
		ClassificationLang classifLang = new ClassificationLang();
		classifLang.setClassif(classification);
		classifLang.setLang(11);
		classifLang.setComment("Comment алабала");
		
		
		
		//Save
		try {
			
			classification.getLangMap().put(classifLang.getLang(), classifLang);
			
			JPA.getUtil().runInTransaction(() -> classDAO.save(classification));

			

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
		
		
	}
	
	
	
	@Test
	public void testSite() {
		
		// Load
		try {
			classDAO.findAllSite(1);
		} catch (DbErrorException e) {			
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
	}
	
	
	
}
