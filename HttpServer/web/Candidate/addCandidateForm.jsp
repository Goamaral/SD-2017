<%@ taglib prefix="s" uri="/struts-tags" %>

<h2>Adicionar membro</h2>
<s:form action="addCandidate">
    <s:select list="electionsIDs" id="electionsIDs" style="display: none" />
    <s:select list="elections" label="Elecicao"  id="elections" onchange="loadVotingLists()"/>
    <s:select name="votingListID" list="votingListsIDs" id="votingListsIDs" style="display: none" />
    <s:select list="votingLists" label="Lista" id="votingLists" onchange="selectVotingListID()" />
    <s:select name="personCC" list="peopleCCs" id="peopleCCs" style="display: none" />
    <s:select list="people" label="Membro" id="people" onchange="selectPersonCC()" />
    <s:submit label="Adicionar" />
</s:form>