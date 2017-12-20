<%@ taglib prefix="s" uri="/struts-tags"%>

<s:form action="removeVotingList">
    <s:select list="electionsIDs" id="electionsIDs" style="display: none" />
    <s:select list="elections" label="Elecicao"  id="elections" onchange="loadVotingLists()"/>
    <s:select name="votingListID" list="votingListsIDs" id="votingListsIDs" style="display: none" />
    <s:select list="votingLists" label="Lista" id="votingLists" onchange="selectVotingListID()" />
    <s:submit value="Remover" />
</s:form>