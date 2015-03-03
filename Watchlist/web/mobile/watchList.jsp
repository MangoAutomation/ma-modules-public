<%@page import="com.serotonin.m2m2.Constants"%>
<%@page import="com.serotonin.m2m2.view.ShareUser"%>
<%@ taglib prefix="watchlist" tagdir="/WEB-INF/tags/watchlists" %>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>


<watchlist:mobilePage dwr="WatchListDwr" 
	js="/resources/view.js,${modulePath}/web/mobile/watchList.js"
	onload="">
	<jsp:attribute name="styles">
		<!-- Put Styles here for use in tag -->
	</jsp:attribute>
	
	<jsp:body>
	<script type="text/javascript">
	mango.view.initWatchlist();
	
	function flash(pointId, count) {
		
		var targetRow = document.getElementById('pointRow_'+pointId);
	    targetRow.style.backgroundColor = (targetRow.style.backgroundColor== "rgb(216, 226, 204)" ? "white" : "rgb(216, 226, 204)");
	    if (count >= 9)
	        return;
	    window.setTimeout(function() {
	        flash(pointId, count+1);
	    }, 350);
	}
	
	function removeFromWatchList(pointId) {
			if(confirm('Are you sure you want to remove this point from WatchList'))
			{
	          WatchListDwr.removeFromWatchList(pointId);
			 }
	}
	function moveRowUp(pointId) {
	var rowTitle = document.getElementById('pointTitleRow_'+pointId);
	var rowData = document.getElementById('pointRow_'+pointId);
	var rowTitle2 = document.getElementById('pointTitleRow_'+2);
	var rowTitlePrev = rowTitle.previousSibling;
	alert(rowTitlePrev);
	rowTitlePrev.parentNode.insertBefore(rowTitle, rowTitle2);
	//row.parentNode.removeChild(row);
	//WatchListDwr.moveUp(pointId);
	}
	function moveRowDown(pointId) {
	WatchListDwr.moveDown(pointId);
	}
	function savePointData(pointId) {
	mango.view.setPoint(pointId,pointId+'',document.getElementById('changeValueInput_'+pointId).value);
	
	document.getElementById('pointValueSpan_'+pointId).innerHTML=document.getElementById('changeValueInput_'+pointId).value;
	hideDataDiv(pointId);
	hideButtonsDiv(pointId);
	flash(pointId,0);
	}
	function showSetDataDiv(pointId) {
	var targetRow = document.getElementById('pointRow_'+pointId);
	var setDataRow = document.createElement('tr');
	setDataRow.setAttribute('id','pointRowSetData_'+pointId);
	setDataRow.innerHTML='<td colspan="3"><div id="changeValueDiv_'+pointId+'" style="visibility:visible;width:100%;text-align:center;font-size:16px;"><span style="font-size: 2.3em;">Enter a value to Set : </span><input style="font-size: 2em;height:60px;width:220px;" type="text" id="changeValueInput_'+pointId+'"/>&nbsp;&nbsp;<input style="height:60px;width:100px;margin-top:-15px;" type="button" value="Save" onclick="savePointData('+pointId+');"></div></td>';
	targetRow.parentNode.insertBefore(setDataRow, targetRow.nextSibling);
	}
	function showButtonsDiv(pointId) {
	var testButtonsRow = document.getElementById('pointRowButtons_'+pointId);
	if(testButtonsRow==null)
	{
	var targetRow = document.getElementById('pointRow_'+pointId);
	var setDataRow = document.createElement('tr');
	setDataRow.setAttribute('id','pointRowButtons_'+pointId);
	setDataRow.innerHTML='<td style="width:33%;text-align:center;border-right:1px solid #804000;"><img onclick="showSetDataDiv(\''+pointId+'\');" style="width:60px;margin-right:5px;" src="/images/icon_edit.png"/></td><td style="width:33%;text-align:center;border-right:1px solid #804000;"><img onclick="showChart('+pointId+')" style="width:60px;margin-right:5px;" src="/images/icon_chart.png"/></td><td style="width:33%;text-align:center;border-right:1px solid #804000;"><img style="width:60px;margin-right:5px;" src="/images/icon_comp.png"/></td>';
	targetRow.parentNode.insertBefore(setDataRow, targetRow.nextSibling);
	}
	else
	{
	hideButtonsDiv(pointId);
	}
	}
	function hideDataDiv(pointId) {
	var row = document.getElementById('pointRowSetData_'+pointId);
	row.parentNode.removeChild(row);
	}
	function hideButtonsDiv(pointId) {
	var row = document.getElementById('pointRowButtons_'+pointId);
	row.parentNode.removeChild(row);
	}
	function showChart(pointId) {
					alert('RRR'+pointId);
				  //mango.view.watchList.setDataImpl(pointId);
	              //alert('aaaaa'+$get('p'+ pointId +'Chart'));
	}
	
	</script>
	<div style="width:100%;">
	

	  <table style="width:100%" id="pointsTable">
		<caption>Watch List</caption>
		<thead>
		<tr>
			<th scope="col" style="width;33%;min-width:33%;">Name</th>
			<th scope="col" style="width;33%;">Value</th>
			<th scope="col" style="width;33%;">Timestamp</th>
		</tr>
		</thead>
		<c:set var="odd" value="1"/>
	    <c:forEach items="${watchListData}" var="state">
		<c:set var="odd" value="${-odd}"/>
	
			<tr class="odd" id="pointTitleRow_${state.id}" onclick="showButtonsDiv('${state.id}');">
			<!--<td>${state.id}</td>-->
	        <td colspan="3">${state.name}</td>
			
			<!--<td rowspan="2" style="text-align:top;"><div style="width:100%;height:100%;clear:both;overflow:hidden;">
			<div style="float:left"><img onclick="showSetDataDiv('${state.id}');" style="width:40px;margin-right:5px;" src="/images/icon_edit.png"/><img style="width:40px;margin-right:5px;" src="/images/icon_chart.png"/><img style="width:40px;" src="/images/tick.png"/></div>
			<div style="clear:both;float:left"><img style="width:20px;" src="/images/arrow_up_thin.png" onclick="moveRowUp(${state.id});"/><img style="width:20px;margin-right:5px;" src="/images/arrow_down_thin.png" onclick="moveRowDown(${state.id});"/><img style="width:40px;margin-right:5px;" src="/images/icon_comp.png"/><img style="width:40px;" onclick="removeFromWatchList('${state.id}');" src="/images/delete.png"/></div>
			</div></td>-->
			</tr>
			<tr id="pointRow_${state.id}" onclick="showButtonsDiv('${state.id}');">
	        <c:choose>
	          <c:when test="${state.disabled}">
	            <td colspan="3"><fmt:message key="common.pointWarning"/></td>
	          </c:when>
	          <c:otherwise>
				<td style="width;33%;min-width:33%;">&nbsp;</td>
	            <td align="center" style="width;33%;"><span id="pointValueSpan_${state.id}">${state.value}</span></td>
	            <td style="width;33%;">${state.time}</td>
	          </c:otherwise>
	        </c:choose>
			</tr>
	
	    </c:forEach>
	  </table>
	  
	  <fmt:message key="header.watchlist"/>:
	  <sst:select id="watchListSelect" value="${selectedWatchList}"
	          onchange="window.location='/mobile/watch_list.shtm?watchListId='+ this.value">
	    <c:forEach items="${watchLists}" var="wl">
	      <sst:option value="${wl.key}">${wl.value}</sst:option>
	    </c:forEach>
	  </sst:select>
	
	  <a href="/mobile/watch_list.shtm"><fmt:message key="header.reload"/></a>
	  
	  <a href="/mobile/logout.htm"><fmt:message key="header.logout"/></a>
	</div>
	</jsp:body>


</watchlist:mobilePage>