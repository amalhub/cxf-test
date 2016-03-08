/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.wsdl11.WSDLServiceFactory;

import java.net.URL;

/**
* Reference: https://github.com/wso2/carbon-mediation/blob/22fb8c92f2d7dbb90bc3faadf43b26308d9fda8e/components/inbound-endpoints/org.wso2.carbon.inbound.endpoint.ext.wsrm/src/main/java/org/wso2/carbon/inbound/endpoint/ext/wsrm/InboundRMHttpListener.java
*/
public class TestServer {
    protected TestServer() throws Exception{
        URL resource = getClass().getResource("/HelloWorld2.wsdl");

        //Generating the bus configurations
        Bus bus;
        bus = BusFactory.newInstance().createBus();
        SoapBindingFactory bindingFactory = new SoapBindingFactory();
        bindingFactory.setBus(bus);
        bus.getExtension(BindingFactoryManager.class)
                .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);
        bus.getExtension(BindingFactoryManager.class)
                .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/http", bindingFactory);


        //Extracting the wsdl content
        WSDLServiceFactory factory = new WSDLServiceFactory(bus, resource.toString(), null);
        Service service = factory.create();

        //Create a dummy class to act as the service class of the CXF endpoint
        InboundRMServiceImpl serviceImpl = new InboundRMServiceImpl();
        ServerFactoryBean serverFactory = new ServerFactoryBean();
        serverFactory.setBus(bus);

        //Add an interceptor to remove the unnecessary interceptors from the CXF Bus
        serverFactory.getInInterceptors().add(new RequestInterceptor());
        //Add an interceptor to alter the outgoing messages
        serverFactory.getOutInterceptors().add(new ResponseInterceptor());
        //Add an invoker to extract the message and inject to ODE
        InboundRMHttpInvoker invoker = new InboundRMHttpInvoker(serviceImpl);
        serverFactory.setInvoker(invoker);

        serverFactory.setServiceBean(serviceImpl);
        serverFactory.setDataBinding(service.getDataBinding());
        serverFactory.setServiceName(service.getName());
        serverFactory.setBindingId(service.getServiceInfos().get(0).getBindings().iterator().next().getBindingId());
        serverFactory.setWsdlLocation(resource.getPath());

        serverFactory.setEndpointName(service.getServiceInfos().iterator().next().getEndpoints().iterator().next().getName());


        String address = service.getServiceInfos().get(0).getEndpoints().iterator().next().getAddress();
        //set the host and port to listen to
        serverFactory.setAddress(address);
        Server server = serverFactory.create();


    }
    public static void main(String[] args) throws Exception{
        new TestServer();
        System.out.println("Server ready...");

        Thread.sleep(5 * 60 * 1000);
        System.out.println("Server exiting");
        System.exit(0);
    }
}
