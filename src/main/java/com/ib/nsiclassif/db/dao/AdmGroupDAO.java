package com.ib.nsiclassif.db.dao;

import java.util.List;

import com.ib.nsiclassif.db.dto.AdmGroup;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;
import com.ib.system.exceptions.DbErrorException;

/**
 * DAO for {@link AdmGroup}    
 *  Автор Г.Белев
 */
public class AdmGroupDAO extends AbstractDAO<AdmGroup> {

	/** @param user */
	public AdmGroupDAO(ActiveUser user) {
		super(AdmGroup.class, user);
	}

	/**
	 * Намира идентификация на група [0]-ИД, [1]-Име
	 *
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findGroupsIdent() throws DbErrorException {
		try {
			return createNativeQuery("select GROUP_ID, GROUP_NAME from ADM_GROUPS").getResultList();
		} catch (Exception e) {
			throw new DbErrorException(e);
		}
	}
}