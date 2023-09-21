import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Main {
    record Weather(String data){}
    record Quotation(String server, int amount) {}

    record Travel(Quotation quote, Weather weather) {}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /*Supplier<Weather> w1 = () -> getWeatherA();
        Supplier<Weather> w2 = () -> getWeatherB();
        Supplier<Weather> w3 = () -> getWeatherC();

        CompletableFuture<Weather> cf1 = CompletableFuture.supplyAsync(w1);
        CompletableFuture<Weather> cf2 = CompletableFuture.supplyAsync(w2);
        CompletableFuture<Weather> cf3 = CompletableFuture.supplyAsync(w3);

        CompletableFuture<Object> weatherCF = CompletableFuture.anyOf(cf1, cf2, cf3);

        weatherCF.thenAccept(System.out::println);*/

        /*Supplier<Quotation> q1 = () -> getQuoteA();
        Supplier<Quotation> q2 = () -> getQuoteB();
        Supplier<Quotation> q3 = () -> getQuoteC();

        CompletableFuture<Quotation> cf1 = CompletableFuture.supplyAsync(q1);
        CompletableFuture<Quotation> cf2 = CompletableFuture.supplyAsync(q2);
        CompletableFuture<Quotation> cf3 = CompletableFuture.supplyAsync(q3);

        CompletableFuture<Void> done = CompletableFuture.allOf(cf1,cf2,cf3);

        Quotation quote = done.thenApply(v -> Stream.of(cf1,cf2,cf3).map(CompletableFuture::join).min(Comparator.comparing(Quotation::amount)).orElseThrow()).join();*/

        var quotationCF = CompletableFuture.supplyAsync(() -> getQuoteA());
        var weatherCF = CompletableFuture.supplyAsync(()-> getWeatherA());

        //var travelPage = new Travel(quotationCF.get(), weatherCF.get());
        var done = CompletableFuture.allOf(quotationCF, weatherCF);

        var travel = quotationCF.thenCompose(
                quotation -> weatherCF.thenApply(
                        weather -> new Travel(quotation, weather)
                )).join();
        System.out.println(travel);
    }

    public static Weather getWeatherA() {
        return new Weather("Weather A : " + 20 + "C°");
    }

    public static Weather getWeatherB() {
        return new Weather("Weather B : " + 25 + "C°");
    }

    public static Weather getWeatherC() {
        return new Weather("Weather C : " + 30 + "C°");
    }

    public static Quotation getQuoteA() { return new Quotation("Marcus", 1); }
    public static Quotation getQuoteB() { return new Quotation("Seneca", 2); }
    public static Quotation getQuoteC() { return new Quotation("Epictetus", 3); }
}