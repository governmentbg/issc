package com.ib.nsiclassif.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.component.FacesComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.ib.indexui.CompObjAuditSys;
import com.ib.indexui.report.uni.SprObject;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.Level;
import com.ib.nsiclassif.db.dto.ObjectDocs;
import com.ib.nsiclassif.db.dto.ObjectUsers;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.Relation;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.ObjectComparator;
import com.ib.nsiclassif.system.SystemData;
import com.ib.system.ObjectsDifference;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.utils.JAXBHelper;

/** */
@FacesComponent(value = "compObjAudit", createTag = true)
public class CompObjAudit extends CompObjAuditSys {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CompObjAudit.class);

	
	/**
	 * Зарежда текущите разликите - сегашно и предишно състояние
	 * @param selectedEvent
	 */
	@Override
	public List<ObjectsDifference> loadCurrentDiff(SystemJournal currentEventTmp,SystemJournal previousEventTmp) {
		List<ObjectsDifference> compareResult=new ArrayList<ObjectsDifference>(); 
		
		
		LOGGER.info("LoadCurrentDiff between {} and {}",currentEventTmp!=null?currentEventTmp.getId():null,previousEventTmp!=null?previousEventTmp.getId():null);

		try {
			
//			Object xmlToObject2 = JAXBHelper.xmlToObject2(getSelectedEvent().getObjectXml());
//			System.out.println("==========================="+xmlToObject2.getClass());
			
			Object currentObj=null,prevObj=null;
			Integer codeObject=currentEventTmp!=null?currentEventTmp.getCodeObject():previousEventTmp.getCodeObject();
			Integer codeAction=currentEventTmp!=null?currentEventTmp.getCodeAction():previousEventTmp.getCodeAction();
			
			if (codeAction != null && codeAction.equals(NSIConstants.CODE_DEIN_UNISEARCH)) {
				if (previousEventTmp.getObjectXml() != null) {
					SprObject spr = JAXBHelper.xmlToObject(SprObject.class, previousEventTmp.getObjectXml());
					
					return convertSprObjectToDifferences(spr, getSystemData(), previousEventTmp.getDateAction()) ;
				}
			}
			
			
			switch (codeObject) {
				case NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_CLASSIF:
					currentObj=currentEventTmp!=null?JAXBHelper.xmlToObject(Classification.class, currentEventTmp.getObjectXml()):new Classification();
					prevObj=previousEventTmp!=null?JAXBHelper.xmlToObject(Classification.class, previousEventTmp.getObjectXml()):new Classification();
					break;
				case NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION:
					currentObj=currentEventTmp!=null?JAXBHelper.xmlToObject(Version.class, currentEventTmp.getObjectXml()):new Version();
					prevObj=previousEventTmp!=null?JAXBHelper.xmlToObject(Version.class, previousEventTmp.getObjectXml()):new Version();
					break;
				case NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_LEVEL:
					currentObj=currentEventTmp!=null?JAXBHelper.xmlToObject(Level.class, currentEventTmp.getObjectXml()):new Level();
					prevObj=previousEventTmp!=null?JAXBHelper.xmlToObject(Level.class, previousEventTmp.getObjectXml()):new Level();
					break;
				case NSIConstants.CODE_ZNACHENIE_JOURNAL_CORESP_TABLE:
					currentObj=currentEventTmp!=null?JAXBHelper.xmlToObject(CorespTable.class, currentEventTmp.getObjectXml()):new CorespTable();
					prevObj=previousEventTmp!=null?JAXBHelper.xmlToObject(CorespTable.class, previousEventTmp.getObjectXml()):new CorespTable();
					break;
				case NSIConstants.CODE_ZNACHENIE_JOURNAL_RELATION:
					currentObj=currentEventTmp!=null?JAXBHelper.xmlToObject(Relation.class, currentEventTmp.getObjectXml()):new Relation();
					prevObj=previousEventTmp!=null?JAXBHelper.xmlToObject(Relation.class, previousEventTmp.getObjectXml()):new Relation();
					break;
				case NSIConstants.CODE_ZNACHENIE_JOURNAL_POSITION:
					currentObj=currentEventTmp!=null?JAXBHelper.xmlToObject(PositionS.class, currentEventTmp.getObjectXml()):new PositionS();
					prevObj=previousEventTmp!=null?JAXBHelper.xmlToObject(PositionS.class, previousEventTmp.getObjectXml()):new PositionS();
					break;
				case NSIConstants.CODE_ZNACHENIE_JOURNAL_OBJECT_DOC:
					currentObj=currentEventTmp!=null?JAXBHelper.xmlToObject(ObjectDocs.class, currentEventTmp.getObjectXml()):new ObjectDocs();
					prevObj=previousEventTmp!=null?JAXBHelper.xmlToObject(ObjectDocs.class, previousEventTmp.getObjectXml()):new ObjectDocs();
					break;
				case NSIConstants.CODE_ZNACHENIE_JOURNAL_OBJECT_USER:
					currentObj=currentEventTmp!=null?JAXBHelper.xmlToObject(ObjectUsers.class, currentEventTmp.getObjectXml()):new ObjectUsers();
					prevObj=previousEventTmp!=null?JAXBHelper.xmlToObject(ObjectUsers.class, previousEventTmp.getObjectXml()):new ObjectUsers();
					break;
				default:
					LOGGER.error("Object code="+currentEventTmp.getCodeObject()+" not implemented");
					break;
			}
//			Doc currentDoc = JAXBHelper.xmlToObject(Doc.class, getSelectedEvent().getObjectXml());
//			Doc prevDoc = previousEventTmp!=null?JAXBHelper.xmlToObject(Doc.class, previousEventTmp.getObjectXml()):new Doc();
//			
			 compareResult = new ObjectComparator(
					previousEventTmp!=null?previousEventTmp.getDateAction():new Date(),
					currentEventTmp!=null?currentEventTmp.getDateAction():new Date(),
							(SystemData) JSFUtils.getManagedBean("systemData"), 
							null).compare( prevObj,currentObj);
			 
			 
			
			 
			
		} catch (Exception e1) {
			LOGGER.error("",e1);
			e1.printStackTrace();
		} 
		
		return compareResult;

	}
	


	
	
}