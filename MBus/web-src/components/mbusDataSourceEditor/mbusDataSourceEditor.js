/**
 * @copyright 2019 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

import componentTemplate from './mbusDataSourceEditor.html';

const $inject = Object.freeze(['maMbusDataSource', 'maDialogHelper']);

class mbusDataSourceEditorController {

    static get $inject() { return $inject; }
    static get $$ngIsClass() { return true; }

    constructor(maMbusDataSource, maDialogHelper) {
        this.maMbusDataSource = maMbusDataSource;
        this.maDialogHelper = maDialogHelper;
    }

    $onInit() {
        this.query = {
            limit: 10,
            page: 1
        };

        this.searchToolData = {
            addressingType: 'MBusTcpIpAddressScanRequest',
            primaryAddressing: {
                firstAddress: '00',
                lastAddress: 'FA'
            },
            secondaryAddress: {
                id: null,
                version: null,
                medium: null,
                manufacturer: null
            }
        };

        this.searchTool = new this.maMbusDataSource();

        this.wsConnection = this.maMbusDataSource.subscribe((event, item) => {
            if (item.status === 'RUNNING') {
                
                this.searching = true;
                this.searchProgress = item.progress === 0 ? item.progress + 1 : item.progress; 
                this.maDialogHelper.toastOptions(
                    {textTr: ['dsEdit.mbus.searching']}
                );
                this.devices = item.result.devices;

            } else if (item.status === 'SUCCESS' && item.result) {
                this.searching = false;
                this.searchProgress = item.progress;
                this.maDialogHelper.toastOptions(
                    {textTr: ['dsEdit.mbus.searching']}
                );

                this.maDialogHelper.toastOptions(
                    {textTr: ['dsEdit.mbus.searchingSuccess']}
                );

                this.devices = item.result.devices;
            } else if (item.status === 'ERROR') {
                this.searching = false;
                this.searchProgress = null;
                this.maDialogHelper.toastOptions(
                    {
                        textTr: ['dsEdit.mbus.searchError', item.error.localizedMessage],
                        classes: 'md-warn'
                    }
                );
            } 
        });
    }

    $onChanges(changes) {
        if (changes.dataSource.previousValue.xid) {
            this.getScans().then(scans => {
                scans = scans.filter(scan => scan.status === 'RUNNING');
                scans.forEach(scan => {
                    this.cancel(scan.id);
                });
                this.searchProgress = null;
                this.searching = false;
                this.devices = null;
            });
        } else {
            this.getScans().then(scans => {
                scans = scans.filter(scan => scan.status === 'RUNNING');
                if (scans && scans.length > 0) {
                    this.searchProgress = scans[0].progress === 0 ? scans[0].progress + 1 : scans[0].progress;
                    this.searching = true;
                }
            });
        }
    }

    getScans() {
        return this.maMbusDataSource.getScans(response => {
            return response;
        });
    }

    search() {
        this.devices = null;
        this.searchProgress = null;

        let data = {
            dataSourceXid: this.dataSource.xid,
            bitsPerSecond: this.dataSource.connection.bitPerSecond,
            responseTimeoutOffset: this.dataSource.connection.responseTimeoutOffset,
            host: this.dataSource.connection.host,
            port: this.dataSource.connection.port,
            type: this.searchToolData.addressingType
        };

        if (data.type === 'MBusTcpIpAddressScanRequest') {
            data.firstAddress = parseInt(this.searchToolData.primaryAddressing.firstAddress, 16);
            data.lastAddress = parseInt(this.searchToolData.primaryAddressing.lastAddress, 16);
        } else {
            data.id = this.searchToolData.secondaryAddress.id;
            data.version = this.searchToolData.secondaryAddress.version;
            data.medium = this.searchToolData.secondaryAddress.medium;
            data.manufacturer = this.searchToolData.secondaryAddress.manufacturer;
        }

        this.getScans().then(scans => {
            scans = scans.filter(scan => scan.status === 'RUNNING');
            if (scans && scans.length > 0) {
                this.maDialogHelper.confirm(event, ['dsEdit.mbus.confirmSearchCancel']).then(() => {
                    scans.forEach(scan => {
                        this.cancel(scan.id);
                    });
                    this.searching = true;
                    this.startNewSearch = true;
                    this.searchTool.scan(data).catch(error => {
                        this.searching = false;
                        this.searchToolValidationMessages = error.data.result.messages;
                    });
                });
            } else {
                this.searchTool.scan(data).catch(error => {
                    this.searchToolValidationMessages = error.data.result.messages;
                });
            }

        });
    }

    cancelScan() {
        this.getScans().then(scans => {
            scans = scans.filter(scan => scan.status === 'RUNNING');
            scans.forEach(scan => {
                this.cancel(scan.id);
            });
        });
    }

    cancel(id) {
        this.searchTool.cancel(id).then(response => {
            if(this.startNewSearch) {
                this.startNewSearch = false;
            } else {
                this.searching = false;
            }
            this.maDialogHelper.toastOptions(
                {textTr: ['dsEdit.mbus.seachStopped']}
            );
        });
    }

}

export default {
    bindings: {
        dataSource: '<source'
    },
    require: {
        dsEditor: '^maDataSourceEditor'
    },
    controller: mbusDataSourceEditorController,
    template: componentTemplate
};