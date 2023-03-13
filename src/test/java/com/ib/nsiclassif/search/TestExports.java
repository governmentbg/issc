package com.ib.nsiclassif.search;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.jpa.QueryHints;
import org.junit.Test;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dto.PositionLang;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.nsiclassif.db.dto.PositionUnits;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.utils.SearchUtils;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class TestExports {
	@Test
	public void createFiles() throws IOException {
//		1.чисто без да променяш нищо
//		2. URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
//		3. new String(fileName.getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_8);
//		4, new String(fileName.getBytes(), StandardCharsets.UTF_8);
		String fileName="КИД_-_2008.txt";

		FileOutputStream f1= new FileOutputStream(new File("D:\\TEMP\\FILES\\1"+fileName));
		f1.flush();
		f1.close();
		FileOutputStream f2= new FileOutputStream(new File("D:\\TEMP\\FILES\\2"+URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())));
		f2.flush();
		f2.close();
		FileOutputStream f3= new FileOutputStream(new File("D:\\TEMP\\FILES\\3"+new String(fileName.getBytes(StandardCharsets.UTF_8),StandardCharsets.UTF_8)));
		f3.flush();
		f3.close();
		FileOutputStream f4= new FileOutputStream(new File("D:\\TEMP\\FILES\\4"+new String(fileName.getBytes(), StandardCharsets.UTF_8)));
		f4.flush();
		f4.close();
		
	}
	
	@Test
	public void simpleExport(){
		try {
			long beg=new Date().getTime();
			
			StringBuffer SQL = new StringBuffer();
			SQL.append("SELECT DISTINCT ");
			SQL.append("    r.id, ");
			SQL.append("    r.SOURCE_CODE, ");
			SQL.append("    r.TARGET_CODE, ");
			SQL.append("    (select s.CODE_EXT from SYSTEM_CLASSIF s where s.CODE_CLASSIF=512 and s.code =r.EXPLANATION ) expl ");
			SQL.append(" FROM ");
			SQL.append("    RELATION r ");
			SQL.append(" WHERE ");
			SQL.append("    r.ID_TABLE=:idTable");
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
			q.setParameter("idTable", 1330);//prodprom 2001-2002
			 
			 
	
			List<Object[]> result = q.getResultList();
			
			StringBuilder sb = new StringBuilder();
			
			
			
			FileOutputStream f= new FileOutputStream(new File("..\\829.txt"));
			
			for (int i = 0; i < result.size(); i++) {
				sb.append(result.get(i)[1]==null?"":result.get(i)[1]);
				sb.append(" ;");
				sb.append(result.get(i)[2]==null?"":result.get(i)[2]);
				sb.append(" ;");
				sb.append(result.get(i)[3]==null?"":result.get(i)[3]);
				sb.append(System.lineSeparator());
				
			}
			sb.append("10.10");
			sb.append(" ;");
			sb.append("10.10");
			sb.append(" ;");			
			sb.append(System.lineSeparator());
			
			 

		      f.write(sb.toString().getBytes());
		      f.flush();
		      f.close();
		      
		      long end=new Date().getTime();
		      System.out.println("simpleExport done in: "+(end-beg)+"ms");

			    

			
		} catch (Exception e) {			
			e.printStackTrace();
			 
		}finally {
			JPA.getUtil().closeConnection();
		}
		
	}
	
//	@Test
//	public void openScvExport(){
//		try {
//			long beg=new Date().getTime();
//			
//			StringBuffer SQL = new StringBuffer();
//			SQL.append("SELECT DISTINCT ");
//			SQL.append("    r.id, ");
//			SQL.append("    r.SOURCE_CODE, ");
//			SQL.append("    r.TARGET_CODE, ");
//			SQL.append("    r.EXPLANATION ");
//			SQL.append(" FROM ");
//			SQL.append("    RELATION r ");
//			SQL.append(" WHERE ");
//			SQL.append("    r.ID_TABLE=:idTable");
//			
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
//			q.setParameter("idTable", 1085);//prodprom 2001-2002
//			
//			
//			
//			List<Object[]> result = q.getResultList();
//			
//			
//		        
//			
//			String csv = "D:\\test2.csv";
//            CSVWriter writer = new CSVWriter(new FileWriter(csv));
//            
//             
//            for (int i = 0; i < result.size(); i++) {
//            	String [] record = new String[3];
//            	record[0]=(String) result.get(i)[1];
//            	record[1]=(String) result.get(i)[2];
//            	record[2]=(String) result.get(i)[3];
//            	 writer.writeNext(record, false);
//			}
//            
//           
//            
//            writer.close();
//			
//			long end=new Date().getTime();
//			
//			System.out.println("openScvExport done in: "+(end-beg)+"ms");
//			
//			
//			
//			
//		} catch (Exception e) {			
//			e.printStackTrace();
//			
//		}finally {
//			JPA.getUtil().closeConnection();
//		}
//		
//	}
	
	
	@Test
	public void scrolableResultExport(){
		try {
			long beg=new Date().getTime();
			
			StringBuffer SQL = new StringBuffer();
			SQL.append("SELECT DISTINCT ");
			SQL.append("    r.id, ");
			SQL.append("    r.SOURCE_CODE, ");
			SQL.append("    r.TARGET_CODE, ");
			SQL.append("    r.EXPLANATION ");
			SQL.append(" FROM ");
			SQL.append("    RELATION r ");
			SQL.append(" WHERE ");
			SQL.append("    r.ID_TABLE=:idTable");
			
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
//			q.setParameter("idTable", 1085);//prodprom 2001-2002
//			
			
			
			StringBuilder sb = new StringBuilder();
		 	
			
			Stream<Object[]> stream = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString()).setParameter("idTable", 1085)
					.setHint(QueryHints.HINT_FETCH_SIZE, 500) 
					.getResultStream();
			
			Iterator<Object[]> it = stream.iterator();
			
			
			FileOutputStream f= new FileOutputStream(new File("D:\\test3.csv"));
			
			JPA.getUtil().begin();
			while (it.hasNext()) {
				Object[] row = it.next();
			
			
				sb.append(row[1]==null?"":row[1]);
				sb.append(',');
				sb.append(row[2]==null?"":row[2]);
				sb.append(',');
				sb.append(row[3]==null?"":row[3]);
				sb.append(System.lineSeparator());
				
			}
			 

		      f.write(sb.toString().getBytes());
		      f.flush();
		      f.close();
			
			long end=new Date().getTime();
			
			System.out.println("scrolableResultExport done in: "+(end-beg)+"ms");
			
			
			
			
		} catch (Exception e) {			
			e.printStackTrace();
			
		}finally {
			JPA.getUtil().closeConnection();
		}
		
	}
	
	
//	@Test
//	public void odfExport(){
//		try {
//			long beg=new Date().getTime();
//			
//			StringBuffer SQL = new StringBuffer();
//			SQL.append("SELECT DISTINCT ");
//			SQL.append("    r.id, ");
//			SQL.append("    r.SOURCE_CODE, ");
//			SQL.append("    r.TARGET_CODE, ");
//			SQL.append("    r.EXPLANATION ");
//			SQL.append(" FROM ");
//			SQL.append("    RELATION r ");
//			SQL.append(" WHERE ");
//			SQL.append("    r.ID_TABLE=:idTable");
//			
//			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
//			q.setParameter("idTable", 1085);//prodprom 2001-2002 - id=1085
//			 
//			 
//			
//	
//			List<Object[]> result = q.getResultList();
//			long beg2=new Date().getTime();
//			StringBuilder sb = new StringBuilder();
//			
//			
//			
//			
////			OdfDocument odt = OdfDocument.loadDocument(new File("D:\\odfExport.odt"));
//		    // Append text to the end of the document.
//			
//			 
//			
//			 
//		      OdfTextDocument odt = OdfTextDocument.newTextDocument();
//		      // Append text to the end of the document.
//		      
//			for (int i = 0; i < result.size(); i++) {
//				sb.append(result.get(i)[1]==null?"":result.get(i)[1]);
//				sb.append(',');
//				sb.append(result.get(i)[2]==null?"":result.get(i)[2]);
//				sb.append(',');
//				sb.append(result.get(i)[3]==null?"":result.get(i)[3]);
//				sb.append(System.lineSeparator());
//				
//			}
//			 
//			 
//			
//			System.out.println(sb.toString());
//			odt.addText(sb.toString());
//		      // Save document
//		    odt.save("D:\\odfExport.ods");
//		      
//		      long end=new Date().getTime();
//		      System.out.println("odfExport done in: "+(end-beg)+"ms");
//		      System.out.println("odfExport only write done in: "+(end-beg2)+"ms");
//
//			    
//
//			
//		} catch (Exception e) {			
//			e.printStackTrace();
//			 
//		}finally {
//			JPA.getUtil().closeConnection();
//		}
//		
//	}
	
	@Test
	public void odsExport(){
		try {
			long beg=new Date().getTime();
			
			StringBuffer SQL = new StringBuffer();
			SQL.append("SELECT DISTINCT ");
			SQL.append("    r.id, ");
			SQL.append("    r.SOURCE_CODE, ");
			SQL.append("    r.TARGET_CODE, ");
			SQL.append("    r.EXPLANATION ");
			SQL.append(" FROM ");
			SQL.append("    RELATION r ");
			SQL.append(" WHERE ");
			SQL.append("    r.ID_TABLE=:idTable");
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
			q.setParameter("idTable", 1330);//prodprom 2001-2002 - id=1085
			
			
			
			
			List<Object[]> result = q.getResultList();
			long beg2=new Date().getTime();
			StringBuilder sb = new StringBuilder();
			
			
			
			
			SpreadsheetDocument ods = SpreadsheetDocument.newSpreadsheetDocument();
		    Table table = Table.getInstance(ods.getTables().get(0));
//		    		table., result.size(), 3, 0, 0);
		    table.setTableName("Релации на Продпром 2001 - 2002");
		    //create style
//		    OdfOfficeAutomaticStyles astyles = ods.getContentDom().getOrCreateAutomaticStyles();
		    
		   
		    
//		    StyleStyleElement ele = astyles.newStyleStyleElement(OdfStyleFamily.TableCell.getName(), "myss");
//		    StyleTableCellPropertiesElement styleTableCellPropertiesElement = ele.newStyleTableCellPropertiesElement();
//		    styleTableCellPropertiesElement.setFoBackgroundColorAttribute("#A5A5A5");
//		    styleTableCellPropertiesElement.setFoBorderAttribute("1.0pt solid #000000");
//		    
//		    
//		    ele.newStyleParagraphPropertiesElement().setFoTextAlignAttribute(HorizontalAlignmentType.CENTER.toString());
//		    StyleTextPropertiesElement styleTextPropertiesElement = ele.newStyleTextPropertiesElement(null);
//		    styleTextPropertiesElement.setStyleFontNameAttribute("Arial");
//		    styleTextPropertiesElement.setFoFontSizeAttribute("7.0pt");
//		    styleTextPropertiesElement.setFoColorAttribute(Color.BLACK.toString());
//		    styleTextPropertiesElement.setFoFontWeightAttribute("bold");
//
//		     
//		    OdfStyle tableStyle = astyles.newStyle(OdfStyleFamily.Text);
//		    StyleTextPropertiesElement styleTextPropertiesElement1 = tableStyle.newStyleTextPropertiesElement(null);
//		    styleTextPropertiesElement1.setStyleFontNameAttribute("Tahoma");
//		    styleTextPropertiesElement1.setFoFontSizeAttribute("10pt");
//		    styleTextPropertiesElement1.setStyleFontNameAsianAttribute("Lucida Sans Unicode");

		    
		    
		    
		    int i=0;
		    for (int j = 0; j < result.size(); j++) {
				Row r=table.appendRow();		    	
	    	 	for (int a = 0; a < 3; a++) {
		            Cell cell = r.getCellByIndex(a);
		            cell.setStringValue(result.get(i)[a+1]==null?"":""+result.get(i)[a+1]);
//		            cell.setCellStyleName("myss");
		            cell.setValueType("string");
		        }

				i++;
		    }
			
			
			
			
			
			// Save document
			ods.save("D:\\odSSSExport.ods");
			System.out.println(ods.getContentStream().readAllBytes());
			
			long end=new Date().getTime();
			System.out.println("odfExport done in: "+(end-beg)+"ms");
			System.out.println("odfExport only write done in: "+(end-beg2)+"ms");
			
			
			
			
		} catch (Exception e) {			
			e.printStackTrace();
			fail();
		}finally {
			JPA.getUtil().closeConnection();
		}
		
	}
	public Integer idClassif=1354;
	public Integer idVersion=13;
	public String format="sdmx";
	
	@Test
	public void testPositions() {
		try {
			
			
			
			
//			Integer idClassif=549;
//			Integer idVersion=743;
			
			// *********ATRIBUTITE DOPUSTIMI ZA KLASIFIKACIQTA - TOVA VEROQTNO SHTE SE PODAVA OT BEANOVETE AMA ZA TESTA TAKA.
			Map <Integer,Boolean> schemePosAttr = new HashMap<Integer, Boolean>();
			Map <Integer,String> schemePosAttrLabels = new HashMap<Integer, String>();
			
			
			List<Integer> listAttr = new ArrayList<Integer>();
			
			listAttr = new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idClassif);
			
			schemePosAttr.clear();
			
			for(Integer attr : listAttr) {
				schemePosAttr.put(attr, Boolean.TRUE);
			}
			
			List<SystemClassif> items = new SystemData().getSysClassification(NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, new Date(), NSIConstants.CODE_DEFAULT_LANG); //izpolzwa se za lejbyli i zatowa ne mi trqbwa na drug ezik
			schemePosAttrLabels.clear();
			for(SystemClassif item : items) {
				schemePosAttrLabels.put(item.getCode(), item.getTekst());
			}
					
			//************************ KRAI NA ATRIBUTITE 
			
			boolean usingSimpleObject=true;
			if (format.equals("sdmx")) {
				usingSimpleObject=false;
			}
			
			List<PositionS> listPost=new ArrayList<PositionS>();
			
			
			long beg=new Date().getTime();
			// IZVLICHANE NA POZICII
			List<Object[]> result = new PositionSDAO(ActiveUser.DEFAULT).loadPositionsForExport(idVersion, schemePosAttr,usingSimpleObject);
			
			System.out.println("positionLoad: "+(new Date().getTime()-beg));
			beg=new Date().getTime();
			
			//IZVLICHANE NA LANGOVE
			List<Object[]> listLang=new PositionSDAO(ActiveUser.DEFAULT).loadPositionsLangsForExport(idVersion, 2, schemePosAttr,usingSimpleObject);
			
			
			
			
			System.out.println("langLoad: "+(new Date().getTime()-beg));
			beg=new Date().getTime();
			
			//IZVLICHANE NA UNITS
			HashMap<Integer, HashMap<Integer, String>> mapUnit=new HashMap<Integer, HashMap<Integer,String>>();
			List<Object[]> unitsList=new ArrayList<Object[]>();
			
			if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA) || schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {
				
				unitsList=new PositionSDAO(ActiveUser.DEFAULT).loadPositionsUnitsForExport(idVersion);
				
				if (usingSimpleObject) {
					mapUnit=new PositionSDAO(ActiveUser.DEFAULT).decodeUnitsAsMap(unitsList, new SystemData(), 1);	
				}
				
			
				System.out.println("unitLoad: "+(new Date().getTime()-beg));		
				beg=new Date().getTime();
			}
			
			
			// V ZAVISIMOST OT TIPA NA FILE-A
			if (usingSimpleObject) {
				listPost=addLangToMainPosAsPositionS(result, listLang, unitsList, schemePosAttr);
				// SORT
				new PositionSDAO(ActiveUser.DEFAULT).doSortSchemePrev(listPost);
				//TODO CREATE FILE - CHAKAME YONCHO.
				
			}else {
				result=addLangToMainPosAsObj(result, listLang, mapUnit, schemePosAttr);
				// SORT
				new PositionSDAO(ActiveUser.DEFAULT).doSortSchemePrevAsObj(result, idVersion);
//				header-a pak po atributite na clasifikaciqta i posle samo puskash dolnoto i bi trqbvalo da e tova.
				List<String> headerNames=getHeaderNames(schemePosAttr);
				
				
				
				StringBuilder sb = new StringBuilder();
				
				FileOutputStream f= new FileOutputStream(new File("D:\\test.txt"));
				

				
				for (int i = 0; i < headerNames.size(); i++) {
					sb.append(headerNames.get(i));
					sb.append(";");	
				}
				sb.append(System.lineSeparator());				
				
				for (int i = 0; i < result.size(); i++) {
					// DA NE ZABRAVISH DA SKIPNESH PARVITE 3 te sa ID,ID_PREV,ID_PARENT - polzvat se za sortiraneto i ne vlizat v export-a
					for (int j = 4; j < result.get(i).length; j++) {
						String s=(""+result.get(i)[j]).replace("\r", "");
						s=s.replace("\n", "");

						sb.append("\""+s+"\"");
						sb.append(";");	
					}				
					sb.append(System.lineSeparator());				
				}
				 

				System.out.println("writingToFile: "+(new Date().getTime()-beg));		
				
		      f.write(sb.toString().getBytes());
		      f.flush();
		      f.close();
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();		
		}finally {
			JPA.getUtil().closeConnection();
		}
		
	}
	
	public List<String> getHeaderNames(Map <Integer,Boolean> schemePosAttr){
		List<String> rez=new ArrayList<String>();
		
		rez.add("Код");
		if (schemePosAttr.containsKey(2)) {
			rez.add("Пълен код");	
		}
    	if (schemePosAttr.containsKey(3)) {
    		rez.add("Код с разделители");	
    	}
    	if (schemePosAttr.containsKey(4)) {
    		rez.add("Тип");	
    	}
    	if (schemePosAttr.containsKey(5)) {
    		rez.add("Статус");	
    	}
    	if (schemePosAttr.containsKey(6)) {
    		rez.add("Номер на ниво");	
    	}
    	if (schemePosAttr.containsKey(7)) {
    		rez.add("Национална измерителна единица");	
    	}
    	if (schemePosAttr.containsKey(8)) {
    		rez.add("Международна измерителна единица");	
    	}
    	if (schemePosAttr.containsKey(11)) {
    		rez.add("Официално наименование");	
    	}
    	if (schemePosAttr.containsKey(12)) {
    		rez.add("Стандартно кратко наименование");	
    	}
    	if (schemePosAttr.containsKey(13)) {
    		rez.add("Стандартно дълго наименование");	
    	}
    	if (schemePosAttr.containsKey(14)) {
    		rez.add("Алтернативни наименования");	
    	}
    	if (schemePosAttr.containsKey(15)) {
    		rez.add("Коментар");	
    	}
    	if (schemePosAttr.containsKey(16)) {
    		rez.add("Включва");	
    	}
    	if (schemePosAttr.containsKey(17)) {
    		rez.add("Включва още");	
    	}
    	if (schemePosAttr.containsKey(18)) {
    		rez.add("Не включва");	
    	}
    	if (schemePosAttr.containsKey(19)) {
    		rez.add("Правила");	
    	}
    	if (schemePosAttr.containsKey(20)) {
    		rez.add("Препратка");	
    	}
    	if (schemePosAttr.containsKey(21)) {
    		rez.add("Статистически показател");	
    	}
		return rez;
	}
	
	public List<Object[]> addLangToMainPosAsObj(List<Object[]> pos, List<Object[]> listLang, HashMap<Integer, HashMap<Integer, String>> mapUnit, Map <Integer,Boolean> schemePosAttr) {
		
		//langMap e sortiran: order by l.POSITION_ID asc,l.LANG desc
		List<Object[]> resultList = new ArrayList<>();
		int curr=0;
		
		for (int i = 0; i < pos.size(); i++) {
			
			
			
			boolean found=false;
			for (int j = curr; j < listLang.size(); j++) {
				
				if (SearchUtils.asInteger(listLang.get(j)[0]).equals(SearchUtils.asInteger(pos.get(i)[0]))) {
					if (!found) {
						 
						String[] s=new String[1];
						if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA) && schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {
							s=new String[2];
						}
						if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_NACIONALNA)) {
							s[0]=mapUnit.get(SearchUtils.asInteger(pos.get(i)[0])).get(NSIConstants.CODE_ZNACHENIE_NACIONALNA);
						}
						if (schemePosAttr.containsKey(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {
							s[1]=mapUnit.get(SearchUtils.asInteger(pos.get(i)[0])).get(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA);
						}
						
						Object[] s2=ArrayUtils.addAll(pos.get(i),s);
						//mahame първите 3 от ланг-а l.ID,	l.POSITION_ID, l.LANG, - за simpleExport ne ni trqbvat.
						resultList.add(ArrayUtils.addAll(s2, ArrayUtils.subarray(listLang.get(j), 3, listLang.get(j).length)));
					}
					found=true;
				}else {
					curr=j;
					break;
				}
				
				
			}
			
		 
		}
		return resultList;
		
		 
	}
	
	public List<PositionS> addLangToMainPosAsPositionS(List<Object[]> pos, List<Object[]> listLang, List<Object[]> unitList, Map <Integer,Boolean> schemePosAttr) {
		
		//langMap e sortiran: order by l.POSITION_ID asc,l.LANG desc
		List<PositionS> resultList = new ArrayList<PositionS>();
		int curr=0;
		
		for (int i = 0; i < pos.size(); i++) {
			PositionS p=new PositionS();
			p.setVersionId(idVersion);
			p.setId(SearchUtils.asInteger(pos.get(i)[0]));
			p.setIdPrev(SearchUtils.asInteger(pos.get(i)[1]));
			p.setIdParent(SearchUtils.asInteger(pos.get(i)[2]));
			p.setCode(SearchUtils.asString(pos.get(i)[3]));
			p.setCodeFull(SearchUtils.asString(pos.get(i)[4]));
			p.setCodeSeparate(SearchUtils.asString(pos.get(i)[5]));
			p.setCodeType(SearchUtils.asInteger(pos.get(i)[6]));
			p.setStatus(SearchUtils.asInteger(pos.get(i)[7]));
			p.setLevelNumber(SearchUtils.asInteger(pos.get(i)[8]));
			
			
			
			boolean found=false;
			for (int j = curr; j < listLang.size(); j++) {
				
				if (SearchUtils.asInteger(listLang.get(j)[0]).equals(SearchUtils.asInteger(pos.get(i)[0]))) {
					if (!found) {
						
						
						//LANGS
						PositionLang pl=new PositionLang();
						pl.setId(SearchUtils.asInteger(listLang.get(j)[0]));
//						pl.setPosition();
						pl.setLang(SearchUtils.asInteger(listLang.get(j)[2]));
						pl.setOffitialTitile(SearchUtils.asString(listLang.get(j)[3]));
						pl.setLongTitle(SearchUtils.asString(listLang.get(j)[4]));
						pl.setShortTitle(SearchUtils.asString(listLang.get(j)[5]));
						pl.setAlternativeNames(SearchUtils.asString(listLang.get(j)[6]));
						
						pl.setComment(SearchUtils.asString(listLang.get(j)[7]));
						pl.setIncludes(SearchUtils.asString(listLang.get(j)[8]));
						pl.setAlsoIncludes(SearchUtils.asString(listLang.get(j)[9]));
						pl.setExcludes(SearchUtils.asString(listLang.get(j)[10]));
						pl.setRules(SearchUtils.asString(listLang.get(j)[11]));
						pl.setPrepratka(SearchUtils.asString(listLang.get(j)[12]));
						pl.setStatPokazatel(SearchUtils.asString(listLang.get(j)[13]));
						
						
						
						
						p.getLangMap().put(SearchUtils.asInteger(listLang.get(j)[2]), pl);
						// UNITS
						for (int j2 = 0; j2 < unitList.size(); j2++) {
							PositionUnits pu=new PositionUnits();
							pu.setId(SearchUtils.asInteger(unitList.get(j2)[0]));
//							pu.setPosition(p);
							pu.setUnit(SearchUtils.asInteger(unitList.get(j2)[2]));
							pu.setTypeUnit(SearchUtils.asInteger(unitList.get(j2)[3]));
							
							p.getUnits().add(pu);	
						}
						
						
						resultList.add(p);
					}
					found=true;
				}else {
					curr=j;
					break;
				}
				
				
			}
			
			
		}
		return resultList;
		
		
	}
	
	@Test
	public void excelExport() throws IOException {

		    String FILE_NAME = "D:/MyFirstExcel.xlsx";

		    StringBuffer SQL = new StringBuffer();
			SQL.append("SELECT DISTINCT ");
			SQL.append("    r.id, ");
			SQL.append("    r.SOURCE_CODE, ");
			SQL.append("    r.TARGET_CODE, ");
			SQL.append("    (select s.CODE_EXT from SYSTEM_CLASSIF s where s.CODE_CLASSIF=512 and s.code =r.EXPLANATION ) expl ");
			SQL.append(" FROM ");
			SQL.append("    RELATION r ");
			SQL.append(" WHERE ");
			SQL.append("    r.ID_TABLE=:idTable");
			
			Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
			q.setParameter("idTable", 1085);//prodprom 2001-2002
			 
			 
	
			List<Object[]> result = q.getResultList();
			

		        XSSFWorkbook workbook = new XSSFWorkbook();
		        XSSFSheet sheet = workbook.createSheet("Sheet1");
		         

		        int rowNum = 0;
		        
		        List<String> headerNames=new ArrayList<String>();
		        headerNames.add("Код източник");
				headerNames.add("Код цел");
				headerNames.add("Символ");
				
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
				int colNum = 0;
			    for (int i = 0; i < headerNames.size(); i++) {
			    	
			    	org.apache.poi.ss.usermodel.Cell cell = row.createCell(colNum++);
			    	cell.setCellValue(headerNames.get(i));
					 
				}
			    
			    int i=0;
			    int skipFirst=1;
			    for (int j = 0; j < result.size(); j++) {
//			    	updateProgress(result.size(), i);
			    	org.apache.poi.ss.usermodel.Row row1 = sheet.createRow(rowNum++);
			    	int colNum1 = 0;
		    	 	for (int a = skipFirst; a < result.get(i).length; a++) {
		    	 		org.apache.poi.ss.usermodel.Cell cell = row1.createCell(colNum1++);
		    	 		cell.setCellValue(result.get(i)[a]==null?"":("\""+result.get(i)[a]).replace("\r", "").replace("\n", "")+"\"");
			        }
					i++;
			    }
		        
		        
		        

		      

		            FileOutputStream outputStream = new FileOutputStream(FILE_NAME);
		            workbook.write(outputStream);
		            workbook.close();

		        System.out.println("Done");
	}
	@Test
	public void pdfExport() throws IOException {
		
		String FILE_NAME = "D:/MyFirstPdf.pdf";
		
		StringBuffer SQL = new StringBuffer();
		SQL.append("SELECT DISTINCT ");
		SQL.append("    r.id, ");
		SQL.append("    r.SOURCE_CODE, ");
		SQL.append("    r.TARGET_CODE, ");
		SQL.append("    (select s.CODE_EXT from SYSTEM_CLASSIF s where s.CODE_CLASSIF=512 and s.code =r.EXPLANATION ) expl ");
		SQL.append(" FROM ");
		SQL.append("    RELATION r ");
		SQL.append(" WHERE ");
		SQL.append("    r.ID_TABLE=:idTable");
		
		Query q = JPA.getUtil().getEntityManager().createNativeQuery(SQL.toString());
		q.setParameter("idTable", 1085);//prodprom 2001-2002
		
		
		
		List<Object[]> result = q.getResultList();
		
		
		Document doc = new Document();
		PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(FILE_NAME));
		// ako iskame nqkakvo formatirane
		//Font font = new Font(Font.HELVETICA, 12, Font.BOLDITALIC);
		
		// ako iskame kletkite da imat formatirane ot fon-ta 
		//cell.setPhrase(new Phrase("Email", font));
	    //table.addCell(cell);
		
	        
        List<String> headerNames=new ArrayList<String>();
        headerNames.add("Код източник");
		headerNames.add("Код цел");
		headerNames.add("Символ");
			
			
			
		PdfPTable table = new PdfPTable(headerNames.size());
		table.setWidthPercentage(100);
      
		 
	    for (int i = 0; i < headerNames.size(); i++) {
	    	table.addCell(headerNames.get(i));
		}
	    
	    int i=0;
	    int skipFirst=1;
	    for (int j = 0; j < result.size(); j++) {
//	    	updateProgress(result.size(), i);
    	 	for (int a = skipFirst; a < result.get(i).length; a++) {
    	 		table.addCell(result.get(i)[a]==null?"":("\""+result.get(i)[a]).replace("\r", "").replace("\n", "")+"\"");
	        }
			i++;
	    }
		
		doc.open();
//		 AKo iskame zaglavie na eksporta parvo tova slagame.
		
//		Paragraph para = new Paragraph("Hello! This PDF is created using openPDF", font);
//		doc.add(para);
		
		 
		
	      doc.add(table);
		doc.close();
		writer.close();
		
		System.out.println("Done");
	}
	
	 

}
