<%--
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
--%>
<%@ include file="/WEB-INF/jsp/include/tech.jsp" %>
<div id="staticEditorPopup" style="display:none;left:0px;top:0px;" class="windowDiv">
  <table cellpadding="0" cellspacing="0"><tr><td>
    <table width="100%">
      <tr>
        <td>
          <tag:img png="html" title="viewEdit.static.editor" style="display:inline;"/>
        </td>
        <td align="right">
          <tag:img png="save" onclick="staticEditor.save()" title="common.save" style="display:inline;"/>&nbsp;
          <tag:img png="cross" onclick="staticEditor.close()" title="common.close" style="display:inline;"/>
        </td>
      </tr>
    </table>
    <table>
      <tr>
        <td class="formField"><textarea id="staticPointContent" rows="10" cols="50"></textarea></td>
      </tr>
    </table>
  </td></tr></table>
  
  <script type="text/javascript">
    function StaticEditor() {
        this.componentId = null;
        
        this.open = function(compId) {
            staticEditor.componentId = compId;
            
            GraphicalViewDwr.getViewComponent(compId, function(comp) {
                // Update the data in the form.
                $set("staticPointContent", comp.content);
                show("staticEditorPopup");
            });
            
            positionEditor(compId, "staticEditorPopup");
        };
        
        this.close = function() {
            hide("staticEditorPopup");
        };
        
        this.save = function() {
            GraphicalViewDwr.saveHtmlComponent(staticEditor.componentId, $get("staticPointContent"), function() {
                staticEditor.close();
                updateHtmlComponentContent("c"+ staticEditor.componentId, $get("staticPointContent"));
            });
        };
    }
    var staticEditor = new StaticEditor();
  </script>
</div>