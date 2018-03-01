
package dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.web;

import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb.BenutzerBean;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.ejb.ValidationBean;
import dhbwka.wwi.vertsys.javaee.kleinanzeigenportal.jpa.Benutzer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Seite zum Bearbeiten eines Benutzers.
 */

@WebServlet(urlPatterns = {"/app/benutzerBearbeiten/"})
public class BenutzerBearbeitenServlet extends HttpServlet {
    
    @EJB
    ValidationBean validationBean;
            
    @EJB
    BenutzerBean benutzerBean;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
              
        // Anfrage an dazugehörige JSP weiterleiten
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/app/benutzerBearbeiten.jsp");
        dispatcher.forward(request, response);
        
        Benutzer benutzer = benutzerBean.getCurrentUser();
        
        
        
        Map<String, String[]> values = request.getParameterMap();
        String[] help = {benutzer.getBenutzername()};
        values.put("bearbeiten_username", help);
        
        FormValues formValues = new FormValues();
        formValues.setValues(values);
        
        HttpSession session = request.getSession();
        session.setAttribute("bearbeiten_form", formValues);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Formulareingaben auslesen
        request.setCharacterEncoding("utf-8");
        
        List<String> error = new ArrayList<>();
        
        String username = request.getParameter("bearbeiten_username");
        String passwortAlt = request.getParameter("bearbeiten_password1");
        String password1 = request.getParameter("bearbeiten_newPassword");
        String password2 = request.getParameter("bearbeiten_newPassword2");
        String vorNachname = request.getParameter("vorNachname");
        String[] namenListe = vorNachname.split(" ");
        String vorname = "";
        String nachname = "";
        if (namenListe.length < 2) {
            error.add("Vor- und Nachname müssen mit einem Leerzeichen getrennt werden.");
        } else {
            vorname = namenListe[0];
            for (int i = 1; i < namenListe.length - 1; i++) {
                vorname += " " + namenListe[i];
            }
            nachname = namenListe[namenListe.length - 1];
        }
        
        String strasseHnr = request.getParameter("strasseHausnr");
        String[] strasseHnrListe = strasseHnr.split(" ");
        String strasse = "";
        String hausNr = "";
        if (strasseHnrListe.length < 2) {
            error.add("Straße und Hausnummer müssen mit einem Leerzeichen getrennt werden.");
        } else {
            strasse = strasseHnrListe[0];
            for (int i = 1; i < strasseHnrListe.length - 1; i++) {
                strasse += " " + strasseHnrListe[i];
            }
            hausNr = strasseHnrListe[strasseHnrListe.length - 1];
        }
        int plz = Integer.parseInt(request.getParameter("plz"));
        String ort = request.getParameter("ort");
        String telefonNr = request.getParameter("telefon");
        String email = request.getParameter("email");
        
        
        // Eingaben Benutzer prüfen  
        List<String> errors = new ArrayList<>();
        
        Benutzer benutzer = new Benutzer(username, password2, vorNachname, strasseHnr, String.valueOf(plz), ort, email, telefonNr);
                
        if (password1 == null || password1.equals("")) {
            password1 = null;
        } else {
            errors.addAll(this.validationBean.validate(benutzer));
        }
        if (!error.equals("")) {
            errors.addAll(error);
        }
        if (password1 != null) {
            this.validationBean.validate(benutzer.getPasswort(), errors);
        }
        
        if (password1 != null && password2 != null && !password1.equals(password2)) {
            errors.add("Die beiden Passwörter stimmen nicht überein.");
        }
        
        if (errors.isEmpty()){
           this.benutzerBean.datenBearbeiten(username, password2, vorNachname, strasseHnr, String.valueOf(plz), ort, email, telefonNr);     
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
            session.setAttribute("bearbeiten_form", formValues);
            response.sendRedirect(request.getRequestURI());
        }
    }

}
