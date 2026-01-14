package com.buuz135.simpleclaims.claim.party;

import com.buuz135.simpleclaims.Main;
import com.buuz135.simpleclaims.claim.tracking.ModifiedTracking;

import javax.annotation.Nullable;
import java.util.*;

public class PartyInfo {

    private UUID id;
    private UUID owner;
    private String name;
    private String description;
    private UUID[] members;
    private int color;
    private List<PartyOverride> overrides;
    private ModifiedTracking createdTracked;
    private ModifiedTracking modifiedTracked;

    public PartyInfo(UUID id, UUID owner, String name, String description, UUID[] members, int color) {
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.members = members;
        this.color = color;
        this.overrides = new ArrayList<>();
        this.overrides.add(new PartyOverride(PartyOverrides.CLAIM_CHUNK_AMOUNT, new PartyOverride.PartyOverrideValue("integer", Main.CONFIG.get().getDefaultPartyClaimsAmount())));
        this.overrides.add(new PartyOverride(PartyOverrides.PARTY_PROTECTION_PLACE_BLOCKS, new PartyOverride.PartyOverrideValue("bool", Main.CONFIG.get().isDefaultPartyBlockPlaceEnabled())));
        this.overrides.add(new PartyOverride(PartyOverrides.PARTY_PROTECTION_BREAK_BLOCKS, new PartyOverride.PartyOverrideValue("bool", Main.CONFIG.get().isDefaultPartyBlockBreakEnabled())));
        this.overrides.add(new PartyOverride(PartyOverrides.PARTY_PROTECTION_INTERACT, new PartyOverride.PartyOverrideValue("bool", Main.CONFIG.get().isDefaultPartyBlockInteractEnabled())));
        this.createdTracked = new ModifiedTracking();
        this.modifiedTracked = new ModifiedTracking();
    }

    public PartyInfo() {
        this(UUID.randomUUID(), UUID.randomUUID(), "", "", new UUID[0], 0);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID[] getMembers() {
        return members;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMembers(UUID[] members) {
        this.members = members;
    }

    public boolean isOwner(UUID uuid){
        return this.owner.equals(uuid);
    }

    public boolean isMember(UUID uuid){
        return Arrays.stream(this.members).anyMatch(uuid::equals);
    }

    public boolean isOwnerOrMember(UUID uuid){
        return isOwner(uuid) || isMember(uuid);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public List<PartyOverride> getOverrides() {
        return overrides;
    }

    public void setOverrides(List<PartyOverride> overrides) {
        this.overrides = overrides;
    }

    public void setOverrides(PartyOverride[] overrides) {
        this.overrides = new ArrayList<>();
        this.overrides.addAll(Arrays.asList(overrides));
    }

    public void addMember(UUID uuid){
        this.members = Arrays.copyOf(this.members, this.members.length + 1);
        this.members[this.members.length - 1] = uuid;
    }

    public void removeMember(UUID uuid){
        this.members = Arrays.stream(this.members).filter(member -> !member.equals(uuid)).toArray(UUID[]::new);
    }

    public int getMaxClaimAmount(){
        var override = this.getOverride(PartyOverrides.CLAIM_CHUNK_AMOUNT);
        if (override != null) {
            return (Integer) override.getValue().getTypedValue();
        }
        return Main.CONFIG.get().getDefaultPartyClaimsAmount();
    }

    public boolean isBlockPlaceEnabled(){
        var override = this.getOverride(PartyOverrides.PARTY_PROTECTION_PLACE_BLOCKS);
        if (override != null) {
            return (Boolean) override.getValue().getTypedValue();
        }
        return Main.CONFIG.get().isDefaultPartyBlockPlaceEnabled();
    }

    public boolean isBlockBreakEnabled(){
        var override = this.getOverride(PartyOverrides.PARTY_PROTECTION_BREAK_BLOCKS);
        if (override != null) {
            return (Boolean) override.getValue().getTypedValue();
        }
        return Main.CONFIG.get().isDefaultPartyBlockBreakEnabled();
    }

    public boolean isBlockInteractEnabled(){
        var override = this.getOverride(PartyOverrides.PARTY_PROTECTION_INTERACT);
        if (override != null) {
            return (Boolean) override.getValue().getTypedValue();
        }
        return Main.CONFIG.get().isDefaultPartyBlockInteractEnabled();
    }

    public void setOverride(PartyOverride override){
        this.overrides.removeIf(partyOverride -> partyOverride.getType().equals(override.getType()));
        this.overrides.add(override);
    }

    public @Nullable PartyOverride getOverride(String type){
        return this.overrides.stream().filter(partyOverride -> partyOverride.getType().equals(type)).findFirst().orElse(null);
    }

    public ModifiedTracking getCreatedTracked() {
        return createdTracked;
    }

    public void setCreatedTracked(ModifiedTracking createdTracked) {
        this.createdTracked = createdTracked;
    }

    public ModifiedTracking getModifiedTracked() {
        return modifiedTracked;
    }

    public void setModifiedTracked(ModifiedTracking modifiedTracked) {
        this.modifiedTracked = modifiedTracked;
    }

    @Override
    public String toString() {
        return "PartyInfo{" +
                "id=" + id +
                ", owner=" + owner +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", members=" + Arrays.toString(members) +
                ", color=" + color +
                ", overrides=" + overrides +
                ", createdTracked=" + createdTracked +
                ", modifiedTracked=" + modifiedTracked +
                '}';
    }
}
