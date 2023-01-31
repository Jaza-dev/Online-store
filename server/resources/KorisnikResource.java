package com.mycompany.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.ws.rs.*;

import javax.ws.rs.core.Response;

@Path("korisnik")
public class KorisnikResource {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory connFactory;
    
    @Resource(lookup="queue1")
    private Queue queue1;
    
    @Resource(lookup="queueReturnKorisnik1")
    private Queue queueReturnKorisnik1;
    
    @Resource(lookup="queueReturnKorisnik2Temp")
    private Queue queueReturnKorisnik2;
    
    @Resource(lookup="queueReturnKorisnik3")
    private Queue queueReturnKorisnik3;
    
    @Resource(lookup="queueReturnKorisnik4")
    private Queue queueReturnKorisnik4;
    
    @Resource(lookup="queueReturnKorisnik5")
    private Queue queueReturnKorisnik5;
    
    @POST
    @Path("napravi/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response napraviKorisnika(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKorisnik1,"operacija='napraviKorisnika'",false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "napraviKorisnika");
            producer.send(queue1, txtMsg);
            
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @POST
    @Path("dodajNovac/{info}")
    public Response dodajNovac(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKorisnik2,"operacija='dodajNovac'",false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "dodajNovac");
            producer.send(queue1, txtMsg);
            
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @POST
    @Path("promenaPrebivalista/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response promenaPrebivalista(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKorisnik3,"operacija='promenaPrebivalista'",false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "promenaPrebivalista");
            producer.send(queue1, txtMsg);
            
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @GET
    @Path("dohvatiSve")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response dohvatiSve(){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKorisnik4,"operacija='dohvatiSveKorisnike'",false);
        
        try {
            TextMessage txtMsg = context.createTextMessage();
            txtMsg.setStringProperty("operacija", "dohvatiSveKorisnike");
            producer.send(queue1, txtMsg);
            
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @GET
    @Path("login/{info}")
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Response login(@PathParam("info") String info){
        
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKorisnik5,"operacija='login'",false);
        
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "login");
            producer.send(queue1, txtMsg);
            
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KorisnikResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}