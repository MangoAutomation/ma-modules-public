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

    var responseFrameTree;
    var deviceInfo;

    /**
     * called from init()
     */
    function initImpl() {
        searchButtons(false);
        updateConnectionType();
        updateAddressing();
    }

    /**
     * enabele/disable search buttons
     */
    function searchButtons(searching) {
        setDisabled("searchBtn", searching);
        setDisabled("cancelSearchBtn", !searching);
    }

    function getConnection() {
        conn = null;
        switch ($get("connectionType")) {
            case  "TCP_IP":
                conn = new TcpIpConnection();
                conn.host = $get("ipAddressOrHostname");
                conn.port = $get("tcpPort");
                break;
            case "SERIAL_DIRECT":
                conn = new SerialPortConnection();
                conn.portName = $get("commPortId");
                break;
            default:
                //ERROR
        }
        conn.bitPerSecond = $get("bitPerSecond");
        conn.responseTimeOutOffset = $get("responseTimeOutOffset");
        return conn;
    }

    function getSearchAddressing() {
        addressing = null;
        if ($get("addressingType") === "PRIMARY") {
            addressing = new PrimaryAddressingSearch();
            addressing.firstPrimaryAddress = $get("firstPrimaryAddress");
            addressing.lastPrimaryAddress = $get("lastPrimaryAddress");
        } else if ($get("addressingType") === "SECONDARY") {
            addressing = new SecondaryAddressingSearch();
            addressing.id = $get("secAddrId");
            addressing.manufacturer = $get("secAddrMan");
            addressing.medium = $get("secAddrMedium");
            addressing.version = $get("secAddrVersion");
        }
        return addressing;
    }


    function search() {
        searchButtons(true);

        hide("responseFrames");
        $set("searchMessage", "<fmt:message key='dsEdit.mbus.searching' />");
        dwr.util.removeAllRows("mbusDevices");


        conn = getConnection();
        addressing = getSearchAddressing();
        MBusEditDwr.searchMBus(currentDsId, conn, addressing, searchCB);
    }

    function searchCB(result) {
        if ((typeof result != 'undefined') && (result.data.sourceRunning === true)) {
            searchButtons(false);
            $set("searchMessage", '<fmt:message key="dsEdit.mbus.noSearchWhileDataSourceRunning"/>');
        } else {
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
            if(result.devices.length < 1) {
            	searchCB();
            	return;
            }
//             dwr.util.removeAllRows("mbusDevices");
            dwr.util.addRows("mbusDevices", result.devices, [
                function (device) {
                    return "<input id='addrHexTd_" + device.index + "' value='" + device.addressHex + "'>" + writeImage("changeAddressImg" + device.index, null, "save",
                            "<fmt:message key='common.save'/>", "changeAddress(" + device.index + ")");
                },
                function (device) {
                    return device.identNumber;
                },
                function (device) {
                    return device.medium;
                },
                function (device) {
                    return device.manufacturer;
                },
                function (device) {
                    return device.versionHex;
                },
                function (device) {
                    return writeImage("responseFramesImg" + device.index, null, "control_play_blue",
                            "<fmt:message key='dsEditMbus.getDetails'/>", "getResponseFrames(" + device.index + ")");
                }

            ],
                    {
                        rowCreator: function (options) {
                            var tr = document.createElement("tr");
                            tr.id = "deviceIndex" + options.rowData.index;
                            tr.className = "row" + (options.rowIndex % 2 == 0 ? "" : "Alt");
                            return tr;
                        }
                    });

            if (result.finished) {
                $set("searchMessage", "Search done!");
                searchButtons(false);
            } else {
                searchCB();
            }
        }
    }

    function changeAddress(index) {
        startImageFader("changeAddressImg" + index, true);
        $set("searchMessage", "Change primary address of: " + "to: " + $get("addrHexTd_" + index));

        if (responseFrameTree) {
            responseFrameTree.destroy();
        }

        MBusEditDwr.changeMBusAddress(index, $get("addrHexTd_" + index), changeAddressCB);
    }

    function changeAddressCB(result) {
        $set("searchMessage", result.message);
        if (result) {
            stopImageFader("changeAddressImg" + result.deviceIndex);
            writePointList(result.points);
        } else {
        }
    }

    function getResponseFrames(index) {
        startImageFader("responseFramesImg" + index, true);
        MBusEditDwr.getMBusResponseFrames(index, getResponseFramesCB);
    }

    function getResponseFramesCB(result) {
        if (responseFrameTree) {
            responseFrameTree.destroy();
        }
        stopImageFader("responseFramesImg" + result.deviceIndex);

        show("responseFrames");

        var storeItems = [];

        var root = {name: "<b>" + result.deviceName + "</b>", children: []};
        storeItems.push(root);

        for (var rsIndex = 0; rsIndex < result.responseFrames.length; rsIndex++) {
            var responseFrame = result.responseFrames[rsIndex];
            var responseFrameItem = {name: responseFrame.name, children: []};
            root.children.push(responseFrameItem);

            for (var dbIndex = 0; dbIndex < responseFrame.dataBlocks.length; dbIndex++) {
                var dataBlock = responseFrame.dataBlocks[dbIndex];
                var dataBlockItem = {
                    name: dataBlock.name + "(" + dataBlock.params + ")" + writeImageSQuote(null, null,
                            "icon_comp_add", "<fmt:message key='dsEdit.mbus.addPoint'/>", "addPoint( { addressing: \"" + result.addressing + "\", deviceIndex: " + result.deviceIndex + ", rsIndex: " + rsIndex + ", dbIndex: " + dbIndex + "})"),
                    children: []
                };
                responseFrameItem.children.push(dataBlockItem);

                dataBlockItem.children.push({name: "<fmt:message key='dsEdit.mbus.presentValue'/>: " + dataBlock.value});
            }
        }

        // Create the item store
        var store = new dojo.data.ItemFileWriteStore({
            data: {label: 'name', items: storeItems},
            clearOnClose: true
        });

        var div = dojo.create("div");
        $("treeAnchor").appendChild(div);

        // Create the responseFrameTree.
        responseFrameTree = new dijit.Tree({
            model: new dijit.tree.ForestStoreModel({store: store}),
            showRoot: false,
            persist: false,
            _createTreeNode: function (args) {
                var tnode = new dijit._TreeNode(args);
                tnode.labelNode.innerHTML = args.label;
                return tnode;
            }
        }, div);

        responseFrameTree._expandNode(responseFrameTree.getNodesByItem(root)[0]);
    }

    function cancelSearch() {
        MBusEditDwr.cancelTestingUtility(cancelSearchCB);
    }

    function cancelSearchCB() {
        $set("searchMessage", "<fmt:message key='dsEdit.mbus.seachStopped'/>");
        searchButtons(false);
        dwr.util.removeAllRows("mbusDevices");
        hide("responseFrames");
    }

    function saveDataSourceImpl() {
        MBusEditDwr.saveMBusDataSource($get("dataSource.name"), $get("dataSource.xid"),
                getConnection(),
                $get("updatePeriodType"), $get("updatePeriods"), $get("quantize"), saveDataSourceCB);
    }

    function appendPointListColumnFunctions(pointListColumnHeaders, pointListColumnFunctions) {

        pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.dbIndex'/>";
        pointListColumnFunctions[pointListColumnFunctions.length] = function (p) {
            return p.pointLocator.dbIndex;
        };

        pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.effectiveSiPrefix'/>";
        pointListColumnFunctions[pointListColumnFunctions.length] = function (p) {
            return p.pointLocator.effectiveSiPrefix;
        };

        pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.addressHex'/>";
        pointListColumnFunctions[pointListColumnFunctions.length] = function (p) {
            return p.pointLocator.addressHex;
        };

        pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.deviceName'/>";
        pointListColumnFunctions[pointListColumnFunctions.length] = function (p) {
            return p.pointLocator.deviceName;
        };

        pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.responseFrame'/>";
        pointListColumnFunctions[pointListColumnFunctions.length] = function (p) {
            return p.pointLocator.responseFrame;
        };

        pointListColumnHeaders[pointListColumnHeaders.length] = "<fmt:message key='dsEdit.mbus.params'/>";
        pointListColumnFunctions[pointListColumnFunctions.length] = function (p) {
            return p.pointLocator.params;
        };
    }

    function addPointImpl(indicies) {
        MBusEditDwr.addMBusPoint(indicies.deviceIndex, indicies.rsIndex, indicies.dbIndex, editPointCB);
    }

    //Save point locator values as reference as null vs "" is  important to differential from
    // and the inputs on the page do not do this for us.
    var pointProperties = {};
    /**
     * Save the value into our storage
     */
    function mbusPointLocatorValueChanged(attribute) {
        pointProperties[attribute] = $get(attribute);
    }

    function editPointCBImpl(locator) {
        pointProperties = locator; //Save for reference as null vs "" is  important to differential from
        // and the inputs on the page do not do this for us.

        $set("dbIndex", locator.dbIndex);
        $set("effectiveSiPrefix", locator.effectiveSiPrefix);
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
        locator.dbIndex = $get("dbIndex");
        locator.effectiveSiPrefix = $get("effectiveSiPrefix");
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
        if (pointProperties.vifeTypes.indexOf(",") > 0) {
            locator.vifeTypes = pointProperties.vifeTypes.split(",");
        } else {
            locator.vifeTypes = pointProperties.vifeTypes;
        }
        if (pointProperties.vifeLabels.indexOf(",") > 0) {
            locator.vifeLabels = pointProperties.vifeLabels.split(",");
        } else {
            locator.vifeLabels = pointProperties.vifeLabels;
        }


        MBusEditDwr.saveMBusPointLocator(currentPoint.id, $get("xid"), $get("name"), locator, savePointCB);
    }

    function addressChanged() {
        deviceInfo = getElement(networkInfo, $get("addressHex"), "addressString");
        dwr.util.addOptions("id", "description");
    }

    function updateConnectionType() {
    	var connectionTcp = $get("connectionType") !== "TCP_IP";
    	
    	switch($get("connectionType")){
    	case "TCP_IP":
        	setDisabled("ipAddressOrHostname", false);
        	setDisabled("tcpPort", false);
        	setDisabled("commPortId", true);
        	$set("useTcpIpConnection", true)
        break;
    	default:
    	case "SERIAL_DIRECT":
        	setDisabled("ipAddressOrHostname", true);
        	setDisabled("tcpPort", true);
    		setDisabled("commPortId", false);
    		$set("useDirectConnection", true);
    	}
        
    }

    function updateAddressing() {
        //Primary Addressing
        setDisabled("firstPrimaryAddress", $get("addressingType") !== "PRIMARY");
        setDisabled("lastPrimaryAddress", $get("addressingType") !== "PRIMARY");

        //Secondary Addressing
        setDisabled("secAddrId", $get("addressingType") !== "SECONDARY");
        setDisabled("secAddrMan", $get("addressingType") !== "SECONDARY");
        setDisabled("secAddrMedium", $get("addressingType") !== "SECONDARY");
        setDisabled("secAddrVersion", $get("addressingType") !== "SECONDARY");
    }
</script>

<tag:dataSourceAttrs descriptionKey="dsEdit.mbus.desc" helpId="mBusDS">
    <jsp:attribute name="extraPanels">
        <td valign="top">
            <div class="borderDiv marB">
                <table>
                    <tr><td colspan="2" class="smallTitle"><fmt:message key="dsEdit.mbus.search"/><tag:help id="mBusDiscovery"/></td></tr>
                    <tr>
                        <td>
                            <input type="radio" name="addressingType" id="usePrimnaryAddressing" value="PRIMARY" onclick="updateAddressing()">
                            <label class="formLabelRequired" for="usePrimnaryAddressing"><fmt:message key="dsEdit.mbus.usePrimaryAddressing"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <table>
                                <tr>
                                    <td width="30" />
                                    <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.firstHexAddress"/></td>
                                    <td class="formField"><input type="text" id="firstPrimaryAddress" value="00"/></td>
                                </tr>
                                <tr>
                                    <td width="30" />
                                    <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.lastHexAddress"/></td>
                                    <td class="formField"><input type="text" id="lastPrimaryAddress" value="FA"/></td>
                                </tr>
                            </table>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="radio" name="addressingType" id="useSecondaryAddressing" checked="checked" value="SECONDARY" onclick="updateAddressing()">
                            <label class="formLabelRequired" for="useSecondaryAddressing"><fmt:message key="dsEdit.mbus.useSecondaryAddressing"/></label>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <table>
                                <tr>
                                    <td width="30" />
                                    <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.id"/></td>
                                    <td class="formField"><input type="text" id="secAddrId" value=""/></td>
                                </tr>
                                <tr>
                                    <td width="30" />
                                    <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.man"/></td>
                                    <td class="formField"><input type="text" id="secAddrMan" value=""/></td>
                                </tr>
                                <tr>
                                    <td width="30" />
                                    <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.secAddrMedium"/></td>
                                    <td class="formField">
                                        <sst:select id="secAddrMedium" value="">
                                            <sst:option></sst:option>
                                            <c:forEach items="${dataSource.regularMedia}" var="medium">
                                                <sst:option value="${medium.name}">${medium.label}</sst:option>
                                            </c:forEach>
                                        </sst:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="30" />
                                    <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.version"/></td>
                                    <td class="formField"><input type="text" id="secAddrVersion" value=""/></td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                <table>
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
                </table>
                <hr>
                <table>
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
            <td class="formLabel"><fmt:message key="dsEdit.quantize"/></td>
            <td class="formField"><sst:checkbox id="quantize" selectedValue="${dataSource.quantize}"/></td>
        </tr>

        <tr>
            <td colspan="2">
                <table>
                    <tr>
                        <td width="30" />
                        <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.bitPerSecond"/></td>
                        <td class="formField">
                            <sst:select id="bitPerSecond" value="${empty dataSource.connection ? 2400 : dataSource.connection.bitPerSecond}">
                                <sst:option>300</sst:option>
                                <sst:option>2400</sst:option>
                                <sst:option>9600</sst:option>
                            </sst:select>
                        </td>        
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="3">
                <label class="formLabelRequired" for="responseTimeOutOffset" ><fmt:message key="dsEdit.mbus.responseTimeOutOffset"/></label>
                <input class="formShort" type="text" id="responseTimeOutOffset" value="${empty dataSource.connection ? 50 : dataSource.connection.responseTimeOutOffset}" />
                <label class="formLabelRequired">ms</label>
            </td>
        </tr>

        <tr>
            <td colspan="2">
                <input type="radio" name="connectionType" id="useTcpIpConnection" value="TCP_IP" <c:if test="${dataSource.tcpIp}">checked="checked"</c:if> onchange="updateConnectionType()" >
                <label class="formLabelRequired" for="useDirectConnection"><fmt:message key="dsEdit.mbus.useTcpIpConnection"/></label>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <table>
                    <tr>
                        <td width="30" />
                        <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.tcpAddr"/></td>
                        <td class="formField"><input type="text" id="ipAddressOrHostname" value="${dataSource.tcpIp ? dataSource.connection.host : ""}" /></td>
                    </tr>
                    <tr>
                        <td width="30" />
                        <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.tcpPort"/>
                        <td class="formField"><input type="text" id="tcpPort" value="${dataSource.tcpIp ? dataSource.connection.port: ""}" /></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <input type="radio" name="connectionType" id="useDirectConnection" value="SERIAL_DIRECT" <c:if test="${dataSource.serialDirect}">checked="checked"</c:if> onchange="updateConnectionType()" >
                <label class="formLabelRequired" for="useDirectConnection"><fmt:message key="dsEdit.mbus.useDirectConnection"/></label>
            </td>
            <td>
        <tr>
            <td class="formLabelRequired"><fmt:message key="dsEdit.serial.port"/></td>
            <td class="formField">
                <c:choose>
                    <c:when test="${!empty commPortError}">
                        <input id="commPortId" type="hidden" value=""/>
                        <span class="formError">${commPortError}</span>
                    </c:when>
                    <c:otherwise>
                        <input id="commPortId" type="text" value="${dataSource.serialDirect ? dataSource.connection.portName : ''}"/><br/>
                        <sst:select id="commPortIds" value="${dataSource.serialDirect ? dataSource.connection.portName : ''}">
                            <c:forEach items="${commPorts}" var="port">
                                <sst:option value='${sst:quotEncode(port.name)}'>${fn:escapeXml(port.name)}</sst:option>
                            </c:forEach>
                        </sst:select>
                        <tag:img id='commPortsLoadingImg' src="/images/hourglass.png" style='display: none;'/>
                        <tag:img src="/images/arrow-turn-090-left.png" onclick="$set('commPortId', $get('commPortIds'))"/>
                        <tag:img id='commPortRefreshButton' src="/images/arrow_refresh.png" onclick="reloadCommPorts('commPortIds', 'commPortsLoadingImg')" title='common.refreshCommPorts'/>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </jsp:body>
</tag:dataSourceAttrs>

<tag:pointList pointHelpId="mBusPP">
    <tr>
        <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.dbIndex"/></td>
        <td class="formField"><input type="text" id="dbIndex"/></td>
    </tr>
    <tr>
        <td class="formLabelRequired"><fmt:message key="dsEdit.mbus.effectiveSiPrefix"/></td>
        <td class="formField"><input type="text" id="effectiveSiPrefix"/></td>
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