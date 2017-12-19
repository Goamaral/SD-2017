<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Criar Departamento</h2>
<s:form action="createDepartment">
    <s:select name="department.facultyName" label="Faculdade" list="faculties"/>
    <s:textfield name="department.name" label="Nome"/>
    <s:submit value="Criar" />
</s:form>