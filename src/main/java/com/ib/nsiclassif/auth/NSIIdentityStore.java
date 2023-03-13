package com.ib.nsiclassif.auth;


import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.Constants;
import com.ib.nsiclassif.db.dao.AdmUserDAO;
import com.ib.nsiclassif.db.dto.AdmGroup;
import com.ib.nsiclassif.db.dto.AdmUser;
import com.ib.nsiclassif.ldap.ActiveDirectory;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.nsiclassif.system.SystemData;
import com.ib.nsiclassif.system.UserData;
import com.ib.system.ActiveUser;
import com.ib.system.BaseUserData;
import com.ib.system.auth.DBCredential;
import com.ib.system.auth.EAuthCredential;
import com.ib.system.auth.IBIdentityStore;
import com.ib.system.auth.LDAPCredential;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.AuthenticationException;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.InvalidParameterException;

/**
 * Прави логин чрез имплементираните по потребителско име и парола
 *
 * @author belev
 */
@ApplicationScoped
public class NSIIdentityStore extends IBIdentityStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(NSIIdentityStore.class);

	SystemData systemData=null;
	@Inject
	private ServletContext servletContext;

	/** */
	@Override
	protected Optional<BaseUserData> findUserDB(DBCredential credential) throws AuthenticationException {
		String username = credential.getCaller();
		String password = credential.getPasswordAsString();

		if (username == null || password == null) {
			return Optional.empty();
		}

		boolean isPotrebAct = true; 
		AdmUserDAO dao = new AdmUserDAO(ActiveUser.DEFAULT);
		AdmUser user  = null;
		try {
			
//			if (getSystemData().getSettingsValue("useLdap").equals("true") && (username.toUpperCase().compareTo("ZXC") != 0
//			    && username.toUpperCase().compareTo("ZXC1") != 0	
//			    && username.toUpperCase().compareTo("ZXC2") != 0) ) {  // За всички потребители без служебния zxc
//			/********************************************************************************************************
//			// Първо проверка в активната директория по username и password
//			//*******************************************************************************************************
//			*/ 
//								
//				try {
//					 user =  readInfoFromAktDir (username, password);
//				     if (user == null)   	throw new AuthenticationException(AuthenticationException.CODE_USER_UNKNOWN, null);
//				     if (user.getStatus() != null && user.getStatus().intValue() == Constants.CODE_ZNACHENIE_STATUS_INACTIVE)
//				    	 isPotrebAct = false;     // Неактивен потребител
//				} catch (DbErrorException e1) {
//					throw new AuthenticationException(e1);
//				}
//				   // След успешно намиране на потребителя в активната директория -  търсене в базата на НСИ по username  и валидиране
//				user = dao.validateUser(getSystemData(), username, isPotrebAct); // намира потребителя и валидира дали може да се направи логин
//			} else {   // Потребител zxc
				user = dao.validateUser(getSystemData(), username, password); // намира потребителя по username и password и валидира дали може да се направи логин
				
//			}
					
			BaseUserData userData = loadUserData(user, null);
			
			return Optional.of(userData);

		} catch (AuthenticationException e) {
			LOGGER.error(e.getMessage());
			throw e; // трябва да се знае че това е проблема

		} catch (Exception e) {
			LOGGER.error("DBCredential login ERROR!", e);
			throw new AuthenticationException(e);

		} finally {
			JPA.getUtil().closeConnection();
		}
	}
	
	/** */
	@Override
	protected Optional<BaseUserData> findUserEAuth(EAuthCredential credential) throws AuthenticationException {
		Integer userId = credential.getUserId();

		if (userId == null) {
			throw new AuthenticationException(new InvalidParameterException("UserId can not be null!"));
		}

		try {
			SystemData systemData = (SystemData) this.servletContext.getAttribute("systemData");

			// TODO може да се наложи да се вика нещо подобно на AdmUserDAO.validateUser заради брой опити за достъп, проверка на
			// статуси и т.н.
			AdmUser user = JPA.getUtil().getEntityManager().find(AdmUser.class, userId); // логин

			// initialize user's accessValues
			AdmUserDAO admUserDAO = new AdmUserDAO(ActiveUser.of(userId));
			final Map<Integer, Map<Integer, Boolean>> userAccessMap = admUserDAO.findUserAccessMap(userId);
			user.setAccessValues(userAccessMap);

			UserData userData = loadUserData(user, Long.valueOf(NSIConstants.CODE_DEFAULT_LANG));

			return Optional.of(userData);

		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new AuthenticationException(e);

		} finally {
			JPA.getUtil().closeConnection();
		}
	}

	
	/** */
	@Override
	protected Optional<BaseUserData> findUserLDAP(LDAPCredential credential) throws AuthenticationException {
		SystemData systemData = (SystemData) this.servletContext.getAttribute("systemData");

		ActiveDirectory ac=new ActiveDirectory();
		DirContext context=null;
		try {
			context=ac.createContext(credential.getCaller(), credential.getPasswordAsString());
		} catch (DbErrorException e) {
			e.printStackTrace();
			throw new AuthenticationException(AuthenticationException.CODE_USER_UNKNOWN, null);
		}
		
		LOGGER.info(context==null?"NQMA CONTEXT"+credential.getCaller():"IMA CONTEXT"+credential.getCaller());
		if (context == null) {     // Въведеният потребител фигурира в Ldap
			throw new AuthenticationException(AuthenticationException.CODE_USER_UNKNOWN, null);
		} 
		  
		try { // load user data from database

			AdmUserDAO dao = new AdmUserDAO(ActiveUser.DEFAULT);
			
			// След успешно намиране на потребителя в активната директория -  търсене в базата на НСИ по username
			
			AdmUser user  = dao.searchInDbByIdOrName(systemData, null, credential.getCaller());    // търси потребителя в базата
			LOGGER.info(user==null?"NQMA user v bazata":"IMA user v bazata");
			if (user!=null) {
				LOGGER.info("user is Active: "+ user.getStatus());
				if (!user.getStatus().equals(Constants.CODE_ZNACHENIE_STATUS_ACTIVE)) { // проверка на статус
					LOGGER.info("user is IN Active krai.");
					throw new AuthenticationException(AuthenticationException.CODE_UNAUTHORIZED_STATUS, user.getId());
				}
				
				BaseUserData userData = loadUserData(user, null);
				
				return Optional.of(userData);	
			}else {
				AdmUser user1=ac.searchUserInAktDir(context,credential.getCaller(),credential.getPasswordAsString(),"sAMAccountName");
				LOGGER.info("USER DANNI FROM DIR: "+user1);
				if (user1!=null) {
					if (user1.getStatus()!=Constants.CODE_ZNACHENIE_STATUS_ACTIVE) {
						throw new AuthenticationException(AuthenticationException.CODE_UNAUTHORIZED_STATUS, null);
					}
					JPA.getUtil().begin();
					user1.setStatusDate(new Date());
					user1.setLang(Constants.CODE_DEFAULT_LANG);
					user1.setUserType(NSIConstants.CODE_ZNACHENIE_ROLE_CLASS_EXP);
					user1.getUserGroups().add(new AdmGroup(NSIConstants.CODE_ZNACHENIE_GROUP_USER_SPRAVKI));
					user1=new AdmUserDAO(ActiveUser.DEFAULT).save(user1);	
					
					JPA.getUtil().commit();
					if (user1!=null) {
						Map<Integer, Map<Integer, Boolean>> accessValues = new AdmUserDAO(ActiveUser.DEFAULT).findUserAccessMap(user1.getId());
						user1.setAccessValues(accessValues); 
					}
					UserData userData = loadUserData(user1, Long.valueOf(NSIConstants.CODE_DEFAULT_LANG));
					return Optional.of(userData);	
				}else {
					throw new AuthenticationException(AuthenticationException.CODE_USER_UNKNOWN, null);
				}

			}
	        

		// TODO тука с грешките надолу не е добре
		} catch (DbErrorException e) {
			LOGGER.error("LDAPCredential login ERROR - db select!", e);
			throw new AuthenticationException(e);

		} catch (BaseException e) {
			LOGGER.error("LDAPCredential login ERROR - db select!", e);
			throw new AuthenticationException(e);
		} catch (NamingException e) {
			LOGGER.error("LDAPCredential login ERROR - db select!", e);
			throw new AuthenticationException(e);
		} finally {
			try {
				context.close();
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JPA.getUtil().closeConnection();
		}
	}

	

	public UserData loadUserData(AdmUser user, Long lang) throws DbErrorException {
	
		if (user == null) {
			return null;
		}
		
		
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("loadUserData  - start");
		}
		
		  

		
		if (lang == null){
			lang = Long.valueOf(NSIConstants.CODE_DEFAULT_LANG);
		}
		
		
		
		UserData userData = new UserData(user.getId(), user.getUsername(), user.getNames());
		
		userData.setAccessValues(new HashMap<>(user.getAccessValues())); // задавам правата
		user.setAccessValues(null); // зачиствам ги от ентитито!
		
		
		
		return userData;
	}
	
	
	public SystemData getSystemData() {
		if (systemData==null) {
			systemData=(SystemData)servletContext.getAttribute("systemData");
		}
		return systemData;
	}
	
//	public AdmUser readInfoFromAktDir (String userName, String password)  throws DbErrorException {
//		
//		ActiveDirectory aDir = new ActiveDirectory ();
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
//		//ako context-a е инициализиран значи потребителя съществува и паролата му е вярна отиваме да му вземем данните...
//		if (context==null)  return null;
//		
//		AdmUser us = new AdmUser ();
//		
//		String searchBy = "sAMAccountName"; // "sAMAccountName", "givenName", "cn", "mail"
//	
//	        	try {
//					us = aDir.searchUserInAktDir (context,userName, password,  searchBy);
//				} catch (DbErrorException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					throw new DbErrorException (e.getLocalizedMessage());
//					
//				}
//		return us;
//	}
}