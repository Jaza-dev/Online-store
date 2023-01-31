package com.mycompany.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.ws.rs.*;

import javax.ws.rs.core.Response;

@Path("narudzbina")
public class NarudzbinaResource {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory connFactory;
    
    @Resource(lookup="queue3")
    private Queue queue3;
    
    @Resource(lookup="queueReturnNarudzbina1")
    private Queue queueReturnNarudzbina1;
    
    @Resource(lookup="queueReturnNarudzbina2")
    private Queue queueReturnNarudzbina2;
    
    @GET
    @Path("dohvatiNarudzbineKorisnika/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response dohvatiNarudzbineKorisnika(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnNarudzbina1, "operacija='dohvatiNarudzbineKorisnika'", false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "dohvatiNarudzbineKorisnika");
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
    
    @GET
    @Path("dohvatiSve")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response dohvatiSveNarudzbine(){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnNarudzbina2, "operacija='dohvatiSveNarudzbine'", false);
        
        try {
            TextMessage txtMsg = context.createTextMessage();
            txtMsg.setStringProperty("operacija", "dohvatiSveNarudzbine");
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