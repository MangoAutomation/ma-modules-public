<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<script type="text/javascript">
  dojo.require("dojo.store.Memory");
  dojo.require("dijit.form.ComboBox");
  
  var allPoints = [];  
  var selectedPoints = [];  
  var pointLookupText = ""; //For selection to remain in the filter

  dojo.ready(function() { 
      SquwkPublisherDwr.initSender(function(response) {
          dojo.forEach(response.data.allPoints, function(item) {
              allPoints.push({
                  id: item.id, 
                  name: item.extendedName, 
                  enabled: item.enabled, 
                  type: item.dataTypeMessage,
                  fancyName: item.extendedName
              });
          });
          
          dojo.forEach(response.data.publisher.points, function(item) {
              addToSelectedArray(item.dataPointId, item.guid);
          });
          refreshSelectedPoints();
          
          // Create the lookup
          new dijit.form.ComboBox({
              store: new dojo.store.Memory({ data: allPoints }),
              labelAttr: "fancyName",
              labelType: "html",
              searchAttr: "name",
              autoComplete: false,
              style: "width: 254px;",
              queryExpr: "*\${0}*",
              highlightMatch: "all",
              required: false,
              onChange: function(point) {
                  if (this.item) {
                      selectPoint(this.item.id);
                      this.set('displayedValue',pointLookupText);
                      this.openDropDown();
                  }
              },
              onKeyUp: function(event){
                  pointLookupText = this.get('displayedValue');
              }
          }, "pointLookup");        
      });
  });
  
  function selectPoint(pointId) {
      if (!containsPoint(pointId)) {
          addToSelectedArray(pointId, "");
          refreshSelectedPoints();
      }
  }
  
  function containsPoint(pointId) {
      return getElement(selectedPoints, pointId, "id") != null;
  }
  
  function addToSelectedArray(pointId, guid) {
      var data = getElement(allPoints, pointId);
      
      if (data) {
          data.fancyName = "<span class='disabled'>"+ data.name +"</span>";
          
          // Missing names imply that the point was deleted, so ignore.
          selectedPoints[selectedPoints.length] = {
              id : pointId,
              pointName : data.name,
              enabled : data.enabled,
              pointType : data.type,
              guid: guid
          };
      }
  }
  
  function removeFromSelectedPoints(pointId) {
      removeElement(selectedPoints, pointId);
      refreshSelectedPoints();
      
      var data = getElement(allPoints, pointId);
      if (data)
          data.fancyName = data.name;
  }
  
  function refreshSelectedPoints() {
      dwr.util.removeAllRows("selectedPoints");
      if (selectedPoints.length == 0)
          show("selectedPointsEmpty");
      else {
          hide("selectedPointsEmpty");
          dwr.util.addRows("selectedPoints", selectedPoints,
              [
                  function(data) { return data.pointName; },
                  function(data) { return "<img src='images/"+ (data.enabled ? "brick_go" : "brick_stop") +".png'/>"; },
                  function(data) { return data.pointType; },
                  function(data) {
                      return "<input type='text' value='"+ data.guid +"' "+
                              "onblur='updateGuid("+ data.id +", this.value)'/>";
                  },
                  function(data) { 
                          return "<img src='images/bullet_delete.png' class='ptr' "+
                                  "onclick='removeFromSelectedPoints("+ data.id +")'/>";
                  }
              ],
              {
                  rowCreator: function(options) {
                      var tr = document.createElement("tr");
                      tr.className = "row"+ (options.rowIndex % 2 == 0 ? "" : "Alt");
                      return tr;
                  },
                  cellCreator: function(options) {
                      var td = document.createElement("td");
                      if (options.cellNum == 1 || options.cellNum == 3)
                          td.align = "center";
                      return td;
                  } 
              });
      }
  }

  function updateGuid(pointId, guid) {
      updateElement(selectedPoints, pointId, "guid", guid);
  }
  
  function savePublisherImpl(name, xid, enabled, cacheWarningSize, cacheDiscardSize, changesOnly, sendSnapshot,
          snapshotSendPeriods, snapshotSendPeriodType) {
      // Clear messages.
      hide("accessKeyMsg");
      hide("secretKeyMsg");
      hide("pointsMsg");
      
      var points = new Array();
      for (var i=0; i<selectedPoints.length; i++)
          points[points.length] = {dataPointId: selectedPoints[i].id, guid: selectedPoints[i].guid};
      
      PublisherEditDwr.saveSquwkSender(name, xid, enabled, points, $get("accessKey"), $get("secretKey"), 
              cacheWarningSize, cacheDiscardSize, changesOnly, sendSnapshot, snapshotSendPeriods, 
              snapshotSendPeriodType, savePublisherCB);
  }
</script>

<table cellpadding="0" cellspacing="0">
  <tr>
    <td valign="top">
      <div class="borderDiv marR marB">
        <table>
          <tr>
            <td colspan="2" class="smallTitle"><fmt:message key="publisherEdit.squwk.props"/> <tag:help id="squwkPublishing"/></td>
          </tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.squwk.accessKey"/></td>
            <td class="formField">
              <input type="text" id="accessKey" value="${publisher.accessKey}" class="formLong"/>
              <div id="accessKeyMsg" class="formError" style="display:none;"></div>
            </td>
          </tr>
          
          <tr>
            <td class="formLabelRequired"><fmt:message key="publisherEdit.squwk.secretKey"/></td>
            <td class="formField">
              <input type="text" id="secretKey" value="${publisher.secretKey}" class="formLong"/>
              <div id="secretKeyMsg" class="formError" style="display:none;"></div>
            </td>
          </tr>
        </table>
      </div>
    </td>
  </tr>
</table>

<table cellpadding="0" cellspacing="0"><tr><td>
  <div class="borderDiv">
    <table width="100%">
      <tr>
        <td class="smallTitle"><fmt:message key="publisherEdit.points"/></td>
        <td align="right"><div id="pointLookup"></div></td>
      </tr>
    </table>
    
    <table cellspacing="1" cellpadding="0">
      <tr class="rowHeader">
        <td><fmt:message key="publisherEdit.point.name"/></td>
        <td><fmt:message key="publisherEdit.point.status"/></td>
        <td><fmt:message key="publisherEdit.point.type"/></td>
        <td><fmt:message key="publisherEdit.squwk.guid"/></td>
        <td></td>
      </tr>
      <tbody id="selectedPointsEmpty" style="display:none;"><tr><td colspan="4"><fmt:message key="publisherEdit.noPoints"/></td></tr></tbody>
      <tbody id="selectedPoints"></tbody>
    </table>
    <div id="pointsMsg" class="formError" style="display:none;"></div>
  </div>
</td></tr></table>