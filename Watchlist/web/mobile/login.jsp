<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="watchlist" tagdir="/WEB-INF/tags/watchlists" %>

<c:choose>
  <c:when test="${!empty loginResult && !loginResult.hasMessages}">
    <c:redirect url="/mobile/watch_list.shtm"/>
  </c:when>
  <c:otherwise>
    <watchlist:mobilePage>
      <form action="/mobile/login.htm" method="post">
        <table>
          <tr>
            <td class="formLabelRequired"><fmt:message key="login.userId"/></td>
            <td class="formField">
              <input id="username" type="text" name="username" value="${param.username}" maxlength="40"/>
            </td>
          </tr>
          <tr><td colspan="2" class="formError">${m2m2:contextualMessage('username', loginResult, pageContext)}</td></tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="login.password"/></td>
            <td class="formField">
              <input id="password" type="password" name="password" value="${param.password}" maxlength="20"/>
            </td>
          </tr>
          <tr><td colspan="2" class="formError">${m2m2:contextualMessage('password', loginResult, pageContext)}</td></tr>
          
          <c:set var="genmsg" value="${m2m2:genericMessages(loginResult, pageContext)}"/>
          <c:if test="${!empty genmsg}">
            <td colspan="2" class="formError">
              <c:forEach items="${genmsg}" var="error">
                <c:out value="${error}"/><br/>
              </c:forEach>
            </td>
          </c:if>
          
          <tr>
            <td colspan="2" align="center">
              <input type="submit" value="<fmt:message key="login.loginButton"/>"/>
            </td>
            <td></td>
          </tr>
        </table>
      </form>
      <br/>
      <br/>
    </watchlist:mobilePage>
  </c:otherwise>
</c:choose>