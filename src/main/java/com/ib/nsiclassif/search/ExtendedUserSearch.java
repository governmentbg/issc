package com.ib.nsiclassif.search;

import java.util.Map;

import com.ib.indexui.search.UserSearch;

public class ExtendedUserSearch extends UserSearch {
	/**  */
	private static final long serialVersionUID = -6734028006280514175L;
	
	private Integer blockUser;
	
	/** */
	public ExtendedUserSearch() {
		super();
	}
	
	/** */
	@Override
	protected void extendQueryUserList(StringBuilder select, StringBuilder from, StringBuilder where, Map<String, Object> params) {
		if (this.blockUser != null) {
			where.append(" and u.user_block = :block ");
			params.put("block", this.blockUser);          // Integer.valueOf(1)
		}
		
	}

	public Integer getBlockUser() {
		return blockUser;
	}

	public void setBlockUser(Integer blockUser) {
		this.blockUser = blockUser;
	}
	
	

}
