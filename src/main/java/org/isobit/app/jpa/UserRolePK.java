package org.isobit.app.jpa;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Embeddable
public class UserRolePK implements Serializable {
    
    private static final long serialVersionUID = 2L;
    
    @Basic(optional = false)
    @NotNull
    private int uid;

    @Basic(optional = false)
    @NotNull
    private int rid;

}
