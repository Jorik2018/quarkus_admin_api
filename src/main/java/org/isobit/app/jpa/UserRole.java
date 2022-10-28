package org.isobit.app.jpa;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Basic;
import javax.persistence.Transient;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "dru_users_roles")
public class UserRole implements Serializable {
    
    private static final long serialVersionUID = 2L;
    
    @EmbeddedId
    @EqualsAndHashCode.Include()
    protected UserRolePK pk;

    @Basic(optional = false)
    private boolean active;
    @Transient
    private Role role;
}
