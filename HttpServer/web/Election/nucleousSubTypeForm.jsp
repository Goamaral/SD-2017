<%@ taglib prefix="s" uri="/struts-tags"%>

<h2>Departamento onde se localiza o Nucleo</h2>
<s:select label="Faculdade" list="faculties" id="faculties" onchange="loadDepartments()"/>
<s:select label="Departamento" list="departments" id="departments"/>
<button onclick="getMainMenu()">Escolher</button>