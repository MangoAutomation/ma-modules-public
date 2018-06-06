<%--
    Copyright (C) 2006-2015 Infinite Automation Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<tag:page dwr="ThreadsDwr">
  <script type="text/javascript">
    require(["dojo/ready"], function(ready) {
        ready(getTaskStats);
    });
    
    function getTaskStats() {
        ThreadsDwr.getTaskStats(function(result) {
        	draw(result.data.highStats, "highList");
        	draw(result.data.mediumStats, "mediumList");
        });            
    }
    
    function draw(list, listId) {
        
        dwr.util.removeAllRows(listId);
        dwr.util.addRows(listId, list, [
                function(t) { 
                	if(t.id != null)
                		return encodeHtml(t.id);
                	else
                		return 'N/A';
                },
                function(t) { return encodeHtml(t.name); },
                function(t) { return t.currentlyRunning; },
                function(t) { return t.poolFull; },
                function(t) { return t.queueFull; },
                function(t) { return t.totalRejections; }
            ],
            {
                rowCreator: function(options) {
                    var tr = document.createElement("tr");
                    if (options.rowData.name == null)
                        tr.className = "rowHeader";
                    else
                        tr.className = "row"+ (options.rowIndex % 2 == 0 ? "" : "Alt");
                    return tr;
                },
                cellCreator: function(options) {
                    var td = document.createElement("td");
                    if (options.rowData.name == null) {
                        if (options.cellNum == 0)
                            td.colSpan = 2;                                
                        else
                            return null;
                    }
                    else if (options.cellNum == 1)
                        td.style.textAlign = "right";
                    return td;
                }
            }
        );
    }
  </script>
  
  <div>
    <a href="/internal/status.shtm"><fmt:message key="internal.status"/></a> |
    <a href="/internal/threads.shtm"><fmt:message key="internal.threads"/></a> |
    <a href="/internal/workItems.shtm"><fmt:message key="internal.workItems"/></a> |
    <a href="/internal/queueStats.shtm"><fmt:message key="internal.queue.stats"/></a> | 
    <fmt:message key="internal.tasks.stats"/>
  </div>
  <br/>
  <div>
    <span class="copyTitle"><fmt:message key="internal.workItems.high"/></span>
    <table>
      <thead>
        <tr class="rowHeader">
          <td><fmt:message key="internal.id"/></td>
          <td><fmt:message key="common.name"/></td>
          <td><fmt:message key="internal.tasks.currentlyRunningRejections"/></td>
          <td><fmt:message key="internal.tasks.poolFullRejections"/></td>
          <td><fmt:message key="internal.tasks.queueFullRejections"/></td>
          <td><fmt:message key="internal.tasks.totalRejections"/></td>
        </tr>
      </thead>
      <tbody id="highList"></tbody>
    </table>
  </div>  
  <div>
    <span class="copyTitle"><fmt:message key="internal.workItems.med"/></span>
    <table>
      <thead>
        <tr class="rowHeader">
          <td><fmt:message key="internal.id"/></td>
          <td><fmt:message key="common.name"/></td>
          <td><fmt:message key="internal.tasks.currentlyRunningRejections"/></td>
          <td><fmt:message key="internal.tasks.poolFullRejections"/></td>
          <td><fmt:message key="internal.tasks.queueFullRejections"/></td>
          <td><fmt:message key="internal.tasks.totalRejections"/></td>
        </tr>
      </thead>
      <tbody id="mediumList"></tbody>
    </table>
  </div>
  <button onclick="getTaskStats()"><fmt:message key="common.refresh"/></button>
</tag:page>