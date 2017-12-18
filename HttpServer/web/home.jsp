<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>iVotas</title>
	</head>
	<body>
		<h1>iVotas</h1>
		<s:form action="login">
			<s:textfield name="cc" label="Cartao cidadao"/>
			<s:textfield name="username" label="Numero cartao" required="true"/>
			<s:textfield name="password" label="Password" type="password" required="true"/>
			<s:submit value="Login"/>
		</s:form>
	</body>
</html>