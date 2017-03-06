<%--
    Copyright (C) 2013 Infinite Automation All rights reserved.
    @author Phillip Dunlap
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp"%>

<script type="text/javascript">

	/**
	 * Save the DS
	 */
	function saveDataSourceImpl(basic){

		AsciiFileEditDwr.saveFileDataSource(basic, $get("updatePeriods"), $get("updatePeriodType"), $get("filePath"), saveDataSourceCB);
	}
	
	function hideTSindex() {
		if(!dojo.byId("hasTimestamp").checked) {
			  $("timestampIndexRow").style.visibility = "collapse";
			  $("timestampFormatRow").style.visibility = "collapse";
		}
		  else {
			  $("timestampIndexRow").style.visibility = "visible";
			  $("timestampFormatRow").style.visibility = "visible";
		  }
	}
	
	
	/**
	 * Check if the file to be read exists and is readable.
	 */
	  function checkFile() {
		  fileTestButton(true);
		  AsciiFileEditDwr.checkIsFileReadable($get("filePath"), checkFileCB);
	  }
	  
	  function fileTestButton(testing) {
		  setDisabled($("fileTestButton"), testing);
	  }
	  
	  function checkFileCB(result) {
		  if(result)
	   		$set("fileTestMessage", "<fmt:message key="dsEdit.file.canRead"/>");
		  else
			$set("fileTestMessage", "<fmt:message key="dsEdit.file.cannotRead"/>")
	      fileTestButton(false);
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
		  $set("pointIdentifierIndex",locator.pointIdentifierIndex);
		  $set("valueIndex",locator.valueIndex);
		  $set("dataTypeId",locator.dataTypeId);
		  $set("hasTimestamp",locator.hasTimestamp);
		  $set("timestampIndex",locator.timestampIndex);
		  $set("timestampFormat",locator.timestampFormat);
		  hideTSindex();
	  }
	  
	  /**
	   * Save a Point
	   */
	  function savePointImpl(locator) {
		  delete locator.pointIdentifier;
		  delete locator.valueRegex;
		  delete locator.pointIdentifierIndex;
		  delete locator.valueIndex;
		  delete locator.dataType;
		  delete locator.hasTimestamp;
		  delete locator.timestampIndex;
		  delete locator.timestampFormat;
		  
		  locator.pointIdentifier = $get("pointIdentifier");
		  locator.valueRegex = $get("valueRegex");
		  locator.pointIdentifierIndex = $get("pointIdentifierIndex");
		  locator.valueIndex = $get("valueIndex");
		  locator.dataType = $get("dataTypeId");
		  locator.hasTimestamp = $get("hasTimestamp");
		  locator.timestampIndex = $get("timestampIndex");
		  locator.timestampFormat = $get("timestampFormat");
		  
		  AsciiFileEditDwr.savePointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
	  }
	  
	  /**
	   * Runs a test string
	   */
	  function submitTestString() {
		  AsciiFileEditDwr.testString(
				  dataSources.currentId,
				  $get("testString"),
				  displayResult);
	  }
	  
	  function displayResult(resp){
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
	  
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.file.desc" helpId="asciiFileDS">
<tr>
  <td class="formLabelRequired"><fmt:message key="dsEdit.updatePeriod"/></td>
  <td class="formField">
    <input type="text" id="updatePeriods" value="${dataSource.updatePeriods}" class="formShort"/>
    <tag:timePeriods id="updatePeriodType" value="${dataSource.updatePeriodType}" s="true" min="true" h="true"/>
  </td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.file.path"/></td>
 <td><input id="filePath" type="text" value="${dataSource.filePath}"></input></td>
</tr>
<tr>
 <td align="right"><input id="fileTestButton" type="button" value="<fmt:message key="dsEdit.file.check"/>" onclick="checkFile();"></input></td>
 <td class="formError" id="fileTestMessage"></td>
</tr>
<tr>
  <td colspan="2">
    <div class="borderDiv marB marR">
	  <table class="wide">
        <tr>
          <td class="smallTitle" colspan="2"><fmt:message key="dsEdit.file.test.submitTestString"/></td>
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
             <td><fmt:message key="common.pointName"/></td>
             <td><fmt:message key="dsEdit.file.pointIdentifier"/></td>
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

<tag:pointList pointHelpId="asciiFilePP">
  <tr>
    <td class="formLabelRequired"><fmt:message key="dsEdit.pointDataType"/></td>
    <td class="formField"><tag:dataTypeOptions id="dataTypeId" excludeImage="true"/></td>
  </tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.file.pointIdentifier"/></td>
 <td><input id="pointIdentifier" type="text" ></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.file.pointIdentifierIndex"/></td>
 <td><input id="pointIdentifierIndex" type="number" ></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.file.valueIndex"/></td>
 <td><input id="valueIndex" type="number" ></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.file.valueRegex"/></td>
 <td><input id="valueRegex" type="text" ></input></td>
</tr>
<tr>
  <td class="formLabelRequired"><fmt:message key="dsEdit.file.hasTimestamp"/></td>
  <td><input id="hasTimestamp" type="checkbox" onchange="hideTSindex();"></input></td>
</tr>
<tr id="timestampIndexRow">
 <td class="formLabelRequired"><fmt:message key="dsEdit.file.timestampIndex"/></td>
 <td><input id="timestampIndex" type="number" ></input></td>
</tr>
<tr id="timestampFormatRow">
 <td class="formLabelRequired"><fmt:message key="dsEdit.file.timestampFormat"/></td>
 <td><input id="timestampFormat" type="text" ></input></td>
</tr>

</tag:pointList>