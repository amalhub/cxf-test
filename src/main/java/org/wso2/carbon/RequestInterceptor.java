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

import org.apache.cxf.binding.soap.interceptor.*;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.StaxInInterceptor;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxb.attachment.JAXBAttachmentSchemaValidationHack;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.https.CertConstraintsInterceptor;
import org.apache.cxf.wsdl.interceptors.DocLiteralInInterceptor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This interceptor intercepts all incoming message and removes Interceptors from the interceptor
 * chain that will not be needed.
 * It then gets a copy of the message to a byte[] and attaches it to the CXF Message
 */
public class RequestInterceptor extends AbstractPhaseInterceptor<Message> {
    private Set<Class<? extends Interceptor<?>>> interceptorSet;
    private static final Logger logger = Logger.getLogger(RequestInterceptor.class);

    public RequestInterceptor() {
        super(Phase.RECEIVE);
        getBefore().add(DocLiteralInInterceptor.class.getName());

        interceptorSet = new HashSet<Class<? extends Interceptor<?>>>();

        interceptorSet.add(DocLiteralInInterceptor.class);
        interceptorSet.add(SoapActionInInterceptor.class);
        interceptorSet.add(MustUnderstandInterceptor.class);
        interceptorSet.add(SoapHeaderInterceptor.class);
        interceptorSet.add(CertConstraintsInterceptor.class);
        interceptorSet.add(StaxInInterceptor.class);
        interceptorSet.add(RPCInInterceptor.class);
        interceptorSet.add(ReadHeadersInterceptor.class);
        interceptorSet.add(JAXBAttachmentSchemaValidationHack.class);
        interceptorSet.add(StartBodyInterceptor.class);
    }

    public void handleMessage(Message message) throws Fault {
        //testing purpose, todo: link with log4j
        System.out.println("################## Service Invoked ###############");

        /*
         * Remove the unnecessary interceptors from the message's interceptor chain
         */
        this.removeInterceptors(interceptorSet, message);

        InputStream is = message.getContent(InputStream.class);

        try {
            String myString = IOUtils.toString(is, "UTF-8");
            //testing purpose, todo: link with log4j
            System.out.println(myString);
        } catch (IOException e) {
            logger.error("Error reading message from message content", e);
        }

        if (is != null) {
            CachedOutputStream bos = new CachedOutputStream();
            try {
                IOUtils.copy(is, bos);
                bos.flush();

                message.setContent(InputStream.class, bos.getInputStream());
                byte[] bytes = bos.getBytes();
                //Attach the payload to the cxf message
                message.put("cxf-rm.message-payload", bytes);
            } catch (IOException ioe) {
                logger.error("Error while extracting the payload from the message", ioe);
                throw new Fault(new Exception("Error while processing the request"));
            } finally {
                org.apache.commons.io.IOUtils.closeQuietly(is);
                org.apache.commons.io.IOUtils.closeQuietly(bos);
            }
        }
    }

    /**
     * Removes interceptors specified in the interceptorSet from the interceptor chain
     *
     * @param interceptorSet the set of interceptors to be removed
     * @param message        Incoming message
     */
    private void removeInterceptors(Set<Class<? extends Interceptor<?>>> interceptorSet, Message message) {

        Iterator<Interceptor<? extends Message>> iterator = message.getInterceptorChain().iterator();
        while (iterator.hasNext()) {
            Interceptor<?> interceptor = iterator.next();
            Class interceptorClass = interceptor.getClass();
            if (interceptorSet.contains(interceptorClass)) {
                message.getInterceptorChain().remove(interceptor);
            }
        }
    }
}
