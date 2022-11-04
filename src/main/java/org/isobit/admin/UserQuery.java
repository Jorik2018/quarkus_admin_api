package org.isobit.admin;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.isobit.app2.jpa.Role;
import org.isobit.admin.jpa.User;
import org.isobit.app2.jpa.UserRole;
import org.isobit.app2.jpa.UserRolePK;
import org.isobit.directory2.jpa.People;
import org.isobit.util.XUtil;

import java.util.*;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@GraphQLApi
public class UserQuery {
    
    @Inject
    JsonWebToken jwt; 

    @Inject
    private Repository repository;

    @Query("user")
    public User getUser(int uid,String roleName) {
        User user=uid!=0?User.findById(uid):new User();
        if(uid==0)user.setUid(0);
        EntityManager em = User.getEntityManager();
        List<UserRole> userRoles=em.createQuery("select ur from UserRole ur where ur.pk.uid=:uid",UserRole.class).setParameter("uid", user.getUid()).getResultList();
        Map<Object,UserRole> map=new HashMap<Object,UserRole>();
        for(UserRole ur:userRoles){
            ur.setRole(em.find(org.isobit.app2.jpa.Role.class, ur.getPk().getRid()));
            map.put(ur.getPk().getRid(), ur);
        }
        userRoles=new ArrayList<UserRole>();
        if(roleName!=null){
            List<org.isobit.app2.jpa.Role> roles=em.createQuery("SELECT r from Role r where r.name LIKE :roleName",org.isobit.app2.jpa.Role.class)
                .setParameter("roleName", roleName+"%").getResultList();
                for(Role r:roles){
                    UserRole ur=map.get(r.getRid());
                    if(ur==null){
                        ur=new UserRole();
                        ur.setPk(new UserRolePK(user.getUid(),r.getRid()));
                        ur.setRole(r);
                    }
                    userRoles.add(ur);
                }
        }
        if(user.getDirectoryId()!=null){
            user.setPeople(em.find(People.class, user.getDirectoryId()));
        }
        user.setUserRoles(userRoles);
        return user;
    }

    @Query("users")
    @PermitAll
    public Result<User> getUserQuery(int offset, int limit, Integer[] role, String name, String fullName,
            String roleName, String email) {
                int uid=XUtil.intValue(jwt.getClaim("uid"));
        //only user with roles managed by this user
        System.out.println("uid==="+uid);
        EntityManager em = User.getEntityManager();
        boolean useRole = role != null && role.length > 0;
        List<javax.persistence.Query> ql = new ArrayList();
        String sql;
     
        ql.add(em
                .createQuery(
                        "SELECT DISTINCT u " + (sql = "FROM User u,UserRole ur,Role r WHERE r.rid=ur.pk.rid AND u.uid=ur.pk.uid"
                                + (useRole ? " AND ur.pk.rid IN :rid" : "")
                                + (name != null ? " AND UPPER(u.name) LIKE :name" : "")
                                + (email != null ? " AND UPPER(u.mail) LIKE :email" : "")
                                + (roleName != null ? " AND UPPER(r.name) Like :roleName" : "")))
                .setFirstResult(offset)
                .setMaxResults(limit));
        try {
            ql.add(em.createQuery("SELECT COUNT(DISTINCT u) " + sql, Number.class));
            for (javax.persistence.Query q : ql) {
                if (useRole)
                    q.setParameter("rid", Arrays.asList(role));
                if (name != null)
                    q.setParameter("name", '%' + name.toUpperCase() + '%');
                if (email != null)
                    q.setParameter("email", '%' + email.toUpperCase() + '%');
                if (roleName != null)
                    q.setParameter("roleName", '%' + roleName.toUpperCase() + '%');
            }
            List<User> list=ql.get(0).getResultList();
            list.forEach((user)->{
                if(user.getDirectoryId()!=null){
                    user.setPeople(em.find(People.class, user.getDirectoryId()));
                }
            });
            return new Result(list, ((Number) ql.get(1).getSingleResult()).intValue());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}