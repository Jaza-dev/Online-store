package com.mycompany.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.ws.rs.*;

import javax.ws.rs.core.Response;

@Path("korpa")
public class KorpaResource {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory connFactory;
    
    @Resource(lookup="queue2")
    private Queue queue2;
    
    @Resource(lookup="queueReturnKorpa1")
    private Queue queueReturnKorpa1;
    
    @Resource(lookup="queueReturnKorpa2")
    private Queue queueReturnKorpa2;
    
    @Resource(lookup="queueReturnKorpa3")
    private Queue queueReturnKorpa3;
    
    @POST
    @Path("dodaj/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response dodajUKorpu(@PathParam("info") String info) throws InterruptedException{
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKorpa1, "operacija='dodajUKorpu'", false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "dodajUKorpu");
            producer.send(queue2, txtMsg);
        } catch (JMSException ex) {
            Logger.getLogger(KorpaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KorpaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @POST
    @Path("obrisi/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response obrisiIzKorpe(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKorpa2, "operacija='obrisiIzKorpe'", false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "obrisiIzKorpe");
            producer.send(queue2, txtMsg);
        } catch (JMSException ex) {
            Logger.getLogger(KorpaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KorpaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @GET
    @Path("dohvatiKorpuKorisnika/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response dohvatiKorpuKorisnika(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKorpa3, "operacija='dohvatiKorpuKorisnika'", false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "dohvatiKorpuKorisnika");
            producer.send(queue2, txtMsg);
        } catch (JMSException ex) {
            Logger.getLogger(KorpaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KorpaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}