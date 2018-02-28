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

import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.Benutzer;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Spezielle EJB zum Anlegen eines Benutzers und Aktualisierung des Passworts.
 */
@Stateless
public class BenutzerBean {

    @PersistenceContext
    EntityManager em;
    
    @Resource
    EJBContext ctx;

    /**
     * Gibt das Datenbankobjekt des aktuell eingeloggten Benutzers zurück,
     *
     * @return Eingeloggter Benutzer oder null
     */
    public Benutzer getCurrentUser() {
        return this.em.find(Benutzer.class, this.ctx.getCallerPrincipal().getName());
    }

    /**
     *
     * @param benutzername
     * @param passwort
     * @param vorNachname
     * @param strasseHnr
     * @param plz
     * @param ort
     * @param email
     * @param telefonnr
     * @throws UserBean.UserAlreadyExistsException
     */
    public void signup(String benutzername, String passwort, String vorNachname, String strasseHnr, int plz, String ort, String email, String telefonnr) throws UserAlreadyExistsException {
        if (em.find(Benutzer.class, benutzername) != null) {
            throw new UserAlreadyExistsException("Der Benutzername $B ist bereits vergeben.".replace("$B", benutzername));
        }

        Benutzer user = new Benutzer(benutzername, passwort, vorNachname, strasseHnr, plz, ort, email, telefonnr);
        user.addToGroup("todo-app-user");
        em.persist(user);
    }

    /**
     * Passwort ändern (ohne zu speichern)
     * @param user
     * @param oldPassword
     * @param newPassword
     * @throws UserBean.InvalidCredentialsException
     */
    @RolesAllowed("todo-app-user")
    public void changePassword(Benutzer user, String oldPassword, String newPassword) throws InvalidCredentialsException {
        if (user == null || !user.checkPassword(oldPassword)) {
            throw new InvalidCredentialsException("Benutzername oder Passwort sind falsch.");
        }

        user.setPassword(newPassword);
    }
    
    /**
     * Benutzer löschen
     * @param user Zu löschender Benutzer
     */
    @RolesAllowed("todo-app-user")
    public void delete(Benutzer user) {
        this.em.remove(user);
    }
    
    /**
     * Benutzer aktualisieren
     * @param user Zu aktualisierender Benutzer
     * @return Gespeicherter Benutzer
     */
    @RolesAllowed("todo-app-user")
    public Benutzer update(Benutzer user) {
        return em.merge(user);
    }

    /**
     * Fehler: Der Benutzername ist bereits vergeben
     */
    public class UserAlreadyExistsException extends Exception {

        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    /**
     * Fehler: Das übergebene Passwort stimmt nicht mit dem des Benutzers
     * überein
     */
    public class InvalidCredentialsException extends Exception {

        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

}
