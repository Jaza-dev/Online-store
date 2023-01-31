package com.mycompany.server.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("kategorija")
public class KategorijaResource {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    private ConnectionFactory connFactory;
    
    @Resource(lookup="queue2")
    private Queue queue2;
    
    @Resource(lookup="queueReturnKategorija1")
    private Queue queueReturnKategorija1;
    
    @Resource(lookup="queueReturnKategorija2")
    private Queue queueReturnKategorija2;
    
    @POST
    @Path("napravi/{info}")
    public Response napraviKategoriju(@PathParam("info") String info){
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKategorija1, "operacija='napraviKategoriju'", false);
        try {
            TextMessage txtMsg = context.createTextMessage(info);
            txtMsg.setStringProperty("operacija", "napraviKategoriju");
            producer.send(queue2, txtMsg);
        } catch (JMSException ex) {
            Logger.getLogger(KategorijaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KategorijaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @GET
    @Path("dohvatiSve")
    public Response dohvatiSveKategorije(){
        JMSContext context = connFactory.createContext();
        JMSProducer producer = context.createProducer();
        JMSConsumer consumer = context.createConsumer(queueReturnKategorija2, "operacija='dohvatiSveKategorije'", false);
        try {
            TextMessage txtMsg = context.createTextMessage();
            txtMsg.setStringProperty("operacija", "dohvatiSveKategorije");
            producer.send(queue2, txtMsg);
            
        } catch (JMSException ex) {
            Logger.getLogger(KategorijaResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        try {
            Message msg = consumer.receive();
            TextMessage txtMsg = (TextMessage) msg;
            return Response.ok().entity(txtMsg.getText()).build();
        } catch (JMSException ex) {
            Logger.getLogger(KategorijaResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
