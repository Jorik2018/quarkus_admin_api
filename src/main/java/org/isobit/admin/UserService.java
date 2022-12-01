package org.isobit.admin;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.isobit.app.jpa.User;
import org.isobit.util.XUtil;

@Transactional
@ApplicationScoped
public class UserService {

    @Inject
    JsonWebToken jwt;
	
	public User getCurrentUser() {
		User user=new User();
		user.setUid(XUtil.intValue(jwt.getClaim("uid")));
		if(jwt.containsClaim("directory"))
			user.setDirectoryId(XUtil.intValue(jwt.getClaim("directory")));
		return user;
	}

}
