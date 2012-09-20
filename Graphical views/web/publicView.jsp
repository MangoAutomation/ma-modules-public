<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@ taglib prefix="views" tagdir="/WEB-INF/tags/graphicalViews" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html>
<head>
  <title><fmt:message key="header.title"/></title>
  <c:set var="dojoURI">http://ajax.googleapis.com/ajax/libs/dojo/1.7.2/</c:set>
  
  <!-- Style -->
  <link rel="icon" href="images/favicon.ico"/>
  <link rel="shortcut icon" href="images/favicon.ico"/>
  <link href="resources/common.css" type="text/css" rel="stylesheet"/>
  <style type="text/css">
    @import "${dojoURI}/dojox/editor/plugins/resources/css/StatusBar.css";
    @import "${dojoURI}/dojox/layout/resources/FloatingPane.css";
    @import "${dojoURI}/dijit/themes/${theme}/${theme}.css";
    @import "${dojoURI}/dojo/resources/dojo.css";
  </style>  
  
  <!-- Script -->
  <script type="text/javascript" src="${dojoURI}/dojo/dojo.js" data-dojo-config="async: false, parseOnLoad: true, isDebug:true, extraLocale: ['en-us', 'nl', 'nl-nl', 'ja-jp', 'fi-fi', 'sv-se', 'zh-cn', 'zh-tw','xx']"></script>
  <script type="text/javascript" src="dwr/engine.js"></script>
  <script type="text/javascript" src="dwr/util.js"></script>
  <script type="text/javascript" src="resources/common.js"></script>
  <script type="text/javascript" src="dwr/interface/GraphicalViewDwr.js"></script>
  <script type="text/javascript" src="dwr/interface/MiscDwr.js"></script>
  <script type="text/javascript" src="resources/view.js"></script>
  <script type="text/javascript" src="resources/header.js"></script>
  <script type="text/javascript" src="${modulePath}/web/graphicalViews.js"></script>
  <script type="text/javascript" src="${modulePath}/web/wz_jsgraphics.js"></script>
</head>

<body style="background-color:transparent">
  <views:displayView view="${view}" emptyMessageKey="publicView.notFound"/>
  
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