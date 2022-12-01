package org.isobit.admin;

import io.quarkus.hibernate.orm.panache.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.isobit.admin.jpa.RoleRole;

@ApplicationScoped
public class Repository implements PanacheRepositoryBase<RoleRole, Integer> {
}