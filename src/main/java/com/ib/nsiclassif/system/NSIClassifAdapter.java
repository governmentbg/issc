package com.ib.nsiclassif.system;

import static com.ib.system.utils.SearchUtils.asInteger;
import static com.ib.system.utils.SearchUtils.asString;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import com.ib.docu.system.DocuConstants;
import com.ib.system.BaseSystemData;
import com.ib.system.SysClassifAdapter;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.DateUtils;
import com.ib.system.utils.SysClassifUtils;

/**
 * Конкретния адаптер за динамични класификации
 *
 * @author belev
 */



public class NSIClassifAdapter extends SysClassifAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(NSIClassifAdapter.class);

	
	NSIClassifAdapter(BaseSystemData sd) {
		super(sd);
	}

	/**
	 * Семейства и подсемейства - обединена
	 *
	 * @param codeClassif
	 * @param lang
	 * @return
	 * @throws DbErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<SystemClassif> buildFamilySubfamily(Integer codeClassif, Integer lang) throws DbErrorException {
		LOGGER.debug("buildFamilySubfamily(codeClassif={},lang={})", codeClassif, lang);

		List<SystemClassif> classif = new ArrayList<>();
		HashMap<Integer, String> names = new HashMap<Integer, String>(); 
		
		try {
			Date systemMinDate = DateUtils.systemMinDate();

			// за тест и за бързодействие го правя на Stream като е доста важно да има QueryHints.HINT_FETCH_SIZE, за да се получи
			// реален ефект от цялата игра
			
			ArrayList<Object[]> rows = (ArrayList<Object[]>) JPA.getUtil().getEntityManager().createNativeQuery("select id, code, CODE_CLASSIF, CODE_PARENT, CODE_PREV, TEKST, LEVEL_NUMBER from V_SYSTEM_CLASSIF where CODE_CLASSIF = "+NSIConstants.CODE_CLASSIF_CLASSIFICATION_FAMILY_SIMPLE+" and LANG = "+ lang).getResultList();
			
			for (Object[] row : rows) {
				

				SystemClassif item = new SystemClassif();

				item.setId(asInteger(row[0]));
				item.setCode(asInteger(row[1]));
				item.setCodeClassif(codeClassif);
				item.setCodeParent(asInteger(row[3]));
				item.setCodePrev(asInteger(row[4]));								
				item.setTekst(asString(row[5]));
				item.setLevelNumber(asInteger(row[6]));
				item.setDateOt(systemMinDate);
				item.setDateReg(systemMinDate);
				
				classif.add(item);
				
				
				
			}
			
			SysClassifUtils.doSortClassifPrev(classif);
			for (SystemClassif item : classif) {
				
				String name = names.get(item.getCodeParent());
				if (name != null) {
					item.setTekst(name+ "\\" + item.getTekst() );
				}
				
				names.put(item.getCode(), item.getTekst());
			}
			
			
		} catch (Exception e) {
			throw new DbErrorException(e);
		}
		return classif;
	}
	
	
	
}