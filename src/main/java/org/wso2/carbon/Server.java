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

import com.ibm.wsdl.ServiceImpl;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.binding.soap.SoapBindingConstants;
import org.apache.cxf.binding.soap.SoapBindingFactory;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.binding.soap.interceptor.*;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.*;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.apache.cxf.wsdl.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.wsdl11.WSDLServiceFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Server
 */
public class Server {
    protected Server() throws Exception {
        URL resource = getClass().getResource("/HelloWorld2.wsdl");

        System.out.println("Starting Server");
        //String wsdl = "http://localhost:9763/services/HelloService?wsdl";
        String address = "http://localhost:9000/HelloService";
        String ns = "http://ode/bpel/unit-test.wsdl";
        String serviceName = "HelloService";
        Bus bus = BusFactory.newInstance().createBus();

        SoapBindingFactory bindingFactory = new SoapBindingFactory();
        bindingFactory.setBus(bus);
        bus.getExtension(BindingFactoryManager.class)
                .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", bindingFactory);
        bus.getExtension(BindingFactoryManager.class)
                .registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/http", bindingFactory);

        DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);

        SoapTransportFactory soapDF = new SoapTransportFactory();
        dfm.registerDestinationFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapDF);
        dfm.registerDestinationFactory(SoapBindingConstants.SOAP11_BINDING_ID, soapDF);
        dfm.registerDestinationFactory(SoapBindingConstants.SOAP12_BINDING_ID, soapDF);
        dfm.registerDestinationFactory("http://cxf.apache.org/transports/local", soapDF);

        LocalTransportFactory localTransport = new LocalTransportFactory();
        localTransport.setUriPrefixes(new HashSet<String>(Arrays.asList("http", "local")));
        dfm.registerDestinationFactory(LocalTransportFactory.TRANSPORT_ID, localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/transports/http", localTransport);
        dfm.registerDestinationFactory("http://cxf.apache.org/transports/http/configuration", localTransport);

        ConduitInitiatorManager extension = bus.getExtension(ConduitInitiatorManager.class);
        extension.registerConduitInitiator(LocalTransportFactory.TRANSPORT_ID, localTransport);
        extension.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", localTransport);
        extension.registerConduitInitiator("http://cxf.apache.org/transports/http", localTransport);
        extension.registerConduitInitiator("http://cxf.apache.org/transports/http/configuration",
                localTransport);

        Service service = create(bus, resource, ns, serviceName);

        //test 2
        /*ReflectionServiceFactoryBean bean = new JaxWsServiceFactoryBean();
        bean.setWsdlURL(resource.toString());
        bean.setBus(bus);
        bean.setServiceClass(service.getClass());

        BeanInvoker invoker = new BeanInvoker(service.getInvoker());
        bean.setInvoker(invoker);

        ServerFactoryBean svrFactory = new ServerFactoryBean();
        svrFactory.setBus(bus);
        svrFactory.setServiceClass(service.getClass());
        svrFactory.setServiceFactory(bean);
        svrFactory.setAddress(address);
        svrFactory.create();*/

        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(service.getClass());
        svrFactory.setAddress(address);
        svrFactory.setServiceBean(service);
        svrFactory.setBus(bus);
        svrFactory.getInInterceptors().add(new LoggingInInterceptor());
        svrFactory.getOutInterceptors().add(new LoggingOutInterceptor());
        svrFactory.create();
    }

    public Service create(Bus bus, URL wsdl, String ns, String serviceName) {
        WSDLServiceFactory factory = new WSDLServiceFactory(bus, wsdl.toString(), new QName(ns, serviceName));
        Service service = factory.create();
        initializeSoapInterceptors(service, bus);
        //updateEndpointInfo(service);
        return service;
    }

    /*private void updateEndpointInfo(Service service) {

        for (ServiceInfo inf : service.getServiceInfos()) {
            for (EndpointInfo ei : inf.getEndpoints()) {
                //setup the endpoint address
                ei.setAddress("local://" + ei.getService().getName().toString() + "/" + ei.getName().getLocalPart());
                // working as the dispatch mode, the binding factory will not add interceptor
                //ei.getBinding().setProperty(AbstractBindingFactory.DATABINDING_DISABLED, Boolean.TRUE);
            }
        }

    }*/

    // do not handle any payload information here
    private void initializeSoapInterceptors(Service service, Bus bus) {
        //service.getInInterceptors().add(new DataInInterceptor());
        service.getInInterceptors().add(new ReadHeadersInterceptor(bus));
        service.getInInterceptors().add(new MustUnderstandInterceptor());
        service.getInInterceptors().add(new AttachmentInInterceptor());


        service.getInInterceptors().add(new StaxInInterceptor());
        service.getInInterceptors().add(new SoapActionInInterceptor());

        //service.getOutInterceptors().add(new DataOutInterceptor());
        //service.getOutInterceptors().add(new SoapActionOutInterceptor());
        service.getOutInterceptors().add(new AttachmentOutInterceptor());
        service.getOutInterceptors().add(new StaxOutInterceptor());
        service.getOutInterceptors().add(new SoapHeaderOutFilterInterceptor());

        service.getOutInterceptors().add(new SoapPreProtocolOutInterceptor());
        service.getOutInterceptors().add(new SoapOutInterceptor(bus));
        service.getOutFaultInterceptors().add(new SoapOutInterceptor(bus));
    }

    public static void main(String args[]) throws Exception {
        new Server();
        System.out.println("Server ready...");

        Thread.sleep(5 * 60 * 1000);
        System.out.println("Server exiting");
        System.exit(0);
    }
}
