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
@WebServlet(urlPatterns = "/app/task/*")
public class TaskEditServlet extends HttpServlet {

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
        request.setAttribute("categories", this.categoryBean.findAllSorted());

        // Zu bearbeitende Aufgabe einlesen
        HttpSession session = request.getSession();

        Verkaufsanzeige task = this.getRequestedTask(request);
        request.setAttribute("edit", task.getId() != 0);
                                
        if (session.getAttribute("task_form") == null) {
            // Keine Formulardaten mit fehlerhaften Daten in der Session,
            // daher Formulardaten aus dem Datenbankobjekt übernehmen
            request.setAttribute("task_form", this.createTaskForm(task));
        }

        // Anfrage an die JSP weiterleiten
        request.getRequestDispatcher("/WEB-INF/app/task_edit.jsp").forward(request, response);

        session.removeAttribute("task_form");
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

        String taskCategory = request.getParameter("task_category");
        String taskDueDate = request.getParameter("task_due_date");
        String taskDueTime = request.getParameter("task_due_time");
        String taskStatus = request.getParameter("task_status");
        String taskShortText = request.getParameter("task_short_text");
        String taskLongText = request.getParameter("task_long_text");

        Verkaufsanzeige anzeige = this.getRequestedTask(request);

        if (taskCategory != null && !taskCategory.trim().isEmpty()) {
            try {
                anzeige.setKategorie(this.categoryBean.findById(Long.parseLong(taskCategory)));
            } catch (NumberFormatException ex) {
                // Ungültige oder keine ID mitgegeben
            }
        }

        Date dueDate = WebUtils.parseDate(taskDueDate);
        Time dueTime = WebUtils.parseTime(taskDueTime);

        if (dueDate != null) {
            anzeige.setErstellungsdatum(dueDate);
        } else {
            errors.add("Das Datum muss dem Format dd.mm.yyyy entsprechen.");
        }

//        if (dueTime != null) {
//            anzeige.setErstellungsdatum(dueTime);
//        } else {
//            errors.add("Die Uhrzeit muss dem Format hh:mm:ss entsprechen.");
//        }

        anzeige.setBezeichnung(taskShortText);
        anzeige.setBeschreibung(taskLongText);

        this.validationBean.validate(anzeige, errors);

        // Datensatz speichern
        if (errors.isEmpty()) {
            this.taskBean.update(anzeige);
        }

        // Weiter zur nächsten Seite
        if (errors.isEmpty()) {
            // Keine Fehler: Startseite aufrufen
            response.sendRedirect(WebUtils.appUrl(request, "/app/tasks/"));
        } else {
            // Fehler: Formuler erneut anzeigen
            FormValues formValues = new FormValues();
            formValues.setValues(request.getParameterMap());
            formValues.setErrors(errors);

            HttpSession session = request.getSession();
            session.setAttribute("task_form", formValues);

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
        Verkaufsanzeige anzeige = this.getRequestedTask(request);
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
    private Verkaufsanzeige getRequestedTask(HttpServletRequest request) {
        // Zunächst davon ausgehen, dass ein neuer Satz angelegt werden soll
        Verkaufsanzeige anzeige = new Verkaufsanzeige();
        anzeige.setOwner(this.userBean.getCurrentUser());
        anzeige.setErstellungsdatum(new Date(System.currentTimeMillis()));
     //   task.setDueTime(new Time(System.currentTimeMillis()));

        // ID aus der URL herausschneiden
        String taskId = request.getPathInfo();

        if (taskId == null) {
            taskId = "";
        }

        taskId = taskId.substring(1);

        if (taskId.endsWith("/")) {
            taskId = taskId.substring(0, taskId.length() - 1);
        }

        // Versuchen, den Datensatz mit der übergebenen ID zu finden
        try {
            anzeige = this.taskBean.findById(Long.parseLong(taskId));
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

        values.put("task_owner", new String[]{
            anzeige.getOwner().getBenutzername()
        });

        if (anzeige.getKategorie() != null) {
            values.put("task_category", new String[]{
                anzeige.getKategorie().toString()
            });
        }

        values.put("task_due_date", new String[]{
            WebUtils.formatDate(anzeige.getErstellungsdatum())
        });

//        values.put("task_due_time", new String[]{
//            WebUtils.formatTime(anzeige.getDueTime())
//        });


        values.put("task_short_text", new String[]{
            anzeige.getBezeichnung()
        });

        values.put("task_long_text", new String[]{
            anzeige.getBeschreibung()
        });

        FormValues formValues = new FormValues();
        formValues.setValues(values);
        return formValues;
    }

}
