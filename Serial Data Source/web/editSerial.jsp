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
	              $get("ioLogFileSizeMBytes"), $get("maxHistoricalIOLogs"), $get("retries"), saveDataSourceCB);

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
		  SerialEditDwr.testString(
				  $get("testString"),
				  dataSources.currentId,
				  $get("messageRegex"),
				  $get("messageTerminator"),
	              $get("pointIdentifierIndex"),
	              $get("isHex"),
	              $get("useTerminator"),
				  displayResult);
	  }
	  
	  function displayResult(resp) {
		  dwr.util.removeAllRows("pointValuesTestResults");
		  
		  if(resp.hasMessages) {
			  //Error found
			  hide('testResults');
			  hide($("pointValuesTestResultsHeaders"));
			  hide($("pointValuesTestResultsEmpty"));
			  var message = "";
			  for(k in resp.messages)
				  message += resp.messages[k].contextualMessage + "<br>"
			  $("testMessages").style.visibility = "visible";
			  $("testMessages").innerHTML = message;
		  }
		  else {
			  show('testResults')
			  $("testMessages").innerHTML = "";
			  $("testMessages").style.visibility = "collapse";
			  //Show the results
			  
			  if(resp.data.results.length == 0){
				  show($("pointValuesTestResultsEmpty"));
	         	  hide($("pointValuesTestResultsHeaders"));
			  }else{
				  hide($("pointValuesTestResultsEmpty"));
		          show($("pointValuesTestResultsHeaders"));
				  dwr.util.addRows("pointValuesTestResults", resp.data.results,
			              [
			                function(data) { return data.message; },
			                function(data) { return data.name; },
			                function(data) { return data.identifier; },
			                function(data) { return data.value; },
			                function(data) { 
			                	if(data.success === 'true'){
			                		return 'match';
			                	}else{
			                		return data.error;
			                	}
			                }
			                  
			              ],
			              {
			                  rowCreator:function(options) {
			                      var tr = document.createElement("tr");
			                      tr.className = "smRow"+ (options.rowIndex % 2 == 0 ? "" : "Alt");
			                      return tr;
			                  },
			                  cellCreator: function(options){
				                	 var td = document.createElement("td");
									 td.style.textAlign='center';
									 if((options.rowData.success === 'false')&&(options.cellNum === 4))
										 td.style.color='red';
									 if((options.rowData.success === 'true')&&(options.cellNum === 4))
										 td.style.color='green';
				                	 return td;
				                }
			              });
			  }
			  
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
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.retries"/></td>
 <td><input id="retries" type="number" value="${dataSource.retries}"></input></td>
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
  <td colspan="2">
    <div class="borderDiv marB marR">
	  <table class="wide">
        <tr>
          <td class="smallTitle" colspan="2"><fmt:message key="dsEdit.serial.submitTestString"/></td>
        </tr>
        <tr> 
	 	  <td class="formLabel" style="padding-top:0px;"><input id="testString" type="text" style="width: 100%"></input></td>
	 	  <td><button onclick="submitTestString();" style="width:100%"><fmt:message key="common.test"/></button></td>
		</tr>
		<tr><td id="testMessages" style="color:red;" colspan=3></td></tr>
		<tr>
		 <td id="testResults" colspan="2">
		  <table class="wide">
		   <tbody id="pointValuesTestResultsEmpty" style="display:none;">
            <tr><th colspan="3"><fmt:message key="common.noData"/></th></tr>
           </tbody>
           <tbody id="pointValuesTestResultsHeaders" style="display:none;">
            <tr class="smRowHeader">
             <td>message</td>
             <td><fmt:message key="common.pointName"/></td>
             <td><fmt:message key="dsEdit.serial.pointIdentifier"/></td>
             <td><fmt:message key="common.value"/></td>
             <td>status</td>
            </tr>
           </tbody>
		 <tbody id="pointValuesTestResults"></tbody>
		</table>
	   </td>
	  </tr>
	</table>
	</div>
	</td>
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