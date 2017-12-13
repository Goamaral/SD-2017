<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>iVotas</title>
	</head>
	<body>
		<h1>Index</h1>
		<s:form action="login">
			<s:textfield name="cc" label="Cartao cidadao" />
			<s:textfield name="username" label="Numero cartao" />
			<s:textfield name="password" label="Password" type="password" />
			<s:submit />
		</s:form>
	</body>
</html>