
package Cache;

public interface CacheInterface {
    boolean access(int address);
    void insert(String address);
    int getHitCount();
    int getMissCount();
    void reset();
    CacheBlock[] getBlocks();
    int getLastAccessedIndex();
    int getBlockCount();

}
