package com.ib.nsiclassif.integration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ib.nsiclassif.db.dao.ClassificationDAO;
import com.ib.nsiclassif.db.dao.CorespTableDAO;
import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dao.RelationDAO;
import com.ib.nsiclassif.db.dao.VersionDAO;
import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.CorespTable;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.system.ActiveUser;
import com.ib.system.exceptions.DbErrorException;

@Path("/data")
public class NSIRestApiData {

	/**
	 * listClassif
	 * @return връща списък от всички налични класификации
	 */
	@GET
	@Path("/listClassif")
	@Produces({MediaType.APPLICATION_JSON})
	public Response listClassif() {
		
		try {
			List<Classification> allClass = new ClassificationDAO(ActiveUser.DEFAULT).findAll();
			List<Object[]> rez=new ArrayList<Object[]>();
			for (int i = 0; i < allClass.size(); i++) {
				Object[] o=new Object[2];
				o[0]=allClass.get(i).getId();
				for (Integer key : allClass.get(i).getLangMap().keySet()) {
					o[1]=allClass.get(i).getLangMap().get(key).getNameClassif();
					break;
				}
				rez.add(o);
			}
			return Response.ok().entity(Entity.json(rez)).build();	
		} catch (DbErrorException e) {
			return Response.serverError().entity("Грешка при извличане на данни!").build();
		}
		
		
	}
/**
 * 
 * @param classif - id  (от listClassif може да се вземе id-на желатана класификация)
 * @return Връща всички версии за избраната класификация
 */
	@GET
	@Path("/listVersions/{classif}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response listVersions(@PathParam("classif") String classif) {
		
		try {
			ArrayList<Object[]> allVer = new VersionDAO(ActiveUser.DEFAULT).getClassifVersions(Integer.valueOf(classif),1);
			return Response.ok().entity(Entity.json(allVer)).build();	
		} catch (DbErrorException e) {
			return Response.serverError().entity("Грешка при извличане на данни!").build();
		}
		
	}
	
	/**
	 * 
	 * @param version - id (от listVersions може да се вземе id-то) 
	 * @return Връща позициите с техните данни за избраната версия
	 */
	@GET
	@Path("/classifData/{version}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response classifData(@PathParam("version") String version) {
		try {
			List<PositionS> allPos = new PositionSDAO(ActiveUser.DEFAULT).loadScheme(Integer.valueOf(version),null,1,null,1);
			return Response.ok().entity(Entity.json(allPos)).build();	
		} catch (DbErrorException e) {
			return Response.serverError().entity("Грешка при извличане на данни!").build();
		}
		
	}
	/**
	 * 
	 * @param version - id версия източник (от listVersions може да се вземе id-то)
	 * @return Връща списък от всички кореспондиращи таблици за избраната версия източник
	 */
	@GET
	@Path("/listCorespTablesSource/{version}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response listCorespTablesSource(@PathParam("version") String version) {
		
		try {
			ArrayList<Object[]> allCorespTables = new CorespTableDAO(ActiveUser.DEFAULT).getCorespTablesByVersions(Integer.valueOf(version));
			return Response.ok().entity(Entity.json(allCorespTables)).build();	
		} catch (DbErrorException e) {
			return Response.serverError().entity("Грешка при извличане на данни!").build();
		}
		
	}
	/**
	 * 
	 * @param coresptalble - id на кореспондираща таблица (от listCorespTablesSource може да се вземе id-to)
	 * @return Връща релациите за избраната кореспондираща таблица
	 */
	@GET
	@Path("/relations/{coresptalble}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response reltions(@PathParam("coresptalble") String coresptalble) {
		try {
			List<Object[]> allRel = new RelationDAO(ActiveUser.DEFAULT).loadRelationsForExport(Integer.valueOf(coresptalble));
			return Response.ok().entity(Entity.json(allRel)).build();	
		} catch (DbErrorException e) {
			return Response.serverError().entity("Грешка при извличане на данни!").build();
		}
		
	}
	
}
