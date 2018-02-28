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
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.Kategorie;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.Verkaufsanzeige;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet für die Startseite bzw. jede Seite, die eine Liste der Aufgaben
 * zeigt.
 */
@WebServlet(urlPatterns = {"/app/tasks/"})
public class TaskListServlet extends HttpServlet {

    @EJB
    private KategorieBean categoryBean;
    
    @EJB
    private VerkaufsanzeigenBean taskBean;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Verfügbare Kategorien und Stati für die Suchfelder ermitteln
        request.setAttribute("categories", this.categoryBean.findAllSorted());

        // Suchparameter aus der URL auslesen
        String searchText = request.getParameter("search_text");
        String searchCategory = request.getParameter("search_category");

        // Anzuzeigende Aufgaben suchen
        Kategorie category = null;

        if (searchCategory != null) {
            try {
                category = this.categoryBean.findById(Long.parseLong(searchCategory));
            } catch (NumberFormatException ex) {
                category = null;
            }
        }

        List<Verkaufsanzeige> tasks = this.taskBean.search(searchText, category);
        request.setAttribute("tasks", tasks);

        // Anfrage an die JSP weiterleiten
        request.getRequestDispatcher("/WEB-INF/app/task_list.jsp").forward(request, response);
    }
}
