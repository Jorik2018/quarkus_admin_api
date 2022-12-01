package org.isobit.admin;

import org.isobit.util.Encrypter;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.isobit.admin.Repository;
import org.isobit.admin.jpa.RoleRole;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Context;

import org.isobit.app.jpa.User;
import org.isobit.app.jpa.UserRole;
import org.isobit.app.jpa.UserRolePK;
import org.isobit.directory.jpa.Directory;
import org.isobit.directory.jpa.DocumentType;
import org.isobit.directory.jpa.People;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.security.Principal;
import javax.annotation.security.PermitAll;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Resource {

    @Inject
    private Repository repository;

    @Inject
    private UserService userService;

    @GET
    @Path("role/{from:\\d+}/{to:\\d+}")
    @PermitAll
    public Object getRoles(@PathParam("from") int from,@PathParam("to") int to) {
        org.isobit.app.jpa.User u=userService.getCurrentUser();
        
        EntityManager em=RoleRole.getEntityManager();
        List roleIdList=null;
        if(u.getUid()!=1){
            roleIdList=em.createQuery("SELECT rr.slaveId FROM UserRole ur,RoleRole rr WHERE ur.PK.rid=rr.rid AND ur.PK.uid=:uid",Integer.class)
            .setParameter("uid", u.getUid()).getResultList();
            roleIdList.add(0);
        }
        Query q=em.createQuery("SELECT r FROM Role r"+(roleIdList!=null?" WHERE r.rid IN :roles":"")+" ORDER BY r.name");
        if(roleIdList!=null)
            q.setParameter("roles",roleIdList);
        return q.getResultList();
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
        org.isobit.app.jpa.User cu=userService.getCurrentUser();
        if(pass!=null&&pass.length()>0){
            pass=new Encrypter().encode(Encrypter.MD5,pass);
        }else pass=null;
        EntityManager em=RoleRole.getEntityManager();
        People people=user.getPeople();
        if(people!=null){

            if(user.getDirectoryId()==null){
                Directory directory=new Directory();
                directory.setDateinsert(new Date());
                directory.setPspApp(0);
                directory.setTypeId((short)1);
                directory.setPspCxt((short)0);

                directory.setPspUid(cu.getUid());
                em.persist(directory);
                people.setDirectory(directory);
                people.setId(directory.getId());
                people.setDocumentType(em.find(DocumentType.class,(short)4));
                people.setStatus('A');
                people.setFechaIng(new Date());
                people.setFullName(people.getFirstSurname()+" "+people.getLastSurname()+" "+people.getFullName());
                em.persist(people);
                user.setDirectoryId(people.getId());
            }else{
                //the info can be changed by owner or user withy admin permision
                if(cu.getUid().equals(user.getUid())){
                    People cp=em.find(People.class,user.getDirectoryId());
                    cp.setNames(people.getNames());
                    cp.setFirstSurname(people.getFirstSurname());
                    cp.setLastSurname(people.getLastSurname());
                    cp.setSex(people.getSex());
                    cp.setAddress(people.getAddress());
                    cp.setFullName(cp.getFirstSurname()+" "+cp.getLastSurname()+" "+cp.getNames());
                    em.merge(cp);
                }
            }
        }

        if (user.getUid() == null){
            user.setCreated((int) (new java.util.Date().getTime() / 1000));
            em.persist(user);
        }else{
            if(/*can("admin_users")*/true||cu.getUid().equals(user.getUid())){
                User _user =em.find(User.class,user.getUid());
                if(pass!=null)_user.setPass(pass);
                _user.setStatus(user.getStatus());
                _user.setMail(user.getMail());
                _user.setPeople(user.getPeople());
                _user.setName(user.getName());
                _user.setDirectoryId(user.getDirectoryId());
                _user.setUserRoles(user.getUserRoles());
                em.merge(_user);
                user=_user;
            }
        }

        java.util.Collection<UserRole> userRoles=user.getUserRoles();
        if(userRoles!=null){
            for(UserRole role:userRoles){
                UserRolePK pk=role.getPK();
                pk.setUid(user.getUid());
                UserRole userRole=em.find(UserRole.class,pk);
                if(userRole==null)userRole=new UserRole();
                userRole.setActive(role.isActive());
                if(userRole.getPK()==null){
                    userRole.setPK(pk);
                    em.persist(userRole);
                }else
                    em.merge(userRole);
            }
        }
        return user;
    }

}
