package com.mycompany.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("artikal")
public class ArtikalResource {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory connFactory;
    
    @Resource(lookup="queue2")
    private Queue queue2;
    
    @Resource(lookup="queueReturnArtikal1")
    private Queue queueReturnArtikal1;
    
    @Resource(lookup="queueReturnArtikal2")
    private Queue queueReturnArtikal2;
    
    @Resource(lookup="queueReturnArtikal3")
    private Queue queueReturnArtikal3;
    
    @Resource(lookup="queueReturnArtikal4")
    private Queue queueReturnArtikal4;
    
    
    @POST
    @Path("napravi/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response napraviArtikal(@PathParam("info") String info){
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnArtikal1, "operacija='napraviArtikal'", false);
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "napraviArtikal");
            producer.send(queue2, txtMsg);
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
    
    @POST
    @Path("promeniCenu/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response promeniCenu(@PathParam("info") String info){
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnArtikal2, "operacija='promeniCenuArtikla'", false);
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "promeniCenuArtikla");
            producer.send(queue2, txtMsg);
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
    
    @POST
    @Path("postaviPopust/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response postaviPopust(@PathParam("info") String info){
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnArtikal3, "operacija='postaviPopustArtiklu'", false);
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "postaviPopustArtiklu");
            producer.send(queue2, txtMsg);
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
    @Path("dohvatiSveKojeProdaje/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response dohvatiSveKojeProdaje(@PathParam("info") String info){
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnArtikal4, "operacija='dohvatiSveKojeProdaje'", false);
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "dohvatiSveKojeProdaje");
            producer.send(queue2, txtMsg);
            
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