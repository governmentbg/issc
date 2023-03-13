package com.ib.nsiclassif.search;

import java.util.HashMap;
import java.util.Map;

import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.db.AuditExt;
import com.ib.system.db.JournalAttr;
import com.ib.system.db.SelectMetadata;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.DbErrorException;

public class CorespTablesSearch extends SelectMetadata implements AuditExt {
	/**  */
	private static final long serialVersionUID = -6734558006280514175L;
	
	
	@JournalAttr(label="lang",defaultText = "Език", classifID = "" + NSIConstants.CODE_CLASSIF_LANG)	
	private Integer lang;
	
	@JournalAttr(label = "classificationId", defaultText = "Системен идентификатор на класификация")
	private Integer classificationId;
	
	@JournalAttr(label = "versionId", defaultText = "Системен идентификатор на версия")
	private Integer versionId;
	
	
	
	private StringBuilder sqlSelect ;
	private StringBuilder sqlFrom ;
	private StringBuilder sqlWhere ;
	private Map<String, Object> sqlParams;
	
	
	/** */
	public CorespTablesSearch() {
		super();
	}



	@Override
	public SystemJournal toSystemJournal() throws DbErrorException {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Използва се от основния екран за търсене на преписки и дела <br>
	 * На база входните параметри подготвя селект за получаване на резултат от вида: <br>
	 * [0]-ID <br>
	 * [1]-IDENT<br>
	 * [2]-NAME<br>
	 * [3]-ID_VERS_SOURCE<br>
	 * [4]-ID_VERS_TARGET<br>	
	 */
	public void buildQuery() {
		
		sqlParams = new HashMap<>();

		sqlSelect = new StringBuilder();
		sqlFrom = new StringBuilder();
		sqlWhere = new StringBuilder();
		
	
		
		sqlSelect.append("select TABLE_CORRESP.id, isnull(ctl.ident, deff.ident) ident, isnull(ctl.NAME, deff.NAME) name, ID_VERS_SOURCE, ID_VERS_TARGET ");
		
		sqlFrom.append(" from TABLE_CORRESP ");
		sqlFrom.append(" left outer join TABLE_CORRESP_LANG ctl on TABLE_CORRESP.id = ctl.TABLE_CORRESP_ID and ctl.lang = :LANG ");
		sqlFrom.append(" left outer join TABLE_CORRESP_LANG deff on TABLE_CORRESP.id = deff.TABLE_CORRESP_ID and deff.lang = :DEFLANG");
		
		sqlParams.put("DEFLANG", NSIConstants.CODE_DEFAULT_LANG);
		if (lang == null) {
			sqlParams.put("LANG", NSIConstants.CODE_DEFAULT_LANG);
		}else {
			sqlParams.put("LANG", lang);
		}
		
		sqlWhere.append(" where 1=1 ");
		
		if (versionId != null) {
			//sqlWhere.append(" AND ID_VERS_SOURCE = :VID or ID_VERS_TARGET = :VID ");
			sqlWhere.append(" AND ID_VERS_SOURCE = :VID "); //По искане на КК от 18.11.2022
			sqlParams.put("VID", versionId);
		}
		
		if (classificationId != null) {
			//sqlWhere.append(" and ID_VERS_SOURCE in (select id from VERSION where ID_CLASS = :CID) or ID_VERS_TARGET in (select id from VERSION where ID_CLASS = :CID)");
			sqlWhere.append(" and ID_VERS_SOURCE in (select id from VERSION where ID_CLASS = :CID)"); //По искане на КК от 18.11.2022
			sqlParams.put("CID", classificationId);
		}
		
		
		setSqlCount(" select count(distinct TABLE_CORRESP.ID) " + sqlFrom.toString() + sqlWhere.toString()); // на този етап бройката е готова

		
		String queryStr=sqlSelect.toString() + sqlFrom.toString() + sqlWhere.toString();
		setSql(queryStr);
		System.out.println("SQL String: "+ queryStr);

		setSqlParameters(sqlParams);
		
	}



	public Integer getLang() {
		return lang;
	}



	public void setLang(Integer lang) {
		this.lang = lang;
	}



	public Integer getClassificationId() {
		return classificationId;
	}



	public void setClassificationId(Integer classificationId) {
		this.classificationId = classificationId;
	}



	public Integer getVersionId() {
		return versionId;
	}



	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}
	
	
	

}
