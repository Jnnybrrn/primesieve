import java.util.*;
import java.io.*;

public class SequentialPrimeEratosthenes {

  public static void main(String[] args) {

    long startTime = System.currentTimeMillis();

    int maxNumber = 300000;

    ArrayList<Integer> primes = new ArrayList<Integer>(maxNumber);
    for (int i = 1; i <= maxNumber; i++){
      primes.add(i);
    }

    System.out.println("Calculating primes up to " + maxNumber);

    // for each factor
    for(int i = 2; i*i <= maxNumber; i++) {
      int factor = primes.get(i-1);
      // If factor is prime. Multiples of factor are not..
      if (factor != 0) {
          for(int j = i; j*i <= maxNumber; j++) {
            int arrIndex = i*j;
            primes.set(arrIndex-1, 0);
          }
      }
    }

    // now drop the 0's
    primes.removeAll(Collections.singleton(0));

    long endTime = System.currentTimeMillis();

    System.out.println("Calculation completed in " + (endTime - startTime) + " milliseconds");
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter("primes.txt", true));
      for(int i: primes) {
        writer.write(i + "\n");
      }
      System.out.println("Prime numbers (up to " + maxNumber + "): " + primes.size() + " primes written to primes.txt ");
    } catch (IOException e) {
      System.out.println("Writing to file failed - writing to STDOUT instead");
      System.out.println(primes.toString());
    } finally {
      if (writer != null) {
        try {
          writer.close();
        }
        catch (IOException e) {
          // nothing
        }
      }
    };

  }
}
