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

import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * Intercepts the outbound message and sets the SOAPBody and some of the SOAPHeaders
 */
public class ResponseInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Logger logger = Logger.getLogger(RequestInterceptor.class);
    public ResponseInterceptor() {
        super(Phase.PRE_STREAM);
        addBefore(SoapPreProtocolOutInterceptor.class.getName());
    }

    public void handleMessage(Message message) throws Fault {
        boolean isOutbound = message == message.getExchange().getOutMessage()
                || message == message.getExchange().getOutFaultMessage();

        if (isOutbound) {
            InputStream is = message.getContent(InputStream.class);
            try {
                String myString = IOUtils.toString(is, "UTF-8");
                System.out.println(myString);
            } catch (IOException e) {
                logger.error("Error reading message from message content", e);
            }
        }
    }
}
