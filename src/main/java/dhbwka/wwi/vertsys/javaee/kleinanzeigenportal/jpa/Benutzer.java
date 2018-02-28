/*
 * Copyright Â© 2018 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;
import javax.validation.constraints.Email;

/**
 * Datenbankklasse fÃ¼r einen Benutzer.
 */
@Data
@Entity
@Table(name = "JTODO_USER")
public class Benutzer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "USERNAME", length = 64)
    @Size(min = 5, max = 64, message = "Der Benutzername muss zwischen fünf und 64 Zeichen lang sein.")
    @NotNull(message = "Der Benutzername darf nicht leer sein.")
    private String benutzername;

    public class Password {

        @Size(min = 6, max = 64, message = "Das Passwort muss zwischen sechs und 64 Zeichen lang sein.")
        public String passwort = "";
    }
    @Transient
    private final Password passwort = new Password();

    @Column(name = "PASSWORD_HASH", length = 64)
    @NotNull(message = "Das Passwort darf nicht leer sein.")
    private String passwordHash;
    @NotNull(message = "Der Name darf nicht leer sein.")
    private String vorNachname;
    @NotNull(message = "Die StraÃŸe und Hausnunmmer darf nicht leer sein.")
    private String strasseHnr;
    // @Size(max = 99999, min = 10000)
    @NotNull(message = "Die Postleitzahl darf muss einen Wert zwischen 10000 und 99999 sein.")
    private String plz;
    @NotNull(message = "Der Ort darf nicht leer sein.")
    private String ort;

    //@Pattern(regexp = "^\\w+@\\w+\\..{2,3}(.{2,3})?$")
    @Email
    private String email;
    @NotNull(message = "Die Telefonnummer darf nicht leer sein.")
    private String telefonnr;

    @ElementCollection
    @CollectionTable(
            name = "JTODO_USER_GROUP",
            joinColumns = @JoinColumn(name = "USERNAME")
    )
    @Column(name = "GROUPNAME")
    List<String> groups = new ArrayList<>();

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    List<Verkaufsanzeige> anzeige = new ArrayList<>();

    //<editor-fold defaultstate="collapsed" desc="Konstruktoren">
    public Benutzer() {
    }

    public Benutzer(String benutzername, String passwort, String vorNachname, String strasseHnr, String plz, String ort, String email, String telefonnr) {
        this.benutzername = benutzername;
        this.passwort.passwort = passwort;
        this.passwordHash = this.hashPassword(passwort);
        this.vorNachname = vorNachname;
        this.strasseHnr = strasseHnr;
        this.plz = plz;
        this.ort = ort;
        this.telefonnr = telefonnr;
        this.email = email;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Passwort setzen und prÃ¼fen">
    /**
     * Berechnet der Hash-Wert zu einem Passwort.
     *
     * @param password Passwort
     * @return Hash-Wert
     */
    private String hashPassword(String password) {
        byte[] hash;

        if (password == null) {
            password = "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            hash = "!".getBytes(StandardCharsets.UTF_8);
        }

        BigInteger bigInt = new BigInteger(1, hash);
        return bigInt.toString(16);
    }

    /**
     * Berechnet einen Hashwert aus dem Ã¼bergebenen Passwort und legt ihn im
     * Feld passwordHash ab. Somit wird das Passwort niemals als Klartext
     * gespeichert.
     *
     * Gleichzeitig wird das Passwort im nicht gespeicherten Feld passwort
     * abgelegt, um durch die Bean Validation Annotationen Ã¼berprÃ¼ft werden zu
     * kÃ¶nnen.
     *
     * @param password Neues Passwort
     */
    public void setPassword(String password) {
        this.passwort.passwort = password;
        this.passwordHash = this.hashPassword(password);
    }

    /**
     * Nur fÃ¼r die Validierung bei einer PasswortÃ¤nderung!
     *
     * @return Neues, beim Speichern gesetztes Passwort
     */
    public Password getPassword() {
        return this.passwort;
    }

    /**
     * PrÃ¼ft, ob das Ã¼bergebene Passwort korrekt ist.
     *
     * @param password Zu prÃ¼fendes Passwort
     * @return true wenn das Passwort stimmt sonst false
     */
    public boolean checkPassword(String password) {
        return this.passwordHash.equals(this.hashPassword(password));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Zuordnung zu Benutzergruppen">
    /**
     * @return Eine unverÃ¤nderliche Liste aller Benutzergruppen
     */
    public List<String> getGroups() {
        List<String> groupsCopy = new ArrayList<>();

        this.groups.forEach((groupname) -> {
            groupsCopy.add(groupname);
        });

        return groupsCopy;
    }

    /**
     * FÃ¼gt den Benutzer einer weiteren Benutzergruppe hinzu.
     *
     * @param groupname Name der Benutzergruppe
     */
    public void addToGroup(String groupname) {
        if (!this.groups.contains(groupname)) {
            this.groups.add(groupname);
        }
    }

    /**
     * Entfernt den Benutzer aus der Ã¼bergebenen Benutzergruppe.
     *
     * @param groupname Name der Benutzergruppe
     */
    public void removeFromGroup(String groupname) {
        this.groups.remove(groupname);
    }
    //</editor-fold>

}
