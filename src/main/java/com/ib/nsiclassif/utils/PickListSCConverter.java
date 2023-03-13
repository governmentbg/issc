package com.ib.nsiclassif.utils;
import java.util.StringTokenizer;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.ib.system.db.dto.SystemClassif;

@FacesConverter("pickListSCConverter")
public class PickListSCConverter implements Converter<Object> {
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if(value!=null){
		
			StringTokenizer scItem = new StringTokenizer(value, "@@");
			try {					
				return new SystemClassif(Integer.parseInt(scItem.nextToken()), Integer.parseInt(scItem.nextToken()), scItem.nextToken(), null);
			} catch (Exception cce) {
            	SystemClassif newSC =  new SystemClassif();
            	if(value.equals("0@@0@@")) { //ako e now element
            		newSC.setTekst("");
            	} else {
            		newSC.setTekst(value);
            	}
            	return newSC; // това се случва, ако се добави ново значение!
            }	
			
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object object) {
		if(object != null) {	
			if(object instanceof SystemClassif ){
				SystemClassif scItem = (SystemClassif) object;
				 return scItem.getCodeClassif()+"@@"+scItem.getCode()+"@@"+scItem.getTekst(); 
				
			} else {
				return String.valueOf(object.toString());
			}
        }
        return null;
    }
	
}

