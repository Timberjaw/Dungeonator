package com.aranai.dungeonator.dungeonchunk;

/**
 * Represents a rectangular solid volume of rooms
 */
public class DungeonRoomSet {

	// The position of the set origin, if this set is actively loaded, measured in rooms
	private int originX = 0;	// This will be a chunk X coordinate
	private int originY = 0;	// This will be a room coordinate within a chunk
	private int originZ = 0;	// This will be a chunk Z coordinate
	
	// The dimensions of the set (measured in rooms)
	private int sizeX = 1;
	private int sizeY = 1;
	private int sizeZ = 1;
	
	// The printed title of the room set (optional) 
	private String title = "Unnamed Room Set";
	
	// The name (and thus the folder name) of the set
	private String name = "";
	
	// The loaded status: true if the set represents a loaded set
	private boolean isLoaded = false;
	
	// The active id; 0 if unloaded
	private long activeID = -1;
	
	// The library id; 0 if unloaded
	private long libraryID = -1;
	
	// The library room ids
	private long[][][] libraryRoomsID;
	
	// The origin chunk, if loaded
	private DungeonChunk originChunk = null;
	
	/**
	 * Instantiates a new dungeon room set.
	 *
	 * @param name the name
	 * @param title the title
	 * @param sizeX the size x
	 * @param sizeY the size y
	 * @param sizeZ the size z
	 * @param originX the origin x
	 * @param originY the origin y
	 * @param originZ the origin z
	 * @param libraryID the library id
	 */
	public DungeonRoomSet(String name, String title, int sizeX, int sizeY, int sizeZ, int originX, int originY, int originZ, long libraryID)
	{
		this.setName(name);
		this.setTitle(title);
		this.setSizeX(sizeX);
		this.setSizeY(sizeY);
		this.setSizeZ(sizeZ);
		this.setOriginX(originX);
		this.setOriginY(originY);
		this.setOriginZ(originZ);
		this.setLibraryID(libraryID);
		this.resetLibraryRoomIDs();
	}
	
	/**
	 * Instantiates a new dungeon room set.
	 *
	 * @param name the name
	 * @param title the title
	 * @param sizeX the size x
	 * @param sizeY the size y
	 * @param sizeZ the size z
	 * @param originX the origin x
	 * @param originY the origin y
	 * @param originZ the origin z
	 */
	public DungeonRoomSet(String name, String title, int sizeX, int sizeY, int sizeZ, int originX, int originY, int originZ)
	{
		this(name, title, sizeX, sizeY, sizeZ, originX, originY, originZ, 0);
	}
	
	/**
	 * Instantiates a new dungeon room set.
	 *
	 * @param name the name
	 * @param title the title
	 * @param sizeX the size x
	 * @param sizeY the size y
	 * @param sizeZ the size z
	 */
	public DungeonRoomSet(String name, String title, int sizeX, int sizeY, int sizeZ)
	{
		this(name, title, sizeX, sizeY, sizeZ, 0, 0, 0);
	}
	
	/**
	 * Instantiates a new dungeon room set.
	 *
	 * @param name the name
	 * @param title the title
	 */
	public DungeonRoomSet(String name, String title)
	{
		this(name, title, 1, 1, 1);
	}
	
	/**
	 * Instantiates a new dungeon room set.
	 *
	 * @param name the name
	 */
	public DungeonRoomSet(String name)
	{
		this(name, name);
	}
	
	/**
	 * Reset library room ids.
	 */
	private void resetLibraryRoomIDs()
	{
		libraryRoomsID = new long[sizeX][sizeY][sizeZ];
	}

	/**
	 * @return the loaded status
	 */
	public boolean isLoaded() {
		return isLoaded;
	}

	/**
	 * @param isLoaded the loaded status
	 */
	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	/**
	 * @return the libraryID
	 */
	public long getLibraryID() {
		return libraryID;
	}

	/**
	 * @param libraryID the libraryID to set
	 */
	public void setLibraryID(long libraryID) {
		this.libraryID = libraryID;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the originX
	 */
	public int getOriginX() {
		return originX;
	}

	/**
	 * @param originX the originX to set
	 */
	public void setOriginX(int originX) {
		this.originX = originX;
	}

	/**
	 * @return the originY
	 */
	public int getOriginY() {
		return originY;
	}

	/**
	 * @param originY the originY to set
	 */
	public void setOriginY(int originY) {
		this.originY = originY;
	}

	/**
	 * @return the originZ
	 */
	public int getOriginZ() {
		return originZ;
	}

	/**
	 * @param originZ the originZ to set
	 */
	public void setOriginZ(int originZ) {
		this.originZ = originZ;
	}

	/**
	 * @return the sizeX
	 */
	public int getSizeX() {
		return sizeX;
	}

	/**
	 * @param sizeX the sizeX to set
	 */
	public void setSizeX(int sizeX) {
		this.sizeX = sizeX;
	}

	/**
	 * @return the sizeY
	 */
	public int getSizeY() {
		return sizeY;
	}

	/**
	 * @param sizeY the sizeY to set
	 */
	public void setSizeY(int sizeY) {
		this.sizeY = sizeY;
	}

	/**
	 * @return the sizeZ
	 */
	public int getSizeZ() {
		return sizeZ;
	}

	/**
	 * @param sizeZ the sizeZ to set
	 */
	public void setSizeZ(int sizeZ) {
		this.sizeZ = sizeZ;
	}

	/**
	 * @return the originChunk
	 */
	public DungeonChunk getOriginChunk() {
		return originChunk;
	}

	/**
	 * @param originChunk the originChunk to set
	 */
	public void setOriginChunk(DungeonChunk originChunk) {
		this.originChunk = originChunk;
	}

	/**
	 * @return the activeID
	 */
	public long getActiveID() {
		return activeID;
	}

	/**
	 * @param activeID the activeID to set
	 */
	public void setActiveID(long activeID) {
		this.activeID = activeID;
	}
	
	/**
	 * Gets the library room id.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the library room id
	 */
	public long getLibraryRoomID(int x, int y, int z)
	{
		return libraryRoomsID[x][y][z];
	}
	
	/**
	 * Sets the library room id.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param id the id
	 */
	public void setLibraryRoomID(int x, int y, int z, long id)
	{
		libraryRoomsID[x][y][z] = id;
	}
	
	public String toString()
	{
		return "{DungeonRoom<"+libraryID+":"+sizeX+","+sizeY+","+sizeZ+">}";
	}
}
