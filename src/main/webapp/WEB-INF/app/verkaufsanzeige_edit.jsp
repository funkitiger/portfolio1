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
            <a href="<c:url value="/app/tasks/"/>">Übersicht</a>
        </div>
    </jsp:attribute>

    <jsp:attribute name="content">
        <form method="post" class="stacked">
            <div class="column">
                <%-- CSRF-Token --%>
                <input type="hidden" name="csrf_token" value="${csrf_token}">

                <%-- Eingabefelder --%>
                <label for="task_category">Kategorie:</label>
                <div class="side-by-side">
                    <select name="category">

                        <c:forEach items="${kategorien}" var="category">
                            <option value="${kategorie.id}" ${verkaufsanzeige_form.values["kategorie"][0] == category.id ? 'selected' : ''}>
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
                        <c:forEach items="${angebotArten}" var="category">
                            <option value="${angebotArt.id}" ${verkaufsanzeige_form.values["angebotArt"][0] == angebotArt.id ? 'selected' : ''}>
                                <c:out value="${angebotArt.name}" />
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
                            <option value="${preisArt.id}" ${verkaufsanzeige_form.values["preisArt"][0] == preisArt.id ? 'selected' : ''}>
                                <c:out value="${preisArt.name}" />
                            </option>
                        </c:forEach>
                    </select>
                    <input type="text" name="preis" value="${verkaufsanzeige_form.values["preis"][0]}">
                </div>
                <label for="erstellungsdatum">
                    Angelegt am:
                </label>
                <div class="side-by-side">
                    ${verkaufsanzeige_form.values['erstellungsdatum'][0]}
                </div>
                <label for="anbieter">
                    Anbieter:
                </label>
                <div class="side-by-side">
                    ${verkaufsanzeige_form.values['anbieter.vorNachname'][0]} <br />
                    ${verkaufsanzeige_form.values['anbieter.strasseHausnr'][0]} <br />
                    ${verkaufsanzeige_form.values['anbieter.plz'][0]} ${verkaufsanzeige_form.values['anbieter.ort'][0]} <br />
                    ${verkaufsanzeige_form.values['anbieter.telefonnr'][0]} <br/> 
                    ${verkaufsanzeige_form.values['anbieter.email'][0]} 
                </div>
                
                
                
                <label for="task_due_date">
                    Fällig am:
                    <span class="required">*</span>
                </label>
                <div class="side-by-side">
                    <input type="text" name="task_due_date" value="${task_form.values["task_due_date"][0]}">
                    <input type="text" name="task_due_time" value="${task_form.values["task_due_time"][0]}">
                </div>

                <label for="task_status">
                    Status:
                    <span class="required">*</span>
                </label>
                <div class="side-by-side margin">
                    <select name="task_status">
                        <c:forEach items="${statuses}" var="status">
                            <option value="${status}" ${task_form.values["task_status"][0] == status ? 'selected' : ''}>
                                <c:out value="${status.label}"/>
                            </option>
                        </c:forEach>
                    </select>
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
            <c:if test="${!empty task_form.errors}">
                <ul class="errors">
                    <c:forEach items="${task_form.errors}" var="error">
                        <li>${error}</li>
                        </c:forEach>
                </ul>
            </c:if>
        </form>
    </jsp:attribute>
</template:base>