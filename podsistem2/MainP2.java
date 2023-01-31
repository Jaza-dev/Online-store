package podsistem2;

import entiteti.Artikal;
import entiteti.Dodaj;
import entiteti.Kategorija;
import entiteti.Korisnik;
import entiteti.Korpa;
import entiteti.Pripada;
import entiteti.Prodaje;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.persistence.*;

public class MainP2 {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    static ConnectionFactory connFactory;
    
    @Resource(lookup="queue2")
    static Queue queue2;
    
    @Resource(lookup="queue3")
    static Queue queue3;
    
    @Resource(lookup="queue1")
    static Queue queue1;
    
    @Resource(lookup="queueReturnArtikal1")
    static Queue queueReturnArtikal1;
    
    @Resource(lookup="queueReturnArtikal2")
    static Queue queueReturnArtikal2;
    
    @Resource(lookup="queueReturnArtikal3")
    static Queue queueReturnArtikal3;
    
    @Resource(lookup="queueReturnArtikal4")
    static Queue queueReturnArtikal4;
    
    @Resource(lookup="queueReturnKorpa1")
    static Queue queueReturnKorpa1;
    
    @Resource(lookup="queueReturnKorpa2")
    static Queue queueReturnKorpa2;
    
    @Resource(lookup="queueReturnKorpa3")
    static Queue queueReturnKorpa3;
    
    @Resource(lookup="queueReturnKategorija1")
    static Queue queueReturnKategorija1;
    
    @Resource(lookup="queueReturnKategorija2")
    static Queue queueReturnKategorija2;
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public static void main(String[] args) throws InterruptedException {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU2");
        EntityManager em = emf.createEntityManager();

        JMSContext context = connFactory.createContext();
        JMSConsumer consumer = context.createConsumer(queue2);
        JMSProducer producer = context.createProducer();
        
        String odgovor = "";
        while(true){
            try {
                TextMessage primljenaPoruka = (TextMessage)consumer.receive();
                String tekstPrimljenePoruke = primljenaPoruka.getText();
                String operacija = primljenaPoruka.getStringProperty("operacija");
                switch (operacija) { 
                    case "napraviKorisnika":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //idKor,korIme,nazivGrada,adresa
                            Korpa korpa = new Korpa();
                            korpa.setUkupnaCena(0);
                            Korisnik korisnik = new Korisnik(Integer.valueOf(info[0]));
                            korisnik.setIdKorp(korpa);
                            korisnik.setKorIme(info[1]);
                            korisnik.setNazivGrada(info[2]);
                            korisnik.setAdresa(info[3]);
                            em.getTransaction().begin();
                            em.persist(korpa);
                            em.persist(korisnik);
                            em.getTransaction().commit();
                            break;
                        }
                    case "napraviKategoriju":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //naziv,nazivPodkategorije
                            Kategorija kategorija = new Kategorija();
                            kategorija.setNaziv(info[0]);

                            Query setParameter = em.createNamedQuery("Kategorija.findByNaziv", Kategorija.class).setParameter("naziv", info[1]);
                            List resultList = setParameter.getResultList();
                            if(!resultList.isEmpty()){
                                Kategorija podkategorija = (Kategorija)resultList.get(0);
                                kategorija.setIdPodKat(podkategorija);
                            }

                            em.getTransaction().begin();
                            em.persist(kategorija);
                            em.getTransaction().commit();

                            odgovor = "Uspesno napravljena kategorija sa nazivom " + info[0];
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "napraviKategoriju");
                            producer.send(queueReturnKategorija1, txtMsg);
                            break;
                        }
                    case "napraviArtikal":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //naziv,opis,cena,popust,korIme,kategorija
                            
                            //dohvatanje korpe koja pripada tom korisniku
                            Query setParameter = em.createNamedQuery("Korisnik.findByKorIme").setParameter("korIme", info[4]);
                            List resultList = setParameter.getResultList();
                            Korisnik korisnik = (Korisnik) resultList.get(0);
                            
                            Artikal artikal = new Artikal();
                            artikal.setNaziv(info[0]);
                            artikal.setOpis(info[1]);
                            artikal.setCena(Double.parseDouble(info[2]));
                            artikal.setPopust(Integer.parseInt(info[3]));
                            
                            Prodaje prodaje = new Prodaje();
                            prodaje.setIdArt(artikal);
                            prodaje.setIdKor(korisnik);
                            
                            em.getTransaction().begin();
                            em.persist(artikal);
                            em.persist(prodaje);
                            em.getTransaction().commit();
                            
                            TypedQuery<Kategorija> createNamedQuery = em.createNamedQuery("Kategorija.findByNaziv", Kategorija.class).setParameter("naziv",info[5]);
                            List<Kategorija> resultList1 = createNamedQuery.getResultList();
                            Kategorija kategorija = resultList1.get(0);
                            
                            Pripada pripada = new Pripada();
                            pripada.setIdArt(artikal);
                            pripada.setIdKat(kategorija);
                            
                            em.getTransaction().begin();
                            em.persist(pripada);
                            em.getTransaction().commit();
                            
                            odgovor = "Uspesno napravljen artikal sa nazivom " + info[0];
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "napraviArtikal");
                            producer.send(queueReturnArtikal1, txtMsg);
                            
                            //poslati podistemu 3
                            odgovor = artikal.getIdArt().toString();
                            txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "napraviArtikal");
                            producer.send(queue3, txtMsg);
                            break;
                        }
                    case "promeniCenuArtikla":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //naziv,novaCena
                            TypedQuery<Artikal> setParameter = em.createNamedQuery("Artikal.findByNaziv", Artikal.class).setParameter("naziv", info[0]);
                            List<Artikal> resultList = setParameter.getResultList();
                            Artikal artikal = (Artikal)resultList.get(0);
                            
                            artikal.setCena(Double.parseDouble(info[1]));
                            
                            em.getTransaction().begin();
                            em.persist(artikal);
                            em.getTransaction().commit();
                            
                            odgovor = "Uspesno promenjena cena artikla " + info[0] + ", nova cena: " + info[1];
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "promeniCenuArtikla");
                            producer.send(queueReturnArtikal2, txtMsg);
                            break;
                        }
                    case "postaviPopustArtiklu":
                        {
                            String[] info = tekstPrimljenePoruke.split(","); //naziv,popust
                            TypedQuery<Artikal> setParameter = em.createNamedQuery("Artikal.findByNaziv", Artikal.class).setParameter("naziv", info[0]);
                            List<Artikal> resultList = setParameter.getResultList();
                            Artikal artikal = (Artikal)resultList.get(0);
                            
                            artikal.setPopust(Integer.parseInt(info[1]));
                            
                            em.getTransaction().begin();
                            em.persist(artikal);
                            em.getTransaction().commit();
                            
                            odgovor = "Uspesno postavljen popust artiklu " + info[0] + ", novi popust: " + info[1];
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "postaviPopustArtiklu");
                            producer.send(queueReturnArtikal3, txtMsg);
                            break;
                        }
                    case "dodajUKorpu":
                        {
                            String[] info = tekstPrimljenePoruke.split(",");//korIme,naziv,kolicina
                            
                            //dohvatanje korpe koja pripada tom korisniku
                            Query setParameter = em.createNamedQuery("Korisnik.findByKorIme").setParameter("korIme", info[0]);
                            List resultList = setParameter.getResultList();
                            Korisnik korisnik = (Korisnik) resultList.get(0);
                            
                            Korpa korpa = korisnik.getIdKorp();
                            
                            setParameter = em.createNamedQuery("Artikal.findByNaziv").setParameter("naziv", info[1]);
                            resultList = setParameter.getResultList();
                            Artikal artikal = (Artikal) resultList.get(0);
                            
                            setParameter = em.createQuery("SELECT d FROM Dodaj d WHERE d.idArt.idArt=" + artikal.getIdArt() + " AND d.idKorp.idKorp=" + 
                            korpa.getIdKorp());
                            resultList = setParameter.getResultList();
                            Dodaj dodaj;
                            if(resultList.isEmpty()){
                                dodaj = new Dodaj();
                                dodaj.setKolicina(Integer.parseInt(info[2]));
                                dodaj.setIdArt(artikal);
                                dodaj.setIdKorp(korpa);
                            }else{
                                dodaj = (Dodaj) resultList.get(0);
                                dodaj.setKolicina(dodaj.getKolicina() + Integer.parseInt(info[2]));
                            }
                            korpa.setUkupnaCena(korpa.getUkupnaCena() + artikal.getCena() * Integer.parseInt(info[2]));
                            
                            em.getTransaction().begin();
                            em.persist(dodaj);
                            em.persist(korpa);
                            em.getTransaction().commit();
                            
                            odgovor = "Uspesno dodato " + info[2] + " " + info[1] + ", trenutna vrednost korpe " + korpa.getUkupnaCena();
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "dodajUKorpu");
                            producer.send(queueReturnKorpa1, txtMsg);
                            break;
                        }
                    case "obrisiIzKorpe":
                        {
                            String[] info = tekstPrimljenePoruke.split(",");//korIme,naziv,kolicina
                            
                            //dohvatanje korpe koja pripada tom korisniku
                            Query setParameter = em.createNamedQuery("Korisnik.findByKorIme").setParameter("korIme", info[0]);
                            List resultList = setParameter.getResultList();
                            Korisnik korisnik = (Korisnik) resultList.get(0);
                            
                            Korpa korpa = korisnik.getIdKorp();
                            
                            setParameter = em.createNamedQuery("Artikal.findByNaziv").setParameter("naziv", info[1]);
                            resultList = setParameter.getResultList();
                            Artikal artikal = (Artikal) resultList.get(0);
                            
                            setParameter = em.createQuery("SELECT d FROM Dodaj d WHERE d.idArt.idArt=" + artikal.getIdArt() + " AND d.idKorp.idKorp=" + 
                            korpa.getIdKorp());
                            resultList = setParameter.getResultList();
                            Dodaj dodaj = (Dodaj) resultList.get(0);
                            
                            int kolicina;
                            if(Integer.parseInt(info[2]) > dodaj.getKolicina())
                                kolicina = dodaj.getKolicina();
                            else
                                kolicina = Integer.parseInt(info[2]);
                            dodaj.setKolicina(dodaj.getKolicina() - kolicina);
                            
                            korpa.setUkupnaCena(korpa.getUkupnaCena() - artikal.getCena()*kolicina);
                            
                            em.getTransaction().begin();
                            em.persist(dodaj);
                            em.persist(korpa);
                            em.getTransaction().commit();
                            
                            if(dodaj.getKolicina() == 0){
                                em.getTransaction().begin();
                                em.createQuery("DELETE FROM Dodaj d WHERE d.idDod=" + dodaj.getIdDod()).executeUpdate();
                                em.getTransaction().commit();
                            }
                            
                            odgovor = "Uspesno obrisano " + info[2] + " " + info[1] + ", trenutna vrednost korpe " + korpa.getUkupnaCena();
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "obrisiIzKorpe");
                            producer.send(queueReturnKorpa2, txtMsg);
                            break;
                        }
                    case "dohvatiSveKategorije":
                        {
                            TypedQuery<Kategorija> upit = em.createNamedQuery("Kategorija.findAll", Kategorija.class);
                            List<Kategorija> kategorije = upit.getResultList();
                            odgovor = "Dohvacene kategorije:\n";
                            for(Kategorija k : kategorije){
                                odgovor += k.getIdKat()+ ". " + k.getNaziv()+ ", " + (k.getIdPodKat() != null ? k.getIdPodKat().getNaziv() : "null") + "\n";
                            }       
                            System.out.println(odgovor);
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "dohvatiSveKategorije");
                            producer.send(queueReturnKategorija2, txtMsg);
                            break;
                        }
                    case "dohvatiSveKojeProdaje":
                        {
                            //dohvatanje korpe koja pripada tom korisniku
                            Query setParameter = em.createNamedQuery("Korisnik.findByKorIme").setParameter("korIme", tekstPrimljenePoruke);
                            List resultList = setParameter.getResultList();
                            Korisnik korisnik = (Korisnik) resultList.get(0);
                            
                            Query createQuery = em.createQuery("SELECT p.idArt FROM Prodaje p WHERE p.idKor.idKor=" + korisnik.getIdKor());
                            List<Artikal> listaArtikala = createQuery.getResultList();
                            
                            odgovor = "Artikli koje prodaje " + tekstPrimljenePoruke + ":\n";
                            for(Artikal artikal : listaArtikala){
                                odgovor += artikal.getIdArt() + ". " + artikal.getNaziv() + " " 
                                        + artikal.getOpis() + " " + artikal.getCena()+ " "
                                        + artikal.getPopust() + "\n";
                            }
                            System.out.println(odgovor);
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "dohvatiSveKojeProdaje");
                            producer.send(queueReturnArtikal4, txtMsg);
                            break;
                        }
                    case "dohvatiKorpuKorisnika":
                        {
                            //dohvatanje korpe koja pripada tom korisniku
                            Query setParameter = em.createNamedQuery("Korisnik.findByKorIme").setParameter("korIme", tekstPrimljenePoruke);
                            List resultList = setParameter.getResultList();
                            Korisnik korisnik = (Korisnik) resultList.get(0);
                            
                            Korpa korpa = korisnik.getIdKorp();
                            
                            Query createQuery = em.createQuery("SELECT d FROM Dodaj d WHERE d.idKorp.idKorp=" + korpa.getIdKorp());
                            List<Dodaj> listaDodaj = createQuery.getResultList();
                            
                            odgovor = "Korpa korisnika " + tekstPrimljenePoruke +":\n";
                            if(!listaDodaj.isEmpty()){
                                int i = 1;
                                for(Dodaj dodaj : listaDodaj){
                                    odgovor += i + ". " + dodaj.getIdArt().getNaziv() + " , kolicina: " + dodaj.getKolicina() + "\n";
                                }
                            }else
                                 odgovor = "Korpa korisnia " + tekstPrimljenePoruke + " je prazna.";
                            
                            System.out.println(odgovor);
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "dohvatiKorpuKorisnika");
                            producer.send(queueReturnKorpa3, txtMsg);
                            break;
                        }
                    case "dohvatiKorpuKorisnikaZaPlacanje":
                        {
                            //dohvatanje korpe koja pripada tom korisniku
                            Query setParameter = em.createNamedQuery("Korisnik.findByKorIme").setParameter("korIme", tekstPrimljenePoruke);
                            List resultList = setParameter.getResultList();
                            Korisnik korisnik = (Korisnik) resultList.get(0);
                            
                            odgovor = tekstPrimljenePoruke + "," + korisnik.getNazivGrada() + "," + korisnik.getAdresa();
                            
                            Korpa korpa = korisnik.getIdKorp();
                            
                            Query createQuery = em.createQuery("SELECT d FROM Dodaj d WHERE d.idKorp.idKorp=" + korpa.getIdKorp());
                            List<Dodaj> listaDodaj = createQuery.getResultList();
                            
                            //razcuanje ukupne cene sa popustom artikala
                            Artikal artikal;
                            String artikli = "";
                            double ukupnaCena = 0, cenaSaPopustom;
                            if(!listaDodaj.isEmpty()){
                                for(Dodaj dodaj : listaDodaj){
                                    createQuery = em.createQuery("SELECT a FROM Artikal a WHERE a.idArt=" + dodaj.getIdArt().getIdArt());
                                    resultList = createQuery.getResultList();
                                    artikal = (Artikal) resultList.get(0);
                                    
                                    System.out.println("Cena artikla bez popusta: " + artikal.getCena());
                                    cenaSaPopustom = artikal.getCena() * (1-artikal.getPopust()/100.0);
                                    System.out.println("Cena artikla sa popustom: " + cenaSaPopustom);
                                    
                                    ukupnaCena += cenaSaPopustom * dodaj.getKolicina();
                                    artikli += artikal.getIdArt() + "-" + cenaSaPopustom + "-" + dodaj.getKolicina() + "|";
                                    
                                    //pronalazenje prodavca kako bih uvecao njegov novac
                                    Query createQuery1 = em.createQuery("SELECT p FROM Prodaje p WHERE p.idArt.idArt=" + artikal.getIdArt());
                                    List resultList1 = createQuery1.getResultList();
                                    Prodaje prodaje = (Prodaje) resultList1.get(0);
                                    Korisnik prodavac = prodaje.getIdKor();
                                    
                                    TextMessage txtMsg = context.createTextMessage(prodavac.getKorIme()+ "," + (cenaSaPopustom*dodaj.getKolicina()));
                                    txtMsg.setStringProperty("operacija", "uvecajNovac");
                                    producer.send(queue1, txtMsg);
                                    
                                }
                                odgovor += "," + ukupnaCena + "," + artikli;
                            }else
                                 odgovor = "Korpa korisnia " + tekstPrimljenePoruke + " je prazna.";
                            
                            System.out.println(odgovor);
                            TextMessage txtMsg = context.createTextMessage(odgovor);
                            txtMsg.setStringProperty("operacija", "izvrsiPlacanje");
                            producer.send(queue3, txtMsg);
                            break;
                        }
                    case "obrisiSveIzKorpe":
                    {
                        //dohvatanje korpe koja pripada tom korisniku
                        Query setParameter = em.createNamedQuery("Korisnik.findByKorIme").setParameter("korIme", tekstPrimljenePoruke);
                        List resultList = setParameter.getResultList();
                        Korisnik korisnik = (Korisnik) resultList.get(0);

                        Korpa korpa = korisnik.getIdKorp();
                        em.getTransaction().begin();
                        em.createQuery("DELETE FROM Dodaj d WHERE d.idKorp.idKorp=" + korpa.getIdKorp()).executeUpdate();
                        em.getTransaction().commit();
                        
                        korpa.setUkupnaCena(0);
                        em.getTransaction().begin();
                        em.persist(korpa);
                        em.getTransaction().commit();
                        break;
                    }
                    case "promenaPrebivalista":
                    {
                        String[] info = tekstPrimljenePoruke.split(",");//idKor,grad,adresa
                        TypedQuery<Korisnik> setParameter = em.createNamedQuery("Korisnik.findByIdKor", Korisnik.class).setParameter("idKor", Integer.valueOf(info[0]));
                        List<Korisnik> resultList = setParameter.getResultList();
                        Korisnik korisnik = resultList.get(0);
                        
                        korisnik.setAdresa(info[2]);
                        korisnik.setNazivGrada(info[1]);
                        em.getTransaction().begin();
                        em.persist(korisnik);
                        em.getTransaction().commit();
                    }
                    default:
                        break;
                }
            } catch (JMSException ex) {
                Logger.getLogger(MainP2.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
    }
    
}
