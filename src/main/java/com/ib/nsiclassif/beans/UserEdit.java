package com.ib.nsiclassif.beans;

import static com.ib.indexui.system.Constants.CODE_CLASSIF_STATUS_POTREB;
import static com.ib.indexui.system.Constants.CODE_CLASSIF_TIP_POTREB;
import static com.ib.indexui.system.Constants.CODE_ZNACHENIE_STATUS_ACTIVE;
import static com.ib.system.SysConstants.CODE_CLASSIF_LANG;
import static com.ib.system.SysConstants.CODE_CLASSIF_USERS;
import static com.ib.system.SysConstants.CODE_DEFAULT_LANG;
import static com.ib.system.utils.SearchUtils.asInteger;
import static com.ib.system.utils.SearchUtils.asString;
import static com.ib.system.utils.SearchUtils.isEmpty;
import static com.ib.system.utils.SearchUtils.trimToNULL;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.directory.DirContext;

import org.primefaces.PrimeFaces;
import org.primefaces.component.tree.Tree;
import org.primefaces.event.AbstractAjaxBehaviorEvent;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.ldap.ActiveDirectory;
import com.ib.nsiclassif.db.dao.AdmGroupDAO; 
import com.ib.nsiclassif.db.dao.AdmUserDAO;
import com.ib.nsiclassif.db.dto.AdmGroup;
import com.ib.nsiclassif.db.dto.AdmUser;
  
import com.ib.nsiclassif.db.dto.AdmUserRole;
import com.ib.indexui.navigation.Navigation;
import com.ib.indexui.navigation.NavigationData;
import com.ib.indexui.navigation.NavigationDataHolder;
import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.indexui.utils.JSFUtils;
import com.ib.indexui.utils.TreeUtils;
import com.ib.system.db.JPA;
import com.ib.system.db.dao.FilesDAO;
import com.ib.system.db.dto.Files;
import com.ib.system.db.dto.SystemClassif;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.utils.PasswordUtils;
import com.ib.system.utils.SearchUtils;
import com.ib.system.utils.ValidationUtils;
import com.ib.system.utils.X;

/**
 *  Автор на общосистемния вариант в  IndexUix  Г.Белев,, изменения и допълнения за НСИ  Стоян 
 */

@ViewScoped
@Named(value = "userEdit")
public class UserEdit extends IndexUIbean {

	/**  */
	private static final long serialVersionUID = -3475898578038821141L;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserEdit.class);

//	/**  */
//	protected static final String	DIV_CLASS_P_COL_4	= "p-col-4";

	private AdmUser user;
	private AdmUserDAO dao;

	private List<SelectItem>	groupsItemList;
	private List<SelectItem>	statusItemList;
	private List<SelectItem>	typeItemList;
//	private List<SelectItem>	langItemList;

	private Date currentDate;

	private Integer	dbStatus;	// статуса при изтегляне на потребител от БД
	private String	dbUsername;	// потребителското име при изтегляне на потребител от БД

	private String[] selectedGroupsIDs;

	private List<SelectItem> classifList;    // Списък с класификации за достъпи 

	private TreeNode						rootNode;
	private TreeNode[]					selectedNodes;
	private Map<Integer, List<TreeNode>>	allroles;         // Всички роли за потребител
	
	private HashMap<Integer, Boolean> hAdmStr = new HashMap<> ();    // Кодове от адм. структура
		
//	private List<Files> filesList; // прикачените файлове

	/** взимат се от системна настройка system.usernameRegex */
	private String	usernameRegexInfo;
	/** взимат се от системна настройка system.passwordRegex */
	private String	passwordRegexInfo;

	/**
	 * класификации за контрол на достъпа, за които ако нищо не е избрано се дава пълен достъп
	 * ("system.classificationsNotFilteredIfNotGranted")
	 */
	private Set<Integer> freeAccessList;	
	
	private Integer typePanelData; // използва се за вида на панела, който се отваря с данни на екрана

	/**
	 * класификации за Опции от менюто, които са забранени за избор при даване на достъп - списък за подаване на дървото за readOnlyCodes
	 * ("system.menuForbiddenItems")
	 *   Не се използува
	 */
	private List<Integer> forbiddenAccessList;
	private boolean changePass=false;
	
	private boolean useLdap=false;
	private boolean prOpenAll = false;    // Дали да се показват всички полета
	private boolean prAktDirY = false;               // Дали е открито userName в активната. директория
	private boolean prAktDirNamesY = false;   // Дали има върната стойност за namesl от акт. директория 
	private boolean prAktDirEmailY = false;     // Дали има върната стойност за email от акт. директория
     private String textErr = null;
     private boolean blockPotreb = false;         // Проверка за блокиран потребител 
     private boolean isPotrebZXC = false;           //  Дали потребителят е от типа ZXC- работен потребител - при актуализация само се тества
      private String passPlain = null, passRepeat = null;   // Полета, за въвеждане на новата парола - само при актуализаця на потребители от типа ZXC 
	private String nameClassifIndDostap = null;
      
	/** */
	@PostConstruct
	protected void initData() {
		LOGGER.debug("!!! PostConstruct !!!");

		this.currentDate = new Date();

		this.allroles = new HashMap<>();

		this.dao = new AdmUserDAO(getUserData());

		try {
			this.statusItemList = createItemsList(false, Constants.CODE_CLASSIF_STATUS_POTREB, this.currentDate, null);
			this.typeItemList = createItemsList(false, Constants.CODE_CLASSIF_BUSINESS_ROLE, this.currentDate, null);   // Тип (роля) за потребител - администратор, класиф. експерт, експерт
//			this.langItemList = createItemsList(false, Constants.CODE_CLASSIF_LANG, this.currentDate, null);
			          
			
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
			
			
			// HashMap с кодове от административната структура
//					List<SystemClassif> list = getSystemData().getSysClassification(Constants.CODE_CLASSIF_ADMIN_STR, this.currentDate, getCurrentLang());
//			        if (list != null && !list.isEmpty())  
//					  for (SystemClassif item :list )  hAdmStr.put(item.getCode(),Boolean.TRUE);  
			

			String passwordRegex = getSystemData().getSettingsValue("system.passwordRegex");
			if (passwordRegex != null) {
				this.passwordRegexInfo = getSystemData().getSettingsDopInfo("system.passwordRegex");
			}
			String usernameRegex = getSystemData().getSettingsValue("system.usernameRegex");
			if (usernameRegex != null) {
				this.usernameRegexInfo = getSystemData().getSettingsDopInfo("system.usernameRegex");
			}

			Integer objectID = (Integer) JSFUtils.flashScope().get("objectID");   // id на избан в userList   потребител за актуализация

			JPA.getUtil().runWithClose(() -> {
				if (objectID == null) {
					newUser(0);

				} else { // ще се преминава в редакция
					this.user = this.dao.findById(objectID);

					this.dbStatus = this.user.getStatus();
					this.dbUsername = this.user.getUsername();

					int grSize = this.user.getUserGroups().size();
					if (grSize > 0) { // трябва да му се заредят и групите
						this.selectedGroupsIDs = new String[grSize];
						for (int i = 0; i < grSize; i++) {
							this.selectedGroupsIDs[i] = String.valueOf(this.user.getUserGroups().get(i).getId());
						}
					}
					this.user.getUserRoles().size(); // защото е LAZY
				}
				this.groupsItemList = new AdmGroupDAO(getUserData()).findGroupsIdent().stream().map(x -> new SelectItem(asInteger(x[0]), asString(x[1]))).collect(toList());
//				this.filesList = new FilesDAO(getUserData()).selectByFileObject(this.user.getId(), Constants.CODE_ZNACHENIE_JOURNAL_USER);
			});
            
			// Класификации, с които се формират достъпи
			String accessControlList = getSystemData().getSettingsValue("system.classificationsForAccessControl");   //   Само достъп до менюто класификация 7
			if (accessControlList == null)
			        accessControlList = String.valueOf(Constants.CODE_CLASSIF_MENU);;     // Достъп само до менюто чрез класификацията за меню
			        
			if (accessControlList != null) { // може и да няма такъв параметър
				this.classifList = new ArrayList<>();

				String[] classifIDs = accessControlList.split(",");
				for (String id : classifIDs) {
					Integer codeClassif = Integer.valueOf(id);

					this.classifList.add(new SelectItem(codeClassif, getSystemData().getNameClassification(codeClassif, getCurrentLang())));
				}
			}
			   
			//  За меню - ако не е избрано нищо от класификацията, се дава пълен достъп до менюто
//			String notFilteredIfNotGranted = getSystemData().getSettingsValue("system.classificationsNotFilteredIfNotGranted");
//			String notFilteredIfNotGranted = String.valueOf(Constants.CODE_CLASSIF_MENU);
			String notFilteredIfNotGranted = null;    // Нямя да има класификация, за която ако не се избере нищо, ще има достъп до всички позиции от нея
			this.freeAccessList = new HashSet<>();
			
//			if (notFilteredIfNotGranted != null) {
//				this.freeAccessList = new HashSet<>();
//
//				String[] classifIDs = notFilteredIfNotGranted.split(",");
//				for (String id : classifIDs) {
//					this.freeAccessList.add(Integer.valueOf(id));
//				}
//			} 

	
//			String menuForbiddenItems = getSystemData().getSettingsValue("system.menuForbiddenItems");
//			if (menuForbiddenItems != null) {
//				this.forbiddenAccessList = new ArrayList<>();
//
//				String[] classifIDs = menuForbiddenItems.split(",");
//				for (String id : classifIDs) {
//					this.forbiddenAccessList.add(Integer.valueOf(id));
//				}
//			}
			
//			if (getSystemData().getSettingsValue("useLdap").equals("true")) {
//				useLdap=true;
//			}else {
				useLdap = false;
				this.prOpenAll = true;
				this.prAktDirY = true;
				this.prAktDirNamesY = false;
				this.prAktDirEmailY = false;
//			}
//			if (this.user.getId() != null) {  // Актуализация
//				
//				if (this.user.getUsername().trim().toUpperCase().compareTo("ZXC") == 0
//					|| this.user.getUsername().trim().toUpperCase().compareTo("ZXC1") == 0	
//					|| this.user.getUsername().trim().toUpperCase().compareTo("ZXC2") == 0
//				    ||  this.user.getId().intValue() < 0 )   { 
//					   this.isPotrebZXC = true;
//					   this.prOpenAll = true;
//					   this.prAktDirY = true;
//					   this.prAktDirNamesY = false;
//					   this.prAktDirEmailY = false;
//				}  else {
//					if (getSystemData().getSettingsValue("useLdap").equals("true")) {
						// Връзка с активната директория    
//						 checkAktDirForUsernameAct ();         // Ако се е получила грешка, на екрана ще излезе само съобщение
//					}else {
//						this.prOpenAll = false;
//					   this.prAktDirY = true;
//					   this.prAktDirNamesY = true;
//					   this.prAktDirEmailY = false;
//					}
					
//				}		 
//	    	 }  
					
			  
	
			this.typePanelData = 1;   // Първоначално се изобразява панел с основни данни
				
		} catch (Exception e) {
		//	JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg") + " - " + e.getLocalizedMessage(), e);
			textErr = "Грешка при опит за четене от базата ! - " + e.getLocalizedMessage();
			LOGGER.error(e.getMessage(), e);
			return;
		}
		if (this.classifList != null && this.classifList.size() == 1 ) {  
			this.nameClassifIndDostap = this.classifList.get(0).getLabel();
			actionLoadTree() ;
		}	
	}
	
	public void clearPrizn () {
		this.prOpenAll = false;                    // Дали да се показват всички полета
		this.prAktDirY = false;                     // Дали е открито userName в активната. директория - при false - не се дава възможност за актуализация, а данните само се показват при актуализация
		this.prAktDirNamesY =false;           // Дали има върната стойност за namesl от акт. директория 
		this.prAktDirEmailY = false;             // Дали има върната стойност за email от акт. директория
	
	}
	
//	/**
//	 * Проверка за username при регистрация на нов потребител
//	 */
//	public void checkAktDirForUsername ()  {
//		try {
//			if (getSystemData().getSettingsValue("useLdap").equals("true")) {
//				
//			
//				// В this.user.username се намира въведено usernameq за което се прави тестване
//				this.user.setUsername(trimToNULL(this.user.getUsername()));
//				if (this.user.getUsername() == null) {
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "users.usernameEmpty"));
//					return;
//				}
//				
//				if (chectForSpace(this.user.getUsername())) {
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.usernameNoSpace"));
//					return;
//					
//				}
//				
//
//				
//				AdmUser us = null;
//				try {
//					us = readInfoFromAktDirForUsername (this.user.getUsername(),this.user.getPassword());
//				} catch (DbErrorException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, "Грешка при опит за свързване с активната директория - "  + e.getLocalizedMessage());
//					this.prOpenAll = false;
//					return;
//				}
//				
//				if (us == null ) {
//					// Не е намерена информация за това username в акт. директория
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.noPotrenInAktDir"));
//					this.prOpenAll = false;
//					return;
//				}
//				
//				/*******************************************************************************************************************************************************************************************/
//			           
//				if (this.user.getUsername().compareToIgnoreCase("zxc") == 0 || this.user.getUsername().compareToIgnoreCase("zxc1") == 0 || this.user.getUsername().compareToIgnoreCase("zxc2") == 0 )  {
//					JSFUtils.addErrorMessage( getMessageResourceString(beanMessages, "userEdit.noNameZXC"));
//					return;
//				}
//				    // Тест - проверка за въведено username - при нормално изпълнение това не се прави - проверката е чрез активната директория
//				 /*  
//								try {    
//						//	Минимална дължина на потребителското име е 3 символа. Не се допускат празни интервали. Позволени са главни и малки букви на латиница, цифри и символите @._-
//								String regex = getSystemData().getSettingsValue("system.usernameRegex");
//									if (regex != null) { // само ако има нещо, защото ако няма се смята че няма да се налага валидация
//										boolean valid = this.user.getUsername().matches(regex);
//										if (!valid) {
//											String dopInfo = getSystemData().getSettingsDopInfo("system.usernameRegex");
//						
//											StringBuilder error = new StringBuilder(getMessageResourceString(UI_beanMessages, "users.usernameNotValid"));
//											if (dopInfo != null) {
//												error.append(" ");
//												error.append(dopInfo);
//											}
//											JSFUtils.addMessage("formUserEdit:username0", FacesMessage.SEVERITY_ERROR, error.toString());
//											return ;
//										}
//									}
//						
//							} catch (DbErrorException e) {
//								LOGGER.error("Грешка при проверка на потребителско име за валидност", e);
//								JSFUtils.addMessage("formUserEdit:username0", FacesMessage.SEVERITY_ERROR,"Грешка при проверка на потребителско име за валидност" + " - " + e.getLocalizedMessage());
//								return ;
//							}
//
//			   */
//				/********************************************************************************************************************************************************************************************/
//				
//				if (us.getStatus() != null && us.getStatus().intValue() == Constants.CODE_ZNACHENIE_STATUS_INACTIVE) {
//					// Намерен е неактивен потребител
//					JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.potrebInAktDirNeAkt"));
//					return;
//					
//				}
//				
//				//  Намерен е потребител в акт. директория
//				//*********************************************************************************************************************************************************************************************
//				// Проверка дали вече има регисриран потребител в НСИ с това username
//				try { // проверка дали има вече въведено същото потребителско име
//					X<Boolean> used = X.empty();
//					JPA.getUtil().runWithClose(() -> used.set(this.dao.checkUsernameExist(this.user.getUsername(), null)));
//
//					if (used.isPresent() && used.get().booleanValue()) {
//						JSFUtils.addErrorMessage( getMessageResourceString(UI_beanMessages, "users.usernameExist"));
//						return;
//					}
//
//				} catch (BaseException e) {
//					JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg") + " - при проверка за наличие на вече регистриран потребител с това ротр. име", e);
//
//					LOGGER.error(e.getMessage(), e);
//					return;
//				}
//				//************************************************************************************************************************************************************************************************
//			
//				this.prOpenAll = true;                    //  Ще се  показват всички полета
//				this.prAktDirY = true;                     // Дали е открито userName в активната. директория
//				
//				
//				if (us.getNames() != null) {     // Има прочетени имена от акт. директория
//					this.prAktDirNamesY = true;
//					this.user.setNames(us.getNames());
//				} else {
//					this.prAktDirNamesY = false;
//					this.user.setNames(us.getNames());    // null
//				}
//				
//				if (us.getEmail() != null) {     // Има прочетен email от активната директория
//					if (!ValidationUtils.isEmailValid(us.getEmail())) {      // Не е правилен формат за email
//						this.prAktDirEmailY = false;
//					}  else {
//						this.prAktDirEmailY = true;
//					}	
//					this.user.setEmail(us.getEmail());
//				} else {
//					this.prAktDirEmailY = false;
//					this.user.setEmail(us.getEmail());   // null
//				}
//			
//			}
//		} catch (DbErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//*********************************************************************************************************************************************************************************************************
		//  Предоставят се за актуализация
//		this.prAktDirNamesY = false;
//		this.user.setNames(us.getNames());    
//		this.prAktDirEmailY = false;
//		this.user.setEmail(us.getEmail());

//		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_INFO, "Въведеното потребителско име е минало през проверка в активната директория (засега тази проверка е фиктивна) и данните се предоставят за актуализация! Не се въвежда парола, тъй като проверката за парола се прави чрез активната директория при опит за влизане в системата (за сега и тази проверка е фиктивна)! Може да се въвежда всяко потребителско име без ZXC, ZXC1 и ZXC2!");

 //*********************************************************************************************************************************************************************************************************
//	}
	
//	/**
//	 * Проверка за username при актуализация на  потребител
//	 */
//	public void checkAktDirForUsernameAct () {
//		
//		// В this.user.username се намира въведено usernameq за което се прави тестване
//		this.user.setUsername(trimToNULL(this.user.getUsername()));
//		if (this.user.getUsername() == null) {
//	//		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "users.namesEmpty"));
////			textErr = "Не е въведено потребителско име!"; 
//			textErr = "Потребителят е бил въведен извън системата без потребителско име - недопустимо!";
//	
//			return;
//		}
//		
//		try {
//			if (getSystemData().getSettingsValue("useLdap").equals("true")) {
//				
//				AdmUser us;
//				try {
//					us = readInfoFromAktDirForUsername (this.user.getUsername(), this.user.getPassword());
//				
//				} catch (DbErrorException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					textErr = "Грешка при опит за свързване с активна директория за проверка ! - " + e.getLocalizedMessage();
//					return;
//				}
//				
//				if (us == null ) {
//					// Не е намерена информация за това username в акт. директория
//					if (this.user.getStatus() != null && this.user.getStatus().intValue() == Constants.CODE_ZNACHENIE_STATUS_INACTIVE) {
//						// Намерен е неактивен потребител
////				JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "users.potrebInAktDirNeAkt"));
////				textErr = getMessageResourceString(beanMessages, "userEdit.potrebInAktDirNeAkt")+ " - потребителят трябва да остане записан  като неактивен задължително!";
//						textErr = getMessageResourceString(beanMessages, "userEdit.noPotrenInAktDir")+ " - потребителят трябва да остане записан  като неактивен задължително!";
//						this.prOpenAll = true;
//						this.prAktDirY = false;
//						this.user.setStatus(Integer.valueOf(Constants.CODE_ZNACHENIE_STATUS_INACTIVE));
//						return;
//						   	
//					}  else {
//					
//					//		JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "users.noPotrenInAktDir"));
//							textErr = getMessageResourceString(beanMessages, "userEdit.noPotrenInAktDir") + " - потребителят трябва да се запише  като неактивен задължително!";
//							this.prOpenAll = true;
//							this.prAktDirY = false;
//							this.user.setStatus(Integer.valueOf(Constants.CODE_ZNACHENIE_STATUS_INACTIVE));
//							return;
//					}		
//				}
//				
//				// Намерен е потребител по username в активната директория
//				if (us.getStatus() != null && us.getStatus().intValue() == Constants.CODE_ZNACHENIE_STATUS_INACTIVE) {
//					// Намерен е неактивен потребител
////			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "users.potrebInAktDirNeAkt"));
//					textErr = getMessageResourceString(beanMessages, "userEdit.potrebInAktDirNeAkt")+ " - потребителят трябва да се запише  като неактивен задължително!";
//					this.prOpenAll = true;
//					this.prAktDirY = false;
//					this.user.setStatus(Integer.valueOf(Constants.CODE_ZNACHENIE_STATUS_INACTIVE));
//					return;
//						
//				}
//				
//				//  Намерен е потребител в акт. директория
//				this.prOpenAll = true;                    // Дали да се показват всички полета
//				this.prAktDirY = true;                     // Дали е открито userName в активната. директория
//				
//				
//				String s = ""; 
//				if (us.getNames() != null) {     // Има прочетени имена от акт. директория
//					// Проверка за промяна на имената
//					if (us.getNames().trim().isEmpty()) {
//						if (this.user.getNames() != null)
//							  this.prAktDirNamesY = true;
//						else   this.prAktDirNamesY = false;   // Възможност за актуализация на имена
//					} else {
//						// Сравнение за имената
//						if (this.user.getNames().compareTo(us.getNames().trim()) != 0) {
//							// Има промяна на имената
//							s = "В активната директория има промяна в имената";
//							this.prAktDirNamesY = true;
//							this.user.setNames(us.getNames());
//						} else {
//							this.prAktDirNamesY = true;
//						}
//					}
//				
//				} else {
////			if (this.user.getNames() != null)
////			  this.prAktDirNamesY = true;
////			else  
//						this.prAktDirNamesY = false;   // Възможност за актуализация на имена, ако в активната директория липсват имена
//					
//				}
//				
//				if (us.getEmail() != null) {     // Има прочетен email от активната директория
//					if (ValidationUtils.isEmailValid(us.getEmail())) {      // Правилен формат за email
//						// Сравнение за email
//						if (this.user.getEmail().compareTo(us.getEmail().trim()) != 0) {
//							// Има промяна на email
//							if (!s.isEmpty())
//								s += " и на email ";
//							else
//							   s = "В активната директория има промяна на email ";
//							this.prAktDirEmailY = true;   // Не се актуализира - остава новия e-mail със задължително записване
//						    this.user.setEmail(us.getEmail());
//						} else    this.prAktDirEmailY = true;    // Не се актуализира остава стария email
//					} else {
//						this.prAktDirEmailY = true;    // Остава записаният преди email
//					}	
//								
//				} else {
//				   	this.prAktDirEmailY = false;  // Дава се възможност за актуализация на стария email, ако в акт.директория липсва email
//				}
//				
//				if (!s.isEmpty()) {
//					s += " - потребителят трябва да се актуализира задължително!";
//					this.textErr = s;   // Съобщение за задължителна актуализация
//				}
//			
//			}
//		} catch (DbErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
////		this.prAktDirNamesY = false;
////		this.prAktDirEmailY = false;
//		  
//	}
	
//	/**
//	 *  Търсене на информация  за потребител по username в активната директория
//	 * @param userName
//	 * @return
//	 * @throws DbErrorException
//	 */
//	public AdmUser readInfoFromAktDirForUsername (String userName,String password)  throws DbErrorException {
//		AdmUser us = new AdmUser ();
//		// Проверка в активна директория за username
//		
//		ActiveDirectory aDir = new ActiveDirectory ();
//
//		
//		DirContext context=null;
//		try {
//			context=aDir.createContext(userName, password);
//		} catch (DbErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			throw new DbErrorException (e.getLocalizedMessage());
//			
//		}
//		//ako imame context значи усер/пасс са ок.
//		if (context!=null) {
//			String searchBy = "sAMAccountName"; // "sAMAccountName", "givenName", "cn", "mail"
//			
//        	try {
//				us = aDir.searchUserInAktDir (context,userName, password,  searchBy);
//			} catch (DbErrorException e) { 
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				throw new DbErrorException (e.getLocalizedMessage());
//				
//			}
//		}
//		
//	        	
//		return us;
//	}
	
    public void actionChangeStatus() {
		
//		if (user.getStatus().equals(Constants.CODE_ZNACHENIE_STATUS_ACTIVE)) {
//			user.setStatusExplain(null);			
//		}		
	 }
    
    /**
     * Проверка дали записът за  въведен потребител е блокиран
     * @return
     */
    public boolean isUserBlockYes () {
    	this.blockPotreb = false;
    	if (this.user != null && this.user.getId() != null) {
    		if (this.user.getUserBlock() != null && this.user.getUserBlock().intValue() == 1 ) {
    			this.blockPotreb = true;
    		}	
    	}
    	    	
    	return this.blockPotreb;
    }
    public void actionChangeBlock () {
    	if (!this.blockPotreb) {
    		this.user.setUserBlock(null);
    		JSFUtils.addInfoMessage(getMessageResourceString(beanMessages, "userEdit.messUnBlock"));
    	}
    	
    }
    
     public void actionChangeTypePanel () {
    	 
     }
     
     public void clearPass () {
    	 this.passPlain = null;
    	 this.passRepeat = null;
     }
     
     public boolean checkPass () {
       	    	
	 		if (isEmpty(this.passPlain)) {
	 			JSFUtils.addMessage("formUserEdit:passPlain", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "login.changePassNewPassword1Empty"));
	 			return false;
	 		}
	     	
	 	if ( !isEmpty(this.passRepeat)) {	
	 		if (!Objects.equals(this.passPlain, this.passRepeat)) {
	 			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "login.changePassNew12NotSame"));
	 			return false;
	 		}
	 	}
	 	
 		return true;
     }
     
     public boolean checkPassEQ () {
//    	 if (isEmpty(this.passPlain) && isEmpty(this.passRepeat) )  return true;
    	 boolean ok = true;
    	
	 		if (isEmpty(this.passPlain)) {
	 			JSFUtils.addMessage("formUserEdit:passPlain", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "login.changePassNewPassword1Empty"));
	 			ok = false;
	 		}
	 		if (isEmpty(this.passRepeat)) {
	 			JSFUtils.addMessage("formUserEdit:passRepeat", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "login.changePassNewPassword2Empty"));
	 			ok = false;
	 		}
    	
	 	if (ok) {	
	 		if (!Objects.equals(this.passPlain, this.passRepeat)) {
	 			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "login.changePassNew12NotSame"));
	 			ok = false;
	 		}
	 	}
	 	
 		return ok;
     }
     
//     /**
// 	 * Проверка за парола
// 	 *
// 	 * @return
// 	 */
// 	public boolean checkPassword() {
// 		 if (isEmpty(this.passPlain) && isEmpty(this.passRepeat) )  return true;
// 		boolean ok = true;
//           ok = checkPassEQ ();
//        if (!ok)  return false;
//        
// 		try {    // Проверка за валидация на въведена парола за потребтел ZXC - 
// 			     // Минимална дължина на паролата е 3 символа. Не се допускат празни интервали. Позволени са главни и малки букви на латиница, цифри и символите @#$%^&+=._-
// 			String regex = getSystemData().getSettingsValue("system.passwordRegex");
// 			if (regex != null) { // само ако има нещо, защото ако няма се смята че няма да се налага валидация
// 				boolean valid = this.passPlain.matches(regex);
// 				if (!valid) {
// 					String dopInfo = getSystemData().getSettingsDopInfo("system.passwordRegex");
//
// 					StringBuilder error = new StringBuilder(getMessageResourceString(UI_beanMessages, "login.changePassNotValid"));
// 					if (dopInfo != null) {
// 						error.append(" ");
// 						error.append(dopInfo);
// 					}
// 					JSFUtils.addGlobalMessage( FacesMessage.SEVERITY_ERROR, error.toString());
// 					ok = false;
// 				}
// 			}
//
// 		} catch (DbErrorException e) {
// 			LOGGER.error("Грешка при проверка на парола за валидност", e);
// 			JSFUtils.addGlobalMessage(FacesMessage.SEVERITY_ERROR,"Грешка при проверка на парола за валидност" + " - " + e.getLocalizedMessage());
// 			ok = false;
// 		}
// 		
// 		return ok;
// 	}
	
	/** @param mode 0-от търсене, 1-от екрана за редакция */
	public void newUser(int mode) {
        
		clearPrizn (); 
		this.blockPotreb = false;
		
		String username = null;
		
		this.user = createUserObject();

		this.user.setUserRoles(new ArrayList<>());
		this.user.setStatus(Integer.valueOf(Constants.CODE_ZNACHENIE_STATUS_ACTIVE));


//		this.filesList = new ArrayList<>();

//		if (mode == 1) {

			this.dbStatus = null;
			this.dbUsername = null;

			this.selectedGroupsIDs = null;
			this.rootNode = null;
			this.selectedNodes = null;
			this.allroles = new HashMap<>();
//		}
			
		if (this.classifList != null && this.classifList.size() == 1 ) {  
			this.nameClassifIndDostap = this.classifList.get(0).getLabel();
			actionLoadTree() ;
		}	
		
	}

	/**
	 * Изтриване на потребител
	 */
	public void actionDelete() {
		if (isUserConfirmed()) {
			return; // бутона не трябв да се вижда, но все пак да се защитим
		}

		try {
			JPA.getUtil().runInTransaction(() -> {
				this.dao.deleteById(this.user.getId());

			});

			getSystemData().reloadClassif(CODE_CLASSIF_USERS, false, false);

			Navigation navHolder = new Navigation();
			int i = navHolder.getNavPath().size();
			if (i > 1) {

				NavigationDataHolder holder = (NavigationDataHolder) JSFUtils.getManagedBean("navigationSessionDataHolder");
				Stack<NavigationData> stackPath = holder.getPageList();
				NavigationData nd = stackPath.get(i - 2);
				if (nd != null) {
					Map<String, Object> mapV = nd.getViewMap();
					UserList bean = (UserList) mapV.get("userList");
					if (bean != null) {
						bean.actionSearch(); // да презареди списъка, за да се коригира броя резултати
					}
				}
				navHolder.goBack(); // връща към предходната страница - списъка на събитията
			}

		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"), e);
			LOGGER.error(e.getMessage(), e);
			PrimeFaces.current().executeScript("PF('confirmDialog').hide();");
		}
	}

	/** */
	public void actionLoadTree() {
		this.rootNode = null;
		this.selectedNodes = null;

		Integer selectedClassifCode1 = null;
	    String selectedClassifIndex	 = null;
		if (this.classifList != null && this.classifList.size() > 1.) {
		   selectedClassifCode1 = Integer.valueOf(JSFUtils.getRequestParameter("selectedClassifCode"));
		    selectedClassifIndex = JSFUtils.getRequestParameter("selectedClassifIndex");
		} else {
			if (this.classifList != null) {
			    selectedClassifCode1 =  Integer.valueOf((int)this.classifList.get(0).getValue());
			    selectedClassifIndex = "0";
			}    
		}
		Integer selectedClassifCode = selectedClassifCode1;
		
		try {
			List<Integer> rolesList = null;

			if (this.allroles.containsKey(selectedClassifCode)) { // взимам от паметта
				rolesList = this.allroles.get(selectedClassifCode).stream().map(x -> ((SystemClassif) x.getData()).getCode()).collect(toList());

			} else { // ще се взимат ролите от тези в потребителя
				rolesList = this.user.getUserRoles().stream().filter(x -> x.getCodeClassif().equals(selectedClassifCode)).map(AdmUserRole::getCodeRole).collect(toList());
			}

			List<SystemClassif> listItems = getSystemData().getSysClassification(selectedClassifCode, getCurrentDate(), getCurrentLang());
			this.rootNode = new TreeUtils().loadCheckboxTree(listItems, false, rolesList, true, this.forbiddenAccessList, null);
			
					
			PrimeFaces.current().executeScript("PrimeFaces.widgets.dostapWV.unselectAllRows()");
			PrimeFaces.current().executeScript("PrimeFaces.widgets.dostapWV.selectRow(" + selectedClassifIndex + ")");
			

		} catch (DbErrorException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"), e);

			LOGGER.error(e.getMessage(), e);

		} catch (Exception e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.exception"), e);

			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Показва че има кликане по дървото
	 *
	 * @param event
	 */
	public void actionNodeClick(AbstractAjaxBehaviorEvent event) {
		SystemClassif classif = (SystemClassif) ((Tree) event.getSource()).getRowNode().getData();

		List<TreeNode> current = this.allroles.computeIfAbsent(classif.getCodeClassif(), ArrayList::new);
		current.clear();

		current.addAll(Arrays.asList(this.selectedNodes));
	}
	
	/**
	 * @param id
	 * @return <code>true</code> ако класификацията е обяване за достъп до всичко ако не е дадено нищо
	 */
	public boolean isClassifFreeAccess(Integer id) {
		return this.freeAccessList != null && this.freeAccessList.contains(id);
	}
	
	
	/** @return <code>true</code> ако има поне една класификация */
	public boolean showFreeAccessMessage() {
		return this.freeAccessList != null && !this.freeAccessList.isEmpty();

	}

	/** @return дава нова инстанцията на обекта потребител */
	protected AdmUser createUserObject() {
		return new AdmUser();
	}

	/** @return the dao */
	protected AdmUserDAO getDao() {
		return this.dao;
	}

	/**  */
	public void actionSave() {
		boolean valid = checkFormValid();
		if (!valid) {
			return;
		}

		//**********************************************************************************************************************************************************************************************************************
		if (this.user.getId() == null && (this.user.getUsername().compareToIgnoreCase("zxc") == 0 || this.user.getUsername().compareToIgnoreCase("zxc1") == 0 || this.user.getUsername().compareToIgnoreCase("zxc2") == 0) )  {
			JSFUtils.addErrorMessage( getMessageResourceString(beanMessages, "userEdit.noNameZXC"));
			return;
		}
		//**********************************************************************************************************************************************************************************************************************
		
		boolean changeStatus = !Objects.equals(this.dbStatus, this.user.getStatus());

		if (this.user.getId() == null || changeStatus) { // нов или с различни статуси
			if (this.user.getId() == null)    	this.user.setLang(CODE_DEFAULT_LANG);
			this.user.setStatusDate(new Date());
		}

		if (changeStatus && this.user.getStatus().equals(CODE_ZNACHENIE_STATUS_ACTIVE)) { // смяна на статус към активен
//			this.user.setLoginAttempts(0); // чистят се
		}
		
		if (this.user.getId() != null && this.blockPotreb)       // Остава блокиран
			this.user.setUserBlock(Integer.valueOf(1));
		else  this.user.setUserBlock(null);

		if ((this.isPotrebZXC || !useLdap) && !isEmpty(this.passPlain)) {
			// Има въведена нова парола
			try {
				this.user.setPassword(PasswordUtils.hashPassword(this.passPlain));
			} catch (Exception e) {
				JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.exception") + " при хеширане на нова парола - " + e.getLocalizedMessage() , e);
				return;
			}
		}
		
//

		selectUserGroups();   //  участието в групите
		selectUserRoles();     //  връзката с ролите

		try { // трябва да е готово за запис

			JPA.getUtil().runInTransaction(() -> this.user = this.dao.save(this.user));

			getSystemData().reloadClassif(CODE_CLASSIF_USERS, false, false);

			JSFUtils.addInfoMessage(getMessageResourceString(UI_beanMessages, "general.succesSaveMsg"));
	
			this.dbStatus = this.user.getStatus();
			this.dbUsername = this.user.getUsername();
			
			clearPass();

		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg") + " - " + e.getLocalizedMessage(), e);

			LOGGER.error(e.getMessage(), e);
		}
	}
  
	/**
	 * Освен проврката слага и съобщения в jsf-а ако има невалидни данни.
	 *
	 * @return <code>true</code> данните от екрана са валидни за запис, <code>false</code> ако не са
	 */
	protected boolean checkFormValid() {
		boolean valid = true;

		if (!checkUserName()) {
			valid = false;
		}
		if (!checkPassword()) {
			valid = false;
		}

		String s = trimToNULL(this.user.getNames());
		if (s == null) {
			JSFUtils.addMessage("formUserEdit:names", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "users.namesEmpty"));
			valid = false;
		} else {
			this.user.setNames(s);
		}
		
		s = trimToNULL(this.user.getEmail());
		if (s != null) {
//			JSFUtils.addMessage("formUserEdit:email", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.emailEmpty"));
//			valid = false;
//		} else {
			// Проверка за валидвност на eMail
			if (!ValidationUtils.isEmailValid(s)) {
				JSFUtils.addMessage("formUserEdit:email", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.errEmail"));
				valid = false;
			} else   this.user.setEmail(s);
		}
		if (this.user.getStatus() == null) {
			JSFUtils.addMessage("formUserEdit:stat", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.statusEmty"));
			valid = false;
		}
		
		if (this.user.getUserType() == null || this.user.getUserType().intValue() <= 0) {
			JSFUtils.addMessage("formUserEdit:tip", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.typeUserEmpty"));
			valid = false;
		}
		
//		if (this.user.getUserAdmStr() == null) {
//			JSFUtils.addMessage("formUserEdit:chooseAdmStr", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.slLicaEmpty"));
//			valid = false;
//		}
		
	    if (this.isPotrebZXC ) {
	    	if (!checkPassword ())  valid = false;
	    }

		return valid;
	}
	
	/**
	 * Проверка за потребителско име
	 *
	 * @return
	 */
	public boolean checkUserName() {
		String username = trimToNULL(this.user.getUsername());
		if (username == null) {
			JSFUtils.addMessage("formUserEdit:username", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "users.usernameEmpty"));
			return false;
		}
		if (chectForSpace(this.user.getUsername())) {
			JSFUtils.addMessage("formUserEdit:username", FacesMessage.SEVERITY_ERROR, getMessageResourceString(beanMessages, "userEdit.usernameNoSpace"));
			return  false;
			
		}
		
//		try {    
//			if (!Objects.equals(this.dbUsername, username)) { // само ако има реална промяна на потр.име. - проверка за формата на потребителско име
//
//				String regex = getSystemData().getSettingsValue("system.usernameRegex");
//				if (regex != null) { // само ако има нещо, защото ако няма се смята че няма да се налага валидация
//					boolean valid = username.matches(regex);
//					if (!valid) {
//						String dopInfo = getSystemData().getSettingsDopInfo("system.usernameRegex");
//
//						StringBuilder error = new StringBuilder(getMessageResourceString(UI_beanMessages, "users.usernameNotValid"));
//						if (dopInfo != null) {
//							error.append(" ");
//							error.append(dopInfo);
//						}
//						JSFUtils.addMessage("formUserEdit:username", FacesMessage.SEVERITY_ERROR, error.toString());
//						return false;
//					}
//				}
//			}
//
//		} catch (DbErrorException e) {
//			LOGGER.error("Грешка при проверка на потребителско име за валидност", e);
//			return false;
//		}

		this.user.setUsername(username); // за да е изчистено от разни празни интервали

		boolean ok = true;
		try { // проверка дали има вече въведено същото потребителско име
			X<Boolean> used = X.empty();
			JPA.getUtil().runWithClose(() -> used.set(this.dao.checkUsernameExist(this.user.getUsername(), this.user.getId())));

			if (used.isPresent() && used.get().booleanValue()) {
				JSFUtils.addMessage("formUserEdit:username", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "users.usernameExist"));
				ok = false;
			}

		} catch (BaseException e) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "general.errDataBaseMsg"), e);

			LOGGER.error(e.getMessage(), e);
		}
		return ok;
	}


	/** Оправят се ролите, за да се запишат  в БД */
	protected Set<Integer> selectUserRoles() {
		if (this.allroles.isEmpty()) {
			return null; // няма какво да се прави, защото нищо не е кликано
		}

		for (Entry<Integer, List<TreeNode>> roles : this.allroles.entrySet()) {
			
			Map<Integer, AdmUserRole> fixMap = new HashMap<>(); // в този пореден мап ще се пазят ролите от БД за избраната класификация
			int i = 0;
			while (i < this.user.getUserRoles().size()) {
				AdmUserRole tmp = this.user.getUserRoles().get(i);

				if (tmp.getCodeClassif().equals(roles.getKey())) {
					fixMap.put(tmp.getCodeRole(), tmp);

					this.user.getUserRoles().remove(i); // чистят се роли  само за избраната класификация
				} else {
					i++;
				}
			}

			Map<Integer, Set<Integer>> parents = new HashMap<>(); // горните нива

			for (TreeNode node : roles.getValue()) {
				if (node.getChildCount() == 0) { // Взимат се само листата, 

					SystemClassif classif = (SystemClassif) node.getData();

					AdmUserRole old = fixMap.get(classif.getCode());
					if (old != null) {
						this.user.getUserRoles().add(old);
					} else {
						this.user.getUserRoles().add(new AdmUserRole(classif.getCodeClassif(), classif.getCode())); // листото е добавено
					}

					TreeNode parent = node.getParent();
					while (parent != null && parent.getData() != null) { // добавям всички нагоре по дървото
						classif = (SystemClassif) parent.getData();

						Set<Integer> current = parents.computeIfAbsent(classif.getCodeClassif(), HashSet::new);
						current.add(classif.getCode()); // за да може да се маркира

						parent = parent.getParent();
					}
				}
			}

			for (Entry<Integer, Set<Integer>> entry : parents.entrySet()) { // оправят се горните нива само ако има нещо за тях
				for (Integer value : entry.getValue()) {
					AdmUserRole old = fixMap.get(value);
					if (old != null) {
						this.user.getUserRoles().add(old);
					} else {
						this.user.getUserRoles().add(new AdmUserRole(entry.getKey(), value));
					}
				}
			}
		}

		// така става ясно кои са променяни, за да може в последстиве които го вика, да си прави анализи и примерно да си рефрешни някои кешове
		Set<Integer> changed = new HashSet<>(this.allroles.keySet());
		
		this.allroles.clear();
		
		return changed;
	}

	private void selectUserGroups() {
		if (this.selectedGroupsIDs == null) {
			return;
		}
		this.user.getUserGroups().clear();
		for (String groupId : this.selectedGroupsIDs) {
			this.user.getUserGroups().add(new AdmGroup(Integer.valueOf(groupId)));
		}
	}
	
	
	public boolean chectForSpace (String username) {
		if (username == null || username.trim().isEmpty())  return false;
		for (int i = 0; i < username.length(); i++) {
			if (username.charAt(i) == ' ') return true;
		}
		return false;
	}
	
	/**
	 * Проверка за парола
	 *
	 * @return
	 */
	public boolean checkPassword() {
		if (!this.changePass) {
			return true; // ако няма смяна няма какво да и се валидира
		}

		boolean ok = true;
		if (isEmpty(this.passPlain)) {
			JSFUtils.addMessage("formUserEdit:passPlain", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "login.changePassNewPassword1Empty"));
			ok = false;
		}
		if (isEmpty(this.passRepeat)) {
			JSFUtils.addMessage("formUserEdit:passRepeat", FacesMessage.SEVERITY_ERROR, getMessageResourceString(UI_beanMessages, "login.changePassNewPassword2Empty"));
			ok = false;
		}
		if (!Objects.equals(this.passPlain, this.passRepeat)) {
			JSFUtils.addErrorMessage(getMessageResourceString(UI_beanMessages, "login.changePassNew12NotSame"));
			ok = false;
		}

		try {
			String regex = getSystemData().getSettingsValue("system.passwordRegex");
			if (regex != null) { // само ако има нещо, защото ако няма се смята че няма да се налага валидация
				boolean valid = this.passPlain.matches(regex);
				if (!valid) {
					String dopInfo = getSystemData().getSettingsDopInfo("system.passwordRegex");

					StringBuilder error = new StringBuilder(getMessageResourceString(UI_beanMessages, "login.changePassNotValid"));
					if (dopInfo != null) {
						error.append(" ");
						error.append(dopInfo);
					}
					JSFUtils.addMessage("formUserEdit:passPlain", FacesMessage.SEVERITY_ERROR, error.toString());
					ok = false;
				}
			}

		} catch (DbErrorException e) {
			LOGGER.error("Грешка при проверка на парола за валидност", e);
			ok = false;
		}
		return ok;
	
	}

	
	
	// при нов потребител се зачистваше паролата при смяна на таб преди запис и затова да я пази в бийна
	public void actionCheckPass() {
		this.user.setPassword(this.passPlain);
	}
	
	public void actionCheckPassRepeat() {
		this.setPassRepeat(this.passRepeat);
	}


	/** @return the classifList */
	public List<SelectItem> getClassifList() {
		return this.classifList;
	}

	/** @return the currentDate */
	public Date getCurrentDate() {
		return this.currentDate;
	}


//	/** @return the filesList */
//	public List<Files> getFilesList() {
//		return this.filesList;
//	}

	/** @return the groupsItemList */
	public List<SelectItem> getGroupsItemList() {
		return this.groupsItemList;
	}
	
	/** @return the selectedGroupsIDs */
	public String[] getSelectedGroupsIDs() {
		return this.selectedGroupsIDs;
	}
	
	/** @return the statusItemList */
	public List<SelectItem> getStatusItemList() {
		return this.statusItemList;
	}

	/** @return the typeItemList */
	public List<SelectItem> getTypeItemList() {
		return this.typeItemList;
	}

	/** @return the user */
	public AdmUser getUser() {
		return this.user;
	}

	/** @return the usernameRegexInfo */
	public String getUsernameRegexInfo() {
		return this.usernameRegexInfo;
	}
	
	/** @return the rootNode */
	public TreeNode getRootNode() {
		return this.rootNode;
	}

	/** @return the selectedNodes */
	public TreeNode[] getSelectedNodes() {
		return this.selectedNodes;
	}


	/** @return */
	public boolean isUserConfirmed() {
//		return Boolean.TRUE.equals(this.user.getConfirmed());
		return true;
	}

	/**
	 * Зареждаме данни от адм. структурата
	 */
	public void loadAdmStrData() { //
	}

	/** @param selectedGroupsIDs the selectedGroupsIDs to set */
	public void setSelectedGroupsIDs(String[] selectedGroupsIDs) {
		this.selectedGroupsIDs = selectedGroupsIDs;
	}

	/** @param selectedNodes the selectedNodes to set */
	public void setSelectedNodes(TreeNode[] selectedNodes) {
		this.selectedNodes = selectedNodes;
	}


	public Integer getTypePanelData() {
		return typePanelData;
	}

	public void setTypePanelData(Integer typePanelData) {
		this.typePanelData = typePanelData;
	}
	 
	

	public List<Integer> getForbiddenAccessList() {
		return forbiddenAccessList;
	}

	public void setForbiddenAccessList(List<Integer> forbiddenAccessList) {
		this.forbiddenAccessList = forbiddenAccessList;
	}


//	/** @param filesList the filesList to set */
//	public void setFilesList(List<Files> filesList) {
//		this.filesList = filesList;
//	}
	
	public boolean isPrOpenAll() {
		return prOpenAll;
	}

	public void setPrOpenAll(boolean prOpenAll) {
		this.prOpenAll = prOpenAll;
	}

	public boolean isPrAktDirNamesY() {
		return prAktDirNamesY;
	}

	public void setPrAktDirNamesY(boolean prAktDirNamesY) {
		this.prAktDirNamesY = prAktDirNamesY;
	}

	public boolean isPrAktDirEmailY() {
		return prAktDirEmailY;
	}

	public void setPrAktDirEmailY(boolean prAktDirEmailY) {
		this.prAktDirEmailY = prAktDirEmailY;
	}

	public boolean isPrAktDirY() {
		return prAktDirY;
	}

	public void setPrAktDirY(boolean prAktDirY) {
		this.prAktDirY = prAktDirY; 
	}

	public String getTextErr() {
		return textErr;
	}

	public void setTextErr(String textErr) {
		this.textErr = textErr;
	}

	public boolean isBlockPotreb() {
		return blockPotreb;
	}

	public void setBlockPotreb(boolean blockPotreb) {
		this.blockPotreb = blockPotreb;
	}

	public boolean getIsPotrebZXC() {
		return isPotrebZXC;
	}

	public void setPotrebZXC(boolean isPotrebZXC) {
		this.isPotrebZXC = isPotrebZXC;
	}

	
	public String getPassPlain() {
		return passPlain;
	}

	public void setPassPlain(String passPlain) {
		this.passPlain = passPlain;
	}

	public String getPassRepeat() {
		return passRepeat;
	}

	public void setPassRepeat(String passRepeat) {
		this.passRepeat = passRepeat;
	}

	public String getNameClassifIndDostap() {
		return nameClassifIndDostap;
	}

	public void setNameClassifIndDostap(String nameClassifIndDostap) {
		this.nameClassifIndDostap = nameClassifIndDostap;
	}

	public boolean isuseLdap() {
		return useLdap;
	}

	public void setuseLdap(boolean useLdap) {
		this.useLdap = useLdap;
	}

	public boolean isChangePass() {
		return changePass;
	}

	public void setChangePass(boolean changePass) {
		this.changePass = changePass;
	}

	public String getPasswordRegexInfo() {
		return passwordRegexInfo;
	}

	public void setPasswordRegexInfo(String passwordRegexInfo) {
		this.passwordRegexInfo = passwordRegexInfo;
	}

	
	
}
