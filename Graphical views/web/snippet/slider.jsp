<!-- 
  This is a work in progress and isn't assigned to anything yet
  
  The general idea is that this would be the HTML for a component
  and could tie into any PointComponent as in Input for it.
  
  The Point Component could then just call:
  createSlider() when it is action to set the point
  and
  setSliderValue() when the value changes for the point.
  
  This works as the text for an HTML Component now
  and just requires this as a ServerSide Script for the component:
  
  var script = '<span id="test" style="font-size: 24px; color: #6699FF"';
  script += ' onClick="showSlider(this,' + value + ', 300, 0, 100, 1, 3, [\'low\',\'mid\',\'high\'])">';
  script += + value.toFixed(2) + '</span><span style="font-size: 14px; color:white"> % </span>';
  
  script += "<script type='text/javascript'>";
  script += "if(typeof setSliderValue == 'function')";
  script += "setSliderValue(dojo.byId('test')," + value + ");";
  script += "</script>";
  
  return script;
 -->
<script type="text/javascript">
require(["dojo/parser", "dijit/form/HorizontalSlider", "dijit/form/HorizontalRule", "dijit/form/HorizontalRuleLabels", "dojo/touch"]);

var sliderViewComponentIdToUse = 0;
var idRegex = /^\D+(\d+)$/
var sliderStyle = {
  width: "200px",
  
};

/**
 * element - Dom node for calling item, used to set colors and point via the component id
 * value - Number value to set in the slider
 * width - Number of pixels wide the slider should be
 * min - minimum Number value of slider
 * max - maximum Number value of slider 
 * pageIncrement - number of ticks to move the slider when the buttons are pressed
 * ruleCount - Number of ticks on slider and will define the only valid positions for the slider
 * labels - Array of String labels, size should match rule count but doesn't have to
 */
function showSlider(element, value, width, min, max, pageIncrement, ruleCount, labels){
  
  var containerDiv = element.parentNode.parentNode;
    var idToUse = getContainerId(containerDiv);
  
  if(typeof labels == 'undefined'){
    labels = generateLabels(ruleCount);
  }
  
    //If we are a new component, then load up a new slider
    if(idToUse != sliderViewComponentIdToUse){
      var oldContainerDiv = dojo.byId("c" + sliderViewComponentIdToUse);
      if(oldContainerDiv != null)
        oldContainerDiv.style.backgroundColor = ''; //Reset
      sliderViewComponentIdToUse = idToUse;
    createNewSlider(value, width, min, max, pageIncrement, ruleCount, labels);
    }
    
  show('sliderContainer');
  
  containerDiv.style.backgroundColor = 'lightgreen';  
}

/**
 * value - Number value to set in the slider
 * width - Number of pixels wide the slider should be
 * min - minimum Number value of slider
 * max - maximum Number value of slider 
 * pageIncrement - number of ticks to move the slider when the buttons are pressed
 * ruleCount - Number of ticks on slider and will define the only valid positions for the slider
 * labels - Array of String labels, size should match rule count but doesn't have to
 */
function createNewSlider(value, width, min, max, pageIncrement, ruleCount, labels){
  
  destroySlider();
  
  //Create the empty div
  var sliderContainerNode = dojo.byId('sliderContainer');
  var sliderNode = document.createElement('div');
  sliderNode.id = 'slider';
  sliderContainerNode.appendChild(sliderNode);
  
    //Create the Rule
    var rulesNode = document.createElement('div');
    rulesNode.id = 'rulesNode';
    sliderNode.appendChild(rulesNode);
    var sliderRules = new dijit.form.HorizontalRule({
      count: ruleCount,
      style: "height: 5px",
    }, rulesNode); 
    
    var labelsNode = document.createElement('div');
    labelsNode.id = 'rulesLabelsNode';
    sliderNode.appendChild(labelsNode);
    var sliderLabels = new dijit.form.HorizontalRuleLabels({
      	container: "bottomDecoration",
      	style: {
      	  fontFamily: "courier",
    	  fontSize: "15pt",
    	  color: "orange"
      	},
    	labels: labels,
    }, labelsNode);
    
    sliderStyle.width = width + "px";
  	slider = new dijit.form.HorizontalSlider({
    	style: sliderStyle,
    	minimum: min,
    	maximum: max,
    	value: value,
   		discreteValues: ruleCount,
    	pageIncrement: pageIncrement, //Number to page up or down with button
    	intermediateChanges: true, //Allow onchange to track while dragging
    	onMouseUp: function(event){
    		mango.view.setPoint(null, sliderViewComponentIdToUse, this.value);
    	},
    	onChange: function(value){
    		dojo.byId('sliderDisplay').innerHTML = value;
    	}
  	}, 'slider');
  	
  	/* Add a touch - release event for dojo touch */
  	dojo.touch.release(slider, function(event){
  		mango.view.setPoint(null, sliderViewComponentIdToUse, slider.value);
	});
  	
  	slider.startup();
  	
  	//Set the initial value in the display:
  	dojo.byId('sliderDisplay').innerHTML = value;
  	
}

function destroySlider(){
  var slider = dijit.byId('slider');
  if(slider != null){
    slider.destroy();
    var rules = dijit.byId('rulesNode');
    rules.destroy();
    var labels = dijit.byId('rulesLabelsNode');
    labels.destroy();
  } 
}

function hideSlider(){
  var slider = dijit.byId('slider');
  hide('sliderContainer');
}

/**
 * element - Dom node for calling element
 * value - Number to set value of slider
 */
function setSliderValue(element, value){
  var containerDiv = element.parentNode.parentNode;
    var idToUse = getContainerId(containerDiv);
  
  //Check if we even have a slider
  var slider = dijit.byId('slider');
  if(slider == null)
    return;
    
    if(idToUse != sliderViewComponentIdToUse){
      return; //Don't set a value for the slider we aren't using it
    }else{
      slider.set('value', value, false);
      dojo.byId('sliderDisplay').innerHTML = value;
    }
}

function getContainerId(container){
    var parts = container.id.match(idRegex);
    return parts[1]; //Will be the numeric part
}

function generateLabels(ruleCount){
  var labels = new Array();
  for(var i=0; i<ruleCount; i++){
    labels.push(i + "");
  }
  return labels;
}


</script>
<div id='sliderContainer' style='display:none'>
  <div id='sliderDisplay' style='font-size: 15pt; padding: 2px; text-align:center'></div>
</div>