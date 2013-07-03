package de.uniluebeck.itm.examples.simple.server;

import com.google.common.util.concurrent.SettableFuture;
import de.uniluebeck.itm.ncoap.message.CoapRequest;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 24.05.13
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class SimpleNotObservableWebServiceWithDelay extends SimpleNotObservableWebservice {

    private static Logger log = Logger.getLogger(SimpleNotObservableWebservice.class.getName());
    private long delay;

    public SimpleNotObservableWebServiceWithDelay(String servicePath, Long initialStatus, long delayMillis) {
        super(servicePath, initialStatus);
        this.delay = delayMillis;
    }

    @Override
    public void processCoapRequest(SettableFuture<CoapResponse> responseFuture, CoapRequest request,
                                   InetSocketAddress remoteAddress) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            log.error("This should never happen.", e);
        }

        super.processCoapRequest(responseFuture, request, remoteAddress);
    }
}
