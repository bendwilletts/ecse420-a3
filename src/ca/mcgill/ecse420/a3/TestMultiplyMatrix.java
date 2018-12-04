package ca.mcgill.ecse420.a3;

public class TestMultiplyMatrix {

  private static final int MSIZE = 2000;

  // Create a random matrix given dimensions
  private static double[][] initRandomMatrix(int rows, int cols) {
    double m[][] = new double[rows][cols];
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        m[row][col] = (double) ((int) (Math.random() * 10.0));
      }
    }
    return m;
  }

  // Create a random vector given length
  private static double[] initRandomVector(int rows) {
    double v[] = new double[rows];
    for (int row = 0; row < rows; row++) {
      v[row] = (double) ((int) (Math.random() * 10.0));
    }
    return v;
  }

  // Print function for a given vector - Useful for checking results of sequential and parallel
  private static void printVector(double[] v) {
    System.out.print("[ " + v[0]);
    for (int i = 1; i < v.length; i++) {
      System.out.print(", " + v[i]);
    }
    System.out.println("]");
  }

  public static void main(String[] args) {
    // initialize Matrix m and Vector v
    double[][] m = initRandomMatrix(MSIZE, MSIZE);
    double[] v = initRandomVector(MSIZE);

    //Sequential Test + Result
    double startS = System.nanoTime();
    double[] seqResult = SequentialMultiply.seqMult(m, v, MSIZE);
    double endS = System.nanoTime();
    double durationS = (endS-startS) / 1000000000;
    printVector(seqResult);
    
    //Parallelization Test + Result
    double startP = System.nanoTime();
    double[] result = ParallelMultiply.parMult(m, v, MSIZE);
    double endP = System.nanoTime();
    double durationP = (endP-startP) / 1000000000;
    printVector(result);
    
    //Display Time Result
    System.out.println("Sequential Runtime (seconds): " +  durationS);
    System.out.println("Parallel Runtime (seconds): " + durationP);
  }
}
