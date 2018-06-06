<%--
    Copyright (C) 2006-2015 Infinite Automation Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<tag:page dwr="ThreadsDwr">
  <script type="text/javascript">
    require(["dojo/ready"], function(ready) {
        ready(getWorkItems);
    });
    
    function getWorkItems() {
        ThreadsDwr.getWorkItems(function(result) {
        	draw(result.data.highClassCounts, "highList");
            draw(result.data.medClassCounts, "medList");
            draw(result.data.lowClassCounts, "lowList");
            //setTimeout(getStatusVars, 2000);
        });            
    }
    
    function draw(counts, listId) {
        var list = [];
        for (p in counts)
            list.push({name: p, count: counts[p]});
        
        dwr.util.removeAllRows(listId);
        dwr.util.addRows(listId, list, [
                function(t) { return encodeHtml(t.name); },
                function(t) { return t.count; },
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
    <fmt:message key="internal.workItems"/> |
	<a href="/internal/queueStats.shtm"><fmt:message key="internal.queue.stats"/></a> |
	<a href="/internal/tasks.shtm"><fmt:message key="internal.tasks.stats"/></a>
  </div>
  <br/>
  <div>
    <span class="copyTitle"><fmt:message key="internal.workItems.high"/></span>
    <table>
      <thead>
        <tr class="rowHeader">
          <td><fmt:message key="internal.workItems.class"/></td>
          <td><fmt:message key="internal.workItems.count"/></td>
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
          <td><fmt:message key="internal.workItems.class"/></td>
          <td><fmt:message key="internal.workItems.count"/></td>
        </tr>
      </thead>
      <tbody id="medList"></tbody>
    </table>
  </div>
  <div>
    <span class="copyTitle"><fmt:message key="internal.workItems.low"/></span>
    <table>
      <thead>
        <tr class="rowHeader">
          <td><fmt:message key="internal.workItems.class"/></td>
          <td><fmt:message key="internal.workItems.count"/></td>
        </tr>
      </thead>
      <tbody id="lowList"></tbody>
    </table>
  </div>
  
  <button onclick="getWorkItems()"><fmt:message key="common.refresh"/></button>
</tag:page>