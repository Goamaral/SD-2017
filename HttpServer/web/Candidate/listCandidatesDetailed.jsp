<%@ taglib prefix="s" uri="/struts-tags" %>

<h2>Lista de Candidatos</h2>
<s:select list="electionsIDs" id="electionsIDs" style="display: none" />
<s:select list="elections" label="Elecicao"  id="elections" onchange="loadVotingListsSecundary()"/>
<s:select name="votingListID" list="votingListsIDs" id="votingListsIDs" style="display: none" />
<s:select list="votingLists" label="Lista" id="votingLists" onchange="loadCandidatesBeans()" />
<div id="candidates">
    <s:iterator value="candidatesBeans">
        <p>Nome: <s:property value="name"/> - CC: <s:property value="cc"/></p>
    </s:iterator>
</div>
