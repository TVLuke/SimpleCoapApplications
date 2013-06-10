package de.uniluebeck.itm.spitfire.nCoap.application.server;

import de.uniluebeck.itm.spitfire.nCoap.application.server.webservice.ObservableWebService;
import de.uniluebeck.itm.spitfire.nCoap.message.CoapRequest;
import de.uniluebeck.itm.spitfire.nCoap.message.CoapResponse;
import de.uniluebeck.itm.spitfire.nCoap.message.header.Code;
import de.uniluebeck.itm.spitfire.nCoap.message.options.OptionRegistry.MediaType;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 17.04.13
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class SimpleObservableTimeService extends ObservableWebService<Long> {

    private Logger log = Logger.getLogger(SimpleObservableTimeService.class.getName());

    public static int RESOURCE_UPDATE_INTERVAL_MILLIS = 20;

    public SimpleObservableTimeService(String path) {
        super(path, System.currentTimeMillis());
        setMaxAge(1);
    }

    public void schedulePeriodicResourceUpdate(){
        getExecutorService().scheduleAtFixedRate(new Runnable(){

            @Override
            public void run() {
                setResourceStatus(System.currentTimeMillis());
                log.info("New status of resource " + getPath() + ": " + getResourceStatus());
            }
        },0, RESOURCE_UPDATE_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    public CoapResponse processMessage(CoapRequest coapRequest, InetSocketAddress remoteAddress) {
        try{
            if(coapRequest.getCode() == Code.GET)
                return processGet(coapRequest);
            else
                return new CoapResponse(Code.METHOD_NOT_ALLOWED_405);
        }
        catch(Exception e){
            return new CoapResponse(Code.INTERNAL_SERVER_ERROR_500);
        }
    }

    private CoapResponse processGet(CoapRequest request) throws Exception{

        //Try to get the payload according to the requested media type
        MediaType contentType = null;
        byte[] payload = null;
        if(request.getAccept().isEmpty()){
            log.debug("Incoming request has no accept-option. Use " +  MediaType.TEXT_PLAIN_UTF8 + " as default.");
            payload = getPayload(MediaType.TEXT_PLAIN_UTF8);
            contentType = MediaType.TEXT_PLAIN_UTF8;
        }
        else{
            for(MediaType mediaType : request.getAccept()){
                payload = getPayload(mediaType);
                if(payload != null){
                    contentType = mediaType;
                    break;
                }
            }
        }

        //Prepare the response
        CoapResponse coapResponse;
        if(payload != null && contentType != null){
            coapResponse = new CoapResponse(Code.CONTENT_205);
            coapResponse.setContentType(contentType);
            coapResponse.setPayload(payload);
        }
        else{
            coapResponse = new CoapResponse(Code.UNSUPPORTED_MEDIA_TYPE_415);
            String text = "Requested media type(s) not supported:";
            for(MediaType mediaType : request.getAccept()){
                text = text + "\n" + mediaType;
            }
            coapResponse.setPayload(text.getBytes(Charset.forName("UTF-8")));
        }

        return coapResponse;
    }

    private byte[] getPayload(MediaType mediaType){

        log.debug("Try to create " + mediaType + " payload");

        long time = getResourceStatus() % 86400000;
        long hours = time / 3600000;
        long remainder = time % 3600000;
        long minutes = remainder / 60000;
        long seconds = (remainder % 60000) / 1000;

        if(mediaType == MediaType.TEXT_PLAIN_UTF8)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds).getBytes(Charset.forName("UTF-8"));

        else if(mediaType == MediaType.APP_XML)
            return String.format("<time>\n" +
                    "\t<hour>%02d</hour>\n" +
                    "\t<minute>%02d</minute>\n" +
                    "\t<second>%02d</second>\n" +
                    "</time>", hours, minutes, seconds).getBytes(Charset.forName("UTF-8"));

        else
            return null;
    }

    @Override
    public void shutdown() {
        //Nothing to do here...
    }
}
