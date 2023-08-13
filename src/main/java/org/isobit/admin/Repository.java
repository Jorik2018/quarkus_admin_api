package org.isobit.admin;

import org.isobit.admin.model.RoleRole;

import io.quarkus.hibernate.orm.panache.*;
import jakarta.enterprise.context.ApplicationScoped;



@ApplicationScoped
public class Repository implements PanacheRepositoryBase<RoleRole, Integer> {
}