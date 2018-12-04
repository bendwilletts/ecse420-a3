package ca.mcgill.ecse420.a3;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


public class ParallelMultiply {
	
    static final int NUM_THREADS = 32;
	static int min_size;
	static ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
	
	static class Multiply implements Runnable {
		
		private double[][] matrix;
		private double[] vector, r;
		private int m_col, m_row, v_row, r_row, size;
		
		Multiply(double[][] matrix, double[] vector, double[] r, int m_col, int m_row, int v_row, int r_row, int size){
			this.matrix = matrix;
			this.vector = vector;
			this.r = r;
			this.m_col = m_col;
			this.m_row = m_row;
			this.v_row = v_row;
			this.r_row = r_row;
			this.size = size;
		}
		
		public void run(){
			int mid = size/2;
			
			if (size < min_size){
				for (int i=0; i<size; i++){
					for (int j=0; j<size; j++){
						r[r_row + i] += matrix[m_row+i][m_col+j] * vector[v_row+j];
					}
				}
			}
			else{
				Multiply[] quadrant = new Multiply[4];
				quadrant[0] = new Multiply(matrix, vector, r, m_col, m_row, v_row, r_row, mid);
				quadrant[1] = new Multiply(matrix, vector, r, m_col+mid, m_row, v_row+mid, r_row, mid);
				quadrant[2] = new Multiply(matrix, vector, r, m_col, m_row+mid, v_row, r_row+mid, mid);
				quadrant[3] = new Multiply(matrix, vector, r, m_col+mid, m_row+mid, v_row+mid, r_row+mid, mid);
				
				Runnable m1 = () -> {
				  quadrant[0].run();
				  quadrant[1].run();
	              quadrant[2].run();
	              quadrant[3].run();
				};
//				Runnable m2 = () -> {
//
//				};
				
				FutureTask ft1 = new FutureTask(m1, null);
//				FutureTask ft2 = new FutureTask(m2, null);
				
				ft1.run();
//				ft2.run();
				
//				for (int i=0; i<mid; i++){
//				  r[r_row+i] = quadrant[0].r[r_row+i] + quadrant[1].r[r_row+i];
//				}
//				for (int i=0; i<mid; i++){
//				  r[r_row+mid+i] = quadrant[2].r[r_row+mid+i] + quadrant[3].r[r_row+mid+i];
//				}
				//printVector(quadrant[2].r);
				
				
//				Future[] quadrant = new Future[4];
//				quadrant[0] = executor.submit(new Multiply(matrix, vector, r, m_col, m_row, v_row, r_row, mid));
//				quadrant[1] = executor.submit(new Multiply(matrix, vector, r, m_col+mid, m_row, v_row+mid, r_row, mid));
//				quadrant[2] = executor.submit(new Multiply(matrix, vector, r, m_col, m_row+mid, v_row, r_row+mid, mid));
//				quadrant[3] = executor.submit(new Multiply(matrix, vector, r, m_col+mid, m_row+mid, v_row+mid, r_row+mid, mid));
//				
//				for (int i=0; i<4; i++) {
//				  try {
//                    quadrant[i].get();
//                  } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                  } catch (ExecutionException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                  }
//				}
			}
		 }
		
	   }
	
	   public static double[] parallelMultiply(double[][] matrix, double[] vector, int size){
	        min_size = size/NUM_THREADS;
	        double[] r = new double[size];
	        Multiply task = new Multiply(matrix, vector, r, 0, 0, 0, 0, size);
	        Future fval = executor.submit(task);
	        try {
	          fval.get();
	        } catch (Exception error) {
	          
	        }
	        executor.shutdown();
	        return r;
	    }
	   
	   private static void printVector(double[] vector){
	      System.out.print("| ");
	      for(int i=0;i< vector.length;i++){
	          System.out.print(vector[i]+ " ");
	      }
	      System.out.println("|");
	  }
}
