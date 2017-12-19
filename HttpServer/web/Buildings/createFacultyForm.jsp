<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Criar Faculdade</h2>
<s:form action="createFaculty">
    <s:textfield name="faculty.name" label="Nome"/>
    <s:submit value="Criar" />
</s:form>