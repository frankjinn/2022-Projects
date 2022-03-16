package enigma;

/**
 * Superclass that represents a rotor in the enigma machine.
 *
 * @author Frank Jin
 */
class Rotor {

    /**
     * A rotor named NAME whose permutation is given by PERM.
     */
    Rotor(String name, Permutation perm) {
        _name = name;
        _permutation = perm;
        _setting = 0;
        _ringstell = 0;
    }

    /**
     * Return my name.
     */
    String name() {
        return _name;
    }

    /**
     * Return my alphabet.
     */
    Alphabet alphabet() {
        return _permutation.alphabet();
    }

    /**
     * Return my permutation.
     */
    Permutation permutation() {
        return _permutation;
    }

    /**
     * Return the size of my alphabet.
     */
    int size() {
        return _permutation.size();
    }

    /**
     * Return true iff I have a ratchet and can move.
     */
    boolean rotates() {
        return false;
    }

    /**
     * Return true iff I reflect.
     */
    boolean reflecting() {
        return false;
    }

    /**
     * Return my current setting.
     */
    int setting() {
        return _setting;
    }

    /**
     * Set setting() to POSN.
     * Is POSN always in the alphabet index?
     */
    void set(int posn) {
        _setting = _permutation.wrap(posn);
    }

    /**
     * Set setting() to character CPOSN.
     */
    void set(char cposn) {
        _setting = alphabet().toInt(cposn);
    }

    /**
     * Return the conversion of P (P is within alphabet index)
     * according to my permutation.
     */
    int convertForward(int p) {
        int contactOutput = _permutation.permute(inputToContact(p));
        int result = contactToOutput(contactOutput);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return result;
    }

    /**
     * Return the conversion of E (E is within alphabet index)
     * according to the inverse of my permutation.
     */
    int convertBackward(int e) {
        int contactOutput = _permutation.invert(inputToContact(e));
        int result = contactToOutput(contactOutput);
        if (Main.verbose()) {
            System.err.printf("%c -> ", alphabet().toChar(result));
        }
        return result;
    }

    /**
     * Returns the positions of the notches, as a string giving the letters
     * on the ring at which they occur.
     */
    String notches() {
        return "";
    }

    /**
     * Returns true iff I am positioned to allow the rotor to my left
     * to advance.
     */
    boolean atNotch() {
        return notches().contains(_permutation.alphabet().
                toChar(_setting) + "");
    }

    /**
     * Advance me one position, if possible. By default, does nothing.
     */
    void advance() {
    }

    /**
     * Finds the forward conversion of raw input to rotor contact input.
     * Takes in int withing alphabet index
     * Results may be greater than alphabet index
     * @param input the position that input is at
     * @return returns the contact that the input in connected to
     **/
    int inputToContact(int input) {
        return input - _ringstell + setting();
    }

    /**
     * Finds the forward conversion of raw input to rotor contact input.
     * Results may be greater than alphabet index. Additional feature
     * of mod alphabet size needed here, since subtracting setting may cause
     * negative values.
     * @param contact the contact that is outputted by the machine
     * @return returns the actual output after accounting for setting
     **/
    int contactToOutput(int contact) {
        return _permutation.wrap(contact + _ringstell - setting());
    }

    @Override
    public String toString() {
        return "Rotor " + _name;
    }

    /**
     * Set _ringstell to character CPOSN.
     */
    void setRingstell(char cposn) {
        _ringstell = alphabet().toInt(cposn);
    }

    /**
     * Set setting() to POSN.
     * Is POSN always in the alphabet index?
     */
    void setRingstell(int posn) {
        _ringstell = _permutation.wrap(posn);
    }

    /**
     * Return my ringStell setting.
     */
    public int getRingStell() {
        return _ringstell;
    }

    /**
     * My name.
     */
    private final String _name;

    /**
     * The permutation implemented by this rotor in its 0 position.
     */
    private final Permutation _permutation;

    /**
     * Numerical position of Rotor.
     */
    private int _setting;

    /**
     * The ringstellung setting.
     */
    private int _ringstell;
}
