package com.ib.nsiclassif.system;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.SystemClassifDAO;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.UnexpectedResultException;
import com.ib.system.utils.Multilang;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.SysClassifUtils;
import javax.persistence.Query;

public class TestMigrate {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMigrate.class);
	
	private static Integer maxCod = 200000;
	
	private static HashMap <Integer, Integer> codeObjectMap = new HashMap <Integer, Integer>();
	
	
	
	public static void main(String[] args) {
		
		
		try {
			
			
			System.out.println("start");
			
			
			JPA.getUtil().begin();
			
			
//			doSysClassif(29);
		SystemData sd = new SystemData();
		System.out.println(sd.decodeItem(129, 14, 1, new Date())); 
//			
			
			
			
			JPA.getUtil().commit();
			
			
			System.out.println("end");
			
		} catch (Exception e) {
			JPA.getUtil().rollback();			
			e.printStackTrace();			
		}finally {
			JPA.getUtil().closeConnection();
			JPA.getUtil("old").closeConnection();
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static void doSysClassif(Integer codeC) throws ParseException, DbErrorException, UnexpectedResultException {
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		Date dat = sdf.parse("01.01.1900");
		System.out.println("dat ---->>>>>>>>>>>>>>>>>>>>" + dat);
		
		SystemClassifDAO dao = new SystemClassifDAO(ActiveUser.DEFAULT);
		
		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id, seq_number, classification, code, text, lang, level_number, parrent_id, ind_child, end_level from system_classif where classification = "+codeC+" and lang = 1 order by level_number, parrent_id, seq_number");
		List<Object[]> result = q.getResultList();
		
		int tekParent = 0;
		int tekPrev = 0;
		ArrayList<SystemClassif> classif = new ArrayList<SystemClassif>();
		for (Object[] row : result) {
			
			
			//int i = NSIConstants.CODE_CLASSIF_COUNTRIES;
			
			Integer codeClassif = SearchUtils.asInteger(row[2]);
			Integer code = SearchUtils.asInteger(row[3]);
			String tekst = SearchUtils.asString(row[4]);
			Integer lang = SearchUtils.asInteger(row[5]);
			Integer level = SearchUtils.asInteger(row[6]);
			Integer parentId = SearchUtils.asInteger(row[7]);
			
			if (tekParent != parentId) {
				tekPrev = 0;
			}
			
			SystemClassif item = new SystemClassif();
			item.setCode(code);
			item.setCodeClassif(100+codeClassif);
			item.setCodeParent(parentId);
			item.setCodePrev(tekPrev);
			item.setDateOt(dat);
			item.setLevelNumber(level);
			item.setTekst(tekst);
			
			System.out.println("Adding " + code + "\t" + tekst);
			
			
			Multilang l = new Multilang();
			l.setLang(lang);
			l.setTekst(tekst);
			item.getTranslations().add(l);
			
			Multilang l2 = new Multilang();
			l2.setLang(2);
			l2.setTekst(tekst+"EN");
			item.getTranslations().add(l2);
			
			classif.add(item);
			tekPrev = code;
			tekParent = parentId;
			System.out.println("save " + tekst);
			dao.doSimpleSave(item);
			
		}
		
		SysClassifUtils.doSortClassifPrev(classif);
		
		for (SystemClassif tek : classif) {
			for (int i = 0; i < tek.getLevelNumber(); i++) {
				System.out.print("\t");					
			}
			System.out.println(tek.getTekst());
		}
	}
//	
//	
//	
//	
//	@SuppressWarnings("unchecked")
//	public static void doClassification(Integer id) throws DbErrorException {
//		
//		//ClassificationDAO dao = new ClassificationDAO(ActiveUser.DEFAULT);
//		
//		
//		
//		
//		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("SELECT id, class_type, class_unit, family, date_created, user_created, date_last_modified, user_last_modified FROM classification where id = :IDC");
//		q.setParameter("IDC", id);
//		
//		Object[]  result = (Object[]) q.getSingleResult();
//		
//		
//		
//		Classification c = new Classification();
//		c.setId(id);
//		c.setClassType(SearchUtils.asInteger(result[1]));
//		c.setClassUnit(SearchUtils.asInteger(result[2]));
//		c.setFamily(SearchUtils.asInteger(result[3]));
//		c.setUserReg(ActiveUser.DEFAULT.getUserId());
//		c.setDateReg(new Date());
//		
//		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select code_attribute, code_lang, text_attribute from attribute_languages where code_object = :CO and id_object = :IDC");
//		q.setParameter("IDC", id);
//		q.setParameter("CO", 2);
//		ArrayList<Object[]> langs = (ArrayList<Object[]>) q.getResultList();
//		for (Object[] lang : langs) {
//			Integer codeAttr = SearchUtils.asInteger(lang[0]);
//			Integer codeLang = SearchUtils.asInteger(lang[1]);
//			String text = SearchUtils.clobToString((Clob)lang[2]);
//			
//			ClassificationLang lo = c.getLangMap().get(codeLang);
//			if (lo == null) {
//				lo = new ClassificationLang();
//				lo.setClassif(c);
//				lo.setLang(codeLang);
//			}
//						
//			switch(codeAttr) {
//			  case 1: //CODE_ATTRIB_IDENT
//				lo.setIdent(text);
//			    break;
//			  case 2: //CODE_ATTRIB_NAME
//			    lo.setNameClassif(text);
//			    break;
//			  case 3: //CODE_ATTRIB_DESCRIPTION
//			   lo.setDescription(text);
//			    break;  
//			  case 4: //CODE_ATTRIB_COMMENT
//			    lo.setComment(text);
//			    break;
//				    
//			  case 7: //CODE_ATTRIB_NEWS
//			    lo.setNews(text);
//			    break;
//			  case 8: //CODE_ATTRIB_AREAS
//			    lo.setArea(text);
//			    break;
//			 
//			}
//			
//			c.getLangMap().put(codeLang, lo);
//		}
//		
//		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select code_attribute from class_attrib where class_id = :IDC");
//		q.setParameter("IDC", id);			
//		ArrayList<Object> attribs = (ArrayList<Object>) q.getResultList();
//		for (Object tek : attribs) {
//			ClassificationAttributes att = new ClassificationAttributes();
//			att.setCodeAttrib(SearchUtils.asInteger(tek));	
//			c.getAttributes().add(att);
//		}
//		
//		
//		
//		//dao.save(c);
//		insertClassification(c);
////		
//		doObjectUsers(2, id);
//		doObjectDocs(2, id);
////		
////		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id from version where id_class = :IDC");
////		q.setParameter("IDC", id);
////		
////			
////		ArrayList<Object> verIds = (ArrayList<Object>) q.getResultList();
////		for (Object o : verIds) {
////			doVersion(SearchUtils.asInteger(o));
////		}
////		
////		bulkInsertPositions(id);
////		bulkInsertRelations(id);
//		
//		
//		
//	}
//	
//	@SuppressWarnings("unchecked")
//	public static void doCorespTable(Integer idTable) throws DbErrorException {
//		try {
//			
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select count(id) from TABLE_CORRESP where id = :IDT");
//			q.setParameter("IDT", idTable);
//			
//			Integer cnt = SearchUtils.asInteger(q.getSingleResult());
//			if (cnt != null && cnt > 0) {
//				return; //Таблицата вече е създадена от другата версия
//			}
//			
//			q = JPA.getUtil("old").getEntityManager().createNativeQuery("SELECT id, id_vers_source, id_vers_target, status, table_type, relations_count, source_pos_count, target_pos_count, relation_type, path  FROM table_corresp where id = :IDT");
//			q.setParameter("IDT", idTable);
//			
//			Object[] row = (Object[]) q.getSingleResult(); 
//			
//			CorespTable t = new CorespTable();
//			t.setId(SearchUtils.asInteger(row[0]));
//			t.setIdVersSource(SearchUtils.asInteger(row[1]));
//			t.setIdVersTarget(SearchUtils.asInteger(row[2]));
//			t.setStatus(SearchUtils.asInteger(row[3]));
//			t.setTableType(SearchUtils.asInteger(row[4]));
//			t.setRelationsCount(SearchUtils.asInteger(row[5]));
//			t.setSourcePosCount(SearchUtils.asInteger(row[6]));
//			t.setTargetPosCount(SearchUtils.asInteger(row[7]));
//			t.setRelationType(SearchUtils.asInteger(row[8]));
//			t.setPath(SearchUtils.asString(row[9]));
//			t.setDateReg(new Date());
//			t.setUserReg(ActiveUser.DEFAULT.getUserId());
//			
//			q = JPA.getUtil("old").getEntityManager().createNativeQuery("select code_attribute, code_lang, text_attribute from attribute_languages where code_object = :CO and id_object = :IDO");
//			q.setParameter("IDO", idTable);
//			q.setParameter("CO", 12);
//			
//			ArrayList<Object[]> langs = (ArrayList<Object[]>) q.getResultList();
//			for (Object[] lang : langs) {
//				Integer codeAttr = SearchUtils.asInteger(lang[0]);
//				Integer codeLang = SearchUtils.asInteger(lang[1]);
//				String text = SearchUtils.clobToString((Clob)lang[2]);
//				
//				CorespTableLang lo = t.getLangMap().get(codeLang);
//				if (lo == null) {
//					lo = new CorespTableLang();
//					lo.setCorespTable(t);
//					lo.setLang(codeLang);
//				}
//							
//				switch(codeAttr) {
//				  case 2: //CODE_ATTRIB_IDENT
//					lo.setIdent(text);
//				    break;
//				  case 3: //CODE_ATTRIB_NAME
//				    lo.setName(text);
//				    break;
//				  case 11: //CODE_ATTRIB_REGION
//				   lo.setRegion(text);
//				    break;  
//				  case 12: //CODE_ATTRIB_COMMENT
//				    lo.setComment(text);
//				    break;	
//				}
//				
//				t.getLangMap().put(codeLang, lo);
//			
//			}
//			
//			insertCorespTable(t);
//			
//			doObjectUsers(12, idTable);
//			doObjectDocs(12, idTable);
//			
//			
////			q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id from relation where id_table_corr  = :IDT");
////			q.setParameter("IDT", idTable);
////			
////			ArrayList<Object> relationsIds = (ArrayList<Object>) q.getResultList();
////			for (Object o : relationsIds) {
////				doRelation(SearchUtils.asInteger(o));
////			}
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на кореспондираща таблица !");
//			throw new DbErrorException("Грешка при запис на кореспондираща таблица !",e);
//		}
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//
//
//	@SuppressWarnings("unchecked")
//	public static void doObjectDocs(Integer codeObject, Integer idObject) throws DbErrorException {
//		
////		if (codeObject == 3) {
////			System.out.println("wersia");
////		}
//		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id, reg_number, reg_date, type, publ  from doc where id in (select id_object2 from object_relations where code_object1 = :CO and code_object2 = 9 and id_object1 = :IDO)");
//		q.setParameter("IDO", idObject);
//		q.setParameter("CO", codeObject);	
//		ArrayList<Object[]> docs = (ArrayList<Object[]>) q.getResultList();
//		for (Object[] tek : docs) {
//			ObjectDocs doc = new ObjectDocs();
//			doc.setCodeObject(codeObjectMap.get(codeObject));
//			doc.setIdObject(idObject);
//			doc.setId(SearchUtils.asInteger(tek[0]));
//			doc.setRnDoc(SearchUtils.asString(tek[1]));
//			doc.setDatDoc(SearchUtils.asDate(tek[2]));
//			doc.setType(SearchUtils.asInteger(tek[3]));
//			doc.setPubl(SearchUtils.asInteger(tek[4]));
//			
//			
//			q = JPA.getUtil("old").getEntityManager().createNativeQuery("select code_attribute, code_lang, text_attribute from attribute_languages where code_object = :CO and id_object = :IDO");
//			q.setParameter("IDO", doc.getId());
//			q.setParameter("CO", 9);
//			
//			ArrayList<Object[]> langs = (ArrayList<Object[]>) q.getResultList();
//			for (Object[] lang : langs) {
//				Integer codeAttr = SearchUtils.asInteger(lang[0]);
//				Integer codeLang = SearchUtils.asInteger(lang[1]);
//				String text = SearchUtils.clobToString((Clob)lang[2]);
//				
//				ObjectDocsLang lo = doc.getLangMap().get(codeLang);
//				if (lo == null) {
//					lo = new ObjectDocsLang();
//					lo.setObjectDocs(doc);;
//					lo.setLang(codeLang);
//				}
//							
//				switch(codeAttr) {
//				  case 4: //CODE_ATTRIB_ANOTATION
//					lo.setAnot(text);
//				    break;
//				  case 5: //CODE_ATTRIB_TEXT
//				    //lo.setTitle(text);
//					//За сега не искат да се прехвърля
//				    continue;
//				  case 6: //CODE_ATTRIB_DOP_INFO
//				   lo.setDescription(text);
//				   break;  
//				  
//				 
//				}
//				
//				doc.getLangMap().put(codeLang, lo);
//			}
//			insertObjectDoc(doc);
//			
//		}		
//		
//	}
//
//
//	
//
//
//	@SuppressWarnings("unchecked")
//	public static void doObjectUsers(Integer codeObject, Integer idObject) throws DbErrorException {
//		
//		
//		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id, id_object2, role, role_date, role_comment  from object_relations where id_object1 = :IDO and code_object1 = :CO and code_object2 = 10");
//		q.setParameter("IDO", idObject);
//		q.setParameter("CO", codeObject);
//		
//		ArrayList<Object[]> users = (ArrayList<Object[]>) q.getResultList();
//		for (Object[] user : users) {			
//			
//			ObjectUsers ou = new ObjectUsers();
//			ou.setId(SearchUtils.asInteger(user[0]));
//			ou.setCodeObject(codeObjectMap.get(codeObject));
//			ou.setIdObject(idObject);
//			ou.setCodeLice(SearchUtils.asInteger(user[1]));
//			ou.setRole(SearchUtils.asInteger(user[2]));
//			ou.setRoleDate(SearchUtils.asDate(user[3]));
//			//ou.setRoleComment(null);
//			ou.setUserReg(ActiveUser.DEFAULT.getUserId());
//			ou.setDateReg(new Date());
//			
//			String comment = SearchUtils.asString(user[4]);
//			if (comment != null) {
//				ObjectUsersLang lang = new ObjectUsersLang();
//				lang.setLang(1);
//				lang.setRoleComment(comment);
//				lang.setObjectUsers(ou);
//				
//				ou.getLangMap().put(1, lang);
//			}
//			
//			
//			
//			insertObjectUsers(ou);
//		
//		}
//		
//		
//		
//	}
//
//
//	
//
//
//	@SuppressWarnings("unchecked")
//	public static void doVersion(Integer idVersion) throws DbErrorException {
//		
//		//VersionDAO dao = new VersionDAO(ActiveUser.DEFAULT);
//		
//		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("SELECT ID, STATUS, CONFIRM_DATE, RELEASE_DATE, TERMINATION_DATE, COPYRIGHT, RAZPROSTRANENIE, POSITION_COUNT, LEVEL_COUNT, EXPANDED_LEVEL, ID_CLASS, ID_NEXT_VER, ID_PREV_VER FROM VERSION WHERE ID = :IDV");
//		q.setParameter("IDV", idVersion);
//		
//		Object[]  result = (Object[]) q.getSingleResult();
//		
//		Version v = new Version();
//		v.setId(SearchUtils.asInteger(result[0]));
//		v.setConfirmDate(SearchUtils.asDate(result[2]));
//		v.setCopyright(SearchUtils.asInteger(result[5]));
//		v.setExpandedLevel(SearchUtils.asInteger(result[9]));
//		//v.setIdent("alabala");		
//		v.setIdNextVer(SearchUtils.asInteger(result[11]));
//		v.setIdPrevVer(SearchUtils.asInteger(result[12]));
//		v.setLevelCount(SearchUtils.asInteger(result[8]));
//		v.setPositionCount(SearchUtils.asInteger(result[7]));
//		v.setRazprostranenie(SearchUtils.asInteger(result[6]));
//		v.setReleaseDate(SearchUtils.asDate(result[3]));
//		v.setStatus(SearchUtils.asInteger(result[1]));
//		v.setTerminationDate(SearchUtils.asDate(result[4]));
//		v.setIdClss(SearchUtils.asInteger(result[10]));
//		v.setUserReg(ActiveUser.DEFAULT.getUserId());
//		v.setDateReg(new Date());
//		
//		
//		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select code_attribute, code_lang, text_attribute from attribute_languages where code_object = :CO and id_object = :IDO");
//		q.setParameter("IDO", idVersion);
//		q.setParameter("CO", 3);
//		
//		ArrayList<Object[]> langs = (ArrayList<Object[]>) q.getResultList();
//		for (Object[] lang : langs) {
//			Integer codeAttr = SearchUtils.asInteger(lang[0]);
//			Integer codeLang = SearchUtils.asInteger(lang[1]);
//			String text = SearchUtils.clobToString((Clob)lang[2]);
//			
//			VersionLang lo = v.getLangMap().get(codeLang);
//			if (lo == null) {
//				lo = new VersionLang();
//				lo.setVersion(v);
//				lo.setLang(codeLang);
//			}
//						
//			switch(codeAttr) {
//			  case 1: //CODE_ATTRIB_IDENT
//				lo.setIdent(text);
//			    break;
//			  case 2: //CODE_ATTRIB_NAME
//			    lo.setTitle(text);
//			    break;
//			  case 3: //CODE_ATTRIB_DESCRIPTION
//			   lo.setDescription(text);
//			    break;  
//			  case 5: //CODE_ATTRIB_METHODOLOGY
//			    lo.setMethodology(text);
//			    break;				    
//			  case 6: //CODE_ATTRIB_COMMENT
//				lo.setComment(text);
//				break;
//			  case 7: //CODE_ATTRIB_NEWS
//				lo.setNews(text);
//				break;
//			  case 11: //CODE_ATTRIB_AREAS
//				lo.setAreas(text);
//				break;
//			  case 16: //CODE_ATTRIB_PUBLICATIONS
//				lo.setPublications(text);
//				break;
//			  case 8: //CODE_ATTRIB_LEGAL_BASE
//				lo.setLegalbase(text);
//				break;
//			  case 19: //CODE_ATTRIB_POD
//				lo.setPod(text);
//				break;
//			  case 20: //CODE_ATTRIB_POD_URL
//				lo.setPodUrl(text);
//				break;
//			 
//			}
//			
//			v.getLangMap().put(codeLang, lo);
//		
//		}
//		//dao.save(v);
//		insertVersion(v);
//		
//		doObjectUsers(3, idVersion);
//		doObjectDocs(3, idVersion);
//		
//		doVersionScheme(idVersion);
//		
//		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id from level where VERSION_ID  = :IDV");
//		q.setParameter("IDV", idVersion);
//		
//		ArrayList<Object> levelIds = (ArrayList<Object>) q.getResultList();
//		for (Object o : levelIds) {
//			doLevel(SearchUtils.asInteger(o));
//		}
//		
//		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id from table_corresp where id_vers_source = :IDV or id_vers_target = :IDV");
//		q.setParameter("IDV", idVersion);
//		
//		ArrayList<Object> tablesIds = (ArrayList<Object>) q.getResultList();
//		for (Object o : tablesIds) {
//			doCorespTable(SearchUtils.asInteger(o));
//		}
//		
//	}
//	
//	
//	
//
//
//	@SuppressWarnings("unchecked")
//	public static void doLevel(Integer idNivo) throws DbErrorException {
//		
//		//LevelDAO dao = new LevelDAO(ActiveUser.DEFAULT);
//		
//		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("SELECT id, version_id, level_number, name, symbol_count, code_type, mask_real, mask_inherit, position_count FROM level where id = :IDN");
//		q.setParameter("IDN", idNivo);
//		
//		Object[]  result = (Object[]) q.getSingleResult();
//		
//		Level l = new Level();
//		l.setId(SearchUtils.asInteger(result[0]));
//		l.setVersionId(SearchUtils.asInteger(result[1]));
//		l.setLevelNumber(SearchUtils.asInteger(result[2]));
//		l.setLevelName(SearchUtils.asInteger(result[3]));
//		l.setSymbolCount(SearchUtils.asInteger(result[4]));
//		l.setCodeType(SearchUtils.asInteger(result[5]));
//		l.setMaskReal(SearchUtils.asString(result[6]));		
//		l.setMaskInherit(SearchUtils.asString(result[7]));		
//		l.setPositionCount(SearchUtils.asInteger(result[8]));	
//		l.setUserReg(ActiveUser.DEFAULT.getUserId());
//		l.setDateReg(new Date());
//		
//		
//		
//		
//		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select code_attribute, code_lang, text_attribute from attribute_languages where code_object = :CO and id_object = :IDO");
//		q.setParameter("IDO", idNivo);
//		q.setParameter("CO", 7);
//		
//		ArrayList<Object[]> langs = (ArrayList<Object[]>) q.getResultList();
//		for (Object[] lang : langs) {			
//			Integer codeLang = SearchUtils.asInteger(lang[1]);
//			String text = SearchUtils.clobToString((Clob)lang[2]);			
//			
//			
//			LevelLang lo = l.getLangMap().get(codeLang);
//			if (lo == null) {
//				lo = new LevelLang();
//				lo.setLevel(l);
//				lo.setLang(codeLang);
//			}
//						
//			lo.setDescription(text);			
//			l.getLangMap().put(codeLang, lo);
//		
//		}
//		
//		insertLevel(l);
//		
//		
//	}
//	
//	@SuppressWarnings("unchecked")
//	public static void doSysClassifNew(Integer codeC) throws ParseException, DbErrorException, UnexpectedResultException {
//		
//		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//		Date dat = sdf.parse("01.01.1900");
//		System.out.println("dat ---->>>>>>>>>>>>>>>>>>>>" + dat);
//		
//		maxCod = 200000;
//		
//		SystemClassifDAO dao = new SystemClassifDAO(ActiveUser.DEFAULT);
//		
//		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id, seq_number, classification, code, text, lang, level_number, parrent_id, ind_child, end_level from system_classif where classification = "+codeC+" and lang = 1 order by seq_number");
//		List<Object[]> result = q.getResultList();
//		
//		
//		ArrayList<SystemClassif> classif = new ArrayList<SystemClassif>();
//		HashMap<Integer, Integer> codes = new HashMap<Integer, Integer>(); 
//		recBuildClassif(classif, 0, 0, 1, result, codes, dat, -100000, 100000);
//		
//		
//		SysClassifUtils.doSortClassifPrev(classif);
//		
//		for (SystemClassif tek : classif) {
//			dao.doSimpleSave(tek);
//			for (int i = 0; i < tek.getLevelNumber(); i++) {
//				System.out.print("\t");					
//			}
//			System.out.println(tek.getTekst() + " - " + tek.getCode());
//		}
//	}
//
//
//	
//	
//	
//	@SuppressWarnings("unchecked")
//	public static void doVersionScheme (Integer vesrionId) throws DbErrorException {
//		
//		
//		
//		try {
//			
//			
//			
//			//Order-a e ВАЖЕН !!!
//			String sql = "select id, code, text, level_number, parrent_code, ind_child, id_position from scheme where id_version = :ViD order by number_item ";
//			
//			Query q = JPA.getUtil("old").getEntityManager().createNativeQuery(sql);
//			q.setParameter("ViD", vesrionId);
//			
//			ArrayList<Object[]> rows = (ArrayList<Object[]>) q.getResultList();
//			ArrayList<SchemeItem> scheme = new ArrayList<SchemeItem> ();
//			
//			System.out.println("Build Started");
//			recBuildScheme(scheme, rows, 0,vesrionId);
//			System.out.println("Build Ended");
//			System.out.println("rows.size="+rows.size());
//			System.out.println("scheme.size="+scheme.size());
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			
//			
//			
//			
//			
//			for (SchemeItem tek : scheme) {				
//				
//				sql = "INSERT INTO SCHEME(ID, CODE, DEFF_NAME, VERSION_ID, POSITION_ID, ID_PREV, ID_PARENT, LEVEL_NUMBER, USER_REG, DATE_REG, IND_CHILD) " +  
//						" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
//				
//				q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
//				q.setParameter(1, tek.getId());
//				q.setParameter(2, tek.getCode());
//				q.setParameter(3, tek.getDeffName());
//				q.setParameter(4, tek.getVersionId());
//				q.setParameter(5, tek.getPositionId());
//				q.setParameter(6, tek.getIdPrev());
//				q.setParameter(7, tek.getIdParent());
//				q.setParameter(8, tek.getLevelNumber());
//				q.setParameter(9, ActiveUser.DEFAULT.getUserId());
//				q.setParameter(10, new Date());
//				q.setParameter(11, tek.getIndChild());
//				q.executeUpdate();
//				
//				//System.out.println("---->" +tek.getPositionId());
////				q = JPA.getUtil().getEntityManager().createNativeQuery("select count(id) from position where id = :IDP");
////				q.setParameter("IDP", tek.getPositionId());
////				
////				Integer br = (Integer)q.getSingleResult();
////				if (br == null || br == 0) {
////					//System.out.println("************************************************************** Inserting new position ...");					
////					doPosition(tek.getPositionId());
////				}
//				
//			}
//			
//			
//		
//		
//		
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на документ !");
//			throw new DbErrorException("Грешка при запис на документ !",e);
//		}
//		
//	}
//
////	@SuppressWarnings("unchecked")
////	public static void doPosition(Integer positionId) throws DbErrorException {
////		
////		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("SELECT id, code, code_full, code_separate, code_type, status, level_number FROM position where id = :IDP");
////		q.setParameter("IDP", positionId);
////		
////		Object[]  result = (Object[]) q.getSingleResult();
////		
////		Position p = new Position();
////		p.setCode(SearchUtils.asString(result[1]));
////		p.setCodeFull(SearchUtils.asString(result[2]));
////		p.setCodeSeparate(SearchUtils.asString(result[3]));
////		p.setCodeType(SearchUtils.asInteger(result[4]));
////		p.setId(SearchUtils.asInteger(result[0]));
////		p.setLevelNumber(SearchUtils.asInteger(result[6]));
////		p.setStatus(SearchUtils.asInteger(result[5]));
////		
////		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select unit from pu where position_id = :IDP");
////		q.setParameter("IDP", positionId);
////		
////		ArrayList<Object> units = (ArrayList<Object>) q.getResultList();
////		for (Object row : units) {
////			PositionUnits pu = new PositionUnits();
////			pu.setPosition(p);
////			pu.setUnit(SearchUtils.asInteger(row));
////			p.getUnits().add(pu);
////		}
////		
////		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select code_attribute, code_lang, text_attribute from attribute_languages where code_object = :CO and id_object = :IDO");
////		q.setParameter("IDO", positionId);
////		q.setParameter("CO", 8);
////		
////		ArrayList<Object[]> langs = (ArrayList<Object[]>) q.getResultList();
////		for (Object[] lang : langs) {
////			Integer codeAttr = SearchUtils.asInteger(lang[0]);
////			Integer codeLang = SearchUtils.asInteger(lang[1]);
////			String text = SearchUtils.clobToString((Clob)lang[2]);
////			
////			PositionLang lo = p.getLangMap().get(codeLang);
////			if (lo == null) {
////				lo = new PositionLang();
////				lo.setPosition(p);
////				lo.setLang(codeLang);
////			}
////						
////			switch(codeAttr) {
////			  case 11: //CODE_ATTRIB_OFFICIAL_TITLE
////				lo.setOffitialTitile(text);
////			    break;
////			  case 12: //CODE_ATTRIB_SHORT_TITLE
////			    lo.setShortTitle(text);
////			    break;
////			  case 13: //CODE_ATTRIB_MEDIUM_TITLE
////			   lo.setLongTitle(text);
////			    break;  
////			  case 14: //CODE_ATTRIB_ALTERNATE_TITLE
////			    lo.setAlternativeNames(text);
////			    break;				    
////			  case 15: //CODE_ATTRIB_COMMENT
////				lo.setComment(text);
////				break;
////			  case 16: //CODE_ATTRIB_INCLUDES
////				lo.setIncludes(text);
////				break;
////			  case 17: //CODE_ATTRIB_ALSO_INCLUDES
////				lo.setAlsoIncludes(text);
////				break;
////			  case 18: //CODE_ATTRIB_EXCLUDES
////				lo.setExcludes(text);
////				break;
////			  case 19: //CODE_ATTRIB_CASE_LAW
////				lo.setRules(text);
////				break;
////			  
////			 
////			}
////			
////			p.getLangMap().put(codeLang, lo);
////		}
////		
////		insertPosition(p);
////		
////		
////		
////	}
//
//	@SuppressWarnings("unchecked")
//	public static void doRelation(Integer relationId) throws DbErrorException {
//		
//		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("SELECT id, source_code, target_code, weigth_source, weigth_target, relation_type, change_type, id_table_corr FROM relation where id = :IDR");
//		q.setParameter("IDR", relationId);
//		
//		Object[]  result = (Object[]) q.getSingleResult();
//		
//		Relation r = new Relation();
//		r.setId(SearchUtils.asInteger(result[0]));
//		r.setSourceCode(SearchUtils.asString(result[1]));
//		r.setTargetCode(SearchUtils.asString(result[2]));
////		r.setWeigthSource(SearchUtils.asDouble(result[3]));
////		r.setWeigthTarget(SearchUtils.asDouble(result[4]));
////		r.setRelationType(SearchUtils.asInteger(result[5]));
////		r.setChangeType(SearchUtils.asInteger(result[6]));
//		r.setIdTable(SearchUtils.asInteger(result[7]));
//		r.setUserReg(ActiveUser.DEFAULT.getUserId());
//		r.setDateReg(new Date());
//		
//		
//		q = JPA.getUtil("old").getEntityManager().createNativeQuery("select code_attribute, code_lang, text_attribute from attribute_languages where code_object = :CO and id_object = :IDO");
//		q.setParameter("IDO", relationId);
//		q.setParameter("CO", 13);
//		
//		ArrayList<Object[]> langs = (ArrayList<Object[]>) q.getResultList();
//		for (Object[] lang : langs) {
//			//Integer codeAttr = SearchUtils.asInteger(lang[0]);
//			Integer codeLang = SearchUtils.asInteger(lang[1]);
//			String text = SearchUtils.clobToString((Clob)lang[2]);
//			
////			RelationLang lo = r.getLangMap().get(codeLang);
////			if (lo == null) {
////				lo = new RelationLang();
////				lo.setRelation(r);
////				lo.setLang(codeLang);
////			}
////						
////			lo.setComment(text);
////			
////			r.getLangMap().put(codeLang, lo);
//		}
//		
//		insertRelation(r);
//		
//		
//		
//	}
//	
//	
//	
//	
//
//
//	public static void insertClassification(Classification classif) throws DbErrorException {
//		
//		
//		try {
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO CLASSIFICATION(ID, CLASS_TYPE, CLASS_UNIT, FAMILY, DATE_REG, USER_REG, DATE_LAST_MOD, USER_LAST_MOD) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
//			q.setParameter(1, classif.getId());
//			q.setParameter(2, classif.getClassType());
//			q.setParameter(3, classif.getClassUnit());
//			q.setParameter(4, classif.getFamily());
//			q.setParameter(5, classif.getDateReg());
//			q.setParameter(6, classif.getUserReg());
//			q.setParameter(7, classif.getDateLastMod());
//			q.setParameter(8, classif.getUserLastMod());
//			
//			q.executeUpdate();
//			
//			Iterator<Entry<Integer, ClassificationLang>> it = classif.getLangMap().entrySet().iterator();
//			while (it.hasNext()) {
//				
////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_CLASSIFICATION_LANG");
////				Integer idL = SearchUtils.asInteger(q.getSingleResult());
//				
//				ClassificationLang cLang = it.next().getValue(); 
//				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO CLASSIFICATION_LANG(ID, CLASSIFICATION_ID, LANG, IDENT, NAME_CLASSIF, DESCRIPTION, COMMENT, NEWS, AREA) VALUES(next value for SEQ_CLASSIFICATION_LANG,?,?,?,?,?,?,?,?)");
//				q.setParameter(1,classif.getId());
//				q.setParameter(2,cLang.getLang());
//				q.setParameter(3,cLang.getIdent());
//				q.setParameter(4,cLang.getNameClassif());
//				q.setParameter(5,cLang.getDescription());
//				q.setParameter(6,cLang.getComment());
//				q.setParameter(7,cLang.getNews());
//				q.setParameter(8,cLang.getArea());
//				//q.setParameter(9,idL);
//				q.executeUpdate();				
//			}
//			
//			for (ClassificationAttributes attr : classif.getAttributes()) {
//				
////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_CLASSIFICATION_ATTRIBUTES");
////				Integer idL = SearchUtils.asInteger(q.getSingleResult());
//				
//				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO CLASSIFICATION_ATTRIBUTES(ID, CLASSIFICATION_ID, CODE_ATTRIB)  VALUES(next value for SEQ_CLASSIFICATION_ATTRIBUTES, ?, ?)");
//				//q.setParameter(1,idL);
//				q.setParameter(1,classif.getId());
//				q.setParameter(2,attr.getCodeAttrib());
//				
//				q.executeUpdate();		
//			}
//				
//			
//			
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на класификация !");
//			throw new DbErrorException("Грешка при запис на класификация !",e);
//		}
//		
//		
//	}
//	
//	
//	public static void insertVersion(Version version) throws DbErrorException {
//		
//		
//		try {
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO VERSION(ID, STATUS, CONFIRM_DATE, RELEASE_DATE, TERMINATION_DATE, COPYRIGHT, RAZPROSTRANENIE, POSITION_COUNT, LEVEL_COUNT, EXPANDED_LEVEL, ID_CLASS, ID_NEXT_VER, ID_PREV_VER, DATE_REG, USER_REG, DATE_LAST_MOD, USER_LAST_MOD) 	VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			q.setParameter(1, version.getId());
//			q.setParameter(2, version.getStatus());
//			q.setParameter(3, new TypedParameterValue(StandardBasicTypes.DATE, version.getConfirmDate()));
//			q.setParameter(4, new TypedParameterValue(StandardBasicTypes.DATE, version.getReleaseDate()));			
//			q.setParameter(5, new TypedParameterValue(StandardBasicTypes.DATE, version.getTerminationDate()));
//			q.setParameter(6, version.getCopyright());
//			q.setParameter(7, version.getRazprostranenie());
//			q.setParameter(8, version.getPositionCount());
//			q.setParameter(9, version.getLevelCount());
//			q.setParameter(10, version.getExpandedLevel());
//			q.setParameter(11, version.getIdClss());
//			q.setParameter(12, version.getIdNextVer());
//			q.setParameter(13, version.getIdPrevVer());
//			
//			q.setParameter(14, version.getDateReg());
//			q.setParameter(15, version.getUserReg());
//			q.setParameter(16, version.getDateLastMod());
//			q.setParameter(17, version.getUserLastMod());
//			
//			q.executeUpdate();
//			
//			Iterator<Entry<Integer, VersionLang>> it = version.getLangMap().entrySet().iterator();
//			while (it.hasNext()) {
//				
////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_VERSION_LANG");
////				Integer idL = SearchUtils.asInteger(q.getSingleResult());
//				
//				VersionLang vLang = it.next().getValue(); 
//				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO VERSION_LANG(ID, VERSION_ID, LANG, IDENT, TITLE, DESCRIPTION, METHODOLOGY, COMMENT, NEWS, LEGALBASE, PUBLICATIONS, AREAS, POD, POD_URL) VALUES(next value for SEQ_VERSION_LANG, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//				//q.setParameter(1,idL);
//				q.setParameter(1,version.getId());
//				q.setParameter(2,vLang.getLang());
//				q.setParameter(3,vLang.getIdent());
//				q.setParameter(4,vLang.getTitle());
//				q.setParameter(5,vLang.getDescription());
//				q.setParameter(6,vLang.getMethodology());
//				q.setParameter(7,vLang.getComment());
//				q.setParameter(8,vLang.getNews());
//				q.setParameter(9,vLang.getLegalbase());
//				q.setParameter(10,vLang.getPublications());
//				q.setParameter(11,vLang.getAreas());
//				q.setParameter(12,vLang.getPod());
//				q.setParameter(13,vLang.getPodUrl());
//				
//				q.executeUpdate();				
//			}
//				
//			
//			
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на версия !");
//			throw new DbErrorException("Грешка при запис на версия !",e);
//		}
//		
//		
//	}
//	
//	
//	public static void insertLevel(Level level) throws DbErrorException {
//		
//		
//		try {
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO LEVEL(ID, LEVEL_NUMBER, VERSION_ID, LEVEL_NAME, SYMBOL_COUNT, CODE_TYPE, MASK_REAL, MASK_INHERIT, POSITION_COUNT, DATE_REG, USER_REG, DATE_LAST_MOD, USER_LAST_MOD)  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			q.setParameter(1, level.getId());
//			q.setParameter(2, level.getLevelNumber());
//			q.setParameter(3, level.getVersionId());
//			q.setParameter(4, level.getLevelName());
//			q.setParameter(5, level.getSymbolCount());
//			q.setParameter(6, level.getCodeType());
//			q.setParameter(7, level.getMaskReal());
//			q.setParameter(8, level.getMaskInherit());
//			q.setParameter(9, level.getPositionCount());
//			q.setParameter(10, level.getDateReg());
//			q.setParameter(11, level.getUserReg());
//			q.setParameter(12, level.getDateLastMod());
//			q.setParameter(13, level.getUserLastMod());
//			
//			
//			q.executeUpdate();
//			
//			Iterator<Entry<Integer, LevelLang>> it = level.getLangMap().entrySet().iterator();
//			while (it.hasNext()) {
//				
////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_LEVEL_LANG");
////				Integer idL = SearchUtils.asInteger(q.getSingleResult());
//				
//				LevelLang lLang = it.next().getValue(); 
//				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO LEVEL_LANG(ID, LEVEL_ID, LANG, DESCRIPTION) VALUES(next value for SEQ_LEVEL_LANG, ?, ?, ?)");
//				//q.setParameter(1,idL);
//				q.setParameter(1,level.getId());				
//				q.setParameter(2,lLang.getLang());
//				q.setParameter(3,lLang.getDescription());	
//				q.executeUpdate();				
//			}
//				
//			
//			
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на ниво !");
//			throw new DbErrorException("Грешка при запис на ниво !",e);
//		}
//		
//		
//	}
//	
//	
//	
//	
//	public static void insertObjectUsers(ObjectUsers ou) throws DbErrorException {
//		try {
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO OBJECT_USERS(ID, CODE_OBJECT, ID_OBJECT, CODE_LICE, ROLE, ROLE_DATE, DATE_REG, USER_REG, DATE_LAST_MOD, USER_LAST_MOD) 	VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			q.setParameter(1, ou.getId());
//			q.setParameter(2, ou.getCodeObject());
//			q.setParameter(3, ou.getIdObject());
//			q.setParameter(4, ou.getCodeLice());
//			q.setParameter(5, ou.getRole());
//			q.setParameter(6, ou.getRoleDate());
//			
//			
//			q.setParameter(7, ou.getDateReg());
//			q.setParameter(8, ou.getUserReg());
//			q.setParameter(9, ou.getDateLastMod());
//			q.setParameter(10, ou.getUserLastMod());
//			
//			
//			q.executeUpdate();
//			
//			Iterator<Entry<Integer, ObjectUsersLang>> it = ou.getLangMap().entrySet().iterator();
//			while (it.hasNext()) {
//				
////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_OBJECT_USERS_LANG");
////				Integer idL = SearchUtils.asInteger(q.getSingleResult());
//				
//				ObjectUsersLang cLang = it.next().getValue(); 
//				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO OBJECT_USERS_LANG(ID, OBJECT_USERS_ID, LANG, ROLE_COMMENT) 	VALUES(next value for SEQ_OBJECT_USERS_LANG, ?, ?, ?)");
//				//q.setParameter(1,idL);
//				q.setParameter(1,ou.getId());
//				q.setParameter(2,cLang.getLang());
//				q.setParameter(3,cLang.getRoleComment());
//				
//				q.executeUpdate();				
//			}
//				
//			
//			
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на потребители !");
//			throw new DbErrorException("Грешка при запис на потребители !",e);
//		}
//	}
//	
//	public static void insertObjectDoc(ObjectDocs doc) throws DbErrorException {
//		try {
//			
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_OBJECT_DOCS");
//			Integer idDoc = SearchUtils.asInteger(q.getSingleResult());
//			
//			q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO OBJECT_DOCS(ID, CODE_OBJECT, ID_OBJECT, RN_DOC, DAT_DOC, TYPE, DATE_REG, USER_REG, publ) 	VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			q.setParameter(1, idDoc);
//			q.setParameter(2, doc.getCodeObject());
//			q.setParameter(3, doc.getIdObject());
//			q.setParameter(4, doc.getRnDoc());
//			//q.setParameter(5, doc.getDatDoc());			
//			q.setParameter(5, new TypedParameterValue(StandardBasicTypes.DATE, doc.getDatDoc()));			
//			q.setParameter(6, doc.getType());
//			q.setParameter(7, new Date());
//			q.setParameter(8, ActiveUser.DEFAULT.getUserId());
//			q.setParameter(9, doc.getPubl());
//			
//			
//			q.executeUpdate();
//			
//			Iterator<Entry<Integer, ObjectDocsLang>> it = doc.getLangMap().entrySet().iterator();
//			while (it.hasNext()) {
//				
////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_OBJECT_DOCS_LANG");
////				Integer idL = SearchUtils.asInteger(q.getSingleResult());
//				
//				ObjectDocsLang lLang = it.next().getValue(); 
//				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO OBJECT_DOCS_LANG(ID, OBJECT_DOCS_ID, LANG, ANOT, DESCRIPTION) VALUES(next value for SEQ_OBJECT_DOCS_LANG, ?, ?, ?, ?)");
//				//q.setParameter(1,idL);
//				q.setParameter(1,idDoc);				
//				q.setParameter(2,lLang.getLang());
//				q.setParameter(3,lLang.getAnot());	
//				q.setParameter(4,lLang.getDescription());
//				q.executeUpdate();				
//			}
//				
//			
//			
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на документ !");
//			throw new DbErrorException("Грешка при запис на документ !",e);
//		}
//		
//	}
//	
//	
////	public static void insertPosition(Position p) throws DbErrorException {
////		try {
////			
////			
////			
////			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO POSITION(ID, CODE, CODE_FULL, CODE_SEPARATE, CODE_TYPE, STATUS, LEVEL_NUMBER, DATE_REG, USER_REG) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
////			q.setParameter(1, p.getId());
////			q.setParameter(2, p.getCode());
////			q.setParameter(3, p.getCodeFull());
////			q.setParameter(4, p.getCodeSeparate());						
////			q.setParameter(5, p.getCodeType());			
////			q.setParameter(6, p.getStatus());
////			q.setParameter(7, p.getLevelNumber());
////			q.setParameter(8, new Date());
////			q.setParameter(9, ActiveUser.DEFAULT.getUserId());
////			
////			
////			
////			q.executeUpdate();
////			
////			Iterator<Entry<Integer, PositionLang>> it = p.getLangMap().entrySet().iterator();
////			while (it.hasNext()) {
////				
//////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_POSITION_LANG");
//////				Integer idP = SearchUtils.asInteger(q.getSingleResult());
////				
////				PositionLang lLang = it.next().getValue(); 
////				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO POSITION_LANG(ID, POSITION_ID, LANG, OFFICIAL_TITLE, LONG_TITLE, SHORT_TITLE, ALTERNATE_TITLES, COMMENT, INCLUDES, ALSO_INCLUDES, EXCLUDES, RULES) 	VALUES(next value for SEQ_POSITION_LANG, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
////				//q.setParameter(1,idP);
////				q.setParameter(1,p.getId());				
////				q.setParameter(2,lLang.getLang());
////				q.setParameter(3,lLang.getOffitialTitile());	
////				q.setParameter(4,lLang.getLongTitle());
////				q.setParameter(5,lLang.getShortTitle());
////				q.setParameter(6,lLang.getAlternativeNames());
////				q.setParameter(7,lLang.getComment());
////				q.setParameter(8,lLang.getIncludes());
////				q.setParameter(9,lLang.getAlsoIncludes());
////				q.setParameter(10,lLang.getExcludes());
////				q.setParameter(11,lLang.getRules());
////				q.executeUpdate();				
////			}
////				
////			for (PositionUnits pu : p.getUnits()) {
//////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_POSITION_UNITS");
//////				Integer idPU = SearchUtils.asInteger(q.getSingleResult());
////				
////				
////				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO POSITION_UNITS(ID, POSITION_ID, UNIT) 	VALUES(next value for SEQ_POSITION_UNITS, ?, ?)");
////				//q.setParameter(1,idPU);
////				q.setParameter(1,p.getId());	
////				q.setParameter(2,pu.getUnit());
////				
////				q.executeUpdate();
////			}
////			
////			
////		} catch (Exception e) {
////			LOGGER.error("Грешка при запис на позиция !");
////			throw new DbErrorException("Грешка при запис на позиция !",e);
////		}
////		
////	}
//	
//	public static void insertCorespTable(CorespTable t) throws DbErrorException {
//		try {
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO TABLE_CORRESP(ID, ID_VERS_SOURCE, ID_VERS_TARGET, STATUS, TABLE_TYPE, RELATIONS_COUNT, SOURCE_POS_COUNT, TARGET_POS_COUNT, RELATION_TYPE, PATH, DATE_REG, USER_REG) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			q.setParameter(1, t.getId());
//			q.setParameter(2, t.getIdVersSource());
//			q.setParameter(3, t.getIdVersTarget());
//			q.setParameter(4, t.getStatus());
//			q.setParameter(5, t.getTableType());
//			q.setParameter(6, t.getRelationsCount());
//			q.setParameter(7, t.getSourcePosCount());
//			q.setParameter(8, t.getTargetPosCount());
//			
//			q.setParameter(9,  t.getRelationType());
//			q.setParameter(10, t.getPath());
//			q.setParameter(11, t.getDateReg());
//			q.setParameter(12, t.getUserReg());
//			
//			q.executeUpdate();
//			
//			Iterator<Entry<Integer, CorespTableLang>> it = t.getLangMap().entrySet().iterator();
//			while (it.hasNext()) {
//				
////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_TABLE_CORRESP_LANG");
////				Integer idL = SearchUtils.asInteger(q.getSingleResult());
//				
//				CorespTableLang cLang = it.next().getValue(); 
//				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO TABLE_CORRESP_LANG(ID, TABLE_CORRESP_ID, LANG, IDENT, NAME, REGION, COMMENT) VALUES(next value for SEQ_TABLE_CORRESP_LANG, ?, ?, ?, ?, ?, ?)");
//				//q.setParameter(1,idL);
//				q.setParameter(1, t.getId());
//				q.setParameter(2,cLang.getLang());
//				q.setParameter(3,cLang.getIdent());
//				q.setParameter(4,cLang.getName());
//				q.setParameter(5,cLang.getRegion());
//				q.setParameter(6,cLang.getComment());				
//				q.executeUpdate();				
//			}
//			
//			
//				
//			
//			
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на таблица !");
//			throw new DbErrorException("Грешка при запис на таблица !",e);
//		}
//		
//		
//	}
//	
//	
//	private static void insertRelation(Relation r) throws DbErrorException {
//		
//		try {
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO RELATION(ID, ID_TABLE, SOURCE_CODE, TARGET_CODE, WEIGTH_SOURCE, WEIGTH_TARGET, RELATION_TYPE, CHANGE_TYPE, DATE_REG, USER_REG)	VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			q.setParameter(1, r.getId());
//			q.setParameter(2, r.getIdTable());
//			q.setParameter(3, r.getSourceCode());
//			q.setParameter(4, r.getTargetCode());
////			q.setParameter(5, r.getWeigthSource());
////			q.setParameter(6, r.getWeigthTarget());
////			q.setParameter(7, r.getRelationType());
////			q.setParameter(8, r.getChangeType());
//			q.setParameter(9, r.getDateReg());
//			q.setParameter(10, r.getUserReg());
//			
//			q.executeUpdate();
//			
////			Iterator<Entry<Integer, RelationLang>> it = r.getLangMap().entrySet().iterator();
////			while (it.hasNext()) {
////				
//////				q = JPA.getUtil().getEntityManager().createNativeQuery("select next value for SEQ_RELATION_LANG");
//////				Integer idL = SearchUtils.asInteger(q.getSingleResult());
////				
////				RelationLang cLang = it.next().getValue(); 
////				q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO RELATION_LANG(ID, RELATION_ID, LANG, COMMENT) 	VALUES(next value for SEQ_RELATION_LANG, ?, ?, ?)");
////				//q.setParameter(1,idL);
////				q.setParameter(1, r.getId());
////				q.setParameter(2,cLang.getLang());				
////				q.setParameter(3,cLang.getComment());				
////				q.executeUpdate();				
////			}
//			
//			
//		} catch (Exception e) {
//			LOGGER.error("Грешка при запис на релация !");
//			throw new DbErrorException("Грешка при запис на релация !",e);
//		}
//		
//	}
//
//	
//
//
//	private static void recBuildScheme(ArrayList<SchemeItem> scheme, ArrayList<Object[]> rows, Integer parentId, Integer versionId) {
//		
//		Integer codePrev = 0;
//		for (Object[] row : rows) {
//			Integer tekParent = SearchUtils.asInteger(row[4]);
//			if (parentId.equals(tekParent)) {
//				String codeExt = SearchUtils.asString(row[1]);
//				String tekst = SearchUtils.asString(row[2]);
//				Integer id = SearchUtils.asInteger(row[0]);				
//				Integer levelNumber = SearchUtils.asInteger(row[3]);
//				Integer indChild = SearchUtils.asInteger(row[5]);
//				Integer idPosition = SearchUtils.asInteger(row[6]);
//				
//				
//				
//				SchemeItem item = new SchemeItem();
//				
//				item.setVersionId(versionId);
//				item.setCode(codeExt);
//				item.setIdParent(parentId);
//				item.setIdPrev(codePrev);
//				item.setPositionId(idPosition);				
//				item.setId(id);
//				item.setLevelNumber(levelNumber);
//				item.setDeffName(tekst);
//				item.setDateReg(new Date());
//				item.setUserReg(ActiveUser.DEFAULT.getUserId());
//				item.setIndChild(indChild);
//				
//				scheme.add(item);
//				codePrev = id;
//				
//				//if(indChild.equals(1)) {
//					recBuildScheme(scheme, rows, id, versionId);
//				//}
//				
//				
//			}
//		}
//		
//	}
//	
//	private static void recBuildClassif(ArrayList<SystemClassif> classif, Integer codeParent, Integer newCodeParent, Integer levelNumber, List<Object[]> result, HashMap<Integer, Integer> codes, Date dat, Integer fromSeq, Integer toSeq) {
//		
//		Integer tekPrev = 0;
//		for (Object[] row : result) {
//			Integer seq = SearchUtils.asInteger(row[1]);
//			Integer codeClassif = SearchUtils.asInteger(row[2]);
//			Integer code = SearchUtils.asInteger(row[3]);
//			String tekst = SearchUtils.asString(row[4]);
//			Integer lang = SearchUtils.asInteger(row[5]);
//			Integer level = SearchUtils.asInteger(row[6]);
//			Integer parentId = SearchUtils.asInteger(row[7]);
//			
//			if (seq <= fromSeq) {
//				continue;
//			}
//			
//			if (seq >= toSeq) {
//				break;
//			}
//			
//			if (level.equals(levelNumber) && parentId.equals(codeParent) ) {
//				//В правилното ниво сме
//				Integer newMax = 100000;
//				
//				//Търсим следващо значение с такъв код
//				for (Object[] temp : result) {
//					Integer seqTemp = SearchUtils.asInteger(temp[1]);
//					Integer codeTemp = SearchUtils.asInteger(temp[3]);					
//					if (code.equals(codeTemp) && seq < seqTemp) {
//						//Следващо значение с този код
//						newMax = seqTemp;
//						break;
//					}
//				}
//				
//				//Дали трябва да вземем нов код
//				Integer flag = codes.get(code);
//				Integer newCode = code;
//				if (flag != null) {
//					newCode = maxCod;
//					maxCod ++;
//				}
//				codes.put(code, 1); //Izpolzwan weche
//				
//				
//				SystemClassif item = new SystemClassif();
//				item.setCode(newCode);
//				item.setCodeClassif(100+codeClassif);
//				item.setCodeParent(newCodeParent);
//				item.setCodePrev(tekPrev);
//				item.setDateOt(dat);
//				item.setLevelNumber(level);
//				item.setTekst(tekst);
//				
//				Multilang l = new Multilang();
//				l.setLang(lang);
//				l.setTekst(tekst);
//				item.getTranslations().add(l);
//				
//				Multilang l2 = new Multilang();
//				l2.setLang(2);
//				l2.setTekst(tekst+"EN");
//				item.getTranslations().add(l2);
//				
//				classif.add(item);
//				tekPrev = newCode;
//				
//				recBuildClassif(classif, code, newCode, level+1, result, codes, dat,seq,newMax);
//				
//			}
//		}
//		
//	}
//	
//	
//	@SuppressWarnings("unchecked")
//	public static void bulkInsertRelations(Integer idClass) {
//		
//		
//		String sqlInsert = "INSERT INTO RELATION(ID, ID_TABLE, SOURCE_CODE, TARGET_CODE, WEIGTH_SOURCE, WEIGTH_TARGET, RELATION_TYPE, CHANGE_TYPE, DATE_REG, USER_REG)	VALUES ";
//		String sqlValues = "";
//		
//		String sql = "select id, source_code, target_code, weigth_source, weigth_target, relation_type, change_type, id_table_corr from relation where id_table_corr in "
//				+ "        ( select id from table_corresp where "
//				+ "                id_vers_source in ( select id from version where id_class = :IDC) "
//				+ "                    or "
//				+ "                id_vers_target in ( select id from version where id_class = :IDC) )";
//		
//		
//		Stream<Object[]> stream = JPA.getUtil("old").getEntityManager().createNativeQuery(sql) 
//				.setParameter("IDC", idClass) //				
//				.setHint(QueryHints.HINT_FETCH_SIZE, 1000) 
//				.getResultStream();
//		
//		Iterator<Object[]> it = stream.iterator();
//		int counter = 0;
//		while (it.hasNext()) {
//			Object[] row = it.next();
//			
//			//System.out.println(row[1] + " --> " + row[2]);
//			sqlValues += " (" + row[0] + "," + row[7] + ",'" + row[1] + "','" + row[2] + "'," + row[3] + "," + row[4] + "," + row[5] + ","  + row[6] + ", :DAT ,-1 ),"; 
//			
//			counter++;
//			
//			if (counter == 100) {
//				sqlValues = sqlValues.substring(0, sqlValues.length()-1);
//				//System.out.println(sqlValues);
//				
//				JPA.getUtil().getEntityManager().createNativeQuery(sqlInsert + sqlValues ).setParameter("DAT", new Date()).executeUpdate();
//				
//				counter = 0;
//				sqlValues = "";
//				
//			}
//			
//			
//		}
//		if (! sqlValues.isEmpty()) {
//			sqlValues = sqlValues.substring(0, sqlValues.length()-1);
//			JPA.getUtil().getEntityManager().createNativeQuery(sqlInsert + sqlValues ).setParameter("DAT", new Date()).executeUpdate();
//		}
//		
//		
//	}
//	
//	
//	@SuppressWarnings("unchecked")
//	public static void bulkInsertPositions(Integer idClass) {
//		
//		
//		String sqlInsert = "INSERT INTO POSITION(ID, CODE, CODE_FULL, CODE_SEPARATE, CODE_TYPE, STATUS, LEVEL_NUMBER, DATE_REG, USER_REG) VALUES ";
//		String sqlValues = "";
//		
//		//String sql = "SELECT id, code, code_full, code_separate, code_type, status, level_number FROM position where id in (select id_position from scheme where id_version in (select id from version where id_class = :IDC ))";
//		String sql = "SELECT distinct position.id, position.code, position.code_full, position.code_separate, position.code_type, position.status, position.level_number "
//				+ "FROM position, scheme, version "
//				+ "where   position.id = scheme.id_position and "
//				+ "        scheme.id_version = version.id and "
//				+ "        version.id_class = :IDC";
//		
//		Stream<Object[]> stream = JPA.getUtil("old").getEntityManager().createNativeQuery(sql) 
//				.setParameter("IDC", idClass) //				
//				.setHint(QueryHints.HINT_FETCH_SIZE, 1000) 
//				.getResultStream();
//		
//		Iterator<Object[]> it = stream.iterator();
//		int counter = 0;
//		while (it.hasNext()) {
//			Object[] row = it.next();
//			
//			if (row[2] == null) {
//				row[2] = "";
//			}
//			if (row[3] == null) {
//				row[3] = "";
//			}
//			//System.out.println(row[1] + " --> " + row[2]);
//			sqlValues += " (" + row[0] + ",'" + row[1] + "','" + row[2] + "','" + row[3] + "'," + row[4] + "," + row[5] + "," + row[6] + ", :DAT ,-1 ),"; 
//			
//			counter++;
//			
//			if (counter == 100) {
//				sqlValues = sqlValues.substring(0, sqlValues.length()-1);
//				//System.out.println(sqlValues);
//				
//				JPA.getUtil().getEntityManager().createNativeQuery(sqlInsert + sqlValues ).setParameter("DAT", new Date()).executeUpdate();
//				
//				counter = 0;
//				sqlValues = "";
//				
//			}
//			
//			
//		}
//		if (! sqlValues.isEmpty()) {
//			sqlValues = sqlValues.substring(0, sqlValues.length()-1);
//			JPA.getUtil().getEntityManager().createNativeQuery(sqlInsert + sqlValues ).setParameter("DAT", new Date()).executeUpdate();
//		}
//		
//		
//	}
//	
//	
////	@SuppressWarnings("unchecked")
////	public static HashMap<Integer, HashMap<Integer, PositionLang>> createPositionLangMap(Integer idClass){
////		HashMap<Integer, HashMap<Integer, PositionLang>> result = new HashMap<Integer, HashMap<Integer, PositionLang>>();
////		
////		//String sql = "select id_object, code_attribute, code_lang, text_attribute from attribute_languages where code_object = 8 and id_object in (select id from POSITION where id in (select id_position from scheme where id_version in (select id from version where id_class = :IDC )))";
////		
////		String sql = "select DISTINCT attribute_languages.id_object, attribute_languages.code_attribute, attribute_languages.code_lang, attribute_languages.text_attribute "
////				+ " from attribute_languages ,  position, scheme, version "
////				+ " where code_object = 8 and code_attribute < 21 and "
////				+ "        attribute_languages.id_object =  position.id and  "
////				+ "        position.id = scheme.id_position and  "
////				+ "        scheme.id_version = version.id and "
////				+ "        version.id_class = :IDC";
////		
////		Stream<Object[]> stream = JPA.getUtil("old").getEntityManager().createNativeQuery(sql) 
////				.setParameter("IDC", idClass) //				
////				.setHint(QueryHints.HINT_FETCH_SIZE, 1000) 
////				.getResultStream();
////		
////		Iterator<Object[]> it = stream.iterator();
////		int cnt=0;
////		while (it.hasNext()) {
////			Object[] row = it.next();
////			//System.out.println(SearchUtils.clobToString((Clob)row[3]));
////			
////			Integer idObject = SearchUtils.asInteger(row[0]);
////			Integer lang = SearchUtils.asInteger(row[2]);
////			Integer codeAttr = SearchUtils.asInteger(row[1]);
////			String text = SearchUtils.clobToString((Clob)row[3]);
////			cnt++;
////			if ((double)cnt %  1000.0 == 0) {
////				System.out.println("Обработени:" + cnt);
////			}
////			//Systm.out.println(++cnt + ". " +text + "   ****    " + codeAttr);
////			
////			HashMap<Integer, PositionLang> tekMap = result.get(idObject);
////			if (tekMap == null) {
////				tekMap = new HashMap<Integer, PositionLang>();
////			}
////			PositionLang lo = tekMap.get(lang); 
////			if (lo == null) {
////				lo = new PositionLang();
////				lo.setLang(lang);
////				Position p = new Position();
////				p.setId(idObject);
////				lo.setPosition(p);
////			}
////						
////			switch(codeAttr) {
////			  case 11: //CODE_ATTRIB_OFFICIAL_TITLE
////				lo.setOffitialTitile(text);
////			    break;
////			  case 12: //CODE_ATTRIB_SHORT_TITLE
////			    lo.setShortTitle(text);
////			    break;
////			  case 13: //CODE_ATTRIB_MEDIUM_TITLE
////			   lo.setLongTitle(text);
////			    break;  
////			  case 14: //CODE_ATTRIB_ALTERNATE_TITLE
////			    lo.setAlternativeNames(text);
////			    break;				    
////			  case 15: //CODE_ATTRIB_COMMENT
////				lo.setComment(text);
////				break;
////			  case 16: //CODE_ATTRIB_INCLUDES
////				lo.setIncludes(text);
////				break;
////			  case 17: //CODE_ATTRIB_ALSO_INCLUDES
////				lo.setAlsoIncludes(text);
////				break;
////			  case 18: //CODE_ATTRIB_EXCLUDES
////				lo.setExcludes(text);
////				break;
////			  case 19: //CODE_ATTRIB_CASE_LAW
////				lo.setRules(text);
////				break;
////			  
////			 
////			}
////			tekMap.put(lang, lo);
////			result.put(idObject, tekMap);
////			
////		}
////		
////		System.out.println("--------------------------------------" + result.size());
////		return result;
////		
////	}
//	
//	
//	
//	@SuppressWarnings("unchecked")
//	public static void  transferClob() throws DbErrorException{
//		
//		System.out.println("Start at " + new Date());
//		//String sql = "select id_object, code_attribute, code_lang, text_attribute from attribute_languages where code_object = 8 and id_object in (select id from POSITION where id in (select id_position from scheme where id_version in (select id from version where id_class = :IDC )))";
//		
//		try {
//			String sql = "select id, code_object, id_object, code_attribute, code_lang, text_attribute from attribute_languages where id > 609048  order by id";
//					
//			
//			Stream<Object[]> stream = JPA.getUtil("old").getEntityManager().createNativeQuery(sql)
//					.setHint(QueryHints.HINT_FETCH_SIZE, 500) 
//					.getResultStream();
//			
//			Iterator<Object[]> it = stream.iterator();
//			int cnt=0;
//			JPA.getUtil().begin();
//			while (it.hasNext()) {
//				Object[] row = it.next();
//				
//				String text = SearchUtils.clobToString((Clob)row[5]);
//				
//				Query q = JPA.getUtil().getEntityManager().createNativeQuery("INSERT INTO attribute_languages(id, code_object, id_object, code_attribute, code_lang, text_attribute) 	VALUES(?, ?, ?, ?, ?, ?)");
//				
//				q.setParameter(1,row[0]);				
//				q.setParameter(2,row[1]);
//				q.setParameter(3,row[2]);	
//				q.setParameter(4,row[3]);
//				q.setParameter(5,row[4]);
//				q.setParameter(6, text);
//				q.executeUpdate();	
//				
//				cnt++;
//				if ((double)cnt %  500.0 == 0) {
//					System.out.println("Обработени:" + cnt + " " + new Date());
//					JPA.getUtil().commit();
//					JPA.getUtil().begin();
//				}
//				
//				//break;
//			}
//			JPA.getUtil().commit();
//		} catch (DbErrorException e) {			
//			e.printStackTrace();
//			JPA.getUtil().rollback();
//			throw new DbErrorException("bumbum", e);
//		}finally {
//			JPA.getUtil().closeConnection();
//		}
//		
//		System.out.println("--------------------------------------");
//		
//	}
//	
//	//INSERT INTO POSITION_LANG(ID, POSITION_ID, LANG, OFFICIAL_TITLE) 
//	//		select next value for SEQ_POSITION_LANG, id_object, code_lang, text_attribute from attribute_languages where code_object = 8 and code_attribute = 11 and id_object in (select id from position)
//
//
//	public static void doClassifSchemes(Integer idClassif) throws DbErrorException {
//		
//		Query q = JPA.getUtil("old").getEntityManager().createNativeQuery("select id from version where id_class = :IDC");
//		q.setParameter("IDC", idClassif);
//		
//			
//		ArrayList<Object> verIds = (ArrayList<Object>) q.getResultList();
//		for (Object o : verIds) {
//			doVersionScheme(SearchUtils.asInteger(o));
//		}
//		
//		//bulkInsertPositions(idClassif);
//		
//	}
	
	
	

}
