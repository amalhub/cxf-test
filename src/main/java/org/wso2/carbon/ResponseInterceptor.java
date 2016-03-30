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
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        //testing purpose, todo: link with log4j
        System.out.println("########### Sending Response #################");
        boolean isOutbound = message == message.getExchange().getOutMessage()
                || message == message.getExchange().getOutFaultMessage();

        //If the response came through Synapse, it is handled here
        if (isOutbound) {

            String response = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                    "   <soapenv:Header/>\n" +
                    "   <soapenv:Body>\n" +
                    "         <result>Success</result>\n" +
                    "   </soapenv:Body>\n" +
                    "</soapenv:Envelope>";
            InputStream is = new ByteArrayInputStream(response.getBytes());
            OutputStream os = message.getContent(OutputStream.class);

            try {
                SOAPEnvelope cxfOutEnvelope = SOAPEnvelopeCreator.getSOAPEnvelopeFromStream(is);

                InputStream replaceInStream = org.apache.commons.io.IOUtils.toInputStream(cxfOutEnvelope.toString(), "UTF-8");
                IOUtils.copy(replaceInStream, os);
                os.flush();
                message.setContent(OutputStream.class, os);
            } catch (IOException ioe) {
                logger.error("Error while processing the response message through the response interceptor", ioe);
                throw new Fault(new Exception("Error while processing the response"));
            } finally {
                org.apache.commons.io.IOUtils.closeQuietly(os);
                org.apache.commons.io.IOUtils.closeQuietly(is);
            }
        }
    }

}
