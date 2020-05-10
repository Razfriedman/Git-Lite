package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** A class the stores information pertaining to the branches
 * and current commits of the initialized gitlet.
 *  @author Raz Friedman
 */
public class Head implements Serializable {

    /** Name of active branch. */
    private String _activeBranch;

    /** HashMap Branch Name --> latest commit. */
    private HashMap<String, String> _branches;

    /** Constructor for head object.*/
    public Head() {
        _activeBranch = "";
        _branches = new HashMap<>();
    }

    /** setter - sets the active branch.
     *@param branchName - Branch name */
    public void setActiveBranch(String branchName) {
        _activeBranch = branchName;
    }

    /** Checks if branch is active.
     * @param branch - the branch being checked
     * @return true iff active */
    public boolean isActiveBranch(String branch) {
        return branch.equals(_activeBranch);
    }

    /** Getter - gets the active branch.
     * @return Name of active branch */
    public String getActiveBranch() {
        return _activeBranch;
    }

    /** Getter - the branches Hashmap.
     * @return all branches and current commits */
    public HashMap<String, String> getBranches() {
        return _branches;
    }

    /** setter - sets the current commit.
     *@param currentCommit - commit */
    public void setCurrentCommit(String currentCommit) {
        _branches.put(_activeBranch, currentCommit);
    }

    /** setter - sets a new pair.
     *@param name - Branch name
     *@param commit - commit id */
    public void setNewPair(String name, String commit) {
        _branches.put(name, commit);
    }

    /** Deletes a branch from the HashMap.
     * @param branch - to be deleted. */
    public void deleteBranch(String branch) {
        _branches.remove(branch);
    }

    /** Getter - current commit.
     * @return current commit's ID */
    public String currentCommit() {
        return _branches.get(_activeBranch);
    }

    /** Getter - head of a given branch.
     * @param branchName - Branch name.
     * @return the commit ID of the head of the branch */
    public String getHeadofBranch(String branchName) {
        return _branches.get(branchName);
    }

    /** Checks out a given branch.
     * @param branch - Name of branch to be checked out.
     * @return Commit ID of head of said branch. */
    public String checkoutBranch(String branch) {
        _activeBranch = branch;
        return _branches.get(branch);
    }

    /** Checks if branch exists.
     * @param branchName - name of branch
     * @return true iff branch exists. */
    public boolean checkBranch(String branchName) {
        return _branches.keySet().contains(branchName);
    }
}
