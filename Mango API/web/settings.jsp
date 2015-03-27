<%--
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Terry Packer
--%><%@ include file="/WEB-INF/jsp/include/tech.jsp" %>

<tag:versionedJavascript src="/dwr/interface/MangoApiSystemSettingsDwr.js" />
<script type="text/javascript">
	
	function loadMangoApiSettings(){
		MangoApiSystemSettingsDwr.loadHeaders(function(response){
			displayHeaders(response.data.headers);
		});
	}

	function removeCorsHeader(value, rowId){
		MangoApiSystemSettingsDwr.removeHeader(value, function(response){
			if(!response.hasMessages){
				displayHeaders(response.data.headers);
			}else{
				showDwrMessages(response);
			}
		});
	}
	
	function addCorsHeader(){
		var name = $get('newCorsHeader');
		var value = $get('newCorsHeaderValue');
		MangoApiSystemSettingsDwr.addHeader(name, value, function(response){
			if(!response.hasMessages){
				displayHeaders(response.data.headers);
			}else{
				showDwrMessages(response);
			}
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
		                    tr.id = "cors_" + options.rowData.key;
		                    tr.className = "smRow"+ (options.rowIndex % 2 == 0 ? "" : "Alt");
		                    return tr;
		                },
		                cellCreator: function(options){
		                	var td = document.createElement("td");
		                	if(options.cellNum == 2){
		                		td.innerHTML =  "<img src='images/bullet_delete.png' class='ptr' "+
                                "onclick=\"removeCorsHeader('"+ options.rowData.key +"', 'cors_" + options.rowData.key + "');\"/>";
		                	}
		                	return td;
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
      <tr>
        <td class="formLabel"><fmt:message key="rest.settings.header"/></td>
        <td class="formField"><input id="newCorsHeader" type="text"/></td>
      </tr>
      <tr>
        <td class="formLabel"><fmt:message key="common.value"/></td>
        <td class="formField"><input id="newCorsHeaderValue" type="text"/></td>
      </tr>
      <tr>
        <td class="formField" colspan="2"><button onClick="addCorsHeader()"><fmt:message key="common.add"/></button></td>
      </tr>
      <tr><td colspan="2" id="saveMangoApiMessage" class="formError"></td></tr>
    </table>
    