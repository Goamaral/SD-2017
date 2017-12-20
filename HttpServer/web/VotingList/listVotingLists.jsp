<%@ taglib prefix="s" uri="/struts-tags" %>

<s:select name="votingList.id" list="votingListsIDs" id="votingListsIDs" />
<s:select list="votingLists" label="Lista" id="votingLists" onchange="selectVotingListID" />
