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

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ODE invoker, Injecting incoming messages to ODE
 */
public class ODEInvoker {
    private static final Logger logger = Logger.getLogger(RequestInterceptor.class);
    private static SOAPEnvelope response;

    public static synchronized void invoke(Exchange exchange) {
        Message message = exchange.getInMessage();
        //todo: Calling ODE and processing the request (inMessage)
        String response = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "         <result>Success</result>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        InputStream is = new ByteArrayInputStream(response.getBytes());
        SOAPEnvelope cxfOutEnvelope;
        try {
            cxfOutEnvelope = SOAPEnvelopeCreator.getSOAPEnvelopeFromStream(is);
        } catch (IOException e) {
            logger.error("Error while processing the request message through the ODE Invoker", e);
            throw new Fault(new Exception("Error while processing the response"));
        }
        setResponse(cxfOutEnvelope);
    }

    public static SOAPEnvelope getResponse() {
        return response;
    }

    public static void setResponse(SOAPEnvelope response) {
        ODEInvoker.response = response;
    }
}
