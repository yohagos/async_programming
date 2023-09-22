import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Main {
    public record Quotation(String server, int amount) {}
    public record Weather(String server, String data) {}
    public record TravelPage(Quotation quotation, Weather weather) {}

    public static void main(String[] args) {
        Random random = new Random();

        List<Supplier<Weather>> weatherTasks = buildWeatherTasks(random);
        List<Supplier<Quotation>> quotationTasks = buildQuotationTasks(random);

        List<CompletableFuture<Weather>> weatherCFs = new ArrayList<>();
        for (Supplier<Weather> task: weatherTasks) {
            CompletableFuture<Weather> weather =
                    CompletableFuture.supplyAsync(task)
                                        .exceptionally(e -> {
                                            System.out.println("e = "+e);
                                            return new Weather("Unknown", "Unknown");
                                        });
            weatherCFs.add(weather);
        }

        CompletableFuture<Weather> anyOfWeather =
                CompletableFuture.anyOf(weatherCFs.toArray(CompletableFuture[]::new))
                        .thenApply(weather -> (Weather) weather);

        List<CompletableFuture<Quotation>> quotationCFs = new ArrayList<>();
        for (Supplier<Quotation> task: quotationTasks) {
            CompletableFuture<Quotation> quote =
                    CompletableFuture.supplyAsync(task)
                            .handle((quotation, exception) -> {
                                if (exception == null)
                                    return quotation;
                                System.out.println("exception = "+exception);
                                return new Quotation("Unknown", 0);
                            });
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
    }

    public static List<Supplier<Weather>> buildWeatherTasks(Random random) {
        Supplier<Weather> fetchA =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server A", "Sunny");
                };
        Supplier<Weather> fetchB =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    //return new Weather("Server B", "Rain");
                    throw new RuntimeException(
                            new IOException("Weather server B unavailable")
                    );
                };
        Supplier<Weather> fetchC =
                () -> {
                    try {
                        Thread.sleep(random.nextInt(80, 120));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return new Weather("Server C", "Rain");
                };
        return List.of(fetchA, fetchB, fetchC);
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
            //return new Quotation("FETCH QUOTE b", random.nextInt(40,60));
            throw new RuntimeException(
                    new IOException("Weather server B unavailable")
            );
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
}