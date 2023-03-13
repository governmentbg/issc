package com.ib.nsiclassif.search;


import java.util.HashMap;
import java.util.Map;

import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JPA;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

/**
 * Търсене на статистически класификации
 *
 * @author mamun
 */
/**
 * @author vassil
 *
 */
public class ClassificationSearch extends SelectMetadata implements AuditExt {

	

	/**  */
	private static final long serialVersionUID = -4363991799689317766L;

	
	@JournalAttr(label = "classType", defaultText = "Тип класификация", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_TYPE)
	private Integer classType;
	
	
	@JournalAttr(label = "classUnit", defaultText = "Класификационна единица", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_UNIT)
	private Integer classUnit;
	
	
	@JournalAttr(label = "family", defaultText = "Семейство/Подсемейство", classifID = "" + NSIConstants.CODE_CLASSIF_CLASSIFICATION_FAMILY)
	private Integer family;
	
	
	@JournalAttr(label = "ident", defaultText = "Идентификатор на класификация")
	private String ident;
	
	
	@JournalAttr(label = "nameClassif", defaultText = "Име на класификация")
	private String nameClassif;
	
	@JournalAttr(label = "nameVersion", defaultText = "Име на класификация")
	private String nameVersion;
	
	@JournalAttr(label = "namePosition", defaultText = "Име на класификация")
	private String namePosition;
	
	@JournalAttr(label="lang",defaultText = "Език", classifID = "" + NSIConstants.CODE_CLASSIF_LANG)	
	private Integer lang;
	

	private StringBuilder sqlSelect ;
	private StringBuilder sqlFrom ;
	private StringBuilder sqlWhere ;
	private Map<String, Object> sqlParams;
	
	
	// като са с еднакви имена няма да го има 2 пъти в xml-a
	@JournalAttr(label="sql",defaultText = "SQL зявка")
	private String	sql;
	@JournalAttr(label="sqlCount",defaultText = "SQL зявка за брой")
	private String	sqlCount;
	
	/** */
	public ClassificationSearch() {		
		super();
		this.lang = NSIConstants.CODE_DEFAULT_LANG;
	}
	
	public ClassificationSearch(Integer lang) {
		this.lang = lang;
	}
	

	/** */
	@Override
	public void setSql(String sql) {
		this.sql = sql;
		super.setSql(sql);
	}
	/** */
	@Override
	public void setSqlCount(String sqlCount) {
		this.sqlCount = sqlCount;
		super.setSqlCount(sqlCount);
	}

	
	

	

	/**
	 * Използва се от основния екран за търсене на преписки и дела <br>
	 * На база входните параметри подготвя селект за получаване на резултат от вида: <br>
	 * [0]-DELO_ID (DVIJ_ID - само ако this.dvijNotReturned==true)<br>
	 * [1]-RN_DELO<br>
	 * [2]-DELO_DATE<br>
	 * [3]-DELO_TYPE<br>
	 * [4]-STATUS<br>
	 * [5]-CODE_REF_LEAD<br>
	 * [6]-DELO_NAME (String)<br>
	 * [7]-LOCK_USER<br>
	 * [8]-LOCK_DATE<br>
	 * [9]-WITH_TOM<br>
	 * [10]-BR_TOM<br>
	 * [11]-DELO_ID 	- само ако this.dvijNotReturned==true<br>
	 * [12]-DVIJ_METHOD - само ако this.dvijNotReturned==true<br>
	 * [13]-DVIJ_TEXT 	- само ако this.dvijNotReturned==true<br>
	 * [14]-DVIJ_DATE 	- само ако this.dvijNotReturned==true<br>
	 *
	 * @param userData това е този които изпълнява търсенето
	 * @param mode : 0-актуализация, 1-разглеждане, 2-пререгистрация, 3-съхранение, 4-прехвърляне в друга регистратура
	 */
	public void buildQuery() {
		
		Map<String, Object> params = new HashMap<>();

		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();
		
		String dialect = JPA.getUtil().getDbVendorName();
		
		select.append("select CLASSIFICATION.ID, ISNULL(lang1.IDENT, deff.IDENT) ident, ISNULL(lang1.NAME_CLASSIF, deff.NAME_CLASSIF) namec,  FAMILY ");
		from.append(" from CLASSIFICATION ");
		from.append(" left outer join CLASSIFICATION_LANG lang1 on lang1.CLASSIFICATION_ID = CLASSIFICATION.id  and lang1.lang = :LANG");
		from.append(" left outer join CLASSIFICATION_LANG deff on deff.CLASSIFICATION_ID = CLASSIFICATION.id  and deff.lang = :DEFLANG");
		
		params.put("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
		if (lang == null) {
			params.put("LANG", NSIConstants.CODE_DEFAULT_LANG);
		}else {
			params.put("LANG", lang);
		}
		
		where.append(" where 1=1 ");
		
		if (family != null) {
			where.append(" and CLASSIFICATION.family = :fam");
			params.put("fam", family);
		}
		
		if (classType != null) {
			where.append(" and CLASSIFICATION.CLASS_TYPE = :TYPE");
			params.put("TYPE", classType);
		}
		
		if (classUnit != null) {
			where.append(" and CLASSIFICATION.CLASS_UNIT = :UNIT");
			params.put("UNIT", classUnit);
		}
		
		if (ident != null && ident.trim().length() > 0) {
			where.append(" and CLASSIFICATION.id in (select distinct CLASSIFICATION_ID from CLASSIFICATION_LANG where upper(IDENT) like :IDENT)");
			params.put("IDENT", "%" + ident.trim().toUpperCase() + "%");
		}
		
		if (nameClassif != null && nameClassif.trim().length() > 0) {
			where.append(" and CLASSIFICATION.id in (select distinct CLASSIFICATION_ID from CLASSIFICATION_LANG where upper(NAME_CLASSIF) like :NAMEC)");
			params.put("NAMEC", "%" + nameClassif.trim().toUpperCase() + "%");
		}
		
		setSqlCount(" select count(distinct CLASSIFICATION.ID) " + from.toString() + where.toString()); // на този етап бройката е готова

		
		String queryStr=select.toString() + from.toString() + where.toString();
		setSql(queryStr);
		System.out.println("SQL String: "+ queryStr);

		setSqlParameters(params);
		
	}
	
	
	
	

	
	
	

	
	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		SystemJournal dj = new  SystemJournal();				
		dj.setCodeObject(NSIConstants.CODE_ZNACHENIE_JOURNAL_CLASSIF);
		//dj.setIdObject(getId());
		//dj.setIdentObject("Търсене на документи");
		return dj;
	}

	public Integer getClassType() {
		return classType;
	}

	public void setClassType(Integer classType) {
		this.classType = classType;
	}

	public Integer getClassUnit() {
		return classUnit;
	}

	public void setClassUnit(Integer classUnit) {
		this.classUnit = classUnit;
	}

	public Integer getFamily() {
		return family;
	}

	public void setFamily(Integer family) {
		this.family = family;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getNameClassif() {
		return nameClassif;
	}

	public void setNameClassif(String nameClassif) {
		this.nameClassif = nameClassif;
	}

	public String getNameVersion() {
		return nameVersion;
	}

	public void setNameVersion(String nameVersion) {
		this.nameVersion = nameVersion;
	}

	public String getNamePosition() {
		return namePosition;
	}

	public void setNamePosition(String namePosition) {
		this.namePosition = namePosition;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Map<String, Object> getSqlParams() {
		return sqlParams;
	}

	public void setSqlParams(Map<String, Object> sqlParams) {
		this.sqlParams = sqlParams;
	}

	public String getSql() {
		return sql;
	}

	public String getSqlCount() {
		return sqlCount;
	}




}