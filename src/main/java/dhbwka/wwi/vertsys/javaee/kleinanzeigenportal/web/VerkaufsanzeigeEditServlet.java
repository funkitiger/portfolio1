/*
 * Copyright © 2018 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.web;

import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb.KategorieBean;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb.VerkaufsanzeigenBean;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb.BenutzerBean;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb.ValidationBean;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.AngebotArt;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.PreisArt;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.Verkaufsanzeige;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Seite zum Anlegen oder Bearbeiten einer Aufgabe.
 */
@WebServlet(urlPatterns = "/app/angebot/*")
public class VerkaufsanzeigeEditServlet extends HttpServlet {

    @EJB
    VerkaufsanzeigenBean anzeigeBean;

    @EJB
    KategorieBean categoryBean;

    @EJB
    BenutzerBean userBean;

    @EJB
    ValidationBean validationBean;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verfügbare Kategorien und Stati für die Suchfelder ermitteln
        request.setAttribute("kategorien", this.categoryBean.findAllSorted());
        request.setAttribute("angebotArten", AngebotArt.values());
        request.setAttribute("preisArten", PreisArt.values());

        // Zu bearbeitende Anzeige einlesen
        HttpSession session = request.getSession();

        Verkaufsanzeige anzeige = this.getRequestedAnzeige(request);
        request.setAttribute("edit", anzeige.getId() != 0);
        request.setAttribute("user", anzeige.getOwner());
        if(!userBean.getCurrentUser().getBenutzername().equals(anzeige.getOwner().getBenutzername()))
        {
            request.setAttribute("readonly", "readonly = readonly");
            request.setAttribute("disabled", "disabled=disabled");
        }
        

        if (session.getAttribute("verkaufsanzeige_form") == null) {
            // Keine Formulardaten mit fehlerhaften Daten in der Session,
            // daher Formulardaten aus dem Datenbankobjekt übernehmen
            request.setAttribute("verkaufsanzeige_form", this.createTaskForm(anzeige));
        }

        // Anfrage an die JSP weiterleiten
        request.getRequestDispatcher("/WEB-INF/app/verkaufsanzeige_edit.jsp").forward(request, response);

        session.removeAttribute("verkaufsanzeige_form");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Angeforderte Aktion ausführen
        request.setCharacterEncoding("utf-8");

        String action = request.getParameter("action");

        if (action == null) {
            action = "";
        }

        switch (action) {
            case "save":
                this.saveAnzeige(request, response);
                break;
            case "delete":
                this.deleteTask(request, response);
                break;
        }
    }

    /**
     * Aufgerufen in doPost(): Neue oder vorhandene Aufgabe speichern
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void saveAnzeige(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Formulareingaben prüfen
        List<String> errors = new ArrayList<>();

        String kategorie = request.getParameter("category");
        String angebotArt = request.getParameter("angebotArt");
        String bezeichnung = request.getParameter("bezeichnung");
        String beschreibung = request.getParameter("beschreibung");
        String preisArt = request.getParameter("preisArt");
        String preis = request.getParameter("preis");

        Verkaufsanzeige anzeige = this.getRequestedAnzeige(request);

        if (kategorie != null && !kategorie.trim().isEmpty()) {
            try {
                anzeige.setKategorie(this.categoryBean.findById(Long.parseLong(kategorie)));
            } catch (NumberFormatException ex) {
                // Ungültige oder keine ID mitgegeben
            }
        }
        try {
            anzeige.setPreisArt(PreisArt.valueOf(preisArt));
        } catch (IllegalArgumentException ex) {
            errors.add("Die ausgewählte Art des Preises ist nicht vorhanden.");
        }
        try {
            anzeige.setAngebotArt(AngebotArt.valueOf(angebotArt));
        } catch (IllegalArgumentException ex) {
            errors.add("Die ausgewählte Art des Angebots ist nicht vorhanden.");
        }

        if (preis != null && !preis.trim().isEmpty()) {
            try {
                anzeige.setPreis(Double.parseDouble(preis));
            } catch (NumberFormatException ex) {
                errors.add("Bitte geben Sie einen Preis an");
            }
        }

        anzeige.setBezeichnung(bezeichnung);
        anzeige.setBeschreibung(beschreibung);

        Date aktuellesDatum = new Date(System.currentTimeMillis());
        Time aktuelleZeit = new Time(System.currentTimeMillis());
        anzeige.setErstellungsdatum(aktuellesDatum);
        anzeige.setErstellungszeit(aktuelleZeit);

        this.validationBean.validate(anzeige, errors);

        // Datensatz speichern
        if (errors.isEmpty()) {
            this.anzeigeBean.update(anzeige);
        }

        // Weiter zur nächsten Seite
        if (errors.isEmpty()) {
            // Keine Fehler: Startseite aufrufen
            response.sendRedirect(WebUtils.appUrl(request, "/app/uebersicht/"));
        } else {
            // Fehler: Formuler erneut anzeigen
            FormValues formValues = new FormValues();
            formValues.setValues(request.getParameterMap());
            formValues.setErrors(errors);

            HttpSession session = request.getSession();
            session.setAttribute("verkaufsanzeige_form", formValues);

            response.sendRedirect(request.getRequestURI());
        }
    }

    /**
     * Aufgerufen in doPost: Vorhandene Aufgabe löschen
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void deleteTask(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Datensatz löschen
        Verkaufsanzeige anzeige = this.getRequestedAnzeige(request);
        this.anzeigeBean.delete(anzeige);

        // Zurück zur Übersicht
        response.sendRedirect(WebUtils.appUrl(request, "/app/tasks/"));
    }

    /**
     * Zu bearbeitende Aufgabe aus der URL ermitteln und zurückgeben. Gibt
     * entweder einen vorhandenen Datensatz oder ein neues, leeres Objekt
     * zurück.
     *
     * @param request HTTP-Anfrage
     * @return Zu bearbeitende Aufgabe
     */
    private Verkaufsanzeige getRequestedAnzeige(HttpServletRequest request) {
        // Zunächst davon ausgehen, dass ein neuer Satz angelegt werden soll
        Verkaufsanzeige anzeige = new Verkaufsanzeige();
        anzeige.setAngebotArt(AngebotArt.BIETE); //Standardwert
        anzeige.setPreisArt(PreisArt.VERHANDLUNGSBASIS); //Standardwert 
        anzeige.setOwner(this.userBean.getCurrentUser());
        anzeige.setErstellungsdatum(new Date(System.currentTimeMillis()));
        anzeige.setErstellungszeit(new Time(System.currentTimeMillis()));

        // ID aus der URL herausschneiden
        String anzeigeId = request.getPathInfo();

        if (anzeigeId == null) {
            anzeigeId = "";
        }

        anzeigeId = anzeigeId.substring(1);

        if (anzeigeId.endsWith("/")) {
            anzeigeId = anzeigeId.substring(0, anzeigeId.length() - 1);
        }

        // Versuchen, den Datensatz mit der übergebenen ID zu finden
        try {
            anzeige = this.anzeigeBean.findById(Long.parseLong(anzeigeId));
        } catch (NumberFormatException ex) {
            // Ungültige oder keine ID in der URL enthalten
        }

        return anzeige;
    }

    /**
     * Neues FormValues-Objekt erzeugen und mit den Daten eines aus der
     * Datenbank eingelesenen Datensatzes füllen. Dadurch müssen in der JSP
     * keine hässlichen Fallunterscheidungen gemacht werden, ob die Werte im
     * Formular aus der Entity oder aus einer vorherigen Formulareingabe
     * stammen.
     *
     * @param anzeige Die zu bearbeitende Aufgabe
     * @return Neues, gefülltes FormValues-Objekt
     */
    private FormValues createTaskForm(Verkaufsanzeige anzeige) {
        Map<String, String[]> values = new HashMap<>();

        if (anzeige.getKategorie() != null) {
            values.put("kategorie", new String[]{
                anzeige.getKategorie().toString()
            });
        }

        values.put("bezeichnung", new String[]{
            anzeige.getBezeichnung()
        });

        values.put("beschreibung", new String[]{
            anzeige.getBeschreibung()
        });
        values.put("preis", new String[]{
            Double.toString(anzeige.getPreis())
        });
        values.put("erstellungsdatum", new String[]{
            WebUtils.formatDate(anzeige.getErstellungsdatum())
        });
        values.put("erstellungszeit", new String[]{
           WebUtils.formatTime(anzeige.getErstellungszeit())
        });
        values.put("preisArt", new String[]{
            anzeige.getPreisArt() + ""
        });
        values.put("angebotArt", new String[]{
            anzeige.getAngebotArt() + ""
        });

        FormValues formValues = new FormValues();
        formValues.setValues(values);
        return formValues;
    }

}
