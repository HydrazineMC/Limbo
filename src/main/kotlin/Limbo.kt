package net.horizonsend.limbo

import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.readText
import net.kyori.adventure.text.minimessage.MiniMessage.miniMessage
import net.minestom.server.MinecraftServer.getDimensionTypeManager
import net.minestom.server.MinecraftServer.getGlobalEventHandler
import net.minestom.server.MinecraftServer.getInstanceManager
import net.minestom.server.MinecraftServer.getSchedulerManager
import net.minestom.server.MinecraftServer.init
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode.ADVENTURE
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.DamageType.fromPlayer
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.player.PlayerDeathEvent
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerMoveEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.extras.velocity.VelocityProxy.enable
import net.minestom.server.particle.Particle
import net.minestom.server.particle.ParticleCreator.createParticlePacket
import net.minestom.server.timer.TaskSchedule.tick
import net.minestom.server.utils.NamespaceID.from
import net.minestom.server.world.DimensionType.builder

fun main() {
	val server = init()

	val dimension = builder(from("minecraft:the_end"))
		.ambientLight(0f)
		.fixedTime(6000)
		.effects("minecraft:the_end")
		.minY(0)
		.height(16)
		.logicalHeight(16)
		.infiniburn(from("minecraft:infiniburn_end"))
		.build()

	getDimensionTypeManager().addDimension(dimension)

	val instance = getInstanceManager().createInstanceContainer(dimension)

	instance.time = 18000
	instance.timeRate = 0

	val secretPath = Path("velocitySecret")
	if (!secretPath.exists()) secretPath.createFile()

	val secret = secretPath.readText()

	enable(secret) // Enable Velocity

	instance.chunkGenerator = Generator()

	getGlobalEventHandler().addListener(PlayerLoginEvent::class.java) {
		it.player.gameMode = ADVENTURE

		it.setSpawningInstance(instance)
		it.player.respawnPoint = Pos(0.0, 1.0, 0.0)
	}

	getGlobalEventHandler().addListener(PlayerSpawnEvent::class.java) {
		it.player.sendMessage(miniMessage().deserialize("<aqua><red><b>Welcome to Limbo!</b></red>\nAs you're here, the server is restarting, or something broke.\n<grey><i>How am I meant to know? I'm just a pre-written message.</i></grey>\nAnyway, you can switch to another server with <white>\"/server\"</white>.\nHowever you're probably looking for <blue><u><click:run_command:/server creative>Creative</click></u></blue>, so just click the button."))
	}

	getGlobalEventHandler().addListener(PlayerMoveEvent::class.java) {
		var newX = it.newPosition.x
		var newZ = it.newPosition.z

		if (newX > 32) newX = -32.0
		if (newZ > 32) newZ = -32.0

		if (newX < -32) newX = 32.0
		if (newZ < -32) newZ = 32.0

		if (newX != it.newPosition.x || newZ != it.newPosition.z) it.newPosition = Pos(newX, it.newPosition.y, newZ)
	}

	getSchedulerManager().scheduleTask({
		instance.players.forEach {
			it.sendPacket(
				createParticlePacket(
					Particle.UNDERWATER,
					it.position.x,
					it.position.y,
					it.position.z,
					4f,
					4f,
					4f,
					1024,
				)
			)
		}
	}, tick(1), tick(2))

	getGlobalEventHandler().addListener(EntityAttackEvent::class.java) {
		(it.target as Player).damage(fromPlayer(it.entity as Player), 1f)
	}

	getGlobalEventHandler().addListener(PlayerDeathEvent::class.java) {
		it.deathText = null
		it.chatMessage = null
	}

	server.start("0.0.0.0", 10000)
}