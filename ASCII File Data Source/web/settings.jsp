<%--
    Copyright (C) 2017 Infinite Automation Systems Inc. All rights reserved.
    @author Phillip Dunlap
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="com.infiniteautomation.asciifile.AsciiFileSystemSettingsDefinition"%>
<%@page import="com.serotonin.m2m2.Common" %>

<c:set var="restrictedPathKey"><%= AsciiFileSystemSettingsDefinition.RESTRICTED_PATH %></c:set>
<c:set var="restrictedPathDefault"><%= Common.MA_HOME %></c:set>


<script type="text/javascript" src="/dwr/interface/AsciiFileEditDwr.js"></script>
<script>
function saveAsciiFileReaderSystemSettings() {
	var settings = {};
	
	setUserMessage("saveAsciiFileSettingsMessage");
    setDisabled("saveAciiFileReaderSettings", true);
    
    var restrictedPath = $get("${restrictedPathKey}");
    settings["${restrictedPathKey}"] =  restrictedPath;

	AsciiFileEditDwr.validateSettings(settings, function(response) {
        if(!response.hasMessages) {
        	SystemSettingsDwr.saveSettings(settings, function(){
        		setDisabled("saveAciiFileReaderSettings", false);
            	setUserMessage("saveAsciiFileSettingsMessage", "<fmt:message key='common.saved'/>");
        	});
        }
        else
        	setUserMessage("saveAsciiFileSettingsMessage", response.messages[0]);
  });
}
</script>

   <%--Anchor for referencing events --%>
    <table id="asciiFileDataSourceTab">    
      <%--Performance Settings Here --%>
      <tr>
        <td class="formLabelRequired"><fmt:message key="dsEdit.file.restrictedPaths"/></td>
        <td class="formField">
          <c:set var="restrictedPathValue"><m2m2:systemSetting key="${restrictedPathKey}" defaultValue="${restrictedPathDefault}"/></c:set>
          <input id="${restrictedPathKey}" type="text" value="${restrictedPathValue}"></input>
        </td>
      </tr>
      <tr>
        <td colspan="2" align="center">
          <input id="saveAciiFileReaderSettings" type="button" value="<fmt:message key="common.save"/>" onclick="saveAsciiFileReaderSystemSettings();"/>
          <tag:help id="asciiFileSystemSettings"/>
        </td>
      </tr>
      <tr><td colspan="2" id="saveAsciiFileSettingsMessage" class="formError"></td></tr>
    </table>