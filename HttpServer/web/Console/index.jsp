<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>iVotas</title>
        <script type="text/javascript" src="/Console/index.js"></script>
    </head>
    <body>
        <header>
            <h1 style="display: inline-block">iVotas - Consola de Administracao</h1>
            <button onclick="logout()" style="display: inline-block; float: right">Logout</button>
        </header>

        <div id="console">
            <button onclick="ajaxRender('/Core/chooseMemberType.jsp')">Registar membro</button>
        </div>
    </body>
</html>