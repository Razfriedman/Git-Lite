package gitlet;


import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Raz Friedman (collaborators: Jai Bansal, Nick Majamaki)
 */
public class Main extends Commands {

    /** List of all the commands in gitlet. */
    static final String[] COMMAND = {
        "init", "status", "log", "global-log",
        "add", "commit", "rm", "find", "checkout",
        "branch", "rm-branch", "reset", "merge"
    };


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            if (args.length == 1) {
                oneArgHelper(args[0]);
            }
            if (args.length == 2) {
                twoArgHelper(args[0], args[1]);
            } else {
                otherArgHelper(args);
            }
        } catch (GitletException | IOException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    /**Helper function for one argument input.
     * @param args - inputs */
    private static void otherArgHelper(String[] args) {
        if (args.length == 0) {
            throw new GitletException("Please enter a command.");
        } else {
            if (args[0].equals("checkout")
                    && args.length == 3
                    && args[1].equals("--")) {
                checkoutF(args[2]);
                System.exit(0);
            }
            if (args[0].equals("checkout")
                    && args.length == 4
                    && args[2].equals("++")) {
                throw new GitletException("Incorrect operands.");
            }
            if (args[0].equals("checkout")
                    && args.length == 4
                    && args[2].equals("--")) {
                checkoutID(args[1], args[3]);
                System.exit(0);
            } else {
                for (String c: COMMAND) {
                    if (c.equals(args[0])) {
                        throw new GitletException("Incorrect operands.");
                    } else {
                        throw new GitletException(
                                "No command with that name exists.");
                    }
                }
            }
        }
    }

    /**Helper function for one argument input.
     * @param arg0 - first input */
    private static void oneArgHelper(String arg0) throws IOException {
        if (arg0.equals("init")) {
            init();
            System.exit(0);
        }
        if (arg0.equals("status")) {
            status();
            System.exit(0);
        }
        if (arg0.equals("log")) {
            log();
            System.exit(0);
        }
        if (arg0.equals("global-log")) {
            globalLog();
            System.exit(0);
        }
        for (String c: COMMAND) {
            if (c.equals(arg0)) {
                throw new GitletException("Incorrect operands.");
            }
        }
        throw new GitletException("No command with that name exists.");
    }

    /**Helper function for two argument input.
     * @param arg0 - first input
     * @param arg1 - second input */
    private static void twoArgHelper(String arg0, String arg1) {
        if (arg0.equals("add")) {
            add(arg1);
            System.exit(0);
        }
        if (arg0.equals("commit")) {
            commit(arg1);
            System.exit(0);
        }
        if (arg0.equals("rm")) {
            rm(arg1);
            System.exit(0);
        }
        if (arg0.equals("find")) {
            find(arg1);
            System.exit(0);
        }
        if (arg0.equals("checkout")) {
            checkoutB(arg1);
            System.exit(0);
        }
        if (arg0.equals("branch")) {
            branch(arg1);
            System.exit(0);
        }
        if (arg0.equals("rm-branch")) {
            rmBranch(arg1);
            System.exit(0);
        }
        if (arg0.equals("reset")) {
            reset(arg1);
            System.exit(0);
        }
        if (arg0.equals("merge")) {
            merge(arg1);
            System.exit(0);
        }
        if (arg0.equals("init") || arg0.equals("status")
                || arg0.equals("log")
                || arg0.equals("global-log")) {
            throw new GitletException("Incorrect operands.");
        } else {
            throw new GitletException("No command with that name exists.");
        }
    }
}

