package com.ib.nsiclassif.system;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.system.BaseSystemData;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.SysClassifUtils;

public class TestHelloWorld {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestHelloWorld.class);

	
	@Test
	public void testHelloWorld (){
		LOGGER.info("info");
		LOGGER.warn("warn");
		LOGGER.debug("debug");
		LOGGER.error("error");
	}
	
	
	@Test
	public void testHelloSd (){
		
		SystemData sd = new SystemData();
		try {
			List<SystemClassif> classif = sd.getSysClassification(NSIConstants.CODE_CLASSIF_CLASSIFICATION_SLUJBI_LICA, new Date(), NSIConstants.CODE_DEFAULT_LANG);
			
			
			SysClassifUtils.doSortClassifPrev(classif);
			
			for (SystemClassif tek : classif) {
				for (int i = 0; i < tek.getLevelNumber(); i++) {
					System.out.print("\t");					
				}
				System.out.println(tek.getTekst() + "\t-\t" + tek.getCode());
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	

}
