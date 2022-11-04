package org.isobit.admin;

import org.isobit.util.Encrypter;
import org.isobit.admin.Repository;
import org.isobit.admin.jpa.User;

import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Context;
import org.isobit.app2.jpa.UserRole;
import org.isobit.app2.jpa.UserRolePK;
import javax.persistence.EntityManager;

import java.security.Principal;
import javax.annotation.security.PermitAll;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Resource {

    @Inject
    private Repository repository;

    @GET
    @Path("user/{uid:\\d+}")
    public Object getUser(@PathParam("uid") int uid) {
        User user = User.findById(uid);
        EntityManager em = User.getEntityManager();
        user.setUserRoles(em.createQuery("select ur from UserRole ur where ur.pk.uid=:uid").setParameter("uid", user.getUid()).getResultList());
        return user;
    }

    @PermitAll
    @GET
    @Path("token")
    public Object getFree (@Context SecurityContext sec) {
        Principal user = sec.getUserPrincipal(); 
        String name = user != null ? user.getName() : "anonymous";
        return name;
    }

    @POST
    @Path("user")
    @javax.transaction.Transactional
    public Object postUser(User user) {
        String pass=user.getPass();
        if(pass!=null&&pass.length()>0){
            pass=new Encrypter().encode(Encrypter.MD5,pass);
        }else pass=null;

        if (user.getUid() == null){
            user.setCreated((int) (new java.util.Date().getTime() / 1000));
            repository.persist(user);
        }else{
            User _user = User.findById(user.getUid());
            if(pass!=null)_user.setPass(pass);
            _user.setStatus(user.getStatus());
            _user.setMail(user.getMail());
            _user.setName(user.getName());
            _user.setUserRoles(user.getUserRoles());
            User.getEntityManager().merge(_user);
            user=_user;
        }
        java.util.Collection<UserRole> userRoles=user.getUserRoles();
        EntityManager em = User.getEntityManager();
        if(userRoles!=null){
            for(UserRole role:userRoles){
                UserRolePK pk=role.getPk();
                pk.setUid(user.getUid());
                UserRole userRole=em.find(UserRole.class,pk);
                if(userRole==null)userRole=new UserRole();
                userRole.setActive(role.isActive());
                if(userRole.getPk()==null){
                    userRole.setPk(pk);
                    em.persist(userRole);
                }else
                    em.merge(userRole);
            }
        }
        return user;
    }

}
