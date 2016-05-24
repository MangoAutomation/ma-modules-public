<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<tag:page>
<div id="dataImportDiv" style="display:none">
  <tag:help id="import-help"/>
  <div id="upload-type-choice"> </div>
  <jsp:include page="/WEB-INF/snippet/view/pointValue/pointValueEmport.jsp"/>
</div>  
<script type="text/javascript" src="${modulePath}/web/js/dataImport.js"></script>
</tag:page>