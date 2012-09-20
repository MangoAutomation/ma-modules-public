/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import com.serotonin.json.spi.JsonEntity;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.view.ImplDefinition;

/**
 * @author Matthew Lohbihler
 */
@JsonEntity
public class WirelessTempHumSensor extends CompoundComponent {
    private static final long serialVersionUID = -1;
    public static ImplDefinition DEFINITION = new ImplDefinition("wirelessTempHumSensor", "WIRELESS_TEMP_HUM_SENSOR",
            "graphic.wirelessTempHumSensor", null);

    public static final String TEXT = "text";
    public static final String BATTERY = "battery";
    public static final String SIGNAL = "signal";
    public static final String TEMPERATURE = "temperature";
    public static final String HUMIDITY = "humidity";

    public WirelessTempHumSensor() {
        initialize();
    }

    @Override
    protected void initialize() {
        HtmlComponent text = new HtmlComponent();
        text.setContent("Shameless promotion. Contact <a href='http://mango.serotoninsoftware.com/contact.jsp' target='_blank'>Serotonin Software</a> to order custom components.");
        text.setLocation(0, 40);

        String script = "if (value > 0.7) return \"<img src='graphics/Battery/batt_full.png'/>\"; ";
        script += "if (value > 0.4) return \"<img src='graphics/Battery/batt_med.png'/>\";";
        script += "if (value > 0.2) return \"<img src='graphics/Battery/batt_low.png'/>\";";
        script += "return \"<img src='graphics/Battery/batt_dead.png'/>\";";
        ScriptComponent battery = new ScriptComponent();
        battery.setScript(script);
        battery.setLocation(179, 0);
        battery.setDisplayControls(false);

        script = "if (value > 0.7) return \"<img src='graphics/Signal/good.png'/>\"; ";
        script += "if (value > 0.4) return \"<img src='graphics/Signal/ok.png'/>\";";
        script += "if (value > 0.2) return \"<img src='graphics/Signal/bad.png'/>\";";
        script += "return \"<img src='graphics/Signal/none.png'/>\";";
        ScriptComponent signal = new ScriptComponent();
        signal.setScript(script);
        signal.setLocation(179, 10);
        signal.setDisplayControls(false);

        SimplePointComponent temperature = new SimplePointComponent();
        temperature.setLocation(16, 0);
        temperature.setDisplayControls(true);
        temperature.setSettableOverride(true);

        SimplePointComponent humidity = new SimplePointComponent();
        humidity.setLocation(16, 20);
        humidity.setDisplayControls(true);

        addChild(TEXT, "graphic.wirelessTempHumSensor.staticText", text);
        addChild(BATTERY, "graphic.wirelessTempHumSensor.battery", battery, new int[] { DataTypes.NUMERIC });
        addChild(SIGNAL, "graphic.wirelessTempHumSensor.signal", signal, new int[] { DataTypes.NUMERIC });
        addChild(TEMPERATURE, "graphic.wirelessTempHumSensor.temperature", temperature, new int[] { DataTypes.NUMERIC });
        addChild(HUMIDITY, "graphic.wirelessTempHumSensor.humidity", humidity, new int[] { DataTypes.NUMERIC });
    }

    @Override
    public boolean hasInfo() {
        return true;
    }

    @Override
    public ImplDefinition definition() {
        return DEFINITION;
    }

    @Override
    public String getStaticContent() {
        return "<img src='images/logo.gif'/>";
    }

    @Override
    public boolean isDisplayImageChart() {
        return true;
    }

    @Override
    public String getImageChartData(Translations translations) {
        return generateImageChartData(translations, 1000 * 60 * 60, TEMPERATURE, HUMIDITY);
    }
}
