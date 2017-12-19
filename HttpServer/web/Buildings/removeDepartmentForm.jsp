<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Remover Departamento</h2>
<s:form action="removeDepartment">
    <s:select name="department.facultyName" label="Faculdade" list="faculties" id="faculties" onchange="loadDepartments()"/>
    <s:select name="department.name" label="Departamento" list="departments" id="departments"/>
    <s:submit value="Remover" />
</s:form>