/*
 * This file was automatically generated by EvoSuite
 * Sun Jan 14 15:50:11 GMT 2024
 */


import org.junit.Test;
import static org.junit.Assert.*;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true) 
public class TestTarget_ESTest extends TestTarget_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      TestTarget testTarget0 = new TestTarget();
      int int0 = testTarget0.add(0, 0);
      assertEquals(0, int0);
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      TestTarget testTarget0 = new TestTarget();
      int int0 = testTarget0.add(0, (-1));
      assertEquals((-1), int0);
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      TestTarget testTarget0 = new TestTarget();
      int int0 = testTarget0.add(398, 398);
      assertEquals(796, int0);
  }
}
