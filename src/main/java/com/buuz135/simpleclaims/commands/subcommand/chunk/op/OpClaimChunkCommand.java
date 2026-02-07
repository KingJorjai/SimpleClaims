package com.buuz135.simpleclaims.commands.subcommand.chunk.op;

import com.buuz135.simpleclaims.Main;
import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.commands.CommandMessages;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

import static com.hypixel.hytale.server.core.command.commands.player.inventory.InventorySeeCommand.MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD;

public class OpClaimChunkCommand extends AbstractAsyncCommand {

    public OpClaimChunkCommand() {
        super("admin-claim", "Claims the chunk where you are, must have selected a party first using the /scp admin-party-list command");
        this.requirePermission(CommandMessages.ADMIN_PERM + "admin-claim");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        CommandSender sender = commandContext.sender();
        if (sender instanceof Player player) {
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                return CompletableFuture.runAsync(() -> {
                    PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
                    if (playerRef == null) return;
                    var selectedPartyID = ClaimManager.getInstance().getAdminUsageParty().get(playerRef.getUuid());
                    if (selectedPartyID == null) {
                        player.sendMessage(CommandMessages.ADMIN_PARTY_NOT_SELECTED);
                        return;
                    }
                    var party = ClaimManager.getInstance().getPartyById(selectedPartyID);
                    if (party == null) {
                        player.sendMessage(CommandMessages.PARTY_NOT_FOUND);
                        return;
                    }
                    var chunk = ClaimManager.getInstance().getChunkRawCoords(player.getWorld().getName(), (int) playerRef.getTransform().getPosition().getX(), (int) playerRef.getTransform().getPosition().getZ());
                    if (chunk != null) {
                        player.sendMessage(chunk.getPartyOwner().equals(party.getId()) ? CommandMessages.ALREADY_CLAIMED_BY_YOU : CommandMessages.ALREADY_CLAIMED_BY_ANOTHER_PLAYER);
                        return;
                    }
                    
                    int chunkX = ChunkUtil.chunkCoordinate((int) playerRef.getTransform().getPosition().getX());
                    int chunkZ = ChunkUtil.chunkCoordinate((int) playerRef.getTransform().getPosition().getZ());
                    
                    // Check if chunk is reserved by selected party - if so, allow claiming it
                    boolean isOwnReserved = ClaimManager.getInstance().isReservedByOwnParty(player.getWorld().getName(), chunkX, chunkZ, party.getId());
                    
                    // Check if chunk is reserved by another party (only if perimeter reservation is enabled)
                    if (Main.CONFIG.get().isEnablePerimeterReservation() && !isOwnReserved &&
                        ClaimManager.getInstance().isReservedByOtherParty(player.getWorld().getName(), chunkX, chunkZ, party.getId())) {
                        player.sendMessage(CommandMessages.CHUNK_RESERVED_BY_OTHER_PARTY);
                        return;
                    }
                    
                    // Check if claiming this chunk would create a perimeter that overlaps with chunks reserved by other parties
                    // Skip this check if the chunk itself is reserved by the selected party (we can claim our own reserved chunks)
                    if (Main.CONFIG.get().isEnablePerimeterReservation() && !isOwnReserved &&
                        ClaimManager.getInstance().wouldPerimeterOverlapOtherReserved(player.getWorld().getName(), chunkX, chunkZ, party.getId())) {
                        player.sendMessage(CommandMessages.CHUNK_RESERVED_BY_OTHER_PARTY);
                        return;
                    }
                    
                    // Check if party has any claims - if yes, new chunk must be adjacent OR be a reserved chunk by the same party (only if restriction is enabled)
                    if (Main.CONFIG.get().isEnableAdjacentChunkRestriction() && 
                        ClaimManager.getInstance().getAmountOfClaims(party) > 0) {
                        boolean isAdjacent = ClaimManager.getInstance().isAdjacentToPartyClaims(player.getWorld().getName(), chunkX, chunkZ, party.getId());
                        if (!isAdjacent && !isOwnReserved) {
                            player.sendMessage(CommandMessages.CHUNK_NOT_ADJACENT);
                            return;
                        }
                    }
                    
                    if (!ClaimManager.getInstance().hasEnoughClaimsLeft(party)) {
                        player.sendMessage(CommandMessages.NOT_ENOUGH_CHUNKS);
                        return;
                    }
                    var chunkInfo = ClaimManager.getInstance().claimChunkByRawCoords(player.getWorld().getName(), (int) playerRef.getTransform().getPosition().getX(), (int) playerRef.getTransform().getPosition().getZ(), party, player, playerRef);
                    ClaimManager.getInstance().queueMapUpdate(player.getWorld(), chunkInfo.getChunkX(), chunkInfo.getChunkZ());
                    player.sendMessage(CommandMessages.CLAIMED);
                }, world);
            } else {
                commandContext.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
                return CompletableFuture.completedFuture(null);
            }
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
}
