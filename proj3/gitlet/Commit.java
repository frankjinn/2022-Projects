package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;


public class Commit implements Serializable {
    /**
     * Makes bloblist based on files in the current working directory.
     * @param prevCommitIDin
     * @param commitMessagein
     * @param currentBranchin
     * @param branchesin
     * @param rmvListin
     */
    public Commit(String prevCommitIDin, String commitMessagein,
                  String currentBranchin, TreeMap<String, String> branchesin,
                  ArrayList<String> rmvListin) {
        this.prevCommitID = prevCommitIDin;
        ZonedDateTime commitDate = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        timeOfCommit = formatter.format(commitDate);
        this.rmvList = rmvListin;
        this.commitMessage = commitMessagein;
        this.currentBranch = currentBranchin;
        this.branches = branchesin;
        createBlobList();
    }

    /**
     *
     * @param prevCommitIDin
     * @param commitMessagein
     * @param currentBranchin
     * @param branchesin
     * @param initial
     */
    public Commit(String prevCommitIDin, String commitMessagein,
                  String currentBranchin, TreeMap<String,
            String> branchesin, Boolean initial) {
        if (initial) {
            this.prevCommitID = prevCommitIDin;
            long x = 0;
            Date temp = new Date(x);
            LocalDateTime ldt = LocalDateTime.of(temp.getYear(), 1,
                    1, 0, 0, 0);
            OffsetDateTime odt = ldt.atOffset(ZoneOffset.UTC);
            ZoneId z = ZoneId.systemDefault();
            ZonedDateTime zdt = odt.atZoneSameInstant(z);
            DateTimeFormatter f = DateTimeFormatter.ofPattern(
                    "EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
            timeOfCommit = zdt.format(f);
            this.commitMessage = commitMessagein;
            this.currentBranch = currentBranchin;
            this.branches = branchesin;
        }
    }

    /**
     * Commit constructor for merged commits.
     * @param prevCommitIDin
     * @param mergeParentin
     * @param currentBranchin
     * @param mergedBranchin
     * @param branchesin
     * @param blobMapin
     */
    public Commit(String prevCommitIDin, String mergeParentin,
                  String currentBranchin, String mergedBranchin, TreeMap<String,
                  String> branchesin, TreeMap<String, String> blobMapin) {
        this.prevCommitID = prevCommitIDin;
        ZonedDateTime commitDate = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        timeOfCommit = formatter.format(commitDate);
        this.commitMessage = String.format("Merged %s into %s.", mergedBranchin,
                currentBranchin);
        this.currentBranch = currentBranchin;
        this.branches = branchesin;
        this.blobMap = blobMapin;
        this.mergeParent = mergeParentin;
        this.rmvList = null;
    }

    /** Looks through the current staged files (named with the SHA1 IDs), and
     *  sees if some already exist in the blob directory. If some do, pointer to
     *  them will be created. For new blobs, they will be created and added to
     *  the blob directory. Then it will look at files in the current directory,
     *  and if the SHA1 ID matches is in previous commit's blobList, then it
     *  will save those as well. After, it removes all blobs from bloblist that
     *  are in the rmvStaging directory.
     */
    void createBlobList() {
        File addStaging = new File(Main.getRepositoryDir(),
                "/.gitlet/addStaging");
        if (!Utils.plainFilenamesIn(addStaging).isEmpty()) {
            for (String name : Utils.plainFilenamesIn(addStaging)) {
                File searchupLoc = new File(String.format(
                        "%s/.gitlet/blobDump/%s",
                        Main.getRepositoryDir(), name));
                if (searchupLoc.exists()) {
                    String blobFilename = Utils.readObject(searchupLoc,
                            Blob.class).getName();

                    blobMap.put(blobFilename, name);
                } else {
                    Blob newBlob = Utils.readObject(
                            Utils.join(addStaging, name), Blob.class);
                    Utils.writeObject(searchupLoc, newBlob);
                    blobMap.put(newBlob.getName(), name);
                }
            }
        }
        Commit prevCommit = Utils.readObject(Utils.join(Main.getRepositoryDir(),
                ".gitlet/commitDump", prevCommitID), Commit.class);
        for (String name : Utils.plainFilenamesIn(Main.getRepositoryDir())) {
            File temp = Utils.join(Main.getRepositoryDir(), name);
            Blob thisBlob = new Blob(temp, name);
            if (prevCommit.blobMap.containsValue(thisBlob.getHash())
                && prevCommit.blobMap.containsKey(thisBlob.getName())) {
                blobMap.put(name, thisBlob.getHash());
            }
        }
        for (String i : rmvList) {
            blobMap.remove(i);
        }
    }

    /**
     * Returns the time of commit in a string.
     * @return Commit time string
     */
    public String getTime() {
        return this.timeOfCommit;
    }

    public TreeMap<String, String> getBlobMap() {
        return blobMap;
    }

    public String getPrevCommitID() {
        return prevCommitID;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public ArrayList<String> getRmvList() {
        return rmvList;
    }

    public String getCurrentBranch() {
        return currentBranch;
    }

    public TreeMap<String, String> getBranches() {
        return branches;
    }

    public String getMergeParent() {
        return mergeParent;
    }

    /** Dictionary of filename:hashcodes of all blobs that this commit
     * references.**/
    private TreeMap<String, String> blobMap = new TreeMap<>();

    /** Hashcode of previous commit's ID. **/
    private String prevCommitID;

    /** Time of commit, formatted for log.**/
    private String timeOfCommit;

    /** Commit message. **/
    private String commitMessage;

    /** rmvList that was passed in. **/
    private ArrayList<String> rmvList = new ArrayList<>();

    /** The branch this commit is on. **/
    private String currentBranch;

    /** Snapshot of the heads of branches during BEFORE this commit. **/
    private TreeMap<String, String> branches = new TreeMap<>();

    /** If commit comes from the merge two other commits, record the other
     * commit hash here.
     */
    private String mergeParent = null;
}
