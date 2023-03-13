package com.ib.nsiclassif.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.faces.annotation.FacesConfig;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;

import com.ib.nsiclassif.system.UserData;
import com.ib.system.IBUserPrincipal;
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.xhtml",
                useForwardToLogin = false
            )
)
@FacesConfig
@ApplicationScoped
public class ApplicationConfig {

	@Inject
    private SecurityContext securityContext;
	
	@Produces
	@Named("userData")
	UserData getUserData() {
		IBUserPrincipal up = ((IBUserPrincipal)securityContext.getCallerPrincipal());
		if(up!=null) {
			return (UserData) up.getUserData();
		} else {
			return new UserData(-100 ,"external" ,"external");
		}
	}

	
}



