package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/***
 * 
 * @author Nathan Lafrance-Berger
 *
 */

public class Question3 {
	
	public static void main(String[] args) {
		
		
		int capacity = 6;
		int threadNum = 3;
		Runnable[] t = new Runnable[threadNum];
		BoundedQueue q = new BoundedQueue(capacity);
		
		class Enqueuer implements Runnable {
			String data;
	        
	        Enqueuer(String data) {
	        	this.data = data;
	        }
	        
	        @Override
	        public void run() {
	        	char item;
	        	for (int i=0; i<100; i++){
	        		try {
	        			q.print();
	        			item = data.charAt((int)(Math.random() * data.length()));
						q.enq(item);
						System.out.println("Item enqueued: " + item);
						q.print();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        	}
	        }
	    }
		
		class Dequeuer implements Runnable {
	        
	        @Override
	        public void run() {
	        	for (int i=0; i<100; i++){
	        		try {
	        			q.print();
						System.out.println("Item dequeued: " + q.deq());
						q.print();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	        	}
	        }
	    }
		   
		Enqueuer enqer = new Enqueuer("botacelivermiceli");
		Enqueuer enqer1 = new Enqueuer("phalentropur");
		Dequeuer deqer = new Dequeuer();
		
	    //initialize ExecutorService with N # of threads
	    System.out.println("# OF THREADS = " + threadNum);
	    ExecutorService executor = Executors.newFixedThreadPool(threadNum);
	    
		t[0] = new Thread(enqer);
		t[1] = new Thread(enqer1);
		t[2] = new Thread(deqer);
		executor.execute(t[0]);
		executor.execute(t[1]);
		executor.execute(t[2]);
		
	}
	
	/*
	 * Question 3.1
	 */
	
	/**
	 * Bounded Queue class implementation using circular array 
	 */
	public static class BoundedQueue {
		
		ReentrantLock enqLock, deqLock;
		Condition notEmpty, notFull;
		int enqSize;
		int deqSize;
		int head;
		int tail;
		int capacity;
		Object[] queue;
		
		public BoundedQueue(int capacity){
			
			this.enqLock = new ReentrantLock();
			this.notFull = enqLock.newCondition();
			this.deqLock = new ReentrantLock();
			this.notEmpty = deqLock.newCondition();
			
			this.capacity = capacity;
			this.head = 0;
			this.tail = 0;
			this.enqSize = 0;
			this.deqSize = 0;
			this.queue = new Object[this.capacity];
		}
		
		/**
		 * Adds object to the queue
		 * @param x
		 * @throws InterruptedException
		 */
		public void enq(Object x) throws InterruptedException{
			boolean wakeupDequeuers = false;
			enqLock.lock();
			
			try{
				while (enqSize >= capacity){	// Update size parameters and wait for signal
					deqLock.lock();
					enqSize += deqSize;
					deqSize = 0;
					deqLock.unlock();
					notFull.await();
				}
				queue[(tail++ % capacity)] = x;	// Add object to queue and update tail index
				tail = tail % capacity;
				if(enqSize++ == 0)
					wakeupDequeuers = true;		// If size was 0, set signal flag
			} finally {
				enqLock.unlock();
			}
			
			if (wakeupDequeuers) {
				deqLock.lock();
				try {
					notEmpty.signalAll();		// Signal all dequeuing threads 
				} finally {
					deqLock.unlock();
				}
			}
		}
		
		/**
		 * Removes object from the queue
		 * @return
		 * @throws InterruptedException
		 */
		public Object deq() throws InterruptedException{
			Object obj;
			boolean wakeupEnqueuers = false;
			deqLock.lock();
			
			try{
				while (enqSize == 0){
					notEmpty.await();	// wait for signal if queue is empty
				}
				obj = queue[head];		// Get object at head
				queue[head++ % capacity] = null;	// Update head
				head = head % capacity;				// Wrap around
				deqSize--;							// Update size
				if (enqSize == capacity)
					wakeupEnqueuers = true;			// Set signal flag if queue was full
			} finally {
				deqLock.unlock();
			}
			
			if (wakeupEnqueuers){
				enqLock.lock();
				try{
					notFull.signalAll();			// Signal enqueuing threads
				} finally {
					enqLock.unlock();
				}
			}
			
			return obj;
		}
		
		/**
		 * Prints out contents of queue on one line
		 */
		public void print(){
			String s = "HEAD: " + head + " | " + "TAIL: " + tail + " | ";
			for (int i=0; i<capacity; i++){
				s += queue[i] + " , ";
			}
			System.out.println(s);
		}

	}
	
	/*
	 * Question 3.2
	 */
	
	/**
	 * Attempt at a lock-free implementation of a bounded queue using a circular array
	 * @author natha
	 *
	 */
	public static class LFBoundedQueue {
		
		Condition notEmpty, notFull;
		int enqSize;
		int deqSize;
		AtomicInteger head;	// Head index made atomic to enable lock-free implementation
		AtomicInteger tail; // Tail index made atomic to enable lock-free implementation
		int capacity;
		Object[] queue;
		
		/**
		 * Constructor
		 * @param capacity
		 */
		public LFBoundedQueue(int capacity){
			
			this.capacity = capacity;
			
			this.head = new AtomicInteger();
			this.head.set(0);
			this.tail = new AtomicInteger();
			this.tail.set(0);
			
			this.enqSize = 0;
			this.deqSize = 0;
			this.queue = new Object[this.capacity];
		}
		
		/**
		 * Adds object to queue
		 * @param x
		 * @throws InterruptedException
		 */
		public void enq(Object x) throws InterruptedException{
			boolean wakeupDequeuers = false;

			while (enqSize == capacity){	// Update size variables and wait if queue is full
				enqSize += deqSize;
				deqSize = 0;		
				notFull.await();
			}

			queue[tail.getAndUpdate(old -> (old+1 % capacity))] = x;	// Add element to queue and update tail index

			if(enqSize++ == 0)
				wakeupDequeuers = true;
			
			if (wakeupDequeuers) {
				notEmpty.signalAll();		// Signal threads if queue was empty
			}
		}
		
		/**
		 * Removes and returns object from queue
		 * @return
		 * @throws InterruptedException
		 */
		public Object deq() throws InterruptedException{
			Object obj;
			int prevHead = 0;
			boolean wakeupEnqueuers = false;
			
			while (enqSize == 0){
				notEmpty.await();		// Wait if queue is empty
			}
			
			obj = queue[(prevHead = head.getAndUpdate(old -> (old+1 % capacity)))];	// get object at head and update head
			// Non Atomic
			queue[prevHead] = null;			// Set previous head to null
											// This sequence is non-atomic which poses a problem in this lock-free implementation.
											// The position we are setting to null could have been altered from another thread.
			deqSize--;						// Update size
			if (enqSize == capacity)
				wakeupEnqueuers = true;

			if (wakeupEnqueuers){
				notFull.signalAll();		// Signal threads if queue was full
			}
			
			return obj;
		}

	}
}
