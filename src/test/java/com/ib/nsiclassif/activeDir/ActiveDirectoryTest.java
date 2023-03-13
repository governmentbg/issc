package com.ib.nsiclassif.activeDir;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class ActiveDirectoryTest {
	
	// ВСИЧКО ТУК Е ЗА ДА МОЖЕ JAR ПРИ КЛИЕНТ ДА ТЕСТВАМЕ ЧЕ ИНАЧЕ САМО ДЕПЛОИ.
	private static final String[] RETURN_ATTRIBUTES = { "sAMAccountName", "givenName", "cn", "mail", "userAccountControl", "memberOf" };
	//*****************************************************************************
//	private String	LDAP_URL		= "indexbg.lirex.com:389";
//	private String	DOMAIN	= "indexbg";
//	private String	DOMAIN_DN	= "DC=INDEXBG,DC=LIREX,DC=COM";
//	public static final String SECURITY_AUTENTICATION = "simple";       // java.naming.security.authentication = Context.SECURITY_AUTHENTICATION
//	String username = "testuser";
//	String password = "Alab@la";
	//***********************************

	public static void main(String[] args) {
		DirContext context = null;
		try {
			String domainString=args[0];
			String domainString2=args[1];
			String user=args[2];
			String pass=args[3];
			String personSearch=args[4];
			
////			user="sc_user_2@nsi.internal";
////			pass="&SK*4wp2@qea";
//			user="sc_user_1@nsi.internal";
//			pass="4M*jYn9HQn$d";
			
			
			context = createContext(domainString,domainString2,user, pass);
			System.out.println(context!=null?"ИМАМЕ КОНТЕКСТ":"КОНТЕКСТА Е НУЛЛ");
			// System.out.println("tconnToAktivnaDir -> Connect SUCCESS!");

//		        SearchControls searchControls = new SearchControls();
//		        String[] attrIDs = {"cn"};
//		        searchControls.setReturningAttributes(attrIDs);
//		        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
//
//		        NamingEnumeration answer = context.search("cn", "((&(objectClass=user)(mail=*)))", searchControls);
//		        while (answer.hasMore()) {
//		            SearchResult rslt = (SearchResult) answer.next();
//		            Attributes attrs = rslt.getAttributes();
//		            System.out.println(attrs.get("cn"));
//		        }

			NamingEnumeration<SearchResult> result = searchUsers(context,domainString2, personSearch, "sAMAccountName");

			if (result != null && result.hasMore()) {
				System.out.println("Search Result: ");

			} else {
				System.out.println("Not found ");
			}


			while (result.hasMore()) {

				SearchResult element = result.next();

				Attributes attrs = element.getAttributes();

				System.out.println(attrs.toString());
				
				String temp = attrs.get("sAMAccountName").toString();
				 System.out.println("Username : " + temp.substring(temp.indexOf(':') +
				 1).trim());

				 temp = attrs.get("givenName").toString(); // Кратко име
				 System.out.println("Name : " + temp.substring(temp.indexOf(':') + 1).trim());

				temp = attrs.get("cn").toString(); // Пълно име
				 System.out.println("Display Name : " + temp.substring(temp.indexOf(':') +
				 1).trim());

					temp = attrs.get("mail").toString();
//					us.setEmail(temp.substring(temp.indexOf(':') + 1).trim());
				 System.out.println("Email : " + temp.substring(temp.indexOf(':') +
				 1).trim());

//					SearchControls ctls = new SearchControls();
//					String usersContainer = "cn=users,dc=example,dc=com";  
//					String[] attrIDs = { "cn", "memberOf" };
//					ctls.setReturningAttributes(attrIDs);
//					ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
//
//					NamingEnumeration answer = context.search(usersContainer, "(&(objectclass=person)(cn=*sahi*))", ctls);

				String groups = attrs.get("memberOf").toString();
				System.out.println("Grоups: " + groups);

				temp = attrs.get("userAccountControl").toString();
				long userAccountControl = Long.parseLong(temp.substring(temp.indexOf(':') + 1).trim());
				boolean active = (userAccountControl & 2) == 0;
				System.out.println("Потребителя активен: " + active);
//					if (active) {
//						us.setStatus(Constants.CODE_ZNACHENIE_STATUS_ACTIVE);
//					} else {
//						us.setStatus(Constants.CODE_ZNACHENIE_STATUS_INACTIVE);
//					}
//					
//	
//					if (active) break;   // При първия активен статус се връща управлението
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeContext(context);
		}

	}

	public static void closeContext(DirContext dirContext) {
		try {
			if (dirContext != null) {
				dirContext.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static DirContext createContext(String domain,String defaultDomain, String username, String password) {
		Properties properties = new Properties();
		
		 
		
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		properties.put(Context.PROVIDER_URL, "ldap://" + domain);
		properties.put(Context.SECURITY_PRINCIPAL, username);
		properties.put(Context.SECURITY_CREDENTIALS, password);
		properties.put(Context.REFERRAL, "follow");
		properties.put(Context.SECURITY_AUTHENTICATION, "simple");

		
		
		
		
		DirContext context;
		try {
			context = new InitialDirContext(properties);
			
		} catch (NamingException e) {
			context = null;
			e.printStackTrace();
		}
		return context;
	}

	public static NamingEnumeration<SearchResult> searchUsers(DirContext context,String domain, String searchValue, String searchBy)
			throws NamingException {
		SearchControls searchCtls = new SearchControls();
		searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchCtls.setReturningAttributes(RETURN_ATTRIBUTES);
//		SearchControls ctls = new SearchControls();
//		String[] attrIDs = {"cn"};
//		ctls.setReturningAttributes(attrIDs);
//		String[] attributes = {"memberOf"};
//		ctls.setReturningAttributes(attributes);
//		String filter = "(&(objectclass=user)(sAMAccountName=userName))";
		String filter = "(&((&(objectCategory=Person)(objectClass=User)))";
//		filter += "(!(userAccountControl:1.2.840.113556.1.4.803:=2))"; // С‰Рµ РІСЉСЂРЅРµ СЃР°РјРѕ Р°РєС‚РёРІРЅРёС‚Рµ

		if (searchBy != null) {
			filter += "(" + searchBy + "=" +  searchValue+"))";
		}
		return context.search(domain, filter, searchCtls);
	}
}
