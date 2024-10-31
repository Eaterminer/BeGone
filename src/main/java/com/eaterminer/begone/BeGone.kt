package com.eaterminer.begone

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.ModifyEntries
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.world.World


class Teleporter(settings: Settings?) : Item(settings) {
	override fun use(world: World, playerEntity: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
		// Play teleport sound and create an non-greifing explosion
		playerEntity.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN, 2.5f, 2.0f)
		playerEntity.getStackInHand(hand).count = 0
		world.createExplosion(playerEntity, playerEntity.x, playerEntity.y, playerEntity.z, 2.0f, World.ExplosionSourceType.BLOCK)

		// Make sure player is in the overworld
		val overworld = world.server?.getWorld(World.OVERWORLD)
		if (overworld != null && world != overworld) {
			playerEntity.moveToWorld(overworld)
		}

		// Teleport player
		playerEntity.teleport(world.spawnPos.x.toDouble(), world.spawnPos.y.toDouble(), world.spawnPos.z.toDouble())

		// Add generic status effects
		playerEntity.addStatusEffect(StatusEffectInstance(StatusEffects.WEAKNESS, 500, 3, false, false, false))
		playerEntity.addStatusEffect(StatusEffectInstance(StatusEffects.INVISIBILITY, 400, 1, false, false, false))

		return TypedActionResult.success(playerEntity.getStackInHand(hand))
	}
	override fun appendTooltip(
		itemStack: ItemStack?,
		world: World?,
		tooltip: MutableList<Text?>,
		tooltipContext: TooltipContext?
	) {
		tooltip.add(Text.literal("Single Use").formatted(Formatting.RED).formatted(Formatting.BOLD))
		tooltip.add(Text.literal("Teleports you back to spawn, for use in an emergency"))
	}
}

class BeGone : ModInitializer {
	private val item = Teleporter(FabricItemSettings().maxCount(1).rarity(Rarity.EPIC))
	override fun onInitialize() {
		Registry.register(Registries.ITEM, Identifier("begone", "teleporter"), item)
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
			.register(ModifyEntries { content: FabricItemGroupEntries ->
				content.add(
					item
				)
			})
	}

}
