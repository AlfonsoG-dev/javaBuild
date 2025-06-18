package operations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class ExecutorOperation {
    
    public<T> T executeConcurrentCallableList(Callable<T> task) {
        T t = null;
        FutureTask<T> future = new FutureTask<>(task);
        System.out.println("[Info] Starting computation");
        try(ExecutorService executor = Executors.newCachedThreadPool()) {
            executor.submit(future);
            // TODO: verify capture exception of future when invoke `get()`
            t = future.get();
            System.out.println("[Info] Waiting to get the results...");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Error] Execution was interrupted");
        }
        return t;
    }
}
