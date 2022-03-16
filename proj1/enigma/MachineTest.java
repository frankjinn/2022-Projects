package enigma;

import java.util.HashMap;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

/**
 * The suite of all JUnit tests for the Machine class.
 *
 * @author Frank Jin
 */
public class MachineTest {

    /**
     * Testing time limit.
     */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTS ***** */

    private static final Alphabet AZ = new Alphabet(TestUtils.UPPER_STRING);

    private static final HashMap<String, Rotor> ROTORS = new HashMap<>();

    static {
        HashMap<String, String> nav = TestUtils.NAVALA;
        ROTORS.put("B", new Reflector("B", new Permutation(nav.get("B"), AZ)));
        ROTORS.put("Beta",
                new FixedRotor("Beta",
                        new Permutation(nav.get("Beta"), AZ)));
        ROTORS.put("III",
                new MovingRotor("III",
                        new Permutation(nav.get("III"), AZ), "V"));
        ROTORS.put("IV",
                new MovingRotor("IV", new Permutation(nav.get("IV"), AZ),
                        "J"));
        ROTORS.put("I",
                new MovingRotor("I", new Permutation(nav.get("I"), AZ),
                        "Q"));
    }

    private static final String[] ROTORS1 = {"B", "Beta", "III", "IV", "I"};
    private static final String SETTING1 = "AXLE";

    private Machine mach1() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        mach.setRotors(SETTING1);
        return mach;
    }

    @Test
    public void testInsertRotors() {
        Machine mach = new Machine(AZ, 5, 3, ROTORS.values());
        mach.insertRotors(ROTORS1);
        assertEquals(5, mach.numRotors());
        assertEquals(3, mach.numPawls());
        assertEquals(AZ, mach.alphabet());
        assertEquals(ROTORS.get("B"), mach.getRotor(0));
        assertEquals(ROTORS.get("Beta"), mach.getRotor(1));
        assertEquals(ROTORS.get("III"), mach.getRotor(2));
        assertEquals(ROTORS.get("IV"), mach.getRotor(3));
        assertEquals(ROTORS.get("I"), mach.getRotor(4));
    }

    @Test
    public void testAdvance() {
        HashMap<String, Rotor> testRotor = new HashMap<>();
        Alphabet testAlpha = new Alphabet("ABC");
        testRotor.put("testReflector", new Reflector("testReflector",
                new Permutation("(AB) (CD)", AZ)));
        testRotor.put("R2", new MovingRotor("R2",
                new Permutation("(AEFG) (DJ)", testAlpha), "C"));
        testRotor.put("R3", new MovingRotor("R3",
                new Permutation("(AEFG) (DJ)", testAlpha), "C"));
        testRotor.put("R4", new MovingRotor("R4",
                new Permutation("(AEFG) (DJ)", testAlpha), "C"));
        Machine mach = new Machine(testAlpha, 4, 3, testRotor.values());
        String[] rotor1 = {"testReflector", "R2", "R3", "R4"};
        mach.insertRotors(rotor1);
        mach.setRotors("AAA");

        String[] positions = new String[19];
        String[] expected = {"AAAB", "AAAC", "AABA", "AABB",
                             "AABC", "AACA", "ABAB", "ABAC",
                             "ABBA", "ABBB", "ABBC", "ABCA",
                             "ACAB", "ACAC", "ACBA", "ACBB",
                             "ACBC", "ACCA", "AAAB"};

        for (int i = 0; i < 19; i++) {
            mach.convert("A");
            String settings = "";
            for (Rotor x : mach.getRotorList()) {
                settings += "" + testAlpha.toChar(x.setting());
            }
            positions[i] = settings;
        }

        assertArrayEquals(expected, positions);
    }

    @Test
    public void testConvertChar() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(YF) (HZ)", AZ));
        assertEquals(25, mach.convert(24));
    }

    @Test
    public void testConvertMsg() {
        Machine mach = mach1();
        mach.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", AZ));
        assertEquals("QVPQSOKOILPUBKJZPISFXDW",
                mach.convert("FROMHISSHOULDERHIAWATHA"));
    }
}
