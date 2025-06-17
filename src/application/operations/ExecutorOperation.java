package operations;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class ExecutorOperation {
    
    public List<File> executeConcurrentCallableList(Callable<List<File>> task) {
        List<File> result = null;
        FutureTask<List<File>> future = new FutureTask<>(task);
        try(ExecutorService executor = Executors.newCachedThreadPool()) {
            executor.submit(future);
            System.out.println("[Info] Starting computation");
            result = future.get();
            System.out.println("[Info] Waiting to get the results...");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            System.err.println("[Error] Execution was interrupted");
        }
        return result;
    }
}
