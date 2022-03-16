package enigma;

import org.junit.Test;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the FixedRotor class.
 *  @author Frank Jin
 */
public class FixedRotorTest {
    @Test
    public void fixedRotorTest() {
        Permutation testPerm = new Permutation("(ACBE) (FG)", UPPER);
        FixedRotor test = new FixedRotor("testRotor", testPerm);

        assertEquals(0, test.setting());
        assertEquals(0, test.convertForward(4));
        assertEquals(4, test.convertBackward(0));

        test.set(3);
        assertEquals(0, test.convertForward(0));
        assertEquals(1, test.convertForward(24));
        assertEquals(25, test.convertForward(23));
        assertEquals(1, test.convertBackward(23));

        assertEquals(16, test.convertForward(16));

        test.advance();
        assertEquals(3, test.setting());
    }
}
