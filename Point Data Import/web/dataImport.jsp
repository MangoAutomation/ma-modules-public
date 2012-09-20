<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<tag:page>
  <form action="dataImport.shtm" method="post" enctype="multipart/form-data">
    <table>
      <tr>
        <td class="formLabelRequired"><fmt:message key="dataImport.importFile"/></td>
        <td><input type="file" name="uploadFile"/></td>
      </tr>
      <c:if test="${!empty error}">
        <tr><td colspan="2" class="formError">${error}</td></tr>
      </c:if>
      <c:if test="${!empty result}">
        <tr><td colspan="2" class="formError">${result}</td></tr>
      </c:if>
      
      <tr>
        <td colspan="2" align="center">
          <input type="submit" value="<fmt:message key="dataImport.upload"/>" name="upload"/>
        </td>
      </tr>
    </table>
  </form>
</tag:page>