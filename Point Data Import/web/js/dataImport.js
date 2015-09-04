require(["dijit/form/Select", "dojo/dom-style","dojo/_base/html", "put-selector/put", "dojo/when", "dojo/on",
         "dojo/_base/fx", "dojo/fx","dojo/query", "dojox/layout/ContentPane","dojo/dom-construct",
         "dojo/domReady!"],
function(Select,domStyle,html,put,when,on,
		baseFx,coreFx,query,ContentPane,domConstruct) {
	
	 var inputDiv = dojo.byId("upload-type-choice");
	 var uploadTypeChoice = new Select({
	        name: "uploadTypeSelect",
	        options: [
	            { label:  "Select File Type...", value: "Select File Type...", selected: true},
	            { label: "Excel", value: ".xlsx"},
	            { label: "Comma Separated Value (CSV)", value: ".csv", },
	        ]
	    }).placeAt(inputDiv);
	
	  on(uploadTypeChoice,"change",function(value){
		 
		  if(value === ".xlsx"){
			  showPointValueEmport('/upload.shtm');
		  }else if(value === ".csv"){
			  showPointValueEmport('/csvDataImport.shtm');
		  }
	  });
	
	//Finally show the div
	show('dataImportDiv');
	
	
});