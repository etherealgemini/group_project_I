/*
 * This file was automatically generated by EvoSuite
 * Sun Jan 14 15:51:17 GMT 2024
 */


import org.junit.Test;
import static org.junit.Assert.*;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true) 
public class TestTargetPar_ESTest extends TestTargetPar_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      TestTargetPar testTargetPar0 = new TestTargetPar();
      boolean boolean0 = testTargetPar0.isPrime(841);
      assertTrue(boolean0);
  }
}
