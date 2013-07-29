<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp"%>

<script type="text/javascript">

	/**
	 * Save the DS
	 */
	function saveDataSourceImpl(basic){

		  SerialEditDwr.saveSerialDataSource(basic,
	              $get("commPortId"), $get("baudRate"), $get("flowControlIn"), $get("flowControlOut"), $get("dataBits"), 
	              $get("stopBits"), $get("parity"),$get("readTimeout"),$get("messageTerminator"),saveDataSourceCB);

	}

	/**
	 * Add a Point
	 */
	  function addPointImpl() {
		  DataSourceEditDwr.getPoint(-1, function(point) {
			  editPointCB(point);
		  });
	  }
		
	  function editPointCBImpl(locator) {}; 
	  
	  /**
	   * Save a Point
	   */
	  function savePointImpl(locator) {
	      SerialEditDwr.savePointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
	  }
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.serial.desc" helpId="serial-ds">
<tag:serialSettings/>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.readTimeout"/></td>
 <td><input id="readTimeout" type="number" value="${dataSource.readTimeout}"></input></td>
</tr>
<tr>
 <td class="formLabelRequired"><fmt:message key="dsEdit.serial.messageTerminator"/></td>
 <td><input id="messageTerminator" type="text" value="${dataSource.messageTerminator}"></input></td>
</tr>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="serial-pp">

</tag:pointList>