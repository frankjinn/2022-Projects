package enigma;

import org.junit.Test;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Rotor class.
 *  @author Frank Jin
 */
public class RotorTest {
    @Test
    public void checkIdTransform() {
        Permutation testPermutation = new Permutation("(ADBCE) (ZYKP)", UPPER);
        Rotor testRotor = new Rotor("testRotor", testPermutation);
        assertEquals("testRotor", testRotor.name());
        assertEquals(26, testRotor.size());

        testRotor.set(9);
        assertEquals(9, testRotor.setting());
        testRotor.set(30);
        assertEquals(4, testRotor.setting());

        System.out.println(testRotor);
    }
}
