<%--
    Copyright (C) 2014 Infinite Automation. All rights reserved.
    @author Terry Packer
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>


<tag:page dwr="M2MReportImportDwr">
<script type="text/javascript" src="${modulePath}/web/m2mReportImport.js"></script>

	<table id="migrationForm">
	    <tr>
	      <td class="smallTitle" colspan="2">
	        <fmt:message key="reports.import"/>
	        <tag:help id="m2mReportImport"/>
	    </td>
	    <tr>
	      <td class="formLabelRequired"><fmt:message key="reports.import.driverClassName"/></td>
	      <td class="formField"><input id="driverClassname" type="text" value="com.mysql.jdbc.Driver"/></td>
	    </tr>
	    
	    <tr>
	      <td class="formLabelRequired"><fmt:message key="reports.import.connectionString"/></td>
	      <td class="formField"><input id="connectionUrl" type="text" class="formLong" value="jdbc:mysql://localhost/mangoLoad"/></td>
	    </tr>
	    
	    <tr>
	      <td class="formLabelRequired"><fmt:message key="reports.import.username"/></td>
	      <td class="formField"><input id="username" type="text" value="mango"/></td>
	    </tr>
	    
	    <tr>
	      <td class="formLabelRequired"><fmt:message key="reports.import.password"/></td>
	      <td class="formField"><input id="password" type="text" value='mango' /></td>
	    </tr>
    <tr>
      <td style="text-align:center">
        <button type="button" onClick="migrate();"><fmt:message key="reports.import.migrateNow"/></button>
      
        </td>
      <td style="text-align:center">
        <button type="button" onClick="generateJson();"><fmt:message key="reports.import.generateJson"/></button>
      </td>
    </tr>
    <tr>
        <td colspan="2">
            <table>
                <tbody id="migrationMessages"></tbody>
            </table>
        </td>
      </tr>
	</table>
  <textarea rows="40" cols="150" id="jsonOutput" style="display:none"></textarea>
</tag:page>