package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Question2 {
  public class FineList<T>{
    
    //Node class used within the list
    private class Node {
      T item; 
      int key;
      Node next;
      //create Locks per node which have their own lock() and unlock() methods
      ReentrantLock l = new ReentrantLock();    
      
      //constructor for Head and Tail
      private Node(int x) {
       key = x;
      }
 
      //constructor for creating a Node with an item
      private Node(T newItem) {
        item = newItem;
        key = newItem.hashCode();
      }
    }
    
    private Node head;
    
    //Fine-Grained List Constructor
    public FineList() {
      //Create head and tail with key set to the max and min range
      head = new Node(Integer.MIN_VALUE);
      head.next = new Node(Integer.MAX_VALUE);
    }
    
    //Function add(item) included to create test list
    public boolean add(T item) {
      int key = item.hashCode();
      head.l.lock();
      Node pred = head;
      try {
        Node curr = pred.next;
        curr.l.lock();
        try {
          while (curr.key < key) {
            pred.l.unlock();
            pred = curr;
            curr = curr.next;
            curr.l.lock();
          }
          if (curr.key == key) {
            return false;
          }
          Node newNode = new Node(item);
          newNode.next = curr;
          pred.next = newNode;
          return true;
        } finally {
          curr.l.unlock();
        }
      } finally {
        pred.l.unlock();
      }
    }
    
    //Q2.1 Function contains(item) utilizing fine-grained synchronization
    public boolean contains(T item) {
      int key = item.hashCode();
      head.l.lock();
      Node pred = head;
      try {
        Node curr = pred.next;
        curr.l.lock();
        try {
          while (curr.key < key) {
            pred.l.unlock();
            pred = curr;
            curr = curr.next;
            curr.l.lock();
          }
          if (curr.key == key) {
            return true;
          }
          return false;
        } finally {
          curr.l.unlock();
        }
      } finally {
        pred.l.unlock();
      }
    }
    
  }
  
  //Creates a linked list containing N even numbers
  public static void main(String[] args) throws InterruptedException {
    //constants
    int test_length = 10000;
    int no_of_threads = 2;
    
    //initialize ExecutorService with N # of threads
    System.out.println("# OF THREADS = " + no_of_threads);
    ExecutorService executor = Executors.newFixedThreadPool(no_of_threads);

    //initialize test list
    Question2 q2 = new Question2();
    FineList<Integer> flist = q2.new FineList<Integer>();
    
    //Runnable class that takes in integer to be added
    //Allows for faster building of initial linked list
    class InitRunnable implements Runnable {
      private int x;
      
      InitRunnable(int data) {
        x = data;
      }
      
      @Override
      public void run() {
        //System.out.println("Adding " + x + " to init list...");
        flist.add(x);
      }
    }
    
    for (int i = 0; i < test_length; i++) {
      executor.submit(new InitRunnable(i*2));
    }
    
    //Allow threads to completely finish
    System.out.println("Loading...");
    TimeUnit.SECONDS.sleep(3);
    
    //create anonymous runnables for contains() and add()
    Runnable containsTask1 = () -> {
      System.out.println("Executing contains(999) within: " + Thread.currentThread().getName());
      boolean ret = flist.contains(999);
      System.out.println("contains(999) in " + Thread.currentThread().getName() + " ---> Returned: " + ret);
    };
    
    Runnable addTask1 = () -> {
      System.out.println("Executing add(999) within: " + Thread.currentThread().getName());
      boolean ret = flist.add(999);
      System.out.println("add(999) in " + Thread.currentThread().getName() + " ---> Returned: " + ret);
    };
    
    Runnable containsTask2 = () -> {
      System.out.println("Executing contains(777) within: " + Thread.currentThread().getName());
      boolean ret = flist.contains(777);
      System.out.println("contains (777) in " + Thread.currentThread().getName() + " ---> Returned: " + ret);
    };
    
    Runnable addTask2 = () -> {
      System.out.print("Executing add(777) within: " + Thread.currentThread().getName());
      boolean ret = flist.add(777);
      System.out.println("add(777) in " + Thread.currentThread().getName() + " ---> Returned: " + ret);
    };
    
    //run executor service
    System.out.println("Submitting Tasks ...");
    executor.submit(containsTask1);
    executor.submit(addTask1);
    executor.submit(containsTask1);
    executor.submit(containsTask2);
    executor.submit(addTask2);
    executor.submit(containsTask2);
    
    //check console for resulting print statements
    executor.shutdown();
    
  }

}
