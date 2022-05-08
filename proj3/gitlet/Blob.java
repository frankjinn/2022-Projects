package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    Blob(File file, String name) {
        _blob = Utils.readContents(file);
        _hash = Utils.sha1(_blob) + Utils.sha1(name);
        _name = name;
    }

    public byte[] getBlob() {
        return _blob;
    }

    public String getHash() {
        return _hash;
    }

    public String getName() {
        return _name;
    }

    /** Byte array for this blob.**/
    private byte[] _blob;

    /** Hashcode for the blob.**/
    private String _hash;

    /** Filename for the blob.**/
    private String _name;

}
