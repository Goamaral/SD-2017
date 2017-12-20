<%@ taglib prefix="s" uri="/struts-tags"%>

<s:form>
    <s:select label="Elecicao" list="elections"/>
    <s:select name="votingList.electionID" list="electionsIDs" />
    <s:textfield label="Nome" name="votingList.name" />
</s:form>