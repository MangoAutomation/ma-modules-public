/**
 * Copyright (C) 2019  Infinite Automation Software. All rights reserved.
 */
package com.infiniteautomation.mango.rest.latest;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.infiniteautomation.mango.rest.latest.exception.AbstractRestException;
import com.infiniteautomation.mango.rest.latest.exception.BadRequestException;
import com.infiniteautomation.mango.rest.latest.exception.ServerErrorException;
import com.infiniteautomation.mango.rest.latest.model.MBusAddressScanRequest;
import com.infiniteautomation.mango.rest.latest.model.MBusDeviceScanResult;
import com.infiniteautomation.mango.rest.latest.model.MBusScanRequest;
import com.infiniteautomation.mango.rest.latest.model.MBusScanResult;
import com.infiniteautomation.mango.rest.latest.model.MBusSecondaryAddressScanRequest;
import com.infiniteautomation.mango.rest.latest.temporaryResource.MangoTaskTemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResource.TemporaryResourceStatus;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceManager;
import com.infiniteautomation.mango.rest.latest.temporaryResource.TemporaryResourceWebSocketHandler;
import com.infiniteautomation.mango.spring.service.DataSourceService;
import com.infiniteautomation.mango.util.exception.NotFoundException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.mbus.MangoMBusSerialConnection;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.vo.permission.PermissionHolder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.sf.mbus4j.Connection;
import net.sf.mbus4j.SerialPortConnection;
import net.sf.mbus4j.dataframes.Frame;
import net.sf.mbus4j.dataframes.UserDataResponse;
import net.sf.mbus4j.devices.GenericDevice;
import net.sf.mbus4j.master.MBusMaster;

/**
 * @author Terry Packer
 *
 */
@Api(value = "MBus data sources utilities")
@RestController()
@RequestMapping("/mbus-data-sources")
public class MBusDataSourceRestController {

    private static final String RESOURCE_TYPE_MBUS = "MBUS";

    private final DataSourceService service;
    private final TemporaryResourceManager<MBusScanResult, AbstractRestException> temporaryResourceManager;
    private final ExecutorService executor;

    @Autowired
    public MBusDataSourceRestController(DataSourceService service, TemporaryResourceWebSocketHandler handler, ExecutorService executor) {
        this.service = service;
        this.temporaryResourceManager = new MangoTaskTemporaryResourceManager<MBusScanResult>(service.getPermissionService(), handler);
        this.executor = executor;
    }

    @PreAuthorize("hasDataSourcePermission()")
    @ApiOperation(value = "Start an MBus scan")
    @RequestMapping(method = RequestMethod.POST, value= {"/scan"})
    public ResponseEntity<TemporaryResource<MBusScanResult, AbstractRestException>> operation(
            @ApiParam(value = "Resource expiry milliseconds", required = false,
            allowMultiple = false) @RequestParam(value = "expiry",
            required = false) Long expiry,
            @ApiParam(value = "Listener timeout milliseconds", required = false, allowMultiple = false)
            @RequestParam(value = "timeout", required = false) Long timeout,

            @RequestBody MBusScanRequest requestBody,

            @AuthenticationPrincipal User user,

            UriComponentsBuilder builder) {

        requestBody.ensureValid();

        if(requestBody.getDataSourceXid() != null)
            ensureNotRunning(requestBody.getDataSourceXid(), user);

        TemporaryResource<MBusScanResult, AbstractRestException> responseBody = temporaryResourceManager.newTemporaryResource(
                RESOURCE_TYPE_MBUS, null, expiry, timeout, (resource)-> {

                    //Start the discovery
                    MBusScan scan = new MBusScan(requestBody, Common.getUser(), resource);
                    Future<?> future = this.executor.submit(scan);

                    return r -> {
                        if(r.getStatus() == TemporaryResourceStatus.CANCELLED || r.getStatus() == TemporaryResourceStatus.TIMED_OUT) {
                            scan.cancelled = true;
                            future.cancel(true);
                        }

                    };
                });

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/mbus-data-sources/scan/{id}").buildAndExpand(responseBody.getId()).toUri());
        return new ResponseEntity<TemporaryResource<MBusScanResult, AbstractRestException>>(responseBody, headers, HttpStatus.CREATED);

    }

    class MBusScan implements Runnable {

        private volatile boolean cancelled;
        private final MBusScanRequest requestBody;
        private final PermissionHolder user;
        private final TemporaryResource<MBusScanResult, AbstractRestException> resource;

        public MBusScan(MBusScanRequest requestBody, PermissionHolder user,
                TemporaryResource<MBusScanResult, AbstractRestException> resource) {
            this.requestBody = requestBody;
            this.user = user;
            this.resource = resource;
        }

        @Override
        public void run() {
            MBusScanResult result = new MBusScanResult();
            Connection connection = requestBody.createConnection();
            CopyOnWriteArrayList<MBusDeviceScanResult> devices = new CopyOnWriteArrayList<>();
            try (final MBusMaster master = new MBusMaster();){
                if (connection instanceof SerialPortConnection) {
                    //replace with buggy jssc
                    SerialPortConnection spc = (SerialPortConnection) connection;
                    String owner = "Mango MBus Serial Test Tool by " + user.getPermissionHolderName();
                    master.setConnection(new MangoMBusSerialConnection(owner, spc.getPortName(), spc.getBitPerSecond(), 1000));
                } else {
                    master.setConnection(connection);
                }

                //Open the connection
                master.open();

                if(requestBody instanceof MBusAddressScanRequest) {
                    MBusAddressScanRequest asr = (MBusAddressScanRequest)requestBody;
                    int position = 0;
                    int last = asr.getLastAddress() & 0xFF;
                    int first = asr.getFirstAddress() & 0xFF;
                    int maximum = last - first;
                    resource.progressOrSuccess(result, position, maximum);
                    for (int address = first; address <= last; address++) {
                        Frame requestFrame = master.sendRequestUserData((byte)address);
                        if(requestFrame instanceof UserDataResponse) {
                            UserDataResponse udResp = (UserDataResponse) requestFrame;
                            GenericDevice d = new GenericDevice(udResp, requestFrame);
                            devices.add(new MBusDeviceScanResult(d));
                            result.setDevices(devices);
                        }
                        position++;
                        if(cancelled)
                            break;
                        resource.progressOrSuccess(result, position, maximum);
                    }
                }else if(requestBody instanceof MBusSecondaryAddressScanRequest){
                    //TODO No easy way to get feedback during request without using deprecated APIs
                    resource.progressOrSuccess(result, 0, 1);
                    MBusSecondaryAddressScanRequest sasd = (MBusSecondaryAddressScanRequest)requestBody;
                    Collection<GenericDevice> genericDevices = master.widcardSearch(
                            sasd.createMaskedId(),
                            sasd.createMaskedManufacturer(),
                            sasd.createMaskedVersion(),
                            sasd.createMaskedMedium());
                    for(GenericDevice c : genericDevices)
                        devices.add(new MBusDeviceScanResult(c));
                    result.setDevices(devices);
                    if(cancelled)
                        return;
                    resource.progressOrSuccess(result, 1, 1);
                }
            }catch(Exception e) {
                resource.error(new ServerErrorException(e));
            }
        }
    }

    @ApiOperation(value = "Get a list of current MBus scans", notes = "User can only get their own operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/scan")
    public List<TemporaryResource<MBusScanResult, AbstractRestException>> getOperations() {

        return this.temporaryResourceManager.list();
    }

    @ApiOperation(value = "Get the status of an MBus scan using its id", notes = "User can only get their own operations unless they are an admin")
    @RequestMapping(method = RequestMethod.GET, value="/scan/{id}")
    public TemporaryResource<MBusScanResult, AbstractRestException> getOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id) {

        return temporaryResourceManager.get(id);
    }

    @ApiOperation(value = "Cancel an MBus scan using its id",
            notes = "Only cancels if the operation is not already complete." +
                    "May also be used to remove a completed temporary resource by passing remove=true, otherwise the resource is removed when it expires." +
            "User can only cancel their own operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.POST, value="/scan/{id}")
    public TemporaryResource<MBusScanResult, AbstractRestException> cancelOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id,

            @ApiParam(value = "Remove the temporary resource", required = false, defaultValue = "false", allowMultiple = false)
            @RequestParam(required=false, defaultValue = "false") boolean remove) {

        TemporaryResource<MBusScanResult, AbstractRestException> resource = temporaryResourceManager.get(id);
        resource.cancel();
        if (remove) {
            resource.remove();
        }

        return resource;
    }

    @ApiOperation(value = "Remove an MBus scan using its id",
            notes = "Will only remove an operation if it is complete. " +
            "User can only remove their own operations unless they are an admin.")
    @RequestMapping(method = RequestMethod.DELETE, value="/scan/{id}")
    public TemporaryResource<MBusScanResult, AbstractRestException> removeOperation(
            @ApiParam(value = "Temporary resource id", required = true, allowMultiple = false)
            @PathVariable String id) {

        TemporaryResource<MBusScanResult, AbstractRestException> resource = temporaryResourceManager.get(id);
        resource.remove();

        return resource;
    }

    /**
     * Utility to ensure source is not running
     *
     * @param dataSourceXid
     */
    private void ensureNotRunning(String dataSourceXid, User user) throws PermissionException {
        try {
            DataSourceVO ds = service.get(dataSourceXid);
            if (Common.runtimeManager.isDataSourceRunning(ds.getId()))
                throw new BadRequestException(new TranslatableMessage("dsEdit.mbus.noSearchWhileDataSourceRunning"));
        }catch(NotFoundException e) {
            //Don't care its not running
        }
    }

}
