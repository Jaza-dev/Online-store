package com.mycompany.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.ws.rs.*;

import javax.ws.rs.core.Response;

@Path("transakcija")
public class TransakcijaResource {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory connFactory;
    
    @Resource(lookup="queue3")
    private Queue queue3;
    
    @Resource(lookup="queueReturnTransakcija")
    private Queue queueReturnTransakcija;
    
    @GET
    @Path("dohvatiSve")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response dohvatiSveTransakcije(){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnTransakcija, "operacija='dohvatiSveTransakcije'", false);
        
        try {
            TextMessage txtMsg = context.createTextMessage();
            txtMsg.setStringProperty("operacija", "dohvatiSveTransakcije");
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