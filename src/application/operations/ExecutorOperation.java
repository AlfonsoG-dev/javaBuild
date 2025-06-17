package operations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class ExecutorOperation {
    
    public<T> List<T> executeConcurrentCallableList(Callable<List<T>> task) {
        List<T> result = new ArrayList<>();
        FutureTask<List<T>> future = new FutureTask<>(task);
        System.out.println("[Info] Starting computation");
        try(ExecutorService executor = Executors.newCachedThreadPool()) {
            executor.submit(future);
            result.addAll(future.get());
            System.out.println("[Info] Waiting to get the results...");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Error] Execution was interrupted");
        }
        return result;
    }
}
