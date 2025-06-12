//Implements direct-mapped logic.
//Address is split into index and tag
//HIT occurs if cache[index].tag == tag and valid == true
//MISS causes replacement of the block
package Cache;
public class DirectMappedCache implements CacheInterface
 {
    private int lastIndexAccessed = -1;
    private int hitCount = 0;
    private int missCount = 0;

    private final CacheBlock[] blocks;
    private final int size;

    public DirectMappedCache(int size) {
        this.size = size;
        this.blocks = new CacheBlock[size];
        for (int i = 0; i < size; i++) {
            blocks[i] = new CacheBlock();
        }
    }

    public int getHitCount() {
        return hitCount;
    }

    public int getMissCount() {
        return missCount;
    }
    public void reset() {

        for (CacheBlock block : blocks) {
            block.setValid(false);
            block.setTag(0);
        }
        hitCount = 0;
        missCount = 0;
    }
    public CacheBlock[] getBlocks() {
        return blocks;
    }

    public boolean access(int address) {
        int index = address % blocks.length;
        int tag = address / blocks.length;

        lastIndexAccessed = index;

        CacheBlock block = blocks[index];
        if (block.isValid() && block.getTag() == tag) {
            hitCount++;
            return true;
        } else {
            missCount++;
            block.setTag(tag);
            block.setValid(true);
            return false;
        }
    }

    public int getLastAccessedIndex() {
        return lastIndexAccessed;
    }



}
