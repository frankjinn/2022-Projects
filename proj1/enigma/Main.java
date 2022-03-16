package enigma;

import ucb.util.CommandArgs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static enigma.EnigmaException.*;
import static java.lang.Integer.parseInt;

/**
 * Enigma simulator.
 *
 * @author Frank Jin
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages.  Otherwise, input comes from the standard
     * input.  ARGS[2] is optional; when present, it names an output
     * file for processed messages.  Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            CommandArgs options =
                    new CommandArgs("--verbose --=(.*){1,3}", args);
            if (!options.ok()) {
                throw error("Usage: java enigma.Main [--verbose] "
                        + "[INPUT [OUTPUT]]");
            }

            _verbose = options.contains("--verbose");
            new Main(options.get("--")).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /**
     * Open the necessary files for non-option arguments ARGS (see comment
     * on main).
     */
    Main(List<String> args) {
        _config = getInput(args.get(0));

        if (args.size() > 1) {
            _input = getInput(args.get(1));
        } else {
            _input = new Scanner(System.in);
        }

        if (args.size() > 2) {
            _output = getOutput(args.get(2));
        } else {
            _output = System.out;
        }
    }

    /**
     * Return a Scanner reading from the file named NAME.
     */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Return a PrintStream writing to the file named NAME.
     */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(name);
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {
        try {
            Machine currentMachine = readConfig();
            while (_input.hasNext()) {
                String nextLine = _input.nextLine();
                while (nextLine.trim().length() == 0) {
                    _output.println();
                    nextLine = _input.nextLine();
                }
                if (nextLine.charAt(0) == '*') {
                    setUp(currentMachine, nextLine);
                } else {
                    printMessageLine(currentMachine.convert(nextLine));
                }
            }
        } catch (NullPointerException err) {
            throw error("config file error", err.getMessage());
        }
    }

    /**
     * Return an Enigma machine configured from the contents of configuration
     * file _config.
     */
    private Machine readConfig() {
        try {
            _alphabet = new Alphabet(_config.nextLine());
            String rotorLine = _config.nextLine();
            Matcher rotorAndPawl =
                    Pattern.compile("(\\d+)\\s+(\\d+)").matcher(rotorLine);
            rotorAndPawl.find();
            int rotor = parseInt(rotorAndPawl.group(1));
            int pawl = parseInt(rotorAndPawl.group(2));
            readRotor();
            return new Machine(_alphabet, rotor,
                               pawl, rotors);
        } catch (Exception excp) {
            throw error("configuration file truncated");
        }
    }

    private void readRotor() {
        try {
            StringBuilder rotorOptions = new StringBuilder();
            while (_config.hasNextLine()) {
                rotorOptions.append(_config.nextLine());
            }
            String rotorName = "";
            String rotorPerm = "";
            String typeAndNotch = "";

            String regex = "\\s*(\\w+)\\s(\\w+)\\s|\\((.*?)\\)";
            Matcher rotorMatch = Pattern.compile(regex).
                                 matcher(rotorOptions.toString());
            while (rotorMatch.find()) {
                String match = rotorMatch.group();
                if (match.matches("(\\((.*?)\\))")) {
                    rotorPerm += match + " ";
                } else {
                    String[] info = {rotorMatch.group(1), rotorMatch.group(2)};
                    if (!rotorName.equals("")) {
                        rotorAdd(rotorName, rotorPerm, typeAndNotch);
                    }
                    rotorName = info[0];
                    typeAndNotch = info[1];
                    rotorPerm = "";
                }

            }
            rotorAdd(rotorName, rotorPerm, typeAndNotch);
        } catch (Exception excp) {
            throw error("bad rotor description");
        }
    }

    private void rotorAdd(String rotorName, String rotorPerm,
                          String typeAndNotch) {
        switch (typeAndNotch.charAt(0)) {
        case 'M' ->
            rotors.add(new MovingRotor(rotorName,
                new Permutation(rotorPerm, _alphabet),
                typeAndNotch.substring(1)));
        case 'N' ->
            rotors.add(new FixedRotor(rotorName,
                new Permutation(rotorPerm, _alphabet)));
        case 'R' ->
            rotors.add(new Reflector(rotorName,
                new Permutation(rotorPerm, _alphabet)));
        default ->
            throw error("error adding rotor");
        }
    }
    /**
     * Set M according to the specification given on SETTINGS,
     * which must have the format specified in the assignment.
     */
    private void setUp(Machine M, String settings) {
        try {
            String[] inputParams = settings.split(" ");
            StringBuilder plugBoard = new StringBuilder();
            String setting;
            String[] rotorNames = new String[M.numRotors()];
            String ringstell = "";
            for (int i = 0; i < M.numRotors(); i++) {
                rotorNames[i] = inputParams[i + 1];
            }
            setting = inputParams[M.numRotors() + 1];
            if (inputParams.length >= M.numRotors() + 3) {
                if (inputParams[M.numRotors() + 2].matches("\\(.*?\\)")) {
                    for (int i = M.numRotors() + 2;
                         i < inputParams.length; i++) {
                        plugBoard.append(inputParams[i]);
                    }
                } else {
                    ringstell = inputParams[M.numRotors() + 2];
                    for (int i = M.numRotors() + 3;
                         i < inputParams.length; i++) {
                        plugBoard.append(inputParams[i]);
                    }
                }
            }
            M.insertRotors(rotorNames);
            M.setRotors(setting);
            M.setPlugboard(new Permutation(plugBoard.toString(), _alphabet));
            if (!ringstell.equals("")) {
                M.setRingStell(ringstell);
            }
        } catch (Exception e) {
            throw error("Could not set up, check rotor settings",
                        e.getMessage());
        }
    }

    /**
     * Return true iff verbose option specified.
     */
    static boolean verbose() {
        return _verbose;
    }

    /**
     * Print MSG in groups of five (except that the last group may
     * have fewer letters).
     */
    private void printMessageLine(String msg) {
        int counter = 5;
        int msgLen = msg.length();
        StringBuilder sb = new StringBuilder(msg);
        while (counter <= msgLen) {
            sb.insert(counter, ' ');
            counter += 6;
            msgLen += 1;
        }
        _output.println(sb);
    }

    /**
     * Alphabet used in this machine.
     */
    private Alphabet _alphabet;

    /**
     * Source of input messages.
     */
    private Scanner _input;

    /**
     * Source of machine configuration.
     */
    private Scanner _config;

    /**
     * File for encoded/decoded messages.
     */
    private PrintStream _output;

    /**
     * True if --verbose specified.
     */
    private static boolean _verbose;

    /**
     * Linked list of all available rotors.
     **/
    private LinkedList<Rotor> rotors = new LinkedList<>();
}
