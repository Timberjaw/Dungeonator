package com.aranai.dungeonator.dungeonchunk;

import org.bukkit.util.Vector;

/**
 * An in-room attachment point for a widget.
 */
public class DungeonWidgetNode {
	
	/** The widget size class allowed for this node. */
	private DungeonWidget.Size size;
	
	/** The position of the node. The widget origin will be placed here. */
	private Vector position;
	
	/** The node id within the room. */
	private int nodeID;
	
	public DungeonWidgetNode(DungeonWidget.Size size, Vector pos, int id)
	{
		this.setSize(size);
		this.setPosition(pos);
		this.setNodeID(id);
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
