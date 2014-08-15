<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp"%>

<style>
.test-button {
	box-shadow:inset 0px 1px 0px 0px #dcecfb;
	background-color:#80b5ea;
	border:1px solid #53a8fc;
	color:#000000;
	font-size:12px;
	font-weight:bold;
	height:20px;
	line-height:17px;
	width:100px;
	text-align:center;
	text-shadow:1px 1px 0px #528ecc;
}
.test-button:hover {
	background-color:#70a5da;
}.test-button:active {
	position:relative;
	top:1px;
}
</style>

<script type="text/javascript">

    /* On Page Load */
	function initImpl() {
	    
	      logIOChanged();
	    
	      SerialEditDwr.getSafeTerminator(function(messageTerminator) {
			$set("messageTerminator", messageTerminator);
			if(${dataSource.useTerminator}) {
				dojo.byId("useTerminator").checked = "true";
			}
			toggleTerminator();
		  });
	}

	/**
	 * Save the DS
	 */
	function saveDataSourceImpl(basic){

		  SerialEditDwr.saveSerialDataSource(basic,
	              $get("commPortId"),$get("baudRate"),$get("flowControlIn"),$get("flowControlOut"),$get("dataBits"), 
	              $get("stopBits"),$get("parity"),$get("readTimeout"),$get("useTerminator"),$get("messageTerminator"),
	              $get("messageRegex"),$get("pointIdentifierIndex"),$get("isHex"),$get("isLogIO"),$get("maxMessageSize"),
	              $get("ioLogFileSizeMBytes"), $get("maxHistoricalIOLogs"),saveDataSourceCB);

	}

	/**
	 * Add a Point
	 */
	  function addPointImpl() {
		  DataSourceEditDwr.getPoint(-1, function(point) {
			  editPointCB(point);
		  });
	  }
		
	  function editPointCBImpl(locator) {
		  $set("pointIdentifier",locator.pointIdentifier);
		  $set("valueRegex",locator.valueRegex);
		  $set("valueIndex",locator.valueIndex);
		  $set("dataTypeId",locator.dataTypeId);
	  }
	  
	  /**
	   * Save a Point
	   */
	  function savePointImpl(locator) {
		  delete locator.pointIdentifier;
		  delete locator.valueRegex;
		  delete locator.valueIndex;
		  delete locator.dataTypeId;
		  
		  locator.pointIdentifier = $get("pointIdentifier");
		  locator.valueRegex = $get("valueRegex");
		  locator.valueIndex = $get("valueIndex");
		  locator.dataTypeId = $get("dataTypeId");
		  
	      SerialEditDwr.savePointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
	  }
	  
	  /**
	   * Toggle Terminator row
	   */
	  function toggleTerminator() {
		  if(!dojo.byId("useTerminator").checked) {
			  $("terminatorRow").style.visibility = "collapse";
		  	  //$("messageRegexRow").style.visibility = "collapse";
		  	  //$("identifierIndexRow").style.visibility = "collapse";
	  	  } else { 
			  $("terminatorRow").style.visibility = "visible";
			  //$("messageRegexRow").style.visibility = "visible";
			  //$("identifierIndexRow").style.visibility = "visible";
		  }
	  }
	  
	  /**
	   * Runs a test string
	   */
	  function submitTestString() {
		  SerialEditDwr.testString($get("testString"), displayResult);
	  }
	  
	  function displayResult(resp) {
		  if(resp.hasMessages) {
			  var message = "";
			  for(k in resp.messages)
				  message += resp.messages[k].contextualMessage + "<br>"
			  $("testMessages").style.visibility = "visible";
			  $("testMessages").innerHTML = message;
		  }
		  else {
			  $("testMessages").innerHTML = "";
			  $("testMessages").style.visibility = "collapse";
		  }
	  }
	  
	  function logIOChanged() {
	      if ($get("isLogIO")){
	          show("ioLogPathMsg");
	          show("maxHistoricalIOLogs_row");
	          show("ioLogFileSizeMBytes_row");
	      }else{
	          hide("ioLogPathMsg");
	          hide("ioLogFileSizeMBytes_row");
	          hide("maxHistoricalIOLogs_row");
	      }
	  }
	  
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.serial.desc" helpId="serialDS">
<tag:serialSettings/>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.readTimeout"/></td>
 <td><input id="readTimeout" type="number" value="${dataSource.readTimeout}"></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.useTerminator"/></td>
 <td><input id="useTerminator" type="checkbox" onchange="toggleTerminator()"></input></td>
</tr>
<tr id="terminatorRow">
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.messageTerminator"/></td>
 <td><input id="messageTerminator" type="text"></input></td>
</tr>
<tr id="messageRegexRow">
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.messageRegex"/></td>
 <td><input id="messageRegex" type="text" value="${dataSource.messageRegex}"></input></td>
</tr>
<tr id="identifierIndexRow">
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.pointIdentifierIndex"/></td>
 <td><input id="pointIdentifierIndex" type="number" value="${dataSource.pointIdentifierIndex}"></input></td>
</tr>
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.serial.hex"/></td>
      <td class="formField"><sst:checkbox id="isHex" selectedValue="${dataSource.hex}"/></td>
    </tr>
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.serial.maxMessageSize"/></td>
      <td class="formField"><input type="number" id="maxMessageSize" value="${dataSource.maxMessageSize}"/></td>
    </tr>
    
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.serial.logIO"/></td>
      <td class="formField">
        <sst:checkbox id="isLogIO" selectedValue="${dataSource.logIO}" onclick="logIOChanged()"/>
        <div id="ioLogPathMsg">
          <fmt:message key="dsEdit.serial.log">
            <fmt:param value="${dataSource.ioLogPath}"/>
          </fmt:message>
        </div>
      </td>
    </tr>
    <tr id="ioLogFileSizeMBytes_row">
      <td class="formLabelRequired"><fmt:message key="dsEdit.serial.logIOFileSize"/></td>
      <td class="formField"><input id="ioLogFileSizeMBytes" type="number" value="${dataSource.ioLogFileSizeMBytes}"/></td>
    </tr>
    <tr id="maxHistoricalIOLogs_row">
      <td class="formLabelRequired"><fmt:message key="dsEdit.serial.logIOFiles"/></td>
      <td class="formField"><input id="maxHistoricalIOLogs" type="number" value="${dataSource.maxHistoricalIOLogs}"/></td>
    </tr>
    
<tr>
 <td class="formLabel" style="padding-top:0px;"><button onclick=submitTestString();><fmt:message key="dsEdit.serial.submitTestString"/></button></td>
 <td><input id="testString" type="text"></input></td>
</tr>
<tr><td id="testMessages" style="color:red;" colspan=2></td>
</tr>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="serialPP">
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.pointDataType"/></td>
    <td class="formField"><tag:dataTypeOptions id="dataTypeId" excludeImage="true"/></td>
  </tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.pointIdentifier"/></td>
 <td><input id="pointIdentifier" type="text" ></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.valueIndex"/></td>
 <td><input id="valueIndex" type="number" ></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.valueRegex"/></td>
 <td><input id="valueRegex" type="text" ></input></td>
</tr>

</tag:pointList>