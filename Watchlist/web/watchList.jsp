<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@page import="com.serotonin.m2m2.Constants"%>
<%@page import="com.serotonin.m2m2.Common"%>
<%@page import="com.serotonin.m2m2.view.ShareUser"%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<tag:page dwr="WatchListDwr" js="/resources/view.js,${modulePath}/web/watchList.js">
  <jsp:attribute name="styles">
    <style>
    html > body .dijitTreeNodeLabelSelected {
        background-color: inherit;
        color: inherit;
    }
    .watchListAttr { min-width:600px; }
    .rowIcons img { padding-right: 3px; }
    html > body .dijitSplitContainerSizerH {
        border: 1px solid #FFFFFF;
        background-color: #F07800;
        margin-top:4px;
        margin-bottom:4px;
    }
    .dijitSplitContainer-child { border: none !important; }
    .dijitTreeIcon { display: none; }
    .wlComponentMin {
        top:0px;
        left:0px;
        position:relative;
        margin:0px;
        padding:0px;
        width:16px;
        height:16px;
    }
    </style>
  </jsp:attribute>
  
  <jsp:body>
    <script type="text/javascript">
      dojo.require("dijit.layout.SplitContainer");
      dojo.require("dijit.layout.ContentPane");
      dojo.require("dijit.Tree");
      dojo.require("dijit.tree.TreeStoreModel");
      dojo.require("dojo.data.ItemFileWriteStore");
      dojo.require("dojo.store.Memory");
      dojo.require("dijit.form.FilteringSelect");
      
      mango.view.initWatchlist();
      mango.share.dwr = WatchListDwr;
      var owner;
      var pointNames = {};
      var pointList = [];
      var watchlistChangeId = 0;
      var iconSrc = "images/bullet_go.png";
      
      dojo.ready(function() {
          WatchListDwr.init(function(data) {
              mango.share.users = data.shareUsers;
              
              // Create the item store
              var storeItems = [];
              addFolder(storeItems, data.pointFolder);
              var store = new dojo.data.ItemFileWriteStore({data: { label: 'name', items: storeItems } });
              function $$(item, attr) { return store.getValue(item, attr); };
              
              // Create the tree.
              var tree = new dijit.Tree({
                  model: new dijit.tree.ForestStoreModel({ store: store }),
                  showRoot: false,
                  persist: false,
                  onClick: function(item) {
                      var pointId = $$(item, "pointId");
                      if (pointId)
                          addToWatchList(pointId);
                  },
                  _createTreeNode: function(/*Object*/ args){
                      var tnode = new dijit._TreeNode(args);
                      tnode.labelNode.innerHTML = args.label;
                      return tnode;
                  },
                  onOpen: function(item, node) {
                      if (item.children) {
                          for (var i=0; i<item.children.length; i++) {
                              var child = item.children[i];
                              if ($$(child, "fresh")) {
                                  // Initialize the node
                                  var pointId = $$(child, "pointId");
                                  var img = $("ph"+ pointId +"Image");
                                  img.src = iconSrc;
                                  img.mangoName = "pointTreeIcon";
                                  
                                  togglePointTreeIcon(pointId, !$("p"+ pointId));
                                  
                                  delete child.fresh;
                              }
                          }
                      }
                  }
              }, "tree");
              
              hide("loadingImg");
              show("treeDiv");
              
              // Add default points.
              displayWatchList(data.selectedWatchList);
              maybeDisplayDeleteImg();
              
              // Create the lookup
              new dijit.form.FilteringSelect({
                  store: new dojo.store.Memory({ data: pointList }),
                  labelAttr: "fancyName",
                  labelType: "html",
                  searchAttr: "name",                  
                  autoComplete: false,
                  style: "width: 100%;",
                  queryExpr: "*\${0}*",
                  highlightMatch: "all",
                  required: false,
                  //onKeyPress: function() {
                  //    alert("open: "+ this._opened);
                  //},
                  onChange: function(point) {
                      if (this.item) {
                          addToWatchList(this.item.id);
                          this.reset();
                      }
                  }
              }, "picker");
              
              // Start EXPERIMENTAL
//               var n = dojo.query("#widget_picker > .dijitValidationContainer");
//               var d = '<div id="pickerAddAll" class="dijitReset" style="float:right; display:inline; padding: 1px; background-color: #FFF; ">\
//                          <input class="dijitReset dijitInputField" style="width:16px; background-image: url(images/add.png)" type="text" role="presentation" readonly="readonly" tabindex="-1" value="">\
//                        </div>';
//               dojo.place(d, n[0], "before");
//               dojo.query("#pickerAddAll")[0].onclick = function() { alert("asdf"); };
              // End EXPERIMENTAL
          });
          
          WatchListDwr.getDateRangeDefaults(<c:out value="<%= Common.TimePeriods.DAYS %>"/>, 1, function(data) { setDateRange(data); });
          
          function addFolder(parent, pointFolder) {
              var i;
              // Add subfolders
              for (i=0; i<pointFolder.subfolders.length; i++) {
                  var folder = pointFolder.subfolders[i];
                  var node = {name: "<img src='images/folder_brick.png'/> "+ folder.name, children: []};
                  parent.push(node);
                  addFolder(node.children, folder);
              }
              
              // Add points
              for (i=0; i<pointFolder.points.length; i++) {
                  var dps = pointFolder.points[i];
                  var node = {pointId: dps.id, fresh: true };
                  var name = dps.extendedName;
                  node.name = "<img src='images/icon_comp.png'/> <span id='ph"+ dps.id +"Name'>"+ name +"</span> "+
                      "<img src='images/bullet_go.png' id='ph"+ dps.id +"Image' title='<fmt:message key="watchlist.addToWatchlist"/>'/>";
                  parent.push(node);
                  pointNames[dps.id] = dps;
                  pointList.push(dps);
                  dps.fancyName = name;
              }
          }
          
          // Wire up the tree options
          dojo.connect($("pointTree"), "onclick", function() {
              if (!dojo.hasClass(this, "active")) {
                  dojo.addClass(this, "active");
                  dojo.removeClass($("pointLookup"), "active");
                  hide("pickerDiv");
                  show("treeDiv");
              }
          });
          
          dojo.connect($("pointLookup"), "onclick", function() {
              if (!dojo.hasClass(this, "active")) {
                  dojo.addClass(this, "active");
                  dojo.removeClass($("pointTree"), "active");
                  hide("treeDiv");
                  show("pickerDiv");
              }
          });
      });
      
      function displayWatchList(data) {
          if (!data.points)
              // Couldn't find the watchlist. Reload the page
              window.location.reload();
          
          var points = data.points;
          owner = data.access == <c:out value="<%= ShareUser.ACCESS_OWNER %>"/>;
          
          // Add the new rows.
          for (var i=0; i<points.length; i++) {
              if (!pointNames[points[i]]) {
                  // The point id isn't in the list. Refresh the page to ensure we have current data.
                  window.location.reload();
                  return;
              }
              addToWatchListImpl(points[i]);
          }
          
          fixRowFormatting();
          mango.view.watchList.reset();
          
          var select = $("watchListSelect");
          var txt = $("newWatchListName");
          $set(txt, select.options[select.selectedIndex].text);
          
          // Display controls based on access
          if (owner) {
              show("wlEditDiv", "inline");
              show("usersEditDiv", "inline");
              
              // Set the share users.
              mango.share.writeSharedUsers(data.users);
              iconSrc = "images/bullet_go.png";
          }
          else {
              hide("wlEditDiv");
              hide("usersEditDiv");
              iconSrc = "images/bullet_key.png";
          }
          
          var icons = getElementsByMangoName($("treeDiv"), "pointTreeIcon");
          for (var i=0; i<icons.length; i++)
              icons[i].src = iconSrc;
      }
      
      function showWatchListEdit() {
          openLayer("wlEdit");
          $("newWatchListName").select();
      }
    
      function saveWatchListName() {
          var name = $get("newWatchListName");
          var select = $("watchListSelect");
          select.options[select.selectedIndex].text = name;
          WatchListDwr.updateWatchListName(name);
          hideLayer("wlEdit");
      }
      
      function watchListChanged() {
          // Clear the list.
          var rows = getElementsByMangoName($("watchListTable"), "watchListRow");
          for (var i=0; i<rows.length; i++)
              removeFromWatchListImpl(rows[i].id.substring(1));
          
          watchlistChangeId++;
          var id = watchlistChangeId;
          WatchListDwr.setSelectedWatchList($get("watchListSelect"), function(data) {
              // Ensure that the data received is the latest data that was requested.
              if (id == watchlistChangeId)
                  displayWatchList(data);
          });
      }
      
      function addWatchList(copy) {
          var copyId = ${NEW_ID};
          if (copy)
              copyId = $get("watchListSelect");
          
          WatchListDwr.addNewWatchList(copyId, function(watchListData) {
              var wlselect = $("watchListSelect");
              wlselect.options[wlselect.options.length] = new Option(watchListData.value, watchListData.key);
              $set(wlselect, watchListData.key);
              watchListChanged();
              maybeDisplayDeleteImg();
          });
      }
      
      function deleteWatchList() {
          var wlselect = $("watchListSelect");
          var deleteId = $get(wlselect);
          wlselect.options[wlselect.selectedIndex] = null;
          
          watchListChanged();
          WatchListDwr.deleteWatchList(deleteId);
          maybeDisplayDeleteImg();
      }
      
      function maybeDisplayDeleteImg() {
          var wlselect = $("watchListSelect");
          display("watchListDeleteImg", wlselect.options.length > 1);
      }
      
      function showWatchListUsers() {
          openLayer("usersEdit");
      }
      
      function openLayer(nodeId) {
          var nodeDiv = $(nodeId);
          closeLayers(nodeId);
          showLayer(nodeDiv, true);
      }
    
      function closeLayers(exclude) {
          if (exclude != "wlEdit")
              hideLayer("wlEdit");
          if (exclude != "usersEdit")
              hideLayer("usersEdit");
      }
      
      
      //
      // Watch list membership
      //
      function addToWatchList(pointId) {
          // Check if this point is already in the watch list.
          if ($("p"+ pointId) || !owner)
              return;
          addToWatchListImpl(pointId);
          WatchListDwr.addToWatchList(pointId, mango.view.watchList.setDataImpl);
          fixRowFormatting();
      }
      
      var watchListCount = 0;
      function addToWatchListImpl(pointId) {
          watchListCount++;
      
          // Add a row for the point by cloning the template row.
          var pointContent = createFromTemplate("p_TEMPLATE_", pointId, "watchListTable");
          pointContent.mangoName = "watchListRow";
          
          if (owner) {
              show("p"+ pointId +"MoveUp");
              show("p"+ pointId +"MoveDown");
              show("p"+ pointId +"Delete");
          }
          
          $("p"+ pointId +"Name").innerHTML = pointNames[pointId].extendedName;
          
          // Disable the element in the point list.
          togglePointTreeIcon(pointId, false);
      }
      
      function removeFromWatchList(pointId) {
          removeFromWatchListImpl(pointId);
          fixRowFormatting();
          WatchListDwr.removeFromWatchList(pointId);
      }
      
      function removeFromWatchListImpl(pointId) {
          watchListCount--;
          var pointContent = $("p"+ pointId);
          var watchListTable = $("watchListTable");
          watchListTable.removeChild(pointContent);
          
          // Enable the element in the point list.
          togglePointTreeIcon(pointId, true);
      }
      
      function togglePointTreeIcon(pointId, enable) {
          // Toggle the tree icon
          var node = $("ph"+ pointId +"Image");
          if (node) {
              if (enable)
                  dojo.style(node, "opacity", 1);
              else
                  dojo.style(node, "opacity", 0.2);
          }
          
          // Toggle the lookup text
          var dps = pointNames[pointId];
          if (enable)
              dps.fancyName = dps.extendedName;
          else
              dps.fancyName = "<span class='disabled'>"+ dps.extendedName +"</span>";
      }
      
      //
      // List state updating
      //
      function moveRowDown(pointId) {
          var watchListTable = $("watchListTable");
          var rows = getElementsByMangoName(watchListTable, "watchListRow");
          var i=0;
          for (; i<rows.length; i++) {
              if (rows[i].id == pointId)
                  break;
          }
          if (i < rows.length - 1) {
              if (i == rows.length - 1)
                  watchListTable.append(rows[i]);
              else
                  watchListTable.insertBefore(rows[i], rows[i+2]);
              WatchListDwr.moveDown(pointId.substring(1));
              fixRowFormatting();
          }
      }
      
      function moveRowUp(pointId) {
          var watchListTable = $("watchListTable");
          var rows = getElementsByMangoName(watchListTable, "watchListRow");
          var i=0;
          for (; i<rows.length; i++) {
              if (rows[i].id == pointId)
                  break;
          }
          if (i != 0) {
              watchListTable.insertBefore(rows[i], rows[i-1]);
              WatchListDwr.moveUp(pointId.substring(1));
              fixRowFormatting();
          }
      }
      
      function fixRowFormatting() {
          var rows = getElementsByMangoName($("watchListTable"), "watchListRow");
          if (rows.length == 0) {
              show("emptyListMessage");
          }
          else {
              hide("emptyListMessage");
              for (var i=0; i<rows.length; i++) {
                  if (i == 0) {
                      hide(rows[i].id +"BreakRow");
                      hide(rows[i].id +"MoveUp");
                  }
                  else {
                      show(rows[i].id +"BreakRow");
                      if (owner)
                          show(rows[i].id +"MoveUp");
                  }
                      
                  if (i == rows.length - 1)
                      hide(rows[i].id +"MoveDown");
                  else if (owner)
                      show(rows[i].id +"MoveDown");
              }
          }
      }
      
      function showChart(mangoId, event, source) {
          if (isMouseLeaveOrEnter(event, source)) {
              // Take the data in the chart textarea and put it into the chart layer div
              $set('p'+ mangoId +'ChartLayer', $get('p'+ mangoId +'Chart'));
              showMenu('p'+ mangoId +'ChartLayer', 4, 12);
          }
      }
      
      function hideChart(mangoId, event, source) {
          if (isMouseLeaveOrEnter(event, source))
              hideLayer('p'+ mangoId +'ChartLayer');
      }
      
      //
      // Image chart
      //
      function getImageChart() {
          var width = dojo.contentBox($("imageChartDiv")).w - 20;
          startImageFader($("imageChartImg"));
          WatchListDwr.getImageChartData(getChartPointList(), $get("fromYear"), $get("fromMonth"), $get("fromDay"), 
                  $get("fromHour"), $get("fromMinute"), $get("fromSecond"), $get("fromNone"), $get("toYear"), 
                  $get("toMonth"), $get("toDay"), $get("toHour"), $get("toMinute"), $get("toSecond"), $get("toNone"), 
                  width, 350, function(data) {
              $("imageChartDiv").innerHTML = data;
              stopImageFader($("imageChartImg"));
              
              // Make sure the length of the chart doesn't mess up the watch list display. Do async to
              // make sure the rendering gets done.
              // TODO - onResized no longer works.
              //setTimeout('dijit.byId("splitContainer").onResized()', 2000);
          });
      }
      
      function getChartData() {
          var pointIds = getChartPointList();
          if (pointIds.length == 0)
              alert("<fmt:message key="watchlist.noExportables"/>");
          else {
              startImageFader($("chartDataImg"));
              WatchListDwr.getChartData(getChartPointList(), $get("fromYear"), $get("fromMonth"), $get("fromDay"), 
                      $get("fromHour"), $get("fromMinute"), $get("fromSecond"), $get("fromNone"), $get("toYear"), 
                      $get("toMonth"), $get("toDay"), $get("toHour"), $get("toMinute"), $get("toSecond"), $get("toNone"), 
                      function(data) {
                  stopImageFader($("chartDataImg"));
                  window.location = "chartExport/watchListData.csv";
              });
          }
      }
      
      function getChartPointList() {
          var pointIds = $get("chartCB");
          for (var i=pointIds.length-1; i>=0; i--) {
              if (pointIds[i] == "_TEMPLATE_") {
                  pointIds.splice(i, 1);
              }
          }
          return pointIds;
      }

      <m2m2:moduleExists name="reports">
        function createReport() {
            var pointIds = getChartPointList();
            var pointList = "";
            for (var i=0; i<pointIds.length; i++) {
                if (i > 0)
                    pointList += ",";
                pointList += pointIds[i];
            }

            var select = $("watchListSelect");
            var name = escape(select.options[select.selectedIndex].text);
            window.location='reports.shtm?createName='+ name +'&createPoints='+ pointList;
        }
      </m2m2:moduleExists>
    </script>
  
    <table class="wide">
    <tr><td>
      <div dojoType="dijit.layout.SplitContainer" orientation="horizontal" sizerWidth="3" activeSizing="true" class="borderDiv"
              id="splitContainer" style="width: 100%; height: 500px;">
        <div dojoType="dijit.layout.ContentPane" sizeMin="20" sizeShare="20" style="overflow:auto;padding:2px;">
          <div>
            <div style="display:inline;"><span class="smallTitle"><fmt:message key="watchlist.points"/></span> <tag:help id="watchListPoints"/></div>
            <div style="float:right; margin: 3px 3px 0 0;">
              <a id="pointTree" class="choice active"><fmt:message key="watchlist.pointTree"/></a>
              <a id="pointLookup" class="choice"><fmt:message key="watchlist.pointLookup"/></a>
            </div>
          </div>
          <div class="clearfix"></div>
          <img src="images/hourglass.png" id="loadingImg"/>
          <div id="treeDiv" style="display:none;"><div id="tree"></div></div>
          <div id="pickerDiv" style="display:none; margin: 10px 5px;">
            <fmt:message key="watchlist.lookupInst"/>
            <div id="picker"></div>
          </div>
        </div>
        <div dojoType="dijit.layout.ContentPane" sizeMin="50" sizeShare="50" style="overflow:auto; padding:2px 10px 2px 2px;">
          <table class="wide">
            <tr>
              <td class="smallTitle"><fmt:message key="watchlist.watchlist"/> <tag:help id="watchList"/></td>
              <td align="right">
                <sst:select id="watchListSelect" value="${selectedWatchList}" onchange="watchListChanged()"
                        onmouseover="closeLayers();">
                  <c:forEach items="${watchLists}" var="wl">
                    <sst:option value="${wl.key}">${sst:escapeLessThan(wl.value)}</sst:option>
                  </c:forEach>
                </sst:select>
                
                <div id="wlEditDiv" style="display:inline;" onmouseover="showWatchListEdit()">
                  <tag:img id="wlEditImg" png="pencil" title="watchlist.editListName"/>
                  <div id="wlEdit" style="visibility:hidden;right:0px;top:15px;" class="labelDiv"
                          onmouseout="hideLayer(this)">
                    <fmt:message key="watchlist.newListName"/><br/>
                    <input type="text" id="newWatchListName"
                            onkeypress="if (event.keyCode==13) $('saveWatchListNameLink').onclick();"/>
                    <a class="ptr" id="saveWatchListNameLink" onclick="saveWatchListName()"><fmt:message key="common.save"/></a>
                  </div>
                </div>
                
                <div id="usersEditDiv" style="display:inline;" onmouseover="showWatchListUsers()">
                  <tag:img png="user" title="share.sharing" onmouseover="closeLayers();"/>
                  <div id="usersEdit" style="visibility:hidden;right:0px;top:15px;" class="labelDiv">
                    <tag:sharedUsers doxId="watchListSharing" noUsersKey="share.noWatchlistUsers"
                            closeFunction="hideLayer('usersEdit')"/>
                  </div>
                </div>
                
                <tag:img png="copy" onclick="addWatchList(true)" title="watchlist.copyList" onmouseover="closeLayers();"/>
                <tag:img png="add" onclick="addWatchList(false)" title="watchlist.addNewList" onmouseover="closeLayers();"/>
                <tag:img png="delete" id="watchListDeleteImg" onclick="deleteWatchList()" title="watchlist.deleteList"
                        style="display:none;" onmouseover="closeLayers();"/>
                <m2m2:moduleExists name="reports">
                  <c:set var="modulesDir" value="<%= Constants.DIR_MODULES %>"/>
                  <tag:img src="/${modulesDir}/reports/web/report_add.png" onclick="createReport();"
                          title="watchlist.createReport" onmouseover="closeLayers();"/>
                </m2m2:moduleExists>
              </td>
            </tr>
          </table>
          <div id="watchListDiv" class="watchListAttr">
            <table style="display:none;">
              <tbody id="p_TEMPLATE_">
                <tr id="p_TEMPLATE_BreakRow"><td class="horzSeparator" colspan="5"></td></tr>
                <tr>
                  <td width="1">
                    <table class="rowIcons">
                      <tr>
                        <td onmouseover="mango.view.showChange('p'+ getMangoId(this) +'Change', 4, 12);"
                                onmouseout="mango.view.hideChange('p'+ getMangoId(this) +'Change');"
                                id="p_TEMPLATE_ChangeMin" style="display:none;"><img alt="" id="p_TEMPLATE_Changing" 
                                src="images/icon_edit.png"/><div id="p_TEMPLATE_Change" class="labelDiv" 
                                style="visibility:hidden;top:10px;left:1px;" onmouseout="hideLayer(this);">
                          <tag:img png="hourglass" title="common.gettingData"/>
                        </div></td>
                        <td id="p_TEMPLATE_ChartMin" style="display:none;" onmouseover="showChart(getMangoId(this), event, this);"
                                onmouseout="hideChart(getMangoId(this), event, this);"><img alt="" 
                                src="images/icon_chart.png"/><div id="p_TEMPLATE_ChartLayer" class="labelDiv" 
                                style="visibility:hidden;top:0;left:0;"></div><textarea
                                style="display:none;" id="p_TEMPLATE_Chart"><tag:img png="hourglass"
                                title="common.gettingData"/></textarea></td>
                      </tr>
                    </table>
                  </td>
                  <td id="p_TEMPLATE_Name" style="font-weight:bold"></td>
                  <td id="p_TEMPLATE_Value" align="center"><img src="images/hourglass.png"/></td>
                  <td id="p_TEMPLATE_Time" align="center"></td>
                  <td style="width:1px; white-space:nowrap;">
                    <input type="checkbox" name="chartCB" id="p_TEMPLATE_ChartCB" value="_TEMPLATE_" checked="checked"
                            title="<fmt:message key="watchlist.consolidatedChart"/>"/>
                    <tag:img png="icon_comp" title="watchlist.pointDetails"
                            onclick="window.location='data_point_details.shtm?dpid='+ getMangoId(this)"/>
                    <tag:img png="arrow_up_thin" id="p_TEMPLATE_MoveUp" title="watchlist.moveUp" style="display:none;"
                            onclick="moveRowUp('p'+ getMangoId(this));"/><tag:img png="arrow_down_thin"
                            id="p_TEMPLATE_MoveDown" title="watchlist.moveDown" style="display:none;"
                            onclick="moveRowDown('p'+ getMangoId(this));"/>
                    <tag:img id="p_TEMPLATE_Delete" png="bullet_delete" title="watchlist.delete" style="display:none;"
                            onclick="removeFromWatchList(getMangoId(this))"/>
                  </td>
                </tr>
                <tr><td colspan="5" style="padding-left:16px;" id="p_TEMPLATE_Messages"></td></tr>
              </tbody>
            </table>
            <table id="watchListTable" class="wide"></table>
            <div id="emptyListMessage" style="color:#888888;padding:10px;text-align:center;">
              <fmt:message key="watchlist.emptyList"/>
            </div>
          </div>
        </div>
      </div>
    </td></tr>
    
    <tr><td>
      <div class="borderDiv" style="width: 100%;">
        <table class="wide">
          <tr>
            <td class="smallTitle"><fmt:message key="watchlist.chart"/> <tag:help id="watchListCharts"/></td>
            <td align="right"><tag:dateRange/></td>
            <td>
              <tag:img id="imageChartImg" png="control_play_blue" title="watchlist.imageChartButton"
                      onclick="getImageChart()"/>
              <tag:img id="chartDataImg" png="bullet_down" title="watchlist.chartDataButton"
                      onclick="getChartData()"/>
            </td>
          </tr>
          <tr><td colspan="3" id="imageChartDiv"></td></tr>
        </table>
      </div>
    </td></tr>
    
    </table>
  </jsp:body>
</tag:page>