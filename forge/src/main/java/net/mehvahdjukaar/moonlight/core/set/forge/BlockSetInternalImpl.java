package net.mehvahdjukaar.moonlight.core.set.forge;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.platform.forge.RegHelperImpl;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.misc.Registrator;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Instruments;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//spaghetti code. Do not touch, it just works
public class BlockSetInternalImpl {

    //maps containing mod ids and block and items runnable. Block one is ready to run, items needs the bus supplied to it
    //they will be run each mod at a time block first then items
    private static final Map<String, Pair<
            List<Runnable>, //block registration function
            List<Consumer<Registrator<Item>>> //item registration function
            >>
            LATE_REGISTRATION_QUEUE = new ConcurrentHashMap<>();

    private static boolean hasFilledBlockSets = false;

    //aaaa
    public static  <T extends BlockType, E> void addDynamicRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<E, T> registrationFunction, Class<T> blockType, Registry<E> registry) {
        if(registry == Registry.BLOCK){
            addDynamicBlockRegistration((BlockSetAPI.BlockTypeRegistryCallback<Block, T>)registrationFunction, blockType);
        }else if(registry == Registry.ITEM){
            addDynamicItemRegistration((BlockSetAPI.BlockTypeRegistryCallback<Item, T>)registrationFunction, blockType);
        }else{
            //other entries
            Consumer<RegisterEvent> eventConsumer = e->{
                if(e.getVanillaRegistry() == registry){
                    registrationFunction.accept(e.getForgeRegistry()::register, BlockSetAPI.getBlockSet(blockType).getValues());
                }
            };
            FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
        }

    }


    public static <T extends BlockType> void addDynamicBlockRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Block, T> registrationFunction, Class<T> blockType) {

        Consumer<RegisterEvent> eventConsumer;

        Pair<List<Runnable>, List<Consumer<Registrator<Item>>>> registrationQueues = getOrAddQueue();

        //if block makes a function that just adds the bus and runnable to the queue whenever reg block is fired
        eventConsumer = e -> {
            if (e.getRegistryKey().equals(ForgeRegistries.BLOCKS.getRegistryKey())) {
                //actual runnable which will registers the blocks
                Runnable lateRegistration = () -> {

                    IForgeRegistry<Block> registry = e.getForgeRegistry();
                    if (registry instanceof ForgeRegistry<?> fr) {
                        boolean frozen = fr.isLocked();
                        fr.unfreeze();
                        registrationFunction.accept(registry::register, BlockSetAPI.getBlockSet(blockType).getValues());
                        if (frozen) fr.freeze();
                    }
                };
                //when this reg block event fires we only add a runnable to the queue
                registrationQueues.getFirst().add(lateRegistration);
            }
        };
        //registering block event to the bus
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(EventPriority.HIGHEST, eventConsumer);
    }

    public static <T extends BlockType> void addDynamicItemRegistration(
            BlockSetAPI.BlockTypeRegistryCallback<Item, T> registrationFunction, Class<T> blockType) {

        Pair<List<Runnable>, List<Consumer<Registrator<Item>>>> registrationQueues = getOrAddQueue();
        //items just get added to the queue. they will already be called with the correct event
        Consumer<Registrator<Item>> itemEvent = e ->
                registrationFunction.accept(e, BlockSetAPI.getBlockSet(blockType).getValues());

        registrationQueues.getSecond().add(itemEvent);
    }

    @NotNull
    private static Pair<List<Runnable>, List<Consumer<Registrator<Item>>>> getOrAddQueue() {
        //this is horrible. worst shit ever
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        return LATE_REGISTRATION_QUEUE.computeIfAbsent(modId, s -> {
            //if absent we register its registration callback
            bus.addListener(EventPriority.HIGHEST, BlockSetInternalImpl::registerLateBlockAndItems);
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        });
    }


    //shittiest code ever lol
    protected static void registerLateBlockAndItems(RegisterEvent event) {
        //fires for items
        if (!event.getRegistryKey().equals(ForgeRegistries.ITEMS.getRegistryKey())) return;
        //when the first registration function is called we find all block types
        if (!hasFilledBlockSets) {
            BlockSetInternal.initializeBlockSets();
            hasFilledBlockSets = true;
        }
        //get the queue corresponding to this certain mod
        String modId = ModLoadingContext.get().getActiveContainer().getModId();
        var registrationQueues = LATE_REGISTRATION_QUEUE.get(modId);

        if (registrationQueues != null) {
            IForgeRegistry<Item> fr = event.getForgeRegistry();
            //register blocks
            var blockQueue = registrationQueues.getFirst();
            blockQueue.forEach(Runnable::run);
            //registers items
            var itemQueue = registrationQueues.getSecond();
            itemQueue.forEach(q -> q.accept(fr::register));
        }
        //clears stuff that's been executed. not really needed but just to be safe its here
        LATE_REGISTRATION_QUEUE.remove(modId);
    }

    public static boolean hasFilledBlockSets() {
        return hasFilledBlockSets;
    }



}
