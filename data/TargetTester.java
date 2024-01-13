import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

public class TargetTester {
    public TargetTester() {
    }

    @Test
    public void test1() throws InvocationTargetException, IllegalAccessException, InstantiationException {
        TestTarget testTarget = new TestTarget();
        testTarget.isPrime(0);
        testTarget.add(1, 2);
    }
}
