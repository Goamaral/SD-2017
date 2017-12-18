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

function loadDepartments() {
    var facultiesSelect = document.getElementById("faculties");
    var selectedFacultyName = facultiesSelect.options[facultiesSelect.selectedIndex].text;
    var parent = document.getElementById("departments").parentNode.parentNode;

    var callback = function (newContent) {
        parent.innerHTML = newContent;
    }

    ajax("GET", "/listDepartments.action?facultyName=" + selectedFacultyName, callback);
}

function updateNewFaculty() {
    var newFacultyName = document.getElementById("newFacultyName");
    var facultiesSelect = document.getElementById("faculties");
    var selectedFacultyName = facultiesSelect.options[facultiesSelect.selectedIndex].text;

    newFacultyName.value = selectedFacultyName;
}