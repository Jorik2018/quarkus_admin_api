package org.isobit.admin;

import org.isobit.util.Encrypter;
import org.isobit.admin.Repository;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.isobit.admin.jpa.User;
import org.isobit.app.jpa.Role;
import org.isobit.app.jpa.UserRole;
import org.isobit.app.jpa.UserRolePK;
import javax.persistence.EntityManager;
import java.util.List;

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
        java.util.Collection<UserRole> roles=user.getUserRoles();
        EntityManager em = User.getEntityManager();
        if(roles!=null){
            for(UserRole role:roles){
                UserRolePK pk=role.getPk();
                pk.setUid(user.getUid());
                UserRole userRole=em.find(UserRole.class,pk);
                if(userRole==null)userRole=new UserRole();
                userRole.setCanceled(role.isCanceled());
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
