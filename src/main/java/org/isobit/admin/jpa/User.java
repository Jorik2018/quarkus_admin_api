package org.isobit.admin.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.isobit.app.jpa.UserRole;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "dru_users")
public class User extends PanacheEntityBase implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @EqualsAndHashCode.Include()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "uid")
    private Integer uid;
    @Column(name = "id_dir")
    private Integer directoryId;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "signature_format")
    private short signature_format = 0;
    @Basic(optional = false)
    @Column(name = "mail")
    private String mail;
    @Basic(optional = false)
    @Column(name = "pass")
    private String pass;
    @Column(name = "pass_strong")
    private String passStrong;
    @Basic(optional = false)
    @Column(name = "sort")
    private short sort = 0;
    @Basic(optional = false)
    @Column(name = "_mode")
    private Short mode = 0;
    @Basic(optional = false)
    @Column(name = "threshold")
    private short threshold = 0;
    @Basic(optional = false)
    @Column(name = "theme")
    private String theme = "";
    @Column(name = "language")
    private String language = "";
    @Basic(optional = false)
    @Column(name = "signature")
    private String signature = "";
    @Basic(optional = false)
    @Column(name = "status")
    private short status;
    @Column(name = "timezone")
    private String timezone = "";
    @Basic(optional = false)
    @Column(name = "picture")
    private String picture = "";
    @Basic(optional = true)
    @Column(name = "init")
    private String init = "";
    @Basic(optional = true)
    @Column(name = "created")
    private int created;
    @Basic(optional = true)
    @Column(name = "dependency_id")
    private Integer dependencyId;
    @Basic(optional = true)
    @Column(name = "access")
    private Integer access;
    @Basic(optional = false)
    @Column(name = "_login")
    private Integer login=0;
    @Transient
    private String fullName;
    @Transient
    private String names;
    @Transient
    private String firstSurname;
    @Transient
    private String lastSurname;
    @Transient
    private String confirm;
    @Transient
    private Collection<UserRole> userRoles;

    /*@Override
    public void valueBound(HttpSessionBindingEvent event) {
        Set<User> logins = (Set<User>) event.getSession().getServletContext().getAttribute("logins");
        if (logins == null) {
            event.getSession().getServletContext().setAttribute("logins", logins = new HashSet<User>());
        }
        logins.add(this);
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        Set<User> logins = (Set<User>) event.getSession().getServletContext().getAttribute("logins");
        logins.remove(this);
    }*/

}
