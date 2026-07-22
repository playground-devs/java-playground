package com.example.javaeight.concurrency;

import lombok.SneakyThrows;
import lombok.Synchronized;

public class ExampleClassDeadLock {

private final Object lock1 = new Object();
private final Object lock2 = new Object();

public void Thread1Task(){
    synchronized(lock1){
        System.out.println("Thread one holding the lock 1......");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        synchronized (lock2){
            System.out.println("Thread one holding the lock 2......");
        }
    }
}

public void Thread2Task() {
    synchronized (lock2){
        System.out.println("Thread Two holding the lock 2......");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        synchronized (lock1){
            System.out.println("Thread Two holding the lock 1......");
        }
    }
}

public static void main(String[] args){

    ExampleClassDeadLock exampleClassDeadLock = new ExampleClassDeadLock();
    Thread thread1 = new Thread(exampleClassDeadLock::Thread1Task);
    Thread thread2 = new Thread(exampleClassDeadLock::Thread2Task);

    thread1.start();
    thread2.start();
}


}
