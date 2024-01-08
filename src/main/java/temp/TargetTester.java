package temp;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TargetTester {
    @Test
    public void test1() throws InvocationTargetException, IllegalAccessException, InstantiationException {
        TestTarget testTarget = new TestTarget();
        testTarget.isPrime(0);
    }
}
