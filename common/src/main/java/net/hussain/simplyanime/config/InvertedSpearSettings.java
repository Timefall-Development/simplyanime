package net.hussain.simplyanime.config;

import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedDouble;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;

public class InvertedSpearSettings extends ConfigSection {

    public float damageModifier = 3.0f;
    public float attackSpeed = -1.8f;

    // Heavenly Slash Field (sneak + right click)
    @ValidatedInt.Restrict(min = 0)
    public int fieldCooldown = 240;
    @ValidatedInt.Restrict(min = 0)
    public int fieldWindup = 5;
    @ValidatedInt.Restrict(min = 1)
    public int fieldSpinDuration = 50;
    @ValidatedInt.Restrict(min = 1)
    public int fieldHitFrequency = 5;
    @ValidatedDouble.Restrict(min = 1.0)
    public double fieldRadius = 4.5;
    @ValidatedFloat.Restrict(min = 0f)
    public float fieldDamageScaling = 0.22f;
    @ValidatedFloat.Restrict(min = 0f)
    public float fieldSpellScaling = 1.0f;
    @ValidatedDouble.Restrict(min = 1.0)
    public double lockOnRange = 9.0;
    @ValidatedInt.Restrict(min = 1)
    public int flingDuration = 5;
    @ValidatedFloat.Restrict(min = 0f)
    public float payoffDamageScaling = 1.1f;
    @ValidatedFloat.Restrict(min = 0f)
    public float payoffSpellScaling = 5.0f;
    @ValidatedInt.Restrict(min = 1)
    public int retractDuration = 8;
    @ValidatedFloat.Restrict(min = 0f)
    public float retractDamageScaling = 0.78f;
    @ValidatedFloat.Restrict(min = 0f)
    public float retractSpellScaling = 3.5f;

    // Twin Heaven Strike (right click)
    @ValidatedInt.Restrict(min = 0)
    public int strikeCooldown = 160;
    @ValidatedInt.Restrict(min = 1)
    public int dashWindup = 5;
    @ValidatedDouble.Restrict(min = 1.0)
    public double dashDistance = 6.0;
    @ValidatedInt.Restrict(min = 1)
    public int dashDuration = 5;
    @ValidatedDouble.Restrict(min = 0.5)
    public double strikeRadius = 2.5;
    @ValidatedFloat.Restrict(min = 0f)
    public float firstStrikeDamageScaling = 1.33f;
    @ValidatedFloat.Restrict(min = 0f)
    public float firstStrikeSpellScaling = 6.1f;
    @ValidatedDouble.Restrict(min = 0.0)
    public double firstStrikeLaunch = 0.45;
    @ValidatedInt.Restrict(min = 1)
    public int secondStrikeDelay = 7;
    @ValidatedFloat.Restrict(min = 0f)
    public float secondStrikeDamageScaling = 1.33f;
    @ValidatedFloat.Restrict(min = 0f)
    public float secondStrikeSpellScaling = 6.1f;
    @ValidatedInt.Restrict(min = 0)
    public int recoveryDuration = 10;
}
