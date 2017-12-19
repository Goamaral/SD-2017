<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Editar Departamento</h2>
<s:form action="editDepartment">
    <s:select name="department.facultyName" label="Faculdade" list="faculties" id="faculties"
          onchange="loadDepartmentsForDepartment()"/>
    <s:select name="department.name" label="Departamento" list="departments" id="departments"/>
    <s:select name="newDepartment.facultyName" label="Nova Faculdade" list="faculties"/>
    <s:textfield name="newDepartment.name" label="Novo Nome" />
    <s:submit value="Guardar" />
</s:form>