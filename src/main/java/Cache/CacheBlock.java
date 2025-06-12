//Represents a cache block with two fields:
//    tag (int)
//    valid (boolean)
package Cache;
public class CacheBlock {
    private int tag;
    private boolean valid;

    public CacheBlock() {
        this.tag = -1;
        this.valid = false;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public String toString() {
        return "Tag: " + tag + ", Valid: " + valid;
    }
}
