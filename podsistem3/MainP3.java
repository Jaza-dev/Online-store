package podsistem3;

import entiteti.Artikal;
import entiteti.Korisnik;
import entiteti.Narudzbina;
import entiteti.Sastojise;
import entiteti.Stavka;
import entiteti.Transakcija;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;
import javax.persistence.*;

public class MainP3 {
    
    @Resource(lookup="jms/__defaultConnectionFactory")
    static ConnectionFactory connFactory;
    
    @Resource(lookup="queue1")
    static Queue queue1;
    
    @Resource(lookup="queue2")
    static Queue queue2;
    
    @Resource(lookup="queue3")
    static Queue queue3;
    
    @Resource(lookup="queueReturnTransakcija")
    static Queue queueReturnTransakcija;
    
    @Resource(lookup="queueReturnPlacanje")
    static Queue queueReturnPlacanje;
    
    @Resource(lookup="queueReturnNarudzbina1")
    static Queue queueReturnNarudzbina1;
    
    @Resource(lookup="queueReturnNarudzbina2")
    static Queue queueReturnNarudzbina2;
    
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public static void main(String[] args) throws InterruptedException {
        
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PU3");
        EntityManager em = emf.createEntityManager();

        JMSContext context = connFactory.createContext();
        JMSConsumer consumer = context.createConsumer(queue3);
        JMSProducer producer = context.createProducer();
        
        String odgovor = "";
        while(true){
            try {
                TextMessage primljenaPoruka = (TextMessage)consumer.receive();
                String tekstPrimljenePoruke = primljenaPoruka.getText();
                String operacija = primljenaPoruka.getStringProperty("operacija");
                switch (operacija) {
                    case "plati":
                    {
                        TextMessage txtMsg = context.createTextMessage(tekstPrimljenePoruke);
                        txtMsg.setStringProperty("operacija", "dohvatiKorpuKorisnikaZaPlacanje");
                        producer.send(queue2, txtMsg);
                        break;
                    }
                    case "izvrsiPlacanje":
                    {
                        String korpa = tekstPrimljenePoruke;
                        if(!korpa.equals("Korpa korisnia " + tekstPrimljenePoruke + " je prazna.")){
                            String[] info = korpa.split(",");//korIme,nazivGrada,Adresa,totalnaCena,idArt-CenaSaPopustom-kolicina|...
                            String[] artikli = info[4].split("\\|");
                            //kreiranje narudzbine
                            Narudzbina narudzbina = new Narudzbina();
                            narudzbina.setUkupnaCena(Double.parseDouble(info[3]));
                            narudzbina.setVremeKreiranja(new Date());
                            narudzbina.setAdresa(info[2]);
                            narudzbina.setNazivGrada(info[1]);
                            em.getTransaction().begin();
                            em.persist(narudzbina);
                            em.getTransaction().commit();

                            //kreiranje Stavki i veza SastojiSe
                            Stavka stavka;
                            Sastojise sastojiSe;
                            for(String artikal : artikli){
                                //slucaj poslednjeg artikla koji je prazan
                                if(artikal.length() == 0)
                                    break;
                                String[] podaciOArtiklu = artikal.split("-");
                                if(podaciOArtiklu[2].equals("0"))
                                    continue;
                                //pravljenje Stavke
                                stavka = new Stavka();
                                stavka.setKolicina(Integer.parseInt(podaciOArtiklu[2]));
                                stavka.setCenaArtikla(Double.parseDouble(podaciOArtiklu[1]));
                                Query createQuery = em.createQuery("SELECT a FROM Artikal a WHERE a.idArt=" + podaciOArtiklu[0]);
                                List resultList = createQuery.getResultList();
                                Artikal a = (Artikal) resultList.get(0);
                                stavka.setIdArt(a);
                                stavka.setIdNar(narudzbina);
                                em.getTransaction().begin();
                                em.persist(stavka);
                                em.getTransaction().commit();

                                //pravljenje SastojiSe
                                sastojiSe = new Sastojise();
                                sastojiSe.setIdNar(narudzbina);
                                sastojiSe.setIdSta(stavka);
                                em.getTransaction().begin();
                                em.persist(sastojiSe);
                                em.getTransaction().commit();
                            }

                            Transakcija transakcija = new Transakcija();
                            transakcija.setPlaceno(Double.parseDouble(info[3]));
                            transakcija.setVreme(new Date());

                            //dohvatanje korisnika koji placa
                            TypedQuery<Korisnik> upit1 = em.createNamedQuery("Korisnik.findByKorIme", Korisnik.class).setParameter("korIme", info[0]);
                            List<Korisnik> korisnici = upit1.getResultList();
                            Korisnik korisnik = (Korisnik)korisnici.get(0);

                            transakcija.setIdKor(korisnik);
                            transakcija.setIdNar(narudzbina);

                            em.getTransaction().begin();
                            em.persist(transakcija);
                            em.getTransaction().commit();
                            
                            //slanje podsistemu 1 da umanji kolicinu novca na racunu korisnika koji placa
                            TextMessage txtMsg = context.createTextMessage(info[0] + "," + -1 * Double.parseDouble(info[3])); //KorIme,novac
                            txtMsg.setStringProperty("operacija", "uvecajNovac");
                            producer.send(queue1, txtMsg);

                            odgovor = "Uspesno napravljena porudzbina i transakcija za korisnika " + info[0];
                            
                            //obrisi sve iz korpe korisnika
                            txtMsg = context.createTextMessage(info[0]);
                            txtMsg.setStringProperty("operacija", "obrisiSveIzKorpe");
                            producer.send(queue2, txtMsg);

                        }else{
                            odgovor = "Korpa je prazna, nije napravljena porudzbina";
                        }
                        
                        TextMessage txtMsg = context.createTextMessage(odgovor);
                        txtMsg.setStringProperty("operacija", "plati");
                        producer.send(queueReturnPlacanje, txtMsg);
                        break;
                    }
                    case "dohvatiNarudzbineKorisnika":
                    {
                        TypedQuery<Korisnik> setParameter = em.createNamedQuery("Korisnik.findByKorIme", Korisnik.class).setParameter("korIme", tekstPrimljenePoruke);
                        List<Korisnik> resultList = setParameter.getResultList();
                        Korisnik korisnik = resultList.get(0);
                        
                        TypedQuery<Narudzbina> setParameter2 = em.createQuery("SELECT n FROM Transakcija t JOIN t.idNar n WHERE t.idKor.idKor=" + korisnik.getIdKor(), Narudzbina.class);
                        List<Narudzbina> resultList2 = setParameter2.getResultList();
                        
                        odgovor = "Sve narudzbine korisnika " + tekstPrimljenePoruke + ":\n";
                        for(Narudzbina narudzbina : resultList2){
                            odgovor += narudzbina.getIdNar()+ ". " + narudzbina.getUkupnaCena()+ " , " + narudzbina.getVremeKreiranja() + " , " + narudzbina.getAdresa()+ " , " + narudzbina.getNazivGrada()+ "\n";
                        }
                        
                        TextMessage txtMsg = context.createTextMessage(odgovor);
                        txtMsg.setStringProperty("operacija", "dohvatiNarudzbineKorisnika");
                        producer.send(queueReturnNarudzbina1, txtMsg);
                        break;
                    }
                    case("dohvatiSveNarudzbine"):
                    {
                        TypedQuery<Narudzbina> createQuery = em.createNamedQuery("Narudzbina.findAll",Narudzbina.class);
                        List<Narudzbina> resultList1 = createQuery.getResultList();
                        
                        odgovor = "Sve narduzbine:\n";
                        for(Narudzbina narudzbine : resultList1){
                            odgovor += narudzbine.getIdNar()+ ". " + narudzbine.getUkupnaCena()+ " , " + narudzbine.getVremeKreiranja() + " , " + narudzbine.getAdresa()+ " , " + narudzbine.getNazivGrada()+ "\n";
                        }
                        System.out.println(odgovor);
                        
                        TextMessage txtMsg = context.createTextMessage(odgovor);
                        txtMsg.setStringProperty("operacija", "dohvatiSveNarudzbine");
                        producer.send(queueReturnNarudzbina2, txtMsg);
                        break;
                    }
                    case("dohvatiSveTransakcije"):
                    {
                        TypedQuery<Transakcija> createQuery = em.createNamedQuery("Transakcija.findAll",Transakcija.class);
                        List<Transakcija> resultList1 = createQuery.getResultList();
                        
                        odgovor = "Sve transakcije:\n";
                        for(Transakcija transakcija : resultList1){
                            odgovor += transakcija.getIdTran() + ". " + transakcija.getPlaceno() + " , " + transakcija.getVreme() + " , " + transakcija.getIdKor().getKorIme() + " , " + transakcija.getIdNar().getIdNar() + "\n";
                        }
                        System.out.println(odgovor);
                        
                        TextMessage txtMsg = context.createTextMessage(odgovor);
                        txtMsg.setStringProperty("operacija", "dohvatiSveTransakcije");
                        producer.send(queueReturnTransakcija, txtMsg);
                        break;
                    }
                    case("napraviKorisnika"):
                    {
                        String[] info = tekstPrimljenePoruke.split(",");//idKor,korIme
                        Korisnik korisnik = new Korisnik();
                        korisnik.setIdKor(Integer.valueOf(info[0]));
                        korisnik.setKorIme(info[1]);
                        
                        em.getTransaction().begin();
                        em.persist(korisnik);
                        em.getTransaction().commit();
                       break;
                    }
                    case("napraviArtikal"):
                    {
                        Artikal artikal = new Artikal();
                        artikal.setIdArt(Integer.valueOf(tekstPrimljenePoruke));

                        em.getTransaction().begin();
                        em.persist(artikal);
                        em.getTransaction().commit();
                        break;
                    }
                    default:
                        break;
                }
            } catch (JMSException ex) {
                Logger.getLogger(MainP3.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        }
    }
    
}
