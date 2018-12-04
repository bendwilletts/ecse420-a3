package ca.mcgill.ecse420.a3;

public class SequentialMultiply {
  
  public static double[] seqMult(double[][] m, double[] v, int size) {
    System.out.println("Starting Sequential Multiplication...");
    double[] r = new double[size];
    for (int i=0;i<size;i++){
        for (int j=0;j<size;j++) {
            r[i] = 0;
            for (int k = 0; k < size; k++) {
                r[i] += m[i][k] * v[k];
            }
        }
    }
    return r;
  }
  
}
