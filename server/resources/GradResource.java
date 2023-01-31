package com.mycompany.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.ws.rs.*;

import javax.ws.rs.core.Response;

@Path("grad")
public class GradResource {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory connFactory;
    
    @Resource(lookup="queue1")
    private Queue queue1;
    
    @Resource(lookup="queueReturnGrad1")
    private Queue queueReturnGrad1;
    
    @Resource(lookup="queueReturnGrad2")
    private Queue queueReturnGrad2;
    
    @POST
    @Path("napravi/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response napraviGrad(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnGrad1);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "napraviGrad");
            producer.send(queue1, txtMsg);
        } catch (JMSException ex) {
            Logger.getLogger(GradResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(GradResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @GET
    @Path("dohvatiSve")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response dohvatiSve(){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnGrad2, "operacija='dohvatiSveGradove'", false);
        
        try {
            TextMessage txtMsg = context.createTextMessage();
            txtMsg.setStringProperty("operacija", "dohvatiSveGradove");
            producer.send(queue1, txtMsg);
            
        } catch (JMSException ex) {
            Logger.getLogger(GradResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(GradResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
}