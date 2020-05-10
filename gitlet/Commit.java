package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

import static gitlet.Utils.sha1;

/** A class representing the meta-data of a commit.
 * @author Raz Friedman */
public class Commit implements Serializable {

    /** This commit's time stamp. */
    private String _timeStamp;

    /** This commit's SHA1 id. */
    private String _commitId;

    /** This commit's message. */
    private String _msg;

    /** The commit's merged parent's SH1. */
    private String _mergeParentID;

    /** The blob's associated with this commit.
     * HashMap Blob Name - Blob ID */
    private HashMap<String, String> _blobs;

    /** The commit's parent's SH1. */
    private String _parentId;

    /** Array of blob names associated with this commit. */
    private ArrayList<String> _blobNames = new ArrayList<>();

    /** The commit's timestamp. */
    private static SimpleDateFormat formatter
            = new SimpleDateFormat("EE MMM d k:mm:ss y Z");

    /** Constructor for the Commit class.
     * @param msg - Commit message
     * @param parentId - Parent id
     * @param blobs - HashMap of blobs*/
    public Commit(String msg, String parentId, HashMap<String, String> blobs) {
        this(msg, parentId, null, blobs, new Date());
    }
    /** Constructor for the Commit class.
     * @param msg - Commit message
     * @param parentId - Parent id
     * @param mergeParent - Merged parent ID
     * @param blobs - HashMap of blobs*/
    public Commit(String msg, String parentId,
                  String mergeParent, HashMap<String, String> blobs) {
        this(msg, parentId, mergeParent, blobs, new Date());
    }

    /** Constructor for the Commit class that includes a timestamp argument.
     * @param msg - Commit message
     * @param parentId - Parent id
     * @param blobs - HashMap of blobs
     * @param timestamp timestamp of commit
     * @param mergeParentID - merged parent ID*/
    public Commit(String msg, String parentId, String mergeParentID,
                  HashMap<String, String> blobs, Date timestamp) {
        _msg = msg;
        _parentId = parentId;
        _mergeParentID = mergeParentID;
        _timeStamp = formatter.format(timestamp);
        _blobs = blobs;
        String concatedBlobIds = "";
        if (_blobs != null) {
            for (String name : _blobs.keySet()) {
                concatedBlobIds += blobs.get(name);
                _blobNames.add(name);
            }
            _commitId = sha1(_msg, _parentId,
                    _timeStamp, concatedBlobIds);
        }
        _commitId = sha1(_msg, _timeStamp);
    }
    /** Getter - commit timestamp.
     * @return the commit's timestamp */
    public String timestamp() {
        return _timeStamp;
    }
    /** Getter - commit id.
     * @return the commit's id */
    public String id() {
        return _commitId;
    }

    /** Getter - commit message.
     * @return the commit's message */
    public String message() {
        return _msg;
    }

    /** Getter - commit parent.
     * @return the commit's parent id */
    public String parentId() {
        return _parentId;
    }

    /** Checks if commit contains a blob.
     * @param blob - the blob being checked
     * @return true iff contains blob */
    public boolean containsBlob(Blob blob) {
        if (_blobs == null) {
            return false;
        }
        return _blobs.containsValue(blob.id());
    }

    /** Getter - commit merge parent.
     * @return the commit's merge parent id */
    public String getMergeParentID() {
        return _mergeParentID;
    }

    /** Getter - commit blobs.
     * @return the commit's blobs */
    public HashMap<String, String> getBlobs() {
        return _blobs;
    }
}
