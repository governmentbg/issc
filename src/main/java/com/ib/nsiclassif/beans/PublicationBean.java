package com.ib.nsiclassif.beans;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;
import com.ib.indexui.utils.JSFUtils;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.faces.context.ExternalContext;
import com.ib.system.db.dto.Files;
import com.ib.nsiclassif.db.dto.Publication;
import com.ib.nsiclassif.db.dto.PublicationLang;
import com.ib.system.db.dao.FilesDAO;
import com.ib.nsiclassif.db.dao.PublicationDAO;
import com.ib.nsiclassif.db.dao.PublicationLangDAO;
import com.ib.nsiclassif.system.NSIConstants;
//import com.ib.indexui.navigation.Navigation;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;

@Named("publData")
@ViewScoped
public class PublicationBean extends IndexUIbean {
	/**
	 * 
	 */
	private static final long serialVersionUID = -938520067947253105L;

	/**
	 * Основен javaBean клас за въвеждане/актуализация на публикациите
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(PublicationBean.class);

	private Publication publication = new Publication();
	private Integer idPubl = null;
	private String sectionText = "";
//	private String publTypeText="";
	private Integer codeSection = null;
//	private Integer codeType=null;
	private Integer idUser = null;
	private StreamedContent imageCont = null;
//	private StreamedContent imageContZ=null;

	private Integer lang = null;
	private Integer oldLang = null;
	private Integer langBG = Integer.valueOf(Constants.CODE_LANG_BG);
	private HashMap<Integer, String> langHM = new HashMap<Integer, String>();
	private PublicationLang publSelLang = new PublicationLang();
	private List<PublicationLang> publLangList = new ArrayList<PublicationLang>();
	private List<PublicationLang> pubLdelLang = new ArrayList<PublicationLang>();
	private HashMap<Integer, PublicationLang> publListHM = new HashMap<Integer, PublicationLang>();
	private Boolean codeYT = false;
	private HashMap<Integer, String> titleAttach = new HashMap<Integer, String>();
	private HashMap<Integer, String> typeFilesAttach = new HashMap<Integer, String>();
	private HashMap<Integer, String> typeFilesMessages = new HashMap<Integer, String>();
	private static final String SUCCESSDELETEMSG = "general.successDeleteMsg";

	// Files

	private List<Files> filesList = new ArrayList<Files>();
	private List<Files> deleteFilesList = new ArrayList<Files>();
	private HashMap<Integer, List<Files>> filesListHM = new HashMap<Integer, List<Files>>();
	private String titleText = "";
	private int size;
	boolean bSave=true;
	/*	*//** за конкретната система */
	/*
	 * public static final String beanMessages = "beanMessages";
	 *//** за конкретната система *//*
									 * public static final String LABELS = "labels";
									 */

	/**
	 * Инициира/сетва първоначалните стойности на атрибутите на обектите. Чете
	 * предадените параметри от други екрани
	 * 
	 */
	@PostConstruct
	public void initData() {
		LOGGER.debug("PostConstruct!");

		this.idUser = getUserData().getUserId();

		// Id from filter
		String par = JSFUtils.getRequestParameter("idPubl");
		// Lang from filter
		String parLang = JSFUtils.getRequestParameter("langF");
		if (parLang != null && !parLang.trim().isEmpty()) {
			this.lang = (Integer.valueOf(parLang));
			this.oldLang = (Integer.valueOf(parLang));
		}

		actionClear();
		fillAttachTitleHM();
//		clearListHM();

		if (par != null && !par.trim().isEmpty()) {
			this.setIdPubl(Integer.valueOf(par));
			loadPublById(this.idPubl);
		}

	}

	/**
	 * Изтрива стойностите на определени атрибути на обектите
	 * 
	 */
	public void actionClear() {
		this.sectionText = "";
		Integer sect = this.publication.getSection();
		Integer typePubl = this.publication.getTypePub();
		this.publication = new Publication();
		this.publication.setSection(sect);
		try {
			if (null != sect)
				this.sectionText = getSystemData().decodeItem(NSIConstants.CODE_SYSCLASS_SECT_PUBL, sect,
						getCurrentLang(), new Date());

		} catch (DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "general.errDataBaseMsg"), e.getMessage());
		} finally {
			JPA.getUtil().closeConnection();
		}

		this.publication.setTypePub(typePubl);
		this.publication.setDateFrom(new Date());
		this.publLangList = new ArrayList<PublicationLang>();
		this.publSelLang = new PublicationLang();
		this.setIdPubl(null);

//		this.publTypeText="";
		this.codeSection = null;
//		this.codeType=null;

		this.imageCont = null;
		this.filesList = new ArrayList<>();
		this.deleteFilesList = new ArrayList<>();

		this.codeYT = false;
		if (null == this.lang)
			this.lang = getCurrentLang();
		if (null == this.oldLang)
			this.oldLang = getCurrentLang();

		clearListHM();
		fillLangHM();

	}

	public Publication getPublication() {
		return publication;
	}

	public void setPublication(Publication publication) {
		this.publication = publication;
	}

	/**
	 * Записва в БД на въведените/актуализираните информационни обекти/публикации
	 * 
	 */
	public void actionSave() {

		if (null == this.publSelLang.getLang())
			this.publSelLang.setLang(getOldLang());
		this.publListHM.put(getOldLang(), this.publSelLang);
		if (!checkData())
			return;

		

		try {

			setAllLangsForPublFiles();
			
			PublicationLangDAO pubLangDAO = new PublicationLangDAO(getUserData());
			PublicationDAO publDAO = new PublicationDAO(getUserData());
			FilesDAO filesDAO = new FilesDAO(getUserData());

			JPA.getUtil().runInTransaction(() -> {

				// Save*Update Section
				this.publication = publDAO.save(this.publication);

				// Delete join records for lang - pubLLang
				if (null != this.pubLdelLang) {
					for (PublicationLang pld : this.pubLdelLang) {
						if (null != pld.getId())
							pubLangDAO.deleteById(pld.getId());
					}
				}

				// Save pubLLang
				if (null != this.publLangList) {
					for (PublicationLang pl : this.publLangList) {
						if (null == pl.getTitle() || pl.getTitle().trim().isEmpty()) {
							if (null!=pl.getId())
								pubLangDAO.deleteById(pl.getId());
							
							continue;
							
						}
						
						if (null == pl.getId())
							pl.setPubId(this.publication.getId());

						pubLangDAO.save(pl);
					}
				}

				// Delete Files
				if (null != this.deleteFilesList) {
					for (Files f : this.deleteFilesList) {
						if (null != f.getId())
							filesDAO.deleteFileObject(f);
					}
				}

				// save/update files
				if (null != this.filesList) {
					for (Files f : this.filesList) {
						if (f.getId() == null) {
							filesDAO.saveFileObject(f, this.publication.getId(),
									NSIConstants.CODE_ZNACHENIE_JOURNAL_PUBLICATION);
						} else { // TODO -update files & filesObjects
							filesDAO.updateFileObject(f, null);
						}
					}
				}
			});

			
			if (null != this.publLangList && !this.publLangList.isEmpty()) {
//			if (this.bSave)
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, getMessageResourceString(UI_beanMessages, SUCCESSAVEMSG));
			}else {
				JSFUtils.addMessage("formPublData:idTitle",	FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "section.noTitle"));
			}

			if (!this.pubLdelLang.isEmpty()) {
				this.pubLdelLang.clear();
			}
			if (!this.deleteFilesList.isEmpty()) {
				this.deleteFilesList.clear();
			}

			refreshImagePub();
			
			scrollToMessages();

			/*
			 * org.hibernate.Cache cacheP = (org.hibernate.Cache)
			 * JPA.getUtil().getEntityManager().getEntityManagerFactory().getCache(); if
			 * (cacheP!=null) cacheP.evictQueryRegion("publR");
			 */

		} catch (BaseException e) {
			LOGGER.error("Грешка при запис на Секция! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());

		}

	}

	/**
	 * Изтрива от БД на определена публикация
	 * 
	 */
	public void actionDelete() {

		try {

			FilesDAO filesDAO = new FilesDAO(getUserData());
			PublicationLangDAO pubLangDAO = new PublicationLangDAO(getUserData());
			PublicationDAO publDAO = new PublicationDAO(getUserData());

			JPA.getUtil().runInTransaction(() -> {
				// Прикачените файлове
				for (Files item : this.filesList) {
					if (null != item.getId()) {
						filesDAO.deleteFileObject(item);
					}
				}

				for (Files item : this.deleteFilesList) {
					if (null != item.getId()) {
						filesDAO.deleteFileObject(item);
					}
				}

				// Публикациите - езиковите записи

				for (PublicationLang item : this.publLangList) {
					if (null != item.getId())
						pubLangDAO.deleteById(item.getId());
				}
				for (PublicationLang item : this.pubLdelLang) {
					if (null != item.getId())
						pubLangDAO.deleteById(item.getId());
				}

				// Публикациите
				if (null != this.publication.getId())
					publDAO.deleteById(this.publication.getId());

			});
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO,
					getMessageResourceString(UI_beanMessages, SUCCESSDELETEMSG));

			if (!this.deleteFilesList.isEmpty())
				this.deleteFilesList.clear();
			if (!this.pubLdelLang.isEmpty())
				this.pubLdelLang.clear();

			actionClear();
			/*
			 * Navigation navHolder = new Navigation(); int i =
			 * navHolder.getNavPath().size();
			 * 
			 * if(i > 2) { navHolder.goTo(String.valueOf(i-3)); } else if(i > 1) {
			 * navHolder.goBack(); }
			 */
		} catch (ObjectInUseException e) {

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
			LOGGER.error(e.getMessage(), e);

		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на Секция! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "general.errDataBaseMsg"), e.getMessage());

		}

	}

	public String getSectionText() {
		return sectionText;
	}

	public void setSectionText(String sectionText) {
		this.sectionText = sectionText;
	}

	public Integer getCodeSection() {
		return codeSection;
	}

	public void setCodeSection(Integer codeSection) {
		this.codeSection = codeSection;
		this.publication.setSection(codeSection);
	}

	/**
	 * Проверява дата от и дата до на публикациите
	 * 
	 * @param nom
	 */
	public boolean checkDates(int nom) {
		boolean check = true;
		if (this.publication.getDateFrom() != null && this.publication.getDateTo() != null) {
			if (this.publication.getDateFrom().compareTo(this.publication.getDateTo()) > 0) {
				check = false;
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
						getMessageResourceString(beanMessages, "section.dateFromLessDateTo"));
			}
		}
		return check;
	}

	/**
	 * Избор на изображение към публикация
	 * 
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		try {
			if (null != event) {
				this.publication.setImagePub(event.getFile().getContent());
				// this.publication.setImagePub(event.getFile().getContents());
				refreshImagePub();
			} else {

				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при качване на файла!");
			}

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при качване на файла!", e.getMessage());
		}

	}

	public Date getCurrentDate() {
		return new Date();
	}

	public StreamedContent getImageCont() {
		return imageCont;
	}

	public void setImageCont(StreamedContent imageCont) {
		this.imageCont = imageCont;
	}

	/**
	 * Избор на файлове за присъединяване към публикациите
	 * 
	 * @param event
	 */
	public void uploadFileListener(FileUploadEvent event) {
		if (null == event)
			return;

		try {

			UploadedFile upFile = event.getFile();
			if (!checkUploadedFileForDuplicate(upFile)) {
				Files fileObject = new Files();
				fileObject.setFilename(upFile.getFileName());
				// fileObject.setFileInfo(upFile.getFileName());
				fileObject.setContentType(upFile.getContentType());
				fileObject.setContent(upFile.getContent());
				fileObject.setFileObjectId(this.publication.getId());
				// fileObject.setLang(this.lang);
				this.getFilesList().add(fileObject);
				// this.filesListHM.get(this.lang).add(fileObject);
				this.filesListHM.get(langBG).add(fileObject);
			}

		} catch (Exception e) {
			LOGGER.error("Exception: " + e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при качване на файла!", e.getMessage());
		}
	}

	public List<Files> getFilesList() {
		return filesList;
	}

	public void setFilesList(List<Files> filesList) {
		this.filesList = filesList;
	}

	public List<Files> getDeleteFilesList() {
		return deleteFilesList;
	}

	public void setDeleteFilesList(List<Files> deleteFilesList) {
		this.deleteFilesList = deleteFilesList;
	}

	/**
	 * Извлича присъединените файлове от БД и ги предлага за разглеждане/съхраняване
	 * при клиента
	 * 
	 * @param file
	 */
	public void download(Files file) {
		try {

			if (file.getContent() == null && file.getId() != null) {

				FilesDAO filesDAO = new FilesDAO(getUserData());
				file = filesDAO.findById(file.getId());

				if (file.getPath() != null && !file.getPath().isEmpty()) {
					Path path = Paths.get(file.getPath());
					file.setContent(java.nio.file.Files.readAllBytes(path));
				}

			}

			String codedfilename = URLEncoder.encode(file.getFilename(), "UTF8");
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			externalContext.setResponseHeader("Content-Type", "application/x-download");
			externalContext.setResponseHeader("Content-Length", file.getContent().length + "");
			externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + codedfilename + "\"");
			externalContext.getResponseOutputStream().write(file.getContent());
			facesContext.responseComplete();

		} catch (DbErrorException e) {
			LOGGER.error("DbErrorException: " + e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "general.errDataBaseMsg"), e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при сваляне на файла!: ", e.getMessage());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при сваляне на файла!: ", e.getMessage());
		}
	}

	/**
	 * Обработва езиковите записи
	 * 
	 */
	public void setAllLangsForPublFiles() {

		// 1. Publication Langs Update DB records, delete old and insert new
		List<PublicationLang> pubLnew = new ArrayList<PublicationLang>();
		this.pubLdelLang = new ArrayList<PublicationLang>();
		Iterator<PublicationLang> iterator = this.publListHM.values().iterator();
		PublicationLang pubL = new PublicationLang();
		int n;
		boolean ch;
		this.bSave=true;
		try {

			while (iterator.hasNext()) {
				n = 0;
				ch=true;
				pubL = new PublicationLang();
				pubL = (PublicationLang) iterator.next();
				
				if (null == pubL)
					continue;
				
				if (null == pubL.getTitle() || pubL.getTitle().isEmpty())
					ch=false;
				
				if (ch) {
					if (this.publLangList.size() > 0) {
						for (int i = 0; i < this.publLangList.size(); i++) {
							if (this.publLangList.get(i).getId().equals(pubL.getId())) {
								this.publLangList.set(i, pubL);
								n++;
							}
						}
					}
	
					if (n == 0)
						pubLnew.add(pubL);
				} 
				else 
//					if (null!=pubL.getLang() && pubL.getLang().equals(Integer.valueOf(getCurrentLang())))
					{ 
//					JSFUtils.addMessage("formPublData:idTitle",
//					FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "section.noTitle")+":" +getSystemData().decodeItemDopInfo(Constants.CODE_CLASSIF_LANG, pubL.getLang(), pubL.getLang(), new Date())); 
//					FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "section.noTitle")+":" +getSystemData().decodeItem(NSIConstants.CODE_CLASSIF_LANG, pubL.getLang(), getCurrentLang(), new Date()));		
					
					this.bSave=false;
				}
			}

			/*
			 * } catch (Exception e) { LOGGER.error(e.getMessage(), e);
			 * JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
			 * "Грешка при обработката на информационните обекти!", e.getMessage()); }
			 */

		if (null != this.pubLdelLang && this.pubLdelLang.size() > 0)
			this.publLangList.removeAll(this.pubLdelLang);

		if (!pubLnew.isEmpty())
			this.publLangList.addAll(pubLnew);

		/*
		 * // 2. Files Langs Update DB records, delete old and insert new List<Files>
		 * filenew= new ArrayList<Files>(); this.deleteFilesList= new
		 * ArrayList<Files>(); Iterator<Files> iter = filesListHM.values().iterator();
		 * Files file = new Files();
		 * 
		 * ch=true; while(iterator.hasNext()){ n=0; file = new Files();
		 * file=(Files)iter.next(); if (null==file) continue; if(null==file.getLang())
		 * ch=false; if (this.filesList.size() > 0) { for (int i = 0; i <
		 * this.filesList.size(); i++) { if
		 * (this.filesList.get(i).getId().equals(file.getId())) { if(ch) {
		 * this.filesList.set(i, file); }else{
		 * this.deleteFilesList.add(this.filesList.get(i)); } n++; } } }
		 * 
		 * if (n==0 && ch) filenew.add(file); }
		 * 
		 * if (null!=this.deleteFilesList && this.deleteFilesList.size()>0)
		 * this.filesList.removeAll(this.deleteFilesList);
		 * 
		 * if (!filenew.isEmpty()) this.filesList.addAll(filenew);
		 * 
		 */
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при обработката на информационните обекти!",
					e.getMessage());
		}

	}

	/**
	 * Проверка на атрибутите на публикация - задължителни, преди запис в БД
	 * 
	 * @return
	 */
	public boolean checkRequiredAttr(PublicationLang pub) {
		boolean ver = true;
		if ((null == pub.getAnnotation() || pub.getAnnotation().isEmpty())
				&& (null == pub.getFullText() || pub.getFullText().isEmpty())
				&& (null == pub.getOtherInfo() || pub.getOtherInfo().isEmpty())
				&& (null == pub.getUrlPub() || pub.getUrlPub().isEmpty()) || null == pub.getLang()) {
			ver = false;
		}

		return ver;
	}

	/**
	 * Проверка на въведените данни на публикация, преди запис в БД
	 * 
	 * @return
	 */

	public boolean checkData() {
		boolean ver = true;
		// 1. Required

		if (null == this.publication.getSection()) {
			JSFUtils.addMessage("formPublData:idSectionText", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "section.nosection"));
			ver = false;
		}
		if (null == this.publication.getTypePub()) {
			JSFUtils.addMessage("formPublData:idPublTypeText", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "section.noTypePubl"));
			ver = false;
		}

		if (null == this.publication.getDateFrom()) {
			JSFUtils.addMessage("formPublData:dateOt", FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "section.noDateOt"));
			ver = false;
		}

//		if (null != this.publLangList && this.publLangList.size() > 0) {
//			for (int i = 0; i < this.publLangList.size(); i++) {
//				if (null != this.publLangList.get(i).getId()) {
//					if (null == this.publLangList.get(i).getTitle()) {
//						JSFUtils.addMessage("formPublData:idTitle", FacesMessage.SEVERITY_ERROR,
//								getMessageResourceString(beanMessages, "section.noTitle"));
//						ver = false;
//					}
//				}
//			}
//		}

		/*
		 * if(null==this.publLangList || this.publLangList.isEmpty()) { ver=false;
		 * JSFUtils.addMessage("formPublData:idTitle", FacesMessage.SEVERITY_ERROR,
		 * getMessageResourceString(beanMessages,"publ.noTitle"));
		 * 
		 * }
		 */

		// 2. Dates

		if (ver) {
			ver = checkDates(1);

			/*
			 * this.publication.setDateFrom(this.getDateFrom());
			 * this.publication.setDateTo(this.getDateTo());
			 */

		}

		return ver;
	}

	public Integer getIdPubl() {
		return idPubl;
	}

	public void setIdPubl(Integer idPubl) {
		this.idPubl = idPubl;
	}

	/**
	 * Извлича данни за определена публикация по ид.
	 * 
	 * @param idPubl
	 */

	public void loadPublById(Integer idPubl) {

		try {

			JPA.getUtil().runWithClose(
					() -> this.publication = (Publication) new PublicationDAO(getUserData()).findById(this.idPubl));
			/*
			 * this.dateFrom=this.publication.getDateFrom();
			 * this.dateTo=this.publication.getDateTo();
			 */

			this.codeSection = this.publication.getSection();
			if (null != this.codeSection) {
				this.sectionText = getSystemData().decodeItem(NSIConstants.CODE_SYSCLASS_SECT_PUBL, this.codeSection,
						getCurrentLang(), new Date());
			}

			/*
			 * this.codeType = this.publication.getTypePub(); if(null!=this.codeType){
			 * this.publTypeText=
			 * getSystemData().decodeItem(Constants.CODE_SYSCLASS_PUBL_TYPE, this.codeType,
			 * getCurrentLang(), new Date(), this.idUser); }
			 */

			refreshImagePub();

			// Lang Sections
			JPA.getUtil().runWithClose(() -> {
				this.publLangList = (List<PublicationLang>) new PublicationLangDAO(getUserData())
						.findByIdPubl(this.idPubl);
				// Files
				this.filesList = (List<Files>) new FilesDAO(getUserData()).selectByFileObjectDop(this.idPubl,
						NSIConstants.CODE_ZNACHENIE_JOURNAL_PUBLICATION);
			});
			loadLangHM();
//			fillAttachTitleHM();

		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане секцията! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}

	}

	public Integer getIdUser() {
		return idUser;
	}

	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}

	public void clearListHM() {
		List<SystemClassif> langElementsList = new ArrayList<SystemClassif>();
		try {
			langElementsList = getSystemData().getSysClassification(Constants.CODE_CLASSIF_LANG, new Date(),
					getCurrentLang());
		} catch (DbErrorException e) {
			LOGGER.error("DbErrorException: " + e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "general.errDataBaseMsg"), e.getMessage());
		}
		this.publListHM = new HashMap<Integer, PublicationLang>();
		for (SystemClassif item : langElementsList) {
			this.publListHM.put(Integer.valueOf(item.getCode()), new PublicationLang());
			this.publListHM.get(Integer.valueOf(item.getCode())).setLang(item.getCode());
		}

		this.filesListHM = new HashMap<Integer, List<Files>>();
		/*
		 * For lang version of Files for (SystemClassif item: langElementsList){
		 * this.filesListHM.put(Integer.valueOf(item.getCode()), new
		 * ArrayList<Files>()); }
		 */
		// For only BG version of Files
		this.filesListHM.put(langBG, new ArrayList<Files>());
	}

	public void fillLangHM() {// HashMap for id & name of langs
		this.langHM = new HashMap<Integer, String>();

		List<SystemClassif> langElementsList = new ArrayList<SystemClassif>();
		try {
			langElementsList = getSystemData().getSysClassification(Constants.CODE_CLASSIF_LANG, new Date(),
					getCurrentLang());
		} catch (DbErrorException e) {
			LOGGER.error("DbErrorException: " + e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "general.errDataBaseMsg"), e.getMessage());
		}
		for (SystemClassif item : langElementsList) {
			this.langHM.put(Integer.valueOf(item.getCode()), item.getDopInfo());
		}

	}

	public List<PublicationLang> getPublLangList() {
		return publLangList;
	}

	public void setPublLangList(List<PublicationLang> publLangList) {
		this.publLangList = publLangList;
	}

	public HashMap<Integer, PublicationLang> getPublListHM() {
		return publListHM;
	}

	public void setPublListHM(HashMap<Integer, PublicationLang> publListHM) {
		this.publListHM = publListHM;
	}

	public Integer getLang() {
		/*
		 * if (!getCurrentLang().equals(this.oldLang)) { setLang(getCurrentLang());
		 * changeLang(); }
		 */
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public Integer getOldLang() {
		return oldLang;
	}

	public void setOldLang(Integer oldLang) {
		this.oldLang = oldLang;
	}

	public PublicationLang getPublSelLang() {
		return publSelLang;
	}

	public void setPublSelLang(PublicationLang publSelLang) {
		this.publSelLang = publSelLang;
	}

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public void changeLang() {
		// Publication Langs
		if (null == this.publSelLang.getLang())
			this.publSelLang.setLang(getOldLang());
		this.publListHM.put(getOldLang(), this.publSelLang);
		this.publSelLang = new PublicationLang();
		this.publSelLang = this.publListHM.get(this.lang);
		if (null == this.publSelLang.getLang())
			this.publSelLang.setLang(this.lang);

		// Files lang
		/*
		 * if(null==this.fileSelLang.getLang()) this.fileSelLang.setLang(getOldLang());
		 * this.filesListHM.put(getOldLang(), this.fileSelLang); this.fileSelLang = new
		 * Files(); this.fileSelLang=this.filesListHM.get(this.lang);
		 * if(null==this.fileSelLang.getLang()) this.fileSelLang.setLang(this.lang);
		 */

		this.oldLang = this.lang;
		refreshImagePub();
	}

	public HashMap<Integer, String> getLangHM() {
		return langHM;
	}

	public void setLangHM(HashMap<Integer, String> langHM) {
		this.langHM = langHM;
	}

	public List<PublicationLang> getPubLdelLang() {
		return pubLdelLang;
	}

	public void setPubLdelLang(List<PublicationLang> pubLdelLang) {
		this.pubLdelLang = pubLdelLang;
	}

	public void loadLangHM() {
		// Publication langs
		if (null != this.publLangList && this.publLangList.size() > 0) {
			for (int i = 0; i < this.publLangList.size(); i++) {
				if (null != this.publLangList.get(i).getLang())
					this.publListHM.put(this.publLangList.get(i).getLang(), this.publLangList.get(i));
			}
		}
		// Дали е тип видео и се използав или не YouTube
		this.publSelLang = this.publListHM.get(this.lang);
		if (this.publication.getTypePub().equals(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_VIDEO))
				&& (null == this.publSelLang.getUrlPub() || this.publSelLang.getUrlPub().equals(""))) {
			this.codeYT = false;
		} else {
			this.codeYT = true;
		}

		// Files langs

		// if (null!=this.filesList && this.filesList.size() > 0) {
		// for (int i = 0; i < this.filesList.size(); i++) {
		// if (null!=this.filesList.get(i).getLang())
		// this.filesListHM.get(this.filesList.get(i).getLang()).add(this.filesList.get(i));
		// }
		// }

		// this.fileSelLang=this.filesListHM.get(this.lang);

		// Only BG version
		if (null != this.filesList && this.filesList.size() > 0) {
			for (int i = 0; i < this.filesList.size(); i++) {
				this.filesListHM.get(langBG).add(this.filesList.get(i));
			}
		}

	}

	public void refreshImagePub() {
		if (null != this.publication.getImagePub()) {
			try {

				InputStream imageStream = new ByteArrayInputStream(this.publication.getImagePub());
//			this.imageCont = new DefaultStreamedContent(imageStream, "image/jpeg");
				this.imageCont = DefaultStreamedContent.builder().contentType("image/jpeg").name("")
						.stream(() -> imageStream).build();
				imageStream.close();

				this.size = 100;

			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при показване на изображение!",
						e.getMessage());
			}

		}
	}

	public HashMap<Integer, List<Files>> getFilesListHM() {
		return filesListHM;
	}

	public void setFilesListHM(HashMap<Integer, List<Files>> filesListHM) {
		this.filesListHM = filesListHM;
	}

	/*
	 * public List<Files> getFileSelLang() { return fileSelLang; }
	 * 
	 * public void setFileSelLang(List<Files> fileSelLang) { this.fileSelLang =
	 * fileSelLang; }
	 */

	// public void onCellEdit(Files row, int ind){
	// String nova=row.getFileInfo();
	// String old=this.filesList.get(ind).getFileInfo();
	// this.filesList.get(ind).setFileInfo(row.getFileInfo());
	// }

	// public void onRowEdit(RowEditEvent event) {
	// String fi=null;
	// fi=event.getObject().toString();
	// int i =1;
	// }

	public void remove(Files file) {

		if (null != file) {
			this.deleteFilesList.add(file);
			this.filesList.remove(file);
			// this.filesListHM.get(this.lang).remove(file);
			this.filesListHM.get(langBG).remove(file);
		}

	}

	public boolean checkUploadedFileForDuplicate(UploadedFile attachFile) {

		if (null == attachFile.getFileName() || null == attachFile.getContentType())
			return false;

		for (Files f : this.filesList) {
			if (f.getFilename().toUpperCase().toString().equals(attachFile.getFileName().toUpperCase().toString())
					&& f.getContentType().toUpperCase().toString()
							.equals(attachFile.getContentType().toUpperCase().toString())) {
				return true;
			}
		}
		return false;
	}

	/*
	 * public String getPublTypeText() { return publTypeText; }
	 * 
	 * public void setPublTypeText(String publTypeText) { this.publTypeText =
	 * publTypeText; }
	 * 
	 * public Integer getCodeType() { return codeType; }
	 * 
	 * public void setCodeType(Integer codeType) { this.codeType = codeType;
	 * this.publication.setTypePub(codeType); }
	 */

	public Boolean getCodeYT() {
		return codeYT;
	}

	public void setCodeYT(Boolean codeYT) {
		this.codeYT = codeYT;
	}

	public HashMap<Integer, String> getTitleAttach() {
		return titleAttach;
	}

	public void setTitleAttach(HashMap<Integer, String> titleAttach) {
		this.titleAttach = titleAttach;
	}

	public void fillAttachTitleHM() {

		this.titleAttach = new HashMap<Integer, String>();

		List<SystemClassif> publTypeList = new ArrayList<SystemClassif>();
		try {
			publTypeList = getSystemData().getSysClassification(NSIConstants.CODE_SYSCLASS_PUBL_TYPE, new Date(),
					getCurrentLang());
			for (SystemClassif item : publTypeList) {
				if (null != item) {
					int l = (int) item.getCode();
					switch (l) {
					case (int) NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_MATERIALI:
						this.titleAttach.put(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_MATERIALI),
								getMessageResourceString(LABELS, "section.attachedMater"));
						this.getTypeFilesAttach().put(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_MATERIALI),
								getSystemData().getSettingsValue("system.fileExtensionsForAttaching"));
						break;
					case (int) NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_IMAGES:
						this.titleAttach.put(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_IMAGES),
								getMessageResourceString(LABELS, "section.attachedImages"));
						this.getTypeFilesAttach().put(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_IMAGES),
								"/(\\.|\\/)(gif|jpe?g|png|bmp)$/");
						this.getTypeFilesMessages().put(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_IMAGES),
								getMessageResourceString(LABELS, "section.attachedImagesMess"));
						break;
					case (int) NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_VIDEO:
						this.titleAttach.put(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_VIDEO),
								getMessageResourceString(LABELS, "section.attachedVideo"));
						this.getTypeFilesAttach().put(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_VIDEO),
								"/(\\.|\\/)(mp4)$/");
						this.getTypeFilesMessages().put(Integer.valueOf(NSIConstants.CODE_ZNACHENIE_TYPE_PUBL_VIDEO),
								getMessageResourceString(LABELS, "section.attachedVideoMess"));
						break;
					default:
						break;
					}

				}

			}

		} catch (DbErrorException e) {
			LOGGER.error("DbErrorException: " + e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,
					getMessageResourceString(beanMessages, "general.errDataBaseMsg"), e.getMessage());
		}

	}

	public HashMap<Integer, String> getTypeFilesAttach() {
		return typeFilesAttach;
	}

	public void setTypeFilesAttach(HashMap<Integer, String> typeFilesAttach) {
		this.typeFilesAttach = typeFilesAttach;
	}

	public void prepareYouTube() {
		if (!this.codeYT) {
			this.publSelLang.setUrlPub(null);
		}

	}

	public HashMap<Integer, String> getTypeFilesMessages() {
		return typeFilesMessages;
	}

	public void setTypeFilesMessages(HashMap<Integer, String> typeFilesMessages) {
		this.typeFilesMessages = typeFilesMessages;
	}

	public void delImage() {
		this.publication.setImagePub(null);
		this.imageCont = null;
	}

	public StreamedContent scaleImage(int proc) {
		if (null == this.publication.getImagePub())
			return null;

		try {

			InputStream imageStream = new ByteArrayInputStream(this.publication.getImagePub());
//			this.imageCont = new DefaultStreamedContent(imageStream, "image/jpeg");
			this.imageCont = DefaultStreamedContent.builder().contentType("image/jpeg").name("")
					.stream(() -> imageStream).build();
			imageStream.close();

			if (null != this.imageCont) {
				BufferedImage img = ImageIO.read(this.imageCont.getStream());
				int h;
				int w;

				h = img.getHeight();
				w = img.getWidth();

				if (proc > 0) {
					this.size += proc;
					h = img.getHeight() * this.size / 100;
					w = img.getWidth() * this.size / 100;
				} else {
					this.size += proc;
					h = img.getHeight() * this.size / 100;
					w = img.getWidth() * this.size / 100;
				}

				Image scaledImage = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
				BufferedImage imageBuff = new BufferedImage(w, h, img.getType());
				imageBuff.getGraphics().drawImage(scaledImage, 0, 0, null);

				ByteArrayOutputStream buffer = new ByteArrayOutputStream();

				ImageIO.write(imageBuff, "jpeg", buffer);
				buffer.flush();
				imageBuff.flush();
				byte[] resizeBa = buffer.toByteArray();
				buffer.close();

				InputStream imageStream2 = new ByteArrayInputStream(resizeBa);
				// this.imageCont = new DefaultStreamedContent(imageStream, "image/jpeg");
				this.imageCont = DefaultStreamedContent.builder().contentType("image/jpeg").name(null)
						.stream(() -> imageStream2).build();
				imageStream2.close();

			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при показване на изображение!",
					e.getMessage());

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при показване на изображение!",
					e.getMessage());
		}

		return this.imageCont;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	public Integer getLangBG() {
		return langBG;
	}

	public void setLangBG(Integer langBG) {
		this.langBG = langBG;
	}

	
	/**
	 * Вика функцията scrollToErrors на страницата, за да се скролне екранът към съобщенията за грешка.
	 * Сложено е, защото иначе съобщенията може да са извън видимия екран и user изобшо да не разбере,
	 * че е излязла грешка, и каква.
	 */
	private void scrollToMessages() {
		PrimeFaces.current().executeScript("scrollToErrors()");
	}
}
