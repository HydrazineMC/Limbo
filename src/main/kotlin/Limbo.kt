package net.horizonsend.limbo

import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.math.max
import kotlin.math.min
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.minestom.server.MinecraftServer.getBiomeManager
import net.minestom.server.MinecraftServer.getDimensionTypeManager
import net.minestom.server.MinecraftServer.getGlobalEventHandler
import net.minestom.server.MinecraftServer.getInstanceManager
import net.minestom.server.MinecraftServer.init
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode.ADVENTURE
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.extras.velocity.VelocityProxy.enable
import net.minestom.server.instance.Chunk.CHUNK_SIZE_X
import net.minestom.server.instance.Chunk.CHUNK_SIZE_Z
import net.minestom.server.instance.DynamicChunk
import net.minestom.server.instance.block.Block.BARRIER
import net.minestom.server.utils.NamespaceID.from
import net.minestom.server.world.DimensionType
import net.minestom.server.world.biomes.Biome
import net.minestom.server.world.biomes.BiomeEffects
import net.minestom.server.world.biomes.BiomeParticle
import net.minestom.server.world.biomes.BiomeParticle.NormalOption

fun main() {
	// Init Server
	val server = init()

	// Create the dimension
	val dimension = DimensionType.builder(from("minecraft:space"))
		.effects("minecraft:the_end")
		.minY(0)
		.height(16)
		.logicalHeight(16)
		.build()

	// Register the dimension
	getDimensionTypeManager().addDimension(dimension)

	// Create the biome
	val biome = Biome.builder()
		.name(from("minecraft:space"))
		.effects(
			BiomeEffects.builder()
				.biomeParticle(BiomeParticle(1f, NormalOption(from("minecraft:underwater"))))
				.build()
		)
		.build()

	// Register the biome
	getBiomeManager().addBiome(biome)

	// Create the instance
	val instance = getInstanceManager().createInstanceContainer(dimension)

	// Chunk supplier
	instance.setChunkSupplier { _, chunkX, chunkZ ->
		val chunk = DynamicChunk(instance, chunkX, chunkZ)

		for (x in 0 ..CHUNK_SIZE_X) for (z in 0 ..CHUNK_SIZE_Z) {
			chunk.setBlock(x, 0, z, BARRIER)

			for (y in 0 .. 15) chunk.setBiome(x, y, z, biome)
		}

		chunk
	}

	// Init Velocity Support
	val secretPath = Path("velocitySecret")
	if (!secretPath.exists()) secretPath.createFile()

	val secret = secretPath.readText()

	enable(secret) // Enable Velocity

	// Handle Player Spawns
	getGlobalEventHandler().addListener(PlayerLoginEvent::class.java) {
		it.player.gameMode = ADVENTURE

		it.setSpawningInstance(instance)
		it.player.respawnPoint = Pos(0.0, 1.0, 0.0)
	}

	// Welcome the player
	getGlobalEventHandler().addListener(PlayerSpawnEvent::class.java) {
		it.player.sendMessage(miniMessage().deserialize("<aqua><red><b>Welcome to Limbo!</b></red>\nAs you're here, the server is restarting, or something broke.\n<grey><i>How am I meant to know? I'm just a pre-written message.</i></grey>\nAnyway, you can switch to another server with <white>\"/server\"</white>.\nHowever you're probably looking for <blue><u><click:run_command:/server creative>Creative</click></u></blue>, so just click the button."))
	}

	// World wrap around
	getGlobalEventHandler().addListener(PlayerMoveEvent::class.java) {
		var newX = it.newPosition.x
		var newZ = it.newPosition.z

		var flipYaw = false

		if (-32 > newX || newX > 32) {
			newX = -min(max(-32.0, newX), 32.0)
			flipYaw = true
		}

		if (-32 > newZ || newZ > 32) {
			newZ = -min(max(-32.0, newZ), 32.0)
			flipYaw = true
		}

		val newYaw = if (flipYaw) -it.newPosition.yaw else it.newPosition.yaw

		if (newX != it.newPosition.x || newZ != it.newPosition.z || newYaw != it.newPosition.yaw)
			it.newPosition = Pos(newX, it.newPosition.y, newZ, newYaw, it.newPosition.pitch)
	}

	server.start("0.0.0.0", 10000)
}