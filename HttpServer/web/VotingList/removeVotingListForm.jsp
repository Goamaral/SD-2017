<%@ taglib prefix="s" uri="/struts-tags"%>

<s:form action="removeVotingList">
    <s:select list="electionsIDs" id="electionsIDs" style="display: none" />
    <s:select list="elections" label="Elecicao"  id="elections" onchange="loadVotingLists()"/>
    <s:select list="votingLists" label="Lista" id="votingLists" />
    <s:submit value="Criar" />
</s:form