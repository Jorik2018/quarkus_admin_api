package org.isobit.directory2.jpa;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "drt_directorio")
public class Directory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @EqualsAndHashCode.Include()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dir")
    private Integer idDir;
    @Basic(optional = false)
    @Column(name = "psp_cxt")
    private short pspCxt;
    @Basic(optional = true)
    @Column(name = "psp_app")
    private int pspApp;
    @Basic(optional = true)
    @Column(name = "psp_uid")
    private Integer pspUid;
    @Basic(optional = true)
    @Column(name = "dateinsert", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateinsert;

}
