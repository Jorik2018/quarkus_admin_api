package org.isobit.admin;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.isobit.admin.jpa.RoleRole;
import org.isobit.app.jpa.Role;
import org.isobit.app.jpa.User;
import org.isobit.app.jpa.UserRole;
import org.isobit.app.jpa.UserRolePK;
import org.isobit.directory.jpa.People;
import org.isobit.util.XUtil;

import java.util.*;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@GraphQLApi
public class UserQuery {

    @Inject
    private UserService userService;

    @Inject
    private Repository repository;

    @Inject
    JsonWebToken jwt;

    @Query("user")
    @PermitAll
    public User getUser(Optional<Integer> uid, String roleName) {
        Integer _uid = 0;
        User currentUser = userService.getCurrentUser();
        EntityManager em = RoleRole.getEntityManager();
        if (uid.isPresent())
            _uid = uid.get();
        else {
            _uid = currentUser.getUid();
        }
        if(roleName==null)roleName="";
        User user = _uid != 0 ? em.find(User.class, _uid) : new User();
        if (user.getUid() == null)
            user.setUid(0);
            //List all roles from user changed it to list only accesibles roles by user
        List<UserRole> userRoles = em.createQuery("select ur from UserRole ur where ur.PK.uid=:uid", UserRole.class)
                .setParameter("uid", user.getUid()).getResultList();
        Map<Object, UserRole> map = new HashMap<Object, UserRole>();
        for (UserRole ur : userRoles) {
            ur.setRole(em.find(org.isobit.app.jpa.Role.class, ur.getPK().getRid()));
            map.put(ur.getPK().getRid(), ur);
        }
        userRoles = new ArrayList<UserRole>();
        List roleIdList = null;
        if (currentUser.getUid() != 1) {
            roleIdList = em
                    .createQuery(
                            "SELECT rr.slaveId FROM UserRole ur,RoleRole rr WHERE ur.PK.rid=rr.rid AND ur.PK.uid=:uid",
                            Integer.class)
                    .setParameter("uid", currentUser.getUid() ).getResultList();
            roleIdList.add(0);
        }

        javax.persistence.Query q = em
                .createQuery(
                        "SELECT r from Role r WHERE r.name LIKE :roleName"
                                + (roleIdList != null ? " AND r.rid IN :roles" : "") + " ORDER BY r.name",
                        org.isobit.app.jpa.Role.class)
                .setParameter("roleName", roleName + "%");

        if (roleIdList != null) {
            q.setParameter("roles", roleIdList);
        }
        List<org.isobit.app.jpa.Role> roles = q.getResultList();
        for (Role r : roles) {
            UserRole ur = map.get(r.getRid());
            if (ur == null) {
                ur = new UserRole();
                ur.setPK(new UserRolePK(user.getUid(), r.getRid()));
                ur.setRole(r);
            }
            userRoles.add(ur);
        }

        if (user.getDirectoryId() != null) {
            user.setPeople(em.find(org.isobit.directory.jpa.People.class, user.getDirectoryId()));
        }

        user.setUserRoles(userRoles);
        return user;
    }

    @Query("users")
    @PermitAll
    public Result<User> getUserQuery(int offset, int limit, Integer[] role, String name, String fullName,
            String roleName, String email) {
        int uid = XUtil.intValue(jwt.getClaim("uid"));
        EntityManager em = RoleRole.getEntityManager();
        boolean useRole = role != null && role.length > 0;
        List<javax.persistence.Query> ql = new ArrayList();
        String sql;
        List roleIdList = null;
        if (uid != 1) {
            roleIdList = em
                    .createQuery(
                            "SELECT rr.slaveId FROM UserRole ur,RoleRole rr WHERE ur.PK.rid=rr.rid AND ur.PK.uid=:uid",
                            Integer.class)
                    .setParameter("uid", uid).getResultList();
            roleIdList.add(0);
        }
        System.out.println("rl=" + roleIdList);
        ql.add(em
                .createQuery(
                        "SELECT DISTINCT u "
                                + (sql = "FROM User u,UserRole ur,Role r WHERE r.rid=ur.PK.rid AND u.uid=ur.PK.uid"
                                        + (useRole ? " AND ur.PK.rid IN :rid" : "")
                                        + (name != null ? " AND UPPER(u.name) LIKE :name" : "")
                                        + (email != null ? " AND UPPER(u.mail) LIKE :email" : "")
                                        + (roleIdList != null ? " AND ur.PK.rid IN :roles" : "")
                                        + (roleName != null ? " AND UPPER(r.name) Like :roleName" : "")))
                .setFirstResult(offset)
                .setMaxResults(limit));
        try {
            ql.add(em.createQuery("SELECT COUNT(DISTINCT u) " + sql, Number.class));
            for (javax.persistence.Query q : ql) {
                if (useRole)
                    q.setParameter("rid", Arrays.asList(role));
                if (roleIdList != null)
                    q.setParameter("roles", roleIdList);

                if (name != null)
                    q.setParameter("name", '%' + name.toUpperCase() + '%');
                if (email != null)
                    q.setParameter("email", '%' + email.toUpperCase() + '%');
                if (roleName != null)
                    q.setParameter("roleName", '%' + roleName.toUpperCase() + '%');
            }
            List<User> list = ql.get(0).getResultList();
            list.forEach((user) -> {
                if (user.getDirectoryId() != null) {
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