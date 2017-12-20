<%@ taglib prefix="s" uri="/struts-tags"%>

<s:form action="createVotingList">
    <s:select name="votingList.electionID" list="electionsIDs" id="electionsIDs" style="display: none" />
    <s:select label="Elecicao" list="elections" id="elections" onchange="selectElectionID()"/>
    <s:textfield name="votingList.name" label="Nome" />
    <s:submit value="Criar" />
</s:form>