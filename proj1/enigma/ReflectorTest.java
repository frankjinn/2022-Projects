package enigma;
import org.junit.Test;

import static enigma.TestUtils.UPPER;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the Reflector class.
 *  @author Frank Jin
 */
public class ReflectorTest {
    @Test
    public void test1() {
        Permutation testPerm = new Permutation("(AC) (BE) (FG)", UPPER);
        FixedRotor test = new Reflector("testReflector", testPerm);

        assertEquals(0, test.setting());
        assertEquals(1, test.convertForward(4));
        assertEquals(6, test.convertBackward(5));

        test.advance();
        assertEquals(0, test.setting());
    }
}
