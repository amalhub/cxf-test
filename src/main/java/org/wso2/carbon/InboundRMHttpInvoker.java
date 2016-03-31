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

import org.apache.cxf.continuations.Continuation;
import org.apache.cxf.continuations.ContinuationProvider;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.invoker.AbstractInvoker;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * After the RM protocol messages are processed, the request is processed here
 */
public class InboundRMHttpInvoker extends AbstractInvoker {

    private static Logger logger = Logger.getLogger(InboundRMHttpInvoker.class);
    private ExecutorService executorService;
    private Object bean;

    /**
     * Constructor for the invoker
     *
     * @param bean               An instance of the backend business logic implementing class
     */
    public InboundRMHttpInvoker(Object bean) {
        this.bean = bean;

        setExecutorService(Executors.newFixedThreadPool(100));
    }

    @Override
    public Object getServiceObject(Exchange exchange) {
        return bean;
    }

    /**
     * This is where the the incoming request from the CXF Bus is paused and handed to ODE
     *
     * @param exchange exchange that contains the messages
     * @param o        the dummy back end object
     * @return null
     */
    @Override
    public Object invoke(Exchange exchange, Object o) {
        //testing purpose, todo: link with log4j
        System.out.println("$$$$$$$$$$$$ Invoker $$$$$$$$$$$$$$");

        ContinuationProvider continuationProvider = (ContinuationProvider) exchange.getInMessage().get(ContinuationProvider.class.getName());
        final Continuation continuation = continuationProvider.getContinuation();

        synchronized (continuation) {
            ODEInvoker.invoke(exchange);
        }

        return null;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public final void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }
}
