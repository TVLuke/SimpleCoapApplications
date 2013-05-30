package de.uniluebeck.itm.spitfire.nCoap.application.server;

import de.uniluebeck.itm.spitfire.nCoap.application.server.webservice.NotObservableWebService;
import de.uniluebeck.itm.spitfire.nCoap.message.CoapRequest;
import de.uniluebeck.itm.spitfire.nCoap.message.CoapResponse;
import de.uniluebeck.itm.spitfire.nCoap.message.MessageDoesNotAllowPayloadException;
import de.uniluebeck.itm.spitfire.nCoap.message.header.Code;
import de.uniluebeck.itm.spitfire.nCoap.message.options.Option;
import de.uniluebeck.itm.spitfire.nCoap.message.options.UintOption;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

import static de.uniluebeck.itm.spitfire.nCoap.message.options.OptionRegistry.MediaType;
import static de.uniluebeck.itm.spitfire.nCoap.message.options.OptionRegistry.MediaType.APP_XML;
import static de.uniluebeck.itm.spitfire.nCoap.message.options.OptionRegistry.MediaType.TEXT_PLAIN_UTF8;
import static de.uniluebeck.itm.spitfire.nCoap.message.options.OptionRegistry.OptionName;

/**
 * This is a simple not observable webservice. It accepts
 */
public class SimpleNotObservableWebservice extends NotObservableWebService<Long> {

    private static Logger log = Logger.getLogger(SimpleNotObservableWebservice.class.getName());

    public SimpleNotObservableWebservice(String servicePath, Long initialStatus){
        super(servicePath, initialStatus);
    }

   @Override
    public CoapResponse processMessage(CoapRequest request, InetSocketAddress remoteAddress) {
        log.debug("Service at " + getPath() + " received request (" + request.getCode() + ").");
        try{
            if(request.getCode() == Code.GET){
                return processGet(request);
            }
            else if(request.getCode() == Code.POST){
                return processPost(request);
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

        List<Option> acceptOptions = request.getOption(OptionName.ACCEPT);

        //If accept option is not set in the request, use the default (TEXT_PLAIN)
        if(acceptOptions.isEmpty()){
            CoapResponse response = new CoapResponse(Code.CONTENT_205);
            response.setPayload(createPayloadFromAcutualStatus(TEXT_PLAIN_UTF8));
            response.setContentType(TEXT_PLAIN_UTF8);
            return response;
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
                return response;
            }
        }

        //This is only reached if all accepted mediatypes are not supported!
        CoapResponse response = new CoapResponse(Code.UNSUPPORTED_MEDIA_TYPE_415);
        return response;
    }

    private synchronized CoapResponse processPost(CoapRequest request){
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

            return response;

        }
        catch(Exception e){
            response = new CoapResponse(Code.BAD_REQUEST_400);
            try {
                response.setPayload(e.getMessage().getBytes(Charset.forName("UTF-8")));
            } catch (MessageDoesNotAllowPayloadException e1) {
                //This should never happen!
            }
            return response;
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
            String payload = "The current value is " + getResourceStatus() + ".";
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
