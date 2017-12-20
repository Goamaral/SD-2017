<%@ taglib prefix="s" uri="/struts-tags" %>

<h2>Remover membro</h2>
<s:form action="removeCandidate">
    <s:select list="electionsIDs" id="electionsIDs" style="display: none" />
    <s:select list="elections" label="Elecicao"  id="elections" onchange="loadVotingLists()"/>
    <s:select name="votingListID" list="votingListsIDs" id="votingListsIDs" style="display: none" />
    <s:select list="votingLists" label="Lista" id="votingLists" onchange="loadCandidates()" />
    <s:select name="candidateCC" list="candidatesCCs" id="candidatesCCs" style="display: none" />
    <s:select list="candidates" label="Membro" id="candidates" onchange="selectCandidateCC()" />
    <s:submit value="Remover" />
</s:form>