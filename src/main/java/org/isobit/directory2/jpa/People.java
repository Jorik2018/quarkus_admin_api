package org.isobit.directory2.jpa;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "drt_personanatural")
public class People implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @EqualsAndHashCode.Include()
    @Basic(optional = false)
    @Column(name = "id_dir")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "ap_materno")
    private String firstSurname = "";
    @Basic(optional = false)
    @Column(name = "ap_paterno")
    private String lastSurname = "";
    @Basic(optional = false)
    @Column(name = "nombre")
    private String names = "";
    @Basic(optional = false)
    @Column(name = "sexo")
    private char sex = ' ';
    @Column(name = "fecha_nac")
    @Temporal(TemporalType.DATE)
    private Date birthdate;
    @Basic(optional = false)
    @Column(name = "estado_pernat")
    private char status = '1';
    @Basic(optional = false)
    @Column(name = "fecha_ing")
    @Temporal(TemporalType.DATE)
    private Date fechaIng;
    @Column(name = "direccion")
    private String address = "";
    @Column(name = "observacion")
    private String observacion = "";
    @Column(name = "numero_pndid")
    private String code = "";
    @Column(name = "id_ubg_nac")
    private Integer idUbgNac;
    @Column(name = "id_ubg_pro")
    private Integer idUbgPro;
    @Column(name = "id_pnec")
    private Integer idPnec;
    @Column(name = "id_grpsng")
    private Integer idGrpsng;
    @Column(name = "nombre_completo")
    private String fullName = "";
    @Column(name = "id_colegio")
    private Integer idColegio = 0;
    @Column(name = "anio_egreso_cole")
    private Integer anioEgresoCole = 0;
    @Column(name = "update_flow")
    private Integer updateFlow;
    @Column(name = "email_prin")
    private String mail = "";
    @Column(name = "telefono_prin")
    private String telefonoPrin = "";
    @Column(name = "celular_prin")
    private String phone = "";
    @Column(name = "update_self")
    private Integer updateSelf = 0;
    @Column(name = "otro_colegio")
    private String otroColegio = "";
    /*@JoinColumn(name = "id_dir", referencedColumnName = "id_dir", insertable = false, updatable = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)*/
    @Transient
    private Directory directory;

}
