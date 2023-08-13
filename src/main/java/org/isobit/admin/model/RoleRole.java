package org.isobit.admin.model;

import java.io.Serializable;
import java.util.Collection;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "dru_role_role")
public class RoleRole extends PanacheEntityBase implements Serializable{

    private static final long serialVersionUID = 1L;
    @Id
    @EqualsAndHashCode.Include()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic(optional = false)
    private Integer rid;
    
    @Basic(optional = false)
    @Column(name =  "slave_id")
    private Integer slaveId;

}
