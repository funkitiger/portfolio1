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
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Spezielle EJB zum Anlegen eines Benutzers und Aktualisierung des Passworts.
 * @param <Entity>
 * @param <EntityId>
 */
@Stateless
public class BenutzerBean extends EntityBean<Benutzer, String> {

    @PersistenceContext
    EntityManager em;
    
    @Resource
    EJBContext ctx;

    public BenutzerBean() {
        super(Benutzer.class);
    }

    /**
     * Gibt das Datenbankobjekt des aktuell eingeloggten Benutzers zurück,
     *
     * @return Eingeloggter Benutzer oder null
     */
    public Benutzer getCurrentUser() {
        String username = this.ctx.getCallerPrincipal().getName();
        return this.em.find(Benutzer.class, username);
    }
    
    public Benutzer getCurrentUser2() {
        String username = this.ctx.getCallerPrincipal().getName();
        List<Benutzer> users = this.em.createQuery("FROM Benutzer b WHERE b.benutzername = :username").setParameter("username", username).getResultList();
        if(users.isEmpty())
            return null;
        else
            return users.get(0);
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
    public void signup(String benutzername, String passwort, String vorNachname, String strasseHnr, String plz, String ort, String email, String telefonnr) throws UserAlreadyExistsException {
        if (em.find(Benutzer.class, benutzername) != null) {
            throw new UserAlreadyExistsException("Der Benutzername $B ist bereits vergeben.".replace("$B", benutzername));
        }

        Benutzer benutzer = new Benutzer(benutzername, passwort, vorNachname, strasseHnr, plz, ort, email, telefonnr);
        benutzer.addToGroup("todo-app-user");
        em.persist(benutzer);
    }

    /**
     * Passwort ändern (ohne zu speichern)
     * @param benutzer
     * @param passwortAlt
     * @param passwortNeu
     * @throws UserBean.InvalidCredentialsException
     */
    @RolesAllowed("todo-app-user")
    public void changePassword(Benutzer benutzer, String passwortAlt, String passwortNeu) throws InvalidCredentialsException {
        if (benutzer == null || !benutzer.checkPassword(passwortAlt)) {
            throw new InvalidCredentialsException("Benutzername oder Passwort sind falsch.");
        }
        benutzer.setPassword(passwortNeu);
    }
    
    /**
     * Benutzer löschen
     * @param benutzer Zu löschender Benutzer
     */
    @RolesAllowed("todo-app-user")
    public void delete(Benutzer benutzer) {
        this.em.remove(benutzer);
    }
    
    /**
     * Benutzer aktualisieren
     * @param benutzer Zu aktualisierender Benutzer
     * @return Gespeicherter Benutzer
     */
    @RolesAllowed("todo-app-user")
    public Benutzer update(Benutzer benutzer) {
        return em.merge(benutzer);
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
    
    public void datenBearbeiten(String benutzername, String password2, String vorNachname, String strasseHnr, String plz, String ort, String email, String telefonNr){
        Benutzer benutzer = findById(benutzername);
       
        benutzer.setBenutzername(benutzername);
        benutzer.setPassword(password2);
        benutzer.setVorNachname(vorNachname);
        benutzer.setStrasseHnr(strasseHnr);
        benutzer.setPlz(plz);
        benutzer.setOrt(ort);
        benutzer.setEmail(email);
        benutzer.setTelefonnr(telefonNr);
        
        em.merge(benutzer);
    }

}
