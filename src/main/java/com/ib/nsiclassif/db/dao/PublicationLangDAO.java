package  com.ib.nsiclassif.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.nsiclassif.db.dto.PublicationLang;

/**
 * @author ivanc
 *
 */

public class PublicationLangDAO extends AbstractDAO<PublicationLang> {

	static final Logger LOGGER = LoggerFactory.getLogger(PublicationLangDAO.class);
	
	public PublicationLangDAO (ActiveUser user){
		
		super(user);		
	}
	
	@SuppressWarnings("unchecked")
	public List<PublicationLang> findByIdPubl(Integer idPub) throws DbErrorException {
		
		try {

			Query query = JPA.getUtil().getEntityManager().createQuery("FROM PublicationLang а WHERE а.pubId = :idPub ");

			query.setParameter("idPub", idPub);

			List<PublicationLang> pubLangs = query.getResultList();

			
			return pubLangs;
			
		} catch (Exception e) {
			throw new DbErrorException("Възникна грешка при извличане на езиковите версии на публикация!!!", e);
		}
		
	}	
	
	
	
	@SuppressWarnings("unchecked")
	public List<PublicationLang> findByIdPublSingleLang(Integer idPub, Integer lang) throws DbErrorException {
		try {

			Query query = JPA.getUtil().getEntityManager().createQuery("FROM PublicationLang а WHERE а.pubId = :idPub and а.lang = :lang ");

			query.setParameter("idPub", idPub);
			query.setParameter("lang", lang);

			List<PublicationLang> pubLangs = query.getResultList();

			
			return pubLangs;
			
		} catch (Exception e) {
			throw new DbErrorException("Възникна грешка при извличане на езиковите версии на публикация!!!", e);
		}
		
		
	}
	
	
	
	/** Извлича от БД на динамичен текст за началната страница на портала
	 * @param Integer codeSect - код на секция 
	 * @param dat - Date - текуща дата
	 * @param lang - Integer - Текущ език
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findPublLangBySect(Integer codeSect, Date dat, Integer lan) throws DbErrorException {
		
		StringBuffer sql = new StringBuffer();
		List<String> whereStr = new ArrayList<String>();
		String dialect = JPA.getUtil().getDbVendorName();

		if(null!=lan){
	    	whereStr.add("pubLang.lang=:langF");
		}
		if(null!=codeSect){
	    	whereStr.add("pub.section=:sectF");
		}
		
	    if (null!=dat){
	    	whereStr.add("pub.date_from <="+ DialectConstructor.convertDateOnlyToSQLString(dialect, dat) + 
	    				" AND (pub.date_to IS NULL OR pub.date_to >= "+DialectConstructor.convertDateOnlyToSQLString(dialect, dat)+")");
		}	

	    String strWhere=""; 
	    
		if (!whereStr.isEmpty()) {
			strWhere+=" WHERE ";
			for (int i = 0; i < whereStr.size(); i++) {	
				strWhere+=whereStr.get(i);
				if (i != (whereStr.size() - 1)) {
					strWhere+=" AND ";
				}
			}
		}
		
		/*sql.append("SELECT "
				+ "pubLang.lang a0, pubLang.title a1, pubLang.annotation a2, pubLang.full_text a3, "
				+ "pubLang.other_info a4, pubLang.url_pub a5, pubLang.publication_id a6 "
				+ "FROM "
				+ "jobs_publication_lang pubLang "
				+ "JOIN jobs_publication pub ON (pubLang.publication_id=pub.id) ");*/
		sql.append("SELECT "
				+ "pubLang.lang, pubLang.title, pubLang.annotation, pubLang.full_text, "
				+ "pubLang.other_info, pubLang.url_pub, pubLang.publication_id, pub.IMAGE_PUB, pub.section "
				+ "FROM "
				+ "PUBLICATION_LANG pubLang "
				+ "JOIN PUBLICATION pub ON (pubLang.publication_id=pub.id) ");
		sql.append(strWhere); 
		
		try {
			Query query = createNativeQuery(sql.toString());
			query.setParameter("langF", lan);
			query.setParameter("sectF", codeSect);
			

			List<Object[]> pubList = query.getResultList();

			if (null!=pubList && pubList.size() > 0) {
				return pubList;
			} else {
				return null;
			}

		} catch (Exception e) {
			throw new DbErrorException("Възникна грешка при извличане на текст Секция!!!", e);
		}
	}
	
	
}