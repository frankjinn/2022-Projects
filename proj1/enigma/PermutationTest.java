package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Frank Jin
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkIdTransform() {
        perm = new Permutation("", UPPER);
        checkPerm("identity", UPPER_STRING, UPPER_STRING);
    }

    @Test
    public void testPermute() {
        Alphabet alphabet = new Alphabet();
        Permutation test = new Permutation("(ACBE) (FG)", alphabet);

        assertEquals(2, test.permute(0));
        assertEquals(0, test.permute(4));
        assertEquals('F', test.permute('G'));
        assertEquals('Z', test.permute('Z'));

    }

    @Test
    public void testInverse() {
        Alphabet alphabet = new Alphabet();
        Permutation test = new Permutation("(ACBE) (FG)", alphabet);

        assertEquals(0, test.invert(2));
        assertEquals(4, test.invert(0));
        assertEquals('G', test.invert('F'));
        assertEquals('Z', test.invert('Z'));
        assertEquals('E', test.invert('A'));
    }

    @Test
    public void testAlphabet() {
        Alphabet alphabet = new Alphabet("HELO");
        Permutation test = new Permutation("(HEOL)", alphabet);
        assertEquals(alphabet, test.alphabet());
    }

    @Test
    public void testDerangement() {
        Alphabet alphabet = new Alphabet("HELO");
        Permutation test1 = new Permutation("(HEOL)", alphabet);
        Permutation test2 = new Permutation("(HE)", alphabet);
        assertTrue(test1.derangement());
        assertFalse(test2.derangement());
    }

    @Test
    public void testSize() {
        Alphabet p = new Alphabet("ABCD");
        assertEquals(4, p.size());
        Alphabet p1 = new Alphabet("");
        assertEquals(0, p1.size());
    }

}
