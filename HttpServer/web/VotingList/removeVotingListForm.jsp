<%@ taglib prefix="s" uri="/struts-tags"%>

<s:form action="removeVotingList">
    <s:select list="electionsIDs" id="electionsIDs" />
    <s:select list="elections" label="Elecicao"  id="elections" onchange="loadVotingLists()"/>
    <s:select name="votingList.id" list="votingListsIDs" id="votingListsIDs" />
    <s:select list="votingLists" label="Lista" id="votingLists" onchange="selectVotingListID()" />
    <s:submit value="Remover" />
</s:form>