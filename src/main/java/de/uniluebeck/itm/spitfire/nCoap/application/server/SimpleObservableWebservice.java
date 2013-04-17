package de.uniluebeck.itm.spitfire.nCoap.application.server;

import de.uniluebeck.itm.spitfire.nCoap.application.webservice.ObservableWebService;
import de.uniluebeck.itm.spitfire.nCoap.message.CoapRequest;
import de.uniluebeck.itm.spitfire.nCoap.message.CoapResponse;
import de.uniluebeck.itm.spitfire.nCoap.message.MessageDoesNotAllowPayloadException;
import de.uniluebeck.itm.spitfire.nCoap.message.header.Code;
import de.uniluebeck.itm.spitfire.nCoap.message.options.OptionRegistry;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 17.04.13
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class SimpleObservableWebservice extends ObservableWebService<Boolean> {

    private Logger log = Logger.getLogger(SimpleObservableWebservice.class.getName());

    protected SimpleObservableWebservice(String path, Boolean initialStatus) {
        super(path, initialStatus);

        new Thread(new Runnable(){

            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.error("This should never happen.", e);
                    }
                    setResourceStatus(!getResourceStatus());
                }
            }
        }).start();
    }

    @Override
    public CoapResponse processMessage(CoapRequest request, InetSocketAddress remoteAddress) {
        try{
            if(request.getCode() == Code.GET){
                return processGet(request);
            }
            else{
                return new CoapResponse(Code.METHOD_NOT_ALLOWED_405);
            }
        }
        catch(Exception e){
            return new CoapResponse(Code.INTERNAL_SERVER_ERROR_500);
        }
    }

    private CoapResponse processGet(CoapRequest request) throws Exception{
        CoapResponse response = new CoapResponse(Code.CONTENT_205);
        String payload = "The observed thing is currently switched " + (getResourceStatus() ? "on" : "off");
        response.setPayload(payload.getBytes(Charset.forName("UTF-8")));
        response.setContentType(OptionRegistry.MediaType.TEXT_PLAIN_UTF8);
        return response;
    }
}
