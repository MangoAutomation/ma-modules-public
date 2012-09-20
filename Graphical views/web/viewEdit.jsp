<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<%@page import="com.serotonin.m2m2.view.ShareUser"%>

<tag:page dwr="GraphicalViewDwr" js="/resources/view.js,${modulePath}/web/graphicalViews.js,${modulePath}/web/wz_jsgraphics.js">
  <style type="text/css">
    #viewContent #compCoords {
        position: absolute; 
        right: -1px; 
        top: -20px; 
        text-align: right;
        background: #F8BB00;
        white-space: nowrap;
        border: 1px solid #F07800;
        padding: 2px;
        border-bottom: none;
    }
  </style>
  <script type="text/javascript">
    dojo.require("dojo.dnd.move");
    
    mango.view.initEditView();
    mango.share.dwr = GraphicalViewDwr;
    
    var viewId = ${view.id};
    
    dojo.ready(function() {
        <c:forEach items="${view.viewComponents}" var="vc">
          <c:set var="compContent"><sst:convert obj="${vc}"/></c:set>
          createViewComponent(${m2m2:escapeScripts(compContent)}, false);
        </c:forEach>
        
        GraphicalViewDwr.editInit(function(result) {
            mango.share.users = result.shareUsers;
            mango.share.writeSharedUsers(result.viewUsers);
            dwr.util.addOptions($("componentList"), result.componentTypes, "key", "value");
            settingsEditor.setPointList(result.pointList);
            compoundEditor.setPointList(result.pointList);
            MiscDwr.notifyLongPoll(mango.longPoll.pollSessionId);
        });
    });
    
    function addViewComponent() {
        GraphicalViewDwr.addComponent($get("componentList"), function(viewComponent) {
            createViewComponent(viewComponent, true);
            MiscDwr.notifyLongPoll(mango.longPoll.pollSessionId);
        });
    }
    
    function createViewComponent(viewComponent, center) {
        var content;
    
        if (viewComponent.pointComponent)
            content = $("pointTemplate").cloneNode(true);
        else if (viewComponent.defName == 'imageChart')
            content = $("imageChartTemplate").cloneNode(true);
        else if (viewComponent.compoundComponent)
            content = $("compoundTemplate").cloneNode(true);
        else
            content = $("htmlTemplate").cloneNode(true);
        
        configureComponentContent(content, viewComponent, $("viewContent"), center);
        
        if (viewComponent.defName == 'simpleCompound') {
            childContent = $("compoundChildTemplate").cloneNode(true);
            configureComponentContent(childContent, viewComponent.leadComponent, $("c"+ viewComponent.id +"Content"),
                    false);
        }
        else if (viewComponent.defName == 'imageChart')
            ;
        else if (viewComponent.compoundComponent) {
            // Compound components only have their static content set at page load.
            $set(content.id +"Content", viewComponent.staticContent);
            
            // Add the child components.
            var childContent;
            for (var i=0; i<viewComponent.childComponents.length; i++) {
                childContent = $("compoundChildTemplate").cloneNode(true);
                configureComponentContent(childContent, viewComponent.childComponents[i].viewComponent,
                        $("c"+ viewComponent.id +"ChildComponents"), false);
            }
        }
        
        addDnD(content.id);
        
        if (center)
            updateViewComponentLocation(content.id);
    }
    
    function configureComponentContent(content, viewComponent, parent, center) {
        content.id = "c"+ viewComponent.id;
        content.viewComponentId = viewComponent.id;
        updateNodeIds(content, viewComponent.id);
        parent.appendChild(content);
        
        if (viewComponent.defName == "html")
            // HTML components only get updated at page load and editing.
            updateHtmlComponentContent(content.id, viewComponent.content);
        
        show(content);
        
        if (center) {
            // Calculate the location for the new point. For now just put it in the center.
            var bkgd = $("viewBackground");
            var bkgdBox = dojo.getMarginBox(bkgd);
            var compContentBox = dojo.getMarginBox(content);
            content.style.left = parseInt((bkgdBox.w - compContentBox.w) / 2) +"px";
            content.style.top = parseInt((bkgdBox.h - compContentBox.h) / 2) +"px";
        }
        else {
            content.style.left = viewComponent.x +"px";
            content.style.top = viewComponent.y +"px";
        }
    }
    
    function updateNodeIds(elem, id) {
        var i;
        for (i=0; i<elem.attributes.length; i++) {
            if (elem.attributes[i].value && elem.attributes[i].value.indexOf("_TEMPLATE_") != -1)
                elem.attributes[i].value = elem.attributes[i].value.replace(/_TEMPLATE_/, id);
        }
        for (var i=0; i<elem.childNodes.length; i++) {
            if (elem.childNodes[i].attributes)
                updateNodeIds(elem.childNodes[i], id);
        }
    }
    
    function updateHtmlComponentContent(id, content) {
        if (!content || content == "")
            $set(id +"Content", '<img src="images/html.png" alt=""/>');
        else
            $set(id +"Content", content);
    }
    
    function openStaticEditor(viewComponentId) {
        closeEditors();
        staticEditor.open(viewComponentId);
    }
    
    function openSettingsEditor(cid) {
        closeEditors();
        settingsEditor.open(cid);
    }
    
    function openGraphicRendererEditor(cid) {
        closeEditors();
        graphicRendererEditor.open(cid);
    }
    
    function openCompoundEditor(cid) {
        closeEditors();
        compoundEditor.open(cid);
    }
    
    function positionEditor(compId, editorId) {
        // Position and display the renderer editor.
        var pDim = getNodeBounds($("c"+ compId));
        var editDiv = $(editorId);
        editDiv.style.left = (pDim.x + pDim.w + 20) +"px";
        editDiv.style.top = (pDim.y + 10) +"px";
    }
    
    function closeEditors() {
        settingsEditor.close();
        graphicRendererEditor.close();
        staticEditor.close();
        compoundEditor.close();
    }
    
    function updateViewComponentLocation(divId) {
        var div = $(divId);
        var lt = div.style.left;
        var tp = div.style.top;
        
        // Remove the 'px's from the positions.
        lt = lt.substring(0, lt.length-2);
        tp = tp.substring(0, tp.length-2);
        
        // Save the new location.
        GraphicalViewDwr.setViewComponentLocation(div.viewComponentId, lt, tp);
    }
    
    function addDnD(divId) {
        var div = $("viewContent")
        var cs = dojo.getComputedStyle(div);
        var mb = dojo.getMarginBox(div, cs);
        
        div = $(divId)
        var coords = $("compCoords");
        //var moveable = new dojo.dnd.move.parentConstrainedMoveable(div);
        var moveable = new dojo.dnd.move.boxConstrainedMoveable(div, { box: { l:0, t:0, w:mb.w, h:mb.h }})
        
        // Save the movable in the div in case it gets deleted. See below.
        div.moveable = moveable;
        
        moveable.onMoveStart = function() {
            closeEditors();
            var lt = div.style.left;
            var tp = div.style.top;
            lt = lt.substring(0, lt.length-2);
            tp = tp.substring(0, tp.length-2);
            moveable.onMoved(moveable, { l: lt, t: tp });
            show(coords);
        };
        
        moveable.onMoved = function(mover, leftTop) {
            $set(coords, leftTop.l +", "+ leftTop.t);
        };
        
        moveable.onMoveStop = function() { 
            hide(coords);
            updateViewComponentLocation(divId);
        };
    }
    
    function deleteViewComponent(viewComponentId) {
        closeEditors();
        GraphicalViewDwr.deleteViewComponent(viewComponentId);
        
        var div = $("c"+ viewComponentId);
        
        // Unregister the moveable from the DnD manager.
        div.moveable.destroy();
        
        // Disconnect the event handling for drag ends on this guy.
        $("viewContent").removeChild(div);
    }
    
    function getViewComponentId(node) {
        while (!(node.viewComponentId))
            node = node.parentNode;
        return node.viewComponentId;
    }
    
    function iconizeClicked() {
        GraphicalViewDwr.getViewComponentIds(function(ids) {
            var i, comp, content;
            if ($get("iconifyCB")) {
                mango.view.edit.iconize = true;
                for (i=0; i<ids.length; i++) {
                    comp = $("c"+ ids[i]);
                    content = $("c"+ ids[i] +"Content");
                    if (!comp.savedContent)
                        comp.savedContent = content.innerHTML;
                    content.innerHTML = "<img src='images/logo_icon.gif'/>";
                }
            }
            else {
                mango.view.edit.iconize = false;
                for (i=0; i<ids.length; i++) {
                    comp = $("c"+ ids[i]);
                    content = $("c"+ ids[i] +"Content");
                    if (comp.savedState)
                        mango.view.setContent(comp.savedState);                
                    else if (comp.savedContent)
                        content.innerHTML = comp.savedContent;
                    else
                        content.innerHTML = '';
                    comp.savedState = null;
                    comp.savedContent = null;
                }
            }
        });
    }
    
    function bgUpload() {
        if (!$get("backgroundImage"))
            alert("<m2m2:translate key="viewEdit.chooseImage" escapeDQuotes="true"/>");
        else {
            GraphicalViewDwr.clearBackground(function() {
                document.getElementById("daform").submit();
                bgUploadCheck();
            });
        }
    }
    
    function bgClear() {
        GraphicalViewDwr.clearBackground(function() { imageUpdate("images/spacer.gif", 740, 500); });
    }
    
    function bgUploadCheck() {
        GraphicalViewDwr.getBackgroundUrl(function(bgurl) {
            if (bgurl)
                imageUpdate(bgurl);
            else
                setTimeout(bgUploadCheck, 500);
        });
    }
    
    function imageUpdate(url, width, height) {
        var bg = $("viewBackground");
        bg.src = url;
        if (width) {
            bg.style.width = width +"px";
            bg.style.height = height +"px";
        }
        else {
            bg.style.width = "";
            bg.style.height = "";
        }
    }
    
    function saveView() {
        hideContextualMessages($("viewProperties"));
        GraphicalViewDwr.saveView($get("name"), $get("xid"), $get("anonymousAccess"), function(result) {
            if (result.hasMessages)
                showDwrMessages(result.messages);
            else {
                viewId = result.data.view.id;
                alert("<m2m2:translate key="viewEdit.confirmSaved" escapeDQuotes="true"/>");
                // Write confirmation message?
            }
        });
    }
    
    function deleteView() {
        if (confirm("<m2m2:translate key="viewEdit.confirmDelete" escapeDQuotes="true"/>"))
            GraphicalViewDwr.deleteView(function(result) { window.location = "/views.shtm"; });
    }
    
    function cancelEdit() {
        if (viewId == <tag:newId/>)
            window.location = "/views.shtm";
        else
            window.location = "/views.shtm?viewId="+ ${view.id};
    }
  </script>
  
  <table>
    <tr>
      <td valign="top">
        <div class="borderDiv marR">
          <table id="viewProperties">
            <tr>
              <td colspan="2">
                <tag:img src="${modulePath}/web/slide.png" title="viewEdit.editView"/>
                <span class="smallTitle"><fmt:message key="viewEdit.viewProperties"/></span>
                <tag:help id="editingGraphicalViews"/>
              </td>
            </tr>
            
            <tr>
              <td class="formLabelRequired"><fmt:message key="viewEdit.name"/></td>
              <td class="formField"><input type="text" id="name" value="${view.name}"/></td>
            </tr>
            
            <tr>
              <td class="formLabelRequired"><fmt:message key="common.xid"/></td>
              <td class="formField"><input type="text" id="xid" value="${view.xid}"/></td>
            </tr>
            
            <tr>
              <td class="formLabelRequired"><fmt:message key="viewEdit.anonymous"/></td>
              <td class="formField">
                <sst:select id="anonymousAccess" value="${view.anonymousAccess}">
                  <sst:option value="<%= Integer.toString(ShareUser.ACCESS_NONE) %>"><fmt:message key="common.access.none"/></sst:option>
                  <sst:option value="<%= Integer.toString(ShareUser.ACCESS_READ) %>"><fmt:message key="common.access.read"/></sst:option>
                  <sst:option value="<%= Integer.toString(ShareUser.ACCESS_SET) %>"><fmt:message key="common.access.set"/></sst:option>
                </sst:select>
              </td>
            </tr>
            
            <tr>
              <td class="formLabelRequired"><fmt:message key="viewEdit.background"/></td>
              <td class="formField">
                <iframe id="target_upload" name="target_upload" src="" style="display: none;"></iframe>
                <form id="daform" action="/graphicalViewsBackgroundUpload" method="post" enctype="multipart/form-data" target="target_upload">
                  <input type="file" name="backgroundImage"/><br/>
                  <input type="button" value="<fmt:message key="viewEdit.upload"/>" onclick="bgUpload();"/>&nbsp;
                  <input type="button" value="<fmt:message key="viewEdit.clearImage"/>" onclick="bgClear();"/>
                </form>
              </td>
            </tr>
            
            <tr>
              <td colspan="2" align="center">
              </td>
            </tr>
          </table>
        </div>
      </td>
      
      <td valign="top">
        <div class="borderDiv">
          <tag:sharedUsers doxId="viewSharing" noUsersKey="share.noViewUsers"/>
        </div>
      </td>
    </tr>
  </table>
  
  <table>
    <tr>
      <td>
        <fmt:message key="viewEdit.viewComponents"/>:
        <select id="componentList"></select>
        <tag:img png="add" title="viewEdit.addViewComponent" onclick="addViewComponent()"/>
      </td>
      <td style="width:30px;"></td>
      <td>
        <input type="checkbox" id="iconifyCB" onclick="iconizeClicked();"/>
        <label for="iconifyCB"><fmt:message key="viewEdit.iconify"/></label>
      </td>
    </tr>
  </table>
  
  <table width="100%" cellspacing="0" cellpadding="0">
    <tr>
      <td>
        <table cellspacing="0" cellpadding="0">
          <tr>
            <td colspan="3">
              <div id="viewContent" class="borderDiv" style="left:0px;top:0px;float:left;
                      padding-right:1px;padding-bottom:1px;">
                <span id="compCoords" style="display:none;"></span>
                
                <c:choose>
                  <c:when test="${empty view.backgroundFilename}">
                    <img id="viewBackground" src="images/spacer.gif" alt="" style="top:1px;left:1px;width:740px;height:500px;"/>
                  </c:when>
                  <c:otherwise>
                    <img id="viewBackground" src="${view.backgroundFilename}" alt="" style="top:1px;left:1px;"/>
                  </c:otherwise>
                </c:choose>
                
                <%@ include file="include/staticEditor.jsp" %>
                <%@ include file="include/settingsEditor.jsp" %>
                <%@ include file="include/graphicRendererEditor.jsp" %>
                <%@ include file="include/compoundEditor.jsp" %>
              </div>
            </td>
          </tr>
          
          <tr><td colspan="3">&nbsp;</td></tr>
          
          <tr>
            <td colspan="2" align="center">
              <input type="button" value="<fmt:message key="common.save"/>" onclick="saveView();"/>
              <input type="button" value="<fmt:message key="common.delete"/>" onclick="deleteView();"/>
              <input type="button" value="<fmt:message key="common.close"/>" onclick="cancelEdit();"/>
            </td>
            <td></td>
          </tr>
        </table>
      
        <div id="pointTemplate" onmouseover="showLayer('c'+ getViewComponentId(this) +'Controls');"
                onmouseout="hideLayer('c'+ getViewComponentId(this) +'Controls');"
                style="position:absolute;left:0px;top:0px;display:none;">
          <div id="c_TEMPLATE_Content"><img src="images/icon_comp.png" alt=""/></div>
          <div id="c_TEMPLATE_Controls" class="controlsDiv">
            <table cellpadding="0" cellspacing="1">
              <tr onmouseover="showMenu('c'+ getViewComponentId(this) +'Info', 16, 0);"
                      onmouseout="hideLayer('c'+ getViewComponentId(this) +'Info');">
                <td>
                  <img src="images/information.png" alt=""/>
                  <div id="c_TEMPLATE_Info" onmouseout="hideLayer(this);">
                    <tag:img png="hourglass" title="common.gettingData"/>
                  </div>
                </td>
              </tr>
              <tr><td><tag:img png="pencil" onclick="openSettingsEditor(getViewComponentId(this))"
                      title="viewEdit.editPointView"/></td></tr>
              <tr><td><tag:img png="graphic" onclick="openGraphicRendererEditor(getViewComponentId(this))"
                      title="viewEdit.editGraphicalRenderer"/></td></tr>
              <tr><td><tag:img png="delete" onclick="deleteViewComponent(getViewComponentId(this))"
                      title="viewEdit.deletePointView"/></td></tr>
            </table>
          </div>
          <div style="position:absolute;left:-16px;top:0px;z-index:1;">
            <div id="c_TEMPLATE_Warning" style="display:none;"
                    onmouseover="showMenu('c'+ getViewComponentId(this) +'Messages', 16, 0);"
                    onmouseout="hideLayer('c'+ getViewComponentId(this) +'Messages');">
              <tag:img png="warn" title="common.warning"/>
              <div id="c_TEMPLATE_Messages" onmouseout="hideLayer(this);" class="controlContent"></div>
            </div>
          </div>
        </div>
        
        <div id="htmlTemplate" onmouseover="showLayer('c'+ getViewComponentId(this) +'Controls');"
                onmouseout="hideLayer('c'+ getViewComponentId(this) +'Controls');"
                style="position:absolute;left:0px;top:0px;display:none;">
          <div id="c_TEMPLATE_Content"></div>
          <div id="c_TEMPLATE_Controls" class="controlsDiv">
            <table cellpadding="0" cellspacing="1">
              <tr><td><tag:img png="pencil" onclick="openStaticEditor(getViewComponentId(this))"
                      title="viewEdit.editStaticView"/></td></tr>
              <tr><td><tag:img png="html_delete" onclick="deleteViewComponent(getViewComponentId(this))"
                      title="viewEdit.deleteStaticView"/></td></tr>
            </table>
          </div>
        </div>
        
        <div id="imageChartTemplate" onmouseover="showLayer('c'+ getViewComponentId(this) +'Controls');"
                onmouseout="hideLayer('c'+ getViewComponentId(this) +'Controls');"
                style="position:absolute;left:0px;top:0px;display:none;">
          <span id="c_TEMPLATE_Content"></span>
          <div id="c_TEMPLATE_Controls" class="controlsDiv">
            <table cellpadding="0" cellspacing="1">
              <tr><td><tag:img png="pencil" onclick="openCompoundEditor(getViewComponentId(this))"
                      title="viewEdit.editPointView"/></td></tr>
              <tr><td><tag:img png="delete" onclick="deleteViewComponent(getViewComponentId(this))"
                      title="viewEdit.deletePointView"/></td></tr>
            </table>
          </div>
        </div>
          
        <div id="compoundTemplate" onmouseover="showLayer('c'+ getViewComponentId(this) +'Controls');"
                onmouseout="hideLayer('c'+ getViewComponentId(this) +'Controls');"
                style="position:absolute;left:0px;top:0px;display:none;">
          <span id="c_TEMPLATE_Content"></span>
          <div id="c_TEMPLATE_Controls" class="controlsDiv">
            <table cellpadding="0" cellspacing="1">
              <tr onmouseover="showMenu('c'+ getViewComponentId(this) +'Info', 16, 0);"
                      onmouseout="hideLayer('c'+ getViewComponentId(this) +'Info');">
                <td>
                  <img src="images/information.png" alt=""/>
                  <div id="c_TEMPLATE_Info" onmouseout="hideLayer(this);">
                    <tag:img png="hourglass" title="common.gettingData"/>
                  </div>
                </td>
              </tr>
              <tr><td><tag:img png="pencil" onclick="openCompoundEditor(getViewComponentId(this))"
                      title="viewEdit.editPointView"/></td></tr>
              <tr><td><tag:img png="delete" onclick="deleteViewComponent(getViewComponentId(this))"
                      title="viewEdit.deletePointView"/></td></tr>
            </table>
          </div>
          
          <div id="c_TEMPLATE_ChildComponents"></div>
        </div>
        
        <div id="compoundChildTemplate" style="position:absolute;left:0px;top:0px;display:none;">
          <div id="c_TEMPLATE_Content"><img src="images/icon_comp.png" alt=""/></div>
        </div>
      </td>
    </tr>
  </table>
</tag:page>