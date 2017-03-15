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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

@ConfigurationProperties("azure.redis")
public class AzureRedisProperties
{
	private static final Logger LOG = LoggerFactory.getLogger(AzureRedisProperties.class);

	private static final String AZURE_REDISCACHE = "azure-rediscache";
	private static final String HOST_NAME = "hostname";
	private static final String SSL_PORT = "sslPort";
	private static final String PRIMARY_KEY = "primaryKey";

	@Autowired
	private VcapParser parser;

	@Autowired
	private Environment environment;
	
	private String hostname = "TBD";
	private String sslPort = "TBD";
	private String primaryKey = "TBD";

	@PostConstruct
	private void populateProperties()
	{
		String vcapServices = environment.getProperty(VcapParser.VCAP_SERVICES);
		VcapPojo[] pojos = parser.parse(vcapServices);
		for (int i=0; i<pojos.length; i++)
		{
			VcapPojo pojo = pojos[i];
			if (AZURE_REDISCACHE.equals(pojo.getServiceBrokerName()))
			{
				LOG.debug("Found the redis cache key");
				hostname = pojo.getCredentials().get(HOST_NAME);
				sslPort = pojo.getCredentials().get(SSL_PORT);
				primaryKey = pojo.getCredentials().get(PRIMARY_KEY);
			}
		}
	}

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getSslPort()
	{
		return sslPort;
	}

	public void setSslPort(String sslPort)
	{
		this.sslPort = sslPort;
	}

	public String getPrimaryKey()
	{
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey)
	{
		this.primaryKey = primaryKey;
	}

	@Override
	public String toString()
	{
		return "AzureRedisProperties [parser=" + parser + ", environment=" + environment + ", hostname=" + hostname + ", sslPort="
				+ sslPort + ", primaryKey=" + primaryKey + "]";
	}

	
}