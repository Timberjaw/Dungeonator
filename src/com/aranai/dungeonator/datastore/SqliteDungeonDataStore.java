package com.aranai.dungeonator.datastore;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import com.aranai.dungeonator.Direction;
import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomType;

/**
 * SQLite implementation of the DungeonDataStore interface
 */
public class SqliteDungeonDataStore implements IDungeonDataStore {
	
	// The dungeonator instance
	private Dungeonator plugin;
	
	/** The database connection. */
	protected Connection conn;
	
	/** The database path. */
	public final static String db = "jdbc:sqlite:" + Dungeonator.BaseFolderPath + "dungeonator.db";
	
	/*
	 * Table names
	 */
	
	private static String TblChunks = "active_chunks";
	private static String TblRooms = "active_rooms";
	private static String TblLibraryRooms = "library_rooms";
	
	/*
	 * Column mappings
	 */
	
	private static HashMap<Byte,String> ColDoorways = new HashMap<Byte,String>();
	
	/*
	 * SQL Strings
	 */
	
	private static String SqlCreateTableChunks = "CREATE TABLE `"+TblChunks+"`" +
			"(`world` varchar(32) NOT NULL, `x` INTEGER, `z` INTEGER, `type` INTEGER);";
	
	private static String SqlCreateIndexChunks = "CREATE UNIQUE INDEX chunkIndex on `"+TblChunks+"` (`world`,`x`,`z`);";
	
	private static String SqlCreateTableRooms = "CREATE TABLE `"+TblRooms+"`" +
			"(`world` varchar(32) NOT NULL, `x` INTEGER, `y` INTEGER, `z` INTEGER, `library_id` INTEGER, `name` varchar(64));";
	
	private static String SqlCreateIndexRooms = "CREATE UNIQUE INDEX roomIndex on `"+TblRooms+"` (`world`,`x`,`y`,`z`);";
	
	private static String SqlCreateTableLibraryRooms = "CREATE TABLE `"+TblLibraryRooms+"`" +
			"(`id` INTEGER PRIMARY KEY, `filename` varchar(64), `name` varchar(64)," +
			"`door_n` BIT, `door_nne` BIT, `door_ene` BIT, `door_e` BIT, `door_ese` BIT, `door_sse` BIT, " +
			"`door_s` BIT, `door_ssw` BIT, `door_wsw` BIT, `door_w` BIT, `door_wnw` BIT, `door_nnw` BIT, " +
			"`door_u` BIT, `door_d` BIT);";
	
	/*
	 * Static initializer
	 */
	static
	{
		// Initialize doorway column names
		ColDoorways.put(Direction.N, "door_n");
		ColDoorways.put(Direction.N, "door_nne");
		ColDoorways.put(Direction.N, "door_ene");
		ColDoorways.put(Direction.N, "door_e");
		ColDoorways.put(Direction.N, "door_ese");
		ColDoorways.put(Direction.N, "door_sse");
		ColDoorways.put(Direction.N, "door_s");
		ColDoorways.put(Direction.N, "door_ssw");
		ColDoorways.put(Direction.N, "door_wsw");
		ColDoorways.put(Direction.N, "door_w");
		ColDoorways.put(Direction.N, "door_wnw");
		ColDoorways.put(Direction.N, "door_nnw");
		ColDoorways.put(Direction.UP, "door_u");
		ColDoorways.put(Direction.DOWN, "door_d");
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#initialize()
	 */
	@Override
	public void initialize(Dungeonator plugin)
	{
		// Save dungeonator instance
		this.plugin = plugin;
		
		// Initialize database
		this.initTables();
		
		// Set columns
		
		
		Dungeonator.getLogger().info("DungeonDataStore(Sqlite) Initialized.");
	}
	
	/**
	 * Initializes the data store tables.
	 */
	private void initTables()
	{
		ResultSet rs = null;
    	Statement st = null;
    	
    	try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	
        	DatabaseMetaData dbm = conn.getMetaData();
        	
			// Active Chunk Table
            rs = dbm.getTables(null, null, TblChunks, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.getLogger().info("[Dungeonator]: Active chunks table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableChunks);
                st.execute(SqlCreateIndexChunks);
                conn.commit();
                
                Dungeonator.getLogger().info("[Dungeonator]: Active chunks table created.");
            }
			
			// Active Room Table
            rs = dbm.getTables(null, null, TblRooms, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.getLogger().info("[Dungeonator]: Active rooms table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableRooms);
                st.execute(SqlCreateIndexRooms);
                conn.commit();
                
                Dungeonator.getLogger().info("[Dungeonator]: Active rooms table created.");
            }
			
			// Library Room Table
            rs = dbm.getTables(null, null, TblLibraryRooms, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.getLogger().info("[Dungeonator]: Library rooms table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableLibraryRooms);
                conn.commit();
                
                Dungeonator.getLogger().info("[Dungeonator]: Library rooms table created.");
            }
        }
    	catch(SQLException e)
        {
        	// ERROR
        	System.out.println("[Dungeonator] (initTables) DB ERROR - " + e.getMessage() + " | SQLState: " + e.getSQLState() + " | Error Code: " + e.getErrorCode());
        }
        catch(Exception e)
        {
        	// Error
        	System.out.println("Error: " + e.getMessage());
        	e.printStackTrace();
        }
        finally
        {
        	if(conn != null)
        	{
        		try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
        	}
        }
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getChunk(java.lang.String)
	 */
	@Override
	public DungeonChunk getChunk(String world, int x, int z) throws DataStoreGetException {
		Connection conn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        boolean loaded = false;
        
        DungeonChunk dc = new DungeonChunk(null, DungeonRoomType.BASIC_TILE, x, z);
        dc.setWorld(plugin.getServer().getWorld(world));
		
		// Get from database
		try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	ps = conn.prepareStatement("SELECT * FROM `"+TblChunks+"` WHERE `world` = ? AND `x` = ? AND `z` = ?");
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, z);
            rs = ps.executeQuery();
             
            while (rs.next()) {
            	loaded = true;
            }
        }
        catch(Exception e) { Dungeonator.getLogger().warning("[Dungeonator] " + e.getMessage()); }
        finally { if(conn != null) { try { conn.close(); } catch(Exception e) { e.printStackTrace(); } } }
        
        if(!loaded)
        {
        	return null;
        }
        
		return dc;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveChunk(com.aranai.dungeonator.dungeonchunk.DungeonChunk)
	 */
	@Override
	public boolean saveChunk(DungeonChunk chunk) throws DataStoreSaveException {
		boolean success = false;
        
        try
        {
	    	Class.forName("org.sqlite.JDBC");
	    	Connection conn = DriverManager.getConnection(db);
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `"+TblChunks+"`" +
	        		"(`world`,`x`,`z`,`type`)" +
	        		"VALUES (?, ?, ?, ?);");
	        
	        ps.setString(1, chunk.getWorldName());
	        ps.setInt(2, chunk.getX());
	        ps.setInt(3, chunk.getZ());
	        ps.setInt(4, 0);
	        ps.execute();
	        conn.commit();
	        conn.close();
	        
	        success = true;
        }
        catch(Exception e) { e.printStackTrace(); }
        
		return success;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#deleteChunk(com.aranai.dungeonator.dungeonchunk.DungeonChunk)
	 */
	@Override
	public void deleteChunk(DungeonChunk chunk) throws DataStoreDeleteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getRoom(java.lang.String, int, int, int)
	 */
	@Override
	public DungeonRoom getRoom(String world, int x, int y, int z) throws DataStoreGetException {
		Connection conn = null;
    	PreparedStatement ps = null;
        ResultSet rs = null;
        
        DungeonChunk dc = null;
        if(plugin.getChunkManager().isChunkGenerated(world, x, z))
        {
        	dc = this.getChunk(world, x, z);
        	//Dungeonator.getLogger().info("Getting "+world+","+x+","+z+" from data store.");
        }
        else
        {
        	dc = new DungeonChunk(null, DungeonRoomType.BASIC_TILE, x, z);
        	dc.setWorld(plugin.getServer().getWorld(world));
        	//Dungeonator.getLogger().info("Getting "+world+","+x+","+z+" from void.");
        }
        DungeonRoom dr = new DungeonRoom(dc, y);
		
		// Get from database
		try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	ps = conn.prepareStatement("SELECT *,`"+TblRooms+"`.`name` AS 'roomname' FROM `"+TblRooms+"`" +
        			" LEFT JOIN `"+TblLibraryRooms+"` ON(`"+TblRooms+"`.`library_id`=`"+TblLibraryRooms+"`.`id`)" +
        			" WHERE `world` = ? AND `x` = ? AND `y` = ? AND `z` = ?");
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            rs = ps.executeQuery();
             
            while (rs.next())
            {
            	// Successfully retrieved the room
            	dr.setLoaded(true);
            	dr.setLibraryId(rs.getLong("library_id"));
            	dr.setName(rs.getString("roomname"));
            	
            	// Set doorways
            	for(byte d : Direction.directionValues.values())
            	{
            		String s = ColDoorways.get(d);
            		if(s != null)
            		{
            			dr.setDoorway(d, rs.getBoolean(s));
            		}
            	}
            }
        }
        catch(Exception e) { Dungeonator.getLogger().warning("[Dungeonator] SQLiteDungeonDataStore#getRoom: " + e.getMessage()); e.printStackTrace(); }
        finally { if(conn != null) { try { conn.close(); } catch(Exception e) { e.printStackTrace(); } } }
        
		return dr;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveRoom(com.aranai.dungeonator.dungeonchunk.DungeonRoom)
	 */
	@Override
	public boolean saveRoom(DungeonRoom room) throws DataStoreSaveException {
		boolean success = false;
        
        try
        {
	    	Class.forName("org.sqlite.JDBC");
	    	Connection conn = DriverManager.getConnection(db);
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("INSERT INTO `"+TblRooms+"`" +
	        		"(`world`,`x`,`y`,`z`,`library_id`,`name`)" +
	        		"VALUES (?, ?, ?, ?, ?, ?);");
	        
	        ps.setString(1, room.getDungeonChunk().getWorldName());
	        ps.setInt(2, room.getX());
	        ps.setInt(3, room.getY());
	        ps.setInt(4, room.getZ());
	        ps.setLong(5, room.getLibraryId());
	        ps.setString(6, room.getName());
	        success = ps.execute();
	        
	        conn.commit();
	        conn.close();
        }
        catch(SQLException e) { Dungeonator.getLogger().warning(room.toString()); e.printStackTrace(); }
        catch(Exception e) { e.printStackTrace(); }
        finally { if(conn != null) { try { conn.close(); } catch(Exception e) { e.printStackTrace(); } } }
        
		return success;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#deleteRoom(java.lang.String, int, int, int)
	 */
	@Override
	public void deleteRoom(String world, int x, int y, int z) throws DataStoreDeleteException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getLibraryRoom(java.lang.String)
	 */
	@Override
	public DungeonRoom getLibraryRoom(long id) throws DataStoreGetException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveLibraryRoom(com.aranai.dungeonator.dungeonchunk.DungeonRoom)
	 */
	@Override
	public boolean saveLibraryRoom(DungeonRoom room) throws DataStoreSaveException {
		boolean success = false;
        
        try
        {
	    	Class.forName("org.sqlite.JDBC");
	    	Connection conn = DriverManager.getConnection(db);
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `"+TblLibraryRooms+"`" +
	        		"(`id`,`filename`,`name`,`door_n`,`door_nne`,`door_ene`,`door_e`,`door_ese`," +
	        		"`door_sse`,`door_s`,`door_ssw`,`door_wsw`,`door_w`,`door_wnw`,`door_nnw`," +
	        		"`door_u`,`door_d`)" +
	        		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	        
	        // Handle library id
	        long libraryId = room.getLibraryId();
	        if(libraryId > 0) { ps.setLong(1, libraryId); } else { ps.setNull(1, java.sql.Types.INTEGER); }
	        
	        ps.setString(2, room.getFilename());
	        ps.setString(3, room.getName());
	        ps.setBoolean(4, room.hasDoorway(Direction.N));
	        ps.setBoolean(5, room.hasDoorway(Direction.NNE));
	        ps.setBoolean(6, room.hasDoorway(Direction.ENE));
	        ps.setBoolean(7, room.hasDoorway(Direction.E));
	        ps.setBoolean(8, room.hasDoorway(Direction.ESE));
	        ps.setBoolean(9, room.hasDoorway(Direction.SSE));
	        ps.setBoolean(10, room.hasDoorway(Direction.S));
	        ps.setBoolean(11, room.hasDoorway(Direction.SSW));
	        ps.setBoolean(12, room.hasDoorway(Direction.WSW));
	        ps.setBoolean(13, room.hasDoorway(Direction.W));
	        ps.setBoolean(14, room.hasDoorway(Direction.WNW));
	        ps.setBoolean(15, room.hasDoorway(Direction.NNW));
	        ps.setBoolean(16, room.hasDoorway(Direction.UP));
	        ps.setBoolean(15, room.hasDoorway(Direction.DOWN));
	        ps.execute();
	        conn.commit();
	        conn.close();
	        
	        success = true;
        }
        catch(Exception e) { e.printStackTrace(); }
        
		return success;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#deleteLibraryRoom(java.lang.String)
	 */
	@Override
	public void deleteLibraryRoom(String hash) throws DataStoreDeleteException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getLibraryRoomRandom()
	 */
	@Override
	public DungeonRoom getLibraryRoomRandom(Vector<Byte> doorways) throws DataStoreGetException {
		ResultSet rs = null;
		String filename = "";
		long libraryId = 0;
		DungeonRoom room = new DungeonRoom();
		
		// Build query string
		StringBuffer queryDoors = new StringBuffer();
		if(doorways != null && doorways.size() > 0)
		{
			for(int i = 0; i < doorways.size(); i++)
			{
				queryDoors.append("`"+ColDoorways.get(doorways.get(i))+"`='1'");
				if(i < doorways.size()-1)
				{
					queryDoors.append(" AND ");
				}
			}
		}
		String query = "SELECT * FROM `"+TblLibraryRooms+"` "+queryDoors+" ORDER BY RANDOM() LIMIT 1;";
		
		
		// Get random record
		try
        {
	    	Class.forName("org.sqlite.JDBC");
	    	Connection conn = DriverManager.getConnection(db);
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement(query);
	        rs = ps.executeQuery();
	        
	        int counter = 0;
	        
	        while (rs.next())
            {
            	// Successfully retrieved the room
            	filename = rs.getString("filename");
            	libraryId = rs.getLong("id");
            	
            	counter++;
            }
	        
	        if(counter == 0)
	        {
	        	if(doorways == null)
	        	{
	        		// Unrecoverable
	        		return null;
	        	}
	        	
	        	Dungeonator.getLogger().info("Random Room: No rows with matching doorways with query:\n"+query);
	        	conn.commit();
	        	conn.close();
	        	return getLibraryRoomRandom(null);
	        }
	        
	        conn.commit();
	        conn.close();
	        
	        if(filename.equals("")) { return null; }
	        
	        // Initialize DungeonRoom
	        room.setFilename(filename);
	        room.setLibraryId(libraryId);
	        
	        return room;
        }
        catch(Exception e) { e.printStackTrace(); }
		
		return null;
	}

}
