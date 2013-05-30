package de.uniluebeck.itm.spitfire.nCoap.application.server;

import de.uniluebeck.itm.spitfire.nCoap.message.CoapRequest;
import de.uniluebeck.itm.spitfire.nCoap.message.CoapResponse;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 * User: olli
 * Date: 24.05.13
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class SimpleNotObservableWebServiceWithDelay extends SimpleNotObservableWebservice{

    private static Logger log = Logger.getLogger(SimpleNotObservableWebservice.class.getName());
    private long delay;

    public SimpleNotObservableWebServiceWithDelay(String servicePath, Long initialStatus, long delayMillis) {
        super(servicePath, initialStatus);
        this.delay = delayMillis;
    }

    @Override
    public CoapResponse processMessage(CoapRequest request, InetSocketAddress remoteAddress) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            log.error("This should never happen.", e);
        }

        return super.processMessage(request, remoteAddress);
    }
}
