package com.aranai.dungeonator.dungeonchunk;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

/**
 * An in-room attachment point for a widget.
 */
public class DungeonWidgetNode {
	
	/** The widget size class allowed for this node. */
	private DungeonWidget.Size size;
	
	/** The position of the node. The widget origin will be placed here. */
	private Vector position;
	
	public static enum AttachmentFace {
		UP (BlockFace.UP), DOWN (BlockFace.DOWN), NORTH (BlockFace.NORTH), EAST (BlockFace.EAST), SOUTH (BlockFace.SOUTH), WEST (BlockFace.WEST);
		private BlockFace blockFace;
		
		private AttachmentFace(BlockFace face)
		{
			this.setBlockFace(face);
		}
		
		public static AttachmentFace GetFaceByName(String name)
		{
			name = name.toLowerCase();
			
			if(name.equals("up")) { return AttachmentFace.UP; }
			else if(name.equals("down")) { return AttachmentFace.DOWN; }
			else if(name.equals("north")) { return AttachmentFace.NORTH; }
			else if(name.equals("east")) { return AttachmentFace.EAST; }
			else if(name.equals("south")) { return AttachmentFace.SOUTH; }
			else if(name.equals("west")) { return AttachmentFace.WEST; }
			
			return null;
		}

		/**
		 * @return the blockFace
		 */
		public BlockFace getBlockFace() {
			return blockFace;
		}

		/**
		 * @param blockFace the blockFace to set
		 */
		public void setBlockFace(BlockFace blockFace) {
			this.blockFace = blockFace;
		}
	};
	
	/** The attachment face for this node */
	private AttachmentFace attachmentFace;
	
	/** The node id within the room. */
	private int nodeID;
	
	public DungeonWidgetNode(DungeonWidget.Size size, Vector pos, AttachmentFace face) { this(size, pos, face, 0); }
	
	public DungeonWidgetNode(DungeonWidget.Size size, Vector pos, AttachmentFace face, int id)
	{
		this.setSize(size);
		this.setPosition(pos);
		this.setNodeID(id);
		this.attachmentFace = face;
	}

	/**
	 * @return the size
	 */
	public DungeonWidget.Size getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(DungeonWidget.Size size) {
		this.size = size;
	}

	/**
	 * @return the position
	 */
	public Vector getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Vector position) {
		this.position = position;
	}

	/**
	 * @return the nodeID
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * @param nodeID the nodeID to set
	 */
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}
}
