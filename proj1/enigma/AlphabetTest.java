package enigma;

import org.junit.Test;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the Alphabet class.
 *  @author Frank Jin
 */
public class AlphabetTest {
    /** General Testing */
    @Test
    public void test1() {
        Alphabet test = new Alphabet("ABCD");

        assertEquals(4, test.size());

        assertTrue(test.contains('B'));
        assertFalse(test.contains('E'));

        assertEquals(1, test.toInt('B'));
        assertEquals('C', test.toChar(2));
    }

    @Test
    public void test2() {
        String testString = "abcdef1234";
        Alphabet test = new Alphabet(testString);
        assertEquals(10, test.size());
        for (int i = 0; i < test.size(); i++) {
            char curr = testString.charAt(i);
            assertEquals(curr, test.toChar(i));
        }

        assertFalse(test.contains('A'));

    }
}
