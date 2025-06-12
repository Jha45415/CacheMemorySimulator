//Interface for all cache types.
//Standardized methods:
//    access(), reset(), getHitCount(), getBlocks(), etc.
package Cache;

public interface CacheInterface {

    boolean access(int address);
        void reset();
        int getHitCount();
        int getMissCount();
        CacheBlock[] getBlocks();
        int getLastAccessedIndex();


}
