/*
 * Copyright Â© 2018 Dennis Schulmeister-Zimolong
 * 
 * E-Mail: dhbw@windows3.de
 * Webseite: https://www.wpvs.de/
 * 
 * Dieser Quellcode ist lizenziert unter einer
 * Creative Commons Namensnennung 4.0 International Lizenz.
 */
package dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.web;

import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb.ValidationBean;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb.BenutzerBean;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.Benutzer;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet fÃ¼r die Registrierungsseite. Hier kann sich ein neuer Benutzer
 * registrieren. AnschlieÃŸend wird der auf die Startseite weitergeleitet.
 */
@WebServlet(urlPatterns = {"/signup/"})
public class SignUpServlet extends HttpServlet {
    
    @EJB
    ValidationBean validationBean;
            
    @EJB
    BenutzerBean benutzerBean;
    BenutzerBean userBean;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Anfrage an dazugehörige JSP weiterleiten
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/login/signup.jsp");
        dispatcher.forward(request, response);
        
        // Alte Formulardaten aus der Session entfernen
        HttpSession session = request.getSession();
        session.removeAttribute("signup_form");
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Formulareingaben auslesen
        request.setCharacterEncoding("utf-8");
        
        String username = request.getParameter("signup_username");
        String password1 = request.getParameter("signup_password1");
        String password2 = request.getParameter("signup_password2");
        String vorNachname = request.getParameter("vorNachname");
        String strasseHnr = request.getParameter("strasseHausNr"); //TODO: Exception abfangen?
        int plz = Integer.parseInt(request.getParameter("plz"));
        String telefonNr = request.getParameter("telefonnr");
        String email = request.getParameter("email");

        
        // Eingaben prüfen
        Benutzer user = new Benutzer(username, password2, username, password2, 0, password2, username, password2);
        List<String> errors = this.validationBean.validate(user);
        this.validationBean.validate(user.getPassword(), errors);
        
        if (password1 != null && password2 != null && !password1.equals(password2)) {
            errors.add("Die beiden Passwörter stimmen nicht Ã¼berein.");
        }
        
        // Neuen Benutzer anlegen
        if (errors.isEmpty()) {
            try {
                this.userBean.signup(username, password2, username, password2, 0, password2, username, password2);
            } catch (BenutzerBean.UserAlreadyExistsException ex) {
                errors.add(ex.getMessage());
            }
        }
        
        // Weiter zur nächsten Seite
        if (errors.isEmpty()) {
            // Keine Fehler: Startseite aufrufen
            request.login(username, password1);
            response.sendRedirect(WebUtils.appUrl(request, "/app/uebersicht/"));
        } else {
            // Fehler: Formuler erneut anzeigen
            FormValues formValues = new FormValues();
            formValues.setValues(request.getParameterMap());
            formValues.setErrors(errors);
            
            HttpSession session = request.getSession();
            session.setAttribute("signup_form", formValues);
            
            response.sendRedirect(request.getRequestURI());
        }
    }
    
}
