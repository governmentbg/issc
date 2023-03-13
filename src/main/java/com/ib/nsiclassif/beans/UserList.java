package com.ib.nsiclassif.beans;

import static com.ib.system.utils.SearchUtils.asInteger;
import static com.ib.system.utils.SearchUtils.asString;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.component.export.PDFOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.customexporter.CustomExpPreProcess;
import com.ib.indexui.db.dao.AdmGroupDAO;
import com.ib.indexui.pagination.LazyDataModelSQL2Array;
import com.ib.indexui.search.UserSearch;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.search.ExtendedUserSearch;
import com.ib.system.db.JPA;
import com.ib.system.utils.SearchUtils;

/**
*
* Автор на общосистемния вариант в  IndexUix  Г. Белев, адаптиран  за НСИ от Стоян
*/
@ViewScoped
@Named(value = "userList")
public class UserList extends IndexUIbean {

	/**  */
	private static final long serialVersionUID = -7303782532826324153L;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserList.class);

	private List<SelectItem>	groupsItemList;
	private List<SelectItem>	statusItemList;
	private List<SelectItem>	typeItemList;

	// private UserSearch params;
	private ExtendedUserSearch  params;

	private LazyDataModelSQL2Array resultList;

	private Date currentDate;

	// долните полета са за разширение на функционалността в конкретна система. принципа е че наследника override-ва get-ера и
	// променя поведението при необходимост.
	/**
	 * <c:if test="${userList.extendedArgs}"> <ui:include src="/include/extendedUserListArgs.xhtml" /> </c:if>
	 */
	private boolean	extendedArgs = false;	// има ли допълнителни полета по които да се търси
	/**
	 * <c:if test="${userList.extendedCols}"> <ui:include src="/include/extendedUserListCols.xhtml" /> </c:if>
	 */
	private boolean	extendedCols = false;	// има ли допълнителни колони в резултата

	private boolean	renderArgGroup	= true;	// показва ли се търсене по групи
	private boolean	renderArgType	= true;	// показва ли се търсене тип потребител - Тип (роля) за потребител
	private boolean    renderArgBlock = true;  // Показва ли се изискване за търсене само на блокирани потребители 

	private boolean	renderColType		= true;	// показва ли се колоната в резултата за тип на потребител
	private boolean	renderColDateReg	= true;	// показва ли се колоната в резултата за дата на регистрация

	private boolean blockPotreb = false;
	
	/** */
	@PostConstruct
	protected void initData() {
		LOGGER.debug("!!! PostConstruct !!!");

		this.params = createSearchObject();

		this.currentDate = new Date();

		try {
			this.statusItemList = createItemsList(false, Constants.CODE_CLASSIF_STATUS_POTREB, this.currentDate, null);
			this.typeItemList = createItemsList(false, Constants.CODE_CLASSIF_BUSINESS_ROLE, this.currentDate, null);    // Тип (роля) за потребител - администратор, класиф. експерт, експерт
			
			if (this.statusItemList != null) {
				// Избор на значения активен/неактивен само
				List <SelectItem> tList = new ArrayList <> ();
			      for (SelectItem item : this.statusItemList) {
			    	   if (SearchUtils.asInteger(item.getValue()).intValue() == Constants.CODE_ZNACHENIE_STATUS_ACTIVE
			    			||   SearchUtils.asInteger(item.getValue()).intValue() == Constants.CODE_ZNACHENIE_STATUS_INACTIVE )
			    	      tList.add(item);
			     }
			      if (!tList.isEmpty()) {
			    	  this.statusItemList.clear();
			    	  this.statusItemList.addAll(tList);
				   }
				  
			}
			

			JPA.getUtil().runWithClose(() -> {
				List<Object[]> idents = new AdmGroupDAO(getUserData()).findGroupsIdent();

				this.groupsItemList = new ArrayList<>(idents.size());
				for (Object[] ident : idents) {
					this.groupsItemList.add(new SelectItem(asInteger(ident[0]), asString(ident[1])));
				}
			});
		} catch (Exception e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg") + " - " + e.getLocalizedMessage(), e);
              
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public void actionChangePrBlock () {
		 this.params.setBlockUser(null);
		if (this.blockPotreb) this.params.setBlockUser(Integer.valueOf(1));
		
	}
	
	/** */
	public void actionReset() {
		this.params = createSearchObject();
		this.resultList = null;
		this.blockPotreb = false;
	}

	/** */
	public void actionSearch() {
		 actionChangePrBlock ();
		
		this.params.buildQueryUserList();

		this.resultList = new LazyDataModelSQL2Array(this.params, "username");
	}

	/** @return the currentDate */
	public Date getCurrentDate() {
		return this.currentDate;
	}

	/** @return the groupsItemList */
	public List<SelectItem> getGroupsItemList() {
		return this.groupsItemList;
	}

	/** @return the params */
	public ExtendedUserSearch getParams() {
		return this.params;
	}

	/** @return the resultList */
	public LazyDataModelSQL2Array getResultList() {
		return this.resultList;
	}

	/** @return the statusItemList */
	public List<SelectItem> getStatusItemList() {
		return this.statusItemList;
	}

	/** @return the typeItemList */
	public List<SelectItem> getTypeItemList() {
		return this.typeItemList;
	}

	/**
	 * @param objectID
	 * @return
	 */
	public String goToDefinition(Integer objectID) {
		JSFUtils.flashScope().put("objectID", objectID);
		return "userEdit?faces-redirect=true";
	}

	/** @return the extendedArgs */
	public boolean isExtendedArgs() {
		return this.extendedArgs;
	}

	/** @return the extendedCols */
	public boolean isExtendedCols() {
		return this.extendedCols;
	}

	/** @return the renderArgGroup */
	public boolean isRenderArgGroup() {
		return this.renderArgGroup;
	}

	/** @return the renderArgType */
	public boolean isRenderArgType() {
		return this.renderArgType;
	}

	/** @return the renderColDateReg */
	public boolean isRenderColDateReg() {
		return this.renderColDateReg;
	}

	/** @return the renderColType */
	public boolean isRenderColType() {
		return this.renderColType;
	}

	/**
	 * @return дава инстанцията на обекта за търсене. Има необходимост да се override-не ако се иска разширение в конкретна
	 *         систама.
	 */
//	protected UserSearch createSearchObject() {
//		return new UserSearch();
//	}
	
	protected ExtendedUserSearch createSearchObject() {
		return new  ExtendedUserSearch();
	}

	
/******************************** EXPORTS **********************************/
	
	/**
	 * за експорт в excel - добавя заглавие и дата на изготвяне на справката и др. - за потребителите
	 */
	public void postProcessXLS(Object document) {
		
		String title = getMessageResourceString(UI_LABELS, "usersList.reportTitle");		  
   	new CustomExpPreProcess().postProcessXLS(document, title, null, null, null);		
    
	}

	/**
	 * за експорт в pdf - добавя заглавие и дата на изготвяне на справката - за потребителите
	 */
	public void preProcessPDF(Object document)  {
		try{
			
			String title = getMessageResourceString(UI_LABELS, "usersList.reportTitle");		
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


	public boolean isRenderArgBlock() {
		return renderArgBlock;
	}


	public void setRenderArgBlock(boolean renderArgBlock) {
		this.renderArgBlock = renderArgBlock;
	}

	public boolean isBlockPotreb() {
		return blockPotreb;
	}

	public void setBlockPotreb(boolean blockPotreb) {
		this.blockPotreb = blockPotreb;
	}
	
	
}
