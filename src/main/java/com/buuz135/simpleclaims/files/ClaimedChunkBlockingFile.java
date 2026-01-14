package com.buuz135.simpleclaims.files;

import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.buuz135.simpleclaims.claim.tracking.ModifiedTracking;
import com.buuz135.simpleclaims.util.FileUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.util.io.BlockingDiskFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class ClaimedChunkBlockingFile extends BlockingDiskFile {

    private HashMap<String, HashMap<String, ChunkInfo>> chunks;

    public ClaimedChunkBlockingFile() {
        super(Path.of(FileUtils.CLAIM_PATH));
        this.chunks = new HashMap<>();
    }

    @Override
    protected void read(BufferedReader bufferedReader) throws IOException {
        var rootElement = JsonParser.parseReader(bufferedReader);
        if (rootElement == null || !rootElement.isJsonObject()) return;
        var root = rootElement.getAsJsonObject();
        JsonArray dimensionsArray = root.getAsJsonArray("Dimensions");
        if (dimensionsArray == null) return;
        this.chunks = new HashMap<>();
        dimensionsArray.forEach(dimElement -> {
            JsonObject dimensionObj = dimElement.getAsJsonObject();
            String dimensionName = dimensionObj.get("Dimension").getAsString();
            HashMap<String, ChunkInfo> chunkMap = new HashMap<>();
            JsonArray chunkInfoArray = dimensionObj.getAsJsonArray("ChunkInfo");
            if (chunkInfoArray != null) {
                chunkInfoArray.forEach(chunkElement -> {
                    JsonObject chunkObj = chunkElement.getAsJsonObject();
                    ChunkInfo chunkInfo = new ChunkInfo(
                            UUID.fromString(chunkObj.get("UUID").getAsString()),
                            chunkObj.get("ChunkX").getAsInt(),
                            chunkObj.get("ChunkY").getAsInt()
                    );
                    if (chunkObj.has("CreatedTracker")) {
                        JsonObject trackerObj = chunkObj.getAsJsonObject("CreatedTracker");
                        chunkInfo.setCreatedTracked(new ModifiedTracking(
                                UUID.fromString(trackerObj.get("UserUUID").getAsString()),
                                trackerObj.get("UserName").getAsString(),
                                trackerObj.get("Date").getAsString()
                        ));
                    }
                    chunkMap.put(ChunkInfo.formatCoordinates(chunkInfo.getChunkX(), chunkInfo.getChunkZ()), chunkInfo);
                });
            }
            this.chunks.put(dimensionName, chunkMap);
        });
    }

    @Override
    protected void write(BufferedWriter bufferedWriter) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray dimensionsArray = new JsonArray();
        this.chunks.forEach((dimensionName, chunkMap) -> {
            JsonObject dimensionObj = new JsonObject();
            dimensionObj.addProperty("Dimension", dimensionName);
            JsonArray chunkInfoArray = new JsonArray();
            chunkMap.values().forEach(chunkInfo -> {
                JsonObject chunkObj = new JsonObject();
                chunkObj.addProperty("UUID", chunkInfo.getPartyOwner().toString());
                chunkObj.addProperty("ChunkX", chunkInfo.getChunkX());
                chunkObj.addProperty("ChunkY", chunkInfo.getChunkZ());
                if (chunkInfo.getCreatedTracked() != null) {
                    JsonObject trackerObj = new JsonObject();
                    trackerObj.addProperty("UserUUID", chunkInfo.getCreatedTracked().getUserUUID().toString());
                    trackerObj.addProperty("UserName", chunkInfo.getCreatedTracked().getUserName());
                    trackerObj.addProperty("Date", chunkInfo.getCreatedTracked().getDate());
                    chunkObj.add("CreatedTracker", trackerObj);
                }
                chunkInfoArray.add(chunkObj);
            });
            dimensionObj.add("ChunkInfo", chunkInfoArray);
            dimensionsArray.add(dimensionObj);
        });
        root.add("Dimensions", dimensionsArray);
        bufferedWriter.write(root.toString());
    }

    @Override
    protected void create(BufferedWriter bufferedWriter) throws IOException {
        this.chunks = new HashMap<>();
        write(bufferedWriter);
    }

    public HashMap<String, HashMap<String, ChunkInfo>> getChunks() {
        return chunks;
    }
}
