import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Main {
    record Quotation(String server, int amount) {}

    record Weather(String server, String weather) {}

    record TravelPage(Quotation quotation, Weather weather) {}

    public static void main(String[] args) {
        /*Random random = new Random();

        var quotationTasks = buildQuotationTasks(random);

        List<CompletableFuture<Quotation>> quotationCF = new ArrayList<>();
        for (Supplier<Quotation> quotationTask: quotationTasks) {
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(quotationTask);
            quotationCF.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(quotationCF.toArray(CompletableFuture[]::new));
        System.out.println(Thread.currentThread());
        Quotation bestQuotation =
        allOf.thenApply(
                v ->  quotationCF.stream()
                            .map(CompletableFuture::join)
                            .min(Comparator.comparing(Quotation::amount))
                            .orElseThrow()
        ).join();
        System.out.println("Best Quotation: " + bestQuotation);*/


        //run();

        //ExampleAnyOfCompletableFuture();

        runCombined();
    }

    public static void run() {
        Random random = new Random();

        List<Supplier<Weather>> weatherTasks = buildWeatherTasks(random);

        List<CompletableFuture<Weather>> futures = new ArrayList<>();
        for (Supplier<Weather> task: weatherTasks) {
            CompletableFuture<Weather> future = CompletableFuture.supplyAsync(task);
            futures.add(future);
        }

        CompletableFuture<Object> future =
        CompletableFuture.anyOf(futures.toArray(CompletableFuture[]::new));

        future.thenAccept(System.out::println).join();
    }

    public static void runCombined() {
        Random random = new Random();

        List<Supplier<Weather>> weatherTasks = buildWeatherTasks(random);
        List<Supplier<Quotation>> quotationTasks = buildQuotationTasks(random);

        List<CompletableFuture<Weather>> weatherCF = new ArrayList<>();
        for (Supplier<Weather> task: weatherTasks) {
            CompletableFuture<Weather> future = CompletableFuture.supplyAsync(task);
            weatherCF.add(future);
        }

        CompletableFuture<Weather> anyWeather = CompletableFuture.anyOf(weatherCF.toArray(CompletableFuture[]::new))
                .thenApply(o -> (Weather) o);

        List<CompletableFuture<Quotation>> quotationCF = new ArrayList<>();
        for (Supplier<Quotation> task : quotationTasks) {
            CompletableFuture future = CompletableFuture.supplyAsync(task);
            quotationCF.add(future);
        }

        CompletableFuture<Void> allOfQuotation = CompletableFuture.allOf(quotationCF.toArray(CompletableFuture[]::new));

        CompletableFuture<Quotation> bestQuotation = allOfQuotation.thenApply(
                v -> quotationCF.stream()
                        .map(CompletableFuture::join)
                        .min(Comparator.comparing(Quotation::amount))
                        .orElseThrow()
        );

        /*TravelPage page = new TravelPage(bestQuotation.join(), anyWeather.join());

        System.out.println("Page = " + page);*/


        // Combine two CompletableFutures into one, in which you use one of those two to initialize it
        /*CompletableFuture<TravelPage> pageCompleteFuture =
            bestQuotation.thenCombine(anyWeather, (q,w) -> new TravelPage(q, w));
        //bestQuotation.thenCombine(anyWeather, TravelPage::new);
        pageCompleteFuture.thenAccept(System.out::println).join();*/

        // Compose example to combine both of them. Call first object
        CompletableFuture<TravelPage> pageCompletableFuture =
            bestQuotation.thenCompose(
                    quotation -> anyWeather.thenApply(
                            weather -> new TravelPage(quotation, weather)
                    )
            );
        pageCompletableFuture.thenAccept(System.out::println).join();
    }

    public static void ExampleAnyOfCompletableFuture() {
        Supplier<Weather> fetchWeatherA =
                () -> {
                    try {
                        Thread.sleep(1_000); // or try 12
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server A - sleep 1 second: ", "Sunny");
                };
        Supplier<Weather> fetchWeatherB =
                () -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server B - sleep 10 millisecond: ", "Rain");
                };

        CompletableFuture<Weather> taskA = CompletableFuture.supplyAsync(fetchWeatherA);
        CompletableFuture<Weather> taskB = CompletableFuture.supplyAsync(fetchWeatherB);

        CompletableFuture.anyOf(taskA, taskB)
                .thenAccept(System.out::println)
                .join();

        System.out.println("Task A : " + taskA);
        System.out.println("Task B : " + taskB);
    }

    private static List<Supplier<Weather>> buildWeatherTasks(Random random) {
        Supplier<Weather> fetchWeatherA =
                () -> {
                    try {
                        Thread.sleep(60,120);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server A","Sunny");
                };
        Supplier<Weather> fetchWeatherB =
                () -> {
                    try {
                        Thread.sleep(60,120);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server B","Fog");
                };
        Supplier<Weather> fetchWeatherC =
                () -> {
                    try {
                        Thread.sleep(60,120);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server C","Rain");
                };
        return List.of(fetchWeatherA, fetchWeatherB, fetchWeatherC);
    }

    private static List<Supplier<Quotation>> buildQuotationTasks(Random random) {
        Supplier<Quotation> fetchQuotationA =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80,120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    //System.out.println(Thread.currentThread());
                    return new Quotation("Server A", random.nextInt(40,60));
                };
        Supplier<Quotation> fetchQuotationB =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80,120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Quotation("Server B", random.nextInt(40,60));
                };
        Supplier<Quotation> fetchQuotationC =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80,120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Quotation("Server C", random.nextInt(40,60));
                };

        return List.of(fetchQuotationA, fetchQuotationB, fetchQuotationC);
    }
}