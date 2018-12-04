package ca.mcgill.ecse420.a3;

public class SequentialMultiply {
  
  public static double[] sequentialMultiply(double[][] matrix, double[] vector, int matrix_size) {
    double[] result = new double[matrix_size];
    for (int i=0;i<matrix_size;i++){
        for (int j=0;j<matrix_size;j++) {
            result[i] = 0;
            for (int k = 0; k < matrix_size; k++) {
                result[i] += matrix[i][k] * vector[k];
            }
        }
    }
    return result;
  }
  
}
