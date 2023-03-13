package com.ib.nsiclassif.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
//import javax.faces.context.ExternalContext;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.faces.view.facelets.FaceletContext;
import javax.inject.Named;
import javax.persistence.Query;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.internal.build.AllowSysOut;
import org.primefaces.PrimeFaces;
import org.primefaces.component.export.PDFOptions;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FilesUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.primefaces.util.EscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.customexporter.CustomExpPreProcess;
import com.ib.nsiclassif.db.dao.AdmGroupDAO;
import com.ib.nsiclassif.db.dao.AdmUserDAO;
import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dto.AdmGroup;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.ActiveUser;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.Files;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.DbErrorException;

import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;


/**
 *   Za export на Релации
 *   Автор  ЛМ
 *
 */

@Named
@ViewScoped
public class ImportRelacii extends IndexUIbean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7056074939252508693L;

    static final Logger LOGGER = LoggerFactory.getLogger(ImportRelacii.class);

    private Integer userId;
    private AdmGroupDAO groupsDAO;
    private AdmUserDAO userDao;
    private AdmGroup group;
    private List<AdmGroup> listGrops = new ArrayList<>();

    private String usersName;
    private List<Integer> usersList = new ArrayList<>();
    private List<String> usersImena = new ArrayList<>();

    private List<String[]> sysClassifList = new ArrayList<>();
    private String selClassif;


    private boolean fromEdit = false;
    private String selRkvForGr;
    private String selRkvForDos;
    //Novo
    private String[] selectedCities2;
    private List<String> cities;
    private List<String> relaciis;
    private String razdelitel=";";
    private List<String> selectedItems;
    private String[] selectedCities1;
    private List<String> items = new ArrayList<>();
    private String textArea="";
    private String textOutput="";
    private List<TempOpis>  opList;
	private List<Object[]> importedRow = new ArrayList<>();
	private List<Object[]> importedRowOnlyErr = new ArrayList<>();
	private List<Object[]> importedRowAll = new ArrayList<>();
    private String[] selectedItem;
    private String[] selectedRelacii;
	private Integer tableId;
	private HashMap<String, Integer> allAtrib=new HashMap<String, Integer>();
	private HashMap<Integer, String> izbr=new HashMap<Integer, String>();
	private Integer nomerPored=0;
	private UserData ud;
	private List<SelectItem>	harakterPromianaList;
	private HashMap<String, Integer> promianaRelacii=new HashMap<String, Integer>();
	private Date currentDate;
	private String tipNaFile;
	private String zabrSimvoli;
	private Integer errMe ;
	boolean flPlus = false;
	private String insertType = "0";
	
	private DualListModel<String> attributes;
	


	public ImportRelacii() {
    	if (selectedItems!=null) {
    	   	selectedItems.clear();
    	}
       	if (textOutput!="") {
       		textOutput="";
    	}
		ud = getUserData(UserData.class);
		userId = ud.getUserId();
		this.currentDate = new Date();
   	
		selectedItems= new ArrayList<>();
        List<String> attributesSource = new ArrayList<>();
        List<String> attributesTarget = new ArrayList<>();

            relaciis = new ArrayList<>();
//            relaciis.add("Номер");
//            relaciis.add("Код на позиция - източник");
//            relaciis.add("Позиция източник");
          relaciis.add("Код източник");
          attributesSource.add("Код източник");
//            relaciis.add("Част на позиция - източник");
//            relaciis.add("Промяна в позиция - източник");
//            relaciis.add("Код на позиция - цел");
//            relaciis.add("Позиция цел");
          relaciis.add("Код цел");
          attributesSource.add("Код цел");

            
//            relaciis.add("Част на позиция - цел");
//            relaciis.add("Промяна в позиция - цел");
            relaciis.add("Характер на промяната");
            attributesSource.add("Характер на промяната");
            
	        attributes = new DualListModel<>(attributesSource, attributesTarget);

			allAtrib=new HashMap<String, Integer>();

			for (int i = 0; i < relaciis.size(); i++) {
            	allAtrib.put(relaciis.get(i), i);
			}
			zabrSimvoli="";
    		try {

				this.harakterPromianaList = createItemsList(false, NSIConstants.CODE_CLASSIF_EXPLANATION, this.currentDate, null);
				if (this.harakterPromianaList != null) {
				      for (SelectItem item : this.harakterPromianaList) {
				    	  String temp = item.getLabel();
				    	  int pos = temp.indexOf("(");
				    	  temp=temp.substring(pos+1,pos+2);
				    	  promianaRelacii.put(temp, (Integer) item.getValue());
				    	  zabrSimvoli+=temp+" ";
				      }
					
				}
    		} catch (Exception e) {
    			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg") + " - " + e.getLocalizedMessage(), e);
                  
    			LOGGER.error(e.getMessage(), e);
    		}
    		zabrSimvoli+="\\"; 
    }

    
    

	/**
	 * Долния метод запомня реда на избор в <p:selectCheckboxMenu
	 */
	public void actionSelect() {
		String redStr = selectedItems.toString();
		String a = selectedItems.toString();
		String substr = ",";
		int brZap = redStr.split(substr, -1).length-1;
		redStr=redStr.replace(", ", "");
		for (int j = 0; j < brZap; j++) {
			String item = izbr.get(j);
			String target = item.substring(1);
			target=target.substring(0,target.length()-1);
			redStr=redStr.replaceFirst(target, "");
			System.out.println();
		}
//		System.out.println(redStr.split(substr, -1).length-1);//брои запетайките
		
//		System.out.println(redStr.split(substr)[0]);
		izbr.put(nomerPored, redStr);
		nomerPored+=1;
//        System.out.println("Minava");

	}
	
		@PostConstruct
		public void initData() {
	        System.out.println("Minava");
			String idObj = JSFUtils.getRequestParameter("idObj");
		    Object id = JSFUtils.getRequestObject("idCorespTable");	
//			String idObj = JSFUtils.getRequestParameter("idObj");
			id = JSFUtils.getRequestObject("idTable");	
			System.out.println("adsdasd");
			FaceletContext faceletContext = (FaceletContext) FacesContext.getCurrentInstance().getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
			String param3 = (String) faceletContext.getAttribute("isView"); // 0 - актуализациял 1 - разглеждане
			System.out.println();
		}
	    private UploadedFile file;
	    private UploadedFiles files;
//	    private String dropZoneText = "Drop zone p:inputTextarea demo.";

	    
	    
//		@SuppressWarnings("resource")
//		public void uploadFileListener(FileUploadEvent event){		
//	        System.out.println("uploadFileListener"+selectedCities2);
//			
//			try {
//				
//				UploadedFile upFile = event.getFile();
//				Files fileObject = new Files();
//				String fileName=upFile.getFileName();
////				File destFile= new File(fileName);
//				FileInputStream file = new FileInputStream(new File(fileName));
//				XSSFWorkbook workbook;
//				workbook = new XSSFWorkbook(file);
//				XSSFSheet sheet = workbook.getSheetAt(0); 
//
//				tipNaFile=fileName.substring(fileName.lastIndexOf(".")+1);
//				if (tipNaFile.equals("xls")) {
//					doXls(upFile);
//				}
//				if (tipNaFile.equals("xlsx")) {
//					byte[] fileCont = upFile.getContent();
//					String s2 = new String(fileCont);
////					XSSFWorkbook workbook = new XSSFWorkbook(s2);
////		            XSSFSheet sheet = workbook.getSheetAt(0);
//
////					FileInputStream file = new FileInputStream(new File(upFile));
//					doXls(upFile);
//				}
//				fileObject.setFilename(upFile.getFileName());
//				fileObject.setContentType(upFile.getContentType());
//				fileObject.setContent(upFile.getContent());	
//				byte[] fileCont = upFile.getContent();
//				String s2 = new String(fileCont);
//				ArrayList<String> ll=new ArrayList<String>();
//				opList=new ArrayList<ImportRelacii.TempOpis>();
//				int j = 0;
//				Object[] obj =new Object[7];
//
//				do {
//					int q = s2.indexOf('\r');
//					if (s2.length()>1){
//						String ww = s2.substring(0, q);
//						ll.add(ww);
//						obj[j]=ww;j+=j;
//						s2=s2.substring(q+2,s2.length());
//					}
//				}while(s2.length()>1 );
//				int  b=0;
//				int llsize = ll.size();
//				for (int i=0;i<llsize;i++){
//					String den = ll.get(i);	
////			        System.out.println("red");
//			        doZaredi(den);
////					int a = ivadiZapisiZaDenia(den);
////					if (a==1){
////						b+=1;
////					}
//				}
////				System.out.println("uploadFileListener");
//				
//				
////				JPA.getUtil().runInTransaction(() -> {
////					new FilesDAO(getUserData()).saveFileObject(fileObject, this.registratura.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_REISTRATURA); 
////
////					// извличане на файловете от таблица с файловете
////					this.filesList = new FilesDAO(getUserData()).selectByFileObject(this.registratura.getId(), DocuConstants.CODE_ZNACHENIE_JOURNAL_REISTRATURA);
////				}); 
//			
//			} catch (Exception e) {
//				LOGGER.error("Exception: " + e.getMessage());	
//			} 
//		}

	    private void doXls(UploadedFile upFile) {
//	       	System.out.println("doXls");
			if (!doTransferSelItemsToSelVersii(selectedItems)) {
				return;
			}
			int brIzbr = selectedItems.size();
			importedRow=new ArrayList<Object[]>();
	        InputStream inputStream = null;
			HSSFWorkbook workbook;
	        try {
	            inputStream = upFile.getInputStream();
	            workbook = new HSSFWorkbook(inputStream);
				 HSSFSheet sheet = workbook.getSheetAt(0); 
				 HSSFRow row = sheet.getRow(0);
//				if (doCompare(row,selectedRelacii)) {
//					return;
//				}
				brIzbr = selectedRelacii.length;
				int rowNumb = sheet.getLastRowNum();
				for (int i = 0; i < rowNumb+1; i++) {
					row = sheet.getRow(i);
//		 	        System.out.println("handleFileUpload");
		 	        
					if (row != null && row.getCell(0) != null && row.getCell(0).getCellType().toString().equals("STRING")  && row.getCell(0).getStringCellValue().contains(selectedRelacii[0])) {
						continue;
					}
					Object[] rowData=new Object[brIzbr];
					for (int k = 0; k < brIzbr; k++) {
						rowData[k]=row.getCell(k);
					}
					importedRow.add(rowData);
				}
				
	        } catch (IOException e) {
	           //Do something with the Exception (logging, etc.)
	        }
		
		}		

		private void doZaredi(String den) {
			// TODO Auto-generated method stub
			  List<TempOpis> opiList = new ArrayList<>();
			  List<String> list = new ArrayList<String>(Arrays.asList(den.split(" , ")));
			  
			  for (int i = 0; i < list.size(); i++) {
	//			  TempOpis=new TempOpis(null, den, den, dateFrom, den, null, dateFrom, den)
				  list.get(i);
				  if (i==0){
					  
				  }
						  
				  
			  }
//		      System.out.println("uploadFileListener");

		}

		public void upload() {
	        System.out.println("Upload");
	        if (file != null) {
	            FacesMessage message = new FacesMessage("Successful", file.getFileName() + " is uploaded.");
	            FacesContext.getCurrentInstance().addMessage(null, message);
	        }
	    }

	    public void uploadMultiple() {
	        if (files != null) {
	            for (UploadedFile f : files.getFiles()) {
	                FacesMessage message = new FacesMessage("Successful", f.getFileName() + " is uploaded.");
	                FacesContext.getCurrentInstance().addMessage(null, message);
	            }
	        }
	    }
		private HashMap<Integer, String> zarediIzbr(List<String> list) {
			HashMap<Integer, String> izbrTemp = new HashMap<Integer, String>();
			
			for (String string : list) {
				izbrTemp.put(nomerPored, string);
//				izbrTemp.put(allAtrib.get(string), string);
				selectedItems.add(string);
				nomerPored++;
			}
			return izbrTemp;
		}

	    public void handleFileUpload(FileUploadEvent event)  {
//	        System.out.println("handleFileUpload");
			if (attributes.getTarget().size()==0) {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
				return ;
			}
			izbr=zarediIzbr(attributes.getTarget());

	    	UploadedFile uploadedFile=event.getFile();
	    	String fileName = uploadedFile.getFileName();
			tipNaFile=fileName.substring(fileName.lastIndexOf(".")+1);
			if (tipNaFile.equals("pdf")) {
				doPdf(uploadedFile);
				return;
			}

			if (tipNaFile.equals("sdmx-json")) {
				doJson(uploadedFile);
			}

			if (tipNaFile.equals("xml")) {
				try {
					doXml(uploadedFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (selectedItems == null && !tipNaFile.equals("")) {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
				return;
			}

			if (tipNaFile.equals("xls")) {
				doXls(uploadedFile);
			}
			if (tipNaFile.equals("xlsx")) {
				doXlsx(uploadedFile);
			}
			if (tipNaFile.equals("ods")) {
				doOds(uploadedFile);
			}
			if (tipNaFile.equals("txt")) {
				doTxt(uploadedFile);
			}
			if (tipNaFile.equals("html")) {
				doHtml(uploadedFile);
			}
			if (tipNaFile.equals("htm")) {
				doHtml(uploadedFile);
			}
			if (tipNaFile.equals("csv")) {
				doCsv(uploadedFile);
			}
		
 	    	
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
							Object[] rowData = new Object[col];
							for (int j = 0; j < col; j++) {
								RectangularTextContainer cell =red.getCell(i, j);
								String cellTxt = cell.getText();
								cellTxt=cellTxt.replace("\"", "").replace("\r", " ");
								rowData[j] = cellTxt;
								System.out.println("cellTxt: "+cellTxt);
							}
							if (fl1red) {
								selectedRelacii = new String[col];
								for (int n = 0; n < col; n++) {
									selectedRelacii[n] = rowData[n].toString() ;
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
		private boolean doTransferSelItemsToSelVersii(List<String> selectedItems2) {
			if (selectedItems2==null) {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noAtribut"));
				return false;
			}
			int brItens = selectedItems2.size();
			String[] selectedRelaciiTemp = new String[brItens];
			int j = 0;
			for (String string : selectedItems2) {
				selectedRelaciiTemp[j] = string;
				j += 1;
			}
			setSelectedRelacii(selectedRelaciiTemp);
//			System.out.println("doTransferSelItemsToSelVersii");
			return true;

		}

	    
		private void doOds(UploadedFile upFile) {
	        InputStream inputStream = null;


			XSSFFactory fs;
			try {
				inputStream = upFile.getInputStream();
//				fs = new POIFSFileSystem(inputStream);
				XSSFWorkbook wb = new XSSFWorkbook(inputStream);
			    XSSFSheet sheet = wb.getSheetAt(0);
			    XSSFSheet sheet2 = wb.getSheetAt(1);
			    XSSFRow row;
			    XSSFCell cell;
			    int rows; // No of rows
			    rows = sheet.getPhysicalNumberOfRows();

			    int cols = 0; // No of columns
			    int tmp = 0;

			    // This trick ensures that we get the data properly even if it doesn't start from first few rows
			    for(int i = 0; i < 10 || i < rows; i++) {
			        row = sheet.getRow(i);
			        if(row != null) {
			            tmp = sheet.getRow(i).getPhysicalNumberOfCells();
			            if(tmp > cols) cols = tmp;
			        }
			    }

			    for(int r = 0; r < rows; r++) {
			        row = sheet.getRow(r);
			        if(row != null) {
			            for(int c = 0; c < cols; c++) {
			                cell = row.getCell((short)c);
			                if(cell != null) {
			                    // Your code here
			                }
			            }
			        }
			    } } catch(Exception ioe) {
			    ioe.printStackTrace();
			     }
			}

//			XSSFWorkbook workbook;
//			importedRow=new ArrayList<Object[]>();
//	        try {
//	            inputStream = upFile.getInputStream();
//	            workbook = new XSSFWorkbook(inputStream);
//				XSSFSheet sheet = workbook.getSheetAt(0); 
//				XSSFRow row = sheet.getRow(0);
////				if (doCompareXlsx(row,selectedItems)) {
////			           JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));
////
////					return ;
////				}
//				int brIzbr = selectedItems.size();
//				for (int i = 0; i < sheet.getLastRowNum(); i++) {
//					row = sheet.getRow(i);
//					if (row != null && row.getCell(0) != null && row.getCell(0).getCellType().toString().equals("STRING")  && row.getCell(0).getStringCellValue().contains(selectedRelacii[0])) {
//						continue;
//					}
//					Object[] rowData=new Object[brIzbr];
//					for (int k = 0; k < brIzbr; k++) {
//						rowData[k]=row.getCell(k);
//					}
//					importedRow.add(rowData);
//				}
//				
//	        } catch (IOException e) {
//	           //Do something with the Exception (logging, etc.)
//	        }
		




		private void doTxt(UploadedFile upFile) {
			selectedRelacii=doTransferSelIzbrToSelRelacii(izbr);
			String fileName = upFile.getFileName();
			byte[] fileCont = upFile.getContent();
			String s2 = new String(fileCont);
			ArrayList<String> ll = new ArrayList<String>();
			int brIzbr = selectedItems.size();
			String chevrons = "\n";
			String tdn = "<";
			int q = s2.indexOf(chevrons);
			do {
				if (s2.length() > 1) {
					String ww = s2.substring(0, q) + chevrons;
					ll.add(ww);
					s2 = s2.substring(q + 1, s2.length());
				}
				q = s2.indexOf(chevrons);
			} while (q > -1);

			int s = ll.size();
			System.out.println("ll.size()" + s);
			int w = 0;
			String red0 = ll.get(0);
			if (razdelitel.equals("\\|")) razdelitel="|";

			if (!red0.contains(razdelitel)) {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noSameDelimiter"));
				return;
			}
			if (razdelitel.equals("|")) razdelitel="\\|";

			for (int i = 1; i < ll.size(); i++) {
				String red = ll.get(i);
				Object[] rowData = new Object[brIzbr];
	          	  List myList = Arrays.asList(red.split(razdelitel));
	          	  for (int j = 0; j < myList.size(); j++) {
	          		  Object a = myList.get(j);
	          		  rowData[j]=a;
	          	  }
	          	  importedRow.add(rowData);
	          	  System.out.println("w:" + w);
				
			}
 			System.out.println();
			
		}




		private void doJson(UploadedFile upFile) {
			selectedRelacii=doTransferSelIzbrToSelRelacii(izbr);
			String fileName = upFile.getFileName();
			byte[] fileCont = upFile.getContent();
			String s2 = new String(fileCont);
			ArrayList<String> ll = new ArrayList<String>();
			ArrayList<String> ll2 = new ArrayList<String>();
			ArrayList<String> lnew = new ArrayList<String>();
//			String chevrons = ">";
//			String chevrons = "\r\n";
			String chevrons = "\n";
			String tdn = "<";
			int p = 0;
			int q = s2.indexOf(chevrons);
			do {
				if (s2.length() > 1) {
					String ww = s2.substring(0, q) + chevrons;
					ll.add(ww);
					s2 = s2.substring(q + 1, s2.length());
//					String s3 = s2.substring(0, s2.length());
//					if (s3.contains("structure")) {
//						System.out.println("ll.size()" + s2);
//					}
					p += 1;
				}
				q = s2.indexOf(chevrons);
			} while (q > -1);

			int s = ll.size();
			System.out.println("ll.size()" + s);
			int w = 0;
			boolean flObservation = false;
			for (String red : ll) {
				red = red.toString().trim();
				System.out.println("w:" + w);
				w += 1;
//				Integer e = Integer.valueOf(w);
//				if (e.equals(10700)) {// Towa se izpolzwa когато има грешка
//					System.out.println("j:" + w);
//				}
				if (red.contains("observation")) {
					flObservation = true;
				}else {
					if(!flObservation)  continue;
				}
//				if (red.contains("<?"))    continue;
//				if (red.contains("<mes"))  continue;
//				if (red.equals("")) 	   continue;
//				if (red.contains("</mes")) continue;
//				if (red.contains("Series"))continue;
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
			
		}




		private void doXml(UploadedFile upFile) throws IOException {
			selectedRelacii=doTransferSelIzbrToSelRelacii(izbr);
			String fileName = upFile.getFileName();
			byte[] fileCont = upFile.getContent();
			String s2 = new String(fileCont);
			ArrayList<String> ll = new ArrayList<String>();
			ArrayList<String> ll2 = new ArrayList<String>();
			ArrayList<String> lnew = new ArrayList<String>();
//			String chevrons = ">";
//			String chevrons = "\r\n";
			String chevrons = "\n";
			String tdn = "<";
			int p = 0;
			int q = s2.indexOf(chevrons);
			do {
				if (s2.length() > 1) {
					String ww = s2.substring(0, q) + chevrons;
					ll.add(ww);
					s2 = s2.substring(q + 1, s2.length());
//					String s3 = s2.substring(0, s2.length());
//					if (s3.contains("structure")) {
//						System.out.println("ll.size()" + s2);
//					}
					p += 1;
				}
				q = s2.indexOf(chevrons);
			} while (q > -1);

			int s = ll.size();
			System.out.println("ll.size()" + s);
			int w = 0;
			for (String red : ll) {
				red = red.toString().trim();
				int r = 20;
				if (red.length() < 20) {
					r = red.length();
				}
				System.out.println("red:" + red.substring(0, r));
				System.out.println("w:" + w);
				w += 1;
//				Integer e = Integer.valueOf(w);
//				if (e.equals(10700)) {// Towa se izpolzwa когато има грешка
//					System.out.println("j:" + w);
//				}

				if (red.contains("<?"))    continue;
				if (red.contains("<mes"))  continue;
				if (red.equals("")) 	   continue;
				if (red.contains("</mes")) continue;
				if (red.contains("Series"))continue;
//				if (red.contains("</str")) continue;
//				if (red.contains("Codelists")) continue;
//				if (red.contains("Annotation"))
//					continue;
//				if (red.contains("\n")) {
//					continue;
//				}
//				if (red.contains("en")) {
//					langBg = false;
//				}

//				if (red.contains("</str:Codelists>")) {
//					break;
//				}
				ll2.add(red.toString().trim());
			}
			importedRow = new ArrayList<Object[]>();			
			for (String red : ll2) {
				red = red.toString().trim();			
				int l = 2;
				Object[] rowData = new Object[3];
				int a1 = red.indexOf("STATUS=")+8;
				String a = doExtractCode(a1, red);
				rowData[l] = a;
				l -= 1;
				a1 = red.indexOf("VALUE=")+8;
				a = doExtractCode(a1, red);
				rowData[l] = a;	
				l -= 1;
				a1 = red.indexOf("PERIOD=")+8;
				a = doExtractCode(a1, red);
				rowData[l] = a;	
				importedRow.add(rowData);
			}
			
			
			
 			System.out.println();
			
		}
		private String doExtractCode(int a1, String red) {
			String a = "";
			for (int i = a1; i < red.length(); i++) {
				System.out.println("red.charAt(i)"+red.charAt(i));
				if (String.valueOf(red.charAt(i)).equals("\"")) {
					break;
				} else {
					a += red.charAt(i);
				}
			}
			return a;
		}

	    
		private void doHtml(UploadedFile upFile) {
//	       	System.out.println("doHtml");
			selectedRelacii=doTransferSelIzbrToSelRelacii(izbr);
			int brIzbr = selectedItems.size();
			importedRow=new ArrayList<Object[]>();
	        byte[] fileCont = upFile.getContent();
	        String s2 = new String(fileCont);
	        s2=s2.replace("&nbsp", "");
	        s2=s2.replace("<pre>", "");
	        s2=s2.replace("</pre>", "");
	        s2=s2.replace("<b>", "");
	        ArrayList<String> ll=new ArrayList<String>();
	        Object[] obj =new Object[7];
	        int j = 0;
	        String trk="</tr>";
	        String trn="<tr>";
	        String tdk="</td>";
	        String tdn="<td>";
	        int q =s2.indexOf(trk);
			do {
			if (s2.length()>1){
				String ww = s2.substring(0, q);
				int w = s2.indexOf(trn);
				ww=ww.substring(w+4, ww.length());
				ll.add(ww);
				s2=s2.substring(q+5,s2.length());
			}
			q = s2.indexOf(trk);
			}while(q>1 );
			
			int llsize = ll.size();
			for (String red : ll) {
		        int qd =red.indexOf(tdk);
		        if (qd<0) continue;
		        red=red.replace("\n", "");
		        red=red.replace("\t", "");
//		        red=red.replace(" valign=top", "");
		        red=red.replace("\"", "");
		        red=red.replace(" valign=top", "");
		        qd =red.indexOf(tdk);
		        int  l=0;
				Object[] rowData=new Object[brIzbr];
				do {
					if (red.length()>1){
						String wd = red.substring(0, qd);
						int w = red.indexOf(tdn);
						wd=wd.substring(w+4, wd.length());
						rowData[l]=wd;
						l+=1;
						red=red.substring(qd+5,red.length());
					}
				qd =red.indexOf(tdk);
				}while(qd>1 );
				importedRow.add(rowData);
				
			}
//			Object[] proverka = importedRow.get(0);
//			System.out.println("doHtml end");		
			ArrayList<Object[]> importedRowTemp = new ArrayList<Object[]>();
				for (int i = 0; i < importedRow.size(); i++) {
					Object[] rowData=new Object[brIzbr];
					Object[] red = importedRow.get(i);
					for (int k = 0; k < red.length; k++) {
						String redString = red[k].toString();
						if(redString.contains("width")) {
							int w = redString.indexOf(">");	
							redString=redString.substring(w+1,redString.length());
							rowData[k]	=	redString;
						}
					}
					if (rowData[0]==null) {
						importedRowTemp.add(red);
					}else {
						importedRowTemp.add(rowData);
					}
					
				}
				importedRow=new ArrayList<Object[]>();
				importedRow.addAll(importedRowTemp);
			
		}
		private String[] doTransferSelIzbrToSelRelacii(HashMap<Integer, String> izbr2) {
			int brItens=izbr2.size();
			String[] selectedRelaciiTemp = new String[brItens];

			for (int i = 0; i < izbr2.size(); i++) {
				selectedRelaciiTemp[i] =izbr2.get(i).replace("]", "").replace("[", "");
			}
			setSelectedRelacii(selectedRelaciiTemp);
//			System.out.println("doTransferSelItemsToSelVersii");
			return selectedRelaciiTemp;
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
			new StringBuilder();
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
				line=line.substring(0,line.length()-1);//защо съдържа на края ";"!!!!!
				if (!line.contains(razdelitel)) {			/// dali ima tozi razdelitel
					JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.noSameDelimiter"));
					return;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int j = 0;
			try {
				while ((line = reader.readLine()) != null) {
					Object[] rowData = new Object[brIzbr];
					if (razdelitel.equals("|")) razdelitel="\\|";
					System.out.println("j:" + j);
					Integer aa = Integer.valueOf(j);
					if (aa.equals(122)) {// Towa se izpolzwa когато има грешка
						System.out.println("j:" + j);
					}
					j++;
//					line=line.substring(0,line.length()-1);//защо съдържа на края ";"!!!!!				
					line = line.replaceAll("\"", "");
					String[] result = line.split(razdelitel,-1);

					List myList = Arrays.asList(line.split(razdelitel,-1));
					if (izbr.size()!=myList.size()) {
						String line1=reader.readLine();
						JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.sameDelimiter"));
						break;
					}
					for (int i = 0; i < myList.size(); i++) {
						String a = myList.get(i).toString();
						a = a.replaceAll("\"", "");
						rowData[i] = a;
					}
					importedRow.add(rowData);
				}
			} catch (IOException e) {
				// Do something with the Exception (logging, etc.)
			}

		}

		private void doCsvOld(UploadedFile upFile) {
//			System.out.println("doCsv");
			selectedRelacii=doTransferSelIzbrToSelRelacii(izbr);
	        InputStream inputStream = null;
			importedRow=new ArrayList<Object[]>();
			int brIzbr=izbr.size();
	        try {
	            inputStream = upFile.getInputStream();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
	            StringBuilder string = new StringBuilder();
	            String line;
	            while ((line = reader.readLine()) != null) {
					Object[] rowData=new Object[brIzbr];
//	            	  System.out.println("This is each line: " + line);
	            	  List myList = Arrays.asList(line.split(razdelitel));
	            	  for (int i = 0; i < myList.size(); i++) {
	            		  Object a = myList.get(i);
	            		  rowData[i]=a;
	            	  }
	            	  importedRow.add(rowData);
//	      			System.out.println("readLine");
	            	  }
	        } catch (IOException e) {
		           //Do something with the Exception (logging, etc.)
		    }
			
		}

		
	    private void  doXlsx(UploadedFile upFile) {
//	       	System.out.println("doXlsx");
//			doTransferSelItemsToSelVersii(selectedItems);
			if (!doTransferSelItemsToSelVersii(selectedItems)) {
				return;
			}
	       	
	        InputStream inputStream = null;
			XSSFWorkbook workbook;
			importedRow=new ArrayList<Object[]>();
	        try {
	            inputStream = upFile.getInputStream();
	            workbook = new XSSFWorkbook(inputStream);
				XSSFSheet sheet = workbook.getSheetAt(0); 
				XSSFRow row = sheet.getRow(0);
				if (doCompareXlsx(row,selectedItems)) {
			           JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));

					return ;
				}
				int brIzbr = selectedItems.size();
				//Долното е с утератор
//				Iterator<Row> rowIterator = sheet.iterator();
//				while (rowIterator.hasNext()) {
//	                Row row = rowIterator.next();
//	                // For each row, iterate through each columns
//	                Object[] rowData=new Object[brIzbr];
//	                int k = 0;
//	                Iterator<Cell> cellIterator = row.cellIterator();
//	                while (cellIterator.hasNext()) {
//
//	                    Cell cell = cellIterator.next();
//	                    rowData[k]=cell;
//	                    k++;
//	                    System.out.println("");
//	                }
//	                importedRow.add(rowData);
//	                System.out.println("");
//	            }
//				CellAddress а = sheet.getActiveCell();
//				int b = sheet.getFirstRowNum();
//				int c = sheet.getLastRowNum();
//				int d = sheet.getPhysicalNumberOfRows();
				int lastRowNumer = sheet.getLastRowNum();
				System.out.println("lastRowNumer: " + lastRowNumer);
				
				for (int i = 0; i < sheet.getLastRowNum()+1; i++) {
					
					System.out.println("i: " + i);
					Integer a = Integer.valueOf(i);
					if (a.equals(4015)) {// Towa se izpolzwa когато има грешка
						System.out.println("i:" + i);
					}
					row = sheet.getRow(i);
					if (row==null) continue;
					Object cells = row.getCell(0);
//					System.out.println("cells"+cells);
					if (cells== null) {
						continue;
					}else {
						if ("".equals(cells.toString())) continue;
					}
//					if (row != null && row.getCell(0) != null && row.getCell(0).getCellType().toString().equals("STRING")  && row.getCell(0).getStringCellValue().contains(selectedRelacii[0])) {
//						continue;
//					}
					Object[] rowData=new Object[brIzbr];
					for (int k = 0; k < brIzbr; k++) {
						rowData[k]=row.getCell(k);
					}
					importedRow.add(rowData);
				}
	        } catch (IOException e) {
				LOGGER.error(getMessageResourceString(beanMessages,"general.errorClassif"), e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
	        }
	    }
	    
		private boolean doCompareXlsx(XSSFRow row, List<String> selectedItems2) {
			Boolean err = false;
			int selected = selectedItems2.size();;
			int j = 0;
			XSSFCell cell = row.getCell(j);
			do {
				j += 1;
				cell = row.getCell(j);
				System.out.println("cell:"+cell);
				if (cell != null) {
					String c = cell.toString();
					System.out.println("c:"+c);
					if ("".equals(c)) break;
				}
			} while (cell != null);

			if (j != selected) {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.onlyOnAtribut"));
				return true;
			}
			j = 0;
			cell = row.getCell(j);
			String[] selectedRelaciiTemp = new String[selected];
			do {
				cell = row.getCell(j);
				if (cell != null) {
					String c = cell.toString();
					if ("".equals(c)) break;
					selectedRelaciiTemp[j] = cell.toString();
				}
				j += 1;
			} while (cell != null);
			setSelectedRelacii(selectedRelaciiTemp);
//			System.out.println("i" + j);
			return err;

		}

	    
		private boolean doCompare(XSSFRow row, String[] selectedRelacii2) {
			Boolean err=true;
			int selected =selectedRelacii2.length;
			int i = 0;
			for (i = 0; i < selectedRelacii2.length; i++) {
				XSSFCell cell = row.getCell(i);
				if (cell==null) break;
			}
			if (i==selected) {
	            JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importVersii.onlyOnAtribut"));
				return false;
			}
//			System.out.println("i"+i);
			
	    	return err;
			
		}
		public void actionVerification()  {
			if (importedRow.size()==0) {
	            JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importRelacii.nImoprt"));
	            return;
			}
			flPlus = false;
			int q = izbr.size();
			String idTable = JSFUtils.getRequestParameter("idTable");
			Integer versionSource = null;
			Integer versionTarget = null;
			importedRowAll=new ArrayList<Object[]>();
			importedRowOnlyErr=new ArrayList<Object[]>();
			
			try {
				JPA.getUtil().begin();
				String corspTablSource = "select ID_VERS_SOURCE from TABLE_CORRESP where ID=:idTable";
				Query qq = JPA.getUtil().getEntityManager().createNativeQuery(corspTablSource);
				qq.setParameter("idTable", idTable);
				versionSource=  (Integer) qq.getSingleResult();
				String corspTablTarget  = "select ID_VERS_TARGET from TABLE_CORRESP where ID=:idTable";
				qq = JPA.getUtil().getEntityManager().createNativeQuery(corspTablTarget);
				qq.setParameter("idTable", idTable);
				versionTarget=  (Integer) qq.getSingleResult();
				
				String positionShemeSource = "select code from POSITION_SCHEME where VERSION_ID=:versionSource";
				qq = JPA.getUtil().getEntityManager().createNativeQuery(positionShemeSource);
				qq.setParameter("versionSource", versionSource);
				List sourceList = qq.getResultList();
				String positionShemeTarget = "select code from POSITION_SCHEME where VERSION_ID=:versionTarget";
				qq = JPA.getUtil().getEntityManager().createNativeQuery(positionShemeTarget);
				qq.setParameter("versionTarget", versionTarget);
				List sourceTarget = qq.getResultList();
				int intNac = 0;
				if (tipNaFile.equals("htm")) {
					intNac=1;
				}

				for (int  i=intNac; i < importedRow.size(); i++) {
					Object[] red = importedRow.get(i);
					if (!sourceList.contains(red[0])) {
						red[0]+=" Липсва";
						setErrMe(1);
						flPlus =true;

					}
					if (!sourceTarget.contains(red[1])) {
						red[1]+=" Липсва";
						setErrMe(1);
						flPlus =true;

					}
					importedRowAll.add(red);
				}
				System.out.println();
			} catch (DbErrorException e) {
				JPA.getUtil().closeConnection();

				return;
			}
			JPA.getUtil().closeConnection();
			if (flPlus) {
				importedRow=new ArrayList<Object[]>();
				importedRow.addAll(importedRowAll);
				importedRowOnlyErr=doOnlyErr(importedRowAll);
			}
			System.out.println("errMe"+errMe);
		}
		
		
		private List<Object[]> doOnlyErr(List<Object[]> importedRow2) {
			List<Object[]> importedRow=new ArrayList<Object[]>();
			boolean fl = false;
			
			for (int  i=0; i < importedRow2.size(); i++) {
				Object[] red = importedRow2.get(i);
				fl = false;
				System.out.println("i: " + i);
				Integer a = Integer.valueOf(i);
				if (a.equals(2165)) {// Towa se izpolzwa когато има грешка
					System.out.println("i:" + i);
				}

				for (Object object : red) {
					if (object!=null) {
						String а = object.toString();
						if (object.toString().contains("Липсва")) {
							fl =true;
						}
						
					}
				}
				if (fl) {
					importedRow.add(red);
					
				}
			}
			return importedRow;
		}




		public void changeType() {
			System.out.println();
			if (insertType.equals("1")){
				importedRowAll.addAll(importedRow);
				importedRow=new ArrayList<Object[]>();
				importedRow.addAll(getImportedRowOnlyErr());
			}else {
				importedRow=new ArrayList<Object[]>();
				importedRow.addAll(importedRowAll);
			}
		}

		public void actionSave() throws DbErrorException {
			if (importedRow.size()==0) {
	            JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importRelacii.nImoprt"));
	            return;
			}
			if (importedRow.isEmpty()) {
				JSFUtils.addErrorMessage(getMessageResourceString(beanMessages, "importRelacii.nImoprt"));
				return;		}
			//Долното да се направиЛМ!!!!
//			if (doImaliZapis()) {
//				return;
//			}
			
			String idTable = JSFUtils.getRequestParameter("idTable");
//	        System.out.println("actionSave");
			String redInsert="";
			String sourceCode = "";
			String targetCode = "";
			Integer explanattion = null;
			int size = importedRow.size();

			int intNac = 0;
			if (tipNaFile.equals("csv")) {
				intNac=1;
			}
//			try {
			System.out.println("size:"+importedRow.size());
				for (int  i=intNac; i < importedRow.size(); i++) {
			    	updateProgress(size, i+1);
			    	if (i%100==0) {
						System.out.println("i:"+i);
			    	}
					Integer a=Integer.valueOf(i);
					if (a.equals(9765)) {
						System.out.println("i:"+i);
					}
					Object[] red = importedRow.get(i);
					if (red[0]==null||red[1]==null) continue;
					for (int j =0; j < izbr.size(); j++) {
						String a1 = izbr.get(j);
						a1=a1.replace("[","").replace("]","");
						
						Integer aa = allAtrib.get(a1);
						if (aa.equals(0)) {//Код позиция източник
							sourceCode = red[j].toString();
							continue;
						}
						if (aa.equals(1)) {//Код позиция цел
							targetCode = red[j].toString();
							continue;
						}
						if (aa.equals(2)) {//Промяна позиция цел
							String redj;
							if (red[j]==null) {
								explanattion=null;
								continue;
							}
							redj  = (String) red[j].toString().replace("\t", "").replace("\n", "");
							if (redj.equals("")) {
								explanattion=null;
								continue;
							}else {
								explanattion=promianaRelacii.get(redj);
								continue;
							}
						}
				
					}
				
			redInsert="INSERT INTO relation (ID,ID_TABLE, SOURCE_CODE,TARGET_CODE,EXPLANATION,DATE_REG,USER_REG) VALUES ";
			redInsert += "( NEXT VALUE FOR SEQ_RELATION, :idTable,:sourceCode,:targetCode,:explanattion,"
					+ ":DAT,:USERID )";
//			System.out.println("actionSave:"+redInsert);
		 	try {
	    		
		    	Date datAction = new Date();
				JPA.getUtil().begin();
	
		        Query q = JPA.getUtil().getEntityManager().createNativeQuery(redInsert);
	    		q.setParameter("idTable", idTable);
	    		q.setParameter("sourceCode", sourceCode);    		
	    		q.setParameter("targetCode", targetCode);    		
	    		q.setParameter("explanattion", explanattion);    		
	    		q.setParameter("USERID", getUserId());
	    		q.setParameter("DAT", datAction);    		
	
		        q.executeUpdate();
				JPA.getUtil().commit(); // Успешно завършване на транзакцията
//		        System.out.println("zapis");
		   	} catch (Exception e) {
				LOGGER.error("Грешка при инсърт в таблица RELATION"+redInsert);
				LOGGER.error("Грешка при извличане на елемент по id на предходен", e);
//				System.out.println("actionSave:"+redInsert);
//				System.out.println("sourceCode:"+sourceCode);
				return;
			}
		  }

			JPA.getUtil().closeConnection();
			System.out.println("kraj na save");

		 	try {
	    		
			JPA.getUtil().begin();
			String idShema = "select count(distinct r.ID)  from RELATION r where r.ID_TABLE=:idTable";
			Query qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
			qq.setParameter("idTable", idTable);
			Integer relationsCount = (Integer) qq.getSingleResult();
			
			idShema = "select count(distinct  r.target_code)  from RELATION r where r.ID_TABLE=:idTable";
			qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
			qq.setParameter("idTable", idTable);
			Integer targetPosCount = (Integer) qq.getSingleResult();
			idShema = "select count(distinct  r.SOURCE_CODE)  from RELATION r where r.ID_TABLE=:idTable";
			qq = JPA.getUtil().getEntityManager().createNativeQuery(idShema);
			qq.setParameter("idTable", idTable);
			Integer sourcePosCount = (Integer) qq.getSingleResult();
			
			String redUpdate = " Update TABLE_CORRESP set RELATIONS_COUNT=:relationsCount ,SOURCE_POS_COUNT=:sourcePosCount,TARGET_POS_COUNT=:targetPosCount "
					+ " where ID=:id ";//and ID_VERS_SOURCE:idVersSource and ID_VERS_TARGET:idVersTarget ";
			qq = JPA.getUtil().getEntityManager().createNativeQuery(redUpdate);
			qq.setParameter("id", idTable);
    		qq.setParameter("relationsCount", relationsCount);    		
    		qq.setParameter("sourcePosCount", sourcePosCount);    		
    		qq.setParameter("targetPosCount", targetPosCount);    		
    		int result = qq.executeUpdate();
			JPA.getUtil().commit(); // Успешно завършване на транзакцията

	   	} catch (Exception e) {
			LOGGER.error("Грешка при инсърт в таблица RELATION"+redInsert);
			LOGGER.error("Грешка при извличане на елемент по id на предходен", e);
			System.out.println("actionSave:");
//			System.out.println("sourceCode:"+sourceCode);
			return;
		}
			
			JPA.getUtil().closeConnection();
			
			
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
					getMessageResourceString("ui_beanMessages", SUCCESSAVEMSG));
			//Долното от Илия за да се показват позициите веднага след запис
			CorespTableBean veb = (CorespTableBean) JSFUtils.getManagedBean("corespTableBean");
			System.out.println();
			veb.loadRelations();
			if(veb!=null) {
//				veb.setPosition3Initialized(false);
			}

		}

	    
		public void handleFileUploadTextarea(FileUploadEvent event) {
	        String jsVal = "PF('textarea').jq.val";
	        String fileName = EscapeUtils.forJavaScript(event.getFile().getFileName());
	        PrimeFaces.current().executeScript(jsVal + "(" + jsVal + "() + '\\n\\n" + fileName + " uploaded.')");
	    }

	    public void handleFilesUpload(FilesUploadEvent event) {
	        for (UploadedFile f : event.getFiles().getFiles()) {
	            FacesMessage message = new FacesMessage("Successful", f.getFileName() + " is uploaded.");
	            FacesContext.getCurrentInstance().addMessage(null, message);
	        }
	    }

	    public UploadedFile getFile() {
	        return file;
	    }

	    public void setFile(UploadedFile file) {
	        this.file = file;
	    }

	    public UploadedFiles getFiles() {
	        return files;
	    }

	    public void setFiles(UploadedFiles files) {
	        this.files = files;
	    }

//	    public String getDropZoneText() {
//	        return dropZoneText;
//	    }
//
//	    public void setDropZoneText(String dropZoneText) {
//	        this.dropZoneText = dropZoneText;
//	    }
	
	
	
	
	
		@SuppressWarnings("rawtypes")
		public void onItemSelect(SelectEvent event) {
//			System.out.println("onItemSelect");
	    	Object a = event.getSource();

		}
   
    
	@SuppressWarnings("rawtypes")
	public void onItemUnselect(UnselectEvent event) {
//		System.out.println("hhh");
    	Object a = event.getSource();
    	String b = event.getObject().toString();
        FacesMessage msg = new FacesMessage();
        msg.setSummary("Item unselected: " + event.getObject().toString());
        msg.setSeverity(FacesMessage.SEVERITY_INFO);

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }


    public void actionNew() {

        this.fromEdit = true;
        this.group = new AdmGroup();
        this.group.setGroupRoles(new ArrayList<>());
        
        this.usersList.clear();
        this.usersImena.clear();
        this.usersName = "";
        this.selClassif = "";

//        this.rootNode = null;
//        this.selectedNode = null;
    }

    // намиране на всички родители на конкретния код
    public List<SystemClassif> findAllParents(Integer codeClassif, Integer codeRole) {

        List<SystemClassif> parentsList = new ArrayList<>();

        try {

            parentsList = getSystemData().getParents(codeClassif, codeRole, getToday(), getCurrentLang());

        } catch (DbErrorException e) {
            LOGGER.error("Грешка при търсене на списъка с родителите! ", e);
            JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"));
        }

        return parentsList;
    }
    
 
    public Integer getCodeClassifUser() {
        return SysConstants.CODE_CLASSIF_USERS;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public AdmGroup getGroup() {
        return group;
    }

    public void setGroup(AdmGroup group) {
        this.group = group;
    }

    public List<AdmGroup> getListGrops() {
        return listGrops;
    }

    public void setListGrops(List<AdmGroup> listGrops) {
        this.listGrops = listGrops;
    }

    public String getUsersName() {
        return usersName;
    }

    public void setUsersName(String usersName) {
        this.usersName = usersName;
    }

    public List<Integer> getUsersList() {
        return usersList;
    }


    public List<String> getUsersImena() {
        return usersImena;
    }

    public void setUsersImena(List<String> usersImena) {
        this.usersImena = usersImena;
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

 
    public boolean isFromEdit() {
        return fromEdit;
    }

    public void setFromEdit(boolean fromEdit) {
        this.fromEdit = fromEdit;
    }

    public String getSelRkvForGr() {
        return selRkvForGr;
    }

    public void setSelRkvForGr(String selRkvForGr) {
        this.selRkvForGr = selRkvForGr;
    }

    public String getSelRkvForDos() {
        return selRkvForDos;
    }

    public void setSelRkvForDos(String selRkvForDos) {
        this.selRkvForDos = selRkvForDos;
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
    
/******************************** EXPORTS **********************************/
	
	/**
	 * за експорт в excel - добавя заглавие и дата на изготвяне на справката и др. - за групи потребителите
	 */
	public void postProcessXLS(Object document) {
		
		String title = getMessageResourceString(UI_LABELS, "groupsList.reportTitle");		  
    	new CustomExpPreProcess().postProcessXLS(document, title, null, null, null);		
     
	}

	/**
	 * за експорт в pdf - добавя заглавие и дата на изготвяне на справката - за групи потребителите
	 */
	public void preProcessPDF(Object document)  {
		try{
			
			String title = getMessageResourceString(UI_LABELS, "groupsList.reportTitle");		
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

	public String[] getSelectedCities2() {
		return selectedCities2;
	}

	public void setSelectedCities2(String[] selectedCities2) {
		this.selectedCities2 = selectedCities2;
	}

	public List<String> getCities() {
		return cities;
	}

	public void setCities(List<String> cities) {
		this.cities = cities;
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


	public String[] getSelectedCities1() {
		return selectedCities1;
	}


	public void setSelectedCities1(String[] selectedCities1) {
		this.selectedCities1 = selectedCities1;
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






	public List<String> getRelaciis() {
		return relaciis;
	}

	public void setRelaciis(List<String> relaciis) {
		this.relaciis = relaciis;
	}



	public Integer getTableId() {
		return tableId;
	}




	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}



	public class TempOpis implements Serializable{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public Long   id;
    	public String docTom;
 		public String regNomer;
    	public Date   dataReg;
    	public String otnosno;
    	public Long   brLista;
    	public Date   dataVl;
    	public String tekStr;
    	
    	
		public String getTekStr() {
			return tekStr;
		}

		public void setTekStr(String tekStr) {
			this.tekStr = tekStr;
		}

		public TempOpis(Long id, String docTom, String regNomer, Date dataReg,String otnosno,
				Long brLista,Date dataVl, String tekStr) {
			this.id		 = id;
			this.docTom  = docTom;
			this.regNomer= regNomer;
			this.dataReg = dataReg;
			this.otnosno = otnosno;
			this.brLista = brLista;
			this.dataVl  = dataVl;
			this.tekStr  = tekStr;
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
	public String[] getSelectedRelacii() {
		return selectedRelacii;
	}

	public void setSelectedRelacii(String[] selectedRelacii) {
		this.selectedRelacii = selectedRelacii;
	}
	public HashMap<String, Integer> getAllAtrib() {
		return allAtrib;
	}

	public void setAllAtrib(HashMap<String, Integer> allAtrib) {
		this.allAtrib = allAtrib;
	}

	public Integer getNomerPored() {
		return nomerPored;
	}
	public void setNomerPored(Integer nomerPored) {
		this.nomerPored = nomerPored;
	}
	   
    public HashMap<Integer, String> getIzbr() {
		return izbr;
	}

	public void setIzbr(HashMap<Integer, String> izbr) {
		this.izbr = izbr;
	}




	public List<SelectItem> getHarakterPromianaList() {
		return harakterPromianaList;
	}




	public void setHarakterPromianaList(List<SelectItem> harakterPromianaList) {
		this.harakterPromianaList = harakterPromianaList;
	}




	public HashMap<String, Integer> getPromianaRelacii() {
		return promianaRelacii;
	}




	public void setPromianaRelacii(HashMap<String, Integer> promianaRelacii) {
		this.promianaRelacii = promianaRelacii;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}
	private Integer progress=0;

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
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
		System.out.println("progress:"+progress);
	}




	public String getTipNaFile() {
		return tipNaFile;
	}




	public void setTipNaFile(String tipNaFile) {
		this.tipNaFile = tipNaFile;
	}




	public String getZabrSimvoli() {
		return zabrSimvoli;
	}




	public void setZabrSimvoli(String zabrSimvoli) {
		this.zabrSimvoli = zabrSimvoli;
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
	public Integer getErrMe() {
		return errMe;
	}

	public void setErrMe(Integer errMe) {
		this.errMe = errMe;
	}
	public boolean isFlPlus() {
		return flPlus;
	}

	public void setFlPlus(boolean flPlus) {
		this.flPlus = flPlus;
	}




	public String getInsertType() {
		return insertType;
	}

	public void onTransfer(TransferEvent event) {
		System.out.println("minava");
	}



	public void setInsertType(String insertType) {
		this.insertType = insertType;
	}
	
	public DualListModel<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(DualListModel<String> attributes) {
		this.attributes = attributes;
	}
	
	
}


