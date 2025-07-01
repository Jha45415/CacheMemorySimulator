package Cache;

public class MultiLevelCache implements CacheInterface {
    private final CacheInterface L1Cache;
    private final CacheInterface L2Cache;
    private final CacheInterface L3Cache;
    private int lastIndexAccessed = -1;
    private boolean lastAccessWasHit = false;
    private String lastHitLevel = "MISS";

    public MultiLevelCache(CacheInterface l1, CacheInterface l2, CacheInterface l3) {
        this.L1Cache = l1;
        this.L2Cache = l2;
        this.L3Cache = l3;
    }

    @Override
    public boolean access(int address) {
        if (L1Cache.access(address)) {
            lastHitLevel = "L1";
            lastAccessWasHit = true;
            lastIndexAccessed = L1Cache.getLastAccessedIndex();
            return true;
        }
        if (L2Cache.access(address)) {
            L1Cache.insert(String.valueOf(address));
            lastHitLevel = "L2";
            lastAccessWasHit = true;
            lastIndexAccessed = L2Cache.getLastAccessedIndex();
            return true;
        }
        if (L3Cache.access(address)) {
            L2Cache.insert(String.valueOf(address));
            L1Cache.insert(String.valueOf(address));
            lastHitLevel = "L3";
            lastAccessWasHit = true;
            lastIndexAccessed = L3Cache.getLastAccessedIndex();
            return true;
        }
        L3Cache.insert(String.valueOf(address));
        L2Cache.insert(String.valueOf(address));
        L1Cache.insert(String.valueOf(address));
        lastHitLevel = "MISS";
        lastAccessWasHit = false;
        lastIndexAccessed = -1;
        return false;
    }

    @Override
    public int getLastAccessedIndex() {
        return lastIndexAccessed;
    }

    public boolean wasLastAccessHit() {
        return lastAccessWasHit;
    }

    public String getLastHitLevel() {
        return lastHitLevel;
    }

    @Override
    public void insert(String address) {
        int addr = address.startsWith("0x") ? Integer.parseInt(address.substring(2), 16) : Integer.parseInt(address);
        access(addr);
    }

    @Override
    public int getHitCount() {
        return L1Cache.getHitCount() + L2Cache.getHitCount() + L3Cache.getHitCount();
    }

    @Override
    public int getMissCount() {
        return L1Cache.getMissCount() + L2Cache.getMissCount() + L3Cache.getMissCount();
    }

    @Override
    public void reset() {
        L1Cache.reset();
        L2Cache.reset();
        L3Cache.reset();
        lastIndexAccessed = -1;
        lastAccessWasHit = false;
        lastHitLevel = "MISS";
    }

    @Override
    public CacheBlock[] getBlocks() {
        return L1Cache.getBlocks();
    }

    public CacheBlock[] getL1Blocks() { return L1Cache.getBlocks(); }
    public CacheBlock[] getL2Blocks() { return L2Cache.getBlocks(); }
    public CacheBlock[] getL3Blocks() { return L3Cache.getBlocks(); }

    public int getL1BlockCount() { return L1Cache.getBlockCount(); }
    public int getL2BlockCount() { return L2Cache.getBlockCount(); }
    public int getL3BlockCount() { return L3Cache.getBlockCount(); }

    @Override
    public int getBlockCount() {
        return L1Cache.getBlockCount();
    }
}
