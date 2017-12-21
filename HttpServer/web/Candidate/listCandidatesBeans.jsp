<%@ taglib prefix="s" uri="/struts-tags" %>

<s:iterator value="candidatesBeans">
    <p><s:property value="name"/> - CC: <s:property value="cc"/></p>
</s:iterator>