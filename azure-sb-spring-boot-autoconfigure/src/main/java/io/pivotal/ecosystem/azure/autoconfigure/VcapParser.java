/**
 Copyright (C) 2017-Present Pivotal Software, Inc. All rights reserved.

 This program and the accompanying materials are made available under
 the terms of the under the Apache License, Version 2.0 (the "License”);
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package io.pivotal.ecosystem.azure.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

@Service
@Configuration
/**
 * Parses VCAP_SERVICES environment variable and sets corresponding property values.
 * 
 * Note that this class gets invoked before Spring creates the logging subsystem, so 
 * we just use System.out.println instead.  
 */
public class VcapParser implements EnvironmentPostProcessor
{
	public static final String VCAP_SERVICES = "VCAP_SERVICES";

	private static final boolean LOGFLAG = true;
	
	private static final String AZURE = "azure-";
	private static final String CREDENTIALS = "credentials";
	private static final String LABEL = "label";
	private static final String NAME = "name";
	private static final String PLAN = "plan";
	private static final String PROVIDER = "provider";
	private static final String SYSLOG_DRAIN_URL = "syslog_drain_url";
	private static final String TAGS = "tags";
	private static final String VOLUME_MOUNTS = "volume_mounts";

	private VcapResult result;

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment confEnv, SpringApplication app)
	{
		log("VcapParser.postProcessEnvironment: Start");
		Map<String, Object> environment = confEnv.getSystemEnvironment();
		String vcapServices = (String) environment.get(VcapParser.VCAP_SERVICES);
		result = parse(vcapServices);
		result.setConfEnv(confEnv);
		result.populateProperties();
		log("VcapParser.postProcessEnvironment: End");
	}

	VcapResult parse(String vcapServices)
	{
		VcapResult result = new VcapResult();

		List<VcapPojo> results = new ArrayList<VcapPojo>();

		log("VcapParser.parse:  vcapServices = " + vcapServices);
		if (vcapServices != null)
		{
			try
			{
				JSONObject json = new JSONObject(vcapServices);
				JSONArray names = json.names();
				for (int i = 0; i < names.length(); i++)
				{
					String name = (String) names.get(i);
					if (name.startsWith(AZURE))
					{
						JSONArray azureService = json.getJSONArray(name);
						int numElements = azureService.length();
						for (int index = 0; index < numElements; index++)
						{
							VcapPojo pojo = parseService(name, azureService, vcapServices, index);
							results.add(pojo);
						}
					}
				}
			} catch (JSONException e)
			{
				System.err.println("Error parsing " + vcapServices);
				e.printStackTrace(System.err);
			}
		}

		result.setPojos(results.toArray(new VcapPojo[results.size()]));
		return result;
	}

	private VcapPojo parseService(String serviceBrokerName, JSONArray azureService, String vCapServices, int index)
	{
		VcapPojo result = new VcapPojo();
		result.setServiceBrokerName(serviceBrokerName);

		try
		{
			JSONObject service = azureService.getJSONObject(index);
			result.setLabel(parseString(service, LABEL));
			result.setProvider(parseString(service, PROVIDER));
			result.setServiceInstanceName(parseString(service, NAME));
			result.setServicePlan(parseString(service, PLAN));
			result.setSyslogDrainUrl(parseString(service, SYSLOG_DRAIN_URL));
			result.setTags(parseStringArray(service.getJSONArray(TAGS)));
			result.setVolumeMounts(parseStringArray(service.getJSONArray(VOLUME_MOUNTS)));

			JSONObject credObject = service.getJSONObject(CREDENTIALS);
			if (credObject != null)
			{
				parseMap(credObject, result.getCredentials());
			}
		} catch (JSONException e)
		{
			System.err.println("Found " + serviceBrokerName + ", but missing " + CREDENTIALS + " : " + vCapServices);
			e.printStackTrace(System.err);
		}
		return result;
	}

	private String[] parseStringArray(JSONArray strings)
	{
		List<String> results = new ArrayList<String>();

		for (int i = 0; i < strings.length(); i++)
		{
			try
			{
				results.add((String) strings.get(i));
			} catch (JSONException e)
			{
				System.err.println("Error parsing " + strings);
				e.printStackTrace(System.err);
			}
		}

		return results.toArray(new String[results.size()]);
	}

	private void parseMap(JSONObject mapObject, Map<String, String> target)
	{
		JSONArray keys = mapObject.names();
		for (int i = 0; i < keys.length(); i++)
		{
			try
			{
				String key = (String) keys.get(i);
				String value = mapObject.getString(key);
				target.put(key, value);
			} catch (JSONException e)
			{
				System.err.println("Error parsing " + mapObject);
				e.printStackTrace(System.err);
			}
		}
	}

	private String parseString(JSONObject service, String key)
	{
		String result = null;

		try
		{
			if (!service.isNull(key))
			{
				result = service.getString(key);
			}
		} catch (JSONException e)
		{
			System.err.println("Error parsing " + service);
			e.printStackTrace(System.err);
		}

		return result;
	}

	private void log(String msg)
	{
		if (LOGFLAG)
		{
			System.out.println(msg);
		}
	}
}
