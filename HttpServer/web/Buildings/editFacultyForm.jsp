<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Editar Faculdade</h2>
<s:form action="editFaculty">
    <s:select name="faculty.name" label="Escolha Faculdade" list="faculties" id="faculties" onchange="updateNewFaculty()"/>
    <s:textfield name="newFaculty.name" label="Nome" id="newFacultyName" value="%{facultyName}"/>
    <s:submit value="Guardar" />
</s:form>