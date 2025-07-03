
package Cache;

public class DirectMappedCache implements CacheInterface {
    private int lastIndexAccessed = -1;//the cache block is empty and has not been used yet.
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
        lastIndexAccessed = -1;
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

    public void insert(String address) {
        int addr = address.startsWith("0x") ? Integer.parseInt(address.substring(2), 16) : Integer.parseInt(address);
        int index = addr % blocks.length;
        int tag = addr / blocks.length;
        CacheBlock block = blocks[index];
        block.setTag(tag);
        block.setValid(true);
    }


    public int getBlockCount() {
        return blocks.length;
    }
}
