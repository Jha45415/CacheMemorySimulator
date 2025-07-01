
package Cache;

import java.util.*;

public class SetAssociativeCache implements CacheInterface {
    private final int sets;
    private final int ways;
    private final List<Deque<CacheBlock>> cacheSets;
    private final int indexMask;
    private int hitCount = 0;
    private int missCount = 0;
    private int lastAccessedSet = -1;

    public SetAssociativeCache(int totalBlocks, int ways) {
        this.ways = ways;
        this.sets = totalBlocks / ways;
        this.indexMask = sets - 1;
        cacheSets = new ArrayList<>(sets);
        for (int i = 0; i < sets; i++) {
            cacheSets.add(new ArrayDeque<>());
        }
    }

    @Override
    public boolean access(int address) {
        int index = address & indexMask;
        int tag = address >> Integer.numberOfTrailingZeros(sets);
        lastAccessedSet = index;
        Deque<CacheBlock> set = cacheSets.get(index);
        for (CacheBlock block : set) {
            if (block.isValid() && block.getTag() == tag) {
                set.remove(block);
                set.addLast(block);
                hitCount++;
                return true;
            }
        }
        missCount++;
        CacheBlock newBlock = new CacheBlock();
        newBlock.setTag(tag);
        newBlock.setValid(true);
        if (set.size() >= ways) {
            set.pollFirst();
        }
        set.addLast(newBlock);
        return false;
    }

    @Override
    public void reset() {
        hitCount = 0;
        missCount = 0;
        lastAccessedSet = -1;
        for (Deque<CacheBlock> set : cacheSets) {
            set.clear();
        }
    }

    @Override
    public int getHitCount() {
        return hitCount;
    }

    @Override
    public int getMissCount() {
        return missCount;
    }

    @Override
    public CacheBlock[] getBlocks() {
        List<CacheBlock> all = new ArrayList<>();
        for (Deque<CacheBlock> set : cacheSets) {
            all.addAll(set);
        }
        return all.toArray(new CacheBlock[0]);
    }

    @Override
    public int getLastAccessedIndex() {
        return lastAccessedSet;
    }

    @Override
    public void insert(String address) {
        int addr = address.startsWith("0x") ? Integer.parseInt(address.substring(2), 16) : Integer.parseInt(address);
        access(addr);
    }

    public int getBlockCount() {
        return sets * ways;
    }
}
