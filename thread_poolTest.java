package il.co.ilrd.ThreadPool;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class thread_poolTest {
    @Test
    void submitRunnableTest(){
        ThreadPool threadPool = new ThreadPool(5);
        StringBuilder str = new StringBuilder("HELLO");
        threadPool.submit(() -> {
            str.append(" this");
        } , ThreadPool.Priority.MIN_PRIORITY);

        try{Thread.sleep(1000);}catch (Exception e){}

        System.out.println(str);
        threadPool.shutDown();

        assertEquals("HELLO this", str.toString());
    }

    @Test
    void submitCallableTest(){
        ThreadPool threadPool = new ThreadPool(5);
        StringBuilder str = new StringBuilder("HELLO");
        threadPool.submit(() -> {
            str.append(" added");
            return 5;
        } , ThreadPool.Priority.MIN_PRIORITY);

        try{Thread.sleep(1000);}catch (Exception e){}

        System.out.println(str);
        threadPool.shutDown();

        assertEquals("HELLO added", str.toString());
    }

    @Test
    void shutDownTest(){
        ThreadPool threadPool = new ThreadPool(5);
        StringBuilder str = new StringBuilder("HELLO");
        threadPool.submit(() -> {
            str.append(" this");
        } , ThreadPool.Priority.MIN_PRIORITY);

        try{Thread.sleep(1000);}catch (Exception e){}

        System.out.println(str);

        threadPool.shutDown();
        assertEquals("HELLO this", str.toString());
    }

    @Test
    void priorityTest(){
        ThreadPool threadPool = new ThreadPool(1);
        StringBuilder str = new StringBuilder("HELLO");
        threadPool.pause();

        threadPool.submit(() -> {
            try{Thread.sleep(900);}catch (Exception e){}
            str.append(" second");
        } , ThreadPool.Priority.MIN_PRIORITY);

        threadPool.submit(() -> {
            try{Thread.sleep(900);}catch (Exception e){}
            str.append(" third");
        } , ThreadPool.Priority.MIN_PRIORITY);

        threadPool.submit(() -> {
            try{Thread.sleep(900);}catch (Exception e){}

            str.append(" first");
        } , ThreadPool.Priority.MAX_PRIORITY);

        threadPool.resume();
        try{Thread.sleep(4000);}catch (Exception e){}


        System.out.println(str);

        threadPool.shutDown();
        assertEquals("HELLO first second third", str.toString());
    }

    @Test
    void futureTest() throws ExecutionException, InterruptedException, TimeoutException {
        ThreadPool threadPool = new ThreadPool(5);
        StringBuilder str = new StringBuilder("HELLO");
        threadPool.pause();

        Future retVal1 = threadPool.submit(() -> {
            return str.append(" min");
        } , ThreadPool.Priority.MIN_PRIORITY);


        Future retVal2 = threadPool.submit(() -> {
            str.append(" max");
            return str;
        } , ThreadPool.Priority.MAX_PRIORITY);

        System.out.println("first: " + str);
        threadPool.resume();
        try{Thread.sleep(2000);}catch (Exception e){}

        System.out.println(retVal1.get(10, TimeUnit.SECONDS));


        System.out.println(retVal2.get());

        threadPool.shutDown();
        assertEquals("HELLO max min", str.toString());
    }

    @Test
    void pauseTest() throws ExecutionException, InterruptedException, TimeoutException {
        ThreadPool threadPool = new ThreadPool(5);
        StringBuilder str = new StringBuilder("HELLO");
        threadPool.pause();
        try{Thread.sleep(2000);}catch (Exception e){}

        Future retVal1 = threadPool.submit(() -> {
            return str.append(" min");
        } , ThreadPool.Priority.NORM_PRIORITY);


        Future retVal2 = threadPool.submit(() -> {
            str.append(" max");
            return str;
        } , ThreadPool.Priority.MAX_PRIORITY);

        threadPool.resume();

        System.out.println("first: " + str);
        System.out.println(retVal1.get());

        System.out.println(retVal2.get());
        threadPool.shutDown();
        assertEquals("HELLO max min", str.toString());
    }

    @Test
    void awaitTerminationTest() throws ExecutionException, InterruptedException, TimeoutException {
        ThreadPool threadPool = new ThreadPool(5);
        StringBuilder str = new StringBuilder("HELLO");
        threadPool.pause();

        Future retVal1 = threadPool.submit(() -> {
            try{Thread.sleep(2000);}catch (Exception e){}
            return str.append(" min");
        } , ThreadPool.Priority.NORM_PRIORITY);


        Future retVal2 = threadPool.submit(() -> {
            try{Thread.sleep(2000);}catch (Exception e){}
            str.append(" max");
            return str;
        } , ThreadPool.Priority.MAX_PRIORITY);

        threadPool.resume();
        System.out.println("first: " + str);
        try{Thread.sleep(2000);}catch (Exception e){}

        threadPool.awaitTermination();

        System.out.println(retVal2.get());

        System.out.println(retVal1.get());

        assertEquals("HELLO max min", str.toString());
    }
}
