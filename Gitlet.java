import java.io.File;

/**
 * @author Xizhao Deng
 */

public class Gitlet {

    public static void init(TreeOfCommits theCommits, Commit newCommit, 
        Commit initCommit, File file) {
        theCommits = new TreeOfCommits();
        file = new File(".gitlet");
        if (!file.exists()) {
            file.mkdir();
            initCommit = new Commit();
            theCommits.addCommit(initCommit);
            theCommits.initialized();
            TreeOfCommits.saveTree(theCommits); // save as ser file
        } else {
            String msg1 = "A gitlet version control system ";
            String msg2 = "already exists in the current directory.";
            System.out.println(msg1 + msg2);
        }
    }

    public static void add(TreeOfCommits theCommits, String fileName, String[] args) {
        theCommits = TreeOfCommits.loadList();
        fileName = args[1];

        theCommits.addFile(fileName);
        TreeOfCommits.saveTree(theCommits); // save as ser file
    }

    public static void commit(TreeOfCommits theCommits, 
        String msg, Commit newCommit, String[] args) {
        theCommits = TreeOfCommits.loadList();

        msg = args[1];
        newCommit = new Commit(theCommits.getID(), msg,
                theCommits.getHead(), theCommits.getActiveBch());
        theCommits.addCommit(newCommit);
        TreeOfCommits.saveTree(theCommits);
    }

    public static void rm(TreeOfCommits theCommits, String fileName, String[] args) {
        theCommits = TreeOfCommits.loadList();
        fileName = args[1];
        theCommits.removeFile(fileName);
        TreeOfCommits.saveTree(theCommits);    
    }

    public static void log(TreeOfCommits theCommits) {
        theCommits = TreeOfCommits.loadList();
        theCommits.log();
        TreeOfCommits.saveTree(theCommits);    
    }

    public static void globalLog(TreeOfCommits theCommits) {
        theCommits = TreeOfCommits.loadList();
        theCommits.globalLog();
        TreeOfCommits.saveTree(theCommits);    
    }

    public static void find(TreeOfCommits theCommits, String msg, String[] args) {
        theCommits = TreeOfCommits.loadList();
        msg = args[1];
        theCommits.find(msg);
        TreeOfCommits.saveTree(theCommits);    
    }

    public static void status(TreeOfCommits theCommits) {
        theCommits = TreeOfCommits.loadList();
        theCommits.status();
        TreeOfCommits.saveTree(theCommits);    
    }

    public static void checkout(TreeOfCommits theCommits, String[] args) {
        if (TreeOfCommits.warning()) {
            theCommits = TreeOfCommits.loadList();
            theCommits.checkout(args);
            TreeOfCommits.saveTree(theCommits);
        }    
    }

    public static void branch(TreeOfCommits theCommits, String branchName, String[] args) {
        theCommits = TreeOfCommits.loadList();
        branchName = args[1];
        theCommits.branch(branchName);
        TreeOfCommits.saveTree(theCommits);
    }

    public static void rmBranch(TreeOfCommits theCommits, String branchName, String[] args) {
        theCommits = TreeOfCommits.loadList();
        branchName = args[1];
        theCommits.rmBranch(branchName);
        TreeOfCommits.saveTree(theCommits);
    }

    public static void reset(TreeOfCommits theCommits, String[] args) {
        if (TreeOfCommits.warning()) {
            theCommits = TreeOfCommits.loadList();
            theCommits.reset(Integer.parseInt(args[1]));
            TreeOfCommits.saveTree(theCommits);
        }    
    }

    public static void merge(TreeOfCommits theCommits, String branchName, String[] args) {
        if (TreeOfCommits.warning()) {
            theCommits = TreeOfCommits.loadList();
            branchName = args[1];
            theCommits.merge(branchName);
            TreeOfCommits.saveTree(theCommits);
        }
    }

    public static void rebase(TreeOfCommits theCommits, String branchName, String[] args) {
        if (TreeOfCommits.warning()) {
            theCommits = TreeOfCommits.loadList();
            branchName = args[1];
            theCommits.rebase(branchName, false);
            TreeOfCommits.saveTree(theCommits);
        }
    }

    public static void iRebase(TreeOfCommits theCommits, String branchName, String[] args) {
        if (TreeOfCommits.warning()) {
            theCommits = TreeOfCommits.loadList();
            branchName = args[1];
            theCommits.rebase(branchName, true);
            TreeOfCommits.saveTree(theCommits);
        }
    }

    public static void main(String[] args) {
        TreeOfCommits theCommits = null;
        Commit newCommit = null, initCommit = null;
        String fileName = null, command = null, msg = null, branchName = null;
        File file = null;
        String answer = null;

        try {
            command = args[0];
            switch (command) {
                case "init":
                    Gitlet.init(theCommits, newCommit, initCommit, file);
                    break;
                case "add":
                    if (args.length != 2) {
                        System.out.println("Please enter a file name to add.");
                        break;
                    }                    
                    Gitlet.add(theCommits, fileName, args);
                    break;
                case "commit":
                    if (args.length != 2) {
                        System.out.println("Please enter a commit message.");
                        break;
                    }                   
                    commit(theCommits, msg, newCommit, args);
                    break;
                case "rm":
                    if (args.length != 2) {
                        System.out.println("Please enter a file name to remove.");
                        break;
                    }
                    rm(theCommits, fileName, args);
                    break;
                case "log":
                    log(theCommits);
                    break;
                case "global-log":
                    globalLog(theCommits);
                    break;
                case "find":
                    if (args.length != 2) {
                        System.out
                                .println("Please enter a commit message to find.");
                        break;
                    }
                    find(theCommits, msg, args);
                    break;
                case "status":
                    status(theCommits);
                    break;
                case "checkout":
                    if ((args.length != 2) && (args.length != 3)) {
                        System.out.println("Please enter valid arguments");
                        break;
                    }
                    checkout(theCommits, args);
                    break;
                case "branch":
                    if (args.length != 2) {
                        System.out.println("Please enter a new branch name.");
                        break;
                    }                   
                    branch(theCommits, branchName, args);
                    break;
                case "rm-branch":
                    if (args.length != 2) {
                        System.out.println("Please enter a branch name to remove.");
                        break;
                    }                  
                    rmBranch(theCommits, branchName, args);
                    break;
                case "reset":
                    if (args.length != 2) {
                        System.out.println("Please enter a commit ID to reset.");
                        break;
                    }                    
                    reset(theCommits, args);
                    break;
                case "merge":
                    if (args.length != 2) {
                        System.out.println("Please enter a branch name to merge.");
                        break;
                    }
                    merge(theCommits, branchName, args);
                    break;
                case "rebase":
                    if (args.length != 2) {
                        System.out.println("Please enter a branch name to rebase.");
                        break;
                    }
                    rebase(theCommits, branchName, args);
                    break;
                case "i-rebase":
                    if (args.length != 2) {
                        System.out
                            .println("Please enter a branch name to inverse rebase.");
                        break;
                    }                
                    iRebase(theCommits, branchName, args);
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("ArrayIndexOutOfBoundsException "
                    + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("NullPointerException " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            System.err.println("IndexOutOfBoundsException " + e.getMessage());
        }
    }
}
