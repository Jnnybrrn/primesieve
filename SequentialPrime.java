import java.util.*;
import java.io.*;

public class SequentialPrime {

  public static void main(String[] args) {

    long startTime = System.currentTimeMillis();

    int maxNumber = 50000;
    int output_freqency = 10000;

    ArrayList<Integer> primes = new ArrayList<Integer>();

    System.out.println("Calculating primes up to " + maxNumber);

    // for each number
    for(int i = 1; i < maxNumber; i++) {
      if (i % output_freqency == 0) {
        System.out.println("iter: " + i);
      }
      boolean isPrime = true;
      // check 2 -> i
      for(int j = 2; j < i; j++) {
        if (i%j == 0) {
          isPrime = false;
        }
      }
      if (isPrime) {
        primes.add(i);
      }
    }

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
