/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
*/
sstGraphics = {};
sstGraphics.Dial = {};
sstGraphics.Dial.setValue = function(viewComponentId, value) {
    var g = new jsGraphics("c"+ viewComponentId +"Content");
    
    g.clear();
    
    var xCenter = 76;
    var yCenter = 77;
    
    var xa = new Array(-3, -1,  1,3,1,-1);
    var ya = new Array( 0,-52,-52,0,3, 3);
    var angle = (value * 2 - 1) * 2.1;
    
    mango.view.graphic.transform(xa, ya, 1, 1, xCenter, yCenter, angle);
    
    g.setColor("#A00000");
    g.fillPolygon(xa, ya);
    
    g.setColor("#202020");
    g.drawPolygon(xa, ya);
    g.drawLine(xCenter, yCenter, xCenter, yCenter);
    g.paint();
};

sstGraphics.SmallDial = {};
sstGraphics.SmallDial.setValue = function(viewComponentId, value) {
    var g = new jsGraphics("c"+ viewComponentId +"Content");
    
    g.clear();
    
    var xCenter = 38;
    var yCenter = 38;
    
    var xa = new Array(-2,  0,  0,2,1,-1);
    var ya = new Array( 0,-26,-26,0,2, 2);
    var angle = (value * 2 - 1) * 2.1;
    
    mango.view.graphic.transform(xa, ya, 1, 1, xCenter, yCenter, angle);
    
    g.setColor("#A00000");
    g.fillPolygon(xa, ya);
    
    g.setColor("#202020");
    g.drawPolygon(xa, ya);
    g.drawLine(xCenter, yCenter, xCenter, yCenter);
    g.paint();
};

sstGraphics.VerticalLevel = {};
sstGraphics.VerticalLevel.setValue = function(viewComponentId, value) {
    var g = new jsGraphics("c"+ viewComponentId +"Content");
    
    g.clear();
    
    var maxbars = 24;
    var bars = parseInt(maxbars * value + 0.5);
    var i, max;
    
    // Green
    max = bars > 18 ? 18 : bars;
    g.setColor("#008000");
    for (i=0; i<max; i++)
        g.fillRect(2, 94 - i*4, 16, 3);
    
    // Yellow
    max = bars > 22 ? 22 : bars;
    g.setColor("#E5E500");
    for (i=18; i<max; i++)
        g.fillRect(2, 94 - i*4, 16, 3);
    
    // Red
    max = bars > 24 ? 24 : bars;
    g.setColor("#E50000");
    for (i=22; i<max; i++)
        g.fillRect(2, 94 - i*4, 16, 3);
    
    g.paint();
};

sstGraphics.HorizontalLevel = {};
sstGraphics.HorizontalLevel.setValue = function(viewComponentId, value) {
    var g = new jsGraphics("c"+ viewComponentId +"Content");
    
    g.clear();
    
    var maxbars = 24;
    var bars = parseInt(maxbars * value + 0.5);
    var i, max;
    
    // Green
    max = bars > 18 ? 18 : bars;
    g.setColor("#008000");
    for (i=0; i<max; i++)
        g.fillRect(2 + i*4, 2, 3, 16);
    
    // Yellow
    max = bars > 22 ? 22 : bars;
    g.setColor("#E5E500");
    for (i=18; i<max; i++)
        g.fillRect(2 + i*4, 2, 3, 16);
    
    // Red
    max = bars > 24 ? 24 : bars;
    g.setColor("#E50000");
    for (i=22; i<max; i++)
        g.fillRect(2 + i*4, 2, 3, 16);
    
    g.paint();
};