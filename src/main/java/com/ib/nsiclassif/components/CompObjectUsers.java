package com.ib.nsiclassif.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.indexui.utils.JSFUtils;
import com.ib.nsiclassif.db.dao.ObjectUsersDAO;
import com.ib.nsiclassif.db.dto.ObjectUsers;
import com.ib.nsiclassif.db.dto.ObjectUsersLang;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

@FacesComponent(value = "compObjectUsers", createTag = true)
public class CompObjectUsers extends UINamingContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CompObjectUsers.class);
//	private ArrayList<ObjectUsers> usersList=new ArrayList<ObjectUsers>();
	private Integer selectedId = 0;

	private String message="";
	@Override
	public void encodeAll(FacesContext context) throws IOException {
		LOGGER.debug("encodeAll");
		
		
//		loadUsers();
		super.encodeAll(context);
	}

	/**
	 * Същинското зареждане на таблицата
	 * Отделено е, защото се вика и след запис!!!
	 */
	private void loadUsers() {
		LOGGER.debug("loadUsers");
		// Код на обекта (classification,version, corTable)
		Integer codeObject = (Integer) getAttributes().get("codeObject");
		// КОнкретното ИД с което рабтим
		Integer idObject = (Integer) getAttributes().get("idObject");
		// Ezik
		Integer lang = (Integer) getAttributes().get("lang");

		try {
			LOGGER.debug("find users for:codeObject={},idObject={},lang={}", codeObject, idObject,lang);
			ArrayList<Object[]> findObjectUsers = new ObjectUsersDAO(ActiveUser.DEFAULT)
					.findObjectUsersNative(codeObject, idObject, lang);
			if (findObjectUsers == null) {
				findObjectUsers = new ArrayList<Object[]>();
			}
			setUsersList(findObjectUsers);
		} catch (DbErrorException e) {
			//TODO:ne]o trqbwa da se naprawi tuk!!!
			LOGGER.error("DBError", e);
		}
	}
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
//		LOGGER.debug("encodeBegins");
		super.encodeBegin(context);
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
//		LOGGER.debug("encodeEnd");
		super.encodeEnd(context);
	}

	// Това е ако има <f:event type="preRenderComponent" listener="#{cc.init()}" />
	public void init() {
		LOGGER.debug("init");
		loadUsers();
	}

	public Integer getCodeObject() {
		Integer co=(Integer) getStateHelper().get("codeObject");
		LOGGER.debug("getCodeObject={}",co);
		return co;
	}

	public void setCodeObject(Integer codeObject) {
		LOGGER.debug("setCodeObject");
		getStateHelper().put("codeObject", codeObject);
	}

	public Integer getIdObject() {
		Integer id=(Integer) getStateHelper().get("idObject");
		LOGGER.debug("getIdObject={}",id);
		return id;
	}

	public void setIdObject(Integer idObject) {
		LOGGER.debug("setIdObject");
		getStateHelper().put("idObject", idObject);
	}

	public Integer getLang() {
		
		Integer lang = (Integer) getStateHelper().get("lang");
		LOGGER.debug("getLang:{}",lang);
		return lang;
	}

	public void setLang(Integer lang) {
		LOGGER.debug("setLang:{}"+lang);
		getStateHelper().put("lang", lang);
	}

	public ArrayList<Object[]> getUsersList() {
		LOGGER.debug("phaze=" + FacesContext.getCurrentInstance().getCurrentPhaseId());
		if (FacesContext.getCurrentInstance().getRenderResponse()) {
			LOGGER.debug("--RenderResponse");
		} else if (FacesContext.getCurrentInstance().getResponseComplete()) {
			LOGGER.debug("--ResponseComplete");
		}
//		if (!FacesContext.getCurrentInstance().getRenderResponse()) {
//			LOGGER.debug("--notRenderResponse");
////			return null;
//		} else {
//			LOGGER.debug("++RenderResponse");
//		}
		ArrayList<Object[]> eval = (ArrayList<Object[]>) getStateHelper().eval("usersList", null);
		LOGGER.debug("getUsersList:size={}", (eval != null ?eval.size():"null"));
		return eval != null ? eval : new ArrayList<Object[]>();

	}

	public void setUsersList(ArrayList<Object[]> usersList) {
		getStateHelper().put("usersList", usersList);
	}

	public Integer getSelectedId() {
		Integer eval = (Integer) getStateHelper().get("selectedId");
		LOGGER.debug("getSelectedId:{}", eval);
		return eval;
	}

	public void test(Integer parm) {
		LOGGER.debug("test:{}", parm);
	}

	public void actionNewUser() {
		LOGGER.debug("actionNewObject");

		// Код на обекта (classification,version, corTable)
				Integer codeObject = (Integer) getAttributes().get("codeObject");
				// КОнкретното ИД с което рабтим
				Integer idObject = (Integer) getAttributes().get("idObject");
		LOGGER.debug("--------> CodeObejcte={},idObejct={}",codeObject,idObject);
		
		ObjectUsers ou=new ObjectUsers();
		ou.setCodeObject(codeObject);
		ou.setIdObject(idObject);
		ObjectUsersLang oul=new ObjectUsersLang();
		oul.setLang(1);
		HashMap<Integer,ObjectUsersLang> langMap=new HashMap<Integer, ObjectUsersLang>();
		oul.setObjectUsers(ou);
		langMap.put(1, oul);
		ou.setLangMap(langMap);
		setSelectedUser(ou);
	}
	
	public void actionOpenSelectedId(Integer selectedId,Integer lang) {
		LOGGER.debug("setSelectedId:{}, lang:{}", selectedId,lang);
		getStateHelper().put("selectedId", selectedId);

//		String dialogWidgetVar = "PF('userDlg').show();";
//		PrimeFaces.current().executeScript(dialogWidgetVar);
//		PrimeFaces.current().ajax().update("testForm:myComp:babameca");

		try {
			ObjectUsers selectedUser = new ObjectUsersDAO(ActiveUser.DEFAULT).findById(selectedId);
//			LOGGER.debug("found {} users",findObjectUsers);
			if (!selectedUser.getLangMap().containsKey(lang)) {
				ObjectUsersLang oul=new ObjectUsersLang();
				oul.setLang(lang);
				oul.setObjectUsers(selectedUser);
				selectedUser.getLangMap().put(lang, oul);

			}
			
			setSelectedUser(selectedUser);

		} catch (DbErrorException e) {
			LOGGER.error("DBError", e);

			LOGGER.debug("setSelectedId-ЕНД:{}", selectedId);
		}

	}
	
	public void actionSave() {
		LOGGER.debug("actionSave");
		LOGGER.debug("currentUser:{}", getSelectedUser());
		message="";
		FacesContext context = FacesContext.getCurrentInstance();
		String clientId = this.getClientId(context);
		boolean valid=true;
		//Check correct data
		if (getSelectedUser().getCodeLice()==null) {
//			JSFUtils.addMessage("", FacesMessage.SEVERITY_ERROR, "Моля изберете лице!!");
			message+="Моля изберете лице!!";
			valid=false;
		}
		
		
		if (valid) {
			try {

				JPA.getUtil().begin();

				new ObjectUsersDAO(ActiveUser.DEFAULT).save(getSelectedUser());

				JPA.getUtil().commit();
				loadUsers();
			} catch (Exception e) {
				JPA.getUtil().rollback();
//				JSFUtils.addMessage("", FacesMessage.SEVERITY_ERROR, "Грешка при работа с базата!!", e.getMessage());
				message="Грешка при работа с базата!!";
				valid = false;
				LOGGER.error("DB Error", e);

			} finally {
				JPA.getUtil().closeConnection();
			} 
		}
		if (valid) {
			LOGGER.debug("Hide and update table");
			String dialogWidgetVar = "PF('userDlg').hide();";
			PrimeFaces.current().executeScript(dialogWidgetVar);
			PrimeFaces.current().ajax().update(clientId + ":compTable");
		} else {
			LOGGER.debug("SetError and update messages");
			PrimeFaces.current().ajax().update(clientId + ":compErrorMessages");
		}
	}

	public void setSelectedUser(ObjectUsers selectedUser) {
		getStateHelper().put("selectedUser", selectedUser);
	}

	public ObjectUsers getSelectedUser() {
		ObjectUsers eval = (ObjectUsers) getStateHelper().get("selectedUser");
		return eval != null ? eval : new ObjectUsers();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
