/*
 * Copyright © 2018 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb;

import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.Kategorie;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;

/**
 * Einfache EJB mit den üblichen CRUD-Methoden für Kategorien.
 */
@Stateless
@RolesAllowed("todo-app-user")
public class CategoryBean extends EntityBean<Kategorie, Long> {

    public CategoryBean() {
        super(Kategorie.class);
    }

    /**
     * Auslesen aller Kategorien, alphabetisch sortiert.
     *
     * @return Liste mit allen Kategorien
     */
    public List<Kategorie> findAllSorted() {
        return this.em.createQuery("SELECT c FROM Category c ORDER BY c.name").getResultList();
    }
}
