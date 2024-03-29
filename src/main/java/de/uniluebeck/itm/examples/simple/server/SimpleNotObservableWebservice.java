package de.uniluebeck.itm.examples.simple.server;

import com.google.common.util.concurrent.SettableFuture;
import de.uniluebeck.itm.ncoap.application.server.webservice.NotObservableWebService;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import de.uniluebeck.itm.ncoap.message.MessageDoesNotAllowPayloadException;
import de.uniluebeck.itm.ncoap.message.header.Code;
import de.uniluebeck.itm.ncoap.message.options.Option;
import de.uniluebeck.itm.ncoap.message.options.UintOption;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

import static de.uniluebeck.itm.ncoap.message.options.OptionRegistry.MediaType;
import static de.uniluebeck.itm.ncoap.message.options.OptionRegistry.MediaType.APP_XML;
import static de.uniluebeck.itm.ncoap.message.options.OptionRegistry.MediaType.TEXT_PLAIN_UTF8;
import static de.uniluebeck.itm.ncoap.message.options.OptionRegistry.OptionName;

/**
 * This is a simple not observable webservice. It accepts
 */
public class SimpleNotObservableWebservice extends NotObservableWebService<Long> {

    private static Logger log = Logger.getLogger(SimpleNotObservableWebservice.class.getName());

    public SimpleNotObservableWebservice(String servicePath, Long initialStatus){
        super(servicePath, initialStatus);
    }

   @Override
   public void processCoapRequest(SettableFuture<CoapResponse> responseFuture, CoapRequest request,
                                  InetSocketAddress remoteAddress) {
        log.info("Service " + getPath() + " received request: " + request);
        try{
            if(request.getCode() == Code.GET){
                processGet(responseFuture, request);
            }
            else if(request.getCode() == Code.POST){
                processPost(responseFuture, request);
            }
            else{
                responseFuture.set(new CoapResponse(Code.METHOD_NOT_ALLOWED_405));
            }
        }
        catch(Exception e){
            responseFuture.set(new CoapResponse(Code.INTERNAL_SERVER_ERROR_500));
        }
    }

    private void processGet(SettableFuture<CoapResponse> responseFuture, CoapRequest request) throws Exception{

        List<Option> acceptOptions = request.getOption(OptionName.ACCEPT);

        //If accept option is not set in the request, use the default (TEXT_PLAIN)
        if(acceptOptions.isEmpty()){
            CoapResponse response = new CoapResponse(Code.CONTENT_205);
            response.setPayload(createPayloadFromAcutualStatus(TEXT_PLAIN_UTF8));
            response.setContentType(TEXT_PLAIN_UTF8);
            responseFuture.set(response);
        }

        for(Option option : request.getOption(OptionName.ACCEPT)){
            MediaType acceptedMediaType = MediaType.getByNumber(((UintOption) option).getDecodedValue());
            log.debug("Try to create payload for accepted mediatype " + acceptedMediaType);
            byte[] payload = createPayloadFromAcutualStatus(acceptedMediaType);

            //the requested mediatype is supported
            if(payload != null){
                CoapResponse response = new CoapResponse(Code.CONTENT_205);
                response.setPayload(payload);
                response.setContentType(acceptedMediaType);
                responseFuture.set(response);
            }
        }

        //This is only reached if all accepted mediatypes are not supported!
        CoapResponse response = new CoapResponse(Code.UNSUPPORTED_MEDIA_TYPE_415);
        responseFuture.set(response);
    }

    private synchronized void processPost(SettableFuture<CoapResponse> responseFuture, CoapRequest request){
        CoapResponse response;
        try{
            //parse new status value
            String payload = request.getPayload().toString(Charset.forName("UTF-8"));
            Long newValue = Long.parseLong(payload);

            //set new status
            this.setResourceStatus(newValue);

            //create response
            response = new CoapResponse(Code.CHANGED_204);
            response.setPayload(createPayloadFromAcutualStatus(TEXT_PLAIN_UTF8));
            response.setContentType(MediaType.TEXT_PLAIN_UTF8);

            responseFuture.set(response);

        }
        catch(Exception e){
            response = new CoapResponse(Code.BAD_REQUEST_400);
            try {
                response.setPayload(e.getMessage().getBytes(Charset.forName("UTF-8")));
            } catch (MessageDoesNotAllowPayloadException e1) {
                //This should never happen!
            }
            responseFuture.set(response);
        }
    }

    private byte[] createPayloadFromAcutualStatus(MediaType mediaType){
        if(mediaType == APP_XML){
            StringBuffer payload = new StringBuffer();
            payload.append("<response>\n");
            payload.append("  <value>" + getResourceStatus() + "</value>\n");
            payload.append("</response>");

            return payload.toString().getBytes(Charset.forName("UTF-8"));
        }
        else if(mediaType == TEXT_PLAIN_UTF8){
            String payload = "The value is " + getResourceStatus() + ".";
            return payload.getBytes(Charset.forName("UTF-8"));
        }
        else{
            return null;
        }
    }

    @Override
    public void shutdown() {
        //Nothing to do here...
    }
}
