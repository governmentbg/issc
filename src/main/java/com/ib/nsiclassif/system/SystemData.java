package com.ib.nsiclassif.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.system.BaseSystemData;




/**
 * Конкретната за системата
 */
public class SystemData extends BaseSystemData {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4021105466267422500L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SystemData.class);
	
	
	/** @see BaseSystemData#createDynamicAdapterInstance() */
	@Override
	protected Object createDynamicAdapterInstance() {
		return new NSIClassifAdapter(this);
	}

	
}