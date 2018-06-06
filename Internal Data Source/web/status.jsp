<%--
    Copyright (C) 2006-2013 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<tag:page dwr="ThreadsDwr">
  <script type="text/javascript">
    require(["dojo/ready"], function(ready) {
        ready(function() {
            getStatusVars();
        });
    });
    
    function clipValue(value) {
    	if(typeof value === 'object')
    		return value;
    	
    	if(/\d*\.\d+/.exec(value) === null)
    		return value;
    	
    	return Number(value).toFixed(2);
    }
    
    function getStatusVars() {
        ThreadsDwr.getStatusVars(function(result) {
            dwr.util.removeAllRows("varList");
            
            var list = [];
            for (var groupName in result.data) {
                var group = result.data[groupName];
                list.push({name: groupName});
                
                for (var p in group)
                    list.push({name: p, value: group[p]});
            }
            
            dwr.util.addRows("varList", list, [
                    function(t) { return encodeHtml(t.name); },
                    function(t) { return clipValue(t.value); },
                ],
                {
                    rowCreator: function(options) {
                        var tr = document.createElement("tr");
                        if (options.rowData.value == null)
                            tr.className = "rowHeader";
                        else
                            tr.className = "row"+ (options.rowIndex % 2 == 0 ? "" : "Alt");
                        return tr;
                    },
                    cellCreator: function(options) {
                        var td = document.createElement("td");
                        if (options.rowData.value == null) {
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
            
            setTimeout(getStatusVars, 2000);
        });            
    }
  </script>
  
  <div>
    <fmt:message key="internal.status"/> |
    <a href="/internal/threads.shtm"><fmt:message key="internal.threads"/></a> |
    <a href="/internal/workItems.shtm"><fmt:message key="internal.workItems"/></a> |
    <a href="/internal/queueStats.shtm"><fmt:message key="internal.queue.stats"/></a> |
    <a href="/internal/tasks.shtm"><fmt:message key="internal.tasks.stats"/></a>
    <tag:help id="internalMetrics"/>
  </div>
  <br/>
  
  <table>
    <tbody id="varList"></tbody>
  </table>
</tag:page>