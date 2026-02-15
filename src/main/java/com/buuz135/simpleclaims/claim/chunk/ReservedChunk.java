package com.buuz135.simpleclaims.claim.chunk;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a reserved chunk (perimeter chunk) that cannot be claimed by other parties.
 * These chunks form a protective perimeter around a party's claimed chunks.
 */
public class ReservedChunk {
    
    private UUID reservedBy; // The party that has reserved this chunk
    private int chunkX;
    private int chunkZ;
    
    public ReservedChunk(UUID reservedBy, int chunkX, int chunkZ) {
        this.reservedBy = reservedBy;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
    
    public UUID getReservedBy() {
        return reservedBy;
    }
    
    public void setReservedBy(UUID reservedBy) {
        this.reservedBy = reservedBy;
    }
    
    public int getChunkX() {
        return chunkX;
    }
    
    public void setChunkX(int chunkX) {
        this.chunkX = chunkX;
    }
    
    public int getChunkZ() {
        return chunkZ;
    }
    
    public void setChunkZ(int chunkZ) {
        this.chunkZ = chunkZ;
    }
    
    public static String formatCoordinates(int chunkX, int chunkZ) {
        return chunkX + ":" + chunkZ;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservedChunk that = (ReservedChunk) o;
        return chunkX == that.chunkX && chunkZ == that.chunkZ;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(chunkX, chunkZ);
    }
}
