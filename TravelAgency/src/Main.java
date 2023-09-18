import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class Main {
    record Quotation(String server, int amount) { }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        runASYNC();
    }

    public static void runASYNC() throws ExecutionException, InterruptedException {
        Random random = new Random();

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

        var quotationTasks = List.of(fetchQuoteA, fetchQuoteB, fetchQuoteC);

        Instant begin = Instant.now();

        List<CompletableFuture<Quotation>> futures = new ArrayList<>();
        for (Supplier<Quotation> task: quotationTasks) {
            CompletableFuture<Quotation> future = CompletableFuture.supplyAsync(task);
            futures.add(future);
        }

        //List<Quotation> quotations = new ArrayList<>();
        Collection<Quotation> quotations = new ConcurrentLinkedDeque<>();
        List<CompletableFuture<Void>> voids = new ArrayList<>();
        for (CompletableFuture<Quotation> future: futures) {
            /* Quotation quotation = future.join();
            quotations.add(quotation);*/

            future.thenAccept(System.out::println);
            CompletableFuture<Void> accept =
            // Two ways to add
            //future.thenAccept(quotation -> quotations.add(quotation));
                future.thenAccept(quotations::add);
            voids.add(accept);
        }

        voids.forEach(v -> v.join());
        System.out.println("quotations = " + quotations);

        //Thread.sleep(500);
/*        Quotation bestQuotation =
                quotations.stream().min(Comparator.comparing(Quotation::amount)).orElseThrow();

        Instant end = Instant.now();

        Duration duration = Duration.between(begin, end);
        System.out.println("Best Quotation [ASYNC] = " + bestQuotation + " (" + duration.toMillis() + "ms)");*/
    }
}