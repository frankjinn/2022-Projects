package enigma;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Frank Jin
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        Matcher bracketMatch = Pattern.compile("\\((.*?)\\)").matcher(cycles);
        while (bracketMatch.find()) {
            addCycle(bracketMatch.group(1));
        }
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        for (int i = 0; i < cycle.length(); i++) {
            if (i < cycle.length() - 1) {
                codeHash.put(cycle.charAt(i), cycle.charAt(i + 1));
                reverseCodeHash.put(cycle.charAt(i + 1), cycle.charAt(i));
            } else {
                codeHash.put(cycle.charAt(i), cycle.charAt(0));
                reverseCodeHash.put(cycle.charAt(0), cycle.charAt(i));
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. Converting this into an int */
    int permute(int p) {
        char input = _alphabet.toChar(wrap(p));
        if (codeHash.containsKey(input)) {
            char permutationResult = codeHash.get(input);
            return _alphabet.toInt(permutationResult);
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char input = _alphabet.toChar(wrap(c));
        if (reverseCodeHash.containsKey(input)) {
            char permutationResult = reverseCodeHash.get(input);
            return _alphabet.toInt(permutationResult);
        }
        return c;

    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET. */
    char permute(char p) {
        if (codeHash.containsKey(p)) {
            return codeHash.get(p);
        }
        return p;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (reverseCodeHash.containsKey(c)) {
            return reverseCodeHash.get(c);
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). Checks if
     *  keys in hashmap covers all elements of alphabet. */
    boolean derangement() {
        if (codeHash.size() == _alphabet.size()) {
            return true;
        }
        return false;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Hashmap of forward conversion*. */
    private HashMap<Character, Character> codeHash = new HashMap<>();
    /** Hashmap of backward conversion*. */
    private HashMap<Character, Character> reverseCodeHash = new HashMap<>();
}
