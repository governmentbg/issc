package com.ib.nsiclassif.db.dao;

import static com.ib.system.utils.SearchUtils.asInteger;
import static com.ib.system.utils.SearchUtils.trimToNULL_Upper;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dto.AdmUser;
import com.ib.indexui.system.Constants;
import com.ib.system.ActiveUser;
import com.ib.system.BaseSystemData;
import com.ib.system.SysConstants;
import com.ib.system.db.AbstractDAO;
import com.ib.system.db.JPA;
import com.ib.system.db.dto.SystemJournal;
import com.ib.system.exceptions.AuthenticationException;
import com.ib.system.exceptions.BaseException;
import com.ib.system.exceptions.DbErrorException;
import com.ib.system.exceptions.RestClientException;
import com.ib.system.rest.SystemRestClient;
import com.ib.system.utils.PasswordUtils;

/**
 * DAO for {@link AdmUser}  Автор  Г.Белев, адаптиран за НСИ - Стоян
 */
public class AdmUserDAO extends AbstractDAO<AdmUser> {

	/**  */
	private static final Logger LOGGER = LoggerFactory.getLogger(AdmUserDAO.class);

	/** @param user */
	public AdmUserDAO(ActiveUser user) {
		super(AdmUser.class, user);
	}
  
	/**
	 * Упдате на AdmUser като му задава новата парола
	 *
	 * @param user
	 * @param newPasswordCrypted
	 * @throws DbErrorException
	 */
//	public void changePassword(AdmUser user, String newPasswordCrypted) throws DbErrorException {
//		LOGGER.debug("changePassword userId={}", user.getId());
//
//		user.setPassword(newPasswordCrypted);
//	
//		user.setUserLastMod(getUserId());
//		user.setDateLastMod(new Date());
//
//		user = merge(user);
//
//		SystemJournal journal = new SystemJournal(user.getCodeMainObject(), user.getId(), user.getIdentInfo());
//
//		journal.setCodeAction(SysConstants.CODE_DEIN_PASS_CHANGE);
//		journal.setDateAction(new Date());
//		journal.setIdUser(getUserId());
//
//		saveAudit(journal);
//	}

	/**
	 * Същото като {@link #changePassword(AdmUser, String)} само че през РЕСТ
	 *
	 * @param userId
	 * @param newPasswordCrypted
	 * @throws RestClientException
	 */
//	public void changePasswordRest(Integer userId, String newPasswordCrypted) throws RestClientException {
//		SystemRestClient.getInstance().postBinary("system/userChangePassword", new Object[] { userId, newPasswordCrypted });
//	}

	/**
	 * Проверява дали ID-то на портребителя го има в таблицата
	 *
	 * @param userId
	 * @return
	 * @throws DbErrorException
	 */
	public boolean checkUserIdExist(Integer userId) throws DbErrorException {
		try {
			Query q = createNativeQuery("SELECT USER_ID FROM ADM_USERS WHERE USER_ID = :userId").setParameter("userId", userId);

			return !q.getResultList().isEmpty();

		} catch (Exception e) {
			throw new DbErrorException(e);
		}
	}

	/**
	 * Проверява дали потребителското име е заето
	 *
	 * @param username
	 * @param userId
	 * @return
	 * @throws DbErrorException
	 */
	public boolean checkUsernameExist(String username, Integer userId) throws DbErrorException {
		username = trimToNULL_Upper(username); // вдигам буквите
		if (username == null) {
			return true; // за да не пуска записа. който го вика все пак трябва да не подава null или празен стринг
		}

		try {
			StringBuilder sql = new StringBuilder("SELECT USER_ID FROM ADM_USERS WHERE UPPER(USERNAME) = :username");

			if (userId != null) {
				sql.append(" AND USER_ID <> :userId");
			}
			Query q = createNativeQuery(sql.toString()).setParameter("username", username);

			if (userId != null) {
				q.setParameter("userId", userId);
			}

			return !q.getResultList().isEmpty();

		} catch (Exception e) {
			throw new DbErrorException(e);
		}
	}

	/**
	 * Намира потребител по потребителско име. Ако няма дава NULL.
	 *
	 * @param username
	 * @return
	 */
	public AdmUser findByUsername(String username) {
		Query query = getEntityManager().createQuery("FROM AdmUser WHERE USERNAME = :UNAME") //
			.setParameter("UNAME", username);

		@SuppressWarnings("unchecked")
		List<AdmUser> users = query.getResultList();

		if (users.isEmpty()) {
			return null;
		}
		return users.get(0);
	}

	/**
	 * Намира правата за достъп до системата на потребителя
	 *
	 * @param userId
	 * @return
	 */
	public Map<Integer, Map<Integer, Boolean>> findUserAccessMap(Integer userId) {
		Map<Integer, Map<Integer, Boolean>> map = new HashMap<>();

		String sql = "select ur.CODE_CLASSIF, ur.CODE_ROLE from ADM_USER_ROLES ur where ur.USER_ID = :UIDP" //
			+ " union" //
			+ " select gr.CODE_CLASSIF, gr.CODE_ROLE from ADM_USER_GROUP ug inner join ADM_GROUP_ROLES gr on gr.GROUP_ID = ug.GROUP_ID where ug.USER_ID = :UIDP"; //

		Query query = getEntityManager().createNativeQuery(sql).setParameter("UIDP", userId);

		@SuppressWarnings("unchecked")
		List<Object[]> roles = query.getResultList();
		for (Object[] role : roles) {
			map.computeIfAbsent(asInteger(role[0]), HashMap::new).put(asInteger(role[1]), Boolean.TRUE);
		}
		return map;
	}

	/**
	 * Намира правата за достъп до системата на потребителя, само за избрани класифиакции
	 *
	 * @param userId
	 * @param selectedClassif 
	 * @return
	 */
	public Map<Integer, Map<Integer, Boolean>> findUserAccessMap(Integer userId, List<Integer> selectedClassif) {
		Map<Integer, Map<Integer, Boolean>> map = new HashMap<>();

		if (selectedClassif == null || selectedClassif.isEmpty()) {
			return map; // явно не се иска нищо
		}
		String sql = "select ur.CODE_CLASSIF, ur.CODE_ROLE from ADM_USER_ROLES ur where ur.USER_ID = :UIDP and ur.CODE_CLASSIF in (:SCL)" //
			+ " union" //
			+ " select gr.CODE_CLASSIF, gr.CODE_ROLE from ADM_USER_GROUP ug inner join ADM_GROUP_ROLES gr on gr.GROUP_ID = ug.GROUP_ID where ug.USER_ID = :UIDP and gr.CODE_CLASSIF in (:SCL)"; //

		Query query = getEntityManager().createNativeQuery(sql).setParameter("UIDP", userId).setParameter("SCL", selectedClassif);

		@SuppressWarnings("unchecked")
		List<Object[]> roles = query.getResultList();
		for (Object[] role : roles) {
			map.computeIfAbsent(asInteger(role[0]), HashMap::new).put(asInteger(role[1]), Boolean.TRUE);
		}
		return map;
	}

	/**
	 * По данните прави проверка дали има такъв потребител и може ли да се логне в системата. Идентификацията е по userId или
	 * username в зависимост от подаденото
	 *
	 * @param systemData
	 * @param userId
	 * @param username
	 * @return
	 * @throws AuthenticationException
	 * @throws BaseException
	 */
	public AdmUser searchInDbByIdOrName(BaseSystemData systemData, Integer userId, String username) throws BaseException {
		AdmUser user = null;
		if (userId != null) {
			user = getEntityManager().find(AdmUser.class, userId);
		} else if (username != null) {
			user = findByUsername(username);
		}
//		if (user == null) {
//			throw new AuthenticationException(AuthenticationException.CODE_USER_UNKNOWN, null);
//		}
//
//		if (!user.getStatus().equals(Constants.CODE_ZNACHENIE_STATUS_ACTIVE)) { // проверка на статус
//			throw new AuthenticationException(AuthenticationException.CODE_UNAUTHORIZED_STATUS, user.getId());
//		}
		if (user!=null) {
			Map<Integer, Map<Integer, Boolean>> accessValues = findUserAccessMap(user.getId());
			user.setAccessValues(accessValues); // задавам ги на ентитито, за да може после да ги дам на усердатата	
		}
		

		return user;
	}

	/**
	 * По данните прави проверка дали има такъв потребител и може ли да се логне в системата
	 *
	 * @param systemData
	 * @param username
	 *  @param isPotrebAct - ако от активната директория е открит потребител и той е активет - true, ако не е активен - false 
	 * @return
	 * @throws AuthenticationException
	 * @throws BaseException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public AdmUser validateUser(BaseSystemData systemData, String username, boolean isPotrebAct )
		throws AuthenticationException, NoSuchAlgorithmException, BaseException, InvalidKeySpecException  {
		AdmUser user = findByUsername(username);

		if (user == null) {
			throw new AuthenticationException(AuthenticationException.CODE_USER_UNKNOWN, null);
		}

	
		if (!isPotrebAct) {
			if (user.getStatus().equals(Constants.CODE_ZNACHENIE_STATUS_ACTIVE)) {
			   user.setStatus(Integer.valueOf(Constants.CODE_ZNACHENIE_STATUS_INACTIVE));
			   user.setStatusDate(new Date());
			   if (user.getUserBlock() != null && user.getUserBlock().intValue() == 1) user.setUserBlock(null);
			   // Актуализация на потребителя в базата
				JPA.getUtil().begin();
				merge(user);
				JPA.getUtil().commit();
			   
			   throw new AuthenticationException(AuthenticationException.CODE_UNAUTHORIZED_STATUS, user.getId());
			}  
		}	

		// Ако потребителят е блокиран
		if (user.getUserBlock() != null && user.getUserBlock().intValue() == 1) { // трябва да се отблокира потребителя след като е намерен и е активен
			user.setUserBlock(null);
			JPA.getUtil().begin();
			merge(user);
			JPA.getUtil().commit();
		
		}
		
		if (!user.getStatus().equals(Constants.CODE_ZNACHENIE_STATUS_ACTIVE)) 
			throw new AuthenticationException(AuthenticationException.CODE_UNAUTHORIZED_STATUS, user.getId());
		
		Map<Integer, Map<Integer, Boolean>> accessValues = findUserAccessMap(user.getId());
		user.setAccessValues(accessValues); 

		return user;
	}
	
	
	
	public AdmUser validateUser(BaseSystemData systemData, String username, String password)
			throws  AuthenticationException, NoSuchAlgorithmException, BaseException, InvalidKeySpecException {
			AdmUser user = findByUsername(username);

			if (user == null) {
				throw new AuthenticationException(AuthenticationException.CODE_USER_UNKNOWN, null);
			}

			if (user.getPassword() == null || user.getPassword().trim().isEmpty() )       // Този потребител е бил въведен чрез LDAP  и за него не е записан PASSWORD в базата
				throw new AuthenticationException(AuthenticationException.CODE_WRONG_PASSWORD, user.getId());
			
			boolean valid = PasswordUtils.validatePassword(password, user.getPassword());
			if (!valid) {
				throw new AuthenticationException(AuthenticationException.CODE_WRONG_PASSWORD, user.getId());
			}
			
			if (!user.getStatus().equals(Constants.CODE_ZNACHENIE_STATUS_ACTIVE)) 
				throw new AuthenticationException(AuthenticationException.CODE_UNAUTHORIZED_STATUS, user.getId());

			// Ако потребителят е блокиран
			if (user.getUserBlock() != null && user.getUserBlock().intValue() == 1) { // трябва да се отблокира потребителя след като е намерен и е активен
				user.setUserBlock(null);
				JPA.getUtil().begin();
				merge(user);
				JPA.getUtil().commit();
			
			}
			
				
			Map<Integer, Map<Integer, Boolean>> accessValues = findUserAccessMap(user.getId());
			user.setAccessValues(accessValues); 

		
			return user;
		}
	
	public boolean checkIfEmailIsNotDuplicate(String email, Integer idUser) {
		Query q = null;
		if(idUser == null)
			q = createNativeQuery("select u.user_id from ADM_USERS u where u.email=?").setParameter(1, email);
		else
			q = createNativeQuery("select u.user_id from ADM_USERS u where u.email=? and u.user_id<>?").setParameter(1, email).setParameter(2, idUser);
    	if(q.getResultList() == null || q.getResultList().isEmpty())
    		return true;
    	else
    		return false;
	}
	
}