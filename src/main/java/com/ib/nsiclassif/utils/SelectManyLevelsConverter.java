package com.ib.nsiclassif.utils;

import java.util.StringTokenizer;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.ib.nsiclassif.db.dao.LevelDAO;
import com.ib.nsiclassif.db.dto.Level;
import com.ib.system.exceptions.DbErrorException;

@FacesConverter("levelsConverter")
public class SelectManyLevelsConverter implements Converter<Object> {
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		try {
			return new LevelDAO(null).findById(Long.valueOf(value));
		} catch (NumberFormatException | DbErrorException e) {
			throw new ConverterException(new FacesMessage(value + " is not a valid Level ID"), e);
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object object) {
		if (object == null) {
			return "";
		}
		if (object instanceof Level) {
			Level lev = (Level) object;
			return String.valueOf(lev.getId());
		} else {
			throw new ConverterException(new FacesMessage(object + " is not a valid level"));
		}

	}
	
}
