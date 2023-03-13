package com.ib.nsiclassif.system;

import java.util.Date;

import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.system.BaseObjectComparator;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemJournal;

public class ObjectComparator extends BaseObjectComparator {

	public ObjectComparator(Date oldDate, Date newDate, SystemData sd) {
		super(oldDate, newDate, sd);
		// TODO Auto-generated constructor stub
	}
	
	public ObjectComparator(Date oldDate, Date newDate, SystemData sd, Integer lang) {
		super(oldDate, newDate, sd, lang);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String formatVal(Object o, String codeClassif, int codeObject, Date dat) {
		//System.out.println("CC=" + codeClassif);
		
		Integer lang = NSIConstants.CODE_DEFAULT_LANG;
		
		if (codeObject > 0) {
			
			
			
			String ident = null;
			try {				
				Integer id = Integer.parseInt(""+o);
				
				switch(codeObject) {
				  case NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_CLASSIF:
					  Classification classif = JPA.getUtil().getEntityManager().find(Classification.class, id); // new DeloDAO(ActiveUser.DEFAULT).findById(id);
				    if (classif != null) {
				    	ident = classif.getLangMap().get(lang).getIdent();		    	
				    }
				    break;
				  case NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION:
					    Version version = JPA.getUtil().getEntityManager().find(Version.class, id); // new DocDAO(ActiveUser.DEFAULT).findById(id);
					    if (version != null) {
					    	ident =  version.getLangMap().get(lang).getIdent();		    	
					    }
					    break;
				  case NSIConstants.CODE_ZNACHENIE_JOURNAL_CORESP_TABLE:
					    CorespTable table = JPA.getUtil().getEntityManager().find(CorespTable.class, id);
					    if (table != null) {
					    	ident =  table.getLangMap().get(lang).getIdent();	    	
					    }
					    break;
				  
				  default:
					  break;
				}
				
				if (ident == null) {
					//Правим още един опит през журнала
					SystemJournal j = (SystemJournal) JPA.getUtil().getEntityManager().createQuery("from SystemJournal where codeObject =:co and idObject = :io and codeAction = :ca")
							.setParameter("co", codeObject)
							.setParameter("io", id)
							.setParameter("ca", SysConstants.CODE_DEIN_IZTRIVANE)
							.getSingleResult();
					if (j != null) {
						ident = j.getIdentObject();
					}
				}
				
				if (ident == null) {
					return "Id= " + id;
				}else {
					return ident + "(Id= " +id + ")";
				}
				
			} catch (Exception e) {
				return ident + " (Грешка при идентификация)";
			}

			
		}else {
			if (codeClassif == null || codeClassif.equalsIgnoreCase("none")) {
				return fromatSimpleVal(o);
			}else {
				return decodeVal(o,codeClassif, dat, lang);
			}
		}
		
		
	}
	
	
	
	
}
	


