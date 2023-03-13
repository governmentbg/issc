package com.ib.nsiclassif.beansSite;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dao.ObjectDocsDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.ObjectDocs;
import com.ib.nsiclassif.db.dto.ObjectDocsLang;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.exceptions.BaseException;
import com.ib.system.utils.SearchUtils;

@Named
@ViewScoped
public class DocumentsListView extends IndexUIbean implements Serializable {

	/**
	 * Разглеждане на документи към класификация или версия
	 * 
	 */
	private static final long serialVersionUID = 796444809374159271L;
	static final Logger LOGGER = LoggerFactory.getLogger(DocumentsListView.class);

	private transient ObjectDocsDAO dao;
	private List<Object[]> docsList;
	private Map<Integer, List<Files>> filesListInDoc; // key=ИД на документ, value=списък с файлове

	private Integer lang;
	private Integer idObject;
	private Integer codeObject;
	
	private String panelInfo;
	private ObjectDocs objDoc;
	private ObjectDocsLang objDocLang;
	
	@PostConstruct
	public void initData() {

		LOGGER.debug("DocumentsListView - initData!!!");

		this.dao = new ObjectDocsDAO(ActiveUser.DEFAULT);
		this.docsList = new ArrayList<Object[]>();
		this.filesListInDoc = new LinkedHashMap<>();

		try {

			if (JSFUtils.getRequestParameter("idObj") != null && !"".equals(JSFUtils.getRequestParameter("idObj"))) {
				this.idObject = Integer.valueOf(JSFUtils.getRequestParameter("idObj"));

				if (JSFUtils.getRequestParameter("codeObj") != null && !"".equals(JSFUtils.getRequestParameter("codeObj"))) {
					this.codeObject = Integer.valueOf(JSFUtils.getRequestParameter("codeObj"));
				}

				this.lang = NSIConstants.CODE_DEFAULT_LANG;
				
				if (JSFUtils.getRequestParameter("lang") != null && !"".equals(JSFUtils.getRequestParameter("lang"))) {
					
					try { 
						
						this.lang = Integer.valueOf(JSFUtils.getRequestParameter("lang"));	
						if(this.lang < 0) { 
							this.lang = NSIConstants.CODE_DEFAULT_LANG;
						}
					
					} catch (Exception e) {
						this.lang = NSIConstants.CODE_DEFAULT_LANG;
					}									
				} 

				if (this.idObject != null && this.codeObject != null) {

					JPA.getUtil().runWithClose(() -> this.docsList = this.dao.findObjectDocsNative(this.codeObject, this.idObject, this.lang));
					
					List<Object[]> tmpDocsList = new ArrayList<Object[]>();						

					if (this.docsList != null && !this.docsList.isEmpty()) {

						List<Files> filesList = new ArrayList<>();

						for (Object[] doc : this.docsList) {
							
							Integer publ = SearchUtils.asInteger(doc[4]);
							
							if (publ != null && publ == Integer.valueOf(NSIConstants.CODE_ZNACHENIE_DA)) {
								
								Integer idDoc = SearchUtils.asInteger(doc[0]);

								filesList = new FilesDAO(ActiveUser.DEFAULT).selectByFileObject(idDoc,  NSIConstants.CODE_ZNACHENIE_JOURNAL_OBJECT_DOC);

								this.filesListInDoc.put(idDoc, filesList);
								
								tmpDocsList.add(doc);
							}							
						}
						
						this.docsList = tmpDocsList;
					}
					
					//попълване на информативният панел
					if(codeObject.intValue() == NSIConstants.CODE_ZNACHENIE_JOURNAL_STAT_VERSION) {
						JPA.getUtil().runWithClose(() -> this.panelInfo =  new VersionDAO(ActiveUser.DEFAULT).decodeVersionIdentName(this.idObject, this.lang));
					} else {
						//TODO 
						JPA.getUtil().runWithClose(() -> this.panelInfo =  new ClassificationDAO(ActiveUser.DEFAULT).decodeClassifIdent(this.idObject, this.lang));
					}
				}
			}

		} catch (NumberFormatException e) {
			LOGGER.error("Подаден е невалиден параметър! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_WARN, getMessageResourceString(beanMessages, "general.param"));
			
		} catch (Exception e) {
			LOGGER.error("Грешка при зареждане на списъка с документи!", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, ERRDATABASEMSG), e.getMessage());
		}

	}

	public void actionViewDocData(Integer idDoc) {
		
		try {

			if (idDoc != null) {

				JPA.getUtil().runWithClose(() -> this.objDoc = new ObjectDocsDAO(getUserData()).findById(idDoc) );
				
				if (this.objDoc != null) {
					
					if (this.objDoc.getLangMap().containsKey(getLang())) {
						setObjDocLang(getObjDoc().getLangMap().get(getLang()));
					}					
				}	
			}
			
			String dialogWidgetVar = "PF('modalDocData').show();";
			PrimeFaces.current().executeScript(dialogWidgetVar);

		} catch (BaseException e) {
			LOGGER.error("Грешка при зареждане данните на документа! ", e);
			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
		}	
		
	}

	/**
	 * Download selected file
	 *
	 * @param files
	 */
	public void download(Files files) {

		try {

			if (files.getId() != null) {

				FilesDAO dao = new FilesDAO(ActiveUser.DEFAULT);

				try {

					files = dao.findById(files.getId());

				} finally {
					JPA.getUtil().closeConnection();
				}

				if (files.getContent() == null) {
					files.setContent(new byte[0]);
				}
			}

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();

			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			String agent = request.getHeader("user-agent");

			String codedfilename = "";

			if (null != agent && (-1 != agent.indexOf("MSIE") 
					|| -1 != agent.indexOf("Mozilla") && -1 != agent.indexOf("rv:11") || -1 != agent.indexOf("Edge"))) {
				codedfilename = URLEncoder.encode(files.getFilename(), "UTF8");
			} else if (null != agent && -1 != agent.indexOf("Mozilla")) {
				codedfilename = MimeUtility.encodeText(files.getFilename(), "UTF8", "B");
			} else {
				codedfilename = URLEncoder.encode(files.getFilename(), "UTF8");
			}

			externalContext.setResponseHeader("Content-Type", "application/x-download");
			externalContext.setResponseHeader("Content-Length", files.getContent().length + "");
			externalContext.setResponseHeader("Content-Disposition", "attachment;filename=\"" + codedfilename + "\"");
			externalContext.getResponseOutputStream().write(files.getContent());

			facesContext.responseComplete();

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public List<Object[]> getDocsList() {
		return docsList;
	}

	public void setDocsList(List<Object[]> docsList) {
		this.docsList = docsList;
	}

	public Map<Integer, List<Files>> getFilesListInDoc() {
		return filesListInDoc;
	}

	public void setFilesListInDoc(Map<Integer, List<Files>> filesListInDoc) {
		this.filesListInDoc = filesListInDoc;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
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

	public String getPanelInfo() {
		return panelInfo;
	}

	public void setPanelInfo(String panelInfo) {
		this.panelInfo = panelInfo;
	}

	public ObjectDocs getObjDoc() {
		return objDoc;
	}

	public void setObjDoc(ObjectDocs objDoc) {
		this.objDoc = objDoc;
	}

	public ObjectDocsLang getObjDocLang() {
		return objDocLang;
	}

	public void setObjDocLang(ObjectDocsLang objDocLang) {
		this.objDocLang = objDocLang;
	}

}
