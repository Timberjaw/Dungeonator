package com.aranai.dungeonator.datastore;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
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
			"`door_u` BIT, `door_d` BIT, `theme_default` varchar(16), `themes` varchar(300));";
	
	/*
	 * Static initializer
	 */
	static
	{
		// Initialize doorway column names
		ColDoorways.put(Direction.N, "door_n");
		ColDoorways.put(Direction.NNE, "door_nne");
		ColDoorways.put(Direction.ENE, "door_ene");
		ColDoorways.put(Direction.E, "door_e");
		ColDoorways.put(Direction.ESE, "door_ese");
		ColDoorways.put(Direction.SSE, "door_sse");
		ColDoorways.put(Direction.S, "door_s");
		ColDoorways.put(Direction.SSW, "door_ssw");
		ColDoorways.put(Direction.WSW, "door_wsw");
		ColDoorways.put(Direction.W, "door_w");
		ColDoorways.put(Direction.WNW, "door_wnw");
		ColDoorways.put(Direction.NNW, "door_nnw");
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
		
		// Open connection
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(db);
		} catch (ClassNotFoundException e) {
			Dungeonator.getLogger().severe("Could not load database class!");
			e.printStackTrace();
		} catch (SQLException e) {
			Dungeonator.getLogger().severe("Could not open database connection!");
			e.printStackTrace();
		}
		
		// Initialize database
		this.initTables();
		
		Dungeonator.getLogger().info("DungeonDataStore(Sqlite) Initialized.");
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#shutdown()
	 */
	@Override
	public void shutdown()
	{
		if(conn != null)
		{
			try {
				conn.close();
			} catch (SQLException e) {
				Dungeonator.getLogger().severe("Could not close database connection!");
				e.printStackTrace();
			}
		}
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
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getChunk(java.lang.String)
	 */
	@Override
	public DungeonChunk getChunk(String world, int x, int z) throws DataStoreGetException {
    	PreparedStatement ps = null;
        ResultSet rs = null;
        boolean loaded = false;
        
        DungeonChunk dc = new DungeonChunk(null, DungeonRoomType.BASIC_TILE, x, z);
        dc.setWorld(plugin.getServer().getWorld(world));
		
		// Get from database
		try
        {
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
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getChunkRooms(java.lang.String, int, int)
	 */
	@Override
	public DungeonRoom[] getChunkRooms(String world, int x, int z) throws DataStoreGetException {
		PreparedStatement ps = null;
        ResultSet rs = null;
        
        DungeonChunk dc = null;
        if(plugin.getChunkManager().isChunkGenerated(world, x, z))
        {
        	dc = this.getChunk(world, x, z);
        }
        else
        {
        	return null;
        }
        
        DungeonRoom[] dr = new DungeonRoom[16];
		
		// Get from database
		try
        {
        	ps = conn.prepareStatement("SELECT *,`"+TblRooms+"`.`name` AS 'roomname' FROM `"+TblRooms+"`" +
        			" LEFT JOIN `"+TblLibraryRooms+"` ON(`"+TblRooms+"`.`library_id`=`"+TblLibraryRooms+"`.`id`)" +
        			" WHERE `world` = ? AND `x` = ? AND `z` = ? LIMIT 16");
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, z);
            rs = ps.executeQuery();
            
            int i = 0;
            while (rs.next())
            {
            	i = rs.getInt("y");
            	
            	// Successfully retrieved the room
            	dr[i] = new DungeonRoom(dc, i);
            	dr[i].setLoaded(true);
            	dr[i].setLibraryId(rs.getLong("library_id"));
            	dr[i].setName(rs.getString("roomname"));
            	dr[i].setFilename(rs.getString("filename"));
            	
            	// Set doorways
            	for(byte d : Direction.directionValues.values())
            	{
            		String s = ColDoorways.get(d);
            		if(s != null)
            		{
            			dr[i].setDoorway(d, rs.getBoolean(s));
            		}
            	}
            }
        }
        catch(Exception e) { Dungeonator.getLogger().warning("[Dungeonator] SQLiteDungeonDataStore#getChunkRooms: " + e.getMessage()); e.printStackTrace(); }
        
		return dr;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getRoom(java.lang.String, int, int, int)
	 */
	@Override
	public DungeonRoom getRoom(String world, int x, int y, int z) throws DataStoreGetException {
    	PreparedStatement ps = null;
        ResultSet rs = null;
        
        DungeonChunk dc = null;
        if(plugin.getChunkManager().isChunkGenerated(world, x, z))
        {
        	dc = this.getChunk(world, x, z);
        }
        else
        {
        	dc = new DungeonChunk(null, DungeonRoomType.BASIC_TILE, x, z);
        	dc.setWorld(plugin.getServer().getWorld(world));
        	
        	// Return a blank new room
        	return new DungeonRoom(dc, y);
        }
        DungeonRoom dr = new DungeonRoom(dc, y);
		
		// Get from database
		try
        {
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
        
		return dr;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveRoom(com.aranai.dungeonator.dungeonchunk.DungeonRoom)
	 */
	@Override
	public boolean saveRoom(DungeonRoom room) throws DataStoreSaveException {
		DungeonRoom[] rooms = {room};
		return saveRooms(rooms);
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveRooms(com.aranai.dungeonator.dungeonchunk.DungeonRoom)
	 */
	@Override
	public boolean saveRooms(DungeonRoom[] rooms) throws DataStoreSaveException {
		Statement st = null;
        
        try
        {
	    	conn.setAutoCommit(false);
	    	st = conn.createStatement();
	        
	    	for(int r = 0; r < rooms.length; r++)
	    	{
		        st.execute("INSERT INTO `"+TblRooms+"` (`world`,`x`,`y`,`z`,`library_id`,`name`) VALUES (" +
		        		"'"+rooms[r].getDungeonChunk().getWorldName()+"'," +
		        		"'"+rooms[r].getX()+"'," +
		        		"'"+rooms[r].getY()+"'," +
		        		"'"+rooms[r].getZ()+"'," +
		        		"'"+rooms[r].getLibraryId()+"'," +
		        		"'"+rooms[r].getName()+"');");
	    	}
	        
	        conn.commit();
        }
        catch(SQLException e) { e.printStackTrace(); return false; }
        catch(Exception e) { e.printStackTrace(); return false; }
        
		return true;
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
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `"+TblLibraryRooms+"`" +
	        		"(`id`,`filename`,`name`,`door_n`,`door_nne`,`door_ene`,`door_e`,`door_ese`," +
	        		"`door_sse`,`door_s`,`door_ssw`,`door_wsw`,`door_w`,`door_wnw`,`door_nnw`," +
	        		"`door_u`,`door_d`, `theme_default`, `themes`)" +
	        		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	        
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
	        ps.setBoolean(17, room.hasDoorway(Direction.DOWN));
	        ps.setString(19, room.getDefaultTheme());
	        ps.setString(20, room.getThemeCSV());
	        ps.execute();
	        conn.commit();
	        
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
		String themes = "DEFAULT";
		String theme_default = "DEFAULT";
		DungeonRoom room = new DungeonRoom();
		
		// Build query string
		StringBuffer queryDoors = new StringBuffer();
		if(doorways != null && doorways.size() > 0)
		{
			queryDoors.append("WHERE ");
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
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement(query);
	        rs = ps.executeQuery();
	        
	        int counter = 0;
	        
	        while (rs.next())
            {
            	// Successfully retrieved the room
            	filename = rs.getString("filename");
            	libraryId = rs.getLong("id");
            	
            	// Get theme info
            	String tmpThemes = rs.getString("themes");
    	        String tmpTheme_default = rs.getString("theme_default");
    	        
    	        if(tmpThemes != null && !tmpThemes.equals(""))
    	        {
    	        	themes = tmpThemes;
    	        }
    	        if(tmpTheme_default != null && !tmpTheme_default.equals(""))
    	        {
    	        	theme_default = tmpTheme_default;
    	        }
            	
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
	        	return getLibraryRoomRandom(null);
	        }
	        
	        conn.commit();
	        
	        if(filename.equals("")) { return null; }
	        
	        // Initialize DungeonRoom
	        room.setFilename(filename);
	        room.setLibraryId(libraryId);
	        
	        // Set theme information
        	room.setThemeCSV(themes);
        	room.setDefaultTheme(theme_default);
	        
	        return room;
        }
        catch(Exception e) { e.printStackTrace(); }
		
		return null;
	}

}
