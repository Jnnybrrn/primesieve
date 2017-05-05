// BROKEN - DOES NOT WORK

import mpi.*;

import java.util.*;
import java.io.*;

public class ParallelPrimeEratosthenes {
  final static int maxNumber = 300;

  final static int BLOCK_SIZE = 100; // how many numbers to pass out

  final static int NUM_BLOCKS = maxNumber / BLOCK_SIZE;
  final static int BUFFER_SIZE = 2 + BLOCK_SIZE * NUM_BLOCKS;

  // tag values
  final static int TAG_HELLO   = 0 ;
  final static int TAG_RESULT  = 1 ;
  final static int TAG_TASK    = 2 ;
  final static int TAG_GOODBYE = 3 ;

  static ArrayList<Integer> primes = new ArrayList<Integer>();

  public static void main(String[] args) {

    MPI.Init(args);

    int me = MPI.COMM_WORLD.Rank();
    int P = MPI.COMM_WORLD.Size();

    int numWorkers = P - 1;

    int [] buffer = new int [BUFFER_SIZE] ;

    for (int i=2; i < buffer.length; i++) {
      buffer[i] = i-1;
    }

    if (me == 0) { // master
      long startTime = System.currentTimeMillis();
      System.out.println("Calculating primes up to " + maxNumber);

      int nextBlockStart = 0 ;
      int nextNumToCheck = 1;
      int numHellos = 0 ;
      int numBlocksReceived = 0 ;

      // Handle workers
      while(numBlocksReceived < NUM_BLOCKS*maxNumber || numHellos < numWorkers || nextNumToCheck < maxNumber) {

        // Receive from workers
        Status status = MPI.COMM_WORLD.Recv(buffer, 0, BUFFER_SIZE, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);

        if(status.tag == TAG_RESULT) {

          // Add returned Prime numbers from blockstart -> blockEnd to primes
          int resultBlockStart = buffer [0];
          for(int i=0; i < BLOCK_SIZE; i++) {
            int number = buffer[1 + resultBlockStart + i];
            if (number != 0) {
              primes.add(number);
            }
          }
          numBlocksReceived++;

        } else { // TAG_HELLO
          numHellos++;
        }

        // Send work
        if(nextNumToCheck < maxNumber) {
          if(nextBlockStart < maxNumber) {
            buffer [0] = nextBlockStart;
            buffer [1] = nextNumToCheck;
            MPI.COMM_WORLD.Send(buffer, 0, 1, MPI.INT, status.source, TAG_TASK);
            nextBlockStart += BLOCK_SIZE;
            System.out.println("Sending block starting " + nextBlockStart +" to " + status.source + " with numToCheck = " + nextNumToCheck);
          } else {
            nextBlockStart = 0;
            nextNumToCheck++;
          }
        }
        else { // shutdown, we're done
          MPI.COMM_WORLD.Send(buffer, 0, 0, MPI.INT, status.source, TAG_GOODBYE);
          System.out.println("Sending shutdown signal to " + status.source);
        }

      }

      // Sort the results
      Collections.sort(primes);
      // now drop the 0's
      primes.removeAll(Collections.singleton(0));

      // Finished, write to file
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

    } else { // worker

      // Request work
      MPI.COMM_WORLD.Send(buffer, 0, 0, MPI.INT, 0, TAG_HELLO);

      boolean working = true;
      while(working) {
        Status status = MPI.COMM_WORLD.Recv(buffer, 0, 1, MPI.INT, 0, MPI.ANY_TAG);


        if(status.tag == TAG_TASK) { // we got work
          int blockStart = buffer[0];
          int numToCheck = buffer[1];
          int blockEnd = blockStart + BLOCK_SIZE;

            for(int i = 0; i <= blockEnd; i++) {
              int factor = numToCheck;
              // If factor is prime. Multiples of factor are not..
                for(int j = 1; j*factor <= blockEnd; j++) {
                  buffer[blockStart + i + 2 ] = 0;
                }
              }
          // Send back our primes
          buffer[0] = blockStart;
          buffer[1] = numToCheck;
          MPI.COMM_WORLD.Send(buffer, 0, BUFFER_SIZE, MPI.INT, 0, TAG_RESULT);
        } else {  // we got goodbye, shutdown
          working = false;
        }
      }
    }
    MPI.Finalize();
  }
}
