package com.mycompany.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.*;
import javax.ws.rs.*;

import javax.ws.rs.core.Response;

@Path("placanje")
public class PlacanjeResource {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory connFactory;
    
    @Resource(lookup="queue3")
    private Queue queue3;
    
    @Resource(lookup="queueReturnPlacanje")
    private Queue queueReturnPlacanje;
    
    @POST
    @Path("plati/{info}")
    public Response plati(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnPlacanje, "operacija='plati'", false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "plati");
            producer.send(queue3, txtMsg);
        } catch (JMSException ex) {
            Logger.getLogger(PlacanjeResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(PlacanjeResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}