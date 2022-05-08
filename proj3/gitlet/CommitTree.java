package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

public class CommitTree implements Serializable {
    /**
     * Initializes the repository, creates file structure and makes the first
     * commit.
     */
    public CommitTree() {
        File gitletFiles = new File(Main.getRepositoryDir().toString(),
                "/.gitlet");
        _blobDump = new File(gitletFiles, "/blobDump");
        _blobDump.mkdirs();
        _commitDump = new File(gitletFiles, "/commitDump");
        _commitDump.mkdir();
        _addStaging = new File(gitletFiles, "/addStaging");
        _addStaging.mkdir();
        _branches.put("master", null);
        _currentBranch = "master";
        commit("initial commit", _rmvList, null,
                null, true, false);
    }

    /**
     * Blobify a file in the working directory and adds it to the add-staging
     * folder. If a file has not been modified from the previous commit, then it
     * will do nothing.
     * @param filename Name of the file being added.
     */
    public void add(String filename) {
        if (!Utils.join(Main.getRepositoryDir(), filename).exists()) {
            System.out.println("File does not exist.");
        } else {
            File temp = Utils.join(Main.getRepositoryDir(), filename);
            Blob thisBlob = new Blob(temp, filename);
            Commit thisCommit = Utils.readObject(new File(_commitDump,
                    _head), Commit.class);
            if (_rmvList.contains(filename)) {
                _rmvList.remove(filename);
            } else if (!thisCommit.getBlobMap().containsKey(filename)
                    || !thisCommit.getBlobMap().containsValue(
                            thisBlob.getHash())) {
                Utils.writeObject(new File(_addStaging, thisBlob.getHash()),
                        thisBlob);
            }
        }
    }

    /**
     * Blobify a file. Checks if it is in the addStaging directory, if it is,
     * then remove it. If its not, and it's tracked in the previous commit's
     * blobList, then add to rmvStage directory (filename is hashcode).
     * @param filename file to be removed.
     */
    public void rm(String filename) {
        File temp = Utils.join(Main.getRepositoryDir(), filename);
        Commit thisCommit = Utils.readObject(new File(_commitDump,
                _head), Commit.class);
        if (temp.exists()) {
            Blob thisBlob = new Blob(temp, filename);
            if (Utils.join(_addStaging, thisBlob.getHash()).exists()) {
                Utils.join(_addStaging, thisBlob.getHash()).delete();
            } else if (thisCommit.getBlobMap().containsValue(
                    thisBlob.getHash())) {
                _rmvList.add(filename);
                temp.delete();
            } else {
                System.out.println("No reason to remove the file.");
            }
        } else if (thisCommit.getBlobMap().containsKey(filename)) {
            _rmvList.add(filename);
        }
    }

    /**
     * Creates a new commit and moves all necessary pointers to point to it.
     * Clears all the files from addStaging directory, and clears rmvList.
     * @param commitMessage message for the commit.
     * @param rmvList list of files that are not included in next commit,
     *                hence removed
     * @param initialCommit true if this commit is the intializing commit.
     * @param givenBranch branch that is being merged into this one.
     * @param mergeBlob the result of the merge
     * @param merge if this commit is result of a merge.
     */
    public void commit(String commitMessage, ArrayList<String> rmvList,
                       String givenBranch, TreeMap<String, String> mergeBlob,
                       Boolean initialCommit, Boolean merge) {
        if (initialCommit) {
            Commit currentCommit = new Commit(_head, commitMessage,
                    _currentBranch, _branches, initialCommit);
            save(currentCommit);
            _branches.put("master", _head);
        } else if (merge) {
            Commit currentCommit = Utils.readObject(Utils.join(_commitDump,
                    _head), Commit.class);
            Commit mergedCommit = new Commit(_head,
                    _branches.get(givenBranch), _currentBranch, givenBranch,
                    _branches, mergeBlob);
            if (Utils.sha1(Utils.serialize(currentCommit)).equals(
                    Utils.sha1(Utils.serialize(mergedCommit)))) {
                System.out.println("No changes added to the commit.");
            }
            save(mergedCommit);
            _branches.put(_currentBranch, _head);
        } else {
            if (commitMessage.equals("")) {
                System.out.println("Please enter a commit message.");
            } else if (Utils.plainFilenamesIn(_addStaging).isEmpty()
                    && rmvList.isEmpty()) {
                System.out.println("No changes added to the commit.");
            } else {
                Commit currentCommit = new Commit(_head, commitMessage,
                        _currentBranch, _branches, rmvList);
                save(currentCommit);
                clearDir(_addStaging);
                _rmvList.clear();
                _branches.put(_currentBranch, _head);
            }
        }
    }

    /**
     * Clears a specified directory.
     * @param dir The directory to be cleared.
     */
    private void clearDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }
    /**
     * Takes a commit and commit message and write it into the commitDump folder
     * with its name being the SHA1 code for it. Updates _head to point to this
     * commit, and adds the code and message to the commitCodes dictionary.
     * @param currentCommit the commit that is being commited.
     */
    public void save(Commit currentCommit) {
        String commitCode = Utils.sha1(Utils.serialize(currentCommit));
        Utils.writeObject(Utils.join(_commitDump, commitCode), currentCommit);
        _head = commitCode;
    }

    /**
     * Opens the latest commit on the current branch and opens the blob that
     * filename corresponds to. Then deletes the file in the working directory,
     * and writes in the one that was found.
     * @param filename the file to be replaced.
     */
    public void checkoutFile(String filename) {
        Commit currentCommit = Utils.readObject(Utils.join(_commitDump, _head),
                Commit.class);
        if (!currentCommit.getBlobMap().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
        } else {
            File storedFile = Utils.join(_blobDump,
                    currentCommit.getBlobMap().get(
                    filename));
            Blob storedBlob = Utils.readObject(storedFile, Blob.class);
            File targetFile = new File(Main.getRepositoryDir(), filename);
            targetFile.delete();
            Utils.writeContents(targetFile, storedBlob.getBlob());
        }
    }

    /**
     * Same thing as previous checkoutFile() function, but this time a commitID
     * is specified.
     * @param commitID specific commitID to look for file
     * @param filename file to be replaced
     */
    public void checkoutFile(String commitID, String filename) {
        Commit currentCommit = null;
        if (Utils.join(_commitDump, commitID).exists()) {
            currentCommit = Utils.readObject(
                    Utils.join(_commitDump, commitID),
                    Commit.class);
        } else {
            currentCommit = abbrevHash(commitID);
        }
        if (currentCommit == null) {
            System.out.println("No commit with that id exists.");
        } else if (!currentCommit.getBlobMap().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
        } else {
            File storedFile = Utils.join(_blobDump,
                    currentCommit.getBlobMap().get(filename));
            Blob storedBlob = Utils.readObject(storedFile, Blob.class);
            File targetFile = new File(Main.getRepositoryDir(), filename);
            targetFile.delete();
            Utils.writeContents(targetFile, storedBlob.getBlob());
        }
    }

    /**
     * Searches for a commit hash that corresponds to the abbreviated hash
     * given.
     * @param commitID Abbreviated commitID
     * @return commit object if found, null if not.
     */
    private Commit abbrevHash(String commitID) {
        int matchCounter = 0;
        String matchID = "";
        Commit currentCommit;
        for (String i : Utils.plainFilenamesIn(_commitDump)) {
            if (i.substring(0, 8).equals(commitID)) {
                matchCounter += 1;
                matchID = i;
            }
        }
        if (matchCounter == 1) {
            currentCommit = Utils.readObject(
                    Utils.join(_commitDump, matchID),
                    Commit.class);
            return currentCommit;
        }
        return null;
    }
    /**
     * Loads in the commit pointed to by a specific branch. This is now the
     * current branch, and is the head. Any files that are tracked in the
     * current branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current
     * branch.
     * @param branchName
     */
    public void checkoutBranch(String branchName) {
        if (!_branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
        } else if (branchName.equals(_currentBranch)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            Commit checkoutCom = Utils.readObject(Utils.join(_commitDump,
                    _branches.get(branchName)), Commit.class);
            if (!checkUntracked(checkoutCom)) {
                loadCommit(Utils.sha1(Utils.serialize(checkoutCom)),
                        false);
                clearDir(_addStaging);
                _rmvList.clear();
                _currentBranch = branchName;
                _head = _branches.get(_currentBranch);
            }
        }
    }

    /**
     * Puts a new branch in _branches dictionary. Points it to head.
     * @param branchName Name of the new branch
     */
    public void branch(String branchName) {
        if (_branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
        } else {
            _branches.put(branchName, _head);
        }
    }

    /**
     * Given a branch name, removes the branch from _branches.
     * @param branchName
     */
    public void rmbranch(String branchName) {
        if (branchName.equals(_currentBranch)) {
            System.out.println("Cannot remove the current branch.");
        } else if (!_branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            _branches.remove(branchName);
        }
    }

    /**
     * Loads in a commit using hash, then removes all tracked (prev commit's and
     * staging area) files. Then loads in all files from specified commit, and
     * sets the branch pointers and head to who it was when during this commit.
     * @param commitID the commit id to reset to.
     */
    public void reset(String commitID) {
        if (!Utils.join(_commitDump, commitID).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit newCommit = Utils.readObject(Utils.join(_commitDump, commitID),
                Commit.class);
        if (!checkUntracked(newCommit)) {
            newCommit = Utils.readObject(Utils.join(_commitDump, commitID),
                    Commit.class);
            loadCommit(commitID, false);
            _head = commitID;
            for (String i : newCommit.getBranches().keySet()) {
                if (_branches.containsKey(i)) {
                    _branches.put(i, newCommit.getBranches().get(i));
                }
            }
            _currentBranch = newCommit.getCurrentBranch();
            _branches.put(_currentBranch, _head);
            clearDir(_addStaging);
            _rmvList.clear();
        }
    }

    /**
     * Prints a log of commits starting from the current commit.
     */
    public void log() {
        String hash = _head;
        String message = "";
        Commit thisCommit;
        while (hash != null) {
            thisCommit = Utils.readObject(new File(_commitDump, hash),
                    Commit.class);
            if (thisCommit.getMergeParent() != null) {
                String temp = thisCommit.getPrevCommitID().substring(0, 7) + " "
                        + thisCommit.getMergeParent().substring(0, 7);
                message += String.format("===\ncommit %s\nMerge: %s\nDate:"
                                + " %s\n%s\n\n",
                        hash, temp, thisCommit.getTime(),
                        thisCommit.getCommitMessage());
            } else {
                message += String.format("===\ncommit %s\nDate: %s\n%s\n\n",
                        hash, thisCommit.getTime(),
                        thisCommit.getCommitMessage());
            }
            hash = thisCommit.getPrevCommitID();
        }
        System.out.print(message);
    }

    /**
     * Displays all commits every made. Not necessarily in order.
     */
    public void globalLog() {
        Commit thisCommit;
        for (String hash : Utils.plainFilenamesIn(_commitDump)) {
            thisCommit = Utils.readObject(new File(_commitDump, hash),
                    Commit.class);
            System.out.println(String.format("===\ncommit %s\nDate: %s\n%s\n\n",
                    hash, thisCommit.getTime(), thisCommit.getCommitMessage()));
        }
    }

    public void find(String message) {
        String result = "";
        Commit thisCommit;
        for (String i : Utils.plainFilenamesIn(_commitDump)) {
            thisCommit = Utils.readObject(new File(_commitDump, i),
                    Commit.class);
            if (thisCommit.getCommitMessage().equals(message)) {
                result += i + "\n";
            }
        }
        if (result.equals("")) {
            System.out.println("Found no commit with that message.");
        } else {
            System.out.println(result);
        }
    }
    /**
     * Prints branches by iterating through _branches. Prints staged files by
     * iterating through addStaging folder. Prints removed files by iterating
     * through remove list. Prints the last two using helper function.
     */
    public void status() {
        String msg = "";
        msg += "=== Branches ===\n";
        for (String branches : _branches.keySet()) {
            if (branches.equals(_currentBranch)) {
                msg += "*" + branches + "\n";
            } else {
                msg += branches + "\n";
            }
        }

        msg += "\n=== Staged Files ===\n";
        Blob thisBlob;
        for (String fileSHA : Utils.plainFilenamesIn(_addStaging)) {
            thisBlob = Utils.readObject(Utils.join(_addStaging, fileSHA),
                    Blob.class);
            msg += thisBlob.getName() + "\n";
        }

        msg += "\n=== Removed Files ===\n";
        for (String name : _rmvList) {
            msg += name + "\n";
        }

        List<LinkedList<String>> mods = checkMod();
        msg += "\n=== Modifications Not Staged For Commit ===\n";
        for (String i : mods.get(0)) {
            msg += i + "\n";
        }
        msg += "\n=== Untracked Files ===\n";
        for (String i : mods.get(1)) {
            msg += i + "\n";
        }

        System.out.println(msg);
    }

    /**
     * Helper function for status, which finds modified and untracked files.
     * @return A size 2 array of linkedLists, first one with modified files,
     * second with untracked files.
     */
    public List<LinkedList<String>> checkMod() {
        Commit thisCommit = Utils.readObject(Utils.join(_commitDump, _head),
                Commit.class);
        TreeMap<String, String> prevblobMap = thisCommit.getBlobMap();
        List<LinkedList<String>> mods = new ArrayList<LinkedList<String>>();
        mods.add(new LinkedList<String>());
        mods.add(new LinkedList<String>());

        for (String blobFilename : prevblobMap.keySet()) {
            if (!Utils.join(Main.getRepositoryDir(), blobFilename).exists()
                    && !_rmvList.contains(blobFilename)) {
                mods.get(0).add(blobFilename + " (deleted)");
            } else if (Utils.join(Main.getRepositoryDir(),
                    blobFilename).exists()) {
                File thisFile = Utils.join(
                        Main.getRepositoryDir(), blobFilename);
                Blob temp = new Blob(thisFile, blobFilename);
                if (!temp.getHash().equals(prevblobMap.get(blobFilename))) {
                    mods.get(0).add(blobFilename + " (modified)");
                }
            }
        }
        for (String filename
                : Utils.plainFilenamesIn(Main.getRepositoryDir())) {
            Blob temp = new Blob(Utils.join(Main.getRepositoryDir(), filename),
                    filename);
            if (!prevblobMap.containsKey(filename)
                    && !Utils.join(_addStaging, temp.getHash()).exists()) {
                mods.get(1).add(filename);
            }
        }
        return mods;
    }

    public void merge(String givenBranch) throws IOException {
        if (!Utils.plainFilenamesIn(_addStaging).isEmpty()
                || !_rmvList.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (!_branches.containsKey(givenBranch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (givenBranch.equals(_currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit currentCommit = Utils.readObject(Utils.join(_commitDump,
                _head), Commit.class);
        Commit mergeWith = Utils.readObject(Utils.join(_commitDump,
                _branches.get(givenBranch)), Commit.class);
        Commit split = commonCommit(currentCommit, mergeWith);
        if (checkUntracked(mergeWith)) {
            return;
        }
        assert split != null;
        if (Utils.sha1(Utils.serialize(split)).equals(_head)) {
            checkoutBranch(givenBranch);
            System.out.println("Current branch fast-forwarded.");
        } else if (Utils.sha1(Utils.serialize(split)).equals(
                Utils.sha1(Utils.serialize(mergeWith)))) {
            System.out.println("Given branch is an ancestor of the"
                    + " current branch.");
        } else {
            Set<String> fileNameSet = new TreeSet<>();
            fileNameSet.addAll(currentCommit.getBlobMap().keySet());
            fileNameSet.addAll(mergeWith.getBlobMap().keySet());
            fileNameSet.addAll(split.getBlobMap().keySet());
            TreeMap<String, String> mergeBlob = mergeRes(fileNameSet,
                    currentCommit, mergeWith, split);
            commit("", new ArrayList<String>(), givenBranch,
                    mergeBlob, false, true);
            loadCommit(_head, true);
        }
    }

    private TreeMap<String, String> mergeRes(Set<String> fileNameSet,
                                             Commit thisCommit,
                                             Commit mergeWith, Commit split)
            throws IOException {
        TreeMap<String, String> mergedBlob = new TreeMap<>();
        for (String i : fileNameSet) {
            if (thisCommit.getBlobMap().containsKey(i)
                    && mergeWith.getBlobMap().containsKey(i)
                    && split.getBlobMap().containsKey(i)) {
                if (thisCommit.getBlobMap().get(i).equals(
                        mergeWith.getBlobMap().get(i))) {
                    mergedBlob.put(i, split.getBlobMap().get(i));
                } else if (split.getBlobMap().get(i).equals(
                        thisCommit.getBlobMap().get(i))) {
                    mergedBlob.put(i, mergeWith.getBlobMap().get(i));
                } else if (split.getBlobMap().get(i).equals(
                        mergeWith.getBlobMap().get(i))) {
                    mergedBlob.put(i, thisCommit.getBlobMap().get(i));
                } else {
                    mergedBlob.put(i, mergeConflictAction(split, thisCommit,
                            mergeWith, i, "general"));
                }
                continue;
            }
            String conflict = mergeConflictCheck(split, thisCommit,
                    mergeWith, i);
            if (!conflict.equals("")) {
                mergedBlob.put(i, conflict);
            } else if (!split.getBlobMap().containsKey(i)
                    && mergeWith.getBlobMap().containsKey(i)
                    && !thisCommit.getBlobMap().containsKey(i)) {
                mergedBlob.put(i, mergeWith.getBlobMap().get(i));
            } else if (!split.getBlobMap().containsKey(i)
                    && !mergeWith.getBlobMap().containsKey(i)
                    && thisCommit.getBlobMap().containsKey(i)) {
                mergedBlob.put(i, thisCommit.getBlobMap().get(i));
            }
        }
        return mergedBlob;
    }

    private String mergeConflictCheck(Commit split, Commit thisCommit,
                                      Commit mergeWith, String key)
            throws IOException {
        TreeMap<String, String> splitMap = split.getBlobMap();
        TreeMap<String, String> currentMap = thisCommit.getBlobMap();
        TreeMap<String, String> mergeMap = mergeWith.getBlobMap();
        if (splitMap.containsKey(key)) {
            if (mergeMap.containsKey(key)
                    && thisCommit.getRmvList().contains(key)
                    && !splitMap.get(key).equals(mergeMap.get(key))) {
                return mergeConflictAction(split, thisCommit, mergeWith, key,
                        "first missing");
            } else if (currentMap.containsKey(key)
                    && mergeWith.getRmvList().contains(key)
                    && !splitMap.get(key).equals(currentMap.get(key))) {
                return mergeConflictAction(split, thisCommit, mergeWith, key,
                        "second missing");
            }
        }
        if (currentMap.containsKey(key) && mergeMap.containsKey(key)
                && !currentMap.get(key).equals(mergeMap.get(key))) {
            return mergeConflictAction(split, thisCommit, mergeWith, key,
                    "general");
        }
        if (!mergeMap.containsKey(key) && currentMap.containsKey(key)
                && splitMap.containsKey(key) && !splitMap.get(key).equals(
                        currentMap.get(key))) {
            return mergeConflictAction(split, thisCommit, mergeWith, key,
                    "second missing");
        }
        if (!currentMap.containsKey(key) && mergeMap.containsKey(key)
                && splitMap.containsKey(key) && !splitMap.get(key).
                equals(mergeMap.get(key))) {
            return mergeConflictAction(split, thisCommit, mergeWith,
                    key, "first missing");
        }
        return "";
    }

    private String mergeConflictAction(Commit split, Commit thisCommit,
                                        Commit mergeWith, String key,
                                        String type) throws IOException {
        File temp = new File(Main.getRepositoryDir(), ".gitlet/temp.txt");
        File temp2 = new File(Main.getRepositoryDir(),
                ".gitlet/temp2.txt");
        temp.createNewFile();
        String conflict = "";
        if (type.equals("general")) {
            Blob givenBlob = Utils.readObject(Utils.join(_blobDump,
                    mergeWith.getBlobMap().get(key)), Blob.class);
            Utils.writeContents(temp2, givenBlob.getBlob());
            conflict = "<<<<<<< HEAD\n"
                    + Utils.readContentsAsString(Utils.join(
                            Main.getRepositoryDir(), key))
                    + "=======\n"
                    + Utils.readContentsAsString(temp2)
                    + ">>>>>>>\n";
        } else if (type.equals("first missing")) {
            Blob givenBlob = Utils.readObject(Utils.join(_blobDump,
                    mergeWith.getBlobMap().get(key)), Blob.class);
            Utils.writeContents(temp, givenBlob.getBlob());
            conflict = "<<<<<<< HEAD\n"
                    + "=======\n"
                    + Utils.readContentsAsString(temp)
                    + ">>>>>>>\n";
        } else if (type.equals("second missing")) {
            conflict = "<<<<<<< HEAD\n"
                    + Utils.readContentsAsString(Utils.join(
                            Main.getRepositoryDir(), key))
                    + "=======\n"
                    + ">>>>>>>\n";
        }
        Utils.writeContents(temp, conflict);
        Blob conflictBlob = new Blob(temp, key);
        Utils.writeObject(Utils.join(_blobDump, conflictBlob.getHash()),
                conflictBlob);
        System.out.println("Encountered a merge conflict.");
        temp.delete();
        temp2.delete();
        return conflictBlob.getHash();
    }

    /**
     * Checks if one branch has a removed file, and the other one has a
     * changed file.
     * @param split splitting point
     * @param thisCommit main branch
     * @param mergeWith merging branch
     * @param key file hash
     * @return true if merge conflict, false if not.
     */
    private Boolean mergeRemoveCheck(Commit split, Commit thisCommit,
                                     Commit mergeWith, String key) {
        if (split.getBlobMap().containsKey(key)) {
            if (mergeWith.getBlobMap().containsKey(key)
                    && thisCommit.getRmvList().contains(key)
                    && !split.getBlobMap().get(key).equals(
                            mergeWith.getBlobMap().get(key))) {
                return true;
            } else if ((thisCommit.getBlobMap().containsKey(key)
                    && mergeWith.getRmvList().contains(key)
                    && !split.getBlobMap().get(key).equals(
                            thisCommit.getBlobMap().get(key)))) {
                return true;
            }
        }
        return false;
    }
    /**
     * Helper function that finds the first common commit between two commits.
     * @param com1 commit 1 that you want to compare
     * @param com2 commit 2 that you want to compare
     * @return common commit. Null if there is no common commit.
     */
    public Commit commonCommit(Commit com1, Commit com2) {
        ArrayList<String> commitCodes1 = new ArrayList<>();
        LinkedList<String> searchQ = new LinkedList<>();
        String hash;
        commitCodes1.add(Utils.sha1(Utils.serialize(com1)));
        searchQ.add(com1.getPrevCommitID());
        if (com1.getMergeParent() != null) {
            searchQ.add(com1.getMergeParent());
        }
        while (!searchQ.isEmpty()) {
            hash = searchQ.poll();
            com1 = Utils.readObject(new File(_commitDump, hash),
                    Commit.class);
            if (com1.getPrevCommitID() != null) {
                searchQ.add(com1.getPrevCommitID());
            }
            if (com1.getMergeParent() != null) {
                searchQ.add(com1.getMergeParent());
            }
            commitCodes1.add(hash);
        }

        searchQ.add(Utils.sha1(Utils.serialize(com2)));
        if (com2.getMergeParent() != null) {
            searchQ.add(com2.getMergeParent());
        }
        while (!searchQ.isEmpty()) {
            hash = searchQ.poll();
            if (commitCodes1.contains(hash)) {
                String secondHash = searchQ.poll();
                if (commitCodes1.contains(secondHash)
                        && commitCodes1.indexOf(hash) > commitCodes1.indexOf(
                            secondHash)) {
                    return Utils.readObject(Utils.join(_commitDump,
                                secondHash), Commit.class);
                }
                return Utils.readObject(Utils.join(_commitDump, hash),
                        Commit.class);
            }
            com2 = Utils.readObject(new File(_commitDump, hash),
                    Commit.class);
            if (com2.getPrevCommitID() != null) {
                searchQ.add(com2.getPrevCommitID());
            }
            if (com2.getMergeParent() != null) {
                searchQ.add(com2.getMergeParent());
            }
        }
        return null;
    }

    /** Loads in a previous commit from it's commit ID. Clears tracked files,
     * overwrites new files. Clears staging area.
     * @param loadIn the commit that is loaded in.
     * @param merge if this load is a merge or not.
     * @return returns the commit loadIn refers to.
     **/
    private Commit loadCommit(String loadIn, Boolean merge) {
        Commit newCommit = null;
        if (Utils.join(_commitDump, loadIn).exists()) {
            newCommit = Utils.readObject(Utils.join(_commitDump, loadIn),
                    Commit.class);
        } else {
            newCommit = abbrevHash(loadIn);
        }
        if (newCommit != null) {
            Commit thisCommit = Utils.readObject(Utils.join(_commitDump, _head),
                    Commit.class);
            if (merge) {
                clearDir(Main.getRepositoryDir());
            } else {
                for (String i : thisCommit.getBlobMap().keySet()) {
                    Utils.join(Main.getRepositoryDir(), i).delete();
                }
            }

            for (String i : newCommit.getBlobMap().keySet()) {
                Blob thisBlob = Utils.readObject(Utils.join(_blobDump,
                        newCommit.getBlobMap().get(i)), Blob.class);
                Utils.writeContents(Utils.join(Main.getRepositoryDir(), i),
                        thisBlob.getBlob());
            }
        }
        return newCommit;
    }

    /**
     * Checks if there are any untracked files that will be overwritten.
     * @param com1 commit that is being loaded in
     * @return false if there is, true if there isn't
     */
    private Boolean checkUntracked(Commit com1) {
        for (String i : Utils.plainFilenamesIn(Main.getRepositoryDir())) {
            Blob thisBlob = new Blob(Utils.join(Main.getRepositoryDir(), i),
                    i);
            Commit currentCommit = Utils.readObject(Utils.join(_commitDump,
                    _head), Commit.class);
            if (!currentCommit.getBlobMap().containsValue(thisBlob.getHash())
                    && !Utils.join(_addStaging, thisBlob.getHash()).exists()
                    && com1.getBlobMap().containsKey(i)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getRmvList() {
        return _rmvList;
    }

    /** Hash of the current head of the repository. **/
    private String _head = null;

    /** A treemap of all the branches that were created, and the commit hash
     * that they point to currently.
     */
    private TreeMap<String, String> _branches = new TreeMap<>();

    /** The name of the current branch. **/
    private String _currentBranch;

    /** File location for the commitDump. **/
    private File _commitDump;

    /** File location for the blobDump. **/
    private  File _blobDump;

    /** File location for adding files to commits. **/
    private  File _addStaging;

    /** File location for removing files from commits. **/
    private ArrayList<String> _rmvList = new ArrayList<>();

}
