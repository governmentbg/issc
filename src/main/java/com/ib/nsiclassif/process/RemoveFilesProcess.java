package com.ib.nsiclassif.process;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dao.RelationDAO;
import com.ib.nsiclassif.system.SystemData;
import com.ib.system.ActiveUser;
import com.ib.system.db.DialectConstructor;
import com.ib.system.db.JPA;
import com.ib.system.exceptions.DbErrorException;

public class RemoveFilesProcess implements ServletContextListener, Runnable{
	static final Logger LOGGER = LoggerFactory.getLogger(RemoveFilesProcess.class);
	private ScheduledExecutorService scheduler;
	
	// Tova pokazva prez kolko vreme she hodim da proverqvame za izmeneniq po klasifikaciite
	private long minInterval=5;
	
	public void contextInitialized(ServletContextEvent event) {
		LOGGER.debug("Trying to initialize RemoveFilesProcess Scheduler...");
		 
		String path="";
		try {
			path = new SystemData().getSettingsValue("FilesSaveLocation")+"/start.process";
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File f=new File(path);
		if (f.exists()) {
			scheduler = Executors.newSingleThreadScheduledExecutor();
		    scheduler.scheduleAtFixedRate(this, 0l, minInterval, TimeUnit.MINUTES);	
		}else {
			LOGGER.debug("FILE 'start.process' does not exist so no removing of exports needed!");
		}
		

	}

	public void contextDestroyed(ServletContextEvent event) {
		 LOGGER.debug("Trying to stop RemoveFilesProcess Scheduler ...");
		if (scheduler!=null) {
			//spirame samo ako e puskan
			scheduler.shutdownNow();
		}
	}

	public void sessionCreated(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void sessionDestroyed(HttpSessionEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		try {
			LOGGER.info("Running RemoveFilesProcess: "+new Date());
			/* Generirane na paketi */
 
			LOGGER.debug("Start files removing!!!");
			 
			List<Object[]> rez=new RelationDAO(ActiveUser.DEFAULT).findLastChanges();
			 
			String path="";
			try {
				path = new SystemData().getSettingsValue("FilesSaveLocation");
			} catch (DbErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

			for (Object[] objects : rez) {
				if (objects!=null && objects[0].equals("v")) {
					new ClassificationDAO(ActiveUser.DEFAULT).deleteExportedFiles(path+"/version/"+objects[1]);
				}else {
					new ClassificationDAO(ActiveUser.DEFAULT).deleteExportedFiles(path+"/coresp/"+objects[1]);
				}
			}
			
			// пускаме и update на дата-та за да знаем за следващия път коя дата ще ползваме.
			String dialect = JPA.getUtil().getDbVendorName();
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE ");
			sql.append("    SYSTEM_OPTIONS ");
			sql.append(" SET ");
			sql.append("    OPTION_VALUE = "+DialectConstructor.convertDateToSQLString(dialect, new Date()));
			sql.append(" WHERE ");
			sql.append("    SYSTEM_OPTIONS.OPTION_LABEL='nsi_last_time_exports_check'");
			
			JPA.getUtil().begin();
				JPA.getUtil().getEntityManager().createNativeQuery(sql.toString()).executeUpdate();
			JPA.getUtil().commit();
			
			LOGGER.debug("End files removing!!!");
		} catch (DbErrorException e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
		} catch (Exception e) {
			JPA.getUtil().rollback();
			e.printStackTrace();
		}finally {
			JPA.getUtil().closeConnection();
		}
			
		 
		
	}

}
