/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Jared Wiltshire
 */

import virtualDataPointEditor from './virtualDataPointEditor.html';
import './virtualDataPointEditor.css';

class VirtualDataPointEditorController {
    static get $$ngIsClass() { return true; }
    static get $inject() { return ['maPoint']; }
    
    constructor(maPoint) {
        this.dataTypes = maPoint.dataTypes.filter(t => t.key !== 'IMAGE');
        this.changeTypes = { 
            'BINARY': [
                {key: 'ALTERNATE', translation: 'dsEdit.virtual.changeType.alternate'},
                {key: 'RANDOM', translation: 'dsEdit.virtual.changeType.random'},
                {key: 'NO_CHANGE', translation: 'dsEdit.virtual.changeType.noChange'},
            ],
            'MULTISTATE': [
                {key: 'INCREMENT_MULTISTATE', translation: 'dsEdit.virtual.changeType.increment'},
                {key: 'RANDOM', translation: 'dsEdit.virtual.changeType.random'},
                {key: 'NO_CHANGE', translation: 'dsEdit.virtual.changeType.noChange'},
            ],
            'NUMERIC': [
                {key: 'BROWNIAN', translation: 'dsEdit.virtual.changeType.brownian'},
                {key: 'INCREMENT', translation: 'dsEdit.virtual.changeType.increment'},
                {key: 'ANALOG_ATTRACTOR', translation: 'dsEdit.virtual.changeType.attractor'},
                {key: 'SINUSOIDAL', translation: 'dsEdit.virtual.changeType.sinusoidal'},
                {key: 'NO_CHANGE', translation: 'dsEdit.virtual.changeType.noChange'},
                {key: 'RANDOM', translation: 'dsEdit.virtual.changeType.random'} 
            ],
            'ALPHANUMERIC': [
                {key: 'NO_CHANGE', translation: 'dsEdit.virtual.changeType.noChange'}, 
            ]
        
        };
    }

    $onInit() {
        this.chipsChanged();
    }

    $onChanges(changes) {
        
    }

    attractionPointChanged(){
        this.dataPoint.pointLocator.attractionPointXid = this.attractionPoint.xid;
    }

    chipsChanged() {
        let values = this.dataPoint.pointLocator.values; 
        let lastValue = this.dataPoint.pointLocator.values[values.length - 1] = Number(values[values.length - 1]);

        if (values.length === 0) {
            this.pointValue = 1;
        } else {
            this.pointValue = lastValue + 1;
        }
    }
}

export default {
    template: virtualDataPointEditor,
    controller: VirtualDataPointEditorController,
    bindings: {
        dataPoint: '<point'
    }
};