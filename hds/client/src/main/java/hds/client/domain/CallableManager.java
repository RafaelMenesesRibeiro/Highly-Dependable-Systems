package hds.client.domain;

import hds.security.msgtypes.BasicMessage;

import java.util.concurrent.*;


public class CallableManager implements Callable<BasicMessage> {
    protected Callable<BasicMessage> callable;
    protected long timeout;
    protected TimeUnit timeUnit;

    public CallableManager(Callable<BasicMessage> job, long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.callable = callable;
    }

    @Override
    public BasicMessage call() {
        BasicMessage result = null; // default, this could be adapted
        ExecutorService exec = Executors.newSingleThreadExecutor();

        try {
            result = exec.submit(callable).get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException exc) {
            if (exc instanceof TimeoutException) {
                System.out.println("Failed to obtain a reply from a replica within an acceptable time window...");
                System.out.println(callable.toString());
                return null;
            } else {
                System.out.println(exc.getMessage());
            }
        }
        exec.shutdown();
        return result;
    }
}
