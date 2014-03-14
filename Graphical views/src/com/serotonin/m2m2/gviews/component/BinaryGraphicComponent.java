/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.gviews.component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.view.ImplDefinition;

/**
 * @author Matthew Lohbihler
 */
public class BinaryGraphicComponent extends ImageSetComponent {
    public static ImplDefinition DEFINITION = new ImplDefinition("binaryGraphic", "BINARY_GRAPHIC",
            "graphic.binaryGraphic", new int[] { DataTypes.BINARY });

    @JsonProperty(alias = "zeroImageIndex")
    private int zeroImage;
    @JsonProperty(alias = "oneImageIndex")
    private int oneImage;

    public int getZeroImage() {
        return zeroImage;
    }

    public void setZeroImage(int zeroImage) {
        this.zeroImage = zeroImage;
    }

    public int getOneImage() {
        return oneImage;
    }

    public void setOneImage(int oneImage) {
        this.oneImage = oneImage;
    }

    @Override
    public ImplDefinition definition() {
        return DEFINITION;
    }

    @Override
    public String getImage(PointValueTime pointValue) {
        boolean bvalue = false;
        if (pointValue != null && pointValue.getValue() instanceof BinaryValue)
            bvalue = pointValue.getBooleanValue();
        return imageSet.getImageFilename(bvalue ? oneImage : zeroImage);
    }

    @Override
    public void validate(ProcessResult response) {
        super.validate(response);

        if (zeroImage < 0)
            response.addMessage("zeroImageIndex", new TranslatableMessage("validate.cannotBeNegative"));
        if (oneImage < 0)
            response.addMessage("oneImageIndex", new TranslatableMessage("validate.cannotBeNegative"));

        if (imageSet != null) {
            if (zeroImage >= imageSet.getImageCount())
                response.addMessage("zeroImageIndex", new TranslatableMessage("emport.error.component.imageIndex",
                        zeroImage, imageSet.getId(), imageSet.getImageCount() - 1));
            if (oneImage >= imageSet.getImageCount())
                response.addMessage("oneImageIndex", new TranslatableMessage("emport.error.component.imageIndex",
                        oneImage, imageSet.getId(), imageSet.getImageCount() - 1));
        }
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(zeroImage);
        out.writeInt(oneImage);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            zeroImage = in.readInt();
            oneImage = in.readInt();
        }
    }
}
