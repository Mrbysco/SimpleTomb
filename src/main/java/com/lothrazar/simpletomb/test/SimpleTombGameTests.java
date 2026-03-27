package com.lothrazar.simpletomb.test;

import com.lothrazar.simpletomb.ModTomb;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.function.Consumer;

@EventBusSubscriber(modid = ModTomb.MODID)
public class SimpleTombGameTests {

  private static final Identifier EMPTY_STRUCTURE = Identifier.fromNamespaceAndPath(ModTomb.MODID, "empty");

  @SubscribeEvent
  public static void registerTests(RegisterGameTestsEvent event) {
    Holder<TestEnvironmentDefinition<?>> env = event.registerEnvironment(
        Identifier.fromNamespaceAndPath(ModTomb.MODID, "default"),
        new TestEnvironmentDefinition.AllOf());

    reg(event, "tomb_block_placement", SimpleTombTestFunctions::tombBlockPlacement, env, 100);
    reg(event, "tomb_block_explosion", SimpleTombTestFunctions::tombBlockExplosion, env, 100);
    reg(event, "tomb_block_shape", SimpleTombTestFunctions::tombBlockShape, env, 100);
    reg(event, "block_entity_stores_items", SimpleTombTestFunctions::blockEntityStoresItems, env, 100);
    reg(event, "block_entity_ownership", SimpleTombTestFunctions::blockEntityOwnership, env, 100);
    reg(event, "block_entity_drop_inventory", SimpleTombTestFunctions::blockEntityDropInventory, env, 200);
    reg(event, "grave_activation_owner", SimpleTombTestFunctions::graveActivationOwner, env, 200);
    reg(event, "grave_activation_denied", SimpleTombTestFunctions::graveActivationDenied, env, 200);
    reg(event, "auto_equip_armor", SimpleTombTestFunctions::autoEquipArmor, env, 100);
    reg(event, "auto_equip_shield", SimpleTombTestFunctions::autoEquipShield, env, 100);
    reg(event, "auto_equip_elytra", SimpleTombTestFunctions::autoEquipElytra, env, 100);
    reg(event, "auto_equip_elytra_displace", SimpleTombTestFunctions::autoEquipElytraDisplace, env, 100);
    reg(event, "auto_equip_binding_curse", SimpleTombTestFunctions::autoEquipBindingCurse, env, 100);
    reg(event, "grave_key_position", SimpleTombTestFunctions::graveKeyPosition, env, 100);
    reg(event, "grave_key_removed", SimpleTombTestFunctions::graveKeyRemoved, env, 200);
    reg(event, "grave_key_count", SimpleTombTestFunctions::graveKeyCount, env, 100);
    reg(event, "full_inventory_drops", SimpleTombTestFunctions::fullInventoryDrops, env, 200);
    reg(event, "valid_placement", SimpleTombTestFunctions::validPlacement, env, 100);
  }

  private static void reg(RegisterGameTestsEvent event, String name,
      Consumer<GameTestHelper> function,
      Holder<TestEnvironmentDefinition<?>> environment,
      int timeoutTicks) {
    reg(event, name, function, environment, timeoutTicks, true);
  }

  @SuppressWarnings("unchecked")
  private static void reg(RegisterGameTestsEvent event, String name,
      Consumer<GameTestHelper> function,
      Holder<TestEnvironmentDefinition<?>> environment,
      int timeoutTicks, boolean required) {
    TestData<Holder<TestEnvironmentDefinition<?>>> testData = new TestData<>(
        environment, EMPTY_STRUCTURE, timeoutTicks, 0, required);
    GameTestInstance instance = new DirectGameTestInstance(name, function, (TestData) testData);
    event.registerTest(Identifier.fromNamespaceAndPath(ModTomb.MODID, name), instance);
  }
}
