package com.ib.nsiclassif.beans;

import java.io.IOException;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.credential.Credential;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.system.IndexHttpSessionListener;
import com.ib.indexui.system.IndexLoginBean;
import com.ib.indexui.utils.ClientInfo;
import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.system.NSIConstants;
import com.ib.system.ActiveUser;
import com.ib.system.BaseUserData;
import com.ib.system.IBUserPrincipal;
import com.ib.system.auth.DBCredential;
import com.ib.system.auth.LDAPCredential;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.AuthenticationException;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.RestClientException;

/**
 * Логин със SecurityContext
 *
 * @author belev
 */
@Named("login")
@ViewScoped
public class LoginBean extends IndexLoginBean {

	/**  */
	private static final long serialVersionUID = 8191901936895268740L;

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginBean.class);

	@Inject
	private SecurityContext	securityContext;
	@Inject
	private ExternalContext	externalContext;
	@Inject
	private FacesContext	facesContext;

	
	
	/** */
	public LoginBean() {
		super();
	}

	/**
	 * Логин със SecurityContext
	 */
	@Override
	protected BaseUserData login() {
		LOGGER.info("---> SecurityContext login <---");

		HttpServletRequest request = (HttpServletRequest) this.externalContext.getRequest();
		HttpServletResponse response = (HttpServletResponse) this.externalContext.getResponse();

		try { // винаги е нов логин. може и да има и по хитър начин!
			request.logout();
		} catch (ServletException e) {
			LOGGER.error(e.getMessage(), e);
		}

		AuthenticationStatus status = continueAuthentication(request, response);

		switch (status) {

		case SEND_CONTINUE:
			LOGGER.debug("actionLogin-SEND_CONTINUE");

			this.facesContext.responseComplete();

			break;

		case SEND_FAILURE:
			LOGGER.debug("actionLogin-SEND_FAILURE");

			JSFUtils.addErrorMessage( getMessageResourceString(UI_beanMessages, "login.fail"));

			break;

		case SUCCESS:
			LOGGER.debug("actionLogin-SUCCESS");

			try {
				this.externalContext.redirect(this.externalContext.getRequestContextPath() + "/pages/classificationsList.xhtml");
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}

			break;

		case NOT_DONE:
			// тука няма да влезне
		}

		// трябва да върна данните за логнатия потребител
		Set<IBUserPrincipal> principals = this.securityContext.getPrincipalsByType(IBUserPrincipal.class);

		if (principals.isEmpty()) {
			return null; // явно не се е логнал успешно
		}
		return principals.iterator().next().getUserData(); // ето го усера
	}

	private AuthenticationStatus continueAuthentication(HttpServletRequest request, HttpServletResponse response) {
		
		Credential credential = null;
		String username = getUsername();
		
		try {
			if ((getSystemData().getSettingsValue("useLdap") != null &&  getSystemData().getSettingsValue("useLdap").equals("true")) && (username.toUpperCase().compareTo("ZXC") != 0
				    && username.toUpperCase().compareTo("ZXC1") != 0	
				    && username.toUpperCase().compareTo("ZXC2") != 0) ) {  // За всички потребители без служебния zxc - първо достъп до LDap
				credential = new LDAPCredential(getUsername(), getPassword());// default
			} else {		
			    credential = new DBCredential(getUsername(), getPassword());// default
			}
		} catch (DbErrorException e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		AuthenticationParameters parameters = AuthenticationParameters.withParams().credential(credential);
		
		return this.securityContext.authenticate(request, response, parameters);
		
	}
	
	
	/**
	 * Метод за запис в журнала на успешен логин.
	 *
	 * @param request
	 * @param userIP
	 * @param userData
	 * @see IndexHttpSessionListener
	 */
	@Override
	protected void journalLoginSuccess(HttpServletRequest request, String userIP, BaseUserData userData) {
		
		String sessionId = ((HttpSession) JSFUtils.getExternalContext().getSession(false)).getId();
		String clientBrowser = ClientInfo.getClientBrowser(request);
		String clientOS = ClientInfo.getClientOS(request);
		
		String identObject = "Username=" + userData.getLoginName() + "; IP=" + userIP + "; Browser=" + clientBrowser + "; OS=" + clientOS + ";</br>SESSID=" + sessionId;
		
		SystemJournal journal = new SystemJournal(userData.getUserId(), NSIConstants.CODE_DEIN_LOGIN, NSIConstants.CODE_ZNACHENIE_JOURNAL_USER, userData.getUserId(), identObject, null);
		saveAudit(journal);		
	}
	
	
	/**
	 * Метод за запис в журнала на неуспешен логин.
	 *
	 * @param errorMessage
	 * @param request
	 * @param userIP
	 * @param ae
	 */
	@Override
	protected void journalLoginFail(String errorMessage, HttpServletRequest request, String userIP, AuthenticationException ae) {
		String sessionId = ((HttpSession) JSFUtils.getExternalContext().getSession(false)).getId();
		String clientBrowser = ClientInfo.getClientBrowser(request);
		String clientOS = ClientInfo.getClientOS(request);

		String identObject = errorMessage + "</br>IP=" + userIP + "; Browser=" + clientBrowser + "; OS=" + clientOS + "; SESSID=" + sessionId;

		
	
		SystemJournal journal = new SystemJournal(ActiveUser.DEFAULT.getUserId(), NSIConstants.CODE_DEIN_LOGIN_FAILED, NSIConstants.CODE_ZNACHENIE_JOURNAL_USER, getUserData().getUserId(), identObject, null);
		saveAudit(journal);		
		
	}
	
	@Override
	public void journalLogout() throws ServletException, BaseException, RestClientException, IOException {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest request = (HttpServletRequest) context.getRequest();

		String userIP = ClientInfo.getClientIpAddr(request);
		String sessionId = ((HttpSession) JSFUtils.getExternalContext().getSession(false)).getId();
		String clientBrowser = ClientInfo.getClientBrowser(request);
		String clientOS = ClientInfo.getClientOS(request);

		String identObject = "Username=" + getUserData().getLoginName() + "; IP=" + userIP + "; Browser=" + clientBrowser + "; OS=" + clientOS + ";</br>SESSID=" + sessionId;

		SystemJournal journal = new SystemJournal(getUserData().getUserId(), NSIConstants.CODE_DEIN_LOGOUT, NSIConstants.CODE_ZNACHENIE_JOURNAL_USER, null, identObject, null);
		saveAudit(journal);	

		
	}
	
	
	
	
	private void saveAudit(SystemJournal journal) {
		
		try {
			//В тази системе винаги имаме достъп до базата и не ползваме isDatabaseMode()				
			JPA.getUtil().runInTransaction(() -> JPA.getUtil().getEntityManager().persist(journal));	

		} catch (BaseException e) {
			LOGGER.error("Грешка при журналиране на login", e);
		}
		
	}

	
}