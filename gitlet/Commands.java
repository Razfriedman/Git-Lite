package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/** A Class of alllll the commands.
 * @author Raz Friedman */
public class Commands {

    /** init command. */
    public static void init() throws IOException {
        File init = new File(".gitlet");
        if (init.exists()) {
            throw new GitletException("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
        if (init.mkdir()) {
            Commit initialCommit = new Commit("initial commit",
                    null, null, null, new Date(0));
            String commitId = initialCommit.id();
            Head head = new Head();
            head.setActiveBranch("master");
            head.setCurrentCommit(commitId);
            StagingArea stagingArea = new StagingArea();
            File stagingAreaFile = new File(".gitlet", "stagingArea");
            stagingAreaFile.createNewFile();
            Utils.writeObject(stagingAreaFile, stagingArea);
            File blobsDir = new File(".gitlet", "Blobs");
            blobsDir.mkdir();
            File commitPath = new File(".gitlet", "Commits");
            commitPath.mkdir();
            File initialCommitFile = new File(commitPath, commitId);
            initialCommitFile.createNewFile();
            Utils.writeObject(initialCommitFile, initialCommit);
            File headFile = new File(init, "head");
            headFile.createNewFile();
            Utils.writeObject(headFile, head);
        }
    }

    /** add command - that adds a file.
     * @param filePath - File name to be added. */
    public static void add(String filePath) {
        if (!new File(filePath).exists()) {
            throw new GitletException("File does not exist.");
        }
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        StagingArea staging = Utils.readObject(
                new File(".gitlet/stagingArea"), StagingArea.class);
        String commitPath = ".gitlet/Commits/" + head.currentCommit();
        Commit commit = Utils.readObject(
                new File(commitPath), Commit.class);
        File file = new File(filePath);
        Blob blob = new Blob(file);
        if (commit.containsBlob(blob)) {
            staging.removeFromToAdd(blob);
            staging.removeFromToRemove(blob);
            File stagingAreaFile = new File(".gitlet/stagingArea");
            Utils.writeObject(stagingAreaFile, staging);
            System.exit(0);
        }
        staging.addBlobToAdd(blob);
        if (staging.toRemove().containsValue(blob.id())) {
            staging.removeFromToRemove(blob);
        }
        File blobFile = new File(".gitlet/Blobs/" + blob.id());
        File stagingAreaFile = new File(".gitlet/stagingArea");
        Utils.writeObject(blobFile, blob);
        Utils.writeObject(stagingAreaFile, staging);
    }

    /** Commit command.
     * @param msg - commit message. */
    public static void commit(String msg) {
        if (msg.length() == 0) {
            throw new GitletException("Please enter a commit message.");
        }
        StagingArea staging = Utils.readObject(
                new File(".gitlet/stagingArea"), StagingArea.class);
        if (staging.toAdd().isEmpty() && staging.toRemove().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        String commitPath = ".gitlet/Commits/" + head.currentCommit();
        Commit curr = Utils.readObject(new File(commitPath), Commit.class);
        HashMap<String, String> update = curr.getBlobs();
        Commit newCommit;
        if (curr.getBlobs() == null || curr.getBlobs().size() == 0) {
            newCommit = new Commit(msg, curr.id(), staging.toAdd());
        } else {
            if (!staging.toAdd().isEmpty()) {
                for (String name : staging.toAdd().keySet()) {
                    if (!curr.getBlobs().containsValue(
                            staging.toAdd().get(name))) {
                        update.put(name, staging.toAdd().get(name));
                    }
                }
            }
            if (!staging.toRemove().isEmpty()) {
                for (String name : staging.toRemove().keySet()) {
                    if (update.containsValue(staging.toRemove().get(name))) {
                        update.remove(name);
                    }
                }
            }
            newCommit = new Commit(msg, curr.id(), update);
        }
        File newCommitFile = new File(".gitlet/Commits/" + newCommit.id());
        Utils.writeObject(newCommitFile, newCommit);
        head.setCurrentCommit(newCommit.id());
        File headFile = new File(".gitlet/head");
        Utils.writeObject(headFile, head);
        staging.clearStaging();
        File stagingAreaFile = new File(".gitlet/stagingArea");
        Utils.writeObject(stagingAreaFile, staging);
    }

    /** Rm command.
     * @param fileName - File to be removed. */
    public static void rm(String fileName) {
        StagingArea staging = Utils.readObject(
                new File(".gitlet/stagingArea"), StagingArea.class);
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        String commitPath = ".gitlet/Commits/" + head.currentCommit();
        Commit curr = Utils.readObject(new File(commitPath), Commit.class);
        if (curr.getBlobs() == null) {
            if (!staging.toAdd().containsKey(fileName)) {
                throw new GitletException("No reason to remove the file.");
            }
        }
        if (curr.getBlobs() != null) {
            if (!staging.toAdd().containsKey(fileName)
                    && !curr.getBlobs().containsKey(fileName)) {
                throw new GitletException("No reason to remove the file.");
            }
        }
        if (staging.toAdd().containsKey(fileName)) {
            Blob blob = new Blob(new File(fileName));
            staging.removeFromToAdd(blob);
        }
        if (curr.getBlobs() != null) {
            if (curr.getBlobs().containsKey(fileName)) {
                staging.toRemove().put(fileName, curr.getBlobs().get(fileName));
                Utils.restrictedDelete(fileName);
                File stagingAreaFile = new File(".gitlet/stagingArea");
                Utils.writeObject(stagingAreaFile, staging);
                System.exit(0);
            }
        }
        File stagingAreaFile = new File(".gitlet/stagingArea");
        Utils.writeObject(stagingAreaFile, staging);
    }

    /** Log command. */
    public static void log() {
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        String commitPath = ".gitlet/Commits/" + head.currentCommit();
        Commit curr = Utils.readObject(new File(commitPath), Commit.class);
        while (true) {
            System.out.println("===");
            System.out.println("commit " + curr.id());
            if (curr.getMergeParentID() != null) {
                System.out.println("Merge: "
                        + curr.id().substring(0, 7) + " "
                        + curr.getMergeParentID().substring(0, 7));
            }
            System.out.println("Date: " + curr.timestamp());
            System.out.println(curr.message());
            System.out.println();
            String parentId = curr.parentId();
            if (parentId == null) {
                break;
            }
            String nextCommitPath = ".gitlet/Commits/" + parentId;
            curr = Utils.readObject(new File(nextCommitPath), Commit.class);
        }
    }

    /** Global log command. */
    public static void globalLog() {
        for (String commitID : Utils.plainFilenamesIn(".gitlet/Commits/")) {
            String commitPath = ".gitlet/Commits/" + commitID;
            Commit curr = Utils.readObject(new File(commitPath), Commit.class);
            System.out.println("===");
            System.out.println("commit " + curr.id());
            if (curr.getMergeParentID() != null) {
                System.out.println("Merge: "
                        + curr.id().substring(0, 7) + " "
                        + curr.getMergeParentID().substring(0, 7));
            }
            System.out.println("Date: " + curr.timestamp());
            System.out.println(curr.message());
            System.out.println();
        }
    }

    /** Find command.
     *@param msg - Commit message to be found. */
    public static void find(String msg) {
        boolean found = false;
        for (String commit : Utils.plainFilenamesIn(".gitlet/Commits/")) {
            String commitPath = ".gitlet/Commits/" + commit;
            Commit curr = Utils.readObject(new File(commitPath), Commit.class);
            if (curr.message().equals(msg)) {
                found = true;
                System.out.println(curr.id());
            }
        }
        if (!found) {
            throw new GitletException("Found no commit with that message.");
        }
    }

    /** Helper for status - Lexicographic ordering.
     * @param unordered unordered strings
     * @return - Ordered strings*/
    private static Object[] statusHelper(Object[] unordered) {
        for (int i = 0; i < unordered.length - 1; ++i) {
            for (int j = i + 1; j < unordered.length; ++j) {
                if (unordered[i].toString().compareTo(
                        unordered[j].toString()) > 0) {
                    String temp = unordered[i].toString();
                    unordered[i] = unordered[j];
                    unordered[j] = temp;
                }
            }
        }
        return unordered;
    }

    /** Status command. */
    public static void status() {
        if (!new File(".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        StagingArea staging = Utils.readObject(
                new File(".gitlet/stagingArea"), StagingArea.class);
        Object[] branches = statusHelper(head.getBranches().keySet().toArray());
        Object[] staged = statusHelper(staging.toAdd().keySet().toArray());
        Object[] removed = statusHelper(staging.toRemove().keySet().toArray());
        System.out.println("=== Branches ===");
        for (Object branch : branches) {
            if (branch.equals(head.getActiveBranch())) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        if (staged.length == 0) {
            System.out.println();
        }
        if (staged.length > 0) {
            for (Object o : staged) {
                System.out.println(o);
            }
            System.out.println();
        }
        System.out.println("=== Removed Files ===");
        if (removed.length == 0) {
            System.out.println();
        }
        if (removed.length > 0) {
            for (Object o : removed) {
                System.out.println(o);
            }
            System.out.println();
        }
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Checkout command.
     * @param fileName - A file to be checked out. */
    public static void checkoutF(String fileName) {
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        String commitPath = ".gitlet/Commits/" + head.currentCommit();
        Commit curr = Utils.readObject(new File(commitPath), Commit.class);
        if (!curr.getBlobs().containsKey(fileName)) {
            throw new GitletException("File does not exist in that commit.");
        } else {
            String blobID = curr.getBlobs().get(fileName);
            File blobFile = new File(".gitlet/Blobs/" + blobID);
            Blob blob = Utils.readObject(blobFile, Blob.class);
            Utils.writeContents(new File(blob.fileName()), blob.contents());
        }
    }

    /** Checkout command.
     * @param fileName - A file to be checked out
     * @param commitID - A specified commit.*/
    public static void checkoutID(String commitID, String fileName) {
        String id = "";
        boolean found = false;
        if (commitID.length() < Utils.UID_LENGTH) {
            for (String fullID : Utils.plainFilenamesIn(".gitlet/Commits/")) {
                if (fullID.startsWith(commitID)) {
                    id = fullID;
                    found = true;
                }
            }
        }
        if (commitID.length() == Utils.UID_LENGTH) {
            id = commitID;
            if (Utils.plainFilenamesIn(".gitlet/Commits/").contains(id)) {
                found = true;
            }
        }
        if (!found) {
            throw new GitletException("No commit with that id exists.");
        } else {
            String commitPath = ".gitlet/Commits/" + id;
            Commit curr = Utils.readObject(new File(commitPath), Commit.class);
            if (!curr.getBlobs().containsKey(fileName)) {
                throw new GitletException(
                        "File does not exist in that commit.");
            } else {
                String blobID = curr.getBlobs().get(fileName);
                File blobFile = new File(".gitlet/Blobs/" + blobID);
                Blob blob = Utils.readObject(blobFile, Blob.class);
                Utils.writeContents(new File(blob.fileName()), blob.contents());
            }
        }
    }

    /** Checkout command.
     * @param branch - A branch to be checked out. */
    public static void checkoutB(String branch) {
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        StagingArea staging = Utils.readObject(
                new File(".gitlet/stagingArea"), StagingArea.class);
        if (!head.checkBranch(branch)) {
            throw new GitletException("No such branch exists.");
        }
        if (head.isActiveBranch(branch)) {
            throw new GitletException(
                    "No need to checkout the current branch.");
        }
        String commitPath1 = ".gitlet/Commits/" + head.currentCommit();
        Commit oldHead = Utils.readObject(new File(commitPath1), Commit.class);
        head.checkoutBranch(branch);
        String commitPath2 = ".gitlet/Commits/" + head.currentCommit();
        Commit newHead = Utils.readObject(new File(commitPath2), Commit.class);
        HashMap<String, String> untracked = new HashMap<>();
        for (String fileWorkDir : Utils.plainFilenamesIn(".")) {
            Blob workingBlob = new Blob(new File(fileWorkDir));
            if (!oldHead.containsBlob(workingBlob)
                    && !staging.toAdd().containsValue(workingBlob.id())) {
                untracked.put(workingBlob.fileName(), workingBlob.id());
            }
        }
        if (!untracked.isEmpty() && newHead.getBlobs() != null) {
            for (String name : untracked.keySet()) {
                if (newHead.getBlobs().containsKey(name)) {
                    throw new GitletException("There is an untracked"
                            + " file in the way; delete it or add it first.");
                }
            }
        }
        if (oldHead.getBlobs() != null) {
            for (String fileName : oldHead.getBlobs().keySet()) {
                Utils.restrictedDelete(fileName);
            }
        }
        if (newHead.getBlobs() != null) {
            for (String id: newHead.getBlobs().values()) {
                File blobFile = new File(".gitlet/Blobs/" + id);
                Blob blob = Utils.readObject(blobFile, Blob.class);
                Utils.writeContents(new File(blob.fileName()), blob.contents());
            }
        }
        staging.clearStaging();
        File stagingAreaFile = new File(".gitlet/stagingArea");
        File headFile = new File(".gitlet/head");
        Utils.writeObject(stagingAreaFile, staging);
        Utils.writeObject(headFile, head);
    }

    /** Branch command.
     * @param name - A branch to be created. */
    public static void branch(String name) {
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        if (head.checkBranch(name)) {
            throw new GitletException(
                    "A branch with that name already exists.");
        }
        String curr = head.currentCommit();
        head.setNewPair(name, curr);
        File headFile = new File(".gitlet/head");
        Utils.writeObject(headFile, head);
    }

    /** Rm-Branch command.
     * @param branchName - A branch to be removed. */
    public static void rmBranch(String branchName) {
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        if (head.isActiveBranch(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        if (!head.getBranches().containsKey(branchName)) {
            throw new GitletException(
                    "A branch with that name does not exist.");
        }
        head.deleteBranch(branchName);
        File headFile = new File(".gitlet/head");
        Utils.writeObject(headFile, head);
    }

    /** Reset command.
     * @param commitID -The commit to reset by. */
    public static void reset(String commitID) {
        StagingArea staging = Utils.readObject(
                new File(".gitlet/stagingArea"), StagingArea.class);
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        String commitPath = ".gitlet/Commits/" + head.currentCommit();
        Commit old = Utils.readObject(new File(commitPath), Commit.class);
        String id = "";
        boolean found = false;
        if (commitID.length() < Utils.UID_LENGTH) {
            for (String fullID : Utils.plainFilenamesIn(".gitlet/Commits/")) {
                if (fullID.startsWith(commitID)) {
                    id = fullID;
                    found = true;
                    break;
                }
            }
        }
        if (commitID.length() == Utils.UID_LENGTH) {
            id = commitID;
            if (Utils.plainFilenamesIn(".gitlet/Commits/").contains(id)) {
                found = true;
            }
        }
        if (!found) {
            throw new GitletException("No commit with that id exists.");
        }
        String commitPath2 = ".gitlet/Commits/" + id;
        Commit resetCommit = Utils.readObject(
                new File(commitPath2), Commit.class);
        Set<String> oldTracked = old.getBlobs().keySet();
        Set<String> resetTracked = resetCommit.getBlobs().keySet();
        resetTracked.removeAll(oldTracked);
        for (String fileName : resetTracked) {
            if (new File(fileName).exists()) {
                throw new GitletException("There is an untracked file "
                        + "in the way; delete it or add it first.");
            }
        }
        for (String fileName : old.getBlobs().keySet()) {
            Utils.restrictedDelete(fileName);
        }
        for (String resetBlob : resetCommit.getBlobs().values()) {
            File blobFile = new File(".gitlet/Blobs/" + resetBlob);
            Blob blob = Utils.readObject(blobFile, Blob.class);
            Utils.writeContents(new File(blob.fileName()), blob.contents());
        }
        head.setCurrentCommit(resetCommit.id());
        staging.clearStaging();
        File headFile = new File(".gitlet/head");
        Utils.writeObject(headFile, head);
        File stagingAreaFile = new File(".gitlet/stagingArea");
        Utils.writeObject(stagingAreaFile, staging);
    }

    /** Helper function to find the split point.
     * @param branchName - Branch for merge
     * @return Commit id of the split commit.*/
    private static String getSplitPoint(String branchName) {
        boolean contrainsMerge = false;
        HashSet<String> timesOfBranch = new HashSet<>();
        String splitPointID = ""; String mergeID = "";
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        String commitPath = ".gitlet/Commits/"
                + head.getHeadofBranch(branchName);
        Commit given = Utils.readObject(
                new File(commitPath), Commit.class);
        while (true) {
            if (given.getMergeParentID() != null) {
                contrainsMerge = true;
                mergeID = given.id();
            }
            timesOfBranch.add(given.timestamp());
            String parentId = given.parentId();
            if (parentId == null) {
                break;
            }
            String nextCommitPath = ".gitlet/Commits/" + parentId;
            given = Utils.readObject(
                    new File(nextCommitPath), Commit.class);
        }
        String currPath = ".gitlet/Commits/" + head.currentCommit();
        Commit curr = Utils.readObject(new File(currPath), Commit.class);
        while (true) {
            if (curr.getMergeParentID() != null) {
                contrainsMerge = true;
                mergeID = curr.id();

            }
            if (timesOfBranch.contains(curr.timestamp())) {
                splitPointID = curr.id();
                break;
            }
            String parentId = curr.parentId();
            if (parentId == null) {
                break;
            }
            String nextCommitPath = ".gitlet/Commits/" + parentId;
            curr = Utils.readObject(new File(nextCommitPath), Commit.class);
        }
        if (contrainsMerge) {
            String nextCommitPath = ".gitlet/Commits/" + mergeID;
            Commit merged = Utils.readObject(new
                    File(nextCommitPath), Commit.class);
            splitPointID = merged.getMergeParentID();
        }
        if (splitPointID.equals(head.getHeadofBranch(branchName))) {
            throw new GitletException(
                    "No need to checkout the current branch.");
        }
        if (splitPointID.equals(head.currentCommit())) {
            head.setCurrentCommit(head.getHeadofBranch(branchName));
            Utils.writeObject(new File(".gitlet/head"), head);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        return splitPointID;
    }

    /** Merge helper.
     * @param curr - current commit
     * @param given - given commmit
     * @param staging - staging area. */
    private static void untrackedMerge(
            Commit curr, Commit given, StagingArea staging) {
        HashMap<String, String> untracked = new HashMap<>();
        for (String fileWorkDir : Utils.plainFilenamesIn(".")) {
            Blob workingBlob = new Blob(new File(fileWorkDir));
            if (!curr.containsBlob(workingBlob)
                    && !staging.toAdd().containsValue(workingBlob.id())) {
                untracked.put(workingBlob.fileName(), workingBlob.id());
            }
        }
        if (!untracked.isEmpty()) {
            for (String name : untracked.keySet()) {
                if (given.getBlobs().containsKey(name)) {
                    System.out.println("There is an untracked"
                            + " file in the way; delete it or add it first.");
                    System.exit(0);
                }
            }
        }
    }

    /** Merge command.
     * @param branchName - The branch with which current will merge. */
    public static void merge(String branchName) {
        StagingArea staging = Utils.readObject(
                new File(".gitlet/stagingArea"), StagingArea.class);
        Head head = Utils.readObject(new File(".gitlet/head"), Head.class);
        if (!staging.toAdd().isEmpty() || !staging.toRemove().isEmpty()) {
            throw new GitletException("You have uncommitted changes.");
        }
        if (!head.checkBranch(branchName)) {
            throw new GitletException(
                    "A branch with that name does not exist.");
        }
        if (head.isActiveBranch(branchName)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
        String commitPath = ".gitlet/Commits/"
                + head.getHeadofBranch(branchName);
        Commit given = Utils.readObject(
                new File(commitPath), Commit.class);
        String commitPath2 = ".gitlet/Commits/" + head.currentCommit();
        Commit curr = Utils.readObject(
                new File(commitPath2), Commit.class);
        untrackedMerge(curr, given, staging);
        Commit split = Utils.readObject(new File(".gitlet/Commits/"
                + getSplitPoint(branchName)), Commit.class);
        boolean conflict = merger(curr, given, split, staging);
        String message = "Merged " + branchName
                + " into " + head.getActiveBranch() + ".";
        HashMap<String, String> currentblobs = curr.getBlobs();
        if (!staging.toAdd().isEmpty()) {
            for (String name : staging.toAdd().keySet()) {
                currentblobs.put(name, staging.toAdd().get(name));
            }
        }
        if (!staging.toRemove().isEmpty()) {
            for (String name : staging.toRemove().keySet()) {
                currentblobs.put(name, staging.toRemove().get(name));
            }
        }
        Commit mergedCommit = new Commit(
                message, curr.id(), given.id(), currentblobs);
        head.setCurrentCommit(mergedCommit.id());
        staging.clearStaging();
        Utils.writeObject(new File(".gitlet/Commits/"
                + mergedCommit.id()), mergedCommit);
        Utils.writeObject(new File(".gitlet/head"), head);
        Utils.writeObject(new File(".gitlet/stagingArea"), staging);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
            System.exit(0);
        }
    }

    /** Helper function to check null.
     * @param split - split
     * @param curr - current
     * @param given - given
     * @return iff all not null.*/
    private static boolean checkNotNull(
            String split, String curr, String given) {
        return split != null && curr != null && given != null;
    }

    /** Helper function to merge files.
     * @param split - split commit
     * @param curr - current commit
     * @param given - given commit
     * @param stageArea - staging area
     * @return iff all not null.*/
    private static boolean merger(
            Commit curr, Commit given, Commit split, StagingArea stageArea) {
        boolean conflict = false;
        HashSet<String> allFiles = new HashSet<>();
        HashMap<String, String> currBlobs = new HashMap<>();
        HashMap<String, String> givenBlobs = new HashMap<>();
        HashMap<String, String> splitBlobs = new HashMap<>();
        if (curr.getBlobs() != null) {
            allFiles.addAll(curr.getBlobs().keySet());
            currBlobs = curr.getBlobs();
        }
        if (given.getBlobs() != null) {
            allFiles.addAll(given.getBlobs().keySet());
            givenBlobs = given.getBlobs();
        }
        if (split.getBlobs() != null) {
            allFiles.addAll(split.getBlobs().keySet());
            splitBlobs = split.getBlobs();
        }
        for (String file : allFiles) {
            if (checkNotNull(splitBlobs.get(file),
                    currBlobs.get(file), givenBlobs.get(file))) {
                if (!splitBlobs.get(file).equals(givenBlobs.get(file))
                        && splitBlobs.get(file).equals(currBlobs.get(file))) {
                    checkoutID(given.id(), file);
                    stageArea.addBlobToAdd(new Blob(new File(file)));
                    continue;
                }
                if (fileHelper1(splitBlobs, currBlobs, givenBlobs, file)) {
                    continue;
                }
            }
            if (fileHelper2(splitBlobs, currBlobs, givenBlobs, file)) {
                continue;
            }
            if (splitBlobs.get(file) == null && currBlobs.get(file) == null
                    && givenBlobs.get(file) != null) {
                checkoutID(given.id(), file);
                stageArea.addBlobToAdd(new Blob(new File(file)));
                continue;
            }
            if (splitBlobs.get(file) != null && givenBlobs.get(file) == null
                    && splitBlobs.get(file).equals(currBlobs.get(file))) {
                if (stageArea.toAdd().containsKey(file)) {
                    stageArea.removeFromToAdd(new Blob(new File(file)));
                    Utils.restrictedDelete(file);
                    continue;
                }
                if (!stageArea.toRemove().containsKey(file)) {
                    stageArea.addBlobToRemove(new Blob(new File(file)));
                    Utils.restrictedDelete(file);
                    continue;
                }
            } else {
                conflictFile(file, curr, given);
                conflict = true;
            }
        }
        return conflict;
    }

    /** Helper function for merge.
     * @param file -  file name.
     * @param splitBlobs - hashmap
     * @param currBlobs - hashmap
     * @param givenBlobs - hashmap.
     * @return meets reqs. */
    private static boolean fileHelper1(
            HashMap<String, String> splitBlobs, HashMap<String,
            String> currBlobs, HashMap<String,
            String> givenBlobs, String file) {
        return (splitBlobs.get(file).equals(givenBlobs.get(file))
                && !splitBlobs.get(file).equals(currBlobs.get(file)))
                || (splitBlobs.get(file).equals(givenBlobs.get(file))
                && !splitBlobs.get(file).equals(currBlobs.get(file)))
                || (!splitBlobs.get(file).equals(givenBlobs.get(file))
                && !splitBlobs.get(file).equals(currBlobs.get(file))
                && givenBlobs.get(file).equals(currBlobs.get(file)));
    }

    /** Helper function for merge.
     * @param file -  file name.
     * @param splitBlobs - hashmap
     * @param currBlobs - hashmap
     * @param givenBlobs - hashmap.
     * @return meets reqs. */
    private static boolean fileHelper2(
            HashMap<String, String> splitBlobs, HashMap<String,
            String> currBlobs, HashMap<String,
            String> givenBlobs, String file) {
        return  (givenBlobs.get(file) == null
                && currBlobs.get(file) == null
                && new File(file).exists())
                || (splitBlobs.get(file) == null
                && currBlobs.get(file) != null
                && givenBlobs.get(file) == null)
                || (splitBlobs.get(file) != null
                && splitBlobs.get(file).equals(givenBlobs.get(file))
                && currBlobs.get(file) == null);
    }


    /** Helper function for merge.
     * @param fileName - conflict file name.
     * @param curr - current
     * @param given - given */
    private static void conflictFile(
            String fileName, Commit curr, Commit given) {
        StagingArea staging = Utils.readObject(
                new File(".gitlet/stagingArea"), StagingArea.class);
        String currContents = "";
        String givenContents = "";
        File blobFile = new File(".gitlet/Blobs/"
                + curr.getBlobs().get(fileName));
        if (blobFile.exists()) {
            Blob blobCurr = Utils.readObject(blobFile, Blob.class);
            currContents = Utils.readContentsAsString(blobCurr.getFile());
        }
        File blobFile2 = new File(".gitlet/Blobs/"
                + given.getBlobs().get(fileName));
        if (blobFile2.exists()) {
            Blob blobGiven = Utils.readObject(blobFile2, Blob.class);
            File temp = new File(".gitlet/temp");
            Utils.writeContents(temp, blobGiven.contents());
            givenContents = Utils.readContentsAsString(temp);
        }
        String content = "<<<<<<< HEAD\n"
                + currContents + "=======\n"
                + givenContents + ">>>>>>>\n";
        Utils.writeContents(new File(fileName), content);
        Blob conflictBlob = new Blob(new File(fileName));
        staging.addBlobToAdd(conflictBlob);
    }
}
