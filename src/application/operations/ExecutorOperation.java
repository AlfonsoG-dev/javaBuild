package operations;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;

public class ExecutorOperation {
    
    /**
     * Helper to execute a callable task with virtual threads.
     * @param task the Callable task to execute 
     * @throws InterruptedException if the executor service can't start
     * @throws CancellationException if the future of the task was canceled.
     * @return the result of the execution of the generic type T
     */
    public <T> T executeConcurrentCallableList(Callable<T> task) {
        T result = null;
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("[Info] Starting computation of task");
        try {
            Future<T> future = executor.submit(task);
            System.out.println("[Info] Waiting to get the results...");
            result = future.get();
        } catch (CancellationException cancelException) {
            System.err.println("[Error] Future was canceled: " + cancelException.getMessage());
        } catch (RejectedExecutionException executionException) {
            System.err.println("[Error] Task execution rejected: " + executionException.getMessage());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            System.err.println("[Error] Execution interrupted: " + interruptedException.getMessage());
        } catch (ExecutionException executionException) {
            System.err.println("[Error] Execution failed: " + executionException.getCause());
        } finally {
            executor.shutdown();
        }
        return result;
    }

}
