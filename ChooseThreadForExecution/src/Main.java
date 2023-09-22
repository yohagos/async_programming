import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

public class Main {
    record Quotation(String server, int amount) {}

    record Weather(String server, String weather) {}

    record TravelPage(Quotation quotation, Weather weather) {}

    public static void main(String[] args) {
        ThreadFactory quotationThread = Executors.defaultThreadFactory();
        ThreadFactory weatherThread = Executors.defaultThreadFactory();
        ThreadFactory travelThread = Executors.defaultThreadFactory();

        /*var quoteThread = new Thread();
        quoteThread.setName("Quotation");
        quoteThread.start();
        var weathThread = new Thread();
        weathThread.setName("Weather");
        weathThread.start();
        var minThread = new Thread();
        minThread.setName("Min");
        minThread.start();*/

        run(quotationThread,weatherThread,travelThread);
    }

    public static void run(ThreadFactory quotationThread, ThreadFactory weatherThread, ThreadFactory travelThread) {
        ExecutorService quotationExecutor = Executors.newFixedThreadPool(4, quotationThread);
        ExecutorService weatherExecutor = Executors.newFixedThreadPool(4, weatherThread);
        ExecutorService travelExecutor = Executors.newFixedThreadPool(4, travelThread);

        Random random = new Random();

        List<Supplier<Weather>> weatherTasks = buildWeatherTasks(random);
        List<Supplier<Quotation>> quotationTasks = buildQuotationTasks(random);

        List<CompletableFuture<Weather>> weatherCFs = new ArrayList<>();
        for (Supplier<Weather> task: weatherTasks) {
            CompletableFuture<Weather> weather = CompletableFuture.supplyAsync(task, weatherExecutor);
            System.out.println(Thread.currentThread());
            weatherCFs.add(weather);
        }

        CompletableFuture<Weather> anyOfWeather =
                CompletableFuture.anyOf(weatherCFs.toArray(CompletableFuture[]::new))
                                    .thenApply(weather -> (Weather) weather);

        List<CompletableFuture<Quotation>> quotationCFs = new ArrayList<>();
        for (Supplier<Quotation> task: quotationTasks) {
            CompletableFuture<Quotation> quote = CompletableFuture.supplyAsync(task, quotationExecutor);
            System.out.println(Thread.currentThread());
            quotationCFs.add(quote);
        }

        CompletableFuture<Void> allOfQuotation =
                CompletableFuture.allOf(quotationCFs.toArray(CompletableFuture[]::new));

        CompletableFuture<Quotation> bestQuotation = allOfQuotation.thenApply(
                v -> {
                    System.out.println("AllOf then apply "+Thread.currentThread());
                    return quotationCFs.stream()
                            .map(CompletableFuture::join)
                            .min(Comparator.comparing(Quotation::amount))
                            .orElseThrow();
                }
        );

        CompletableFuture<Void> done = bestQuotation.thenCompose(
                quotation -> anyOfWeather.thenApply(
                        weather -> new TravelPage(quotation, weather)
                ).thenAccept(System.out::println)
        );
        done.join();

        /*CompletableFuture<TravelPage> pageCompletableFuture = bestQuotation.thenCombine(
                CompletableFuture.anyOf(weatherCFs.toArray(CompletableFuture[]::new))
                        .thenApply((o -> (Weather) o)),
                TravelPage::new
        );
        pageCompletableFuture.thenAccept(System.out::println).join();

        CompletableFuture<TravelPage> pageCompletableFuture2 =
                bestQuotation.thenCompose(
                        quotation -> CompletableFuture.anyOf(weatherCFs.toArray(CompletableFuture[]::new))
                                .thenApply((o -> (Weather) o))
                                .thenApply(weather -> new TravelPage(quotation, weather))
                );
        pageCompletableFuture2.thenAccept(System.out::println).join();*/

        weatherExecutor.shutdown();
        quotationExecutor.shutdown();
        travelExecutor.shutdown();

    }

    public static List<Supplier<Quotation>> buildQuotationTasks(Random random) {
        Supplier<Quotation> fetchQuoteA = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Quotation("FETCH QUOTE A", random.nextInt(40,60));
        };

        Supplier<Quotation> fetchQuoteB = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Quotation("FETCH QUOTE b", random.nextInt(40,60));
        };

        Supplier<Quotation> fetchQuoteC = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Quotation("FETCH QUOTE c", random.nextInt(40,60));
        };

        return List.of(fetchQuoteA, fetchQuoteB, fetchQuoteC);
    }

    public static List<Supplier<Weather>> buildWeatherTasks(Random random) {
        Supplier<Weather> fetchQuoteA = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Weather("FETCH QUOTE A", "Sunny");
        };

        Supplier<Weather> fetchQuoteB = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Weather("FETCH QUOTE b", "Rain");
        };

        Supplier<Weather> fetchQuoteC = () -> {
            try {
                Thread.sleep(random.nextInt(80, 120));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Weather("FETCH QUOTE c", "Fog");
        };

        return List.of(fetchQuoteA, fetchQuoteB, fetchQuoteC);
    }
}