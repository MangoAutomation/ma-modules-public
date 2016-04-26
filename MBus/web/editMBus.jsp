<%--
    Copyright (C) 2010 Arne Ploese
    @author Arne Ploese
--%>
<%@page import="com.serotonin.m2m2.Common"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<script type="text/javascript">
  dojo.require("dijit.Tree");
  dojo.require("dijit.tree.TreeStoreModel");
  dojo.require("dojo.data.ItemFileWriteStore");
  
  var tree;
  var deviceInfo;
  
  /**
   * called from init()
   */
  function initImpl() {
      searchButtons(false);
      updateModemOrDirect();
  }

  /**
   * enabele/disable search buttons
   */
  function searchButtons(searching) {
      setDisabled("searchBtn", searching);
      setDisabled("cancelSearchBtn", !searching);
  }

  function search() {
      searchButtons(true);
      hide("responseFrames");
      $set("searchMessage", "<fmt:message key='dsEdit.mbus.searching' />");
      dwr.util.removeAllRows("mbusDevices");
      if ($get("addressingType") == "PRIMARY") {
    	  //For testing and perhaps future implementation
//     	  MBusEditDwr.searchMBusByPrimaryAddressingTcp(currentDsId, "localhost",8100,
//                   $get("firstPrimaryAddress"), $get("lastPrimaryAddress"),
//                   $get("responseTimeoutOffset"), searchCB);

          MBusEditDwr.searchMBusByPrimaryAddressing(currentDsId, $get("commPortId"), $get("phonenumber"),
              $get("baudRate"),  $get("flowControlIn"),  $get("flowControlOut"),
              $get("dataBits"),  $get("stopBits"),  $get("parity"),
              $get("firstPrimaryAddress"), $get("lastPrimaryAddress"),
              $get("responseTimeoutOffset"), searchCB);
      } else if ($get("addressingType") == "SECONDARY") {
          MBusEditDwr.searchMBusBySecondaryAddressing(currentDsId, $get("commPortId"), $get("phonenumber"),
              $get("baudRate"),  $get("flowControlIn"),  $get("flowControlOut"),
              $get("dataBits"),  $get("stopBits"),  $get("parity"),
              $get("responseTimeoutOffset"), searchCB);
      } else {
          MBusEditDwr.searchMBusByUnknownAddress();// Dummy for generating Error in log
      }
  }

  function searchCB(result) {
	  if((typeof result != 'undefined') && (result.data.sourceRunning === true)){
		  searchButtons(false);
		  $set("searchMessage", '<fmt:message key="dsEdit.mbus.noSearchWhileDataSourceRunning"/>');
	  }else{
	      searchButtons(true);
	      //$set("searchMessage", "Callback searchCB");
	      setTimeout(searchUpdate, 1000);		  
	  }
  }

  function searchUpdate() {
      MBusEditDwr.mBusSearchUpdate(searchUpdateCB);
  }
  
  function searchUpdateCB(result) {
      if (result) {
          $set("searchMessage", result.message);
          dwr.util.removeAllRows("mbusDevices");
          dwr.util.addRows("mbusDevices", result.devices, [
              function(device) { return device.addressHex; },
              function(device) { return device.identNumber; },
              function(device) { return device.medium; },
              function(device) { return device.manufacturer; },
              function(device) { return device.versionHex; },
              function(device) {
                  return writeImage("responseFramesImg"+ device.index, null, "control_play_blue",
                  "<fmt:message key='dsEditMbus.getDetails'/>", "getResponseFrames(" + device.index + ")");
              }
          ],
          {
              rowCreator: function(options) {
                  var tr = document.createElement("tr");
                  tr.id = "deviceIndex"+ options.rowData.id;
                  tr.className = "row"+ (options.rowIndex % 2 == 0 ? "" : "Alt");
                  return tr;
              }
          });

          

          if (result.finished) {
              $set("searchMessage", "Finished Search");
              searchButtons(false);
          } else {
              searchCB();
          }
      }
  }

  function getResponseFrames(index) {
      startImageFader("responseFramesImg"+ index, true);
      MBusEditDwr.getMBusResponseFrames(index, getResponseFramesCB);
  }
  
  function getResponseFramesCB(result) {
      if (tree)
          tree.destroy();
      
      if (result) {
          stopImageFader("responseFramesImg"+ result.deviceIndex);

          show("responseFrames");
          
          var storeItems = [];
          
          var root = { name: "<b>" + result.deviceName  + "</b>", children: [] };
          storeItems.push(root);
          
          for (var rsIndex = 0; rsIndex < result.responseFrames.length; rsIndex++) {
              var responseFrame = result.responseFrames[rsIndex];
              var responseFrameItem = { name: responseFrame.name, children: [] };
              root.children.push(responseFrameItem);

              for (var dbIndex = 0; dbIndex < responseFrame.dataBlocks.length; dbIndex++) {
                  var dataBlock = responseFrame.dataBlocks[dbIndex];
                  var dataBlockItem = {
                          name: dataBlock.name + "(" + dataBlock.params  + ")" + writeImageSQuote(null, null,
                                  "icon_comp_add", "<fmt:message key='dsEdit.mbus.addPoint'/>", "addPoint( { addressing: \"" + result.addressing + "\", deviceIndex: "+ result.deviceIndex + ", rsIndex: " + rsIndex + ", dbIndex: " + dbIndex + "})"),
                          children: []
                  };
                  responseFrameItem.children.push(dataBlockItem);

                  dataBlockItem.children.push({ name: "<fmt:message key='dsEdit.mbus.presentValue'/>: "+ dataBlock.value});
              }
          }

          // Create the item store
          var store = new dojo.data.ItemFileWriteStore({
              data: { label: 'name', items: storeItems },
              clearOnClose: true
          });
          
          var div = dojo.create("div");
          $("treeAnchor").appendChild(div);
          
          // Create the tree.
          tree = new dijit.Tree({
              model: new dijit.tree.ForestStoreModel({ store: store }),
              showRoot: false,
              persist: false,
              _createTreeNode: function(args) {
                  var tnode = new dijit._TreeNode(args);
                  tnode.labelNode.innerHTML = args.label;
                  return tnode;
              }
          }, div);
          
          tree._expandNode(tree.getNodesByItem(root)[0]);
      }
  }
  
  function cancelSearch() {
      MBusEditDwr.cancelTestingUtility(cancelSearchCB);
  }
  
  function cancelSearchCB() {
      $set("searchMessage", "<fmt:message key='dsEdit.mbus.seachStopped'/>");
      searchButtons(false);
  }
  
  function saveDataSourceImpl(basic) {
      MBusEditDwr.saveMBusDataSourceConnection(basic,
          $get("useModemOrDirectConnection"), $get("commPortId"), $get("phonenumber"),
          $get("baudRate"),  $get("flowControlIn"),  $get("flowControlOut"),
          $get("dataBits"),  $get("stopBits"),  $get("parity"),
          $get("updatePeriodType"), $get("updatePeriods"), saveDataSourceCB);
  }
  
  function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {
      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.addressing'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.addressing; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.addressHex'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.addressHex; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.identNumber'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.identNumber; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.medium'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.medium; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.manufacturer'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.manufacturer; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.versionHex'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.versionHex; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.responseFrame'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.responseFrame; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.difCode'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.difCode; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.functionField'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.functionField; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.deviceUnit'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.deviceUnit; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.tariff'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.tariff; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.storageNumber'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.storageNumber; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.vifType'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.vifType; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.vifLabel'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.vifLabel; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.unitOfMeasurement'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.unitOfMeasurement; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.siPrefix'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.siPrefix; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.exponent'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.exponent; };

      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.vifeTypes'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.vifeTypess; };
      
      pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.vifeLabels'/>";
      pointListColumnFunctions[pointListColumnFunctions.length] = function(p) { return p.pointLocator.vifeLabels; };
  }
  
  function addPointImpl(indicies) {
      MBusEditDwr.addMBusPoint(indicies.addressing, indicies.deviceIndex, indicies.rsIndex, indicies.dbIndex, editPointCB);
  }
  
  //Save point locator values as reference as null vs "" is  important to differential from
  // and the inputs on the page do not do this for us.
  var pointProperties = {};
  /**
   * Save the value into our storage
   */
  function mbusPointLocatorValueChanged(attribute){
	  pointProperties[attribute] = $get(attribute);
  }
  
  function editPointCBImpl(locator) {
	  pointProperties = locator; //Save for reference as null vs "" is  important to differential from
	  // and the inputs on the page do not do this for us.
	  
      $set("addressing", locator.addressing);
      $set("addressHex", locator.addressHex);
      $set("identNumber", locator.identNumber);
      $set("medium", locator.medium);
      $set("manufacturer", locator.manufacturer);
      $set("versionHex", locator.versionHex);
      $set("responseFrame", locator.responseFrame);
      $set("difCode", locator.difCode);
      $set("functionField", locator.functionField);
      $set("deviceUnit", locator.deviceUnit);
      $set("tariff", locator.tariff);
      $set("storageNumber", locator.storageNumber);
      $set("vifType", locator.vifType);
      $set("vifLabel", locator.vifLabel);
      $set("unitOfMeasurement", locator.unitOfMeasurement);
      $set("siPrefix", locator.siPrefix);
      $set("exponent", locator.exponent);
      $set("vifeTypes", locator.vifeTypes);
      $set("vifeLabels", locator.vifeLabels);
      if (true) {
          show("pointSaveImg");
      } else {
          // Didn't find the device.
          hide("pointSaveImg");
      }
  }
  
  function savePointImpl(locator) {
      locator.addressing = pointProperties.addressing; //$get("addressing");
      locator.addressHex = pointProperties.addressHex; //$get("addressHex");
      locator.identNumber = pointProperties.identNumber; //$get("identNumber");
      locator.medium = pointProperties.medium; //$get("medium");
      locator.manufacturer = pointProperties.manufacturer; //$get("manufacturer");
      locator.versionHex = pointProperties.versionHex; //$get("versionHex");
      locator.responseFrame = pointProperties.responseFrame; //$get("responseFrame");
      locator.difCode = pointProperties.difCode; //$get("difCode");
      locator.functionField = pointProperties.functionField; //$get("functionField");
      locator.deviceUnit = pointProperties.deviceUnit; //$get("deviceUnit");
      locator.tariff = pointProperties.tariff; //$get("tariff");
      locator.storageNumber = pointProperties.storageNumber; //$get("storageNumber");
      
      locator.vifType = pointProperties.vifType; //$get("vifType");
      locator.vifLabel = pointProperties.vifLabel; //$get("vifLabel");

      locator.unitOfMeasurement = pointProperties.unitOfMeasurement;
      locator.siPrefix = pointProperties.siPrefix;
      locator.exponent = pointProperties.exponent;
      
	  locator.vifeTypes = pointProperties.vifeTypes;
	  locator.vifeLabels = pointProperties.vifeLabels;
 

      MBusEditDwr.saveMBusPointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
  }
  
  function addressChanged() {
      deviceInfo = getElement(networkInfo, $get("addressHex"), "addressString");
      dwr.util.addOptions("id", "description");
  }

  //Apl neu
  function updateModemOrDirect() {
      if($get("useModemOrDirectConnection") == "SERIAL_AT_MODEM") {
          document.getElementById("phonenumber").disabled=false;
      } else {
          document.getElementById("phonenumber").disabled=true;
      }
  }

  function addressingChanged() {
      setDisabled("firstPrimaryAddress", $get("addressingType") != "PRIMARY");
      setDisabled("lastPrimaryAddress", $get("addressingType") != "PRIMARY");
  }
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.mbus.desc" helpId="mbusDS">
  <jsp:attribute name="extraPanels">
    <td valign="top">
      <div class="borderDiv marB">
        <table>
          <tr><td colspan="2" class="smallTitle"><fmt:message key="dsEdit.mbus.search"/></td></tr>
          <tr>
            <td colspan="6">
              <input type="radio" name="addressingType" id="usePrimnaryAddressing" value="PRIMARY" checked="checked" onclick="addressingChanged()">
              <label class="formLabelRequired" for="usePrimnaryAddressing"><fmt:message key="dsEdit.mbus.usePrimaryAddressing"/></label>
              <span class="formLabelRequired"><fmt:message key="dsEdit.mbus.firstHexAddress"/></span>
              <span class="formField"><input type="text" id="firstPrimaryAddress" value="00"/></span>
              <span class="formLabelRequired"><fmt:message key="dsEdit.mbus.lastHexAddress"/></span>
              <span class="formField"><input type="text" id="lastPrimaryAddress" value="FA"/></span>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <input type="radio" name="addressingType" id="useSecondaryAddressing" value="SECONDARY" onclick="addressingChanged()">
              <label class="formLabelRequired" for="useSecondaryAddressing"><fmt:message key="dsEdit.mbus.useSecondaryAddressing"/></label>
            </td>
          </tr>
          <tr>
            <td colspan="2" align="center">
              <input id="searchBtn" type="button" value="<fmt:message key="dsEdit.mbus.search"/>" onclick="search();"/>
              <input id="cancelSearchBtn" type="button" value="<fmt:message key="common.cancel"/>" onclick="cancelSearch();"/>
            </td>
          </tr>

          <tr><td colspan="2" id="searchMessage" class="formError"></td></tr>

          <tr>
            <td colspan="2">
              <table cellspacing="1">
                <tr class="rowHeader">
                  <td><fmt:message key="dsEdit.mbus.addressHex"/></td>
                  <td><fmt:message key="dsEdit.mbus.identNumber"/></td>
                  <td><fmt:message key="dsEdit.mbus.medium"/></td>
                  <td><fmt:message key="dsEdit.mbus.manufacturer"/></td>
                  <td><fmt:message key="dsEdit.mbus.versionHex"/></td>
                </tr>
                <tbody id="mbusDevices"></tbody>
              </table>
            </td>
          </tr>

          <tbody id="responseFrames">
            <tr><td colspan="2" id="treeAnchor"></td></tr>
          </tbody>
        </table>
      </div>
    </td>
  </jsp:attribute>
  
  <jsp:body>
    <!-- Disable modem for now-->
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.updatePeriod"/></td>
      <td class="formField">
        <input type="text" id="updatePeriods" value="${dataSource.updatePeriods}" class="formShort"/>
        <tag:timePeriods id="updatePeriodType" value="${dataSource.updatePeriodType}" s="true" min="true" h="true" d="true" w="true" mon="true"/>
      </td>
    </tr>
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.responseTimeoutOffset"/></td>
      <td class="formField"><input type="number" id="responseTimeoutOffset" value="${dataSource.responseTimeoutOffset}" /></td>
    </tr>
    <tr>
      <td colspan="2">
        <input type="radio" name="useModemOrDirectConnection" id="useDirectConnection" value="SERIAL_DIRECT" <c:if test="${dataSource.serialDirect}">checked="checked"</c:if> onclick="updateModemOrDirect()" disabled="disabled">
        <label class="formLabelRequired" for="useDirectConnection"><fmt:message key="dsEdit.mbus.useDirectConnection"/></label>
      </td>
    </tr>
    <tr>
      <td colspan="2">
        <input type="radio" name="useModemOrDirectConnection" id="useModemConnection" value="SERIAL_AT_MODEM" <c:if test="${dataSource.serialAtModem}"> checked="checked"</c:if> onclick="updateModemOrDirect()" disabled="disabled">
        <label class="formLabelRequired" for="useModemConnection"><fmt:message key="dsEdit.mbus.useModemConnection"/></label>
      </td>
    </tr>
    <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.phonenumber"/></td>
      <td class="formField"><input type="text" id="phonenumber" value="${dataSource.phonenumber}" <c:if test="${dataSource.serialDirect}">disabled="disabled"</c:if>/></td>
    </tr>
    
    <tag:serialSettings/>
    

  </jsp:body>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="mbusPP">
  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.addressing"/></td>
      <td class="formField"><input type="text" id="addressing" onchange="mbusPointLocatorValueChanged('addressing');"/></td>
  </tr>
  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.addressHex"/></td>
      <td class="formField"><input type="text" id="addressHex" onchange="mbusPointLocatorValueChanged('addressHex');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.identNumber"/></td>
      <td class="formField"><input type="text" id="identNumber" onchange="mbusPointLocatorValueChanged('identNumber');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.medium"/></td>
      <td class="formField"><input type="text" id="medium" onchange="mbusPointLocatorValueChanged('medium');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.manufacturer"/></td>
      <td class="formField"><input type="text" id="manufacturer" onchange="mbusPointLocatorValueChanged('manufacturer');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.versionHex"/></td>
      <td class="formField"><input type="text" id="versionHex" onchange="mbusPointLocatorValueChanged('versionHex');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.responseFrame"/></td>
      <td class="formField"><input type="text" id="responseFrame" onchange="mbusPointLocatorValueChanged('responseFrame');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.difCode"/></td>
      <td class="formField"><input type="text" id="difCode" onchange="mbusPointLocatorValueChanged('difCode');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.functionField"/></td>
      <td class="formField"><input type="text" id="functionField" onchange="mbusPointLocatorValueChanged('functionField');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.deviceUnit"/></td>
      <td class="formField"><input type="text" id="deviceUnit" onchange="mbusPointLocatorValueChanged('deviceUnit');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.tariff"/></td>
      <td class="formField"><input type="text" id="tariff" onchange="mbusPointLocatorValueChanged('tariff');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.storageNumber"/></td>
      <td class="formField"><input type="text" id="storageNumber" onchange="mbusPointLocatorValueChanged('storageNumber');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.vifType"/></td>
      <td class="formField"><input type="text" id="vifType" onchange="mbusPointLocatorValueChanged('vifType');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.vifLabel"/></td>
      <td class="formField"><input type="text" id="vifLabel" onchange="mbusPointLocatorValueChanged('vifLabel');" /></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.unitOfMeasurement"/></td>
      <td class="formField"><input type="text" id="unitOfMeasurement" onchange="mbusPointLocatorValueChanged('unitOfMeasurement');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.siPrefix"/></td>
      <td class="formField"><input type="text" id="siPrefix" onchange="mbusPointLocatorValueChanged('siPrefix');"/></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.exponent"/></td>
      <td class="formField"><input type="text" id="exponent" onchange="mbusPointLocatorValueChanged('exponent');" /></td>
  </tr>

  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.vifeTypes"/></td>
      <td class="formField"><input type="text" id="vifeTypes" onchange="mbusPointLocatorValueChanged('vifeTypes');"/></td>
  </tr>
  <tr>
      <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.vifeLabels"/></td>
      <td class="formField"><input type="text" id="vifeLabels" onchange="mbusPointLocatorValueChanged('vifeLabels');"/></td>
  </tr>
</tag:pointList>