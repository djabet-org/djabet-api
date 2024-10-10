import org.jsoup.*;

public class Jsoup {
    public static void main(String[] args) {
        // downloading the target website with an HTTP GET request
Document doc = Jsoup
.connect("https://bra.1xbet.com/br/line/football/1268397-brazil-campeonato-brasileiro-serie-a")
.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36")
.get();

System.out.println(doc);
    }
    
}