package de.uniluebeck.itm.examples.simple.client;

import de.uniluebeck.itm.examples.performance.client.CoapResponseProcessor;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.EmptyAcknowledgementProcessor;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.InternalEmptyAcknowledgementReceivedMessage;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.InternalRetransmissionTimeoutMessage;
import de.uniluebeck.itm.ncoap.communication.reliability.outgoing.RetransmissionTimeoutProcessor;
import de.uniluebeck.itm.ncoap.message.CoapResponse;
import org.apache.log4j.Logger;

import java.util.SortedMap;
import java.util.TreeMap;

/**
* Created with IntelliJ IDEA.
* User: olli
* Date: 21.06.13
* Time: 21:14
* To change this template use File | Settings | File Templates.
*/
public class SimpleResponseProcessor implements CoapResponseProcessor, EmptyAcknowledgementProcessor,
        RetransmissionTimeoutProcessor {

    private Logger log = Logger.getLogger(this.getClass().getName());

    private SortedMap<Long, CoapResponse> responses = new TreeMap<Long, CoapResponse>();

    private SortedMap<Long, InternalEmptyAcknowledgementReceivedMessage> emptyAcknowledgements
            = new TreeMap<Long, InternalEmptyAcknowledgementReceivedMessage>();

    private SortedMap<Long, InternalRetransmissionTimeoutMessage> timeoutMessages
            = new TreeMap<Long, InternalRetransmissionTimeoutMessage>();


    @Override
    public void processCoapResponse(CoapResponse coapResponse) {
        log.info("Received Response: " + coapResponse);
        responses.put(System.currentTimeMillis(), coapResponse);
    }

    @Override
    public void processEmptyAcknowledgement(InternalEmptyAcknowledgementReceivedMessage message) {
        log.info("Received empty ACK: " + message);
        emptyAcknowledgements.put(System.currentTimeMillis(), message);
    }

    @Override
    public void processRetransmissionTimeout(InternalRetransmissionTimeoutMessage timeoutMessage) {
        log.info("Transmission timed out: " + timeoutMessage);
        timeoutMessages.put(System.currentTimeMillis(), timeoutMessage);
    }
}
