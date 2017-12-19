<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Registar</h2>
<s:form action="registerMember">
    <s:hidden name="person.type" value="%{personType}" />
    <s:textfield name="person.name" label="Nome" required="true"/>
    <s:textfield name="person.number" label="Numero cartao" required="true"/>
    <s:textfield name="person.password" label="Password" type="password" required="true"/>
    <s:textfield name="person.address" label="Morada" required="true"/>
    <s:textfield name="person.phone" label="Telemovel" required="true"/>
    <s:textfield name="person.cc" label="Cartao Cidadao" required="true"/>
    <s:textfield name="person.ccExpire" label="Data expiracao" required="true"/>
    <s:select label="Faculdade" list="faculties" id="faculties" onchange="loadDepartmentsForPerson()"/>
    <s:select name="person.departmentName" label="Departamento" list="departments" id="departments"/>
    <s:submit value="Registar"/>
</s:form>