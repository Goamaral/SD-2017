function ajax(method, url, callback) {
    var xhr = new XMLHttpRequest();
    xhr.open(method, url);

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4) {
            if (xhr.status === 200)
                callback(xhr.responseText);
        } else {
            callback("Error: " + xhr.status);
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
    var selectedFacultyName = facultiesSelect.options[facultiesSelect.selectedIndex].text;

    newFacultyName.value = selectedFacultyName;
}

function getCreateElectionForm() {
    var departmentsSelect = document.getElementById("departments");
    var selectedDepartmentName = departmentsSelect.options[departmentsSelect.selectedIndex].text;

    ajaxRender("/createElectionForm.action?electionSubtype=" + selectedDepartmentName);
}