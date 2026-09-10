package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor.mode;

import com.drmangotea.tfmg.TFMGRegistries;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

@MethodsReturnNonnullByDefault
public class RotorModeBuilder<T extends RotorMode, P> extends AbstractBuilder<RotorMode, T, P, RotorModeBuilder<T, P>> {
    private final NonNullFunction<RotorMode.Properties, T> factory;
    private NonNullSupplier<RotorMode.Properties> initialProperties = () -> new RotorMode.Properties(ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName()));
    private NonNullFunction<RotorMode.Properties, RotorMode.Properties> propertiesCallback = NonNullUnaryOperator.identity();

    public RotorModeBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<RotorMode.Properties, T> factory) {
        super(owner, parent, name, callback, TFMGRegistries.ROTOR_MODE);
        this.factory = factory;
    }

    public static <T extends RotorMode, P> RotorModeBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<RotorMode.Properties, T> factory) {
        return new RotorModeBuilder<>(owner, parent, name, callback, factory);
    }

    public RotorModeBuilder<T, P> properties(NonNullUnaryOperator<RotorMode.Properties> func) {
        propertiesCallback = propertiesCallback.andThen(func);
        return this;
    }

    public RotorModeBuilder<T, P> initialProperties(NonNullSupplier<RotorMode.Properties> properties) {
        initialProperties = properties;
        return this;
    }

    public RotorModeBuilder<T, P> defaultLang() {
        return lang(RotorMode::getDescriptionId);
    }

    public RotorModeBuilder<T, P> lang(String name) {
        return lang(RotorMode::getDescriptionId, name);
    }

    @Override
    protected T createEntry() {
        return factory.apply(propertiesCallback.apply(this.initialProperties.get()));
    }

    @Override
    protected RegistryEntry<RotorMode, T> createEntryWrapper(DeferredHolder<RotorMode, T> delegate) {
        return new RotorModeEntry<>(getOwner(), delegate);
    }

    public RotorModeEntry<T> register() {
        return (RotorModeEntry<T>) super.register();
    }
}
