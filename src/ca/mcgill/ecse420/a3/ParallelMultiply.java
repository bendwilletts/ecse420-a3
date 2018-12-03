package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ParallelMultiply {
	
	static final int NUM_THREADS = 32;
	static int min_size;
	static ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
	
	public static double[] parallelMultiply(double[][] matrix, double[] vector, int size){
		min_size = size/NUM_THREADS;
		double[] r = new double[size];
		Multiply task = new Multiply(matrix, vector, r, 0, 0, 0, 0, size);
		executor.execute(new Thread(task));
		return task.r;
	}
	
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
			int half = size/2;
			
			if (size < min_size){
				for (int i=0; i<size; i++){
					for (int j=0; j<size; j++){
						r[r_row + i] += matrix[m_row+i][m_col+j] * vector[v_row+j];
					}
				}
			}
			else{
				Multiply[] quadrant = new Multiply[4];
				quadrant[0] = new Multiply(matrix, vector, r, m_col, m_row, v_row, r_row, half);
				quadrant[1] = new Multiply(matrix, vector, r, m_col+half, m_row, v_row+half, r_row, half);
				quadrant[2] = new Multiply(matrix, vector, r, m_col, m_row+half, v_row, r_row+half, half);
				quadrant[3] = new Multiply(matrix, vector, r, m_col+half, m_row+half, v_row+half, r_row+half, half);
				
				for (int i=0; i<4; i++){
					executor.execute(new Thread(quadrant[i]));
				}
				executor.shutdown();
			}
		}
		
	}
}
