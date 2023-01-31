package klijent;

import javax.ws.rs.client.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.ws.rs.core.MediaType;

public class Klijent {
    
    private static final String url = "http://localhost:8080/Server/server/";
    private static Client klijent = ClientBuilder.newClient();
    private static String korImeLogin;
    private static String sifraLogin;

    private static void ispisMenia() {
        System.out.println("0. Login");
        System.out.println("1. Kreiranje grada");
        System.out.println("2. Kreiranje korisnika");
        System.out.println("3. Dodavanje novca korisniku");
        System.out.println("4. Promena adrese i grada za korisnika");
        System.out.println("5. Kreiranje kategorije");
        System.out.println("6. Kreiranje artikla");
        System.out.println("7. Menjanje cene artikla");
        System.out.println("8. Postavljanje popusta za artikal");
        System.out.println("9. Dodavanje artikala u određenoj količini u korpu");
        System.out.println("10. Brisanje artikla u određenoj količini iz korpe");
        System.out.println("11. Plaćanje, koje obuhvata kreiranje transakcije, kreiranje narudžbine sa njenim stavkama, i\n"
                + "brisanje sadržaja iz korpe");
        System.out.println("12. Dohvatanje svih gradova");
        System.out.println("13. Dohvatanje svih korisnika");
        System.out.println("14. Dohvatanje svih kategorija");
        System.out.println("15. Dohvatanje svih artikala koje prodaje korisnik koji je poslao zahtev");
        System.out.println("16. Dohvatanje sadržaja korpe korisnika koji je poslao zahtev");
        System.out.println("17. Dohvatanje svih narudžbina korisnika koji je poslao zahtev");
        System.out.println("18. Dohvatanje svih narudžbina");
        System.out.println("19. Dohvatanje svih transakcija");
        System.out.println("20. Ispis menija");
    }
    
    private static void login(){
        
        while(true){
            System.out.println("------ LOGIN ------");
            Scanner input = new Scanner(System.in).useDelimiter("\n");
            System.out.print("Korisnicko ime: ");
            korImeLogin = input.next();
            System.out.print("Sifra: ");
            sifraLogin = input.next();
            
            String poruka = korImeLogin + "," + sifraLogin;
            String odgovor = klijent.target(url+"korisnik/login").path("{info}").resolveTemplate("info", poruka).request().get(String.class);
            if(odgovor.equals("USPESNO")){
                System.out.println("Dobro dosli " + korImeLogin + "!");
                return;
            }else
                System.out.println("Pogresko korisnicko ime ili lozinka!");
        }
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in).useDelimiter("\n");
        ispisMenia();
        String poruka,odgovor,korIme,sifra,imeKorisnika,prezime,adresa,imeGrada,naziv,nazivPodKategorije,opis,kategorija;
        double iznos, cena;
        int popust, kolicina;
        try {
            login();
            while (true) {
                System.out.print("Zeljena operacija: ");
                int option = input.nextInt();
                switch (option) {
                    case -1:
                        return;
                    case 0:
                        //login
                        login();
                        break;
                    case 1:
                        System.out.print("Ime grada: ");
                        imeGrada = input.next();
                        odgovor = klijent.target(url+"grad/napravi").path("{info}").resolveTemplate("info", imeGrada).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 2:
                        System.out.print("Korisnicko ime: ");
                        korIme = input.next();
                        System.out.print("Sifra: ");
                        sifra = input.next();
                        System.out.print("Ime: ");
                        imeKorisnika = input.next();
                        System.out.print("Prezime: ");
                        prezime = input.next();
                        System.out.print("Adresa: ");
                        adresa = input.next();
                        System.out.print("Ime grada: ");
                        imeGrada = input.next();
                        poruka = korIme+","+sifra+","+imeKorisnika+","+prezime+","+adresa+","+imeGrada;
                        odgovor = klijent.target(url+"korisnik/napravi").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 3:
                        System.out.print("Korisnicko ime: ");
                        korIme = input.next();
                        System.out.print("Iznos: ");
                        iznos = input.nextDouble();
                        poruka = korIme+","+iznos;
                        odgovor = klijent.target(url+"korisnik/dodajNovac").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 4:
                        System.out.print("Korisnicko ime: ");
                        korIme = input.next();
                        System.out.print("Ime grada: ");
                        imeGrada = input.next();
                        System.out.print("Adresa: ");
                        adresa = input.next();
                        poruka = korIme+","+imeGrada+","+adresa;
                        odgovor = klijent.target(url+"korisnik/promenaPrebivalista").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 5:
                        System.out.print("Naziv: ");
                        naziv = input.next();
                        System.out.print("Naziv podkategorije: ");
                        nazivPodKategorije = input.next();
                        poruka = naziv+","+nazivPodKategorije;
                        odgovor = klijent.target(url+"kategorija/napravi").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 6:
                        System.out.print("Naziv artikla: ");
                        naziv = input.next();
                        System.out.print("Opis artikla: ");
                        opis = input.next();
                        System.out.print("Cena artikla: ");
                        cena = input.nextDouble();
                        System.out.print("Popust artikla: ");
                        popust = input.nextInt();
                        System.out.print("Korisnico ime prodavca: ");
                        korIme = input.next();
                        System.out.print("Kategorija artikla: ");
                        kategorija = input.next();
                        poruka = naziv+","+opis+","+cena+","+popust + "," + korIme+ "," + kategorija;
                        odgovor = klijent.target(url+"artikal/napravi").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 7:
                        System.out.print("Naziv artikla: ");
                        naziv = input.next();
                        System.out.print("Nova cena artikla: ");
                        cena = input.nextDouble();
                        poruka = naziv+","+cena;
                        odgovor = klijent.target(url+"artikal/promeniCenu").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 8:
                        System.out.print("Naziv artikla: ");
                        naziv = input.next();
                        System.out.print("Popust: ");
                        popust = input.nextInt();
                        poruka = naziv+","+popust;
                        odgovor = klijent.target(url+"artikal/postaviPopust").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 9:
                        System.out.print("Korisnicko ime korisnik kome se u korpu dodaju artikli: ");
                        korIme = input.next();
                        System.out.print("Artikal: ");
                        naziv = input.next();
                        System.out.print("Kolicina: ");
                        kolicina = input.nextInt();
                        poruka = korIme+","+naziv+","+kolicina;
                        odgovor = klijent.target(url+"korpa/dodaj").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 10:
                        System.out.print("Korisnicko ime korisnik kome se brisu iz korpe artikli: ");
                        korIme = input.next();
                        System.out.print("Artikal: ");
                        naziv = input.next();
                        System.out.print("Kolicina: ");
                        kolicina = input.nextInt();
                        poruka = korIme+","+naziv+","+kolicina;
                        odgovor = klijent.target(url+"korpa/obrisi").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 11:
                        System.out.print("Korisnicko ime: ");
                        korIme = input.next();
                        poruka = korIme;
                        odgovor = klijent.target(url+"placanje/plati").path("{info}").resolveTemplate("info", poruka).request().post(Entity.entity("!", MediaType.APPLICATION_JSON), String.class);
                        System.out.println(odgovor);
                        break;
                    case 12:
                        odgovor = klijent.target(url+"grad/dohvatiSve").request().get(String.class);
                        System.out.println(odgovor);
                        break;
                    case 13:
                        odgovor = klijent.target(url+"korisnik/dohvatiSve").request().get(String.class);
                        System.out.println(odgovor);
                        break;
                    case 14:
                        odgovor = klijent.target(url+"kategorija/dohvatiSve").request().get(String.class);
                        System.out.println(odgovor);
                        break;
                    case 15:
                        poruka = korImeLogin;
                        odgovor = klijent.target(url+"artikal/dohvatiSveKojeProdaje").path("{info}").resolveTemplate("info", poruka).request().get(String.class);
                        System.out.println(odgovor);
                        break;
                    case 16:
                        poruka = korImeLogin;
                        odgovor = klijent.target(url+"korpa/dohvatiKorpuKorisnika").path("{info}").resolveTemplate("info", poruka).request().get(String.class);
                        System.out.println(odgovor);
                        break;
                    case 17:
                        poruka = korImeLogin;
                        odgovor = klijent.target(url+"narudzbina/dohvatiNarudzbineKorisnika").path("{info}").resolveTemplate("info", poruka).request().get(String.class);
                        System.out.println(odgovor);
                        break;
                    case 18:
                        odgovor = klijent.target(url+"narudzbina/dohvatiSve").request().get(String.class);
                        System.out.println(odgovor);
                        break;
                    case 19:
                        odgovor = klijent.target(url+"transakcija/dohvatiSve").request().get(String.class);
                        System.out.println(odgovor);
                        break;
                    case 20:
                        ispisMenia();
                        break;
                    default:
                        klijent.target(url+"grad/iprazni").request().get(String.class);
                        System.out.println("Ne postojeca opcija!");
                        break;
                }
            }
        }catch(InputMismatchException e){
            System.out.println("Neophodno je uneti ceo broj!");
        }
    }
}
