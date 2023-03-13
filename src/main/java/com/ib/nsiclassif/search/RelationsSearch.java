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
 * 
 * Търсене на релации на кореспондиращи таблици
 *
 * @author s.marinov
 *
 */

public class RelationsSearch extends SelectMetadata {
	/**
	 * 
	 */
	private static final long serialVersionUID = -645341838661171480L;

	@JournalAttr(label = "corespTableId", defaultText = "Системен идентификатор на кореспондираща таблица")
	private Integer corespTableId;
	
	@JournalAttr(label = "corespTableId", defaultText = "Позиция от системна класификация източник")
	private String sourceCode;
	
	@JournalAttr(label = "corespTableId", defaultText = "Позиция от системна класификация цел")
	private String targetCode;
	
	@JournalAttr(label="lang",defaultText = "Език", classifID = "" + NSIConstants.CODE_CLASSIF_LANG)	
	private Integer lang;
	
	private Map<String, Object> sqlParams;
	
	private String searchCode;
	//1 - източник или цел
	//2 - източник
	//3 - цел
	private Integer searchLocation=1;
	
	
	// като са с еднакви имена няма да го има 2 пъти в xml-a
	@JournalAttr(label="sql",defaultText = "SQL зявка")
	private String	sql;
	@JournalAttr(label="sqlCount",defaultText = "SQL зявка за брой")
	private String	sqlCount;
	
	/** */
	public RelationsSearch() {		
		super();
		this.lang = NSIConstants.CODE_DEFAULT_LANG;
	}
	
	public RelationsSearch(Integer lang) {
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
	 * 
	 * Използва се за търсене на релации на кореспондиращи таблици<br>
	 * На база входните параметри подготвя селект за получаване на резултат от вида: <br>
	 * [0]-ID<br>
	 * [1]-SOURCE_CODE<br>
	 * [2]-TARGET_CODE<br>
	 * 
	 */
	public void buildQuery() {
		
		Map<String, Object> params = new HashMap<>();

		StringBuilder select = new StringBuilder();
		StringBuilder from = new StringBuilder();
		StringBuilder where = new StringBuilder();
		
		String dialect = JPA.getUtil().getDbVendorName();
		
		
			
		select.append("select distinct r.id, r.SOURCE_CODE, r.TARGET_CODE ");
		from.append(" from RELATION r");
		where.append(" where r.ID_TABLE=:IDTABLE ");
		if (searchCode!=null && !searchCode.equals("")) {			
		
			if (searchLocation==1) {
				where.append(" and (r.SOURCE_CODE like :searchCode or r.TARGET_CODE like :searchCode)");
			}else {
				if (searchLocation==2) {
					where.append(" and r.SOURCE_CODE like :searchCode");
				}else {
					where.append(" and r.TARGET_CODE like :searchCode");
				}
			}
			params.put("searchCode", searchCode.strip()+"%");
		}
		
		params.put("IDTABLE", corespTableId);
		
		
		 
		
		setSqlCount(" select count(distinct r.ID) " + from.toString() + where.toString()); // на този етап бройката е готова

		
		String queryStr=select.toString() + from.toString() + where.toString();
		setSql(queryStr);
		

		setSqlParameters(params);
		
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

	public Integer getCorespTableId() {
		return corespTableId;
	}

	public void setCorespTableId(Integer corespTableId) {
		this.corespTableId = corespTableId;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getTargetCode() {
		return targetCode;
	}

	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	public Integer getSearchLocation() {
		return searchLocation;
	}

	public void setSearchLocation(Integer searchLocation) {
		this.searchLocation = searchLocation;
	}

	public String getSearchCode() {
		return searchCode;
	}

	public void setSearchCode(String searchCode) {
		this.searchCode = searchCode;
	}




}