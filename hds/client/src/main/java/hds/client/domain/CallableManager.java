package hds.client.domain;

import hds.security.msgtypes.BasicMessage;

import java.util.concurrent.*;

/**
 * CallableManager is Callable implementation that empowers ExecutorCompletionServices with the ability to run their
 * callable jobs with timeouts defined for each. This would otherwise be impossible as ExecutionCompletionService only
 * provides away of defining a global for all tasks to complete or by using the .poll() method, which would give each
 * job a defined timeout from the moment .poll was made, instead of when the task was launched.
 * @author Diogo Vilela
 * @author Francisco Barros
 * @author Rafael Ribeiro
 */
public class CallableManager implements Callable<BasicMessage> {
    protected Callable<BasicMessage> callable;
    protected long timeout;
    protected TimeUnit timeUnit;


    public CallableManager(Callable<BasicMessage> callable, long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.callable = callable;
    }

    @Override
    public BasicMessage call() {
        BasicMessage result;
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            result = exec.submit(callable).get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException exc) {
            if (exc instanceof TimeoutException) {
                System.out.println("Failed to obtain a reply from a replica within an acceptable time window...");
                System.out.println(callable.toString());
            } else {
                System.out.println(exc.getMessage());
            }
            exec.shutdown();
            return null;
        }
        exec.shutdown();
        return result;
    }
}
