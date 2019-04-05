/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.v2.model;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.m2m2.i18n.ProcessResult;

import net.sf.mbus4j.MBusUtils;
import net.sf.mbus4j.dataframes.MBusMedium;

/**
 * @author Terry Packer
 *
 */
public class MBusSecondaryAddressSearchRequest extends MBusScanRequest {
    
    private Integer id;
    private Byte version;
    private String medium;
    private String manufacturer;
    
    @Override
    public void validate(ProcessResult response) {
        if(StringUtils.isNotEmpty(medium)){
           try { 
               MBusMedium.fromLabel(medium);
           }catch(Exception e) {
               response.addContextualMessage("medium", "validate.invalidValue");
           }
        }
        if(StringUtils.isNotEmpty(manufacturer)){
            try { 
                MBusUtils.man2Short(manufacturer);
            }catch(Exception e) {
                response.addContextualMessage("manufacturer", "validate.invalidValue");
            }
         }
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Byte getVersion() {
        return version;
    }

    public void setVersion(Byte version) {
        this.version = version;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int createMaskedId() {
        if(id == null)
            return (int)0xFFFFFFFF;
        else
            return id;
    }
    
    public byte createMaskedVersion() {
        if(version == null)
            return (byte)0xFF;
        else
            return version;
    }
    
    public byte createMaskedMedium() {
        if ((medium == null) || (medium.length() == 0)) {
            return (byte)0xFF;
        } else {
             return (byte)MBusMedium.fromLabel(medium).getId();
        }
    }
    
    public short createMaskedManufacturer() {
        if ((manufacturer == null) || (manufacturer.length() == 0)) {
            return (short)0xFFFF;
        } else {
             return MBusUtils.man2Short(manufacturer);
        }
    }

}
