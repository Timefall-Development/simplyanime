package net.hussain.simplyanime.animation;

import org.jetbrains.annotations.Nullable;

public enum AttackStyle {
    SWORD(new String[]{"blade_attack_1", "blade_attack_2", "blade_attack_3"},
            new String[]{"blade_attack_1", "blade_attack_1", "blade_attack_cross"}),
    AXE(new String[]{"axe_attack_1", "axe_attack_2", "axe_attack_3"}, null);

    private final String[] chain;
    @Nullable
    private final String[] paired;

    AttackStyle(String[] chain, @Nullable String[] paired) {
        this.chain = chain;
        this.paired = paired;
    }

    public int length() {
        return this.chain.length;
    }

    public boolean pairs() {
        return this.paired != null;
    }

    public AnimationClip clip(int step, boolean both) {
        return AnimationClips.get((both && this.paired != null ? this.paired : this.chain)[step]);
    }

    public static AttackStyle of(String weaponType) {
        return switch (weaponType) {
            case "greataxe", "greathammer", "halberd", "scythe" -> AXE;
            default -> SWORD;
        };
    }
}
