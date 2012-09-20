/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
*/
mango.view.initJspView = function() {
    mango.view.setData = mango.view.jsp.setData;
    mango.view.setPoint = mango.view.jsp.setPoint;
    // Tell the long poll request that we're interested in jsp view data, and not max alarm.
    mango.longPoll.pollRequest.maxAlarm = false;
    
    mango.longPoll.addHandler("jspView", function(response) {
        if (response.jspViewStates)
            mango.view.setData(response.jspViewStates);
    });
};

mango.view.jsp = {};
mango.view.jsp.functions = {};
mango.view.jsp.setData = function(stateArr) {
    var node;
    for (var i=0; i<stateArr.length; i++) {
        var func = mango.view.jsp.functions["c"+ stateArr[i].id];
        if (func)
            func(stateArr[i].value, new Date(stateArr[i].time));
        else {
            node = $("c"+ stateArr[i].id);
            if (node)
                $set(node, stateArr[i].value);
        }
    }
}

mango.view.jsp.setPoint = function(xid, value, callback) {
    JspViewDwr.setJspViewPoint(mango.longPoll.pollSessionId, xid, value, function() {
        if (callback)
            callback();
    });
};
