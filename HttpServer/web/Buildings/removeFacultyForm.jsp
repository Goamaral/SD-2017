<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Remover Faculdade</h2>
<s:form action="removeFaculty">
    <s:select name="faculty.name" label="Escolha Faculdade" list="faculties"/>
    <s:submit value="Remover" />
</s:form>