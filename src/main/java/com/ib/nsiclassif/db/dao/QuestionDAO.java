package com.ib.nsiclassif.db.dao;

import com.ib.nsiclassif.db.dto.Question;
import com.ib.system.ActiveUser;
import com.ib.system.db.AbstractDAO;

/**
 * DAO for {@link Question}
 * 
 */
public class QuestionDAO extends AbstractDAO<Question> {

	protected QuestionDAO(ActiveUser user) {
		super(user);
	}

}
