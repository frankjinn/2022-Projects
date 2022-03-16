package enigma;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Frank Jin
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        this.rotorNotches = notches;
        shouldAdvance = false;
    }

    /** shouldAdvance getter.
     * @return returns if rotor should advance */
    public Boolean getShouldAdvance() {
        return shouldAdvance;
    }

    /** shouldAdvance setter.
     * @param setting input */
    public void setShouldAdvance(Boolean setting) {
        shouldAdvance = setting;
    }

    @Override
    void advance() {
        set(setting() + 1);
        shouldAdvance = false;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    String notches() {
        return rotorNotches;
    }

    /** Boolean if rotor should advance. */
    private Boolean shouldAdvance;

    /** Notches on this rotor. */
    private String rotorNotches;
}
