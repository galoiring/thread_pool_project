package il.co.ilrd.ThreadPool;

import il.co.ilrd.waitable_queue.waitable_queue;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.currentThread;

public class ThreadPool implements Executor {

    private final waitable_queue<Task<?>> waitableQueue;
    private int numOfThread = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition isPaused = lock.newCondition();
    private final Condition isTerminated = lock.newCondition();

    private volatile boolean isPause = false;

    public ThreadPool(int numOfThread){
        this.waitableQueue = new waitable_queue<>();
        setNumOfThread(numOfThread);
    }

    public <T> Future<T> submit(Callable<T> callable, Priority priority){
        Task<T> task = new Task<>(callable, priority);
        waitableQueue.enqueue(task);
        return task.getFuture();
    }

    public <T> Future<T> submit(Callable<T> callable){
        Task<T> task = new Task<>(callable, Priority.NORM_PRIORITY);
        waitableQueue.enqueue(task);
        return task.getFuture();
    }

    public Future<?> submit(Runnable runnable, Priority priority){
        Callable<?> callable = () -> {
            runnable.run();
            return null;
        };

        Task<?> task = new Task(callable, priority);
        waitableQueue.enqueue(task);
        return task.getFuture();
    }

    public Future<?> submit(Runnable runnable){
        Callable<?> callable = () -> {
            runnable.run();
            return null;
        };

        Task<?> task = new Task<>(callable, Priority.NORM_PRIORITY);
        waitableQueue.enqueue(task);
        return task.getFuture();
    }

    private  <T> Future<T> submitInt(Callable<T> callable, int priority){
        Task<T> task = new Task<>(callable, priority );
        waitableQueue.enqueue(task);
        return task.getFuture();
    }

    public void pause(){
        Callable<?> sleepingPills = new Callable() {
            @Override
            public Object call() throws Exception {
                if(isPause){
                    lock.lock();
                        if(isPause){
                            isPaused.await();
                        }
                    lock.unlock();
                }
                return 0;
            }
        };

        isPause = true;

        for(int i = 0; i < this.numOfThread; ++i) {
            submitInt(sleepingPills, 11);
        }
    }

    public void resume(){
        lock.lock();
        isPause = false;
        isPaused.signalAll();
        lock.unlock();
    }

    public void shutDown(){
        setNumOfThread(0);
    }

    public boolean awaitTermination(){
        Callable<?> badApple = () -> {
            WorkingThread workingThread = (WorkingThread) currentThread();
            workingThread.isRunning = false;
            return 0;
        };

        Callable<?> lastBadApple = () -> {
            WorkingThread workingThread = (WorkingThread) currentThread();
            lock.lock();
            isTerminated.signalAll();
            workingThread.isRunning = false;
            lock.unlock();
            return 0;
        };

        for(int i = 0; i < this.numOfThread - 1; ++i){
            submitInt(badApple, 0);
        }

        submitInt(lastBadApple, -1);

        lock.lock();
        try {
            isTerminated.await();
            lock.unlock();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean awaitTermination(int timeOut, TimeUnit unit){
        return false;
    }

    public void setNumOfThread(int numOfThread){

        Callable<?> badApple = () -> {
            WorkingThread workingThread = (WorkingThread) currentThread();
            workingThread.isRunning = false;
            return 0;
        };

        if(numOfThread > this.numOfThread){
            for(int i = this.numOfThread; i < numOfThread; ++i) {
                new WorkingThread().start();
            }
        }

        else {
            for(int i = 0; i < this.numOfThread - numOfThread; ++i){
                submitInt(badApple, 11);
            }
        }

        this.numOfThread = numOfThread;
    }

    @Override
    public void execute(Runnable runnable) {
        submit(runnable);
    }

    private class WorkingThread extends Thread{

        boolean isRunning = true;

        @Override
        public void run() {
            while(isRunning){
                waitableQueue.dequeue().run();
            }
        }
    }

    private class Task<T> implements Runnable, Comparable<T>{

        private final int priority;
        private final Callable<T> callback;
        private final TaskFuture<T> future;

        public Task(Callable<T> callable, Priority priority){
            this.callback = callable;
            this.priority = priority.getValue();
            future = new TaskFuture<>();
        }

        public Task(Callable<T> callable){
            this.callback = callable;
            this.priority = Priority.NORM_PRIORITY.getValue();
            future = new TaskFuture<>();
        }

        private Task(Callable<T> callable, int priority){
            this.callback = callable;
            this.priority = priority;
            future = new TaskFuture<>();
        }

        @Override
        public int compareTo(T t) {
            return Integer.compare(((Task<?>)t).priority, this.priority);
        }

        @Override
        public void run() {
            try {
                future.returnValue = callback.call();
            } catch (Throwable e) {
                future.exception = e;
            }
            finally {
                future.isDone = true;
                synchronized (future){
                    future.notify();
                }
            }
        }

        public Future<T> getFuture(){
            return future;
        }
    }

    private class TaskFuture<T> implements Future<T>{

        private T returnValue = null;
        private Throwable exception = null;
        private volatile boolean isDone = false;

        @Override
        public boolean cancel(boolean b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCancelled() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDone() {
            return (isDone || exception != null);
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            if(!isDone()){
                synchronized (this){
                    if(!isDone()) {
                        this.wait();
                    }
                }
            }

            if(null != exception){
                throw new ExecutionException(exception);
            }

            return returnValue;
        }

        @Override
        public T get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            if(!isDone){
                synchronized (this){
                    this.wait(timeUnit.toMillis(l));
                }
            }

            if(null != exception){
                throw new ExecutionException(exception);
            }

            if(!isDone()){
                throw new TimeoutException();
            }

            return returnValue;
        }
    }

    public enum Priority{
        MIN_PRIORITY(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        NORM_PRIORITY(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        MAX_PRIORITY(10);

        private final int value;
        Priority(int value){
            this.value = value;
        }

        private int getValue(){
            return this.value;
        }
    }
}
