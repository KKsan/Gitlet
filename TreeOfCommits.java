import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.HashMap;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

/**
 * @author Xizhao Deng
 *
 *         References: fileCompare is adapted from
 *         http://stackoverflow.com/questions
 *         /7119141/java-file-to-binary-conversion fileCopy is adapted from
 *         http:
 *         //stackoverflow.com/questions/106770/standard-concise-way-to-copy
 *         -a-file-in-java/115086#115086
 * 
 */

public class TreeOfCommits implements Serializable {
    private HashSet<String> staged;
    private HashSet<String> toRemove;
    private HashMap<String, Integer> branches;
    private HashMap<Integer, Integer> currToPrev;
    private String activeBranch;
    private int latestID;
    private boolean initialized;
    private HashMap<Integer, Commit> idToCommit;

    public TreeOfCommits() {
        latestID = 0;
        initialized = false;
        staged = new HashSet<String>();
        toRemove = new HashSet<String>();
        currToPrev = new HashMap<Integer, Integer>();
        branches = new HashMap<String, Integer>(); // branchName to head
                                                   // commitID
        idToCommit = new HashMap<Integer, Commit>();
        branches.put("master", -1);
        activeBranch = "master";
    }

    // ===============utility==============
    public int getID() {
        return latestID;
    }

    public int getHead() {
        return branches.get(activeBranch);
    }

    public HashMap<Integer, Commit> getIdCommit() {
        return idToCommit;
    }

    public HashMap<String, Integer> getBranches() {
        return branches;
    }

    public String getActiveBch() {
        return activeBranch;
    }

    public HashSet<String> getStaged() {
        return staged;
    }

    public void clearStaged() {
        staged = new HashSet<String>();
    }

    public HashSet<String> toRemove() {
        return toRemove;
    }

    public void clearToRemove() {
        toRemove = new HashSet<String>();
    }

    public HashMap<Integer, Integer> getCurrToPrev() {
        return currToPrev;
    }

    public static TreeOfCommits loadList() {
        TreeOfCommits newList = null;

        File listFile = new File(".gitlet/TreeOfCommits.ser");
        if (listFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(listFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                newList = (TreeOfCommits) objectIn.readObject();
                fileIn.close();
                objectIn.close();
            } catch (IOException e) {
                String errorMsg = "IOException while loading newList";
                System.out.println(errorMsg);
            } catch (ClassNotFoundException e) {
                String errorMsg = "ClassNotFoundException while loading newList.";
                System.out.println(errorMsg);
            }
        }
        return newList;
    }

    public static void saveTree(TreeOfCommits theList) {
        if (theList == null) {
            return;
        }
        try {
            File listFile = new File(".gitlet/TreeOfCommits.ser");

            // pass in false to overwrite the file
            FileOutputStream fileOut = new FileOutputStream(listFile, false);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(theList);

            fileOut.close();
            objectOut.close();
        } catch (IOException e) {
            String errorMsg = "IOException while saving theList";
            System.out.println(errorMsg);
        }
    }

    public static boolean warning() {
        String msg1 = "Warning: The command you entered ";
        String msg2 = "may alter the files in your working directory. ";
        String msg3 = "Uncommitted changes may be lost. ";
        String msg4 = "Are you sure you want to continue? (yes/no)";
        System.out.println(msg1 + msg2 + msg3 + msg4);
        System.out.print("> ");

        Scanner input = new Scanner(System.in);
        String yesNo = input.nextLine();
        input.close();

        if (yesNo.equals("yes")) {
            return true;
        }
        return false;
    }

    // ==============init================
    public void initialized() {
        initialized = true;
    }

    // ===============add================
    public void addFile(String fileName) {
        try {
            // check if the file exists
            File theFile = new File(fileName);

            if (!theFile.exists()) {
                System.out.println("File does not exist");
                return;
            }

            // check if the file is modified since last commit
            Commit headCommit = idToCommit.get(getHead());
            Integer lastCtID = headCommit.getMap().get(fileName);

            if (lastCtID != null) {
                String subFolder = Integer.toString(lastCtID);
                File currentFile = new File(fileName);
                File lastCtFile = new File(".gitlet/" + subFolder + "/"
                        + fileName);

                if (TreeOfCommits.fileCompare(currentFile, lastCtFile)) {
                    System.out
                            .println("File has not been modified since the last commit");
                    return;
                }
            }

            toRemove.remove(fileName);
            staged.add(fileName);
        } catch (NullPointerException e) {
            System.out.println("addFile has null pointer exception");
        }
    }

    public static boolean fileCompare(File file1, File file2) {
        int length1 = (int) file1.length();
        int length2 = (int) file2.length();

        if (length1 != length2) {
            return false;
        }

        try {
            byte[] fileData1 = new byte[length1];
            FileInputStream fileIn = new FileInputStream(file1);
            fileIn.read(fileData1);
            fileIn.close();

            byte[] fileData2 = new byte[length2];
            FileInputStream fileIn2 = new FileInputStream(file2);
            fileIn2.read(fileData2);
            fileIn2.close();

            int result = 0;

            for (int i = 0; i < length1; i++) {
                int tempResult = fileData1[i] ^ fileData2[i];
                if (tempResult != 0) {
                    result++;
                }
            }

            if (result != 0) {
                return false;
            }
        } catch (IOException e) {
            String errorMsg = "IOException while loading files";
            System.out.println(errorMsg);
        }
        return true;
    }

    // =============commit===============
    public void addCommit(Commit newCommit) {

        // newCommit is initial commit
        if (newCommit.getID() == 0) {
            idToCommit.put(newCommit.getID(), newCommit);
            currToPrev.put(newCommit.getID(), newCommit.getPrev());
            latestID++;
            updateBranches(newCommit);
            return;
        }

        // newCommit is not initial commit
        if (newCommit.getID() != 0) {
            // if staged is empty but toRemove is not, do we commit?
            if (staged.isEmpty() && toRemove.isEmpty()) {
                System.out.println("No changes added to the commit.");
                return;
            }
        }

        Commit prevCommit = idToCommit.get(newCommit.getPrev());
        currToPrev.put(newCommit.getID(), newCommit.getPrev());
        latestID++;

        // add staged files to new commit
        if (!staged.isEmpty()) {
            for (String s : staged) {
                newCommit.getMap().put(s, newCommit.getID());
            }
        }
        clearStaged();

        // do copy to path operation (copy only added files, not including old
        // files)
        for (String s : newCommit.getMap().keySet()) {
            File file = new File(s);
            File destination = new File(".gitlet/"
                    + Integer.toString(newCommit.getID()) + "/" + s);
            TreeOfCommits.fileCopy(file, destination);
        }

        // inherit old files to new commit
        if (!prevCommit.getMap().isEmpty()) {
            for (String s : prevCommit.getMap().keySet()) {
                if (!toRemove.contains(s)) {
                    if (!newCommit.getMap().containsKey(s)) {
                        newCommit.getMap().put(s, prevCommit.getMap().get(s));
                    }
                }
            }
        }
        clearToRemove();
        updateBranches(newCommit);
        idToCommit.put(newCommit.getID(), newCommit);
    }

    public static void fileCopy(File file, File destination) {
        try {
            if (!destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }
            Files.copy(file.toPath(), destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String errorMsg = "IOException while copying files";
            System.out.println(errorMsg);
        }
    }

    // ======================remove======================

    public void removeFile(String fileName) {
        Commit headCommit = idToCommit.get(getHead());
        if (!staged.contains(fileName)) {
            if (!headCommit.getMap().containsKey(fileName)) {
                System.out.println("No reason to remove the file");
                return;
            }
        }
        staged.remove(fileName);
        toRemove.add(fileName);
    }

    // ======================log======================

    public void log() {
        int cursor = getHead();
        while (cursor != -1) {
            Commit cursorCommit = idToCommit.get(cursor);
            System.out.println("====");
            System.out.println("Commit " + Integer.toString(cursor) + ".");
            System.out.println(cursorCommit.getTime());
            System.out.println(cursorCommit.getMsg());
            System.out.println("\n");
            cursor = currToPrev.get(cursor);
        }
    }

    // ======================global-log======================

    public void globalLog() {
        int cursor;
        for (String s : branches.keySet()) {
            if (!s.equals("master")) {
                cursor = branches.get(s);

                while (cursor != -1) {
                    Commit cursorCommit = idToCommit.get(cursor);
                    if (cursorCommit.getBchIDs().contains("master")) {
                        break;
                    }
                    System.out.println("====");
                    System.out.println("Commit " + Integer.toString(cursor)
                            + ".");
                    System.out.println(cursorCommit.getTime());
                    System.out.println(cursorCommit.getMsg());
                    System.out.println("\n");
                    cursor = currToPrev.get(cursor);
                }
            } else {
                cursor = branches.get(s);

                while (cursor != -1) {
                    Commit cursorCommit = idToCommit.get(cursor);
                    System.out.println("====");
                    System.out.println("Commit " + Integer.toString(cursor)
                            + ".");
                    System.out.println(cursorCommit.getTime());
                    System.out.println(cursorCommit.getMsg());
                    System.out.println("\n");
                    cursor = currToPrev.get(cursor);
                }
            }
        }
    }

    // ======================find======================

    public void find(String msg) {
        int cursor;
        boolean foundOrNot = false;
        for (String s : branches.keySet()) {
            if (!s.equals("master")) {
                cursor = branches.get(s);

                while (cursor != -1) {
                    Commit cursorCommit = idToCommit.get(cursor);
                    if (cursorCommit.getBchIDs().contains("master")) {
                        break;
                    }
                    if (cursorCommit.getMsg().equals(msg)) {
                        System.out.println(cursor);
                        foundOrNot = true;
                    }
                    cursor = currToPrev.get(cursor);
                }
            } else {
                cursor = branches.get(s);

                while (cursor != -1) {
                    Commit cursorCommit = idToCommit.get(cursor);
                    if (cursorCommit.getMsg().equals(msg)) {
                        System.out.println(cursor);
                        foundOrNot = true;
                    }
                    cursor = currToPrev.get(cursor);
                }
            }
        }
        if (!foundOrNot) {
            System.out.println("Found no commit with that message.");
        }
    }

    // ======================status======================

    public void status() {
        System.out.println("=== Branches ===");
        System.out.println("*" + activeBranch);

        for (String s : branches.keySet()) {
            if (!s.equals(activeBranch)) {
                System.out.println(s);
            }
        }

        System.out.println("\n");
        System.out.println("=== Staged Files ===");

        for (String s : staged) {
            System.out.println(s);
        }

        System.out.println("\n");
        System.out.println("=== Files Marked for Removal ===");

        for (String s : toRemove) {
            System.out.println(s);
        }
    }

    // ======================checkout======================

    public void checkout(String[] args) {
        if (args.length == 2) {
            // input is a branchName
            String branchName = args[1];
            if (!branches.containsKey(branchName)) {
                String msg1 = "File does not exist in the most recent commit";
                String msg2 = ", or no such branch exists.";
                System.out.println(msg1 + msg2);
            } else if (branchName.equals(activeBranch)) {
                System.out.println("No need to checkout the current branch.");
                return;
            } else {
                // restore current working directory to the latest commit
                activeBranch = branchName;
                Commit headCommit = idToCommit.get(branches.get(activeBranch));

                for (String s : headCommit.getMap().keySet()) {
                    File workingDir = new File(s);
                    File lastVersion = new File(".gitlet/"
                            + Integer.toString(headCommit.getMap().get(s))
                            + "/" + s);
                    TreeOfCommits.fileCopy(lastVersion, workingDir);
                }
                return;
            }

            // input is a fileName
            if (getHead() <= 0) {
                String msg1 = "File does not exist in the most recent ";
                String msg2 = "commit, or no such branch exists.";
                System.out.println(msg1 + msg2);
                return;
            }

            String fileName = args[1];
            Commit headCommit = idToCommit.get(getHead());
            if (!headCommit.getMap().containsKey(fileName)) {
                String msg1 = "File does not exist in the most recent ";
                String msg2 = "commit, or no such branch exists.";
                System.out.println(msg1 + msg2);
                return;
            }

            File workingDir = new File(fileName);
            File lastVersion = new File(".gitlet/"
                    + Integer.toString(headCommit.getMap().get(fileName)) + "/"
                    + fileName);
            TreeOfCommits.fileCopy(lastVersion, workingDir);

        } else {
            // commitID fileName
            String fileName = args[2];
            int commitID = Integer.parseInt(args[1]);

            if (commitID <= 0) {
                System.out.println("File does not exist in that commit.");
                return;
            }

            if (!currToPrev.containsKey(commitID)) {
                System.out.println("No commit with that id exists.");
                return;
            }

            Commit thatCommit = idToCommit.get(commitID);
            if (!thatCommit.getMap().containsKey(fileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }

            File workingDir = new File(fileName);
            File lastVersion = new File(".gitlet/"
                    + Integer.toString(thatCommit.getMap().get(fileName)) + "/"
                    + fileName);
            TreeOfCommits.fileCopy(lastVersion, workingDir);
        }
    }

    // ========================branch========================

    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }

        branches.put(branchName, branches.get(activeBranch));

        Commit headCommit = idToCommit.get(getHead());
        headCommit.getBchIDs().add(branchName); // mark the commit for branch
                                                // split
        idToCommit.put(headCommit.getID(), headCommit);
    }

    private void updateBranches(Commit newCommit) {
        for (String s : newCommit.getBchIDs()) {
            branches.put(s, newCommit.getID());
        }
    }

    // ========================remove branch========================
    public void rmBranch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(activeBranch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchName);
    }

    // ========================reset========================

    public void reset(int commitID) {
        if (!currToPrev.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit thatCommit = idToCommit.get(commitID);

        for (String s : thatCommit.getMap().keySet()) {
            File workingDir = new File(s);
            File lastVersion = new File(".gitlet/"
                    + Integer.toString(thatCommit.getMap().get(s)) + "/" + s);
            TreeOfCommits.fileCopy(lastVersion, workingDir);
        }

        // move current branch's head to that commit node
        branches.put(activeBranch, commitID);
        thatCommit.getBchIDs().add(activeBranch); // is this correct?
        idToCommit.put(thatCommit.getID(), thatCommit);
    }

    // ========================merge========================

    public void merge(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(activeBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        Commit curreCommit = idToCommit.get(getHead());
        Commit givenCommit = idToCommit.get(branches.get(branchName));
        int splitPt = findSplitPt(activeBranch, branchName);
        Commit splitCommit = idToCommit.get(splitPt);

        for (String givS : givenCommit.getMap().keySet()) {
            if (!splitCommit.getMap().containsKey(givS)) {
                File curreDir = new File(givS);
                File givenDir = new File(".gitlet/"
                        + Integer.toString(givenCommit.getMap().get(givS))
                        + "/" + givS);
                TreeOfCommits.fileCopy(givenDir, curreDir);
            } else {
                if (givenCommit.getMap().get(givS) != splitCommit.getMap().get(
                        givS)) {
                    if (!curreCommit.getMap().containsKey(givS)) {
                        File curreDir = new File(givS);
                        File givenDir = new File(".gitlet/"
                                + Integer.toString(givenCommit.getMap().get(
                                        givS)) + "/" + givS);
                        TreeOfCommits.fileCopy(givenDir, curreDir);
                    } else {
                        if (curreCommit.getMap().get(givS) == splitCommit
                                .getMap().get(givS)) {
                            File curreDir = new File(givS);
                            File givenDir = new File(".gitlet/"
                                    + Integer.toString(givenCommit.getMap()
                                            .get(givS)) + "/" + givS);
                            TreeOfCommits.fileCopy(givenDir, curreDir);
                        } else {
                            File curreDir = new File(givS + ".conflicted");
                            File givenDir = new File(".gitlet/"
                                    + Integer.toString(givenCommit.getMap()
                                            .get(givS)) + "/" + givS);
                            TreeOfCommits.fileCopy(givenDir, curreDir);
                        }
                    }
                }
            }
        }
    }

    private int findSplitPt(String branchName1, String branchName2) {
        if (!branches.containsKey(branchName1)
                || !branches.containsKey(branchName2)) {
            System.out.println("A branch with that name does not exist.");
            return -1;
        }

        int cursor1 = branches.get(branchName1);
        int cursor2 = branches.get(branchName2);

        HashSet<Integer> idCache = new HashSet<Integer>();

        while (cursor1 != -1) {
            idCache.add(cursor1);
            cursor1 = currToPrev.get(cursor1);
        }

        while (cursor2 != -1) {
            if (idCache.contains(cursor2)) {
                break;
            }
            cursor2 = currToPrev.get(cursor2);
        }

        return cursor2;
    }

    // ========================rebase and i-rebase========================

    public void rebase(String branchName, boolean methodFlag) {

        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(activeBranch)) {
            System.out.println("Cannot rebase a branch onto itself.");
            return;
        }
        if (foundInHistory(branchName, activeBranch)) {
            System.out.println("Already up-to-date");
            return;
        }
        if (foundInHistory(activeBranch, branchName)) {
            branches.put(activeBranch, branches.get(branchName));
            return;
        }

        int givenBch = branches.get(branchName);
        int currentBch = getHead();
        int splitPt = findSplitPt(activeBranch, branchName);

        Commit commitCopySource = null;
        Commit commitCopyNew = null;

        branches.put(activeBranch, latestID); // update the current branch head

        // copy over the commits
        for (int cursor = currentBch; cursor != splitPt; cursor = currToPrev
                .get(cursor)) {
            commitCopySource = idToCommit.get(cursor);
            if (!methodFlag) {
                replaying(null, cursor, splitPt, commitCopySource,
                        commitCopyNew, givenBch);
            } else {
                currentReplaying(commitCopySource);
                String flag = response();
                if (flag.equals("c")) {
                    replaying(null, cursor, splitPt, commitCopySource,
                            commitCopyNew, givenBch);
                } else if (flag.equals("m")) {
                    String newMsg = getNewMsg();
                    replaying(newMsg, cursor, splitPt, commitCopySource,
                            commitCopyNew, givenBch);
                }
            }
        }
        // check for modified files
        Commit givenCommit = idToCommit.get(givenBch);
        for (int cursor = branches.get(activeBranch); cursor != givenBch; cursor = currToPrev
                .get(cursor)) {
            Commit cursorCommit = idToCommit.get(cursor);
            for (String e : givenCommit.getMap().keySet()) {
                if (givenCommit.getMap().get(e) > splitPt) {
                    // given branch contains modification to a file but current
                    // branch doesn't
                    if (cursorCommit.getMap().containsKey(e)) {
                        if (cursorCommit.getMap().get(e) <= splitPt) {
                            cursorCommit.getMap().put(e,
                                    givenCommit.getMap().get(e));
                        }
                    } else {
                        cursorCommit.getMap().put(e,
                                givenCommit.getMap().get(e));
                    }
                    // both branch contains modification to the same file, by
                    // default it will use current branch's files
                }
            }
        }
        // update the files in the working directory
        reset(getHead());
    }

    private void replaying(String newMsg, int cursor, int splitPt,
            Commit commitCopySource, Commit commitCopyNew, int givenBch) {
        if (currToPrev.get(cursor) == splitPt) {
            commitCopyNew = new Commit(commitCopySource, latestID, givenBch,
                    newMsg);
            currToPrev.put(latestID, givenBch);
            idToCommit.put(latestID, commitCopyNew);
        } else {
            commitCopyNew = new Commit(commitCopySource, latestID,
                    latestID + 1, newMsg);
            currToPrev.put(latestID, latestID + 1);
            idToCommit.put(latestID, commitCopyNew);
        }
        latestID++;
    }


    private void currentReplaying(Commit theCommit) {
        System.out.println("Currently replaying:");
        System.out.println("====");
        System.out.println("Commit " + Integer.toString(theCommit.getID())
                + ".");
        System.out.println(theCommit.getTime());
        System.out.println(theCommit.getMsg());
        System.out.println("\n");
    }

    //check if the given branch is in the history of the current branch
    private boolean foundInHistory(String givenBch, String currentBch) {
        int cursor = branches.get(currentBch);
        int stop = findSplitPt(givenBch, currentBch);
        int givenID = branches.get(givenBch);

        while (cursor != -1) {
            if (cursor == givenID) {
                return true;
            }
            cursor = currToPrev.get(cursor);
        }
        return false;
    }
    private String getNewMsg() {
        System.out.println("Please enter a new message for this commit.");
        System.out.print("> ");

        Scanner input = new Scanner(System.in);
        String newMsg = input.nextLine();
        input.close();
        return newMsg;
    }

    private String response() {
        String msg1 = "Would you like to (c)ontinue, (s)kip ";
        String msg2 = "this commit, or change this commit's (m)essage?";
        System.out.println(msg1 + msg2);
        Scanner input = new Scanner(System.in);
        HashSet<String> options = new HashSet<String>();
        options.add("c");
        options.add("s");
        options.add("m");

        String answer2 = null;
        while (!options.contains(answer2)) {
            System.out.print(">");
            answer2 = input.nextLine();
        }
        input.close();
        return answer2;
    }    
}
