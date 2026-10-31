package so.alaz.cleansingpotions.item;

import so.alaz.cleansingpotions.core.CleanseMode;

/**
 * Implemented by every cleansing potion item so the mode and variant can be read back off a stack.
 *
 * <p>Since 0.3.0 the mode lives in the item's identity rather than in a {@code cleanse_mode}
 * component: 26.3 brewing recipes match on item id and cannot read a component, so brewing a
 * Soothing splash potion is only expressible if that is its own item.
 */
public interface CleansingItem {

    CleanseMode cleanseMode();

    MilkVariant variant();
}
