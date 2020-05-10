package gitlet;
import java.io.File;
import java.io.Serializable;


/** A class representing the contents of files.
 * @author Raz Friedman */
public class Blob implements Serializable {

    /** The file in blob. */
    private File _file;
    /** The file name for blob. */
    private String _fileName;
    /** Secure Hash 1 code for blob. */
    private String _blobId;
    /** The serial sequence of bytes of blob. */
    private byte[] _contents;


    /** Constructs a blob object.
     * @param file - The file contained in blob */
    public Blob(File file) {
        _file = file;
        _fileName = file.getName();
        _contents = Utils.readContents(_file);
        _blobId = Utils.sha1(_fileName, _contents);
    }
    /** Getter - file name.
     * @return The file name */
    public String fileName() {
        return _fileName;
    }

    /** Getter - file contents.
     * @return the file's content */
    public byte[] contents() {
        return _contents;
    }

    /** Getter - file.
     * @return the file */
    public File getFile() {
        return _file;
    }

    /** Getter - ID.
     * @return the file */
    public String id() {
        return _blobId;
    }
}
