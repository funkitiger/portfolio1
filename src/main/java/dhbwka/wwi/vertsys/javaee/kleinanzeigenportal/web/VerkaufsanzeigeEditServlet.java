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
    VerkaufsanzeigenBean taskBean;

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
                this.saveTask(request, response);
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
    private void saveTask(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Formulareingaben prüfen
        List<String> errors = new ArrayList<>();

        String kategorie = request.getParameter("category");
        AngebotArt angebotArt = AngebotArt.valueOf(request.getParameter("angebotArt"));
        String bezeichnung = request.getParameter("bezeichnung");
        String beschreibung = request.getParameter("beschreibung");
        PreisArt preisArt = PreisArt.valueOf(request.getParameter("preisArt"));
        double preis = 0.0;
        try {
            preis = Double.parseDouble(request.getParameter("preis"));
        } catch (NumberFormatException ex) {
            errors.add("Bitte geben Sie einen Preis an");
        }

        //Verkaufsanzeige anzeige = this.getRequestedAnzeige(request);
        Verkaufsanzeige anzeige = new Verkaufsanzeige();

        if (kategorie != null && !kategorie.trim().isEmpty()) {
            try {
                anzeige.setKategorie(this.categoryBean.findById(Long.parseLong(kategorie)));
            } catch (NumberFormatException ex) {
                // Ungültige oder keine ID mitgegeben
            }
        }

        anzeige.setAngebotArt(angebotArt);
        anzeige.setBezeichnung(bezeichnung);
        anzeige.setBeschreibung(beschreibung);
        anzeige.setPreisArt(preisArt);
        anzeige.setPreis(preis);

        Date aktuellesDatum = new Date(System.currentTimeMillis());
        //TODO: auch Time? oder reicht Date?
        anzeige.setErstellungsdatum(aktuellesDatum);

        this.validationBean.validate(anzeige, errors);

        // Datensatz speichern
        if (errors.isEmpty()) {
            this.taskBean.update(anzeige);
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
        this.taskBean.delete(anzeige);

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
        //   task.setDueTime(new Time(System.currentTimeMillis()));

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
            anzeige = this.taskBean.findById(Long.parseLong(anzeigeId));
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
                Double.toString(anzeige.getKategorie().getId())
            });
        }

        values.put("angebotArt", new String[]{
            anzeige.getAngebotArt().toString()
        });

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
            anzeige.getErstellungsdatum().toString()
        });
        values.put("vorNachname", new String[]{
            anzeige.getOwner().getVorNachname()}
        );
        values.put("strasseHausnr", new String[]{
            anzeige.getOwner().getStrasseHnr()
        });
        values.put("plz", new String[]{
            anzeige.getOwner().getPlz()
        });
        values.put("ort", new String[]{
            anzeige.getOwner().getOrt()
        });
        values.put("telefonnr", new String[]{
            anzeige.getOwner().getTelefonnr()
        });
        values.put("email", new String[]{
            anzeige.getOwner().getEmail()
        });

        FormValues formValues = new FormValues();
        formValues.setValues(values);
        return formValues;
    }

}
