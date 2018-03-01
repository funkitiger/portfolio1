<%-- 
    Copyright © 2018 Dennis Schulmeister-Zimolong

    E-Mail: dhbw@windows3.de
    Webseite: https://www.wpvs.de/

    Dieser Quellcode ist lizenziert unter einer
    Creative Commons Namensnennung 4.0 International Lizenz.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib tagdir="/WEB-INF/tags/templates" prefix="template"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<template:base>
    <jsp:attribute name="title">
        <c:choose>
            <c:when test="${edit}">
                Verkaufsanzeige bearbeiten
            </c:when>
            <c:otherwise>
                Verkaufsanzeige anlegen
            </c:otherwise>
        </c:choose>
    </jsp:attribute>

    <jsp:attribute name="head">
        <link rel="stylesheet" href="<c:url value="/css/task_edit.css"/>" />
    </jsp:attribute>

    <jsp:attribute name="menu">
        <div class="menuitem">
            <a href="<c:url value="/app/uebersicht/"/>">Übersicht</a>
        </div>
    </jsp:attribute>

    <jsp:attribute name="content">
        <form method="post" class="stacked">
            <div class="column">
                <%-- CSRF-Token --%>
                <input type="hidden" name="csrf_token" value="${csrf_token}">

                <%-- Eingabefelder --%>
                <label for="category">Kategorie:</label>
                <div class="side-by-side">
                    <select name="category">
                        <option value="">Keine Kategorie</option>

                        <c:forEach items="${kategorien}" var="kategorie">
                            <option value="${kategorie.id}" ${verkaufsanzeige_form.values["kategorie"][0] == kategorie.id ? 'selected' : ''}>
                                <c:out value="${kategorie.name}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <label for="angebotArt">Art des Angebots:
                    <span class="required">*</span>
                </label>
                <div class="side-by-side">
                    <select name="angebotArt">
                        <c:forEach items="${angebotArten}" var="angebotArt">
                            <option value="${angebotArt}" ${verkaufsanzeige_form.values["angebotArt"][0] == angebotArt ? 'selected' : ''}>
                                <c:out value="${angebotArt.label}" />
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <label for="bezeichnung">
                    Bezeichnung:
                    <span class="required">*</span>
                </label>
                <div class="side-by-side">
                    <input type="text" name="bezeichnung" value="${verkaufsanzeige_form.values["bezeichnung"][0]}">
                </div>
                <label for="beschreibung">
                    Beschreibung:
                </label>
                <div class="side-by-side">
                    <textarea name="beschreibung"><c:out value="${verkaufsanzeige_form.values['beschreibung'][0]}"/></textarea>
                </div>
                <label for="preisArt">Preis:</label>
                <div class="side-by-side">
                    <select name="preisArt">
                        <c:forEach items="${preisArten}" var="preisArt">
                            <option value="${preisArt}" ${verkaufsanzeige_form.values["preisArt"][0] == preisArt ? 'selected' : ''}>
                                <c:out value="${preisArt.label}" />
                            </option>
                        </c:forEach>
                    </select>
                    <input type="text" name="preis" value="${verkaufsanzeige_form.values["preis"][0]}">
                </div>
                <label for="erstellungsdatum">
                    Angelegt am:
                </label>
                <div class="side-by-side">
                    ${verkaufsanzeige_form.values['erstellungsdatum'][0]} ${verkaufsanzeige_form.values['erstellungszeit'][0]}
                </div>
                <label for="anbieter">
                    Anbieter:
                </label>
                <div class="side-by-side">
                    ${user.vorNachname} <br />
                    ${user.strasseHnr} <br />
                    ${user.plz} ${user.ort} <br />
                    ${user.telefonnr} <br/> 
                    ${user.email} 
                </div>
                
              <%-- Button zum Abschicken --%>
                <div class="side-by-side">
                    <button class="icon-pencil" type="submit" name="action" value="save">
                        Sichern
                    </button>

                    <c:if test="${edit}">
                        <button class="icon-trash" type="submit" name="action" value="delete">
                            Löschen
                        </button>
                    </c:if>
                </div>
            </div>

            <%-- Fehlermeldungen --%>
            <c:if test="${!empty verkaufsanzeige_form.errors}">
                <ul class="errors">
                    <c:forEach items="${verkaufsanzeige_form.errors}" var="error">
                        <li>${error}</li>
                        </c:forEach>
                </ul>
            </c:if>
        </form>
    </jsp:attribute>
</template:base>