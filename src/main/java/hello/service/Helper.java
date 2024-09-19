package hello.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class Helper {

    private final static List<String> _excludedBookmakers = Arrays.asList(
            "matchbook",
            "unibet_eu",
            "unibet_uk",
            "virginbet",
            "grosvenor",
            "mrgreen",
            "coral",
            "leovegas",
            "betanysports",
            "tipico",
            "coolbet",
            "nordicbet",
            "gtbets",
            "everygame",
            "livescorebet",
            "livescorebet_eu",
            "betonlineag",
            "mybookieag",
            "paddypower",
            "casumo",
            "betway",
            "williamhill",
            "betonlineag",
            "betclic",
            "tipico_de",
            "livescorebet_eu",
            "betmgm",
            "sport888",
            "betfair_ex_uk",
            "betfair_ex_eu");

            public static List<String> getExcludedBookmakers() {
                return _excludedBookmakers;
            }

    public static String quando(String horario) {
            try {
                Instant dateTime = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss'Z'").parse(horario).toInstant();
                Instant date = new SimpleDateFormat("yyyy-MM-dd").parse(horario).toInstant();
        long days = ChronoUnit.DAYS.between(Instant.now(), date);

        if (days == 0) {
            return "Hoje ainda! as " + dateTime.atZone(ZoneOffset.UTC).toLocalTime() + " - "+date.toString();
        } else if (days == 1) {
            return "Amanha";
        } else
            return "Em " + days + " dias";
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "";
            }
    }

    public static boolean didStarted(String horario) {
        try {
            Instant date = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss'Z'").parse(horario).toInstant();
            return date.isBefore(Instant.now());
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean happensInTwoDays(String dateString) {
        try {
            Instant inTwoDays = Instant.now().plus(2, ChronoUnit.DAYS);
            Instant date = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss'Z'").parse(dateString).toInstant();
            return date.isBefore(inTwoDays);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    public static double calculateBetAmount(double probabilityOfWinning, double bankroll, double odd){
        double b = odd-1;
        double p = probabilityOfWinning/100;
        double q = 1-p;
        double k = (b*p-q)/b;

        double fractionalKelly = 0.20;

        return k*fractionalKelly*bankroll;
    }
}
