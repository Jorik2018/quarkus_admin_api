package org.isobit.admin;

import org.isobit.util.Encrypter;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.isobit.admin.Repository;
import org.isobit.admin.model.RoleRole;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;

import org.isobit.app.model.User;
import org.isobit.app.model.UserRole;
import org.isobit.app.model.UserRolePK;
import org.isobit.app.service.UserService;
import org.isobit.directory.model.Directory;
import org.isobit.directory.model.DocumentType;
import org.isobit.directory.model.People;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.security.Principal;
import jakarta.annotation.security.PermitAll;

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
    public Object getRoles(@PathParam("from") int from, @PathParam("to") int to) {
        org.isobit.app.model.User u = userService.getCurrentUser();

        EntityManager em = RoleRole.getEntityManager();
        List roleIdList = null;
        if (u.getUid() != 1) {
            roleIdList = em
                    .createQuery(
                            "SELECT rr.slaveId FROM UserRole ur,RoleRole rr WHERE ur.PK.rid=rr.rid AND ur.PK.uid=:uid",
                            Integer.class)
                    .setParameter("uid", u.getUid()).getResultList();
            roleIdList.add(0);
        }
        Query q = em.createQuery(
                "SELECT r FROM Role r" + (roleIdList != null ? " WHERE r.rid IN :roles" : "") + " ORDER BY r.name");
        if (roleIdList != null)
            q.setParameter("roles", roleIdList);
        return q.getResultList();
    }

    @GET
    @Path("user/{from:\\d+}/{to:\\d+}")
    public Object getUsers(
            @PathParam("from") Integer from,
            @PathParam("to") Integer to,
            @QueryParam("name") String name,
            @QueryParam("people") String people,
            @QueryParam("mail") String mail) {

        EntityManager em = RoleRole.getEntityManager();

        StringBuilder jpql = new StringBuilder(
                "SELECT u FROM User u ");

        StringBuilder countJpql = new StringBuilder(
                "SELECT COUNT(u) FROM User u ");

        /*
         * Solo necesitamos People cuando se filtra por persona.
         */
        if (!isEmpty(people)) {
            jpql.append(", People p ");
            countJpql.append(", People p ");
        }

        jpql.append("WHERE 1=1 ");
        countJpql.append("WHERE 1=1 ");

        if (!isEmpty(people)) {
            jpql.append(
                    "AND u.idDir = p.id " +
                            "AND UPPER(p.fullName) LIKE :people ");

            countJpql.append(
                    "AND u.idDir = p.id " +
                            "AND UPPER(p.fullName) LIKE :people ");
        }

        if (!isEmpty(name)) {
            jpql.append(
                    "AND UPPER(u.name) LIKE :name ");

            countJpql.append(
                    "AND UPPER(u.name) LIKE :name ");
        }

        if (!isEmpty(mail)) {
            jpql.append(
                    "AND UPPER(u.mail) LIKE :mail ");

            countJpql.append(
                    "AND UPPER(u.mail) LIKE :mail ");
        }

        jpql.append("ORDER BY u.name");

        Query query = em.createQuery(
                jpql.toString());

        Query countQuery = em.createQuery(
                countJpql.toString());

        /*
         * Parámetros
         */

        if (!isEmpty(name)) {

            String value = toLike(name);

            query.setParameter("name", value);
            countQuery.setParameter("name", value);
        }

        if (!isEmpty(mail)) {

            String value = toLike(mail);

            query.setParameter("mail", value);
            countQuery.setParameter("mail", value);
        }

        if (!isEmpty(people)) {

            String value = toLike(people);

            query.setParameter("people", value);
            countQuery.setParameter("people", value);
        }

        if (to > 0) {
            query.setFirstResult(from);
            query.setMaxResults(to);
        }

        @SuppressWarnings("unchecked")
        List<User> users = query.getResultList();

        long total = ((Number) countQuery.getSingleResult()).longValue();

        users.forEach(user -> {
            em.detach(user);
            user.setPass(null);
        });

        /*
         * Resolver People asociados.
         */

        List<Integer> peopleIds = users.stream()
                .map(User::getDirectoryId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());

        if (!peopleIds.isEmpty()) {

            @SuppressWarnings("unchecked")
            List<Object[]> peopleRows = em.createQuery(
                    "SELECT p.id, p.fullName " +
                            "FROM People p " +
                            "WHERE p.id IN :ids")
                    .setParameter(
                            "ids",
                            peopleIds)
                    .getResultList();

            Map<Object, String> peopleMap = new HashMap<>();

            for (Object[] row : peopleRows) {
                peopleMap.put(
                        row[0],
                        (String) row[1]);
            }

            users.forEach(user -> {
                //user.setPass(null);
                String fullName = peopleMap.get(user.getDirectoryId());
                People p = new People();
                if (fullName != null) {
                    p.setFullName(fullName);
                    user.setPeople(p);
                }
            });
        }
        Map<String, Object> response = new HashMap<>();

        response.put("data", users);
        response.put("total", total);
        return response;
    }

    @PermitAll
    @GET
    @Path("token")
    public Object getFree(@Context SecurityContext sec) {
        System.out.println("22222");
        Principal user = sec.getUserPrincipal();
        String name = user != null ? user.getName() : "anonymous";
        return name;
    }

    @POST
    @Path("user")
    @jakarta.transaction.Transactional
    public Object postUser(User user) {
        String pass = user.getPass();
        org.isobit.app.model.User cu = userService.getCurrentUser();
        if (pass != null && pass.length() > 0) {
            pass = new Encrypter().encode(Encrypter.MD5, pass);
        } else
            pass = null;
        EntityManager em = RoleRole.getEntityManager();
        People people = user.getPeople();
        if (people != null) {

            if (user.getDirectoryId() == null) {
                Directory directory = new Directory();
                directory.setDateinsert(new Date());
                directory.setPspApp(0);
                directory.setTypeId((short) 1);
                directory.setPspCxt((short) 0);

                directory.setPspUid(cu.getUid());
                em.persist(directory);
                people.setDirectory(directory);
                people.setId(directory.getId());
                people.setDocumentType(em.find(DocumentType.class, (short) 4));
                people.setStatus('A');
                people.setFechaIng(new Date());
                people.setFullName(
                        people.getFirstSurname() + " " + people.getLastSurname() + " " + people.getFullName());
                em.persist(people);
                user.setDirectoryId(people.getId());
            } else {
                // the info can be changed by owner or user withy admin permision
                if (cu.getUid().equals(user.getUid())) {
                    People cp = em.find(People.class, user.getDirectoryId());
                    cp.setNames(people.getNames());
                    cp.setFirstSurname(people.getFirstSurname());
                    cp.setLastSurname(people.getLastSurname());
                    cp.setSex(people.getSex());
                    cp.setAddress(people.getAddress());
                    cp.setFullName(cp.getFirstSurname() + " " + cp.getLastSurname() + " " + cp.getNames());
                    em.merge(cp);
                }
            }
        }

        if (user.getUid() == null) {
            user.setCreated((int) (new java.util.Date().getTime() / 1000));
            em.persist(user);
        } else {
            if (/* can("admin_users") */true || cu.getUid().equals(user.getUid())) {
                User _user = em.find(User.class, user.getUid());
                if (pass != null)
                    _user.setPass(pass);
                _user.setStatus(user.getStatus());
                _user.setMail(user.getMail());
                _user.setPeople(user.getPeople());
                _user.setName(user.getName());
                _user.setDirectoryId(user.getDirectoryId());
                _user.setUserRoles(user.getUserRoles());
                em.merge(_user);
                user = _user;
            }
        }

        java.util.Collection<UserRole> userRoles = user.getUserRoles();
        if (userRoles != null) {
            for (UserRole role : userRoles) {
                UserRolePK pk = role.getPK();
                pk.setUid(user.getUid());
                UserRole userRole = em.find(UserRole.class, pk);
                if (userRole == null)
                    userRole = new UserRole();
                userRole.setActive(role.isActive());
                if (userRole.getPK() == null) {
                    userRole.setPK(pk);
                    em.persist(userRole);
                } else
                    em.merge(userRole);
            }
        }
        return user;
    }

    private boolean isEmpty(String value) {
        return value == null ||
                value.trim().isEmpty();
    }

    private String toLike(String value) {
        return "%" +
                value.trim()
                        .toUpperCase()
                        .replace(" ", "%")
                +
                "%";
    }
}
