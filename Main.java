import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Main {
    record Quotation(String server, int amount) {
    }

    public static void main (String[] args) throws ExecutionException, InterruptedException {
        runSYNC();
        runEXECUTOR();
        runASYNC();
    }

    public static void runEXECUTOR() throws ExecutionException, InterruptedException {
        Random random = new Random();

        Callable<Quotation> fetchQuoteA = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("FETCH QUOTE A", random.nextInt(40,60));
        };

        Callable<Quotation> fetchQuoteB = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("FETCH QUOTE b", random.nextInt(40,60));
        };

        Callable<Quotation> fetchQuoteC = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("FETCH QUOTE c", random.nextInt(40,60));
        };

        var quotationTasks = List.of(fetchQuoteA, fetchQuoteB, fetchQuoteC);

        var executor = Executors.newFixedThreadPool(4);

        Instant begin = Instant.now();

        List<Future<Quotation>> futures = new ArrayList<>();
        for (Callable<Quotation> task: quotationTasks) {

            Future<Quotation> future = executor.submit(task);
            futures.add(future);
        }

        List<Quotation> quotations = new ArrayList<>();
        for (Future<Quotation> future: futures) {
            Quotation quotation = future.get();
            quotations.add(quotation);
        }

        Quotation bestQuotation =
        quotations.stream().min(Comparator.comparing(Quotation::amount)).orElseThrow();

        Instant end = Instant.now();

        Duration duration = Duration.between(begin, end);
        System.out.println("Best Quotation [EXECUTOR] = " + bestQuotation + " (" + duration.toMillis() + "ms)");

        executor.shutdown();
    }

    public static void runSYNC() {
        Random random = new Random();

        Callable<Quotation> fetchQuoteA = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("FETCH QUOTE A", random.nextInt(40,60));
        };

        Callable<Quotation> fetchQuoteB = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("FETCH QUOTE b", random.nextInt(40,60));
        };

        Callable<Quotation> fetchQuoteC = () -> {
            Thread.sleep(random.nextInt(80, 120));
            return new Quotation("FETCH QUOTE c", random.nextInt(40,60));
        };

        var quotationTasks = List.of(fetchQuoteA, fetchQuoteB, fetchQuoteC);

        var executor = Executors.newFixedThreadPool(4);

        Instant begin = Instant.now();

        Quotation bestQuotation = quotationTasks.stream()
                .map(task -> fetchQuotation(task))
                .min(Comparator.comparing(Quotation::amount))
                .orElseThrow();

        Instant end = Instant.now();
        Duration duration = Duration.between(begin, end);
        System.out.println("Best Quotation [SYNC] = " + bestQuotation + " (" + duration.toMillis() + "ms)");
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

        List<Quotation> quotations = new ArrayList<>();
        for (CompletableFuture<Quotation> future: futures) {
            Quotation quotation = future.join();
            quotations.add(quotation);
        }

        Quotation bestQuotation =
                quotations.stream().min(Comparator.comparing(Quotation::amount)).orElseThrow();

        Instant end = Instant.now();

        Duration duration = Duration.between(begin, end);
        System.out.println("Best Quotation [ASYNC] = " + bestQuotation + " (" + duration.toMillis() + "ms)");
    }

    private static  Quotation fetchQuotation(Callable<Quotation> task) {
        try {
            return task.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
