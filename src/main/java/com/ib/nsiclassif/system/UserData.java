package com.ib.nsiclassif.system;


import static com.ib.system.SysConstants.CODE_DEIN_UNAUTHORIZED;

import java.util.Date;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.db.dao.AdmUserDAO;
import com.ib.indexui.system.Constants;
import com.ib.indexui.utils.ClientInfo;
import com.ib.indexui.utils.JSFUtils;
import com.ib.system.BaseUserData;
import com.ib.system.SysConstants;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;

/**
 * Конкретната за системата. В случая за нси!
 */
public class UserData extends BaseUserData {
	/** */
	static final Logger LOGGER = LoggerFactory.getLogger(UserData.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 4861625576007548101L;

	public UserData(Integer userId, String loginName, String liceNames) {
		super(userId, loginName, liceNames);
		// TODO Auto-generated constructor stub
	}
	
	private int currentLang;
	
	/**
	 * Проверява дали потребителя има достъп до страницата. Ако няма го праща на index.xhtml
	 *
	 * @param codePage
	 */
	public String checkPageAccess(Integer codePage) {
		boolean ok = hasAccess(Constants.CODE_CLASSIF_MENU, codePage);

		if (!ok) {
			HttpServletRequest request = (HttpServletRequest) JSFUtils.getExternalContext().getRequest();

			LOGGER.info("!!! UNAUTHORIZED PAGE ACCESS !!! username={};codePage={};url={}", getLoginName(), codePage, request.getRequestURL());

			StringBuilder sb = new StringBuilder();

			sb.append("От потребител \"" + getLoginName() + "\" е направен опит за достъп до страница ");
			sb.append(request.getRequestURL());

			SystemData sd = (SystemData) JSFUtils.getManagedBean("systemData");
			if (codePage != null) {
				try {
					sb.append(" (" + sd.decodeItem(Constants.CODE_CLASSIF_MENU, codePage, getCurrentLang(), new Date()) + ")");
				} catch (DbErrorException e) {
					sb.append(" код=" + codePage + "!");
				}
			}

			String userIP = ClientInfo.getClientIpAddr(request);
			String sessionId = ((HttpSession) JSFUtils.getExternalContext().getSession(false)).getId();
			String clientBrowser = ClientInfo.getClientBrowser(request);
			String clientOS = ClientInfo.getClientOS(request);

			sb.append("</br>IP=" + userIP + "; Browser=" + clientBrowser + "; OS=" + clientOS + "; SESSID=" + sessionId);

			

			SystemJournal journal = new SystemJournal(getUserId(), CODE_DEIN_UNAUTHORIZED, Constants.CODE_ZNACHENIE_JOURNAL_USER, getUserId(), sb.toString(), null);

			try {
				JPA.getUtil().runInTransaction(() -> new AdmUserDAO(this).saveAudit(journal));

			} catch (BaseException e) {
				LOGGER.error("Грешка при журналиране на Опит за неоторизиран достъп", e);
			}

			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			try {
				context.redirect(context.getRequestContextPath() + "/pages/accessDenied.xhtml");
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
		return "@NO@";
	}

	public int getCurrentLang() {
		
		return currentLang==0?NSIConstants.CODE_DEFAULT_LANG:currentLang;
	}

	public void setCurrentLang(int currentLang) {
		this.currentLang = currentLang;
	}
	

}