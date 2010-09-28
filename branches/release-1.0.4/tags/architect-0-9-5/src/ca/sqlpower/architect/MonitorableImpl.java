package ca.sqlpower.architect;

/**
 * The simplest possible Monitorable implementation. It simply starts with
 * reasonable defaults, which you can modify as much as you want. The getter
 * methods simply return the values you gave to the setter methods.
 * 
 * <p>
 * In order to guarantee reliable communication between threads using this
 * object, all methods are declared as synchronized.
 * 
 * <p>
 * This class will not be the best choice of Monitorable in many cases, but it
 * is certainly useful in cases where the work being carried out is shared
 * between several classes. They can pass an instance of this class between
 * them, and the ProgressWatcher will still have a single Monitorable to poll
 * for progress.
 * 
 * <p>The "Reasonable" defaults are:
 * <ul>
 *  <li>progress = 0
 *  <li>jobSize = null (means not yet determined)
 *  <li>message = null
 *  <li>started = false
 *  <li>cancelled = false
 *  <li>finished = false
 * </ul>
 */
public class MonitorableImpl implements Monitorable {

    private int progress = 0;
    private Integer jobSize = null;
    private String message = null;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean finished = false;
    
    public synchronized boolean isCancelled() {
        return cancelled;
    }
    
    public synchronized void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public synchronized Integer getJobSize() {
        return jobSize;
    }
    
    public synchronized void setJobSize(Integer jobSize) {
        this.jobSize = jobSize;
    }
    
    public synchronized String getMessage() {
        return message;
    }
    
    public synchronized void setMessage(String message) {
        this.message = message;
    }
    
    public synchronized int getProgress() {
        return progress;
    }
    
    public synchronized void setProgress(int progress) {
        this.progress = progress;
    }
    
    public synchronized boolean hasStarted() {
        return started;
    }
    
    public synchronized void setStarted(boolean started) {
        this.started = started;
    }
    
    public synchronized boolean isFinished() {
        return finished;
    }
    
    public synchronized void setFinished(boolean finished) {
        this.finished = finished;
    }

    public synchronized void incrementProgress() {
        this.progress++;
    }
    
    public String toString() {
        return String.format("Job size: %4d " +
                                 "Progress: %4d " +
                                 "Started: %b " +
                                 "Finished: %b " +
                                 "Cancelled: %b " +
                                 "Message: %s ",
                                 jobSize, 
                                 progress, 
                                 started, 
                                 finished, 
                                 cancelled, 
                                 message);
    }
}
