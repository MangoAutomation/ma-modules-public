<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<div id="settingsEditorPopup" style="display:none;left:0px;top:0px;" class="windowDiv">
  <table cellpadding="0" cellspacing="0"><tr><td>
    <table width="100%">
      <tr>
        <td>
          <tag:img png="pencil" title="viewEdit.settings.editor" style="display:inline;"/>
          <span class="copyTitle" id="settingsComponentName"></span>
        </td>
        <td align="right">
          <tag:img png="save" onclick="settingsEditor.save()" title="common.save" style="display:inline;"/>&nbsp;
          <tag:img png="cross" onclick="settingsEditor.close()" title="common.close" style="display:inline;"/>
        </td>
      </tr>
    </table>
    <table>
      <tr>
        <td class="formLabelRequired"><fmt:message key="viewEdit.settings.point"/></td>
        <td class="formField"><select id="settingsPointList" onchange="settingsEditor.pointSelectChanged()"></select></td>
      </tr>
      <tr>
        <td class="formLabel"><fmt:message key="viewEdit.settings.nameOverride"/></td>
        <td class="formField"><input id="settingsPointName" type="text"/></td>
      </tr>
      <tr>
        <td class="formLabel"><fmt:message key="viewEdit.settings.settableOverride"/></td>
        <td class="formField"><input id="settingsSettable" type="checkbox"/></td>
      </tr>
      <tr>
        <td class="formLabel"><fmt:message key="viewEdit.settings.background"/></td>
        <td class="formField"><input id="settingsBkgdColor" type="text"/></td>
      </tr>
      <tr>
        <td class="formLabel"><fmt:message key="viewEdit.settings.displayControls"/></td>
        <td class="formField"><input id="settingsControls" type="checkbox"/></td>
      </tr>
      <tr>
        <td class="formLabel">x</td>
        <td class="formField"><input id="settingsX" type="text" class="formShort"/></td>
      </tr>
      <tr>
        <td class="formLabel">y</td>
        <td class="formField"><input id="settingsY" type="text" class="formShort"/></td>
      </tr>
    </table>
  </td></tr></table>
  
  <script type="text/javascript">
    // Script requires
    //  - Drag and Drop library for locating objects and positioning the window.
    //  - DWR utils for using $() prototype.
    //  - common.js
    function SettingsEditor() {
        this.componentId = null;
        this.pointList = [];
        
        this.open = function(compId) {
            settingsEditor.componentId = compId;
            
            GraphicalViewDwr.getViewComponent(compId, function(comp) {
                $set("settingsComponentName", comp.displayName);
                
                // Update the point list
                settingsEditor.updatePointList(comp.supportedDataTypes);
                
                // Update the data in the form.
                $set("settingsPointList", comp.dataPointId);
                $set("settingsPointName", comp.nameOverride);
                $set("settingsSettable", comp.settableOverride);
                $set("settingsBkgdColor", comp.bkgdColorOverride);
                $set("settingsControls", comp.displayControls);
                $set("settingsX", comp.x);
                $set("settingsY", comp.y);

                settingsEditor.pointSelectChanged();
                show("settingsEditorPopup");
            });
            
            positionEditor(compId, "settingsEditorPopup");
        };
        
        this.close = function() {
            hide("settingsEditorPopup");
            hideContextualMessages("settingsEditorPopup");
        };
        
        this.save = function() {
            hideContextualMessages("settingsEditorPopup");
            GraphicalViewDwr.setPointComponentSettings(settingsEditor.componentId, $get("settingsPointList"),
                    $get("settingsPointName"), $get("settingsSettable"), $get("settingsBkgdColor"),
                    $get("settingsControls"), $get("settingsX"), $get("settingsY"), function(response) {
                if (response.hasMessages) {
                    showDwrMessages(response.messages);
                }
                else {
                    var div = $("c"+ settingsEditor.componentId);
                    div.style.left = response.data.x +"px";
                    div.style.top = response.data.y +"px";
                    
                    settingsEditor.close();
                    MiscDwr.notifyLongPoll(mango.longPoll.pollSessionId);
                }
            });
        };
        
        this.setPointList = function(pointList) {
            settingsEditor.pointList = pointList;
        };
        
        this.pointSelectChanged = function() {
            var point = getElement(settingsEditor.pointList, $get("settingsPointList"));
            if (!point || !point.settable) {
                $set("settingsSettable", false);
                $("settingsSettable").disabled = true;
            }
            else
                $("settingsSettable").disabled = false;
        };
        
        this.updatePointList = function(dataTypes) {
            dwr.util.removeAllOptions("settingsPointList");
            var sel = $("settingsPointList");
            sel.options[0] = new Option("", 0);
            
            for (var i=0; i<settingsEditor.pointList.length; i++) {
                if (contains(dataTypes, settingsEditor.pointList[i].dataType))
                    sel.options[sel.options.length] = new Option(settingsEditor.pointList[i].name,
                            settingsEditor.pointList[i].id);
            }
        };
    }
    var settingsEditor = new SettingsEditor();
  </script>
</div>