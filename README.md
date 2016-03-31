# cxf-test

C5 - Enabling SOAP support for BPEL with Apache CXF integration.

The implementation exposes soap webservices by dynamically given wsdl files. 



## The extension points used in Apache CXF are as follows
* To intercept the request message we have extended the org.apache.cxf.phase.AbstractPhaseInterceptor<Message> class under RECEIVE phase and have overridden the handleMessage method.
* To intercept the Response message we have extended the org.apache.cxf.phase.AbstractPhaseInterceptor<Message> class under PRE_STREAM phase and have overridden the handleMessage method.
* Both of the above classes are registered into the interceptor chain at the server initialization. 
* In-order to inject the message into ODE, we have extended the org.apache.cxf.service.invoker.AbstractInvoker class.
* org.apache.cxf.wsdl11.WSDLServiceFactory is used to create the server from the wsdl dynamically.
* org.apache.cxf.binding.soap.SoapBindingFactory is used to register SOAP bindings configurations. 

Currently we are facing a limitation where org.apache.cxf.endpoint.Server[1] only allows to set one address endpoint per object. Therefore we have to create an array of Server objects to interact with multiple wsdl files. 

[1] https://cxf.apache.org/javadoc/latest/org/apache/cxf/endpoint/Server.html
