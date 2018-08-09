/**
 * @copyright 2018 {@link http://infiniteautomation.com|Infinite Automation Systems, Inc.} All rights reserved.
 * @author Luis Güette
 */

const path = require('path');
const moduleConfig = require('@infinite-automation/mango-module-tools');
const CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = moduleConfig.then(config => {
    config.optimization = {
        splitChunks: false
    };
    
    config.output.path = path.resolve('web', 'angular');
    config.output.publicPath = config.output.publicPath + 'angular/';
    
    config.plugins.push(new CleanWebpackPlugin(['web/angular'], {
        root: __dirname
    }));

    return config;
});
