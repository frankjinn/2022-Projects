package enigma;

import java.util.Collection;

import static enigma.EnigmaException.error;

/**
 * Class that represents a complete enigma machine.
 *
 * @author Frank Jin
 */
class Machine {

    /**
     * A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     * and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     * available rotors.
     */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotorsArray = allRotors.toArray(Rotor[]::new);
        _plugboard = new Permutation("", _alphabet);
        _rotors = new Rotor[_numRotors];
    }

    /**
     * Return the number of rotor slots I have.
     */
    int numRotors() {
        return _numRotors;
    }

    /**
     * Return the number pawls (and thus rotating rotors) I have.
     */
    int numPawls() {
        return _pawls;
    }

    /**
     * Return Rotor #K, where Rotor #0 is the reflector, and Rotor
     * #(numRotors()-1) is the fast Rotor.  Modifying this Rotor has
     * undefined results.
     */
    Rotor getRotor(int k) {
        return _rotors[k];
    }

    Rotor[] getRotorList() {
        return _rotors;
    }

    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Set my rotor slots to the rotors named ROTORS from my set of
     * available rotors (ROTORS[0] names the reflector).
     * Initially, all rotors are set at their 0 setting.
     */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (Rotor search : _allRotorsArray) {
                if (rotors[i].equals(search.name())) {
                    _rotors[i] = search;
                    break;
                }
            }
        }
    }

    /**
     * Set my rotors according to SETTING, which must be a string of
     * numRotors()-1 characters in my alphabet. The first letter refers
     * to the leftmost rotor setting (not counting the reflector).
     */
    void setRotors(String setting) {
        char[] settingChar = setting.toCharArray();
        int charIndex = 0;
        int movingRotorCount = 0;
        for (int i = 1; i < _numRotors; i++) {
            _rotors[i].set(settingChar[charIndex]);
            charIndex += 1;
            if (_rotors[i] instanceof MovingRotor) {
                movingRotorCount += 1;
            }
        }
        if (movingRotorCount != numPawls()) {
            throw error("Moving rotors do not match pawls");
        }
    }

    /**
     * Return the current plugboard's permutation.
     */
    Permutation plugboard() {
        return _plugboard;
    }

    /**
     * Set the plugboard to PLUGBOARD.
     */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /**
     * Returns the result of converting the input character C (as an
     * index in the range 0..alphabet size - 1), after first advancing
     * the machine.
     */
    int convert(int c) {
        advanceRotors();
        if (Main.verbose()) {
            System.err.printf("[");
            for (int r = 1; r < numRotors(); r += 1) {
                System.err.printf("%c",
                        alphabet().toChar(getRotor(r).setting()));
            }
            System.err.printf("] %c -> ", alphabet().toChar(c));
        }
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(c));
        }
        c = applyRotors(c);
        c = plugboard().permute(c);
        if (Main.verbose()) {
            System.err.printf("%c%n", alphabet().toChar(c));
        }
        return c;
    }

    /**
     * Condtions for advancement:
     * First make sure rotor is a moving rotor
     * If it is, then it must meet one condition
     * 1. Fast rotor
     * 2. Rotor in notch AND next rotor is a rotating rotor
     * 3. Rotor before was in notch
     * @param index the index of the rotor being checked
     */
    private void checkAdvance(int index) {
        try {
            if (index == _numRotors - 1) {
                ((MovingRotor) _rotors[index]).setShouldAdvance(true);
            }

            if (_rotors[index].atNotch()
                    && _rotors[index - 1] instanceof MovingRotor) {
                ((MovingRotor) _rotors[index]).setShouldAdvance(true);
                ((MovingRotor) _rotors[index - 1]).setShouldAdvance(true);

            }
        } catch (Exception e) {
            throw error("Could not advance, check rotors", e.getMessage());
        }
    }

    /**
     * Advance all rotors to their next position.
     */
    private void advanceRotors() {
        for (int i = _numRotors - 1; i >= _numRotors - _pawls; i--) {
            if (_rotors[i] instanceof MovingRotor) {
                checkAdvance(i);
                if (((MovingRotor) _rotors[i]).getShouldAdvance()) {
                    _rotors[i].advance();
                }
            }
        }
    }

    /**
     * Return the result of applying the rotors to the character C (as an
     * index in the range 0..alphabet size - 1).
     */
    private int applyRotors(int c) {
        int currentLetter = c;
        for (int i = _numRotors - 1; i >= 0; i--) {
            currentLetter = _rotors[i].convertForward(currentLetter);
        }
        for (int i = 1; i < _numRotors; i++) {
            currentLetter = _rotors[i].convertBackward(currentLetter);
        }
        return currentLetter;
    }

    /**
     * Returns the encoding/decoding of MSG, updating the state of
     * the rotors accordingly.
     */
    String convert(String msg) {
        String outputString = "";
        String msgNoWhitespace = msg.replaceAll(" ", "");
        for (int i = 0; i < msgNoWhitespace.length(); i++) {
            int convertedLetter =
                    convert(_alphabet.toInt(msgNoWhitespace.charAt(i)));
            outputString += _alphabet.toChar(convertedLetter);
        }
        return outputString;
    }
    void setRingStell(String setting) {
        char[] settingChar = setting.toCharArray();
        int charIndex = 0;
        for (int i = 1; i < _numRotors; i++) {
            _rotors[i].setRingstell(settingChar[charIndex]);
            charIndex += 1;
        }
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _pawls;

    /** All rotors in an Array. */
    private Rotor[] _allRotorsArray;

    /** Rotors for current machine. */
    private Rotor[] _rotors;

    /** Plugboard permutation. */
    private Permutation _plugboard;
}
