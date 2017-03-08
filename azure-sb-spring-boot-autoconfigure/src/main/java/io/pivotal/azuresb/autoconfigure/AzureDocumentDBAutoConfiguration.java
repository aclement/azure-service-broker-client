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

package io.pivotal.azuresb.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;

/**
 * 
 * @author pbopardikar This class instantiates the DocumentClient bean that will
 *         be injected in the azure-sb-documentdb-client project
 */
@Configuration
@ConditionalOnMissingBean(DocumentClient.class)
@EnableConfigurationProperties(AzureDocumentDBProperties.class)
public class AzureDocumentDBAutoConfiguration {

	private final AzureDocumentDBProperties properties;

	@Autowired
	public AzureDocumentDBAutoConfiguration(AzureDocumentDBProperties properties) {
		this.properties = properties;
	}

	@Bean
	public DocumentClient documentClient() {
		String hostname = properties.getHostEndpoint();
		String masterkey = properties.getMasterKey();
		return new DocumentClient(hostname, masterkey,
				ConnectionPolicy.GetDefault(), ConsistencyLevel.Session);
	}

}
