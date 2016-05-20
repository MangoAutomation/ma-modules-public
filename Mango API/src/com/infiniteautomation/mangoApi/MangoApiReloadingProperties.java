/**
 * Copyright (C) 2014 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.infiniteautomation.mangoApi;

/**
 * 
 * A modified version of the Reloading properties that uses 1 Overriding File Resource
 * 
 * 
 * @author Terry Packer
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.m2m2.web.OverridingFileResource;
import com.serotonin.provider.Providers;
import com.serotonin.provider.TimerProvider;
import com.serotonin.timer.AbstractTimer;
import com.serotonin.util.properties.AbstractProperties;
import com.serotonin.util.properties.PropertyChangeCallback;
import com.serotonin.util.properties.ReloadCallback;

/**
 * @author Matthew Lohbihler
 */
public class MangoApiReloadingProperties extends AbstractProperties {
	private final Log LOG = LogFactory.getLog(this.getClass());

	private Object propertiesLock = new Object(); //For locking them while being reloaded
	private Properties properties = new Properties();
	private OverridingFileResource sourceFile;
	private long lastTimestamp = 0;
	private long lastRecheck = 0;
	private long recheckDeadbandPeriod = 5000; // 5 seconds
	private AbstractTimer timer;
	private ReloadCallback reloadCallback;
	private final Map<String, List<PropertyChangeCallback>> propertyChangeCallbacks = new HashMap<String, List<PropertyChangeCallback>>();

	private final Map<String, String> defaultValues = new HashMap<String, String>();

	public MangoApiReloadingProperties(OverridingFileResource propertiesResource) {
		super("mangoApiHeaders");
		this.timer = Providers.get(TimerProvider.class).getTimer();
		sourceFile = propertiesResource;
		checkForReload(false);
	}



	public void setDefaultValue(String key, String value) {
		defaultValues.put(key, value);
	}

	public long getRecheckDeadbandPeriod() {
		return recheckDeadbandPeriod;
	}

	public void setRecheckDeadbandPeriod(long recheckDeadbandPeriod) {
		this.recheckDeadbandPeriod = recheckDeadbandPeriod;
	}

	public ReloadCallback getReloadCallback() {
		return reloadCallback;
	}

	public void setReloadCallback(ReloadCallback reloadCallback) {
		this.reloadCallback = reloadCallback;
	}

	public void addPropertyChangeCallback(String key,
			PropertyChangeCallback callback) {
		List<PropertyChangeCallback> list = propertyChangeCallbacks.get(key);
		if (list == null) {
			list = new ArrayList<PropertyChangeCallback>();
			propertyChangeCallbacks.put(key, list);
		}
		list.add(callback);
	}

	public void removePropertyChangeCallback(String key,
			PropertyChangeCallback callback) {
		List<PropertyChangeCallback> list = propertyChangeCallbacks.get(key);
		if (list != null) {
			list.remove(callback);
			if (list.isEmpty())
				propertyChangeCallbacks.remove(key);
		}
	}

	@Override
	protected String getStringImpl(String key) {
		checkForReload(false);

		String value = properties.getProperty(key);
		if (value == null)
			value = defaultValues.get(key);

		return value;
	}


	/**
	 * 
	 * @param forceReload
	 */
	public void checkForReload(boolean forceReload) {
		if(!forceReload){
			if (lastRecheck + recheckDeadbandPeriod > timer.currentTimeMillis()) {
				if (LOG.isDebugEnabled())
					LOG.debug("(" + getDescription()
							+ ") In do not check period. Not rechecking");
				// Still in the do not check period.
				return;
			}
		}
		lastRecheck = timer.currentTimeMillis();

		if (LOG.isDebugEnabled())
			LOG.debug("(" + getDescription() + ") Checking for updated files");

		if (sourceFile == null)
			return;

		// Determine the latest time stamp of all of the source files.
		long latestTimestamp = -1;
		if (!sourceFile.exists())
			LOG.error("(" + getDescription() + ") Property file "
					+ sourceFile + " does not exist");
		else {
			if (latestTimestamp < sourceFile.lastModified())
				latestTimestamp = sourceFile.lastModified();
		}

		// Check if we need to reload.
		if (latestTimestamp > lastTimestamp) {
			if (LOG.isInfoEnabled())
				LOG.info("(" + getDescription() + ") Found updated file at "
						+ sourceFile.getName()
						+ ". Reloading properties");

			// Time to reload. Create the new backing properties file.
			Properties newProps = new Properties();

			InputStream in = null;
			try {
				// Load the properties in.
				in = sourceFile.getInputStream();
				newProps.load(in);
			} catch (IOException e) {
				LOG.error("(" + getDescription()
						+ ") Exception while loading property file "
						+ sourceFile, e);
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
					// ignore
				}
			}


			if (reloadCallback != null)
				reloadCallback.propertiesReload(properties, newProps);

			for (Map.Entry<String, List<PropertyChangeCallback>> entry : propertyChangeCallbacks
					.entrySet()) {
				String oldProp = (String) properties.get(entry.getKey());
				String newProp = (String) newProps.get(entry.getKey());

				if (!ObjectUtils.equals(oldProp, newProp)) {
					List<PropertyChangeCallback> cbs = entry.getValue();
					if (cbs != null) {
						for (PropertyChangeCallback cb : cbs)
							cb.propertyChanged(newProp);
					}
				}
			}

			// Set these properties as the actual backing object.
			synchronized(propertiesLock){
				properties = newProps;
			}

			lastTimestamp = latestTimestamp;
		}
	}

	/**
	 * Return a copy of the backing properties
	 * @return
	 */
	public Properties getPropertiesCopy(){
		synchronized(this.propertiesLock){
			Properties copy = new Properties();
			Iterator<Object> it = this.properties.keySet().iterator();
			while(it.hasNext()){
				String key = (String)it.next();
				copy.put(key, this.properties.getProperty(key));
			}
			return copy;
		}
		
	}
}
