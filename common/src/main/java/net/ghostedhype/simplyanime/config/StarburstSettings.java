package net.ghostedhype.simplyanime.config;

import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

// Elucidator in the main hand, Dark Repulser in the other
public class StarburstSettings extends ConfigSection {

    @ValidatedInt.Restrict(min = 0)
    public int cooldown = 1200;
    // how far round the wielder every cut reaches
    @ValidatedDouble.Restrict(min = 2.0, max = 16.0)
    public double reach = 6.5;
    @ValidatedFloat.Restrict(min = 1f, max = 5f)
    public float slashScale = 2.6f;
    @ValidatedFloat.Restrict(min = 0f)
    public float hitDamageScaling = 0.4f;
    @ValidatedFloat.Restrict(min = 0f)
    public float hitSpellScaling = 1.0f;
    @ValidatedDouble.Restrict(min = 3.0, max = 32.0)
    public double crossReach = 11.0;
    @ValidatedDouble.Restrict(min = 2.0, max = 24.0)
    public double burstRadius = 8.0;
    @ValidatedFloat.Restrict(min = 0f)
    public float finalDamageScaling = 3.5f;
    @ValidatedFloat.Restrict(min = 0f)
    public float finalSpellScaling = 9.0f;
    @ValidatedDouble.Restrict(min = 0.0, max = 5.0)
    public double finalKnockback = 1.8;
    @ValidatedFloat.Restrict(min = 0f, max = 1f)
    public float moveSpeed = 0.6f;
    @ValidatedFloat.Restrict(min = 0f, max = 1f)
    public float damageTaken = 0.35f;
    public boolean nameText = true;
    @ValidatedFloat.Restrict(min = 0f, max = 16f)
    public float volume = 2.5f;
}
