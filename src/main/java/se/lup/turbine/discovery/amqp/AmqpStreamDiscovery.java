/**
 * Copyright 2017 eriklupander
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.lup.turbine.discovery.amqp;

import com.netflix.turbine.discovery.StreamAction;
import com.netflix.turbine.discovery.StreamDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.net.URI;

public class AmqpStreamDiscovery implements StreamDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(AmqpStreamDiscovery.class);

    static AmqpStreamDiscovery create(String uriTemplate) {
        return new AmqpStreamDiscovery(uriTemplate);
    }

    final static String HOSTNAME = "{HOSTNAME}";

    private final String uriTemplate;

    private AmqpStreamDiscovery(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    @Override
    public Observable<StreamAction> getInstanceList() {
        return new AmqpInstanceDiscovery()
                .getInstanceEvents()
                .map(ei -> {
                    URI uri;
                    String uriString = uriTemplate.replace(HOSTNAME, ei.getHost() + ":" + ei.getPort());
                    System.out.println("Got disc: " + uriString);
                    try {
                        uri = new URI(uriString);
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid URI: " + uriString, e);
                    }
                    if (ei.getStatus() == AmqpInstance.Status.UP) {
                        System.out.println("StreamAction ADD");
                        return StreamAction.create(StreamAction.ActionType.ADD, uri);
                    } else {
                        System.out.println("StreamAction REMOVE");
                        return StreamAction.create(StreamAction.ActionType.REMOVE, uri);
                    }

                });
    }

}
