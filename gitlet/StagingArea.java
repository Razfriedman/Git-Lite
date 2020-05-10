package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** A class that represents the staging area of gitlet.
 *  @author Raz Friedman
 */
public class StagingArea implements Serializable {

    /** File Name - Blob ID. */
    private HashMap<String, String> _toAdd;

    /** File Name - Blob ID. */
    private HashMap<String, String> _toRemove;

    /** Constructor for staging area. */
    public StagingArea() {
        _toAdd = new HashMap<>();
        _toRemove = new HashMap<>();
    }

    /** Clears the staging area. */
    public void clearStaging() {
        _toAdd.clear();
        _toRemove.clear();
    }
    /** Getter - gets the blobs that need to be added.
     * @return HahsMap of blobs */
    public HashMap<String, String> toAdd() {
        return _toAdd;
    }

    /** Getter - gets the blobs that need to be added.
     * @return HahsMap of blobs */
    public HashMap<String, String> toRemove() {
        return _toRemove;
    }

    /** Adds a blob to ToAdd.
     *@param blob - blob to be added. */
    public void addBlobToAdd(Blob blob) {
        _toAdd.put(blob.fileName(), blob.id());
    }

    /** Adds a blob to ToRemove.
     *@param blob - blob to be added. */
    public void addBlobToRemove(Blob blob) {
        _toRemove.put(blob.fileName(), blob.id());
    }

    /** Removes a blob from ToAdd.
     *@param blob - blob to be removed. */
    public void removeFromToAdd(Blob blob) {
        _toAdd.remove(blob.fileName());
    }

    /** Removes a blob from ToRemove.
     *@param blob - blob to be removed. */
    public void removeFromToRemove(Blob blob) {
        _toRemove.remove(blob.fileName());
    }
}
