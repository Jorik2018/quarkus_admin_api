package org.isobit.admin;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.isobit.admin.jpa.User;
import org.isobit.app.jpa.Role;
import org.isobit.app.jpa.UserRole;
import org.isobit.app.jpa.UserRolePK;

import java.util.*;
import org.isobit.admin.Repository;
import java.util.Optional;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@GraphQLApi
public class UserQuery {

    @Inject
    private Repository repository;

    @Query("test")
    public List<User> gg(){
        ArrayList l=new ArrayList();
        User u=new User();
        l.add(u);
        return l;
    }


    @Query("user")
    public User getUser(int uid,String roleName) {
        User user=User.findById(uid);
        EntityManager em = User.getEntityManager();
        List<UserRole> userRoles=em.createQuery("select ur from UserRole ur where ur.pk.uid=:uid").setParameter("uid", user.getUid()).getResultList();
        Map<Object,UserRole> map=new HashMap();
        for(UserRole ur:userRoles){
            ur.setRole(em.find(org.isobit.app.jpa.Role.class, ur.getPk().getRid()));
            map.put(ur.getPk().getRid(), ur);
        }
        userRoles=new ArrayList();
        if(roleName!=null){
            List<org.isobit.app.jpa.Role> roles=em.createQuery("SELECT r from Role r where r.name LIKE :roleName")
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
        user.setUserRoles(userRoles);
        return user;
    }

    @Query("users")
    public Result<User> getUserQuery(int offset, int limit, Integer[] role, String name, String fullName,
            String roleName, String email) {
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
            return new Result(ql.get(0).getResultList(), ((Number) ql.get(1).getSingleResult()).intValue());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}