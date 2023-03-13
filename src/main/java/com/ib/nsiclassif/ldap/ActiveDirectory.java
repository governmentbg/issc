package com.ib.nsiclassif.ldap;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.Constants;
import com.ib.indexui.system.IndexUIbean;
import com.ib.nsiclassif.db.dto.AdmUser;
import com.ib.system.exceptions.AuthenticationException;
import com.ib.system.exceptions.DbErrorException;

/**
 * @author s.marinov
 */
public class ActiveDirectory extends IndexUIbean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3918765806456579702L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveDirectory.class);
     

	private static final String[] RETURN_ATTRIBUTES = { "sAMAccountName", "givenName", "cn", "mail", "userAccountControl", "memberOf" };
	
	
	
	/**
	 *  Търсене на потребител по username в активната директория
	 * @param searchValue  - username, по което се търси
	 * @param searchBy - признак по какво се търси - по username - "sAMAccountName";
	 * @return
	 * @throws DbErrorException
	 * @throws NamingException 
	 */
	public   AdmUser  searchUserInAktDir (DirContext context, String userName, String password, String searchBy) throws DbErrorException, NamingException {
		

		AdmUser us =  null;
		try {
			
			NamingEnumeration<SearchResult> result = searchUsers(context, userName, searchBy);

			if (result == null || !result.hasMore()) {
				LOGGER.info("NQMA namereni danni za user v AD");
				return null;
			}

			us = new AdmUser();
	
			while (result.hasMore()) {

				SearchResult element = result.next();

				Attributes attrs = element.getAttributes();

	
				String temp = attrs.get("sAMAccountName").toString();
				us.setUsername(temp.substring(temp.indexOf(':') + 1).trim());

		//		temp = attrs.get("givenName").toString();     //  Кратко име

				if (attrs.get("cn")!=null) {
					temp = attrs.get("cn").toString();   // Пълно име
					us.setNames( temp.substring(temp.indexOf(':') + 1).trim());	
				}
				if (attrs.get("mail")!=null) {
					temp = attrs.get("mail").toString();
					us.setEmail(temp.substring(temp.indexOf(':') + 1).trim());	
				}
				
				if (attrs.get("userAccountControl")!=null) {
					temp = attrs.get("userAccountControl").toString();
					long userAccountControl = Long.parseLong(temp.substring(temp.indexOf(':') + 1).trim());
					boolean active = (userAccountControl & 2) == 0;
					
					if (active) {
						us.setStatus(Constants.CODE_ZNACHENIE_STATUS_ACTIVE);
					} else {
						us.setStatus(Constants.CODE_ZNACHENIE_STATUS_INACTIVE);
					}
				}else {
					us.setStatus(Constants.CODE_ZNACHENIE_STATUS_INACTIVE);
				}
				if (getSystemData().getSettingsValue("LDAP_GROUP_MEMBER")!=null) {
					if (attrs.get("memberOf")!=null) {
						String groups = attrs.get("memberOf").toString();		
						LOGGER.info("GROUPS: "+ groups);
						if (!groups.contains(getSystemData().getSettingsValue("LDAP_GROUP_MEMBER"))) {
							us=null;
							throw new AuthenticationException("Не сте член на групата \""+getSystemData().getSettingsValue("LDAP_GROUP_MEMBER")+"\"!", null);
						}
					}else {
						//ne e член на нито една група дори
						us=null;
						throw new AuthenticationException("Не сте член на групата \""+getSystemData().getSettingsValue("LDAP_GROUP_MEMBER")+"\"!", null);
					}				
				}


				
				

				

				if (us!=null && us.getStatus()!=null && us.getStatus()==Constants.CODE_ZNACHENIE_STATUS_ACTIVE) break;   // При първия активен статус се връща управлението
			}

			
		} catch (DbErrorException e) {

			throw new DbErrorException (e.getLocalizedMessage());
		} 
		finally {
			closeContext(context);
		}
		
		return us;
	}
	
//*************************************************************************************************************************************************************************************************	

	/** @param dirContext */
	public void closeContext(DirContext dirContext) {
		try {
			if (dirContext != null) {
				dirContext.close();
			}
		} catch (Exception e) {
			LOGGER.error("Грешка при close context - " + e.getLocalizedMessage(), e);
		}
	}

	public DirContext createContext(String username, String password) throws DbErrorException {
		Properties properties = new Properties();
				
		String domainString=getSystemData().getSettingsValue("DOCU_WORK_DEFAULT_LDAP_DOMAIN");
		String userString=username;		
		
		if (domainString==null) {
			domainString="";
		}else {
			if (domainString.contains("{user}")) {
				if (!username.contains("@nsi")) {
					userString=domainString.replace("{user}", username);					
				}
			}
		}

		
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		properties.put(Context.PROVIDER_URL, getSystemData().getSettingsValue("java.naming.provider.url"));
		properties.put(Context.SECURITY_PRINCIPAL, userString );
		properties.put(Context.SECURITY_CREDENTIALS, password);
		properties.put(Context.REFERRAL, "follow");

		DirContext context;
		try {
			context = new InitialDirContext(properties);
		} catch (NamingException e) {
			context = null;
			LOGGER.error("Грешка - " + e.getLocalizedMessage(), e.getMessage());
		}
		return context;
	}

	/**
	 * @param context
	 * @param searchValue
	 * @param searchBy
	 * @return
	 * @throws NamingException
	 * @throws DbErrorException 
	 */
	public NamingEnumeration<SearchResult> searchUsers(DirContext context, String searchValue, String searchBy) throws NamingException, DbErrorException {
		SearchControls searchCtls = new SearchControls();
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchCtls.setReturningAttributes(RETURN_ATTRIBUTES);

		String filter = "(&((&(objectCategory=Person)(objectClass=User)))";
//		filter += "(!(userAccountControl:1.2.840.113556.1.4.803:=2))"; // С‰Рµ РІСЉСЂРЅРµ СЃР°РјРѕ Р°РєС‚РёРІРЅРёС‚Рµ

		if (searchBy != null) {
			filter += "(" + searchBy + "=" + searchValue + "))";
		}
		return context.search(getSystemData().getSettingsValue("distinguished_name_DN"), filter, searchCtls);
	}
}