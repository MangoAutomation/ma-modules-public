/*
    Copyright (C) 2015 Infinite Automation Systems Inc. All rights reserved.
    @author Terry Packer
*/
    

function generateJson(){
    
    hideContextualMessages("migrationMessages");
    hide('jsonOutput');
    
    M2MReportImportDwr.generateJson(
            $get("driverClassname"),
            $get("connectionUrl"),
            $get("username"),
            $get("password"),
            function(response){
                hideGenericMessages("migrationMessages"); //Hide the old messages if any
                if(response.hasMessages){
                    showDwrMessages(response.messages, $("migrationMessages"));
                }else{
                	$set('jsonOutput',response.data.reports);
                	show('jsonOutput');
                }
            });
    
}


function migrate(){

    hideContextualMessages("migrationMessages");
    hide('jsonOutput');
    
    M2MReportImportDwr.migrate(
            $get("driverClassname"),
            $get("connectionUrl"),
            $get("username"),
            $get("password"),
            function(response){
                hideGenericMessages("migrationMessages"); //Hide the old messages if any
                if(response.hasMessages){
                    showDwrMessages(response.messages, $("migrationMessages"));
                }else{
                	console.log(response.data.reports);
                }
            });
    
}
    
 