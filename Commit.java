import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author Xizhao Deng
 */

public class Commit implements Serializable {
    private String commitMsg;
    private String timeStr;
    private int previous;
    private int commitID;
    private HashMap<String, Integer> fileToID;
    private HashSet<String> branchIDs;

    public Commit() {
        Date commitTime = new Date();
        DateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        fileToID = new HashMap<String, Integer>();
        commitMsg = "initial commit";
        timeStr = formatter.format(commitTime);
        previous = -1;
        commitID = 0;
        branchIDs = new HashSet<String>();
        branchIDs.add("master");
    }

    public Commit(int theID, String msg, int prev, String bchID) {
        Date commitTime = new Date();
        DateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        fileToID = new HashMap<String, Integer>();
        commitMsg = msg;
        timeStr = formatter.format(commitTime);
        previous = prev;
        commitID = theID;
        branchIDs = new HashSet<String>();
        branchIDs.add(bchID);
    }

    public Commit(Commit theOtherCommit, int theID, int prev, String newMsg) {
        Date commitTime = new Date();
        DateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

        timeStr = formatter.format(commitTime);
        previous = prev;
        commitID = theID;

        fileToID = new HashMap<String, Integer>();
        branchIDs = new HashSet<String>();

        for (String e : theOtherCommit.getMap().keySet()) {
            fileToID.put(e, theOtherCommit.getMap().get(e));
        }
        for (String e : theOtherCommit.branchIDs) {
            branchIDs.add(e);
        }

        if (newMsg != null) {
            commitMsg = newMsg;
        } else {
            commitMsg = theOtherCommit.getMsg();
        }
    }

    public String getMsg() {
        return commitMsg;
    }

    public HashSet<String> getBchIDs() {
        return branchIDs;
    }

    public String getTime() {
        return timeStr;
    }

    public int getID() {
        return commitID;
    }

    public int getPrev() {
        return previous;
    }

    public HashMap<String, Integer> getMap() {
        return fileToID;
    }

    public static Commit loadCommit(int theID) {
        Commit newCommit = null;
        String fileName = Integer.toString(theID) + ".ser";
        String subFolder = Integer.toString(theID);

        File commitFile = new File(".gitlet/" + subFolder + "/" + fileName);
        if (commitFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(commitFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                newCommit = (Commit) objectIn.readObject();
                fileIn.close();
                objectIn.close();

            } catch (IOException e) {
                String errorMsg = "IOException while loading newCommit";
                System.out.println(errorMsg);
            } catch (ClassNotFoundException e) {
                String errorMsg = "ClassNotFoundException while loading newCommit.";
                System.out.println(errorMsg);
            }
        }
        return newCommit;
    }

    public static void saveCommit(Commit theCommit) {
        if (theCommit == null) {
            return;
        }
        try {
            String fileName = Integer.toString(theCommit.getID()) + ".ser";
            String subFolder = Integer.toString(theCommit.commitID);
            File commitFolder = new File(".gitlet/" + subFolder);

            if (!commitFolder.exists()) {
                commitFolder.mkdir();
            }

            File commitFile = new File(".gitlet/" + subFolder + "/" + fileName);

            FileOutputStream fileOut = new FileOutputStream(commitFile, false);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(theCommit);
            fileOut.close();
            objectOut.close();
        } catch (IOException e) {
            String errorMsg = "IOException while saving theCommit";
            System.out.println(errorMsg);
        }
    }
}
