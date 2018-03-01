
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
        
        HttpSession session = request.getSession();
        
        if(session.getAttribute("bearbeiten_form") == null){
            Benutzer benutzer = benutzerBean.getCurrentUser2();
            
            Map<String, String[]> values = new HashMap<String, String[]>();
            String[] help = new String[1];

            help[0] = benutzer.getVorNachname();
            values.put("vorNachname", help);

            FormValues formValues = new FormValues();
            formValues.setValues(values);
            
            session.setAttribute("bearbeiten_form", formValues);
        }
        
        // Anfrage an dazugehörige JSP weiterleiten
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/app/benutzerBearbeiten.jsp");
        dispatcher.forward(request, response);
         
        // Alte Formulardaten aus der Session entfernen
        session.removeAttribute("bearbeiten_form");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Formulareingaben auslesen
        request.setCharacterEncoding("utf-8");
        
        // Eingaben Benutzer prüfen  
        List<String> errors = new ArrayList<>();
        
        String username = request.getParameter("bearbeiten_username");
        String passwortAlt = request.getParameter("bearbeiten_password1");
        String password1 = request.getParameter("bearbeiten_newPassword");
        String password2 = request.getParameter("bearbeiten_newPassword2");
        String vorNachname = request.getParameter("vorNachname");
        String strasseHnr = request.getParameter("strasseHausnr");
        String plz = request.getParameter("plz");
        String ort = request.getParameter("ort");
        String telefonNr = request.getParameter("telefon");
        String email = request.getParameter("email");
        
        if (password1 == null || password1.equals("") || password2 == null || password2.equals("") || !password1.equals(password2)) {
            errors.add("Die beiden Passwörter stimmen nicht überein.");
        } else {
            Benutzer benutzer = new Benutzer(username, password2, vorNachname, strasseHnr, plz, ort, email, telefonNr);
            errors.addAll(this.validationBean.validate(benutzer));
        }
        
        if (errors.isEmpty()){
           // Keine Fehler: Benutzer ändern und Startseite aufrufen
           this.benutzerBean.datenBearbeiten(username, password1, vorNachname, strasseHnr, plz, ort, email, telefonNr);     
           response.sendRedirect(WebUtils.appUrl(request, "/app/uebersicht/"));
        } else {
            // Fehler: Formular erneut anzeigen
            FormValues formValues = new FormValues();
            formValues.setValues(request.getParameterMap());
            formValues.setErrors(errors);
            
            HttpSession session = request.getSession();
            session.setAttribute("bearbeiten_form", formValues);
            response.sendRedirect(request.getRequestURI());
        }
    }

}
