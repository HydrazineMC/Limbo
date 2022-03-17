package net.horizonsend.limbo

import net.minestom.server.instance.Chunk.CHUNK_SIZE_X
import net.minestom.server.instance.Chunk.CHUNK_SIZE_Z
import net.minestom.server.instance.ChunkGenerator
import net.minestom.server.instance.ChunkPopulator
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.instance.block.Block.BARRIER

class Generator: ChunkGenerator {
	override fun generateChunkData(batch: ChunkBatch, chunkX: Int, chunkZ: Int) {
		for (x in 0 .. CHUNK_SIZE_X) for (z in 0 .. CHUNK_SIZE_Z) batch.setBlock(x, 0, z, BARRIER)
	}

	override fun getPopulators(): MutableList<ChunkPopulator>? = null
}