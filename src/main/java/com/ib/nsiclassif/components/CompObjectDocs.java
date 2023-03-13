package com.ib.nsiclassif.components;

import static com.ib.system.utils.SearchUtils.isEmpty;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.ObjectDocsDAO;
import com.ib.nsiclassif.db.dto.ObjectDocs;
import com.ib.nsiclassif.db.dto.ObjectDocsLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.UserData;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.ObjectInUseException;

/** */
@FacesComponent(value = "compObjectDocs", createTag = true)
public class CompObjectDocs extends UINamingContainer {
	
	private enum PropertyKeys {
		OBJDOCSLIST, OBJDOC, OBJDOCLANG, FILESLIST, FORPUBL, LANG, SHOWME
	}
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CompObjectDocs.class);
	
	public static final String	UIBEANMESSAGES = "ui_beanMessages";
	public static final String	BEANMESSAGES = "beanMessages";
	public static final String  MSGPLSINS = "general.pleaseInsert";
	public static final String  ERRDATABASEMSG = "general.errDataBaseMsg";
	public static final String  SUCCESSAVEMSG = "general.succesSaveMsg";
	private static final String SUCCESSDELETEMSG = "general.successDeleteMsg";
	public static final String	OBJECTINUSE		 = "general.objectInUse";
	public static final String	LABELS = "labels";	
	
	private String errMsg = null;
	private UserData userData = null;
	private Date dateClassif = null;
	
	private Integer idObject;
	private Integer codeObject;	
	
	/**
	 * Търсене на документи - инциализира компонентата   <f:event type="preRenderComponent" listener="#{cc.initDocsComp()}" />
	 */	
	public void initDocsComp() {
		
		this.idObject = (Integer) getAttributes().get("idObject"); 
		this.codeObject = (Integer) getAttributes().get("codeObject"); 
		setLang((Integer) getAttributes().get("lang")); 
		
		if (getLang() == null) {
			setLang(NSIConstants.CODE_DEFAULT_LANG);
		}
		
		setObjDocsList(new ArrayList<Object[]>());
		
		if (this.idObject != null && this.codeObject != null) {
			actionLoadDocsList(this.idObject, this.codeObject);
		}
		
		actionNew(this.idObject, this.codeObject);
		
		setShowMe(true);
		setErrMsg(null);
		LOGGER.debug("initDocsComp!!!");		
	}
	
	public void actionLoadDocsList(Integer idObj, Integer codeObj) {
		
		try {			
			
			JPA.getUtil().runWithClose(() -> setObjDocsList(new ObjectDocsDAO(getUserData()).findObjectDocsNative(codeObj, idObj, getLang())));
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане на списъка с документи! ", e);
		}
		
	}	
	
	public void actionEditDoc(Integer idDoc) {	
		
		try {

			if (idDoc != null) {

				JPA.getUtil().runWithClose(() -> {					
					
					setObjDoc(new ObjectDocsDAO(getUserData()).findById(idDoc));
					
					// извличане на файловете от таблица с файловете
					setFilesList(new FilesDAO(getUserData()).selectByFileObject(getObjDoc().getId(), NSIConstants.CODE_ZNACHENIE_JOURNAL_OBJECT_DOC));					
				
				});
				
				if (getObjDoc() != null) {
					
					if (Objects.equals(getObjDoc().getPubl(), NSIConstants.CODE_ZNACHENIE_DA)) {
						setForPubl(true);
					} else {
						setForPubl(false);
					}					
					
					if (getObjDoc().getLangMap().containsKey(getLang())) {
						setObjDocLang(getObjDoc().getLangMap().get(getLang()));
					}					
				}				
			}
			
//			String dialogWidgetVar = "PF('docEditVar').show();";
//			PrimeFaces.current().executeScript(dialogWidgetVar);

		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на документа! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}		
	}
	
	/**
	 * Зачиства данните за документ - бутон "нов"
	 * 
	 */
	public void actionNew(Integer idObj, Integer codeObj) {

		setObjDoc(new ObjectDocs());
		setObjDocLang(new ObjectDocsLang());
		setFilesList(new ArrayList<>());		
		
		getObjDoc().setIdObject(idObj);
		getObjDoc().setCodeObject(codeObj);
		
		getObjDocLang().setLang(getLang()); 
		
		setForPubl(false);
	}
	

	/**
	 * Запис на документ
	 */
	public void actionSave() {
		
		LOGGER.debug("actionSave >>> ");	
	
		if (checkData()) {
			errMsg = null;
			
			try {
			
				if (isForPubl()) {
					getObjDoc().setPubl(NSIConstants.CODE_ZNACHENIE_DA);
				} else {
					getObjDoc().setPubl(NSIConstants.CODE_ZNACHENIE_NE);
				}
				
				getObjDocLang().setObjectDocs(getObjDoc()); 
				
				getObjDoc().getLangMap().put(getObjDocLang().getLang(),  getObjDocLang());			
			
				JPA.getUtil().runInTransaction(() -> setObjDoc(new ObjectDocsDAO(getUserData()).save(getObjDoc())));
				
				actionLoadDocsList(getObjDoc().getIdObject(), getObjDoc().getCodeObject());

				// извиква remoteCommnad - ако има такава....
				String remoteCommnad = (String) getAttributes().get("onComplete");
				if (remoteCommnad != null && !"".equals(remoteCommnad)) {
					PrimeFaces.current().executeScript(remoteCommnad);
				}
			
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSAVEMSG));
				
//				String dialogWidgetVar = "PF('docEditVar').hide();";
//				PrimeFaces.current().executeScript(dialogWidgetVar);
//				
//				setObjDoc(new ObjectDocs());		
//				setObjDocLang(new ObjectDocsLang());	
			
			} catch (BaseException e) {
				LOGGER.error("Грешка при запис на документ ", e);
				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
			}			
		}
		
	}
	
	/**
	 * Проверка за валидни данни
	 * 
	 * @return
	 */
	public boolean checkData() {

		boolean flagSave = true;
		FacesContext context = FacesContext.getCurrentInstance();
		String clientId = null;
				
		if (context != null && getObjDoc() != null) {
			clientId = this.getClientId(context);		
		
			if (isEmpty(getObjDoc().getRnDoc())) {
				JSFUtils.addMessage(clientId + ":regNum", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
						IndexUIbean.getMessageResourceString(LABELS, "compObjectDocs.rnDoc")));
				errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compObjectDocs.rnDoc")) + "<br/>";	
				flagSave = false;	
			}
			
			if (getObjDoc().getDatDoc() == null) {
				JSFUtils.addMessage(clientId + ":datDoc", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
						IndexUIbean.getMessageResourceString(LABELS, "compObjectDocs.datDoc")));
				if (!isEmpty(errMsg)) { 
					errMsg += IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compObjectDocs.datDoc")) + "<br/>";
				} else {
					errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compObjectDocs.datDoc")) + "<br/>";
				}
				flagSave = false;	
			}
			
			if (getObjDoc().getType() == null) {
				JSFUtils.addMessage(clientId + ":docType", FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, 
						IndexUIbean.getMessageResourceString(LABELS, "compObjectDocs.type")));
				if (!isEmpty(errMsg)) { 
					errMsg += IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS, IndexUIbean.getMessageResourceString(LABELS, "compObjectDocs.type")) + "<br/>";
				} else {
					errMsg = IndexUIbean.getMessageResourceString(UIBEANMESSAGES, MSGPLSINS,  IndexUIbean.getMessageResourceString(LABELS, "compObjectDocs.type")) + "<br/>";
				}
				flagSave = false;	
			}		
		}
		
		return flagSave;
	}	
	
	/**
	 * Изтриване на документ
	 */
	public void actionDelete() {

		LOGGER.debug("actionDelete >>> ");

		try {
			
			Integer idObj = getObjDoc().getIdObject();
			Integer codeObj = getObjDoc().getCodeObject();
			
			JPA.getUtil().runInTransaction(() -> { 
				
				new ObjectDocsDAO(getUserData()).delete(getObjDoc());
				
				if (getFilesList() != null && !getFilesList().isEmpty()) { // трябва да се трият и файловете
					FilesDAO filesDao = new FilesDAO(getUserData());
					for (Files f : getFilesList()) {
						filesDao.deleteFileObject(f);
					}
				}
			
			});

			// извиква remoteCommnad - ако има такава....
			String remoteCommnad = (String) getAttributes().get("onComplete");
			if (remoteCommnad != null && !"".equals(remoteCommnad)) {
				PrimeFaces.current().executeScript(remoteCommnad);
			}
			
			String dialogWidgetVar = "PF('docEditVar').hide();";
			PrimeFaces.current().executeScript(dialogWidgetVar);
			
			setObjDoc(new ObjectDocs());		
			setObjDocLang(new ObjectDocsLang());

			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, SUCCESSDELETEMSG));
			
			actionLoadDocsList(idObj, codeObj);
		
		} catch (ObjectInUseException e) {
			LOGGER.error("Грешка при изтриване на документ! ObjectInUseException = {}", e.getMessage());
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		
		} catch (BaseException e) {
			LOGGER.error("Грешка при изтриване на документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, ERRDATABASEMSG));
		}
	}
	
	/** Избор на файлове
	 * 
	 * @param event
	 */
	public void uploadFileListener(FileUploadEvent event){		
		
		try {
			
			UploadedFile upFile = event.getFile();
			
			Files fileObject = new Files();
			fileObject.setFilename(upFile.getFileName());
			fileObject.setContentType(upFile.getContentType());
			fileObject.setContent(upFile.getContent());	
			
			JPA.getUtil().runInTransaction(() -> {
				
				new FilesDAO(getUserData()).saveFileObject(fileObject, getObjDoc().getId(), NSIConstants.CODE_ZNACHENIE_JOURNAL_OBJECT_DOC); 

				// извличане на файловете от таблица с файловете
				setFilesList(new FilesDAO(getUserData()).selectByFileObject(getObjDoc().getId(), NSIConstants.CODE_ZNACHENIE_JOURNAL_OBJECT_DOC));
			}); 
		
		} catch (Exception e) {
			LOGGER.error("Exception: " + e.getMessage());	
		} 
	}
	
	/**
	 * Download selected file
	 *
	 * @param files
	 */
	public void downloadFile(Files file) {

			boolean ok = true;
			
			if(file.getContent() == null && file.getId() != null) {
				
				try {					
					
					file = new FilesDAO(getUserData()).findById(file.getId());
					
					if(file.getPath() != null && !file.getPath().isEmpty()){
						Path path = Paths.get(file.getPath());
						file.setContent(java.nio.file.Files.readAllBytes(path));
					}
				
				} catch (DbErrorException e) {
					LOGGER.error("DbErrorException: " + e.getMessage());
					ok = false;
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, "general.errDataBaseMsg"));
				
				} catch (IOException e) {
					LOGGER.error("IOException: " + e.getMessage());
					ok = false;
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES,"general.unexpectedResult"));
					LOGGER.error(e.getMessage(), e);
				
				} catch (Exception e) {
					LOGGER.error("Exception: " + e.getMessage());
					ok = false;
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, "general.exception"));
				
				}  finally {
					JPA.getUtil().closeConnection();
				}
			}
			
			if(ok){
				
				try {
					
					FacesContext facesContext = FacesContext.getCurrentInstance();
				    ExternalContext externalContext = facesContext.getExternalContext();
				    
				    HttpServletRequest request =(HttpServletRequest)externalContext.getRequest();
					String agent = request.getHeader("user-agent");
					
					String codedfilename = ""; 
					
					if (null != agent &&  (-1 != agent.indexOf("MSIE") || (-1 != agent.indexOf("Mozilla") && -1 != agent.indexOf("rv:11")) || (-1 != agent.indexOf("Edge"))  ) ) {
						codedfilename = URLEncoder.encode(file.getFilename(), "UTF8");
					} else if (null != agent && -1 != agent.indexOf("Mozilla")) {
						codedfilename = MimeUtility.encodeText(file.getFilename(), "UTF8", "B");
					} else {
						codedfilename = URLEncoder.encode(file.getFilename(), "UTF8");
					}					
					
				    externalContext.setResponseHeader("Content-Type", "application/x-download");
				    externalContext.setResponseHeader("Content-Length", file.getContent().length + "");
				    externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + codedfilename + "\"");
					externalContext.getResponseOutputStream().write(file.getContent());
					
					facesContext.responseComplete();
				
				} catch (IOException e) {
					LOGGER.error("IOException: " + e.getMessage());
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES,"general.unexpectedResult"));
					LOGGER.error(e.getMessage(), e);
				
				} catch (Exception e) {
					LOGGER.error("Exception: " + e.getMessage());
					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, IndexUIbean.getMessageResourceString(UIBEANMESSAGES, "general.exception"));
				} 
			}
			
		}
	
	/** Премахва избрания файл
	 * 
	 * @param file
	 */
	public void deleteFile(Files file){
		
		try {
			
			JPA.getUtil().runInTransaction(() -> new FilesDAO(getUserData()).deleteFileObject(file));
			
			getFilesList().remove(file);	
		
		} catch (BaseException e) {			
			LOGGER.error("Грешка при изтриване на файл към документ! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());	
		}
	}	
	
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getObjDocsList() {
		return (List<Object[]>) getStateHelper().eval(PropertyKeys.OBJDOCSLIST, null);
	}

	public void setObjDocsList(List<Object[]> objDocsList) {
		getStateHelper().put(PropertyKeys.OBJDOCSLIST, objDocsList);
	}

	public ObjectDocs getObjDoc() {
		return (ObjectDocs) getStateHelper().eval(PropertyKeys.OBJDOC, null);
	}

	public void setObjDoc(ObjectDocs objDoc) {
		getStateHelper().put(PropertyKeys.OBJDOC, objDoc);
	}
	
	public ObjectDocsLang getObjDocLang() {
		return (ObjectDocsLang) getStateHelper().eval(PropertyKeys.OBJDOCLANG, null);
	}

	public void setObjDocLang(ObjectDocsLang objDocLang) {
		getStateHelper().put(PropertyKeys.OBJDOCLANG, objDocLang);
	}
	
	public Integer getLang() {
		return (Integer) getStateHelper().eval(PropertyKeys.LANG, null);
	}

	public void setLang(Integer lang) {
		getStateHelper().put(PropertyKeys.LANG, lang);
	}
	
	@SuppressWarnings("unchecked")
	public List<Files> getFilesList() {
		return (List<Files>) getStateHelper().eval(PropertyKeys.FILESLIST, null);
	}

	public void setFilesList(List<Files> objDocsList) {
		getStateHelper().put(PropertyKeys.FILESLIST, objDocsList);
	}
	
	public boolean isShowMe() {
		return (Boolean) getStateHelper().eval(PropertyKeys.SHOWME, false);
	}
	
	public void setShowMe(boolean showMe) {
		getStateHelper().put(PropertyKeys.SHOWME, showMe);
	}
	
	public boolean isForPubl() {
		return (Boolean) getStateHelper().eval(PropertyKeys.FORPUBL, false);
	}

	public void setForPubl(boolean forPubl) {
		getStateHelper().put(PropertyKeys.FORPUBL, forPubl);
	}
	
	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	
	
	/** @return the userData */
	private UserData getUserData() {
		if (this.userData == null) {
			this.userData = (UserData) JSFUtils.getManagedBean("userData");
		}
		return this.userData;
	}
	
	/** @return the dateClassif */
	private Date getDateClassif() {
		if (this.dateClassif == null) {
			this.dateClassif = (Date) getAttributes().get("dateClassif");
			if (this.dateClassif == null) {
				this.dateClassif = new Date();
			}
		}
		return this.dateClassif;
	}
	
	/** @return */
	public Date getCurrentDate() {
		return getDateClassif();
	}

	public Integer getIdObject() {
		return idObject;
	}

	public void setIdObject(Integer idObject) {
		this.idObject = idObject;
	}

	public Integer getCodeObject() {
		return codeObject;
	}

	public void setCodeObject(Integer codeObject) {
		this.codeObject = codeObject;
	}
	
}
