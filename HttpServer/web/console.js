function ajax(method, url, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open(method, url);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            callback(xhr.responseText);
        }

    };

    xhr.send(null);
}

function render(newContent) {
    var console = document.getElementById("console");
    console.childNodes = [];
    console.innerHTML = newContent;
}

function logout() {
    window.location.href = "/logout.action";
}

function ajaxRender(url) {
    var callback = function (newContent) {
        render(newContent);
    };

    ajax("GET", url, callback);
}

function loadDepartments(type) {
    if (type === undefined) type = "";
    var facultiesSelect = document.getElementById("faculties");
    var selectedFacultyName = facultiesSelect.options[facultiesSelect.selectedIndex].text;
    var parent = document.getElementById("departments").parentNode.parentNode;

    var callback = function (newContent) {
        parent.innerHTML = newContent;
        var departmentsSelect = parent.children[1].children[0];
        var baseName = type;
        var extra = "";
        switch (type) {
            case "person":
                 extra =".departmentName";
                break;
            case "department":
                extra = ".name";
                break;
        }

        departmentsSelect.name = baseName + extra;
    }

    ajax("GET", "/listDepartments.action?facultyName=" + selectedFacultyName, callback);
}

function loadDepartmentsForPerson() {
    loadDepartments("person");
}

function loadDepartmentsForDepartment() {
    loadDepartments("department");
}

function updateNewFaculty() {
    var newFacultyName = document.getElementById("newFacultyName");
    var facultiesSelect = document.getElementById("faculties");
    newFacultyName.value = facultiesSelect.options[facultiesSelect.selectedIndex].text;
}

function getMainMenu() {
    var departmentsSelect = document.getElementById("departments");
    var selectedDepartmentName = departmentsSelect.options[departmentsSelect.selectedIndex].text;

    ajaxRender("/mainMenu.action?electionSubtype=" + selectedDepartmentName);
}

function selectElectionID() {
    var electionsSelect = document.getElementById("elections");
    var electionsIDsSelect = document.getElementById("electionsIDs");

    electionsIDsSelect.selectedIndex = electionsSelect.selectedIndex;

    return electionsIDsSelect.options[electionsIDsSelect.selectedIndex].text;
}

function loadVotingLists() {
    var electionID = selectElectionID();

    var callback = function (newContent) {
        var block =  document.createElement("div");
        block.innerHTML = newContent;


        var votingListsIDsSelectParentNode = document.getElementById("votingListsIDs").parentNode;
        votingListsIDsSelectParentNode.innerHTML = "";
        votingListsIDsSelectParentNode.appendChild(block.children[0]);

        var votingListsSelectParentNode = document.getElementById("votingLists").parentNode.parentNode;
        votingListsSelectParentNode.children[0].innerHTML = "";
        votingListsSelectParentNode.children[1].innerHTML = "";
        votingListsSelectParentNode.children[0].appendChild(block.children[0]);
        votingListsSelectParentNode.children[1].appendChild(block.children[0]);
    }

    ajax("GET", "/listVotingLists.action?electionID=" + electionID, callback);
}

function selectVotingListID() {
    var votingListsSelect = document.getElementById("votingLists");
    var votingListsIDsSelect = document.getElementById("votingListsIDs");

    votingListsIDsSelect.selectedIndex = votingListsSelect.selectedIndex;

    return votingListsIDsSelect.options[votingListsIDsSelect.selectedIndex];
}

function selectPersonCC() {
    var peopleSelect = document.getElementById("people");
    var peopleCCsSelect = document.getElementById("peopleCCs");

    peopleCCsSelect.selectedIndex = peopleSelect.selectedIndex;
}

function loadCandidates() {
    var votingListID = selectVotingListID();

    var callback = function (newContent) {
        var block =  document.createElement("div");
        block.innerHTML = newContent;


        var candidatesCCsSelectParentNode = document.getElementById("candidatesCCs").parentNode;
        candidatesCCsSelectParentNode.innerHTML = "";
        candidatesCCsSelectParentNode.appendChild(block.children[0]);

        var candidatesSelectParentNode = document.getElementById("candidates").parentNode.parentNode;
        candidatesSelectParentNode.children[0].innerHTML = "";
        candidatesSelectParentNode.children[1].innerHTML = "";
        candidatesSelectParentNode.children[0].appendChild(block.children[0]);
        candidatesSelectParentNode.children[1].appendChild(block.children[0]);
    }

    ajax("GET", "/listCandidates.action?votingListID=" + votingListID, callback);
}

function selectCandidateCC() {
    var candidatesSelect = document.getElementById("candidates");
    var candidatesCCsSelect = document.getElementById("candidatesCCs");

    candidatesCCsSelect.selectedIndex = candidatesSelect.selectedIndex;
}

function loadCandidatesBeans() {
    var votingListID = selectVotingListID();

    var callback = function (newContent) {
        document.getElementById("candidates").innerHTML = newContent;
    }

    ajax("GET", "/listCandidatesBeans.action?votingListID=" + votingListID, callback);
}