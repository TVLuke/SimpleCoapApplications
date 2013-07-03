/**
* Copyright (c) 2012, Oliver Kleine, Institute of Telematics, University of Luebeck
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
* following conditions are met:
*
* - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
* disclaimer.
* - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
* following disclaimer in the documentation and/or other materials provided with the distribution.
* - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
* products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
* INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
* INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
* OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package de.uniluebeck.itm.examples.simple.server;

import de.uniluebeck.itm.ncoap.application.server.CoapServerApplication;
import org.apache.log4j.*;

/**
* This is a very simple server application providing a .well-known/core resource and some resources only
* allowing {@link de.uniluebeck.itm.ncoap.message.header.Code#GET} requests.
*
* @author Oliver Kleine
*/
public class SimpleCoapServer {

    public static int NUMBER_OF_SERVICES_WITH_DELAY = 1;
    public static int NUMBER_OF_SERVICES_WITHOUT_DELAY = 1;
    public static boolean ACTIVATE_TIMESERVICE = true;

    //Initialize logging
    static{
        //String pattern = "%r ms: [%C{1}] %m %n";
        String pattern = "%-23d{yyyy-MM-dd HH:mm:ss,SSS} | %-32.32t | %-35.35c{1} | %-5p | %m%n";
        PatternLayout patternLayout = new PatternLayout(pattern);

        AsyncAppender appender = new AsyncAppender();
        appender.addAppender(new ConsoleAppender(patternLayout));
        Logger.getRootLogger().addAppender(appender);

        Logger.getRootLogger().setLevel(Level.DEBUG);
        Logger.getLogger("de.uniluebeck.itm.spitfire.nCoap.communication.reliability").setLevel(Level.DEBUG);
        Logger.getLogger("de.uniluebeck.itm.spitfire.nCoap.application").setLevel(Level.DEBUG);
        Logger.getLogger("de.uniluebeck.itm.examples.simple.server").setLevel(Level.DEBUG);

    }

    private static Logger log = Logger.getLogger(SimpleCoapServer.class.getName());

    public static void main(String[] args){

        //start the server
        CoapServerApplication server = new CoapServerApplication();
        log.info("Server started and listening on port " + server.getServerPort());

        //register resource(s)
        //server.registerService(new SimpleNotObservableWebservice("/simple/not-observable", new Long(10)));

        for(int i = 1; i <= NUMBER_OF_SERVICES_WITHOUT_DELAY; i++){
            server.registerService(new SimpleNotObservableWebservice("/service/not-delayed/" + i,
                    new Long(i)));
        }

        for(int i = 1; i <= NUMBER_OF_SERVICES_WITH_DELAY; i++){
            server.registerService(new SimpleNotObservableWebServiceWithDelay("/service/delayed/" + i,
                    new Long(i), 3000));
        }

        //Time service
        if(ACTIVATE_TIMESERVICE){
            SimpleObservableTimeService timeService = new SimpleObservableTimeService("/simple/observable-utc-time");
            server.registerService(timeService);
        }

        //That's it!
    }
}
