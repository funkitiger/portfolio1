/*
 * Copyright © 2018 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa;

import java.io.Serializable;
import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * Eine zu erledigende Aufgabe.
 */
@Data
@Entity
public class Verkaufsanzeige implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "task_ids")
    @TableGenerator(name = "task_ids", initialValue = 0, allocationSize = 50)
    private long id;

    @ManyToOne
    @NotNull(message = "Die Aufgabe muss einem Benutzer geordnet werden.")
    private Benutzer owner;

    @ManyToOne
    private Kategorie kategorie;

    @Column(length = 50)
    @NotNull(message = "Die Bezeichnung darf nicht leer sein.")
    @Size(min = 1, max = 50, message = "Die Bezeichnung muss zwischen ein und 50 Zeichen lang sein.")
    private String bezeichnung;

    @Lob
    @NotNull
    private String beschreibung;

    private AngebotArt angebotArt;
    
    private Date erstellungsdatum;
    
    private PreisArt preisArt;
    
    private double preis;



    //<editor-fold defaultstate="collapsed" desc="Konstruktoren">
    public Verkaufsanzeige() {
    }

    public Verkaufsanzeige(Benutzer owner, Kategorie category, String bezeichnung, String beschreibung, Date erstellungsdatum) {
        this.owner = owner;
        this.kategorie = category;
        this.bezeichnung = bezeichnung;
        this.beschreibung = beschreibung;
        this.erstellungsdatum = erstellungsdatum;
    }
   

}
