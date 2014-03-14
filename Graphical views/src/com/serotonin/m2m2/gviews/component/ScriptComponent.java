/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.script.ScriptExecutor;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.web.dwr.BaseDwr;
import com.serotonin.m2m2.web.taglib.Functions;
import com.serotonin.util.SerializationHelper;

/**
 * @author Matthew Lohbihler
 */
public class ScriptComponent extends PointComponent {
    public static ImplDefinition DEFINITION = new ImplDefinition("script", "SCRIPT", "graphic.script", new int[] {
            DataTypes.BINARY, DataTypes.MULTISTATE, DataTypes.NUMERIC, DataTypes.ALPHANUMERIC });

    private static final String SCRIPT_PREFIX = "function __scriptRenderer__() {";
    private static final String SCRIPT_SUFFIX = "\r\n}\r\n__scriptRenderer__();";

    @JsonProperty
    private String script;

    @Override
    public String snippetName() {
        return "scriptContent";
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Override
    public void addDataToModel(Map<String, Object> model, PointValueTime value) {
        String result;

        if (value == null)
            result = "--";
        else {
            // Create the script engine.
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("js");

            DataPointVO point = tgetDataPoint();

            // Put the values into the engine scope.
            engine.put("value", value.getValue().getObjectValue());
            engine.put("htmlText", Functions.getHtmlText(point, value));
            engine.put("renderedText", Functions.getRenderedText(point, value));
            engine.put("time", value.getTime());
            engine.put("pointComponent", this);
            engine.put("point", point);
            // Copy properties from the model into the engine scope.
            engine.put(BaseDwr.MODEL_ATTR_EVENTS, model.get(BaseDwr.MODEL_ATTR_EVENTS));
            engine.put(BaseDwr.MODEL_ATTR_HAS_UNACKED_EVENT, model.get(BaseDwr.MODEL_ATTR_HAS_UNACKED_EVENT));
            engine.put(BaseDwr.MODEL_ATTR_TRANSLATIONS, model.get(BaseDwr.MODEL_ATTR_TRANSLATIONS));

            // Create the script.
            String evalScript = SCRIPT_PREFIX + script + SCRIPT_SUFFIX;

            // Execute.
            try {
                Object o = engine.eval(evalScript);
                if (o == null)
                    result = null;
                else
                    result = o.toString();
            }
            catch (ScriptException e) {
                e = ScriptExecutor.prettyScriptMessage(e);
                result = e.getMessage();
            }
        }

        model.put("scriptContent", result);
    }

    @Override
    public ImplDefinition definition() {
        return DEFINITION;
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);

        SerializationHelper.writeSafeUTF(out, script);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1)
            script = SerializationHelper.readSafeUTF(in);
    }
}
