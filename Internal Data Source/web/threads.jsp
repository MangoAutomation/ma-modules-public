<%--
    Copyright (C) 2006-2013 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<tag:page dwr="ThreadsDwr">
  <script type="text/javascript">
    require(["dojo/ready"], function(ready) {
        ready(function() {
            getThreadInfo();
        });
    });
    
    function getThreadInfo() {
        ThreadsDwr.getThreadInfo(function(result) {
            var threads = result.data.threads;
            
            $set("threadCount", threads.length);
            
            // Sort the threads first?
            threads.sort(function(a,b) {
                var diff = b.tenSecCpuTime - a.tenSecCpuTime;
                if (diff == 0)
                    diff = b.cpuTime - a.cpuTime;
                return diff;
            });
            
            dwr.util.removeAllRows("threadList");
            dwr.util.addRows("threadList", threads, [
                    function(t) { return t.id; },
                    function(t) { return t.name; },
                    function(t) { return t.tenSecCpuTime / 1000000000; },
                    function(t) { return t.cpuTime / 1000000000; },
                    function(t) { return t.state; },
                    function(t) {
                        if (!t.stackTrace || t.stackTrace.length == 0)
                            return "";
                        
                        var s = "";
                        for (var i=0; i<t.stackTrace.length; i++)
                            s += t.stackTrace[i].fileName + ":" + t.stackTrace[i].lineNumber +"<br/>";
                        return s;
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
                        td.vAlign = "top";
                        return td;
                    }
                }
            );
            
            setTimeout(getThreadInfo, 5000);
        });            
    }
  </script>
  
  <div>
    <a href="/internal/status.shtm"><fmt:message key="internal.status"/></a> |
    <fmt:message key="internal.threads"/> |
    <a href="/internal/workItems.shtm"><fmt:message key="internal.workItems"/></a> |
    <a href="/internal/queueStats.shtm"><fmt:message key="internal.queue.stats"/></a> |
    <a href="/internal/tasks.shtm"><fmt:message key="internal.tasks.stats"/></a>
  </div>
  <br/>
  
  <div><fmt:message key="internal.thread.count"/> <span id="threadCount"></span></div>
  <table>
    <thead>
      <tr class="rowHeader">
        <td><fmt:message key="internal.id"/></td>
        <td><fmt:message key="common.name"/></td>
        <td><fmt:message key="internal.thread.tenSecCpuTime"/></td>
        <td><fmt:message key="internal.thread.cpuTime"/></td>
        <td><fmt:message key="internal.thread.state"/></td>
        <td><fmt:message key="internal.thread.location"/></td>
      </tr>
    </thead>
    <tbody id="threadList"></tbody>
  </table>
</tag:page>