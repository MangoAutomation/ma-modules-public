<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ page import="com.serotonin.m2m2.Common" %>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="views" tagdir="/WEB-INF/tags/graphicalViews" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html>
<head>
  <title><fmt:message key="header.title"/></title>
  <c:set var="dojoURI">resources</c:set>
  
  <!-- Style -->
  <link rel="icon" href="images/favicon.ico"/>
  <link rel="shortcut icon" href="images/favicon.ico"/>
  <link href="resources/common.css" type="text/css" rel="stylesheet"/>
  <style type="text/css">
    @import "${dojoURI}/dojox/editor/plugins/resources/css/StatusBar.css";
    @import "${dojoURI}/dojox/layout/resources/FloatingPane.css";
    @import "${dojoURI}/dijit/themes/claro/claro.css";
    @import "${dojoURI}/dojo/resources/dojo.css";
  </style>  
  
  <!-- Script -->
  <script type="text/javascript" src="${dojoURI}/dojo/dojo.js" data-dojo-config="async: false, parseOnLoad: true, isDebug:true, extraLocale: ['en-us', 'nl', 'nl-nl', 'ja-jp', 'fi-fi', 'sv-se', 'zh-cn', 'zh-tw','xx']"></script>
  <tag:versionedJavascript  src="/dwr/engine.js" />
  <tag:versionedJavascript  src="/dwr/util.js" />
  <tag:versionedJavascript  src="/resources/common.js" />
  <tag:versionedJavascript  src="/dwr/interface/GraphicalViewDwr.js" />
  <tag:versionedJavascript  src="/dwr/interface/MiscDwr.js" />
  <tag:versionedJavascript  src="/resources/view.js" />
  <tag:versionedJavascript  src="/resources/header.js" />
  <tag:versionedJavascript  src="${modulePath}/web/graphicalViews.js" />
  <tag:versionedJavascript  src="${modulePath}/web/wz_jsgraphics.js" />
<%--   <c:forEach items="<%= Common.applicationScripts %>" var="modScript"> --%>
<%--     <tag:versionedJavascript  src="/${modScript}" /></c:forEach> --%>
</head>

<body style="background-color:transparent">
  <%-- This table is here so that the styles are the same from the editor to the view --%>
  <table width="100%" cellspacing="0" cellpadding="0">
    <tr>
      <td>
        <views:displayView view="${view}" emptyMessageKey="publicView.notFound"/>
      </td>
    </tr>
  </table>  
  <c:if test="${!empty view}">
    <script type="text/javascript">
      mango.i18n = <sst:convert obj="${clientSideMessages}"/>;
      dwr.util.setEscapeHtml(false);
      mango.view.initAnonymousView(${view.id});
      dojo.ready(mango.longPoll.start);
    </script>
  </c:if>
</body>
</html>