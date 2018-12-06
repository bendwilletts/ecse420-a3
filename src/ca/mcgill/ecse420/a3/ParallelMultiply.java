package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


public class ParallelMultiply {
	
    static final int NUM_THREADS = 8; //No. of Threads used
	static int min_size; // Minimum subset size that determines when to start computing multiplication
	static ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS); //Executor with pool of threads
	
	static class Multiply implements Runnable {
		
		private double[][] m;
		private double[] v, r;
		private int m_col, m_row, v_row, r_row, size;
		
		Multiply(double[][] m, double[] v, double[] r, int m_col, int m_row, int v_row, int r_row, int size){ //Constructor
			this.m = m;
			this.v = v;
			this.r = r;
			this.m_col = m_col;
			this.m_row = m_row;
			this.v_row = v_row;
			this.r_row = r_row;
			this.size = size;
		}
		
		public void run(){
			int mid = size/2;
			
			if (size > min_size){ //since curr size greater than min_size, must split matrix into 4 (mid X mid) quadrants
			                       //vectors v and r are split into 2 (length mid) subvectors
              Multiply[] quadrant = new Multiply[4];
              quadrant[0] = new Multiply(m, v, r, m_col, m_row, v_row, r_row, mid);
              quadrant[1] = new Multiply(m, v, r, m_col+mid, m_row, v_row+mid, r_row, mid);
              quadrant[2] = new Multiply(m, v, r, m_col, m_row+mid, v_row, r_row+mid, mid);
              quadrant[3] = new Multiply(m, v, r, m_col+mid, m_row+mid, v_row+mid, r_row+mid, mid);
              
              Runnable m1 = () -> { //Create new runnable that continues Multiply on subquadrants
                quadrant[0].run();
                quadrant[1].run();
                quadrant[2].run();
                quadrant[3].run();
              };
              
              FutureTask ft1 = new FutureTask(m1, null); //Uses new runnable as new asynchronous computation       
              ft1.run(); //Execute runnable to continue multiplication
			}
			else{ //Once matrix/vector size small enough, proceed with computing for current subsection
                for (int i=0; i<size; i++){
                  for (int j=0; j<size; j++){
                      r[r_row + i] += m[m_row+i][m_col+j] * v[v_row+j];
                  }
                }
			}
		 }
		
	   }
	
	   public static double[] parMult(double[][] m, double[] v, int size) {
	        System.out.println("Starting Parallel Multiplication...");
	        System.out.println("No. of Threads: " + NUM_THREADS);
	        min_size = size / (int)(Math.log(NUM_THREADS)/Math.log(4)*2);

	        double[] r = new double[size];
	        Multiply task = new Multiply(m, v, r, 0, 0, 0, 0, size);
	        Future fval = executor.submit(task); //Submit Multiply task to compute
	        try {
              fval.get(); //Blocks method until computation complete
            } catch (InterruptedException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            } catch (ExecutionException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
	        executor.shutdown();
	        return r;
	    }
}
