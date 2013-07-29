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
	              $get("stopBits"), $get("parity"),saveDataSourceCB);

	}

	/**
	 * Add a Point
	 */
	  function addPointImpl() {
		  DataSourceEditDwr.getPoint(-1, function(point) {
			  editPointCB(point);
		  });
	  }
	
	  /**
	   * Save a Point
	   */
	  function savePointImpl(locator) {
	      SerialEditDwr.savePointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
	  }
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.serial.desc" helpId="serial-ds">
<tag:serialSettings/>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="serial-pp">

</tag:pointList>