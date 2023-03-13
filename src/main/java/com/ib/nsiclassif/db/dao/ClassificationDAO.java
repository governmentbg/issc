package com.ib.nsiclassif.db.dao;

import static com.ib.system.utils.SearchUtils.asInteger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.Constants;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.ClassificationLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.system.utils.SearchUtils;

/**
 * DAO for {@link Classification}
 *
 * @author dessy
 */
public class ClassificationDAO extends AbstractDAO<Classification> {
	
	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationDAO.class);

	/** @param user */
	public ClassificationDAO(ActiveUser user) {
		super(user);
		// TODO Auto-generated constructor stub
	}	

	/**
	 * Разкодира наименование на идентификатор 
	 * 
	 * @param idClassif - идентификатор на класификация
	 * @param lang - език
	 * @return String
	 * @throws DbErrorException
	 */
	public String decodeClassifIdent(Integer idClassif, Integer lang) throws DbErrorException {

		try {

			String sql = " SELECT ISNULL(CL.IDENT, DEFCL.IDENT) IDENT " 
					+ " FROM CLASSIFICATION C "
					+ " LEFT OUTER JOIN CLASSIFICATION_LANG CL ON C.ID = CL.CLASSIFICATION_ID AND CL.LANG = :LANG "
					+ " LEFT OUTER JOIN CLASSIFICATION_LANG DEFCL ON C.ID = DEFCL.CLASSIFICATION_ID AND DEFCL.LANG = :DEFLANG "
					+ " WHERE C.ID = :IDCLASSIF ";

			Query q = JPA.getUtil().getEntityManager().createNativeQuery(sql);
			q.setParameter("LANG", lang);
			q.setParameter("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
			q.setParameter("IDCLASSIF", idClassif);
			
			String ident = "";
			
			@SuppressWarnings("unchecked")
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
			
			if(result.size()>0) {
				ident = (String) q.getSingleResult();
			}
			
			return ident;
			
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на идентификатор на класификация", e);
			throw new DbErrorException("Грешка при извличане на идентификатор на класификация", e);
		}

	}
	
	
	@Override
	public void delete(Classification entity) throws DbErrorException, ObjectInUseException {
		
		
		Integer cnt;

		try {
			cnt = asInteger( 
				createNativeQuery("select count(id) as cnt from VERSION where ID_CLASS = ?1") //
					.setParameter(1, entity.getId()) //					
					.getResultList().get(0));
			if (cnt != null && cnt.intValue() > 0) {
				throw new ObjectInUseException("Класификация "+entity.getIdentInfo()+" има версии и не може да бъде изтрита!");
			}
		} catch (ObjectInUseException e) {
			throw e; // за да не се преопакова
		} catch (Exception e) {
			throw new DbErrorException("Грешка при търсене на свързани обекти към класификация!", e);
		}

		
    	
    	ObjectDocsDAO docdao = new ObjectDocsDAO(getUser());
    	ArrayList<Object[]> docs = docdao.findObjectDocsNative(entity.getCodeMainObject(), entity.getId(), NSIConstants.CODE_DEFAULT_LANG);
    	for (Object[] tek : docs){
    		Integer id = SearchUtils.asInteger(tek[0]);
    		docdao.deleteById(id);
    	}
    	
    	ObjectUsersDAO userdao = new ObjectUsersDAO(getUser());
    	ArrayList<Object[]> users = userdao.findObjectUsersNative(entity.getCodeMainObject(), entity.getId(), NSIConstants.CODE_DEFAULT_LANG);
    	for (Object[] tek : users){
    		Integer id = SearchUtils.asInteger(tek[0]);
    		userdao.deleteById(id);
    	}
    	
    	
    	super.delete(entity);
    	
    }
	
	public void deleteExportedFiles(String path) {
		
		File folder= new File(path);
		
		String[] entries = folder.list();
		if (entries!=null) {
			for(String s: entries){
			    File currentFile = new File(folder.getPath(),s);
			    String[] entries2 = currentFile.list();
			    if (entries2!=null) {
			    	for(String s2: entries2){
					    File currentFile2 = new File(currentFile.getPath(),s2);
				    	currentFile2.delete();
					}	
				}
			    currentFile.delete();
			}
		}
		folder.delete();
	}
	
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public  List<Classification> findAllSite(Integer lang) throws DbErrorException {
		LOGGER.debug("Търсят се всички обекти от тип:{}", "Classification");
		try {
			Query q = JPA.getUtil().getEntityManager().createQuery("select c from Classification c join fetch ClassificationLang cl on cl.classif.id = c.id where c.finalized = :FIN and cl.lang = :LANG order by cl.ident ASC");
			q.setParameter("FIN", Constants.CODE_ZNACHENIE_DA);
			q.setParameter("LANG", lang);
			
			return  q.getResultList();
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане класификации сайт", e);
			throw new DbErrorException(e);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public  List<Classification> findAllSiteNew(Integer lang) throws DbErrorException {
		LOGGER.debug("Търсят се всички обекти от тип:{}", "Classification");
		try {
			
			
			HashMap<Integer, Classification> classifMap = new HashMap<Integer, Classification>();
			
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery("select c.ID, LANG, IDENT, NAME_CLASSIF from CLASSIFICATION c join CLASSIFICATION_LANG cl  on c.ID = cl.CLASSIFICATION_ID where FINALIZED = 1 ");
			ArrayList<Object[]> result = (ArrayList<Object[]>) q.getResultList();
			for (Object[] row : result) {
				String ident = SearchUtils.asString(row[2]);
				String name = SearchUtils.asString(row[3]);
				Integer cId = SearchUtils.asInteger(row[0]);
				Integer llang = SearchUtils.asInteger(row[1]);
				
				Classification classif = classifMap.get(cId);
				if (classif == null) {
					//Нова класификация 
					classif = new Classification();
					classif.setId(cId);
					classif.setLangMap(new HashMap<Integer,ClassificationLang>());
					
					ClassificationLang cl = new ClassificationLang();
					cl.setLang(llang);
					cl.setClassif(classif);
					cl.setIdent(ident);
					cl.setNameClassif(name);
					classif.getLangMap().put(llang, cl);
					
					classifMap.put(cId, classif);
					
				}else {
					ClassificationLang cl = new ClassificationLang();
					cl.setLang(llang);
					cl.setClassif(classif);
					cl.setIdent(ident);
					cl.setNameClassif(name);
					classif.getLangMap().put(llang, cl);
					
				}
				
			}
			
			List<Classification> classifList = new ArrayList<Classification>();
			classifList.addAll(classifMap.values());
			Collections.sort(classifList, new SiteComparator(lang));
			
			return classifList;
			
		} catch (Exception e) {
			LOGGER.debug("Грешка при извличане класификации сайт", e);
			throw new DbErrorException(e);
		}
	}
	
	
	 class SiteComparator implements Comparator<Classification> {
		    Integer lang = 1;
		    
		    public SiteComparator(Integer lang ) {
		    	this.lang = lang;
		    }

			@Override
			public int compare(Classification o1, Classification o2) {
				if (o1 == null && o2 != null) {
					return 1;
				}
				
				if (o1 != null && o2 == null) {
					return -1;
				}
				
				if (o1 == null && o2 == null) {
					return 0;
				}
				
				String ident1 = "";
				String ident2 = "";
				
				if (o1.getLangMap().get(lang) != null && o1.getLangMap().get(lang).getIdent() != null) {
					ident1 = o1.getLangMap().get(lang).getIdent();
				}
				
				if (o2.getLangMap().get(lang) != null && o2.getLangMap().get(lang).getIdent() != null) {
					ident2 = o2.getLangMap().get(lang).getIdent();
				}
				
				return ident1.compareTo(ident2);
				
				
			}
	 }
	
}
