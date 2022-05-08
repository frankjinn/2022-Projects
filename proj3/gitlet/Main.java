package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Frank Jin
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (args.length == 1 && args[0].equals("init")) {
            if (!_initialized) {
                _repo = new CommitTree();
            } else {
                System.out.println("Gitlet version-control system already "
                        + "exists in the current directory.");
                return;
            }
        } else if (!_initialized) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        } else {
            _repo = Utils.readObject(Utils.join(_repositoryDir,
                    ".gitlet/commitTreeFile"), CommitTree.class);
            commandSwitch(args);
        }
        Utils.writeObject(Utils.join(_repositoryDir,
                ".gitlet/commitTreeFile"),
                _repo);
        System.exit(0);
    }
    public static void commandSwitch(String[] args) throws IOException {
        if (args.length == 1) {
            switch (args[0]) {
            case "log":
                _repo.log();
                break;
            case "global-log":
                _repo.globalLog();
                break;
            case "status":
                _repo.status();
                break;
            default:
                System.out.println("No command with that name exists");
            }
        } else if (args.length == 2) {
            switch (args[0]) {
            case "add":
                _repo.add(args[1]);
                break;
            case "commit":
                _repo.commit(args[1], _repo.getRmvList(), null,
                        null, false, false);
                break;
            case "rm":
                _repo.rm(args[1]);
                break;
            case "find":
                _repo.find(args[1]);
                break;
            case "checkout":
                _repo.checkoutBranch(args[1]);
                break;
            case "branch":
                _repo.branch(args[1]);
                break;
            case "rm-branch":
                _repo.rmbranch(args[1]);
                break;
            case "reset":
                _repo.reset(args[1]);
                break;
            case "merge":
                _repo.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists");
            }
        } else if (args[0].equals("checkout") && args[1].equals("--")
                && args.length == 3) {
            _repo.checkoutFile(args[2]);
        } else if (args[0].equals("checkout") && args[2].equals("--")
                && args.length == 4) {
            _repo.checkoutFile(args[1], args[3]);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    public static File getRepositoryDir() {
        return _repositoryDir;
    }

    /** Directory to the repository. **/
    private static File _repositoryDir =
            new File(System.getProperty("user.dir"));

    /** True if repo is initialized. **/
    private static boolean _initialized =
            new File(_repositoryDir, "/.gitlet").exists();

    /** The commit tree for the repository. **/
    private static CommitTree _repo;
}
