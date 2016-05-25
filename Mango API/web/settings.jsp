<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Terry Packer
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<script type="text/javascript" src="/dwr/interface/MangoApiSystemSettingsDwr.js"></script>
<script type="text/javascript">
	
	function loadMangoApiSettings(){
		MangoApiSystemSettingsDwr.loadHeaders(function(response){
			displayHeaders(response.data.headers);
		});
	}
	
	function displayHeaders(headers){
		dwr.util.removeAllRows('mangoApiCorsHeaders');
		
		  if (headers.length == 0) {
		        show("noCorsHeaders");
		        hide("corsTableHeaders");
		    }
		    else {
		        hide("noCorsHeaders");
		        show("corsTableHeaders");
		        dwr.util.addRows("mangoApiCorsHeaders", headers,
		            [
		                function(data) { return data.key; },
		                function(data) { return data.value; },
		                function(data) { return; }
		            ],
		            {
		                rowCreator: function(options) {
		                    var tr = document.createElement("tr");
		                    tr.className = "smRow"+ (options.rowIndex % 2 == 0 ? "" : "Alt");
		                    return tr;
		                }
		            }
		        );
	  		}
		}
	

     //Init the settings on the page
     loadMangoApiSettings(); 


</script>

    <table>    
      <tr>
        <td class="formLabel"><fmt:message key="rest.settings.corsHeaders"/><tag:help id="mangoApiSettings"/></td>
        <td class="formField">
          <table>
            <tr id="corsTableHeaders"><th><fmt:message key="rest.settings.header"/></th><th><fmt:message key="common.value"/></th><th></th></tr>
            <tr id="noCorsHeaders"><td colspan="3"><fmt:message key="common.noData"/></td></tr>
            <tbody id="mangoApiCorsHeaders"/>
          </table>
        </td>
      </tr>
      <tr><td colspan="2" id="saveMangoApiMessage" class="formError"></td></tr>
    </table>
    