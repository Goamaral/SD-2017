<%@ taglib prefix="s" uri="/struts-tags"%>

this.name = name;
this.description = description;
this.type = type;
this.subtype = subtype;
this.start = start;
this.end = end;

<h2>Criar Elecicao</h2>
<s:form action="createElection">
    <s:hidden name="election.type" value="%{electionType}" />
    <s:hidden name="election.subtype" value="%{electionSubtype}" />
    <s:textfield name="election.name" label="Nome" required="true"/>
    <s:textfield name="election.description" label="Descricao" required="true"/>
    <s:textfield name="election.start" label="Data inicio" required="true"/>
    <s:textfield name="election.end" label="Data fim" required="true"/>
    <s:submit value="Criar"/>
</s:form>