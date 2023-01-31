package podsistem1;

import entiteti.Grad;
import entiteti.Korisnik;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jms.*;
import javax.persistence.*;

public class MainP1 {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    static ConnectionFactory connFactory;
    
    @Resource(lookup="queue1")
    static Queue queue1;
    
    @Resource(lookup="queue2")
    static Queue queue2;
    
    @Resource(lookup="queue3")
    static Queue queue3;
    
    @Resource(lookup="queueReturnGrad1")
    static Queue queueReturnGrad1;
    
    @Resource(lookup="queueReturnGrad2")
    static Queue queueReturnGrad2;
    
    @Resource(lookup="queueReturnKorisnik1")
    static Queue queueReturnKorisnik1;
    
    @Resource(lookup="queueReturnKorisnik2Temp")
    static Queue queueReturnKorisnik2;
    
    @Resource(lookup="queueReturnKorisnik3")
    static Queue queueReturnKorisnik3;
    
    @Resource(lookup="queueReturnKorisnik4")
    static Queue queueReturnKorisnik4;
    
    @Resource(lookup="queueReturnKorisnik5")
    static Queue queueReturnKorisnik5;
    
    @Resource(lookup="queueReturnKorisnik2")
    static Queue queueTest;
    
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU1");
        EntityManager em = emf.createEntityManager();

        JMSContext context = connFactory.createContext();
        JMSConsumer consumer = context.createConsumer(queue1);
        JMSProducer producer = context.createProducer();
        
//        int i=0;
//        while(i < 1000){
//            try {
//                Message msg = consumer.receive();
//                TextMessage txtMsg = (TextMessage) msg;
//                String poruka = txtMsg.getText();
//                System.out.println(poruka);
//            } catch (JMSException ex) {
//                Logger.getLogger(MainP1.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        
        String odgovor = "";
        while(true){
            try {
                TextMessage primljenaPoruka = (TextMessage)consumer.receive();
                String tekstPrimljenePoruke = primljenaPoruka.getText();
                String operacija = primljenaPoruka.getStringProperty("operacija");
                switch (operacija) { 
                    case "napraviGrad":
                        {
                            Grad grad = new Grad();
                            grad.setNaziv(tekstPrimljenePoruke);
                            System.out.println("Napravio sam grad: "  + grad + grad.getNaziv());
                            em.getTransaction().begin();
                            em.persist(grad);
                            em.getTransaction().commit();
                            odgovor = "Uspesno napravljen grad sa nazivom " + tekstPrimljenePoruke;
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "napraviGrad");
                            producer.send(queueReturnGrad1, txtMsg);
                            break;
                        }
                    case "napraviKorisnika":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //korIme,sifra,imeKorisnika,prezime,adresa,imeGrada;
                            Korisnik korisnik = new Korisnik();
                            korisnik.setKorisnickoIme(info[0]);
                            korisnik.setSifra(info[1]);
                            korisnik.setIme(info[2]);
                            korisnik.setPrezime(info[3]);
                            korisnik.setAdresa(info[4]);
                            Query setParameter = em.createNamedQuery("Grad.findByNaziv", Grad.class).setParameter("naziv", info[5]);
                            List resultList = setParameter.getResultList();
                            Grad grad = (Grad) resultList.get(0);
                            korisnik.setIdGra(grad);
                            korisnik.setNovac(0);
                            em.getTransaction().begin();
                            em.persist(korisnik);
                            em.getTransaction().commit();
                            odgovor = "Uspesno napravljen korisnik " + info[0];
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "napraviKorisnika");
                            producer.send(queueReturnKorisnik1, txtMsg);
                            
                            //slanje podistemu 2
                            odgovor = korisnik.getIdKor().toString() + "," + info[0] + "," + info[5] + "," + info[4]; //korId,korIme,imeGrada,adresa
                            txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "napraviKorisnika");
                            producer.send(queue2, txtMsg);
                            
                            //slanje podistemu 3
                            odgovor = korisnik.getIdKor().toString() + "," + info[0];
                            txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "napraviKorisnika");
                            producer.send(queue3, txtMsg);
                            
                            break;
                        }
                    case "dodajNovac":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //korIme,iznos
                            TypedQuery<Korisnik> setParameter = em.createNamedQuery("Korisnik.findByKorisnickoIme", Korisnik.class).setParameter("korisnickoIme", info[0]);
                            List<Korisnik> resultList = setParameter.getResultList();
                            Korisnik korisnik = (Korisnik)resultList.get(0);
                            korisnik.setNovac(korisnik.getNovac()+Double.parseDouble(info[1]));
                            em.getTransaction().begin();
                            em.persist(korisnik);
                            em.getTransaction().commit();
                            odgovor = "Uspesno dodato " + info[1] + " novca korisniku " + info[0] + " trenurno stanje: " + korisnik.getNovac();
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "dodajNovac");
                            producer.send(queueReturnKorisnik2, txtMsg);
                            break;
                        }
                    case "promenaPrebivalista":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //korIme,grad,adresa
                            TypedQuery<Korisnik> upit1 = em.createNamedQuery("Korisnik.findByKorisnickoIme", Korisnik.class).setParameter("korisnickoIme", info[0]);
                            List<Korisnik> korisnici = upit1.getResultList();
                            Korisnik korisnik = (Korisnik)korisnici.get(0);
                            Query upit2 = em.createNamedQuery("Grad.findByNaziv", Grad.class).setParameter("naziv", info[1]);
                            List gradovi = upit2.getResultList();
                            Grad grad = (Grad) gradovi.get(0);
                            korisnik.setIdGra(grad);
                            korisnik.setAdresa(info[2]);
                            em.getTransaction().begin();
                            em.persist(korisnik);
                            em.getTransaction().commit();
                            
                            //slanje u podsistem dva da azurira
                            odgovor = korisnik.getIdKor() + "," + info[1] + "," + info[2];
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "promenaPrebivalista");
                            producer.send(queue2, txtMsg);
                            
                            odgovor = "Uspesno promenjena adresa i grad korisnika " + info[0];
                            txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "promenaPrebivalista");
                            producer.send(queueReturnKorisnik3, txtMsg);
                            break;
                        }
                    case "dohvatiSveGradove":
                        {
                            System.out.println("Tu sam");
                            TypedQuery<Grad> upit = em.createNamedQuery("Grad.findAll", Grad.class);
                            List<Grad> gradovi = upit.getResultList();
                            odgovor = "Dohvaceni gradovi:\n";
                            for(Grad g : gradovi){
                                odgovor += g.getIdGra() + ". " + g.getNaziv() + "\n";
                            }       
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "dohvatiSveGradove");
                            producer.send(queueReturnGrad2, txtMsg);
                            System.out.println("Poslato na queuReturnGrad");
                            break;
                        }
                    case "dohvatiSveKorisnike":
                        {
                            TypedQuery<Korisnik> upit = em.createNamedQuery("Korisnik.findAll", Korisnik.class);
                            List<Korisnik> korisnici = upit.getResultList();
                            odgovor = "Dohvaceni korisnici:\n";
                            for(Korisnik k : korisnici){
                                odgovor += k.getKorisnickoIme() + ", " + k.getIme() + ", " + k.getPrezime() + ", " + k.getAdresa() + ", "
                                        + k.getNovac() + ", " + k.getIdGra().getNaziv() + "\n";
                            }       
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "dohvatiSveKorisnike");
                            producer.send(queueReturnKorisnik4, txtMsg);
                            break;
                        }
                    case "uvecajNovac":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //korIme,iznos
                            TypedQuery<Korisnik> setParameter = em.createNamedQuery("Korisnik.findByKorisnickoIme", Korisnik.class).setParameter("korisnickoIme", info[0]);
                            List<Korisnik> resultList = setParameter.getResultList();
                            Korisnik korisnik = (Korisnik)resultList.get(0);
                            korisnik.setNovac(korisnik.getNovac()+Double.parseDouble(info[1]));
                            em.getTransaction().begin();
                            em.persist(korisnik);
                            em.getTransaction().commit();
                        }
                    case "login":
                    {
                        String[] info = tekstPrimljenePoruke.split(","); //korIme,sifra
                        Query createQuery = em.createQuery("SELECT k FROM Korisnik k WHERE k.korisnickoIme='" + info[0] + "' AND k.sifra='" + info[1] + "'");
                        List resultList = createQuery.getResultList();
                        odgovor = "USPESNO";
                        if(resultList.isEmpty())
                            odgovor = "GRESKA";
                        TextMessage txtMsg = context.createTextMessage(odgovor);
                        txtMsg.setStringProperty("operacija", "login");
                        producer.send(queueReturnKorisnik5, txtMsg);
                        break;
                    }
                    default:
                        break;
                }
            } catch (JMSException ex) {
                Logger.getLogger(MainP1.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
    }
    
}
