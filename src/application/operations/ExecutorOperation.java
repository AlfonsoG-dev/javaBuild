package operations;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

public class ExecutorOperation {
    
    public<T> T executeConcurrentCallableList(Callable<T> task) {
        T t = null;
        FutureTask<T> future = new FutureTask<>(task);
        System.out.println("[Info] Starting computation");
        try(ExecutorService executor = Executors.newCachedThreadPool()) {
            executor.submit(future);
            try {
                t = future.get();
                System.out.println("[Info] Waiting to get the results...");
            } catch(CancellationException cancelException) {
                System.err.println("[Error] Future was cancelled " + cancelException.getCause());
            }
        } catch(NullPointerException | RejectedExecutionException taskException) {
            System.err.println("[Error] Task execution not allowed " + taskException.getCause());
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Error] Execution was interrupted " + e.getCause());
        }
        return t;
    }
}
