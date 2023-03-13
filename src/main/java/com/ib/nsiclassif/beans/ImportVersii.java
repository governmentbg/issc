package com.ib.nsiclassif.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionManagement;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.persistence.Query;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
//import org.apache.tika.Tika;
//import org.apache.tika.exception.TikaException;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.element.office.OfficeSpreadsheetElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.primefaces.component.export.PDFOptions;
import org.primefaces.component.export.PDFOrientationType;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.xml.sax.ContentHandler;
import org.w3c.dom.NodeList;

import com.ib.nsiclassif.db.dao.LevelDAO;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dto.Level;
import com.ib.nsiclassif.db.dto.Version;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.github.miachm.sods.SpreadSheet;
import com.github.miachm.sods.Style;
import com.ib.indexui.customexporter.CustomExpPreProcess;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.ActiveUser;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.ValidationUtils;

import au.com.bytecode.opencsv.CSVReader;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;


/**
 * Импорт на версии в класификации ЛМ
 *
 */

@SuppressWarnings("deprecation")
@ViewScoped
@Named(value = "importVersii")
public class ImportVersii extends IndexUIbean implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -7056074939252508693L;

	static final Logger LOGGER = LoggerFactory.getLogger(ImportVersii.class);

	private Integer userId;
	/** за конкретната система */
	public static final String beanMessages = "beanMessages";

	private List<String[]> sysClassifList = new ArrayList<>();
	private String selClassif;
	// Novo
	private List<String> versiis;
	private String razdelitel = ";";
	private String razdelitelME = ";";
	private List<String> selectedItems;
	private List<String> items = new ArrayList<>();
	private String textArea = "";
	private String textOutput = "";
	private List<TempOpis> opList;
	private List<Object[]> importedRow = new ArrayList<>();
	private List<Object[]> importedRowOnlyErr = new ArrayList<>();
	private List<Object[]> importedRowAll = new ArrayList<>();
	private List<Object[]> importedRowImported = new ArrayList<>();

	private String[] selectedItem;
	private String[] selectedVersii;

	private VersionEditBean mainBean;
	private Integer lang;
	private Integer versionId;
    private Integer idObj;

	private UserData ud;
	private HashMap<Integer, String> izbr = new HashMap<Integer, String>();
	private HashMap<String, Integer> allAtrib = new HashMap<String, Integer>();
	HashMap<Integer, HashMap<String, Integer>> codeLevel = new HashMap<Integer, HashMap<String, Integer>>();

	private Integer nomerPored = 0;
	private Date currentDate;
	private List<SelectItem> versiiItemList;
	private List<SelectItem> merniEdiniciList;
	private List<SelectItem> merniEdiniciListEn;
	private HashMap<String, Integer> merniEdinici = new HashMap<String, Integer>();
	private HashMap<String, Integer> merniEdiniciEn = new HashMap<String, Integer>();
	private HashMap<String, Integer> ezik = new HashMap<String, Integer>();
	private String tipNaFile;
	private boolean levelNoParent = false;
	private String pаrent = "";
	private String lastPаrent = "";
	private boolean langBg = true;
	private Integer errMe ;
	boolean flPlus 	  = false;
	boolean flMERazd  = false;
	boolean flAntetka = false;
	boolean flBg ;
	boolean flEn ;

	private String fileName;
	private String insertType = "0";
	private String message="Моля изчакайте.";
	
	private DualListModel<String> attributes;
	
	
    private DualListModel<SystemClassif> posAttrList;
    private List<Integer> listAttr;
    private List<Level> selectedLevels;
    private List<Level> availableLevels;
    private String levels="";
    private String headerCodes;
	private static String errString = "";
	private Integer posMENac=0;
	private Integer posMEMejd=0;
	private Integer countColMENac ;
	private Integer countColMEMejd ;

	

	public ImportVersii() {
//		String text = "hi there how are you going?";
//		String wrapped = 
//		  WordWrap.from(text)
//		    .maxWidth(10)
//		    .insertHyphens(true) // true is the default
//		    .wrap();
//		System.out.println("wrapped: "+wrapped);
			selectedItems= new ArrayList<>();
	        List<String> attributesSource = new ArrayList<>();
	        List<String> attributesTarget = new ArrayList<>();
	    countColMENac = 0;   
		if (selectedItems != null) {
			selectedItems.clear();
		}
		if (textOutput != "") {
			textOutput = "";
		}
		ud = getUserData(UserData.class);
		userId = ud.getUserId();
		this.currentDate = new Date();
		versiis = new ArrayList<>();

		try {
			this.versiiItemList = createItemsList(false, NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES,
					this.currentDate, null);
			allAtrib = new HashMap<String, Integer>();
			if (this.versiiItemList != null) {
				for (SelectItem item : this.versiiItemList) {
					versiis.add(item.getLabel());
					allAtrib.put(item.getLabel(), (Integer) item.getValue());
					attributesSource.add(item.getLabel());
				}
			}
			
			if (getCurrentLang()==1) {
				attributesSource.add("Номер");
				attributesSource.add("Родител");
				allAtrib.put("Номер", 0);
				allAtrib.put("Родител", 10);
				versiis.add(0, "Номер");
				versiis.add(10, "Родител");
				allAtrib.put("Език", 9);
			}
			if (getCurrentLang()==2) {
				attributesSource.add("Number");
				attributesSource.add("Parent");
				allAtrib.put("Number", 0);
				allAtrib.put("Parent", 10);
				versiis.add(0, "Number");
				versiis.add(10, "Parent");
				allAtrib.put("Lang", 9);
			}
			
	        attributes = new DualListModel<>(attributesSource, attributesTarget);
			
	        
			Query qq;
			JPA.getUtil().begin();
			String idShema = "select c.ID,c.CODE, c.CODE_CLASSIF, c.CODE_PREV,c.CODE_PARENT, m.TEKST, c.CODE_EXT,c.LEVEL_NUMBER "
					+ " from SYSTEM_CLASSIF c INNER JOIN SYSCLASSIF_MULTILANG m ON "
					+ " m.TEKST_KEY = c.TEKST_KEY where c.CODE_CLASSIF=144 AND m.lang=1 and code_parent=0"
					;
			qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
			@SuppressWarnings("unchecked")
			List list1 = qq.getResultList();
			JPA.getUtil().closeConnection();
			
			for (int i = 0; i < list1.size(); i++) {
				 Object[] a = (Object[]) list1.get(i);
				String str = a[1].toString();
				int number = Integer.parseInt(str);
				String text = a[5].toString().replaceAll("\\s+","");
				merniEdinici.put(text, number);
			}
			merniEdinici.remove("-");
	        
			//Формиране на merniEdinici
//			List<SystemClassif> list;
//			list = getSystemData().getSysClassification(NSIConstants.CODE_CLASSIF_UNITS, this.currentDate, SysConstants.CODE_LANG_BG);
//			List<SelectItem> items = new ArrayList<>(list.size());
//			for (SystemClassif x : list) {
//				if(x.getDopInfo()!=null && !x.getDopInfo().trim().isEmpty()) {
//					items.add(new SelectItem(x.getCode(), x.getTekst(),x.getDopInfo()));
//				} else {
//					items.add(new SelectItem(x.getCode(), x.getTekst()));
//				}
//			}
//			this.merniEdiniciList=new ArrayList<SelectItem>();
//			this.merniEdiniciList.addAll(items);//За БГ
////			this.merniEdiniciList = createItemsList(false, NSIConstants.CODE_CLASSIF_UNITS, this.currentDate, null);
//			if (this.merniEdiniciList != null) {
//				for (SelectItem item : this.merniEdiniciList) {
//					merniEdinici.put(item.getLabel(), (Integer) item.getValue());
//				}
//			}
			ezik.put("bg", 1);ezik.put("en", 2);
			this.importedRow=new ArrayList<Object[]>();
		} catch (Exception e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg") + " - "
					+ e.getLocalizedMessage(), e);
			LOGGER.error(e.getMessage(), e);
		}
	}

	@PostConstruct
	public void initData() {
		this.mainBean = (VersionEditBean) JSFUtils.getManagedBean("versionEdit");
		if (mainBean != null && !Objects.equals(mainBean.getVersionId(), this.versionId)) { // ако вече сме отваряли
			LOGGER.info("INIT LEVELS....");
			this.lang = NSIConstants.CODE_DEFAULT_LANG;
			try {
				if (mainBean != null && mainBean.getVersionId() != null) {
					this.versionId = mainBean.getVersionId();
				}
			} catch (Exception e) {
				LOGGER.error("Грешка при инициализиране на нива на йерархия!", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
			}
		}
		posAttrList = new DualListModel<>();
		Version version = mainBean.getVersion();

//		// зареждаме нивата за избор
//		if (versionId != null) {
//			loadLevelsInVersionList(versionId);
//			selectedLevels = new ArrayList<Level>();
//			for (Level level : availableLevels) {
//				
//			}
//			StringBuilder sb = new StringBuilder();
//			if(!selectedLevels.isEmpty()) {
//				for (Level l : selectedLevels) {
//					sb.append(l.getLevelNumber()).append(",");
//				}
//				if(sb.length()>0) {
//					sb.deleteCharAt(sb.length() - 1);
//					this.levels = sb.toString();
//				}
//			}
//			
//		
//	}
		
// Да се внинава като се отваря долното!!!!
		if (version == null) {
			// трябва ни versionEdit.version.id, за да извлечем всички атрибути на версията
			// на класификацията в listAttr
			idObj = 1;

		} else {
			idObj = mainBean.getVersion().getId();
			listAttr = new ArrayList<Integer>();
			loadHeadersInVersion(listAttr, idObj);
		}
		String stringNacEdinica = "";
		String stringMejdEdinica = "";
		for (int i = 0; i < attributes.getSource().size(); i++) {
			String aa = attributes.getSource().get(i);
			
			if ("Национална измерителна единица".equals(aa)) {
				attributes.getSource().remove("Национална измерителна единица");i-=1;
				stringNacEdinica=aa+"1";
				attributes.getSource().add(stringNacEdinica) ;
				stringNacEdinica=aa+"2";
				attributes.getSource().add(stringNacEdinica) ;
				allAtrib.put("Национална измерителна единица1", 7);
				allAtrib.put("Национална измерителна единица2", 7);
			}
			if ("Международна измерителна единица".equals(aa)) {
				attributes.getSource().remove("Международна измерителна единица");i-=1;
				stringMejdEdinica=aa+"1";
				attributes.getSource().add(stringMejdEdinica) ;
				stringMejdEdinica=aa+"2";
				attributes.getSource().add(stringMejdEdinica) ;
				allAtrib.put("Международна измерителна единица1", 8);
				allAtrib.put("Международна измерителна единица2", 8);
			}
			System.out.println();
		}
		Collections.sort(attributes.getSource());//Сортира по азбучен ред лист от стринг
		System.out.println();
	}
	
	private void loadLevelsInVersionList(Integer versionId) {
		try {
			JPA.getUtil().runWithClose(() -> setAvailableLevels(new LevelDAO(getUserData()).levelsInVersionList(versionId)));

		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на нива във версия!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}
	}


	/**
	 * Долния метод запомня реда на избор в <p:selectCheckboxMenu
	 */
	public void actionSelect() {
//		izbr=new HashMap<Integer, String>();
//		for (int i = 0; i < selectedItems.size(); i++) {
//			String tmp = selectedItems.get(i).replaceAll("\"", "");;
//			izbr.put(nomerPored, tmp);
//			nomerPored += 1;
//		}
		String redStr = selectedItems.toString();
		String a = selectedItems.toString();
		String substr = ",";
		int brZap = redStr.split(substr, -1).length - 1;
		redStr = redStr.replace(", ", "");
		for (int j = 0; j < brZap; j++) {
			String item = izbr.get(j);
			String target = item.substring(1);
			target = target.substring(0, target.length() - 1);
			redStr = redStr.replaceFirst(target, "");
		}
		izbr.put(nomerPored, redStr);
		nomerPored += 1;
		this.importedRow=new ArrayList<Object[]>();
	}

	private void doXls(UploadedFile upFile) {
		System.out.println("doXls");
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}
		int brIzbr = selectedItems.size();
		importedRow = new ArrayList<Object[]>();
		InputStream inputStream = null;
		HSSFWorkbook workbook;
		flPlus = false;
		Boolean longRow = false;
		posMENac=0;countColMENac=0;
		posMEMejd=0;countColMEMejd=0;

		FormulaEvaluator objFormulaEvaluator;
		try {
			inputStream = upFile.getInputStream();
			DataFormatter objDefaultFormat = new DataFormatter();
			workbook = new HSSFWorkbook(inputStream);
			objFormulaEvaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
			HSSFSheet sheet = workbook.getSheetAt(0);
			HSSFRow row = sheet.getRow(0);
			if (doCompareXls(row, attributes.getTarget())) {
				return;
			}
			row = sheet.getRow(0);
			int rowNumb = sheet.getLastRowNum();
			Object[] rowData = new Object[brIzbr];
			for (int k = 0; k < brIzbr; k++) {
				rowData[k] = row.getCell(k);
				if (rowData[k]!=null) {
					String aa = rowData[k].toString();
					if (aa.contains("единица") ) {
						if (aa.contains("Нац") ) {
						posMENac=k;countColMENac+=1;
						}
						if (aa.contains("Межд") ) {
						posMEMejd=k;countColMEMejd+=1;
						}
					}
				}
			}
//			if (countColMENac==0&&countColMEMejd==0) {
//				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noMEtrxt"));
//				return;
//			}

			for (int j = 1; j < rowNumb + 1; j++) {
				System.out.println("j:"+j);
				System.out.println("posMENacNac:"+posMENac);
				Integer a=Integer.valueOf(j);
				if (a.equals(5689)) {
					System.out.println("j:"+j);
					System.out.println("posMENac:"+posMENac);
				} 
				row = sheet.getRow(j);
				Integer b=Integer.valueOf(posMENac);
				if (b.equals(1)) {
					System.out.println("row:"+row);
				} 
				rowData = new Object[brIzbr];
				for (int i = 0; i < brIzbr; i++) {
					Cell cellValue=row.getCell(i);
					objFormulaEvaluator.evaluate(cellValue); // This will evaluate the cell, And any type of cell will return string value
					String cellValueStr = objDefaultFormat.formatCellValue(cellValue,objFormulaEvaluator);
//					cellValueStr=cellValueStr.replace("/", "."); // За какво съм го сложил??
					rowData[i] = cellValueStr;
					for (int k = 0; k < brIzbr; k++) {
						rowData[k] = row.getCell(k);
						if (rowData[k]!=null) {
							String aa = rowData[k].toString();
							if (izbr.get(k).contains("единица")) {
								if ("".equals(aa)||aa==null) continue;
							}
							if (izbr.get(k).contains("единица")) {
								if (aa.contains(razdelitelME)) {
									flMERazd =true;
								}
								if (aa.contains("+")) {
									flPlus =true;
								}
							}
						}
					}

				}
				
				
				Boolean empty = true;
				for (int k = 0; k < brIzbr; k++) {
//					System.out.println("k:"+k);
					if (rowData[k] != null) {
						if (!rowData[k].toString().equals("")) {
							empty = false;
						}
					}
					if (rowData[1] != null) {
						if (rowData[1].toString().equals("")) {
							longRow = true;
						}
					}
				}
				if (!empty) {// запис само ако поне една клетка не е празна
					importedRow.add(rowData);
				}
			
//				importedRow.add(rowData);
			}

		} catch (IOException e) {
			// Do something with the Exception (logging, etc.)
		}
		if (longRow) {
			importedRow = doNewImportedRow(importedRow);
		}

		if (flMERazd) {
			return;
		}
		if (flPlus) {
			importedRowAll=new ArrayList<Object[]>();
			importedRowOnlyErr=new ArrayList<Object[]>();
			importedRow=doMerniEdinici(importedRow,posMENac,brIzbr);
		}else {
			if (countColMENac>0||countColMEMejd>0) {
				importedRowAll=new ArrayList<Object[]>();
				importedRowOnlyErr=new ArrayList<Object[]>();
				importedRow=doTransfMultiColumn(importedRow);
			}
		}

		System.out.println("doXls kraj i errMe:"+errMe);
	}

	private List<Object[]> doOnlyErr(List<Object[]> importedRow2, int brIzbr) {
		ArrayList<Object[]> importedRowT = new ArrayList<Object[]>();
		int brCol=brIzbr+1;
		for (Object[] objects : importedRow2) {
			if (!"".equals(objects[brIzbr])) {
				importedRowT.add(objects);
			}
//			Object[] rowData = new Object[brCol];
//			for (int k = 0; k < brCol; k++) {
//				rowData[k] = objects[k];
//				if (k==(brCol-1) && objects[k]!=null) {
//					importedRowT.add(rowData);
//				}
//			}
		}
		return importedRowT;
	}

	private List<Object[]> doMerniEdinici(List<Object[]> importedRow2, int posMENacNac, int brIzbr) {
		ArrayList<Object[]> importedRowT = new ArrayList<Object[]>();
		String str="";
		errMe=null;
		for (Object[] objects : importedRow2) {
			Object[] rowData = new Object[brIzbr];
			str="";
			for (int k = 0; k < brIzbr; k++) {
				if (objects[k]==null) {
					rowData[k]="";continue;
				}
				String aa = objects[k].toString();
				if ((posMENacNac==k)) {
					if (aa.contains("+")) {// една колона с повече мерни единици свързани с "+"
						aa=aa.replace("+", razdelitelME);
						String me="";
						List<String> list = new ArrayList<String>(Arrays.asList(aa.split(razdelitelME)));
						List myList = Arrays.asList(aa.split(razdelitelME));
						for (int i = 0; i < myList.size(); i++) {
							String aaa = myList.get(i).toString().trim().replaceAll("\\s+","");
							if (izbr.get(posMENacNac).contains("единица")) {
								System.out.println("koda e:"+merniEdinici.get(aaa));
								if (merniEdinici.get(aaa) != null) {//намерен код на МЕ
									me+=aaa+razdelitelME+" ";
									rowData[k] =aaa;
									continue;
								}else {//ненамерен код на МЕ
									if (aaa.length()==1) {
										if (ValidationUtils.isLat(aaa)) {
											me+=aaa+" Липсва ";
											rowData[k] =me;
											continue;
										}else {
											continue;
										}
									}
										me+=aaa+" Липсва ";
										setErrMe(1);
								}
							}
						}
						rowData[k] =me;
						str+=me;
					}else {// една колона с една мерна единица
						if ("".equals(aa)) continue;
						String me="";
						if (izbr.get(posMENacNac).contains("единица")) {
							System.out.println("koda e:"+merniEdinici.get(aa));
							if (merniEdinici.get(aa) != null) {//намерен код на МЕ
								me+=aa+razdelitelME+" ";
								rowData[k] =aa;
								continue;
							}else {//ненамерен код на МЕ
									me+=aa+" Липсва ";
									setErrMe(1);
									rowData[k] = me;
									str+=me;
							}
						}
					}
				}else{
					rowData[k] = objects[k];
					str+=objects[k];
				}
			}
			importedRowT.add(rowData);
			if (str.contains("Липсва")) {
				importedRowOnlyErr.add(rowData);
			}

		}
		return importedRowT;
	}

	@SuppressWarnings("resource")
	private void doXlsx(UploadedFile upFile) {
		System.out.println("doXlsx");
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}
//		if (!doTransferPickedListToSelVersii(attributes.getTarget())) {
//			return;
//		}

		int brIzbr = selectedItems.size();
//		int brIzbr = attributes.getTarget().size();

		importedRow = new ArrayList<Object[]>();
		InputStream inputStream = null;
		XSSFWorkbook workbook;
		Boolean longRow = false;
		flPlus = false;
		posMENac=0;countColMENac=0;
		posMEMejd=0;countColMEMejd=0;
		
		XSSFRow row = null;
		int i = 0;
		try {
			inputStream = upFile.getInputStream();
			workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(0);
			row = sheet.getRow(0);
//			if (doCompareXlsx(row, selectedItems)) {
//				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
//				return;
//			}
			if (doCompareXlsxPckList(row, attributes.getTarget())) {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
				return;
			}
			int rowNumb = sheet.getLastRowNum()+1;
			Object[] rowData = new Object[brIzbr];
			for (int k = 0; k < brIzbr; k++) {
				rowData[k] = row.getCell(k);
				if (rowData[k]!=null) {
					String aa = rowData[k].toString();
					if (aa.contains("единица") ) {
						if (aa.contains("Нац") ) {
						posMENac=k;countColMENac+=1;
						}
						if (aa.contains("Межд") ) {
						posMEMejd=k;countColMEMejd+=1;
						}
					}
				}
			}
//			if (countColMENac==0&&countColMEMejd==0) {
//				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noMEtrxt"));
//				return;
//			}
			
			for (i = 1; i < rowNumb; i++) {
				System.out.println("i:" + i);
				Integer a = Integer.valueOf(i);
				if (a.equals(760)) {// Towa se izpolzwa когато има грешка
					System.out.println("i:" + i);
				}
				updateProgress(rowNumb, i);
				
				row = sheet.getRow(i);
				rowData = new Object[brIzbr];
				for (int k = 0; k < brIzbr; k++) {
					rowData[k] = row.getCell(k);
					if (rowData[k]!=null) {
						String aa = rowData[k].toString();
						if (izbr.get(k).contains("единица")) {
							if ("".equals(aa)||aa==null) continue;
						}
						if (izbr.get(k).contains("единица")) {
							if (aa.contains(razdelitelME)) {
								flMERazd =true;
							}
							if (aa.contains("+")) {
								flPlus =true;
							}
						}
					}
				}
				Boolean empty = true;
				for (int k = 0; k < brIzbr; k++) {
//					System.out.println("k:"+k);
					if (rowData[k] != null) {
						if (!rowData[k].toString().equals("")) {
							empty = false;
						}
					}
					if (rowData[1] != null) {
						if (rowData[1].toString().equals("")) {
							longRow = true;
						}
					}
				}
				if (!empty) {// запис само ако поне една клетка не е празна
					importedRow.add(rowData);
				}
			}

		} catch (IOException e) {
			LOGGER.error("Грешка при извличане на елемент по id на предходен", e);
			System.out.println("i:" + i);
			System.out.println("row:" + row);
			return;
		}
		if (longRow) {
			importedRow = doNewImportedRow(importedRow);
		}
		System.out.println("ErrMe doXlsx kraj"+errMe);
		flAntetka=false;//Няма антетка
		if (flMERazd) {
			return;
		}
		if (flPlus) {
			importedRowAll=new ArrayList<Object[]>();
			importedRowOnlyErr=new ArrayList<Object[]>();
			importedRow=doMerniEdinici(importedRow,posMENac,brIzbr);
		}else {
			if (countColMENac>0||countColMEMejd>0) {
				importedRowAll=new ArrayList<Object[]>();
				importedRowOnlyErr=new ArrayList<Object[]>();
				importedRow=doTransfMultiColumn(importedRow);
			}
		}
		System.out.println("doXlsx kraj");
	}




	private List<Object[]> doTransfMultiColumn(List<Object[]> importedRow2) {
		List<Object[]> importedRowTemp = new ArrayList<>();
		int brCol = izbr.size();
		String str="";
		errMe=null;
		Integer br=0;
		for (Object[] objects : importedRow2) {
			Object[] rowData = new Object[brCol];
//			if (br.equals(1382)) {// Towa se izpolzwa когато има грешка
//				System.out.println("i:" + br);
//			}
//
//			if (errMe!=null) {
//				System.out.println("br:"+br);
//			}
			str="";br+=1;
			for (int k = 0; k < brCol; k++) {
				if (objects[k]==null) {
					rowData[k]="";continue;
				}
				String aa =  objects[k].toString();
				if (izbr.get(k).contains("единица")) {
					if ("".equals(aa)||aa==null) continue;
					String me="";aa=aa.replaceAll("\\s+","").replace("\r\n", "");//последното защото е залепено когато има longrow
//					System.out.println("koda e:"+merniEdinici.get(aa));
					if (merniEdinici.get(aa.trim()) != null) {//намерен код на МЕ
						rowData[k] = aa;
						continue;
					}else {//ненамерен код на МЕ
						if (aa.length()==1) {
//							System.out.println("1");
//							System.out.println("errMe"+errMe);
							if (ValidationUtils.isLat(aa)) {
//								System.out.println("2");
//								System.out.println("errMe"+errMe);
								me+=aa+" Липсва ";
								rowData[k] =me;
								setErrMe(1);
								continue;
							}else {
								rowData[k] ="";
//								System.out.println("3");
								continue;
							}
						}
//							System.out.println("4");
							if (!"".equals(aa)) {
								me+=aa+" Липсва ";
								rowData[k] =me;
								setErrMe(1);
							}
							continue;
					}
					
				}else{
//					System.out.println("5");
					rowData[k] = objects[k];
//					str+=objects[k];
				}
			}
			importedRowTemp.add(rowData);
			if (str.contains("Липсва")) {
				importedRowOnlyErr.add(rowData);
			}
		}
//		System.out.println("ErrMe sled doTransfMultiColumn"+errMe);
		return importedRowTemp;
	}

	private List<Object[]> doNewImportedRow(List<Object[]> importedRow2) {
		List<Object[]> importedRowTemp = new ArrayList<>();
		int brCol = izbr.size();
		Object[] colona = new Object[brCol];
		Object[] red0 = importedRow2.get(0);
		for (int i = 0; i < colona.length; i++) {
			colona[i] = red0[i];
		}
		for (int k = 0; k < importedRow2.size() - 1; k++) {
			Object[] tt = importedRow2.get(k);
			Object[] ttt = importedRow2.get(k + 1);
			String ttString = ttt[0].toString();
			if (!ttString.equals("")) {
				importedRowTemp.add(colona);
				colona = new Object[brCol];
				for (int i = 0; i < colona.length; i++) {
					colona[i] = ttt[i];
				}
				continue;
			}
			for (int j = 1; j < colona.length; j++) {
				colona[j] += ttt[j].toString() + "\r\n";
			}
		}
		importedRowTemp.add(colona);
		return importedRowTemp;
	}

	private void doCsv(UploadedFile upFile) {
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}
		if (razdelitel.equals("")) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.lipsvaRazdelitel"));

		}
		if (selectedItems == null) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
		}
		InputStream inputStream = null;
		importedRow = new ArrayList<Object[]>();
		ArrayList<Object[]> importedRowЕрр = new ArrayList<Object[]>();
		int brIzbr = selectedItems.size();
//		new StringBuilder();
		
		
		String line;
		try {
			inputStream = upFile.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		if (razdelitel.equals("|")) razdelitel="\\|";
		CSVReader readerN = null;
		try {
			if (tipNaFile.equals("txt")) {
				 readerN= new CSVReader(new InputStreamReader(inputStream));
			}
			if (tipNaFile.equals("csv")) {
				 readerN= new CSVReader(new InputStreamReader(inputStream, "cp1251"));
			}
			List<String[]> lines = readerN.readAll();
			System.out.println("lines"+lines.get(0)[0]);
			if (!lines.get(0)[0].contains(razdelitel)) {			/// dali ima tozi razdelitel
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noSameDelimiter"));
				return;
			}
			System.out.println();
			if (lines.get(0)[0].contains("Код")) {			/// dali първия ред е антетка
				flAntetka=true;
			}
			if (lines.get(0)[0].contains("Code")) {			/// dali първия ред е антетка
				flAntetka=true;
			}

			int brRed = lines.size();
			for (int i = 0; i < lines.size(); i++) {
				Integer aa = Integer.valueOf(i);
				if (aa.equals(122)) {// Towa se izpolzwa когато има грешка
					System.out.println("i:" + i);
				}

				String[] lineTemp = lines.get(i);
				String ttt=lineTemp[0].toString();
//				lineTemp = lineTemp.replaceAll("\"", "");
				Object[] rowData = new Object[brIzbr];
				System.out.println("ttt:" + ttt);
				System.out.println("linecccTemp[0]:" + ttt.replace("]", "").replace("[", ""));
				if (razdelitel.equals("|")) razdelitel="\\|";
				List myList = Arrays.asList(ttt.split(razdelitel));
				System.out.println("myList.size():" + myList.size());
				
				if (izbr.size()!=myList.size()) {
//					String line1=reader.readLine();
					JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.sameDelimiter"));
					break;
				}
				for (int j = 0; j < myList.size(); j++) {
					String a = myList.get(j).toString();
					a = a.replaceAll("\"", "");
					rowData[j] = a;
				}
				importedRow.add(rowData);
				
			}
			
			
//			BufferedReader reader = null;
			
//			String [] nextLine;
//			while ((nextLine = readerN.readNext()) != null)
//			  {
//			    //Use the tokens as required
//			    System.out.println(Arrays.toString(nextLine));
//			  }
//			reader = new BufferedReader(new InputStreamReader(inputStream, "cp1251"));
//			line = reader.readLine();
//			line=line.substring(0,line.length()-1);//защо съдържа на края ";"!!!!!
//			if (!line.contains(razdelitel)) {			/// dali ima tozi razdelitel
//				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noSameDelimiter"));
//				return;
//			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
//		int j = 0;
//		try {
//			Stream<String> rows = reader.lines();
//			Integer brRedove = (int) rows.count();
//			reader.close();
//			
//			BufferedReader readern = new BufferedReader(new InputStreamReader(inputStream, "cp1251"));
//			line = readern.readLine();
//			while ((line = readern.readLine()) != null) {
//				Object[] rowData = new Object[brIzbr];
//				if (razdelitel.equals("|")) razdelitel="\\|";
//				System.out.println("j:" + j);
//				Integer aa = Integer.valueOf(j);
//				if (aa.equals(122)) {// Towa se izpolzwa когато има грешка
//					System.out.println("j:" + j);
//				}
////				updateProgress(brRedove, j);
//
//				j++;
//				line=line.substring(0,line.length()-1);//защо съдържа на края ";"!!!!!				
//				line = line.replaceAll("\"", "");
//				List myList = Arrays.asList(line.split(razdelitel));
//				if (izbr.size()!=myList.size()) {
//					String line1=reader.readLine();
//					JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.sameDelimiter"));
//					break;
//				}
//				for (int i = 0; i < myList.size(); i++) {
//					String a = myList.get(i).toString();
//					a = a.replaceAll("\"", "");
//					rowData[i] = a;
//				}
//				importedRow.add(rowData);
//			}
//		} catch (IOException e) {
//			// Do something with the Exception (logging, etc.)
//		}

	}

	private boolean doTransferSelItemsToSelVersii(List<String> selectedItems2) {
		if (selectedItems2==null) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
			return false;
		}
		int brItens = selectedItems2.size();
		String[] selectedVersiiTemp = new String[brItens];
		int j = 0;
		for (String string : selectedItems2) {
			selectedVersiiTemp[j] = string;
			j += 1;
		}
		setSelectedVersii(selectedVersiiTemp);
//		System.out.println("doTransferSelItemsToSelVersii");
		return true;

	}
	
	private boolean doTransferPickedListToSelVersii(List<String> target) {
		if (target==null) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
			return false;
		}
		int brItens = target.size();
		String[] selectedVersiiTemp = new String[brItens];
		int j = 0;
		for (String string : target) {
			selectedVersiiTemp[j] = string;
			j += 1;
		}
		setSelectedVersii(selectedVersiiTemp);
//		System.out.println("doTransferSelItemsToSelVersii");
		return true;
	}

	private HashMap<Integer, String> zarediIzbr(List<String> list) {
		HashMap<Integer, String> izbrTemp = new HashMap<Integer, String>();
		
		for (String string : list) {
			
			izbrTemp.put(nomerPored, string);
//			izbrTemp.put(allAtrib.get(string), string);
			selectedItems.add(string);
			nomerPored++;
		}
		return izbrTemp;
	}

	public void handleFileUpload(FileUploadEvent event) throws Exception {
		System.out.println("handleFileUpload");
		boolean flag=true;
		importedRow = new ArrayList<Object[]>();

		izbr=zarediIzbr(attributes.getTarget());
//		selectedItems.addAll(attributes.getTarget());
		UploadedFile uploadedFile = event.getFile();
		Object source = event.getSource();
		int hash = event.hashCode();
		FacesContext cont = event.getFacesContext();
		fileName = uploadedFile.getFileName();
		tipNaFile = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (tipNaFile.equals("xml")) {
			flag=false;
		}
		if (flag&&attributes.getTarget().size()==0) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "import.noAtribut"));
			return ;
		}
		
		if (tipNaFile.equals("ods")) {
			doOds(uploadedFile);
			return;
		}
		if (tipNaFile.equals("odf")) {
			doOdf(uploadedFile);
			return;
		}

		if (tipNaFile.equals("pdf")) {
			doPdf(uploadedFile);
			return;
		}
		if (tipNaFile.equals("json")) {
			doJson(uploadedFile);
			return;
		}
		if (tipNaFile.equals("xml")) {
			try {
				doXml(uploadedFile);
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

//		if (selectedItems == null && !tipNaFile.equals("")) {
//			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
//			return;
//		}
		if (attributes.getTarget().size() == 0 && !tipNaFile.equals("")) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
			return;
		}

		if (tipNaFile.equals("xls")) {
			doXls(uploadedFile);
			return;
		}
		if (tipNaFile.equals("xlsx")) {
			doXlsx(uploadedFile);
			return;
		}
		if (tipNaFile.equals("html")) {
			doHtmlNew(uploadedFile);
//			doHtml(uploadedFile);
			return;
		}
		if (tipNaFile.equals("htm")) {
			doHtmlNew(uploadedFile);
//			doHtml(uploadedFile);
			return;
		}
		if (tipNaFile.equals("csv")) {
			doCsv(uploadedFile);
			return;
		}
		if (tipNaFile.equals("txt")) {
			doCsv(uploadedFile);
//			doTxt(uploadedFile);
			return;
		}
		if (tipNaFile.equals("docx")) {
			doDocx(uploadedFile);
			return;
		}
	}


	public void onTransfer(TransferEvent event) {
		List<?> a = event.getItems();
		System.out.println("minava");
	}
	private void doOdf(UploadedFile upFile)  {
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}

		InputStream inputStream = null;
		
		ArrayList<String> ll = new ArrayList<String>();
		ArrayList<String> ll2 = new ArrayList<String>();
		ArrayList<String> lnew = new ArrayList<String>();
		String s2 ;
		String chevrons = "<table:table-row>";
		String column = "<table:table-column>";
		String tdn = "<";
		importedRow = new ArrayList<Object[]>();

		try {
			inputStream = upFile.getInputStream();
		
			 OdfDocument odf = OdfDocument.loadDocument(inputStream);
			 List<TableTableElement> aa = odf.getTables();
			  s2 = aa.get(0).toString();
//			 Iterator<TableTableElement> it = aa.iterator();
//			 while(it.hasNext()) {
//				 s2=it.next().toString();
//			      System.out.println(it.next());
//			 } 
			
			 System.out.println();
				List myList2 = Arrays.asList(s2.split("\n"));
				int p = 0;
				int q = s2.indexOf(chevrons);
				do {
					if (s2.length() > 1) {
						String ww = s2.substring(0, q) + chevrons;
						ll.add(ww);
						s2 = s2.substring(q + 1, s2.length());
						p += 1;
						System.out.println("p::" + p);
					}
					q = s2.indexOf(chevrons);
				} while (q > 1);

				int s = ll.size();
			    String red0 = ll.get(0);
				List myList3 = Arrays.asList(red0.split(column));
			    int brColumn = myList3.size()-1;
				Object[] rowData = new Object[brColumn];

			  for (int i = 1; i < ll.size(); i++) {
				  
					System.out.println("i:" + i);
					Integer a = Integer.valueOf(i);
					if (a.equals(4)) {// Towa se izpolzwa когато има грешка
						System.out.println("i:" + i);
					}

				  
				s2 = ll.get(i);
				p = 0;
				q = s2.indexOf("<text:p>");
				if (q<0) continue;//Значи има празни редове
				do {
					if (s2.length() > 1) {
						int nac = s2.indexOf("<text:p>");
						int krai = s2.indexOf("</text:p>");
						String subString = s2.substring(nac+8, krai);
						if (subString.contains("<text:s></text:s>")) {
							subString=subString.replace("<text:s></text:s>", " ");
						}
						rowData[p]=subString;
						s2 = s2.substring(krai + 1, s2.length());
						p += 1;
						System.out.println("p::" + p);
					}
					q = s2.indexOf("<text:p>");
				} while (q > 1);

				importedRow.add(rowData);
				System.out.println();
			  }
			  
			  System.out.println();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
//	private void doOdfRab(UploadedFile upFile) throws TikaException {
//		if (!doTransferSelItemsToSelVersii(selectedItems)) {
//			return;
//		}
//
//		importedRow = new ArrayList<Object[]>();
//		int brIzbr = selectedItems.size();
//
//		InputStream inputStream = null;
//		Tika tika = new Tika();
//		try {
//			inputStream = upFile.getInputStream();
//			String fileContent = tika.parseToString(inputStream);
//			List myList = Arrays.asList(fileContent.split("\n\n"));
//			for (int i = 0; i < myList.size(); i++) {
//				Object[] rowData = new Object[brIzbr];
//				String a = myList.get(i).toString().replace("\t", "");
//				List myList2 = Arrays.asList(a.split("\n"));
//				int k = 0;
//				if (myList2.size()==0) break;///значи са свършили редовете
//				for (int j = 0; j < myList2.size(); j++) {
//					Object object = myList2.get(j);
//					if ("".equals(object)) continue;//Има празни редове
//					
//					rowData[k]=object;
//					k++;
//				}
//				importedRow.add(rowData);
//		
//			}
//
//	        System.out.println(fileContent);
//
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//	}

	private void doOds(UploadedFile upFile) {
		System.out.println("doOds");
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}
		int brIzbr = selectedItems.size();
		importedRow = new ArrayList<Object[]>();
		SpreadSheet spreadsheet;
		InputStream inputStream = null;
		try {
			inputStream = upFile.getInputStream();
			spreadsheet = new SpreadSheet(inputStream);
			List<Sheet> sheets = spreadsheet.getSheets();
			int brColoni = spreadsheet.getSheet(0).getMaxColumns();
			int size = sheets.size();
//			if (size != brColoni) {
//				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
//						getMessageResourceString(beanMessages, "importVersii.diferAtribut"));
//				return;
//			}
			Object[] aa = sheets.toArray();
            for (Sheet sheet : sheets) {
            	for (int i = 0; i < sheet.getMaxRows(); i++) {
        			Object[] rowData = new Object[brIzbr];

            		for (int j = 0; j < sheet.getMaxColumns(); j++) {
            			Range range = sheet.getRange(i, j);
            			if (hasContent(range)) {
            				rowData[j] = range.getValue();
         				
            				
            			}
            		}
            		System.out.println("zz"+rowData.length);
            		if (isNull(rowData)) continue;
            		importedRow.add(rowData);
            	}
                System.out.println("In sheet " + sheet.getName());

                Range range = sheet.getDataRange();
                System.out.println(range.toString());
            }
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		flAntetka=true;//Има антетка
		

	}
	//Метода показва че всички елементи са null
    private boolean isNull(Object[] rowData) {
		boolean nulla=true;
		for (int i = 0; i < rowData.length; i++) {
			Object object = rowData[i];
			if(object!=null) {
				nulla=false;break;
			}
	}
	return nulla;
}

	private boolean hasContent(Range range) {
        if (range.isPartOfMerge()) {
            return isHeadOfMerge(range);
        }

        Object value = range.getValue();
        if (value != null && !value.toString().isEmpty())
            return true;

        Style style = range.getStyle();
        return !style.isDefault();
    }
    private boolean isHeadOfMerge(Range range)
    {
        if (range.isPartOfMerge()) {
            Range group = range.getMergedCells()[0];
            return group.getRow() == range.getRow() &&
                    group.getColumn() == range.getColumn();
        }
        return false;
    }

	
	
	private void doDocx(UploadedFile upFile) {
		System.out.println("doDocx");
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}
		try {
            File file = new File("D:All/1.docx");
            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            XWPFDocument document = new XWPFDocument(fis);
		} catch (Exception e) {
            e.printStackTrace();
        }
		byte[] fileCont = upFile.getContent();
		String s2 = new String(fileCont);
		importedRow = new ArrayList<Object[]>();
		InputStream inputStream = null;
		fileName = upFile.getFileName();		
		InputStream doc1;
		try {
			doc1 = upFile.getInputStream();
			XWPFDocument document1 = new XWPFDocument(doc1);	
			List<XWPFTable> aaa = document1.getTables();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
	}
	private void doHtmlNew(UploadedFile upFile) throws UnsupportedEncodingException {
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}
		boolean flCharset=false;
		int brIzbr = selectedItems.size();
		errMe=null;
		if (selectedItems==null) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
		}
		importedRow = new ArrayList<Object[]>();
		byte[] fileCont = upFile.getContent();
		String s2 = new String(fileCont);
		if (s2.contains("charset=windows-1251")) {
			flCharset=true;
			InputStream inputStream = null;
			StringBuilder html = new StringBuilder();
			String line;
			try {
				inputStream = upFile.getInputStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "cp1251"));
			int k=0;
			String result = "" ;
			try {
				while ((line = reader.readLine()) != null) {
					k++;
					html.append(line);
//					System.out.println();
					result = html.toString();
				}
			} catch (IOException e) {
				// Do something with the Exception (logging, etc.)
			}
			importedRow=doS2(result,brIzbr);
			
		}else {
			if (s2.contains("charset=UTF-8")) {
				importedRow=doS2(s2,brIzbr);
				flCharset=true;
				System.out.println();
			}
		}
		if (!flCharset) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importRelacii.noCharset"));
			return ;
		}
		Object[] red0 = importedRow.get(0);
		for (Object object : red0) {
			if (object!=null&&object.toString().contains("Код")) {			/// dali първия ред е антетка
				flAntetka=true;break;
			}
			if (object!=null&&object.toString().contains("Code")) {			/// dali първия ред е антетка
				flAntetka=true;break;
			}
		}
		
//		if (flPlus) {
//			importedRowAll=new ArrayList<Object[]>();
//			importedRowOnlyErr=new ArrayList<Object[]>();
//
//			importedRow=doMerniEdinici(importedRow,posMENac,brIzbr);
//		}
		
	}

	private List<Object[]> doS2(String s2, int brIzbr) {
		ArrayList<Object[]> importedRowTemp = new ArrayList<Object[]>();
		
		s2 = s2.replace("&nbsp", "");
		s2 = s2.replace("<pre>", "");
		s2 = s2.replace("</pre>", "");
		s2 = s2.replace("<b>", "");
		ArrayList<String> ll = new ArrayList<String>();
		String trk = "</tr>";
		String trn = "<tr>";
		String tdk = "</td>";
		String tdn = "<td>";
		int q = s2.indexOf(trk);
		do {
			if (s2.length() > 1) {
				String ww = s2.substring(0, q);
				int w = s2.indexOf(trn);
				ww = ww.substring(w + 4, ww.length());
				ll.add(ww);
				s2 = s2.substring(q + 5, s2.length());
			}
			q = s2.indexOf(trk);
		} while (q > 1);

		ll.size();
		System.out.println("doHtml");
		for (String red : ll) {
			int qd = red.indexOf(tdk);
			if (qd < 0)
				continue;
			red = red.replace("\n", "");
			red = red.replace("\t", "");
			red = red.replace("\"", "");
			red = red.replace(" valign=top", "");
			qd = red.indexOf(tdk);
			int l = 0;
			Object[] rowData = new Object[brIzbr];
			do {
				if (red.length() > 1) {
					System.out.println("l:" + l);
					Integer a = Integer.valueOf(l);
					if (l > (brIzbr - 1)) {
						System.out.println("l:" + l);
						JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.malkoAtributi"));
						return importedRowTemp;
					}
					String wd = red.substring(0, qd);
					int w = red.indexOf(tdn);
					wd = wd.substring(w + 4, wd.length());
					rowData[l] = wd;
					l += 1;
					red = red.substring(qd + 5, red.length());
				}
				qd = red.indexOf(tdk);
			} while (qd > 1);
			importedRowTemp.add(rowData);
		}
	
		return importedRowTemp;
	}

	private void doTxt(UploadedFile upFile) {
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}
		errMe=null;
		if (razdelitel.equals("")) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.lipsvaRazdelitel"));
		}
		if (selectedItems==null) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
		}
		InputStream inputStream = null;
		importedRow = new ArrayList<Object[]>();
		int brIzbr = selectedItems.size();
//		new StringBuilder();
		String line;
		try {
			inputStream = upFile.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			line = reader.readLine();
			if (razdelitel.equals("\\|")) razdelitel="|";
			if (!line.contains(razdelitel)) {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noSameDelimiter"));
				return;
			}
			Object[] rowData = new Object[brIzbr];
			if (razdelitel.equals("|")) razdelitel="\\|";
			if (razdelitel.equals('\\')) razdelitel="\\";
			
			line=line.replaceAll("\"", "");
			String[] result = line.split(razdelitel);
			List myList = Arrays.asList(line.split(razdelitel));
			for (int i = 0; i < myList.size(); i++) {
				String aaa = myList.get(i).toString();
				if (aaa.contains("единица") ) {
					posMENac=i;break;
				}
			}
			
				
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int k=0;
		try {
			while ((line = reader.readLine()) != null) {
				k++;
				Object[] rowData = new Object[brIzbr];
				line=line.replaceAll("\"", "");
				List<String> list = new ArrayList<String>(Arrays.asList(line.split(razdelitel)));
				List myList = Arrays.asList(line.split(razdelitel));
				if (izbr.size()!=myList.size()) {
					System.out.println("k:"+k);
					System.out.println("line:"+line);
					String line1=reader.readLine();
					JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.sameDelimiter")+" в ред:"+k);
					break;
				}
				for (int i = 0; i < myList.size(); i++) {
					String a = myList.get(i).toString();
					a = a.replaceAll("\"", "");
					rowData[i] = a;
					if (a.contains("+")) {
						flPlus =true;
					}
				
				}
				importedRow.add(rowData);
			}
		} catch (IOException e) {
			// Do something with the Exception (logging, etc.)
		}
//		if (flPlus) {
//			importedRowAll=new ArrayList<Object[]>();
//			importedRowOnlyErr=new ArrayList<Object[]>();
//
//			importedRow=doMerniEdinici(importedRow,posMENac,brIzbr);
//		}
		
	}

		private void doPdf(UploadedFile upFile)  {
			if (!doTransferSelItemsToSelVersii(selectedItems)) {
				return;
			}
			boolean fl1red = true;
			importedRow = new ArrayList<Object[]>();
			InputStream inputStream = null;
			try {
				inputStream = upFile.getInputStream();
				PDDocument pdfDocument = PDDocument.load(inputStream);
				ObjectExtractor oe = new ObjectExtractor(pdfDocument);
				
				PDPageTree pages = pdfDocument.getPages();
				
				PageIterator it = oe.extract();
//				while (it.hasNext()) {
//					Page page = it.next();
					int numberOfPages = pdfDocument.getNumberOfPages();
					int brStr = pdfDocument.getPages().getCount();
		//			ObjectExtractor oe = new ObjectExtractor(pdfDocument);
					SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm(); // Tabula algo.
		//			int qq=1;
		//			while(oe.extract(qq).getPageNumber()>0) {
					for (int qq = 1; qq < numberOfPages+1; qq++) {
						
						Page page1 = oe.extract(qq); // extract only the first page
						System.out.println(qq);
						List<technology.tabula.Table> table = sea.extract(page1);
						
						technology.tabula.Table red = table.get(0);
						int row = red.getRowCount();
						int col = red.getColCount();
						for (int i = 0; i < row; i++) {
							updateProgress(row, i);
							Object[] rowData = new Object[col];
							for (int j = 0; j < col; j++) {
								RectangularTextContainer cell =red.getCell(i, j);
								String cellTxt = cell.getText();
								cellTxt=cellTxt.replace("\"", "").replace("\r", " ");
								rowData[j] = cellTxt;
								System.out.println("cellTxt: "+cellTxt);
							}
							if (fl1red) {
								selectedVersii = new String[col];
								for (int n = 0; n < col; n++) {
									selectedVersii[n] = rowData[n].toString() ;
								}
								fl1red=false;
							}else {
								importedRow.add(rowData);
							}
						}
					}
//				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println();
		}

	private void doJson(UploadedFile upFile) {
		fileName = upFile.getFileName();
		byte[] fileCont = upFile.getContent();
		String s2 = new String(fileCont);
		ArrayList<String> ll = new ArrayList<String>();
		ArrayList<String> ll2 = new ArrayList<String>();
		ArrayList<String> lnew = new ArrayList<String>();
//		String chevrons = ">";
		s2 = s2.replace("\t", "");
//		s2 = s2.replace("\n}", "");
//		s2 = s2.replace("\t", "");
//		String chevrons = "\r";
		String chevrons = "\n";
		String tdn = "<";
		int p = 0;
		int q = s2.indexOf(chevrons);
		do {
			if (s2.length() > 1) {
				String ww = s2.substring(0, q) + chevrons;
				ll.add(ww);
				s2 = s2.substring(q + 1, s2.length());
//				String s3 = s2.substring(0,30);
//				if (s3.contains("structure")) {
//					System.out.println("ll.size()"+s2);
//				}
				p += 1;
				System.out.println("p::" + p);
			}
			q = s2.indexOf(chevrons);
		} while (q > 1);

		int s = ll.size();
		System.out.println("ll.size()" + s);
		int w = 0;
		boolean flObservation = false;
		for (String red : ll) {
			red = red.toString().trim();
			System.out.println("w:" + w);
			w += 1;
//			Integer e = Integer.valueOf(w);
//			if (e.equals(10700)) {// Towa se izpolzwa когато има грешка
//				System.out.println("j:" + w);
//			}
			if (red.contains("observation")) {
				flObservation = true;
			}else {
				if(!flObservation)  continue;
			}
//			if (red.contains("<?"))    continue;
//			if (red.contains("<mes"))  continue;
//			if (red.equals("")) 	   continue;
//			if (red.contains("</mes")) continue;
//			if (red.contains("Series"))continue;
			ll2.add(red.toString().trim());
		}
		importedRow = new ArrayList<Object[]>();			
		int l = 0;
		Object[] rowData = new Object[3];
		for (String red : ll2) {
			red = red.toString().trim();
			if (red.contains("observation")) continue;
			if (red.contains("values"))      continue;
			if (red.equals("")) 	         continue;
			if (red.contains("id")) {
				int a1 =8;
				String a = doExtractCode(a1, red);
				rowData[l] = a;
				l += 1;
				 continue;
			}
			if (red.contains("description")) {
				int a1 =18;
				String a = doExtractCode(a1, red);
				rowData[l] = a;
				importedRow.add(rowData);
				l = 0;
				rowData = new Object[3];
				continue;
			}
			
		}
		
		
		
			System.out.println();

//		JSONArray a = (JSONArray) parser.parse(new FileReader("c:\\exer4-courses.json"));
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			Map<?, ?> map = mapper.readValue(Paths.get("D:\\All\\Dnevnici\\NSI\\Ново\\SDMX-codelists\\SDMX.3.0.JSON.codelist.primer.json").toFile(), Map.class);
//
//			for (Map.Entry<?, ?> entry : map.entrySet()) {
//		        System.out.println(entry.getKey() + "=" + entry.getValue());
//		    }} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@SuppressWarnings("null")
	private void doXml(UploadedFile upFile) throws IOException {
		izbr = new HashMap<Integer, String>();
		izbr.put(0, "Номер на ниво");
		izbr.put(1, "Код");
		izbr.put(2, "Родител");
		izbr.put(3, "Език");
		izbr.put(4, "Официално наименование");
		izbr.put(5, "Стандартно дълго наименование");// В класификацията код 13 е Стандартно дълго маинемование
//		izbr.put(13, "Стандартно дълго наименование");// В класификацията код 13 е Стандартно дълго маинемование

		selectedVersii = doTransferSelIzbrToSelVersii(izbr);
		importedRow = new ArrayList<Object[]>();
		int brIzbr = 7;// selectedItems.size();
		Integer level = 1;
//		Integer indChild = 0;
//		Integer idPаrent = 0;
		String pаrent = "";
		String lastPаrent = "";
		boolean strPаrent = false;
		fileName = upFile.getFileName();
		byte[] fileCont = upFile.getContent();
		String s2 = new String(fileCont);
		ArrayList<String> ll = new ArrayList<String>();
		ArrayList<String> ll2 = new ArrayList<String>();
		ArrayList<String> lnew = new ArrayList<String>();
//		String chevrons = ">";
		String chevrons = "\r\n";
		String tdn = "<";
		int count = s2.length() - s2.replace(chevrons, "").length();//брой на редовете

		int p = 0;
		int q = s2.indexOf(chevrons);
		long start2 = System.currentTimeMillis();
		do {
			if (s2.length() > 1) {
				String ww = s2.substring(0, q) + chevrons;
				ll.add(ww);
				s2 = s2.substring(q + 1, s2.length());
				String s3 = s2.substring(0, s2.length());
				if (s3.contains("structure")) {
//					System.out.println("ll.size()" + s2);
				}
				p += 1;
			}
			q = s2.indexOf(chevrons);
		} while (q > 1);
		long end2 = System.currentTimeMillis();      
	    System.out.println("Elapsed Time in milli seconds1: "+ (end2-start2));
		int s = ll.size();
//		System.out.println("ll.size()" + s);
		int w = 0;
		start2 = System.currentTimeMillis();
		for (String red : ll) {
			red = red.toString().trim();
			int r = 20;
			if (red.length() < 20) {
				r = red.length();
			}
//			System.out.println("red:" + red.substring(0, r));
			if (w%100==0) {
				System.out.println("w:" + w);
			}
			updateProgress(count/2, w+1);
			w += 1;
			Integer e = Integer.valueOf(w);
			if (e.equals(10700)) {// Towa se izpolzwa когато има грешка
				System.out.println("j:" + w);
			}

			if (red.contains("<? "))
				continue;
			if (red.contains("<mes"))
				continue;
			if (red.contains("</mes"))
				continue;
			if (red.contains("Structure"))
				continue;
//			if (red.contains("</str")) continue;
//			if (red.contains("Codelists")) continue;
			if (red.contains("Annotation"))
				continue;
			if (red.contains("str:Parent")) {
				strPаrent = true;
				continue;
			}
			if (red.contains("en")) {
				langBg = false;
			}

			if (red.contains("</str:Codelists>")) {
				break;
			}
			ll2.add(red.toString().trim());
		}
		end2 = System.currentTimeMillis();      
	    System.out.println("Elapsed Time in milli seconds2: "+ (end2-start2));

		start2 = System.currentTimeMillis();
	    
	    for (String red : ll2) {// Това се повтаря щото горе е махнато за да спре след първата класификация по
								// </str:Codelists>
			if (red.contains("</str"))
				continue;
//			if (red.contains("Codelists"))
//				continue;
			lnew.add(red);
		}
		end2 = System.currentTimeMillis();      
	    System.out.println("Elapsed Time in milli seconds3: "+ (end2-start2));

	    start2 = System.currentTimeMillis();
		if (strPаrent) {
			doParent(lnew);
//			levelNoParent=true;
			System.out.println();
//			importedRow=doSecondLevel(importedRow);
//			doSecondLevel(importedRow);
			importedRow = doSort(importedRow);

			return;
		}
		end2 = System.currentTimeMillis();      
	    System.out.println("Elapsed Time in milli seconds4: "+ (end2-start2));

		System.out.println("doXml");
		int l = 0;
		int i = 0;
		Object[] rowData = new Object[brIzbr];
		// Ново трябва е
		String red;
		String red2;
		String red3 = null;
		String red4 = null;
		String red5 = null;
		brIzbr = 3;
		rowData = new Object[brIzbr];
		boolean flBg = false;
		boolean flEn = false;
		String lastCode = null;
		int size = lnew.size();
//		System.out.println("size" + size);

		start2 = System.currentTimeMillis();
		for (int j = 0; j < size; j++) {
//			System.out.println("j" + j);
			Integer a = Integer.valueOf(j);
			if (a.equals(7972)) {// Towa se izpolzwa когато има грешка
				System.out.println("j:" + j);
			}
			red = lnew.get(j);
			if (red.contains("</")) {
				continue;
			}
			red2 = lnew.get(j + 1);
			if ((j + 2) < size) {
				red3 = lnew.get(j + 2);
			} else {
				red3 = "";
			}
			if ((j + 3) < size) {
				red4 = lnew.get(j + 3);
			} else {
				red4 = "";
			}
			if ((j + 4) < size) {
				red5 = lnew.get(j + 4);
			} else {
				red5 = "";
			}

			// За codeList
			if (red.contains("Codelist")) {
				if (red2.contains("bg")) {
					if (red3.contains("en")) {
						if (red4.contains("Description") && red4.contains("bg")) {
//							System.out.println("CodelistBgEnBg");
							if (red5.contains("Description") && red5.contains("en")) {
								doCodelistBgEnBgEn(level, red, red2, red3, red4, red5);
								j += 4;
								continue;
							} else {
								doCodelistBgEnBg(level, red, red2, red3, red4);
								j += 3;
								continue;
							}
						} else {
							if (red4.contains("Description") && red4.contains("en")) {
								doCodelistBgEnEn(level, red, red2, red3, red4);
								j += 3;
								continue;
							}
						}
//						System.out.println("red3.contains(en)");
						doCodelistBgEn(level, red, red2, red3);
						j += 2;
						continue;
					} else {

					}
//					System.out.println("red2.contains(bg)");

				} else {
					if (red2.contains("en")) {
						doCodelistEn(level, red, red2);
						j += 1;
						continue;
					}
				}
//				System.out.println("red.contains(Codelist");

			} else {
//				j+=1;continue;
			}
			//  код
			if (red.contains("Code")) {
				if (red2.contains("bg")) {

					if (red3.contains("en")) {
						if (red4.contains("Description") && red4.contains("bg")) {
							if (red5.contains("Description") && red5.contains("en")) {
								doCodeBgEnBgEn(level, red, red2, red3, red4, red5);
								j += 4;
								continue;
							} else {
								doCodeBgEnBg(level, red, red2, red3, red4);
								j += 3;
								continue;
							}
						} else {
							if (red4.contains("Description") && red4.contains("en")) {
								doCodeBgEnEn(level, red, red2, red3, red4);
								j += 3;
								continue;

							}
						}
						doCodeBgEn(level, red, red2, red3);
						j += 2;
						continue;
					} else {
						if (red3.contains("bg")) {
							doCodeBg(level, red, red2);
							j += 1;
							continue;
						}
					}
//					System.out.println("bg");
					doCodeBg(level, red, red2);
					j += 1;
					continue;

				} else {
					if (red2.contains("en")) {
						if (red3.contains("Description") && red3.contains("en")) {
							doCodeEnEn(level, red, red2, red3);
							j += 2;
							continue;

						} else {
							doCodeEn(level, red, red2);
							j += 1;
							continue;

						}
					}
				}
			} else {
				j += 1;
				continue;
			}

		}
		end2 = System.currentTimeMillis();      
	    System.out.println("Elapsed Time in milli seconds5: "+ (end2-start2));

		System.out.println("doHtml end");

	}

	private List<Object[]> doSort(List<Object[]> importedRow2) {
		ArrayList<Object[]> importedRowTemp = new ArrayList<Object[]>();
		ArrayList<Object[]> importedRowTemp2 = new ArrayList<Object[]>();
		importedRowTemp.addAll(importedRow2);
		ArrayList<Object[]> temp4 = new ArrayList<Object[]>();
		ArrayList<Object[]> temp3 = new ArrayList<Object[]>();
		ArrayList<Object[]> temp2 = new ArrayList<Object[]>();
		ArrayList<Object[]> temp1 = new ArrayList<Object[]>();
		int nivo = 1;

		for (int i = 0; i < importedRowTemp.size(); i++) {
			Object[] red = importedRowTemp.get(i);
			System.out.println("i" + i);
			Integer b = Integer.valueOf(i);
			if (b.equals(403)) {// Towa se izpolzwa когато има грешка
				System.out.println("i:" + i);
			}
			if (red[0].equals(nivo)) {
				temp1.add(red);
				continue;
			}
			if (red[0].equals(nivo + 1)) {
				temp2.add(red);
				continue;
			}
			if (red[0].equals(nivo + 2)) {
				temp3.add(red);
				continue;
			}
			if (red[0].equals(nivo + 3)) {
				temp4.add(red);
				continue;
			}

		}
		System.out.println("i:");
		for (int i = 0; i < temp1.size(); i++) {
			Object[] str1 = importedRowTemp.get(i);
			importedRowTemp2.add(str1);
//			temp1.remove(i);
//			i-=1;
			String code1 = str1[1].toString();
			for (int j = 0; j < temp2.size(); j++) {
				System.out.println("j:" + j);
				Object[] str2 = temp2.get(j);
				String codeParent2 = str2[2].toString();
				if (code1.equals(codeParent2)) {
					String code2 = str2[1].toString().replace("\"", "");
					importedRowTemp2.add(str2);
//					temp2.remove(j);
//					j-=1;
					for (int k = 0; k < temp3.size(); k++) {
						System.out.println("k:" + k);
						Object[] str3 = temp3.get(k);
						String codeParent3 = str3[2].toString();
						System.out.println("codeParent3:" + codeParent3);
						Integer q = Integer.valueOf(k);
						if (q.equals(0)) {// Towa se izpolzwa когато има грешка
							System.out.println("i:" + i);
						}
						if (code2.equals(codeParent3)) {
							String code3 = str3[1].toString();
							importedRowTemp2.add(str3);
//							temp3.remove(k);
//							k-=1;
							for (int m = 0; m < temp4.size(); m++) {
								System.out.println("m:" + m);
								Object[] str4 = temp4.get(m);
								String codeParent4 = str4[2].toString();
								System.out.println("codeParent4:" + codeParent4);
								String code4 = str4[1].toString();
								if (code3.equals(codeParent4)) {
									importedRowTemp2.add(str4);
//									temp4.remove(m);							k-=1;
//									m-=1;
								}
							}

						}
					}

				}

			}
		}

		System.out.println("i:");

		return importedRowTemp2;
	}

	private List<Object[]> doSecondLevel(List<Object[]> importedRow2) {
		ArrayList<Object[]> importedRowTemp = new ArrayList<Object[]>();
		importedRowTemp.addAll(importedRow2);
		ArrayList<String> temp4 = new ArrayList<String>();
		ArrayList<String> temp3 = new ArrayList<String>();
		ArrayList<String> temp2 = new ArrayList<String>();
		Object a = null;
		int nivo = 4;
		for (int i = 0; i < importedRowTemp.size(); i++) {
			Object[] red = importedRowTemp.get(i);
			System.out.println("i" + i);
			Integer b = Integer.valueOf(i);
			if (b.equals(403)) {// Towa se izpolzwa когато има грешка
				System.out.println("i:" + i);
			}
			boolean imaGo = false;
			if (red[0].equals(nivo)) {
				a = red[2];
				imaGo = doImaLiRoditel(a, nivo, importedRowTemp);
			} else {
				continue;
			}
			if (imaGo) {
				importedRowTemp.remove(i);
				i -= i;
			} else {
				if (a != null) {
					if (!doZapisan(a, temp4)) {
						temp4.add(a.toString());
					}
				}
			}
		}
		nivo = 3;
		for (int i = 0; i < importedRowTemp.size(); i++) {
			Object[] red = importedRowTemp.get(i);
			boolean imaGo = false;
			if (red[0].equals(nivo)) {
				a = red[2];
				imaGo = doImaLiRoditel(a, nivo, importedRowTemp);
			} else {
				continue;
			}
			if (imaGo) {
				importedRowTemp.remove(i);
				i -= i;
			} else {
				if (!doZapisan(a, temp3)) {
					temp3.add(a.toString());
				}
			}
		}
		nivo = 2;
		for (int i = 0; i < importedRowTemp.size(); i++) {
			Object[] red = importedRowTemp.get(i);
			boolean imaGo = false;
			if (red[0].equals(nivo)) {
				a = red[2];
				imaGo = doImaLiRoditel(a, nivo, importedRowTemp);
			} else {
				continue;
			}
			if (imaGo) {
				importedRowTemp.remove(i);
				i -= i;
			} else {
				if (!doZapisan(a, temp2)) {
					temp2.add(a.toString());
				}
			}
		}

		System.out.println();
		return importedRowTemp;
	}

	private boolean doZapisan(Object a, ArrayList<String> temp4) {
		boolean imaGo = false;

		for (int i = 0; i < temp4.size(); i++) {
			String red = temp4.get(i);
			if (a.toString().equals(red)) {
				imaGo = true;
				break;
			}
		}
		return imaGo;
	}

	private boolean doImaLiRoditel(Object a, int nivo, List<Object[]> importedRow2) {
		boolean imaGo = false;

		for (int i = 0; i < importedRow2.size(); i++) {
			Object[] red = importedRow2.get(i);
			if (!red[0].equals(nivo - 1)) {
				continue;
			}
			if (a.equals(red[1])) {
				imaGo = true;
				break;
			}

		}
		return imaGo;

	}
	public void changeType() {
		System.out.println();
		if (insertType.equals("1")){
			importedRowAll.addAll(importedRow);
			importedRow=new ArrayList<Object[]>();
			if (importedRowOnlyErr.size()==0) {
				importedRowOnlyErr=doOnlyErr(importedRowAll,izbr.size());
			}
			importedRow.addAll(getImportedRowOnlyErr());
		}else {
			importedRow=new ArrayList<Object[]>();
			importedRow.addAll(importedRowAll);
		}
	}
	private void doParent(ArrayList<String> lnew) {
		System.out.println("doXml");
//		int l = 0;
//		int i = 0;
		Integer level = 1;
//		Object[] rowData = new Object[brIzbr];
		// Ново трябва е
		String red = null;
		String red2;
		String red3 = null;
		String red4 = null;
		String red5 = null;
		int brIzbr = 3;
		Object[] rowData = new Object[brIzbr];
		boolean flBg = false;
		boolean flEn = false;
		String lastCode = null;
		int size = lnew.size();
//		System.out.println("size" + size);
		for (int j = 0; j < size; j++) {
			System.out.println("j" + j);
			Integer a = Integer.valueOf(j);
			if (a.equals(7972)) {// Towa se izpolzwa когато има грешка
				System.out.println("j:" + j);
			}
			red = lnew.get(j);
			if (red.contains("</")) {
				continue;
			}
			red2 = lnew.get(j + 1);
			if ((j + 2) < size) {
				red3 = lnew.get(j + 2);
			} else {
				red3 = "";
			}
			if (red.contains("Codelist")) {
				if (red2.contains("bg")) {
					if (red3.contains("en")) {
						System.out.println("red2.contains(bg)");

					} else {
						if (red2.contains("en")) {
							doCodelistEn(level, red, red2);
							j += 1;
							continue;
						}
					}
					System.out.println("red.contains(Codelist");

				} else {
					if (red2.contains("en")) {
						doCodelistEn(level, red, red2);
						j += 1;
						continue;
					}
				}
				System.out.println("red.contains(Codelist");
			}
			if (red.contains("Code")) {
				if (red2.contains("bg")) {

					if (red3.contains("en")) {
						doCodeBgEn(level, red, red2, red3);
						j += 2;
						continue;
					} else {
						if (red3.contains("bg")) {
							doCodeBg(level, red, red2);
							j += 1;
							continue;
						}
					}
					System.out.println("bg");
					doCodeBg(level, red, red2);
					j += 1;
					continue;

				} else {
					if (red2.contains("en")) {
						if (red3.contains("<Ref id")) {
							doCodeEnRef(level, red, red2, red3);
							j += 2;
							continue;

						} else {
							doCodeEn(level, red, red2);
							j += 1;
							continue;

						}
					}
				}
			} else {
				j += 1;
				continue;
			}

		}
	}

	private void doCodeEnRef(Integer level, String red, String red2, String red3) {
		int l = 0;
		int levelTemp;
		Object[] rowData = new Object[6];
		int a1 = red3.indexOf("id=") + 4;
		String c = doExtractCode(a1, red3);// red3.substring(red3.indexOf(">")+1,red3.indexOf("</")).trim();
		levelTemp = c.length() + 1;
		rowData[l] = levelTemp;
		l += 1;
		int a2 = red.indexOf("id=") + 4;
		String a = doExtractCode(a2, red);
		rowData[l] = a;
		l += 1;
		rowData[l] = c;
		l += 1;
		rowData[l] = "en";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;

		importedRow.add(rowData);
	}

	private void doCodeEnEn(Integer level, String red, String red2, String red3) {
		int l = 0;
		Object[] rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=");
		int a2 = red.indexOf("urn");

		String a = red.substring(red.indexOf("id=") + 3, red.indexOf("urn")).trim();

		rowData[l] = a;
		l += 1;
		rowData[l] = lastPаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		String c = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;

		importedRow.add(rowData);

	}

	private void doCodeBg(Integer level, String red, String red2) {
		int l = 0;
		Object[] rowData = new Object[6];
		level = 2;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=");
		int a2 = red.indexOf("urn");

		String a = red.substring(red.indexOf("id=") + 3, red.indexOf("urn")).trim();
		rowData[l] = a;
		l += 1;
		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		int a3 = red2.indexOf(">");
		int a4 = red2.indexOf("</");

		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		importedRow.add(rowData);
		System.out.println("doCodeEn");

	}

	private void doCodelistBgEnEn(Integer level, String red, String red2, String red3, String red4) {
		int l = 0;
		Object[] rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=") + 4;
		String a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;
		lastPаrent = a;
		pаrent = "";
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		importedRow.add(rowData);
		// втори ред
		l = 0;
		rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		rowData[l] = a;
		l += 1;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		b = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		String c = red4.substring(red4.indexOf(">") + 1, red4.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;

		importedRow.add(rowData);
	}

	private void doCodelistBgEnBg(Integer level, String red, String red2, String red3, String red4) {
		int l = 0;
		Object[] rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=") + 4;
		String a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;
		lastPаrent = a;
		pаrent = "";
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		String c = red4.substring(red4.indexOf(">") + 1, red4.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;

		importedRow.add(rowData);
		// втори ред
		l = 0;
		rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		rowData[l] = a;
		l += 1;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		b = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;

		importedRow.add(rowData);

		System.out.println("doCodelistBgEnBg");
	}

	private void doCodeBgEnEn(Integer level, String red, String red2, String red3, String red4) {
		System.out.println("doCodeBgEnBgEn");
		int l = 0;
		Object[] rowData = new Object[6];
		level = 2;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=") + 4;
		String a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;

		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;

		importedRow.add(rowData);
		l = 0;
		rowData = new Object[6];
		level = 2;
		rowData[l] = level;
		l += 1;
		rowData[l] = a;
		l += 1;
		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		b = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		String c = red4.substring(red4.indexOf(">") + 1, red4.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;

		importedRow.add(rowData);
		System.out.println("doCodeBgEnEn");
	}

	private void doCodelistBgEnBgEn(Integer level, String red, String red2, String red3, String red4, String red5) {
		int l = 0;
		Object[] rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=") + 4;
		String a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;
		lastPаrent = a;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		String c = red4.substring(red4.indexOf(">") + 1, red4.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;
		importedRow.add(rowData);
		l = 0;
		rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		rowData[l] = a;
		l += 1;
		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		b = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		c = red5.substring(red5.indexOf(">") + 1, red5.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;

		importedRow.add(rowData);
		System.out.println("doCodelistBgEnBgEn");
	}

	private String doExtractCode(int a1, String red) {
		String a = "";
		for (int i = a1; i < red.length(); i++) {
			if (String.valueOf(red.charAt(i)).equals("\"")) {
				break;
			} else {
				a += red.charAt(i);
			}
		}
		return a;
	}

	private void doCodelistEn(Integer level, String red, String red2) {
		System.out.println("doCodelistEn");
		int l = 0;
		Object[] rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=") + 4;
		String a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;
		lastPаrent = a;
		pаrent = "";
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		importedRow.add(rowData);
	}

	private void doCodeBgEnBg(Integer level, String red, String red2, String red3, String red4) {
		System.out.println("doCodeBgEnBgEn");
		int l = 0;
		Object[] rowData = new Object[6];
		level = 2;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=") + 4;
		String a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;
		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		String c = red4.substring(red4.indexOf(">") + 1, red4.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;
		importedRow.add(rowData);
		l = 0;
		rowData = new Object[6];
		level = 2;
		rowData[l] = level;
		l += 1;
		a1 = red.indexOf("id=") + 4;
		a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;
		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		b = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;

		importedRow.add(rowData);
	}

	private void doCodeBgEnBgEn(Integer level, String red, String red2, String red3, String red4, String red5) {
		System.out.println("doCodeBgEnBgEn");
		int l = 0;
		Object[] rowData = new Object[6];
		level = 2;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=") + 4;
		String a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;
		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		String c = red4.substring(red4.indexOf(">") + 1, red4.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;
		importedRow.add(rowData);
		// втори ред
		l = 0;
		rowData = new Object[6];
		rowData[l] = level;
		l += 1;
		rowData[l] = a;
		l += 1;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		b = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		c = red5.substring(red5.indexOf(">") + 1, red5.indexOf("</")).trim();
		rowData[l] = c;
		l += 1;

		importedRow.add(rowData);
	}

	private void doCodeEn(int level, String red, String red2) {
		int l = 0;
		Object[] rowData = new Object[6];
		level = 2;
		rowData[l] = level;
		l += 1;
		String a = red.substring(red.indexOf("id=") + 3, red.indexOf("urn")).trim();
		rowData[l] = a;
		l += 1;
		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		importedRow.add(rowData);
		System.out.println("doCodeEn");
	}

	private void doCodeBgEn(int level, String red, String red2, String red3) {
		int l = 0;
		Object[] rowData = new Object[6];
		level = 2;
		rowData[l] = level;
		l += 1;
		String a = red.substring(red.indexOf("id=") + 3, red.indexOf("urn")).trim();
		rowData[l] = a;
		l += 1;
		pаrent = lastPаrent;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		importedRow.add(rowData);
		// втори ред
		l = 0;
		rowData = new Object[6];
		rowData[l] = level;
		l += 1;
		rowData[l] = a;
		l += 1;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		b = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		importedRow.add(rowData);
	}

	private void doCodelistBgEn(int level, String red, String red2, String red3) {
		int l = 0;
		Object[] rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		int a1 = red.indexOf("id=") + 4;
		String a = doExtractCode(a1, red);
		rowData[l] = a;
		l += 1;
		lastPаrent = a;
		pаrent = "";
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "bg";
		l += 1;
		String b = red2.substring(red2.indexOf(">") + 1, red2.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		importedRow.add(rowData);
		// втори ред
		l = 0;
		rowData = new Object[6];
		level = 1;
		rowData[l] = level;
		l += 1;
		rowData[l] = a;
		l += 1;
		rowData[l] = pаrent;
		l += 1;
		rowData[l] = "en";
		l += 1;
		b = red3.substring(red3.indexOf(">") + 1, red3.indexOf("</")).trim();
		rowData[l] = b;
		l += 1;
		importedRow.add(rowData);
	}


	private void doHtml(UploadedFile upFile) {
//		System.out.println("doHtml");
//		doTransferSelItemsToSelVersii(izbr);
		if (!doTransferSelItemsToSelVersii(selectedItems)) {
			return;
		}

//		selectedVersii = doTransferSelIzbrToSelVersii(izbr);
		int brIzbr = selectedItems.size();
		importedRow = new ArrayList<Object[]>();
		byte[] fileCont = upFile.getContent();
		String s2 = new String(fileCont);
		s2 = s2.replace("&nbsp", "");
		s2 = s2.replace("<pre>", "");
		s2 = s2.replace("</pre>", "");
		s2 = s2.replace("<b>", "");
		ArrayList<String> ll = new ArrayList<String>();
		String trk = "</tr>";
		String trn = "<tr>";
		String tdk = "</td>";
		String tdn = "<td>";
		int q = s2.indexOf(trk);
		do {
			if (s2.length() > 1) {
				String ww = s2.substring(0, q);
				int w = s2.indexOf(trn);
				ww = ww.substring(w + 4, ww.length());
				ll.add(ww);
				s2 = s2.substring(q + 5, s2.length());
			}
			q = s2.indexOf(trk);
		} while (q > 1);

		ll.size();
		System.out.println("doHtml");
		for (String red : ll) {
			int qd = red.indexOf(tdk);
			if (qd < 0)
				continue;
			red = red.replace("\n", "");
			red = red.replace("\t", "");
			red = red.replace("\"", "");
			red = red.replace(" valign=top", "");
			qd = red.indexOf(tdk);
			int l = 0;
			Object[] rowData = new Object[brIzbr];
			do {
				if (red.length() > 1) {
					System.out.println("l:" + l);
					Integer a = Integer.valueOf(l);
					if (l > (brIzbr - 1)) {
						System.out.println("l:" + l);
						JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.malkoAtributi"));
						return;
					}
					String wd = red.substring(0, qd);
					int w = red.indexOf(tdn);
					wd = wd.substring(w + 4, wd.length());
					rowData[l] = wd;
					l += 1;
					red = red.substring(qd + 5, red.length());
				}
				qd = red.indexOf(tdk);
			} while (qd > 1);
			importedRow.add(rowData);
		}
		Object[] red0 = importedRow.get(0);
		for (Object object : red0) {
			if (object!=null&&object.toString().contains("Код")) {			/// dali първия ред е антетка
				flAntetka=true;break;
			}
			if (object!=null&&object.toString().contains("Code")) {			/// dali първия ред е антетка
				flAntetka=true;break;
			}
		}
	
//		flAntetka=true;
		System.out.println("doHtml end");
	}

	private String[] doTransferSelIzbrToSelVersii(HashMap<Integer, String> izbr2) {
		int brItens = izbr2.size();
		String[] selectedVersiiTemp = new String[brItens];

		for (int i = 0; i < izbr2.size(); i++) {
			selectedVersiiTemp[i] = izbr2.get(i).replace("]", "").replace("[", "");
		}
//		setSelectedVersii(selectedVersiiTemp);
		System.out.println("doTransferSelItemsToSelVersii");
		return selectedVersiiTemp;
	}

	private boolean doCompareXlsx(XSSFRow row, List<String> selectedItems2) {
		Boolean err = false;
		int selected = selectedItems2.size();
		;
		int j = 0;
		XSSFCell cell = row.getCell(j);
		do {
			j += 1;
			cell = row.getCell(j);
		} while (cell != null);

		if (j != selected) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.onlyOnAtribut"));
			return true;
		}
		j = 0;
		cell = row.getCell(j);
		String[] selectedVersiiTemp = new String[selected];
		do {
			selectedVersiiTemp[j] = cell.toString();
			j += 1;
			cell = row.getCell(j);
		} while (cell != null);
		setSelectedVersii(selectedVersiiTemp);
//		System.out.println("i" + j);
		return err;
	}
	private boolean doCompareXlsxPckList(XSSFRow row, List<String> target) {
		Boolean err = false;
		int selected = target.size();
		;
		int j = 0;
		XSSFCell cell = row.getCell(j);
		do {
			j += 1;
			cell = row.getCell(j);
		} while (cell != null);

		if (j != selected) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.onlyOnAtribut"));
			return true;
		}
		j = 0;
		cell = row.getCell(j);
		String[] selectedVersiiTemp = new String[selected];
		do {
			selectedVersiiTemp[j] = cell.toString();
			j += 1;
			cell = row.getCell(j);
		} while (cell != null);
		setSelectedVersii(selectedVersiiTemp);
//		System.out.println("i" + j);
		return err;
	}

	
	private boolean doCompareXls(HSSFRow row, List<String> selectedItems2) {
		Boolean err = false;
		int selected = selectedItems2.size();
		int j = 0;
		HSSFCell cell = row.getCell(j);
		do {
			j += 1;
			cell = row.getCell(j);
		} while (cell != null);

		if (j != selected) {
//			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.diferAtribut"));
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
					getMessageResourceString(beanMessages, "importVersii.diferAtribut"));
			return true;
		}
		j = 0;
		cell = row.getCell(j);
		String[] selectedVersiiTemp = new String[selected];
		do {
			selectedVersiiTemp[j] = cell.toString();
			j += 1;
			cell = row.getCell(j);
		} while (cell != null);
		setSelectedVersii(selectedVersiiTemp);
//		System.out.println("j" + j);
		return err;
	}

	public boolean strIsNoCIryl(String redStr, String str2) {
		boolean foundIt = true;
		test: for (int j = 0; j < redStr.length(); j++) {
			String bukva = redStr.substring(j, j + 1);
			if (!str2.contains(bukva)) {
				foundIt = false;
				break test;
			}
		}
		return foundIt;
	}

	private String KirSpace = "ЯВЕРТЪУИОПШЩЮАСДФГХЙКЛЗЬЦЖБНМЧявертъуиопшщюасдфгхйклзьцжбнмч ";

	private void doImaLevelBezParent() {
		String a = izbr.toString();
		boolean zaTransform = false;
		
		if (a.contains("ниво") && !a.contains("Родител")) {
			levelNoParent = true;
			zaTransform = true;
		}
		if (a.contains("Level") && !a.contains("Parent")) {
			levelNoParent = true;
			zaTransform = true;
		}
		
		if (zaTransform) {
			importedRow = transorm(importedRow);
			int sIzbr = izbr.size();
			HashMap<Integer, String> temp = new HashMap<Integer, String>();
			if (getCurrentLang()==1) {
				temp.put(-1, "[Родител]");
			}
			if (getCurrentLang()==2) {
				temp.put(-1, "[Parent]");
			}
			for (int j = 0; j < izbr.size(); j++) {
				String a1 = izbr.get(j);
				temp.put(j, a1);
			}
			izbr.clear();
			izbr.putAll(temp);
		}
		System.out.println("doImaLevelBezParent");
	}

	private List<Object[]> transorm(List<Object[]> importedRow2) {
		int q = izbr.size();
		int wk=0;
		Object[] tt = new Object[q + 1];
		ArrayList<Object[]> importedRowTemp = new ArrayList<Object[]>();
		for (Object[] objects : importedRow2) {
			System.out.println("k:"+wk);
			tt = new Object[q + 1];
			wk++;
			tt[q] = "";
			System.out.println("size:"+objects.length);
			System.out.println("objects:"+objects);
			
			for (int i = 0; i < objects.length; i++) {
				tt[i] = objects[i];
			}
			importedRowTemp.add(tt);
		}
//		System.out.println("transorm");
		ArrayList<Object[]> importedRowT = new ArrayList<Object[]>();
		Object[] red = importedRowTemp.get(0);// Прехвърляме антетката

//		if (!tipNaFile.equals("pdf")&&!tipNaFile.equals("xls")&&!tipNaFile.equals("txt")
//				) {// махаме антетката за файловете без pdf
		if (flAntetka) {
		importedRowT.add(red);
			red = importedRowTemp.get(1);
			importedRowTemp.remove(0);
			importedRowT.add(red);
		}else {
			importedRowT.add(red);
		}
		
		String codeLevel1 = "";
		String codeLevel2 = "";
		String codeLevel3 = "";
		String codeLevel4 = "";
		String codeLevel5 = "";
		String codeLevel6 = "";

		for (int i = 0; i < importedRowTemp.size() - 1; i++) {// От 1 за да се махне антетката Преместено при Save
			System.out.println("i:" + i);
			Integer a = Integer.valueOf(i);
			if (a.equals(3662)) {
				System.out.println("i:" + i);
			}

			red = importedRowTemp.get(i);
			Object[] slRed = importedRowTemp.get(i + 1);
			String code = "";
			String slcode = "";
			Integer levelNumber = 0;
			Integer slevelNumber = 0;
			String parent = "";
			for (int j = 0; j < izbr.size(); j++) {
				String a1 = izbr.get(j);
				System.out.println("a1"+a1);
				a1 = a1.replace("[", "").replace("]", "");
				Integer aa = allAtrib.get(a1);
				if (aa.equals(1)) {// Код
					code = red[j].toString();
					slcode = slRed[j].toString();
					continue;
				}
				if (aa.equals(6)) {// Номер на ниво
					String redj = red[j].toString();
					redj = redj.replace("\"", "");
					levelNumber = Double.valueOf(redj).intValue();
//					levelNumber = Integer.parseInt(redj);
					if (levelNumber.equals(1)) {
						codeLevel1 = code;
					}
					if (levelNumber.equals(2)) {
						codeLevel2 = code;
					}
					if (levelNumber.equals(3)) {
						codeLevel3 = code;
					}
					if (levelNumber.equals(4)) {
						codeLevel4 = code;
					}
					if (levelNumber.equals(5)) {
						codeLevel5 = code;
					}
					if (levelNumber.equals(6)) {
						codeLevel6 = code;
					}

					redj = slRed[j].toString();
					slevelNumber = Double.valueOf(redj).intValue();
//					slevelNumber = Integer.parseInt(redj);
					break;
				}
			}
			tt = new Object[q + 1];
			for (int k = 0; k < slRed.length; k++) {
				tt[k] = slRed[k];
			}

			if (slevelNumber > levelNumber) {
				tt[q] = code;
			}
			if (slevelNumber.equals(levelNumber)) {
				if (slevelNumber.equals(2)) {
					tt[q] = codeLevel1;
				}
				if (slevelNumber.equals(3)) {
					tt[q] = codeLevel2;
				}
				if (slevelNumber.equals(4)) {
					tt[q] = codeLevel3;
				}
				if (slevelNumber.equals(5)) {
					tt[q] = codeLevel4;
				}
				if (slevelNumber.equals(6)) {
					tt[q] = codeLevel5;
				}

			}
			if (slevelNumber < levelNumber) {
				if (slevelNumber.equals(1)) {
					tt[q] = "";
					codeLevel1 = code;
				}
				if (slevelNumber.equals(2)) {
					tt[q] = codeLevel1;
					codeLevel2 = code;
				}
				if (slevelNumber.equals(3)) {
					tt[q] = codeLevel2;
					codeLevel3 = code;
				}
				if (slevelNumber.equals(4)) {
					tt[q] = codeLevel3;
					codeLevel4 = code;
				}
				if (slevelNumber.equals(5)) {
					tt[q] = codeLevel4;
					codeLevel5 = code;
				}
				if (slevelNumber.equals(6)) {
					tt[q] = codeLevel5;
					codeLevel6 = code;
				}

			}
			importedRowT.add(tt);
		}

		return importedRowT;
	}

	public void actionSaveBgEn() throws DbErrorException {
		// Проверка дали има запис
//		doImaLevelBezParent();
//		System.out.println("actionSave");
		// променливи за табл. POSITION_SCHEME
		// ID взема се от SEQ_POSITION
		// VERSION_ID = versionId ,което се взема се при натискане на табчето Import от
		// VersionRdit.html ред 606 виж initData()
		boolean flBgEn = false;
		String code = "0";
		Integer positionId = 0;
		String codeFull = "";
		String codeSeparate = "";// Dа се сложи око разширението е txt da se wzeme razdelitel
		if (tipNaFile.equals("txt")) {// При txt
			codeSeparate = razdelitel;
		}
		Integer codeType = 1;
		Integer status = 1;
		Integer level = 1;
		Integer indChild = 0;
		Integer idPаrent = 0;
		String pаrent = "";
		Integer lastLevelNumber = 0;
		Integer levelNumber = 1;
		Integer idPrev = 0;
		Integer idPrevLast = 0;
		Integer idPrevLevel1Last = 0;
		Integer idPrevLevel2Last = 0;
		Integer idPrevLevel3Last = 0;
		Integer idPrevLevel4Last = 0;
		Integer slevelNumber = 0;
		HashMap<String, Integer> codeParent = new HashMap<String, Integer>();
		// променливи за табл. POSITION_LANG
		// ID взема се от SEQ_POSITION_LANG
		// VERSION_ID = versionId ,което се взема се при натискане на табчето Import от
		// VersionRdit.html ред 606 виж initData()
		String oficialTitlе = "";
		String longTitlе = "";
		String shortTitlе = "";
		String alternativeTitlе = "";
		String comment = "";
		String includes = "";
		String alsoIncludes = "";
		String exludes = "";
		String rules = "";
		String prepratka = "";
		String statPokazatel = "";
		Integer langZapis = 1;

		// za tabl POSITION_UNITS
		Integer unit = null;
		Integer typeUnit = null;
		String unitString = "";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String tekData = sdf.format(new Date());
		tekData = "\'" + tekData + "\'";

//		JPA.getUtil().begin();
		String redInsertInto = "INSERT INTO POSITION_SCHEME ( ID,VERSION_ID,CODE,"
				+ " CODE_FULL,CODE_SEPARATE,CODE_TYPE,DEFF_NAME,STATUS,LEVEL_NUMBER,"
				+ " ID_PREV,ID_PARENT,IND_CHILD, DATE_REG," + " USER_REG ) VALUES ";
		int intNac = 0;
		if (!tipNaFile.equals("htm") && !tipNaFile.equals("csv") && !tipNaFile.equals("")) {// При тях антетката е
																								// махната!!!! Da se
																								// провери за всички
																								// файлове
			intNac = 1;
		}

		for (int i = intNac; i < importedRow.size(); i++) {// Почваме от i=1 защото и=0 е антетката
			Object[] red = importedRow.get(i);
			String redStr = "";
			for (int j = 0; j < red.length; j++) {
				redStr += red[j];
			}
			if (strIsNoCIryl(redStr, KirSpace)) {
				continue;// Значи е Антетка която съдържа само кирилица и спейс
			}
			Object[] red1 = null;
			if ((i + 1) < importedRow.size()) {
				red1 = importedRow.get(i + 1);
			}
			flBgEn = false;
			if (red1 != null) {
				if (red[3].equals("bg") && red1[3].equals("en")) {
					flBgEn = true;
				}
				if (red[3].equals("en") && red1[3].equals("bg")) {
					flBgEn = true;
				}
			}

			String redInsert = "";
			redInsert += "( NEXT VALUE FOR SEQ_POSITION," + versionId + ",";
			int nac;
			int krai;
			if (levelNoParent) {
				nac = -1;
				krai = izbr.size() - 1;
			} else {
				nac = 0;
				krai = izbr.size();
			}

			for (int j = nac; j < krai; j++) {
				String a1 = izbr.get(j);
				a1 = a1.replace("[", "").replace("]", "");
				Integer aa = allAtrib.get(a1);
				if (aa.equals(0)) {// Номер
					continue;
				}
				if (aa.equals(1)) {// Код
					code = red[j].toString();
					continue;
				}
				if (aa.equals(2)) {// Пълен код
					codeFull = red[j].toString();
					continue;
				}
				if (aa.equals(3)) {// Код с разделител
					codeSeparate = red[j].toString();
					continue;
				}
				if (aa.equals(4)) {// Тип
					String redj = red[j].toString();
					codeType = Integer.parseInt(redj);
					continue;
				}
				if (aa.equals(5)) {// Статус
					String redj = red[j].toString();
					status = Integer.parseInt(redj);
					continue;
				}
				if (aa.equals(6)) {// Номер на ниво
					String redj = red[j].toString();
					levelNumber = Double.valueOf(redj).intValue();
					if ((i + 2) < importedRow.size()) {
						Object[] slRed;
						if (flBgEn) {
							slRed = importedRow.get(i + 2);
						} else {
							slRed = importedRow.get(i + 1);
						}
//						slRed = importedRow.get(i+1);
						String slRedj = slRed[j].toString();
						slevelNumber = Double.valueOf(slRedj).intValue();
						if (slevelNumber > levelNumber) {
							indChild = 1;
						} else {
							indChild = 0;
						}
						continue;
					} else {
						indChild = 0;
						continue;
					}
				}
				if (aa.equals(9)) {// Език
					String redj = red[j].toString().replace("\"", "");
					langZapis = ezik.get(redj);
					continue;
				}
				
				if (aa.equals(NSIConstants.CODE_ZNACHENIE_NACIONALNA)) {// когато typeUnit=8 от allAtrib от къде да се
					unitString	= null;									// вземат Международна измерителна единица
					if (red[j] != null) {
						unitString = (String) red[j].toString();
						typeUnit = NSIConstants.CODE_ZNACHENIE_NACIONALNA;
						continue;
					}
				}
				if (aa.equals(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {// когато typeUnit=8 
					unitString	= null;									// вземат Международна измерителна единица
					if (red[j] != null) {
						unitString = (String) red[j].toString();
						typeUnit = NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA;
						continue;
					}
				}

				if (aa.equals(10)) {// Родител
					if (levelNoParent) {
						int poslZapis = izbr.size() - 1;
						pаrent = red[poslZapis].toString();
					} else {
						if (red[j] != null) {
							pаrent = red[j].toString();
						}
					}
					continue;
				}

				if (aa.equals(11)) {
					oficialTitlе = (String) red[j].toString();
					continue;
				}
				if (aa.equals(12)) {
					if (level.equals(1) && (red[j] != null)) {
						shortTitlе = (String) red[j].toString();
						if (shortTitlе.length() > 99) {
							shortTitlе = shortTitlе.substring(0, 90) + "...";
							continue;
						}
					}
					if ((red[j] != null)) {
						shortTitlе = (String) red[j].toString();
						continue;
					}
				}

				if (aa.equals(13)) {// Standartno дълго naimenowanie
					if (level.equals(1) && (red[j] != null)) {
						longTitlе = (String) red[j].toString();
						if (longTitlе.length() > 99) {
							longTitlе = longTitlе.substring(0, 90) + "...";
							continue;
						}
					}
					if ((red[j] != null)) {
						longTitlе = (String) red[j].toString();
						continue;
					}
					continue;
				}

				if (aa.equals(14)) {// Алтернативно наименование
					if (level.equals(1) && (red[j] != null)) {
						alternativeTitlе = (String) red[j].toString();
						if (alternativeTitlе.length() > 99) {
							alternativeTitlе = alternativeTitlе.substring(0, 90) + "...";
							continue;
						}
					}
					if ((red[j] != null)) {
						alternativeTitlе = (String) red[j].toString();
						continue;
					}
				}

				if (aa.equals(15)) {// Коментар
					comment = red[j].toString();
					continue;
				}
				if (aa.equals(16)) {// Включва
					includes = red[j].toString();
					continue;
				}
				if (aa.equals(17)) {// Включва още
					alsoIncludes = red[j].toString().trim();
					continue;
				}
				if (aa.equals(18)) {// Не включва
					exludes = red[j].toString().trim();
					continue;
				}
				if (aa.equals(19)) {// Правила
					rules = red[j].toString().trim();
					continue;
				}
				if (aa.equals(20)) {// Препратка
					prepratka = red[j].toString().trim();
					continue;
				}
				if (aa.equals(21)) {
					statPokazatel = red[j].toString().trim();
					continue;
				}

			}

			if (codeParent.size() > 0) {
				if (codeParent.get(pаrent) != null) {
					idPаrent = codeParent.get(pаrent);
				}
				if (levelNumber.equals(1)) {
					idPаrent = 0;
				}
			}
			idPrev = 0;
			if (levelNumber.equals(lastLevelNumber)) {
				idPrev = idPrevLast;
			}
			if (lastLevelNumber < levelNumber) {
				idPrev = 0;
			}
			if (lastLevelNumber > levelNumber) {
				if (levelNumber.equals(1)) {
					idPrev = idPrevLevel1Last;
				}
				if (levelNumber.equals(2)) {
					idPrev = idPrevLevel2Last;
				}
				if (levelNumber.equals(3)) {
					idPrev = idPrevLevel3Last;
				}
				if (levelNumber.equals(4)) {
					idPrev = idPrevLevel4Last;
				}
			}
			lastLevelNumber = levelNumber;

			redInsert += ":code,null,:codeSeparate,1,null,:status ,:levelNumber ,:idPrev ,:idPаrent ,:indChild ,:DAT ,:USERID  )";
			try {
				Date datAction = new Date();
				JPA.getUtil().begin();
				redInsert = redInsertInto + redInsert;
//				System.out.println("actionSave POSITION_SCHEME: " + redInsert);
				Query qq = JPA.getUtil().getEntityManager().createNativeQuery(redInsert);
				qq.setParameter("code", code);
				qq.setParameter("codeSeparate", codeSeparate);
				qq.setParameter("status", status);
				qq.setParameter("levelNumber", levelNumber);
				qq.setParameter("idPrev", idPrev);
				qq.setParameter("idPаrent", idPаrent);
				qq.setParameter("indChild", indChild);

				qq.setParameter("USERID", getUserId());
				qq.setParameter("DAT", datAction);

				qq.executeUpdate();

//				JPA.getUtil().commit(); // Успешно завършване на транзакцията
				String idShema = "select id from POSITION_SCHEME where VERSION_ID=:versionId and ID_PREV=:idPrev and ID_PARENT=:idParent and CODE="
						+ "'" + code + "'";
				qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
				qq.setParameter("versionId", versionId);
				qq.setParameter("idPrev", idPrev);
				qq.setParameter("idParent", idPаrent);
				Integer temp1 = (Integer) qq.getSingleResult();
//				System.out.println("aaa");
				code = code.replace("\"", "");
				if (temp1 != null) {
					idPrevLast = Integer.valueOf(temp1.intValue());
					codeParent.put(code, idPrevLast);
					if (levelNumber.equals(1)) {
						idPrevLevel1Last = idPrevLast;
					}
					if (levelNumber.equals(2)) {
						idPrevLevel2Last = idPrevLast;
					}
					if (levelNumber.equals(3)) {
						idPrevLevel3Last = idPrevLast;
					}
					if (levelNumber.equals(4)) {
						idPrevLevel4Last = idPrevLast;
					}
				}

//				JPA.getUtil().begin();
				// Zapis w POSITION_LANG
				String redIntoPositionLang = "INSERT INTO POSITION_LANG (ID,POSITION_ID,LANG,OFFICIAL_TITLE,LONG_TITLE,SHORT_TITLE,ALTERNATE_TITLES,COMMENT,INCLUDES,"
						+ " ALSO_INCLUDES,EXCLUDES,RULES,PREPRATKA,STAT_POKAZATEL) VALUES ";
				String redPositionLangValues = " ( NEXT VALUE FOR SEQ_POSITION_LANG, :idPrevLast ,:lang," // +
																											// getCurrentLang()
						+ " :oficialTitlе,:longTitlе,:shortTitlе,:alternativeTitlе,:comment,:includes,:alsoIncludes,:exludes,:rules,:prepratka,:statPokazatel)";
				String redInsertPositionLang = redIntoPositionLang + redPositionLangValues;
				qq = JPA.getUtil().getEntityManager().createNativeQuery(redInsertPositionLang);
				qq.setParameter("idPrevLast", idPrevLast);
				qq.setParameter("lang", langZapis);
				qq.setParameter("oficialTitlе", oficialTitlе);
				qq.setParameter("longTitlе", longTitlе);
				qq.setParameter("shortTitlе", shortTitlе);
				qq.setParameter("alternativeTitlе", alternativeTitlе);
				qq.setParameter("comment", comment);
				qq.setParameter("includes", includes);
				qq.setParameter("alsoIncludes", alsoIncludes);
				qq.setParameter("exludes", exludes);
				qq.setParameter("rules", rules);

				qq.setParameter("prepratka", prepratka);
				qq.setParameter("statPokazatel", statPokazatel);

				qq.executeUpdate();
				if (flBgEn) {
					for (int j = nac; j < krai; j++) {
						String a1 = izbr.get(j);
						a1 = a1.replace("[", "").replace("]", "");
						Integer aa = allAtrib.get(a1);
						if (aa.equals(0)) {// Номер
							continue;
						}
						if (aa.equals(1)) {// Код
//					code = red1[j].toString();
							continue;
						}
						if (aa.equals(2)) {// Пълен код
//					codeFull = red1[j].toString();
							continue;
						}
						if (aa.equals(3)) {// Родител
//					if (levelNoParent) {
//						int poslZapis = izbr.size()-1;
//						pаrent = red[poslZapis].toString();
//					}else {
//						if (red[j]!=null) {
//							pаrent = red1[j].toString();
//						}
//					}
							continue;
						}
						if (aa.equals(4)) {// Тип
//					String redj = red1[j].toString();
//					codeType = Integer.parseInt(redj);
							continue;
						}
						if (aa.equals(5)) {// Статус
//					String redj = red1[j].toString();
//					status = Integer.parseInt(redj);
							continue;
						}
						if (aa.equals(6)) {// Номер на ниво
//					String redj = red1[j].toString();
//					levelNumber = Double.valueOf(redj).intValue();
//					if ((i+1)<importedRow.size()) {
//						Object[] slRed = importedRow.get(i+1);
//						String slRedj = slRed[j].toString();
//						slevelNumber=Double.valueOf(slRedj).intValue();
////						slevelNumber=Integer.parseInt(slRedj);
//						if (slevelNumber>levelNumber) {
//							indChild=1;
//						}else {
//							indChild=0;
//						}
//						continue;
//					}else{
//						indChild=0;
//						continue;
//					}
							continue;
						}
						if (aa.equals(9)) {// Език
							String redj = red1[j].toString().replace("\"", "");
							langZapis = ezik.get(redj);
							continue;
						}

//						if (aa.equals(NSIConstants.CODE_ZNACHENIE_NACIONALNA)) {// когато typeUnit=8 от allAtrib от къде
//																				// да се вземат Международна измерителна
//																				// единица
//
//							String redj;
//							redj = (String) red1[j].toString();
//							if (redj.equals("")) {
//								unit = null;
//								typeUnit = null;
//								continue;
//
//							} else {
//								unit = merniEdinici.get(redj);
//								typeUnit = 7;
//								continue;
//							}
//						}
						if (aa.equals(NSIConstants.CODE_ZNACHENIE_NACIONALNA)) {// когато typeUnit=8 от allAtrib от къде да се
							unitString	= null;									// вземат Международна измерителна единица
							if (red[j] != null) {
								unitString = (String) red[j].toString();
								typeUnit = NSIConstants.CODE_ZNACHENIE_NACIONALNA;
								continue;
							}
						}
						if (aa.equals(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {// когато typeUnit=8 
							unitString	= null;									// вземат Международна измерителна единица
							if (red[j] != null) {
								unitString = (String) red[j].toString();
								typeUnit = NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA;
								continue;
							}
						}
						
						
						
						if (aa.equals(11)) {
							oficialTitlе = (String) red1[j].toString();
							continue;
						}
//				if (aa.equals(13)) {
//					if (level.equals(1)&&(red[j]!=null)) {
//						codeSeparate = (String) red1[j].toString();
//						if (codeSeparate.length()>99) {
//							codeSeparate=codeSeparate.substring(0,90)+"...";
//							continue;
//						}
//					}
//					if ((red[j]!=null)) {
//						alternativeTitlе= (String) red[j].toString();
//						continue;
//					}
//				}

						if (aa.equals(15)) {// Коментар
							comment = red1[j].toString();
							continue;
						}
						if (aa.equals(16)) {// Включва
							includes = red1[j].toString();
							continue;
						}
						if (aa.equals(17)) {// Включва още
							alsoIncludes = red1[j].toString();
							continue;
						}
						if (aa.equals(18)) {// Не включва
							exludes = red1[j].toString();
							continue;
						}
						if (aa.equals(19)) {// Правила
							rules = red1[j].toString();
							continue;
						}
						if (aa.equals(20)) {// Препратка
							prepratka = red1[j].toString().trim();
							continue;
						}
						if (aa.equals(21)) {
							statPokazatel = red1[j].toString().trim();
							continue;
						}

					}

					// Zapis na vtori ezik
					// Zapis w POSITION_LANG
					redIntoPositionLang = "INSERT INTO POSITION_LANG (ID,POSITION_ID,LANG,OFFICIAL_TITLE,LONG_TITLE,SHORT_TITLE,ALTERNATE_TITLES,COMMENT,INCLUDES,"
							+ " ALSO_INCLUDES,EXCLUDES,RULES,PREPRATKA,STAT_POKAZATEL) VALUES ";
					redPositionLangValues = " ( NEXT VALUE FOR SEQ_POSITION_LANG, :idPrevLast ,:lang," // +
																										// getCurrentLang()
							+ " :oficialTitlе,:longTitlе,:shortTitlе,:alternativeTitlе,:comment,:includes,:alsoIncludes,:exludes,:rules,:prepratka,:statPokazatel)";
					redInsertPositionLang = redIntoPositionLang + redPositionLangValues;
					qq = JPA.getUtil().getEntityManager().createNativeQuery(redInsertPositionLang);
					qq.setParameter("idPrevLast", idPrevLast);
					qq.setParameter("lang", langZapis);
					qq.setParameter("oficialTitlе", oficialTitlе);
					qq.setParameter("longTitlе", longTitlе);
					qq.setParameter("shortTitlе", shortTitlе);
					qq.setParameter("alternativeTitlе", alternativeTitlе);
					qq.setParameter("comment", comment);
					qq.setParameter("includes", includes);
					qq.setParameter("alsoIncludes", alsoIncludes);
					qq.setParameter("exludes", exludes);
					qq.setParameter("rules", rules);

					qq.setParameter("prepratka", prepratka);
					qq.setParameter("statPokazatel", statPokazatel);

					qq.executeUpdate();
					i += 1;
				}
				// Zapis w POSITION_UNITS
				if (unit != null) {
					String redIntoPositionUnit = "INSERT INTO POSITION_UNITS (ID,POSITION_ID,UNIT,TYPE_UNIT) VALUES ";
					String redPositionUnitValues = " ( NEXT VALUE FOR SEQ_POSITION_UNITS, :idPrevLast,:unit,:typeUnit )";
					String redInsertPositionUnit = redIntoPositionUnit + redPositionUnitValues;
//					System.out.println("actionSave POSITION_UNITS: " + redInsertPositionUnit);
					qq = JPA.getUtil().getEntityManager().createNativeQuery(redInsertPositionUnit);
					qq.setParameter("idPrevLast", idPrevLast);
					qq.setParameter("unit", unit);
					qq.setParameter("typeUnit", typeUnit);

					qq.executeUpdate();
				}

				JPA.getUtil().commit(); // Успешно завършване на транзакцията
			} catch (Exception e) {
				LOGGER.error("Грешка при извличане на елемент по id на предходен", e);
//				System.out.println("Level:"+levelNumber);
//				System.out.println("kod:"+code);
				return;
			} finally {
				JPA.getUtil().closeConnection();
//			System.out.println("finally");
			}
		}
		JPA.getUtil().closeConnection();

		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
				getMessageResourceString("ui_beanMessages", SUCCESSAVEMSG));
		//Долното от Илия за да се показват позициите веднага след запис
		VersionEditBean veb = (VersionEditBean) JSFUtils.getManagedBean("versionEdit");
		if(veb!=null) {
			veb.setPosition3Initialized(false);
		}

		
	}

	public void actionBack() {
		if (razdelitel.equals('\\')) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noSameDelimiter"));
	
		}
	}
	public void onComplete() {
		System.out.println("onComplete()");
		progress=0;	
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Progress Completed"));  
	}
	public void cancel() {  
		progress = null; 
	}
	
	public void actionSave() throws DbErrorException {
		// Проверка дали има запис
		System.out.println("actionSave");
		progress=0;
		if (importedRow.isEmpty()) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importRelacii.nImoprt"));
			return;		}
		if (doImaliZapis()) {
			return;
		}
		if (!langBg) {
			actionSaveBgEn();
			return;
		}
		doImaLevelBezParent();
		// променливи за табл. POSITION_SCHEME
		// ID взема се от SEQ_POSITION
		// VERSION_ID = versionId ,което се взема се при натискане на табчето Import от
		// VersionRdit.html ред 606 виж initData()
		String code = "0";
		Integer positionId = 0;
		String codeFull = "";
		String codeSeparate = "";// Dа се сложи око разширението е txt da se wzeme razdelitel
		if (tipNaFile.equals("txt")||tipNaFile.equals("csv")) {// При txt
			codeSeparate = razdelitel;
		}
		Integer codeType = 1;
		Integer status = 1;
		Integer level = 1;
		Integer indChild = 0;
		Integer idPаrent = 0;
		String  pаrent = "";
		Integer lastLevelNumber = 0;
		Integer levelNumber = 1;
		Integer idPrev = 0;
		Integer idPrevLast = 0;
		Integer idPrevLevel1Last = 0;
		Integer idPrevLevel2Last = 0;
		Integer idPrevLevel3Last = 0;
		Integer idPrevLevel4Last = 0;
		Integer idPrevLevel5Last = 0;
		Integer idPrevLevel6Last = 0;
		Integer slevelNumber = 0;
		HashMap<String, Integer> codeParent = new HashMap<String, Integer>();
		// променливи за табл. POSITION_LANG
		// ID взема се от SEQ_POSITION_LANG
		// VERSION_ID = versionId ,което се взема се при натискане на табчето Import от
		// VersionRdit.html ред 606 виж initData()
		String oficialTitlе = "";
		String longTitlе = "";
		String shortTitlе = "";
		String alternativeTitlе = "";
		String comment = "";
		String includes = "";
		String alsoIncludes = "";
		String exludes = "";
		String rules = "";
		String prepratka = "";
		String statPokazatel = "";
		Integer langZapis = getCurrentLang();

		// za tabl POSITION_UNITS
		Integer unit = null;
		Integer typeUnitNac = null;
		Integer typeUnitMejd = null;
			String unitStringNac = "";
		String unitStringMejd = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String tekData = sdf.format(new Date());
		tekData = "\'" + tekData + "\'";

//		JPA.getUtil().begin();
		String redInsertInto = "INSERT INTO POSITION_SCHEME ( ID,VERSION_ID,CODE,"
				+ " CODE_FULL,CODE_SEPARATE,CODE_TYPE,DEFF_NAME,STATUS,LEVEL_NUMBER,"
				+ " ID_PREV,ID_PARENT,IND_CHILD, DATE_REG," + " USER_REG ) VALUES ";
		int intNac = 0;
//		if (!tipNaFile.equals("htm") 
//				&& !tipNaFile.equals("txt") && !tipNaFile.equals("pdf")&& !tipNaFile.equals("xls") 
//				&& !tipNaFile.equals("")) {// При тях антетката е
//			intNac = 1;
//		}
		if (flAntetka) {
			intNac = 1;//Има АНТЕТКА: ods,csv,xlsx,
		}else {
			intNac = 0;//Няма Антетка:htm,pdf,xls,txt
		}
		int size = importedRow.size();
		int step2 = 1;
		int step3 = 1;
		int step = step2+step3;
		for (int i = intNac; i < importedRow.size(); i++) {// Почваме от i=1 защото и=0 е антетката
			unitStringNac="";unitStringMejd="";
//			System.out.println("i" + i);
			Integer a = Integer.valueOf(i);
			if (a.equals(5)) {// Towa se izpolzwa когато има грешка
				System.out.println("i:" + i);
			}
	    	updateProgress(size, i+1);
			if (a.equals(size)) {// Значи е последния запис
				progress=100;
				System.out.println("i:" + i);
			}

			Object[] red = importedRow.get(i);
			String redStr = "";
			for (int j = 0; j < red.length; j++) {
				redStr += red[j];
			}
			if (strIsNoCIryl(redStr, KirSpace)) {
//				lang=2;
//				System.out.println("Kir");
				continue;// Значи е Антетка която съдържа само кирилица и спейс
			}
			String redInsert = "";
			redInsert += "( NEXT VALUE FOR SEQ_POSITION," + versionId + ",";
			int nac;
			int krai;
			if (levelNoParent) {
				nac = -1;
				krai = izbr.size() - 1;
			} else {
				nac = 0;
				krai = izbr.size();
			}
//			for (int j = nac; j < attributes.getTarget().size(); j++) {
//				String a1 = attributes.getTarget().get(i);
				
			for (int j = nac; j < krai; j++) {
				String a1 = izbr.get(j);
				a1 = a1.replace("[", "").replace("]", "");
				Integer aa = allAtrib.get(a1);
				if (aa.equals(0)) {// Номер
					continue;
				}
				if (aa.equals(1)) {// Код
					if (red[j]!=null) {
						code = red[j].toString();
					}
					continue;
				}
				if (aa.equals(2)) {// Пълен код
					codeFull = red[j].toString();
					continue;
				}
				if (aa.equals(3)) {// Код с разделител
					codeSeparate = red[j].toString();
					continue;
				}
				if (aa.equals(4)) {// Тип
					String redj = red[j].toString();
//					codeType = Integer.parseInt(redj);
					codeType = Double.valueOf(redj).intValue();
					continue;
				}
				if (aa.equals(5)) {// Статус
					String redj = red[j].toString();
//					status = Integer.parseInt(redj);
					status = Double.valueOf(redj).intValue();
					continue;
				}
				if (aa.equals(6)) {// Номер на ниво
					String redj = red[j].toString();
					levelNumber = Double.valueOf(redj).intValue();
					if ((i + 1) < importedRow.size()) {
						Object[] slRed = importedRow.get(i + 1);
						String slRedj = slRed[j].toString();
						slevelNumber = Double.valueOf(slRedj).intValue();
//						slevelNumber=Integer.parseInt(slRedj);
						if (slevelNumber > levelNumber) {
							indChild = 1;
						} else {
							indChild = 0;
						}
						continue;
					} else {
						indChild = 0;
						continue;
					}
				}
				if (aa.equals(9)) {// Език
					String redj = red[j].toString().replace("\"", "");
					langZapis = ezik.get(redj);
					continue;
				}

				if (aa.equals(NSIConstants.CODE_ZNACHENIE_NACIONALNA)) {// когато typeUnit=8 от allAtrib от къде да се
					if (red[j] != null&&!"".equals(red[j])) {
						if ("".equals(unitStringNac)) {
							unitStringNac = (String) red[j].toString().trim().replace("\"", "");
						}else {
							unitStringNac+=razdelitelME+ (String) red[j].toString().trim().replace("\"", "");
						}
						typeUnitNac = NSIConstants.CODE_ZNACHENIE_NACIONALNA;
					}
					continue;
				}
				if (aa.equals(NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA)) {// когато typeUnit=8 
					if (red[j] != null&&!"".equals(red[j])) {
						if ("".equals(unitStringMejd)) {
							unitStringMejd = (String) red[j].toString().trim().replace("\"", "");
						}else {
							unitStringMejd+=razdelitelME+ (String) red[j].toString().trim().replace("\"", "");
						}
						typeUnitMejd= NSIConstants.CODE_ZNACHENIE_MEJDUNARODNA;
					}
					continue;
				}
				if (aa.equals(10)) {// Родител
					if (levelNoParent) {
						int poslZapis = izbr.size() - 1;
						pаrent = red[poslZapis].toString();
					} else {
						if (red[j] != null) {
							pаrent = red[j].toString();
						}
					}
					continue;
				}

				if (aa.equals(11)) {// Официално наименование
					oficialTitlе = (String) red[j].toString();
					continue;
				}
				if (aa.equals(12)) {// Съандартно кратко наименование
					if (level.equals(1) && (red[j] != null)) {
						shortTitlе = (String) red[j].toString();
						if (shortTitlе.length() > 99) {
							shortTitlе = shortTitlе.substring(0, 90) + "...";
							continue;
						}
					}
					if ((red[j] != null)) {
						shortTitlе = (String) red[j].toString();
						continue;
					}
				}

				if (aa.equals(13)) {// Standartno дълго naimenowanie
					if (level.equals(1) && (red[j] != null)) {
						longTitlе = (String) red[j].toString();
						if (longTitlе.length() > 99) {
							longTitlе = longTitlе.substring(0, 90) + "...";
							continue;
						}
					}
					if ((red[j] != null)) {
						longTitlе = (String) red[j].toString();
						continue;
					}
				}

				if (aa.equals(14)) {// Алтернативно наименование
					if (level.equals(1) && (red[j] != null)) {
						alternativeTitlе = (String) red[j].toString();
						if (alternativeTitlе.length() > 99) {
							alternativeTitlе = alternativeTitlе.substring(0, 90) + "...";
							continue;
						}
					}
					if ((red[j] != null)) {
						alternativeTitlе = (String) red[j].toString();
						continue;
					}
				}

				if (aa.equals(15)) {// Коментар
					if (red[j] != null) {
						comment = red[j].toString();
					}
					continue;
				}
				if (aa.equals(16)) {// Включва
					if (red[j] != null) {
						includes = red[j].toString();
					}
					continue;
				}
				if (aa.equals(17)) {// Включва още
					if (red[j] != null) {
						alsoIncludes = red[j].toString();
					}
					continue;
				}
				if (aa.equals(18)) {// Не включва
					if (red[j] != null) {
						exludes = red[j].toString();
					}
					continue;
				}
				if (aa.equals(19)) {// Правила
					if (red[j] != null) {
						rules = red[j].toString();
					}
					continue;
				}
				if (aa.equals(20)) {// Препратка
					if (red[j] != null) {
						prepratka = red[j].toString();
					}
					continue;
				}
				if (aa.equals(21)) {
					if (red[j] != null) {
						statPokazatel = red[j].toString().trim();
					}
					continue;
				}

			}
 
			if (codeParent.size() > 0) {
				if (codeParent.get(pаrent) != null) {
					idPаrent = codeParent.get(pаrent);
				}
				if (levelNumber.equals(1)) {
					idPаrent = 0;
				}
			}
			idPrev = 0;
			if (levelNumber.equals(lastLevelNumber)) {
				idPrev = idPrevLast;
			}
			if (lastLevelNumber < levelNumber) {
				idPrev = 0;
			}
			if (lastLevelNumber > levelNumber) {
				if (levelNumber.equals(1)) {
					idPrev = idPrevLevel1Last;
				}
				if (levelNumber.equals(2)) {
					idPrev = idPrevLevel2Last;
				}
				if (levelNumber.equals(3)) {
					idPrev = idPrevLevel3Last;
				}
				if (levelNumber.equals(4)) {
					idPrev = idPrevLevel4Last;
				}
				if (levelNumber.equals(5)) {
					idPrev = idPrevLevel5Last;
				}
				if (levelNumber.equals(6)) {
					idPrev = idPrevLevel6Last;
				}
			}
			lastLevelNumber = levelNumber;

			redInsert += ":code,null,:codeSeparate,1,null,:status ,:levelNumber ,:idPrev ,:idPаrent ,:indChild ,:DAT ,:USERID  )";
			try {
				Query qq;
				JPA.getUtil().begin();
				if (!flBg&&!flEn){
					Date datAction = new Date();
					redInsert = redInsertInto + redInsert;
//					System.out.println("actionSave POSITION_SCHEME: " + redInsert);
					qq = JPA.getUtil().getEntityManager().createNativeQuery(redInsert);
					qq.setParameter("code", code);
					qq.setParameter("codeSeparate", codeSeparate);
					qq.setParameter("status", status);
					qq.setParameter("levelNumber", levelNumber);
					qq.setParameter("idPrev", idPrev);
					qq.setParameter("idPаrent", idPаrent);
					qq.setParameter("indChild", indChild);
					qq.setParameter("USERID", getUserId());
					qq.setParameter("DAT", datAction);

					int ww = qq.executeUpdate();
					String idShema = "select id from POSITION_SCHEME where VERSION_ID=:versionId and ID_PREV=:idPrev and ID_PARENT=:idParent and CODE="
							+ "'" + code + "'";
					qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
					qq.setParameter("versionId", versionId);
					qq.setParameter("idPrev", idPrev);
					qq.setParameter("idParent", idPаrent);
					Integer temp1 = (Integer) qq.getSingleResult();
					if (temp1 != null) {
						idPrevLast = Integer.valueOf(temp1.intValue());
						codeParent.put(code, idPrevLast);
						if (levelNumber.equals(1)) {
							idPrevLevel1Last = idPrevLast;
						}
						if (levelNumber.equals(2)) {
							idPrevLevel2Last = idPrevLast;
						}
						if (levelNumber.equals(3)) {
							idPrevLevel3Last = idPrevLast;
						}
						if (levelNumber.equals(4)) {
							idPrevLevel4Last = idPrevLast;
						}
						if (levelNumber.equals(5)) {
							idPrevLevel5Last = idPrevLast;
						}
						if (levelNumber.equals(6)) {
							idPrevLevel6Last = idPrevLast;
						}

					}

				}
				if ((flBg&&!flEn)||(!flBg&&flEn)){
					String idShema = "select id from POSITION_SCHEME where VERSION_ID=:versionId and CODE="
							+ "'" + code + "'";
					qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
					qq.setParameter("versionId", versionId);
					Integer temp1 = (Integer) qq.getSingleResult();
					if (temp1 != null) {
						idPrevLast = Integer.valueOf(temp1.intValue());
					}
				}
		

				// Zapis w POSITION_LANG
				String redIntoPositionLang = "INSERT INTO POSITION_LANG (ID,POSITION_ID,LANG,OFFICIAL_TITLE,LONG_TITLE,SHORT_TITLE,ALTERNATE_TITLES,COMMENT,INCLUDES,"
						+ " ALSO_INCLUDES,EXCLUDES,RULES,PREPRATKA,STAT_POKAZATEL) VALUES ";
				String redPositionLangValues = " ( NEXT VALUE FOR SEQ_POSITION_LANG, :idPrevLast ,:lang," // +
																											// getCurrentLang()
						+ " :oficialTitlе,:longTitlе,:shortTitlе,:alternativeTitlе,:comment,:includes,:alsoIncludes,:exludes,:rules,:prepratka,:statPokazatel)";
				String redInsertPositionLang = redIntoPositionLang + redPositionLangValues;
				qq = JPA.getUtil().getEntityManager().createNativeQuery(redInsertPositionLang);
				qq.setParameter("idPrevLast", idPrevLast);
				qq.setParameter("lang", langZapis);
				qq.setParameter("oficialTitlе", oficialTitlе);
				qq.setParameter("longTitlе", longTitlе);
				qq.setParameter("shortTitlе", shortTitlе);
				qq.setParameter("alternativeTitlе", alternativeTitlе);
				qq.setParameter("comment", comment);
				qq.setParameter("includes", includes);
				qq.setParameter("alsoIncludes", alsoIncludes);
				qq.setParameter("exludes", exludes);
				qq.setParameter("rules", rules);

				qq.setParameter("prepratka", prepratka);
				qq.setParameter("statPokazatel", statPokazatel);

				qq.executeUpdate();

				// Zapis w POSITION_UNITS
				if (unitStringNac != null&&!"".equals(unitStringNac)) {
					if (flPlus||posMENac>2) {
						List<String> myList ;
						myList = Arrays.asList(unitStringNac.split(razdelitelME));
						for (int m = 0; m < myList.size(); m++) {
							String aaa = myList.get(m).toString().trim().replaceAll("\\s+","");;
								unit = merniEdinici.get(aaa);
							if (unit!=null) {
								doInsert(idPrevLast,unit,typeUnitNac);
							}
						}
					}else {
						unitStringNac=unitStringNac.replaceAll("\\s+","");
							if (unitStringNac.equals("м2")) unitStringNac="кв.м";//Изключение за м2
							unit = merniEdinici.get(unitStringNac);
							if (unit!=null) {
								doInsert(idPrevLast,unit,typeUnitNac);
							}
					}
				}
				//За Международни
				if (unitStringMejd != null&&!"".equals(unitStringMejd)) {
					if (flPlus||posMEMejd>2) {
						List<String> myList;
						myList = Arrays.asList(unitStringMejd.split(razdelitelME));
						for (int m = 0; m < myList.size(); m++) {
							String aaa = myList.get(m).toString().trim().replaceAll("\\s+","");;
								unit = merniEdinici.get(aaa);
							if (unit!=null) {
								doInsert(idPrevLast,unit,typeUnitMejd);
							}
						}
					}else {
						unitStringMejd=unitStringMejd.replaceAll("\\s+","");
							if (unitStringMejd.equals("м2")) unitStringMejd="кв.м";//Изключение за м2
							unit = merniEdinici.get(unitStringMejd);
							if (unit!=null) {
								doInsert(idPrevLast,unit,typeUnitMejd);
							}
					}
				}

				JPA.getUtil().commit(); // Успешно завършване на транзакцията
			} catch (Exception e) {
				LOGGER.error("Грешка при извличане на елемент по id на предходен", e);
//				System.out.println("Level:"+levelNumber);
//				System.out.println("kod:"+code);
				return;
			} finally {
				JPA.getUtil().closeConnection();
//			System.out.println("finally");
			}
		}
		JPA.getUtil().closeConnection();
		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
				getMessageResourceString("ui_beanMessages", SUCCESSAVEMSG));
		//Долното от Илия за да се показват позициите веднага след запис
		VersionEditBean veb = (VersionEditBean) JSFUtils.getManagedBean("versionEdit");
		if(veb!=null) {
			veb.setPosition3Initialized(false);
		}

		
	}

	private void doInsert(Integer idPrevLast, Integer unit, Integer typeUnit) {
		String redIntoPositionUnit = "INSERT INTO POSITION_UNITS (ID,POSITION_ID,UNIT,TYPE_UNIT) VALUES ";
		String redPositionUnitValues = " ( NEXT VALUE FOR SEQ_POSITION_UNITS, :idPrevLast,:unit,:typeUnit )";
		String redInsertPositionUnit = redIntoPositionUnit + redPositionUnitValues;
//		System.out.println("actionSave POSITION_UNITS: " + redInsertPositionUnit);
		Query qq = JPA.getUtil().getEntityManager().createNativeQuery(redInsertPositionUnit);
		qq.setParameter("idPrevLast", idPrevLast);
		qq.setParameter("unit", unit);
		qq.setParameter("typeUnit", typeUnit);

		qq.executeUpdate();
	}

	private boolean doImaliZapis() throws DbErrorException {
		Boolean imaZapis = false;
		flBg= false;
		flEn= false;
		try {
			JPA.getUtil().begin(); // Начало на транзакция
			String idShema = "select id from POSITION_SCHEME where VERSION_ID=:versionId";
			Query qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
			qq.setParameter("versionId", versionId);
			List temp1 = qq.getResultList();
//			System.out.println("aaa");
//			JPA.getUtil().commit();
			if (temp1.size() != 0) {
				Integer positionId = (Integer) temp1.get(0);
				idShema = "select id from POSITION_LANG where POSITION_ID=:positionId and LANG=:lang";
				qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
				qq.setParameter("positionId", positionId);
				qq.setParameter("lang", 1);
				List temp2 = qq.getResultList();
				System.out.println();
				if (temp2.size() != 0) {
					flBg = true;
				}
				qq.setParameter("lang", 2);
				temp2 = qq.getResultList();
				System.out.println();
				if (temp2.size() != 0) {
					flEn = true;
				}
			
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при извличане на елемент по id на предходен", e);
			throw new DbErrorException("Грешка при извличане на елемент по id на предходен", e);
		}
		JPA.getUtil().closeConnection();
		if (getCurrentLang()==1 && flBg) {
			imaZapis = true;
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.imaZapis")+" на Bg");
		}
		if (getCurrentLang()==2 && flEn) {
			imaZapis = true;
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.imaZapis")+" на En");
		}
		if (flBg&&flEn) imaZapis = true;
		return imaZapis;
	}

//	public Integer getCodeClassifUser() {
//		return SysConstants.CODE_CLASSIF_USERS;
//	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public List<String[]> getSysClassifList() {
		return sysClassifList;
	}

	public void setSysClassifList(List<String[]> sysClassifList) {
		this.sysClassifList = sysClassifList;
	}

	public String getSelClassif() {
		return selClassif;
	}

	public void setSelClassif(String selClassif) {
		this.selClassif = selClassif;
	}

	public Date getToday() {
		return new Date();
	}

	private Date dateFrom;

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getRazdelitel() {

		return razdelitel;
	}

	public void setRazdelitel(String razdelitel) {
		this.razdelitel = razdelitel;
	}

	public List<String> getSelectedItems() {
		return selectedItems;
	}

	public void setSelectedItems(List<String> selectedItems) {
		this.selectedItems = selectedItems;
	}

	public List<String> getItems() {
		return items;
	}

	public void setItems(List<String> items) {
		this.items = items;
	}

	public String getTextArea() {
		return textArea;
	}

	public void setTextArea(String textArea) {
		this.textArea = textArea;
	}

	public String getTextOutput() {
		return textOutput;
	}

	public void setTextOutput(String textOutput) {
		this.textOutput = textOutput;
	}

	public List<TempOpis> getOpList() {
		return opList;
	}

	public void setOpList(List<TempOpis> opList) {
		this.opList = opList;
	}

	public List<String> getVersiis() {
		return versiis;
	}

	public void setVersiis(List<String> versiis) {
		this.versiis = versiis;
	}

	public List<Object[]> getImportedRow() {
		return importedRow;
	}

	public void setImportedRow(List<Object[]> importedRow) {
		this.importedRow = importedRow;
	}

	public String[] getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(String[] selectedItem) {
		this.selectedItem = selectedItem;
	}

	public String[] getSelectedVersii() {
		return selectedVersii;
	}

	public void setSelectedVersii(String[] selectedVersii) {
		this.selectedVersii = selectedVersii;
	}

	public VersionEditBean getMainBean() {
		return mainBean;
	}

	public void setMainBean(VersionEditBean mainBean) {
		this.mainBean = mainBean;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getLang() {
		return lang;
	}

	public Integer getVersionId() {
		return versionId;
	}

	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}

	public HashMap<Integer, String> getIzbr() {
		return izbr;
	}

	public void setIzbr(HashMap<Integer, String> izbr) {
		this.izbr = izbr;
	}

	public Integer getNomerPored() {
		return nomerPored;
	}

	public void setNomerPored(Integer nomerPored) {
		this.nomerPored = nomerPored;
	}
	
	/**
	 * за експорт в pdf - добавя заглавие и дата на изготвяне на справката - за потребителите
	 */
	public void preProcessPDF(Object document)  {
		try{
			
			String title = "Версии \n грешки при импорт на файл: "+fileName ;		
			new CustomExpPreProcess().preProcessPDF(document, title, null, null, null);		
						
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(),e);			
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);			
		} 
	}
	/**
	 * за експорт в pdf 
	 * @return
	 */
	public PDFOptions pdfOptions() {
		PDFOptions pdfOpt = new CustomExpPreProcess().pdfOptions(null, null, null);
       return pdfOpt;
	}
	/**
	 * pdfOptions при експорт в pdf
	 * @param fontSize
	 * @param fontName
	 * @param orientation
	 * @return
	 */
	public PDFOptions pdfOptions(String fontSize, String fontName, PDFOrientationType orientation ) {
		if(fontSize == null) {
			fontSize = "10";
		}
		if(fontName == null) {
			fontName = "Arial Unicode MS";// за да няма проблем с кирилицата !!! 
		}
		if(orientation == null) {
			orientation = PDFOrientationType.LANDSCAPE;
		}
		PDFOptions pdfOpt = new PDFOptions();
        pdfOpt.setCellFontSize(fontSize);
        pdfOpt.setOrientation(orientation);
        pdfOpt.setFontName(fontName); 
        return pdfOpt;
	}

	

	public class TempOpis implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public Long id;
		public String docTom;
		public String regNomer;
		public Date dataReg;
		public String otnosno;
		public Long brLista;
		public Date dataVl;
		public String tekStr;

		public String getTekStr() {
			return tekStr;
		}

		public void setTekStr(String tekStr) {
			this.tekStr = tekStr;
		}

		public TempOpis(Long id, String docTom, String regNomer, Date dataReg, String otnosno, Long brLista,
				Date dataVl, String tekStr) {
			this.id = id;
			this.docTom = docTom;
			this.regNomer = regNomer;
			this.dataReg = dataReg;
			this.otnosno = otnosno;
			this.brLista = brLista;
			this.dataVl = dataVl;
			this.tekStr = tekStr;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getDocTom() {
			return docTom;
		}

		public void setDocTom(String docTom) {
			this.docTom = docTom;
		}

		public String getRegNomer() {
			return regNomer;
		}

		public void setRegNomer(String regNomer) {
			this.regNomer = regNomer;
		}

		public Date getDataReg() {
			return dataReg;
		}

		public void setDataReg(Date dataReg) {
			this.dataReg = dataReg;
		}

		public String getOtnosno() {
			return otnosno;
		}

		public void setOtnosno(String otnosno) {
			this.otnosno = otnosno;
		}

		public Long getBrLista() {
			return brLista;
		}

		public void setBrLista(Long brLista) {
			this.brLista = brLista;
		}

		public Date getDataVl() {
			return dataVl;
		}

		public void setDataVl(Date dataVl) {
			this.dataVl = dataVl;
		}

		public int compareTo(TempOpis o) {
			// TODO Auto-generated method stub
			return 0;
		}

		public int compare(TempOpis arg0, TempOpis arg1) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public List<SelectItem> getVersiiItemList() {
		return versiiItemList;
	}

	public void setVersiiItemList(List<SelectItem> versiiItemList) {
		this.versiiItemList = versiiItemList;
	}

	public List<SelectItem> getMerniEdiniciList() {
		return merniEdiniciList;
	}

	public void setMerniEdiniciList(List<SelectItem> merniEdiniciList) {
		this.merniEdiniciList = merniEdiniciList;
	}

	public HashMap<String, Integer> getMerniEdinici() {
		return merniEdinici;
	}

	public void setMerniEdinici(HashMap<String, Integer> merniEdinici) {
		this.merniEdinici = merniEdinici;
	}

	public HashMap<String, Integer> getAllAtrib() {
		return allAtrib;
	}

	public void setAllAtrib(HashMap<String, Integer> allAtrib) {
		this.allAtrib = allAtrib;
	}

	public String getTipNaFile() {
		return tipNaFile;
	}

	public void setTipNaFile(String tipNaFile) {
		this.tipNaFile = tipNaFile;
	}

	public HashMap<Integer, HashMap<String, Integer>> getCodeLevel() {
		return codeLevel;
	}

	public void setCodeLevel(HashMap<Integer, HashMap<String, Integer>> codeLevel) {
		this.codeLevel = codeLevel;
	}

	public boolean isLevelNoParent() {
		return levelNoParent;
	}

	public void setLevelNoParent(boolean levelNoParent) {
		this.levelNoParent = levelNoParent;
	}

	public HashMap<String, Integer> getEzik() {
		return ezik;
	}

	public void setEzik(HashMap<String, Integer> ezik) {
		this.ezik = ezik;
	}

	public String getPаrent() {
		return pаrent;
	}

	public void setPаrent(String pаrent) {
		this.pаrent = pаrent;
	}

	public String getLastPаrent() {
		return lastPаrent;
	}

	public void setLastPаrent(String lastPаrent) {
		this.lastPаrent = lastPаrent;
	}

	public boolean isLangBg() {
		return langBg;
	}

	public void setLangBg(boolean langBg) {
		this.langBg = langBg;
	}

	public List<SelectItem> getMerniEdiniciListEn() {
		return merniEdiniciListEn;
	}

	public void setMerniEdiniciListEn(List<SelectItem> merniEdiniciListEn) {
		this.merniEdiniciListEn = merniEdiniciListEn;
	}

	public HashMap<String, Integer> getMerniEdiniciEn() {
		return merniEdiniciEn;
	}

	public void setMerniEdiniciEn(HashMap<String, Integer> merniEdiniciEn) {
		this.merniEdiniciEn = merniEdiniciEn;
	}


	public boolean isFlPlus() {
		return flPlus;
	}

	public void setFlPlus(boolean flPlus) {
		this.flPlus = flPlus;
	}

	public Integer getErrMe() {
		return errMe;
	}

	public void setErrMe(Integer errMe) {
		this.errMe = errMe;
	}
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getInsertType() {
		return insertType;
	}

	public void setInsertType(String insertType) {
		this.insertType = insertType;
	}

	public List<Object[]> getImportedRowOnlyErr() {
		return importedRowOnlyErr;
	}

	public void setImportedRowOnlyErr(List<Object[]> importedRowOnlyErr) {
		this.importedRowOnlyErr = importedRowOnlyErr;
	}

	public List<Object[]> getImportedRowAll() {
		return importedRowAll;
	}

	public void setImportedRowAll(List<Object[]> importedRowAll) {
		this.importedRowAll = importedRowAll;
	}

	public void updateProgress(Integer size, Integer current) {
		if (progress==null) {
			progress=0;
		}else {
			if (current>0) {
				if (((current*100)/size+1)>progress) {
					progress=(current*100)/size+1;	
				}
			}	
		}
		if (progress>100) {
			progress=100;
		}
//		System.out.println("progress:"+progress);
	}
	private Integer progress=0;



	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public DualListModel<SystemClassif> getPosAttrList() {
		return posAttrList;
	}

	public void setPosAttrList(DualListModel<SystemClassif> posAttrList) {
		this.posAttrList = posAttrList;
	}
	public void loadHeadersInVersion(List<Integer> listAttr, Integer idObj) {
		try {
			listAttr = new PositionSDAO(ActiveUser.DEFAULT).loadPositionAttr(idObj);
			posAttrList = new DualListModel<>(getSystemData()
					.getSysClassification(NSIConstants.CODE_CLASSIF_POSITION_ATTRIBUTES, new Date(), lang),
					new ArrayList<SystemClassif>());

			StringBuilder sb =new StringBuilder();
			// тук ще видим, кой атрибути не се срещат в класификацията
			List<SystemClassif> missingAttr = new ArrayList<>();
			for (int i = 0; i < posAttrList.getSource().size(); i++) {
				SystemClassif sc = posAttrList.getSource().get(i);
				sb.append(sc.getCode()).append(",");
				if (!listAttr.contains(sc.getCode())) {
					missingAttr.add(sc);
				}
			}
			for (SystemClassif systemClassif : missingAttr) {
				String ttt = systemClassif.getTekst();
				attributes.getSource().remove(ttt);
			}
			
			
//			//we remove the last comma
//			sb.deleteCharAt(sb.length()-1);
//			setHeaderCodes( sb.toString());
//
//			// махаме излишните атрибути от версията на класификацията
//			for (SystemClassif attr : missingAttr) {
//				posAttrList.getSource().remove(attr);
//			}
//			
//			if (posAttrList.getTarget().size() > 0) {
//				////LOGGER.info("posAttrList.getTarget()");
//			}
//
//			for (int i = 0; i < posAttrList.getTarget().size(); i++) {
//				//LOGGER.info("i" + posAttrList.getTarget().get(i));
//			}
		} catch (DbErrorException e) { // тук не се затваря сесия h2
			LOGGER.error("Грешка при разкодиране на налични атрибути за класификацията", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					"Грешка при разкодиране на налични атрибути за класификацията");
		}
	}
	public void actionVerification() throws DbErrorException  {
		if (importedRow.isEmpty()) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importRelacii.nImoprt"));
			return;	
		}
		if (availableLevels.isEmpty()) {
			JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noLevels"));
			return;
		}
//		int qq = izbr.size();
		boolean flErr = false;
		errString=""; 
		int nac=0;
		if (tipNaFile.equals("ods")) {
			nac=1;//маха се антетката
		}
		if (flAntetka) nac = 1;//Има АНТЕТКА

		int q = izbr.size();
		Object[] tt = new Object[q + 1];
		ArrayList<Object[]> importedRowTemp = new ArrayList<Object[]>();
//			for (Object[] objects : importedRow) {
			for (int i = nac; i < importedRow.size(); i++) {//i=1 zaradi antetkata
				System.out.println("i" + i);
				Integer a = Integer.valueOf(i);
				if (a.equals(2188)) {// Towa se izpolzwa когато има грешка
					System.out.println("i:" + i);
				}
				
				Object[] objects = importedRow.get(i);
				Boolean flEmpty=true;
				for (int j = 0; j < attributes.getTarget().size(); j++) {
					if (!"".equals(objects[j])) flEmpty=false;
				}
				if (flEmpty) continue;
				tt = new Object[q + 1];
				tt[q] = "";
				String code="";
				Integer nivo=0;
				Integer symbolCount=0;
				String mask="";
				int k =0;
				int j =0;
				for (j =0; j < attributes.getTarget().size(); j++) {
					k++;
					String a1 = attributes.getTarget().get(j);
					tt[j] = objects[j];
					if (a1.equals("Код")||a1.equals("Code")) {
						if (objects[j]!=null) {
							code= objects[j].toString().trim();
						}
						tt[j] = code;
						continue;
					}
					if (a1.contains("ниво")||a1.contains("Level")) {
						String ttt=objects[j].toString();//Този и долния ред са защото ниво 1 го чете като 1.0
						int nivoTemp = Double.valueOf(ttt).intValue();
						nivo= Integer.valueOf(nivoTemp) ;
						tt[j] = nivo;
						continue;
					}
//					if (a1.contains("единица")||a1.equals("Code")) {
//						code= objects[j].toString().trim();
//						tt[j] = code;
//						continue;
//					}

//					if (!"".equals(code)&&nivo>0) break; //Да не се отваря, че не прехвърля всички полета
					System.out.println();
				}
				if (nivo>0 &&!"".equals(code)) {
					for (Level level : availableLevels) {
						if (nivo.equals(level.getLevelNumber())) {
							mask=level.getMaskReal();
							symbolCount=level.getSymbolCount();
							break;
						}
					}
				
					if ("*".equals(mask))continue;
//					isOk(code,symbolCount,mask,nivo);
					if (!isOk(code,symbolCount,mask,nivo)) {
						tt[q] =errString;
						flErr = true;errMe=1;
						errString="";
					}
				}
				if (k==j) {
					importedRowTemp.add(tt);
				}
			}
			if(flErr) {
				String[] selectedVersiiTemp = new String[q+1];
				for (int n = 0; n < q; n++) {
					selectedVersiiTemp[n]=selectedVersii[n];
				}
				selectedVersiiTemp[q]="Грешки";
				selectedVersii = new String[q+1];
				for (int n = 0; n < q+1; n++) {
					selectedVersii[n]=selectedVersiiTemp[n];
				}
				
				importedRowImported.addAll(importedRow);
				importedRow=new ArrayList<Object[]>();
				importedRow.addAll(importedRowTemp);

			}else {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importRelacii.ok"));
			
			}
			// Това надолу  да се довърши за проверка на Мерните единици при Проверка
//			if (flPlus) {
//				importedRowAll=new ArrayList<Object[]>();
//				importedRowOnlyErr=new ArrayList<Object[]>();
//
//				importedRow=doMerniEdinici(importedRow,posMENac,qq);
//			}

		System.out.println();
	
	}
	private boolean isOk(String code, Integer symbolCount, String mask, Integer nivo) {

		boolean flOk=true;
		int count = 0;
		if ("".equals(mask)) {
			errString+="Ниво "+nivo+" "+"не е дефинирано ";
			return false;
		}

		for (int i = 0; i < code.length(); i++) {
			count++;
		}
		if (code.length()!=symbolCount) {
			errString+=code+" "+"Некоректен брой символи "+symbolCount;
			return false;
		}
		
		String oneSimvol;
		String oneSimvolCode;
		for (int i = 0; i < mask.length(); i++) {
			oneSimvol=mask.substring(i, i+1);
			oneSimvolCode=code.substring(i, i+1);
			if ("X".equals(oneSimvol)) continue;
			if (".".equals(oneSimvol)) {
				flOk=isPoint(oneSimvolCode);continue;
			}
			if ("L".equals(oneSimvol)) {
				flOk=isBigLat(oneSimvolCode);continue;
			}
			if ("l".equals(oneSimvol)) {
				flOk=isBigLat(oneSimvolCode);continue;
			}
			if ("9".equals(oneSimvol)) {
				flOk=isNumbers(oneSimvolCode);continue;
			}

			if ("C".equals(oneSimvol)) {
				flOk=isLettersAndNumbers(oneSimvolCode);continue;
			}
			if ("К".equals(oneSimvol)) {
				flOk=isBigBg(oneSimvolCode);continue;
			}
			if ("к".equals(oneSimvol)) {
				flOk=isSmallBg(oneSimvolCode);continue;
			}

		}
		return flOk;
	}
	private boolean isPoint(String oneSimvolCode) {
		if (".".equals(oneSimvolCode)) {
			return true;
		}
		errString+=oneSimvolCode+" ";
		return false;
	}

	/**
	 * Дали стринга съдържа само големи латински букви!!!!
	 *
	 * @param str
	 * @return
	 */
	public static boolean isBigLat(String str) {

		Pattern pattern = Pattern.compile("^[A-Z]+$");

		Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) {
			errString+=str+" кода не е латиница - само главни букви ";
			return false;
		}
		return true;
	}
	/**
	 * Дали стринга съдържа само букви и цифри!!!!
	 *
	 * @param str
	 * @return
	 */
	public static boolean isLettersAndNumbers(String str) {

		Pattern pattern = Pattern.compile("^[а-яА-Яa-zA-Z0-9]+$");

		Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) {
			errString+=str+" кода съдържа не само букви и цифри!";
			return false;
		}
		return true;
	}
	/**
	 * Дали стринга съдържа само цифри!!!!
	 *
	 * @param str
	 * @return
	 */
	public static boolean isNumbers(String str) {

		Pattern pattern = Pattern.compile("^[0-9]+$");

		Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) {
			errString+=str+" кода не съдържа само цифри!";
			return false;
		}
		return true;
	}

	/**
	 * Дали стринга съдържа само големи български букви!!!!
	 *
	 * @param str
	 * @return
	 */
	public static boolean isBigBg(String str) {

		Pattern pattern = Pattern.compile("^[А-Я]+$");

		Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) {
			errString+=str+" кода не е кирилица - само главни букви ";
			return false;
		}
		return true;
	}

	/**
	 * Дали стринга съдържа само малки български букви!!!!
	 *
	 * @param str
	 * @return
	 */
	public static boolean isSmallBg(String str) {
		Pattern pattern = Pattern.compile("^[а-я]+$");
		Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) {
			errString+=str+" кода не е кирилица - само малки букви";
			return false;
		}
		return true;
	}

	
	public List<Level> getAvailableLevels() {
		return availableLevels;
	}

	public void setAvailableLevels(List<Level> availableLevels) {
		this.availableLevels = availableLevels;
	}

	public List<Level> getSelectedLevels() {
		return selectedLevels;
	}

	public void setSelectedLevels(List<Level> selectedLevels) {
		this.selectedLevels = selectedLevels;
	}

	public String getLevels() {
		return levels;
	}

	public void setLevels(String levels) {
		this.levels = levels;
	}

	public Integer getIdObj() {
		return idObj;
	}

	public void setIdObj(Integer idObj) {
		this.idObj = idObj;
	}
	public String getHeaderCodes() {
		return headerCodes;
	}


	public void setHeaderCodes(String headerCodes) {
		this.headerCodes = headerCodes;
	}

	public DualListModel<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(DualListModel<String> attributes) {
		this.attributes = attributes;
	}

	public List<Object[]> getImportedRowImported() {
		return importedRowImported;
	}

	public void setImportedRowImported(List<Object[]> importedRowImported) {
		this.importedRowImported = importedRowImported;
	}

	public String getErrString() {
		return errString;
	}

	public void setErrString(String errString) {
		this.errString = errString;
	}
	public boolean isFlAntetka() {
		return flAntetka;
	}

	public void setFlAntetka(boolean flAntetka) {
		this.flAntetka = flAntetka;
	}

	public boolean isFlBg() {
		return flBg;
	}

	public void setFlBg(boolean flBg) {
		this.flBg = flBg;
	}

	public boolean isFlEn() {
		return flEn;
	}

	public void setFlEn(boolean flEn) {
		this.flEn = flEn;
	}

	public String getRazdelitelME() {
		return razdelitelME;
	}

	public void setRazdelitelME(String razdelitelME) {
		this.razdelitelME = razdelitelME;
	}

	public boolean isFlMERazd() {
		return flMERazd;
	}

	public void setFlMERazd(boolean flMERazd) {
		this.flMERazd = flMERazd;
	}


}
