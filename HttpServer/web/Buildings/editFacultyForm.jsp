<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Editar Faculdade</h2>
<s:form action="editFaculty">
    <s:select name="facultyName" label="Escolha Faculdade" list="faculties" id="faculties" onchange="updateNewFaculty()"/>
    <s:textfield name="newFacultyName" label="Nome" id="newFacultyName" value="%{facultyName}"/>
    <s:submit value="Guardar" />
</s:form>