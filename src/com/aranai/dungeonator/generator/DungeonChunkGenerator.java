package com.aranai.dungeonator.generator;

import java.util.Vector;

import com.aranai.dungeonator.Direction;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonChunkDoorway;

/**
 * Handles the down and dirty details of dungeon data generation
 */
public class DungeonChunkGenerator {
	
	/**
	 * Generates chunk data for a new DungeonChunk.
	 *
	 * @param dc the new DungeonChunk to process
	 */
	public void generateChunk(DungeonChunk dc)
	{
		/*
		 * At this point, the DungeonChunk should have correct neighbor information,
		 * a basic chunk type, and a handle to the server's raw chunk (which we are
		 * going to overwrite).
		 */
		
		/*
		 * TODO: Evaluate neighbors to see if the current chunk MUST be a
		 * set chunk (tile set or biome).
		 */
		
		/* Get candidate exit list to narrow exit possibilities.
		 * We can't force a doorway into a previously generated chunk, but we
		 * CAN block a doorway from a previously generated chunk. We can
		 * specify arbitrary doorways for any side that does not yet have a
		 * generated neighbor (in the majority of cases, at least one side
		 * will not yet have a neighbor).
		 */
		
		DungeonChunkDoorway candidateDoorways[] = new DungeonChunkDoorway[12];
		
		/*
		 * There are 3 candidate doorways for each cardinal direction
		 * We can retrieve a list of matching doorways from each of the
		 * neighboring DungeonChunks.
		 */
		
		Vector<DungeonChunkDoorway> tmpDoorways;
		
		// Loop through the cardinal directions and check for adjacent doorways
		for(byte direction : Direction.CARDINALS)
		{		
			if(dc.hasNeighbor(direction))
			{
				/*
				 * Get the doorways for the neighbor on the side opposite the
				 * side the neighbor is on. So for the north neighbor, we want
				 * to get the doorways on the south side of the neighbor.
				 * We do this by getting the direction 6 steps away from the
				 * neighbor direction, then fetching a vector with the active
				 * doorways on that side. 
				 */
				tmpDoorways = dc.getNeighbor(direction).getDoorwaysOnSide(
						Direction.getSideFromDirection(
								Direction.rotate(
										direction,
										Direction.CLOCKWISE,
										6
								)
						)
				);
				
				/*
				 * Add the retrieved doorways to our candidate list
				 */
				for(DungeonChunkDoorway doorway : tmpDoorways)
				{
					candidateDoorways[doorway.getDirection()] = doorway;
				}
			}
		}
		
		/*
		 * Determine what to do next based on the inferred chunk type.
		 */
		
		switch(dc.getType())
		{
			case PROCEDURAL:
			default:
				// Generate a procedural chunk
				this.generateProceduralChunk(dc, candidateDoorways);
			break;
		}
	}
	
	/**
	 * Generates basic procedural chunk data using the specified DungeonChunk
	 * and candidate doorways.
	 *
	 * @param dc the DungeonChunk being processed
	 * @param candidateDoorways the candidate doorways
	 */
	private void generateProceduralChunk(DungeonChunk dc, DungeonChunkDoorway candidateDoorways[])
	{
		/*
		 * At this point, a list of candidate doorways has been generated based
		 * on the neighboring chunk data. We MUST provide a doorway on our side
		 * to at LEAST one of these candidate doorways. We can safely ignore
		 * any other specifics of the neighboring chunks. If they are set
		 * chunks, their set volume does not extend to this chunk.
		 */
		
		/*
		 * TODO: Create a zone graph. This is a 3x3 grid with 12 connectors. We'll
		 * use this to generate a set of candidate paths through the chunk.
		 */
		
		/*
		 * TODO: Generate candidate paths
		 */
		
		/*
		 * TODO: Build base (floor)
		 * Layer 0: Filled cuboid from <0,0,0> to <15,1,15> with material BEDROCK
		 * Layer 1: Filled cuboid from <0,1,0> to <15,2,15> with material COBBLESTONE
		 */
		
		/*
		 * TODO: Build walls
		 * Hollow cuboid from <0,2,0> to <15,6,15> with thickness 1
		 */
		
		/*
		 * TODO: Build ceiling
		 * Filled cuboid from <0,6,0> to <15,7,15>
		 */
		
		/*
		 * TODO: Knock out walls for connectors
		 */
		
		/*
		 * TODO: Knock out walls for doorways
		 */
		
		
	}
}
