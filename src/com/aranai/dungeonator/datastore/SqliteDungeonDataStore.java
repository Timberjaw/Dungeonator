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

import org.bukkit.util.BlockVector;

import com.aranai.dungeonator.Direction;
import com.aranai.dungeonator.DungeonDataManager;
import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomSet;
import com.aranai.dungeonator.dungeonchunk.DungeonRoomType;
import com.aranai.dungeonator.dungeonchunk.DungeonWidget;

/**
 * SQLite implementation of the DungeonDataStore interface
 */
public class SqliteDungeonDataStore implements IDungeonDataStore {
	
	// The dungeonator instance
	private Dungeonator plugin;
	
	/** The database connection. */
	protected Connection conn;
	
	/** The database path. */
	public final static String rawDb = Dungeonator.BaseFolderPath + "dungeonator.db";
	public final static String db = "jdbc:sqlite:" + rawDb;
	
	/*
	 * Table names
	 */
	
	// Active elements
	private static String TblChunks = "active_chunks";
	private static String TblRooms = "active_rooms";
	private static String TblRoomSets = "active_room_sets";
	private static String TblRoomReservations = "active_room_reservations";
	
	// The Library
	private static String TblLibraryRooms = "library_rooms";
	private static String TblLibraryRoomSets = "library_room_sets";
	private static String TblLibraryWidgets = "library_widgets";
	
	/*
	 * Column mappings
	 */
	
	private static HashMap<Byte,String> ColDoorways = new HashMap<Byte,String>();
	
	/*
	 * SQL Strings for static operations
	 */
	
	// Active Chunks
	private static String SqlCreateTableChunks = "CREATE TABLE `"+TblChunks+"`" +
			"(`world` varchar(32) NOT NULL, `x` INTEGER, `z` INTEGER, `type` INTEGER);";
	
	private static String SqlCreateIndexChunks = "CREATE UNIQUE INDEX chunkIndex on `"+TblChunks+"` (`world`,`x`,`z`);";
	
	// Active Rooms
	private static String SqlCreateTableRooms = "CREATE TABLE `"+TblRooms+"`" +
			"(`world` varchar(32) NOT NULL, `x` INTEGER, `y` INTEGER, `z` INTEGER, `library_id` INTEGER, `set_id` INTEGER, `name` varchar(64));";
	
	private static String SqlCreateIndexRooms = "CREATE UNIQUE INDEX roomIndex on `"+TblRooms+"` (`world`,`x`,`y`,`z`);";
	
	// Active Room Sets
	private static String SqlCreateTableRoomSets = "CREATE TABLE `"+TblRoomSets+"`" +
			"(`world` varchar(32) NOT NULL, `x` INTEGER, `y` INTEGER, `z` INTEGER," +
			"`library_id` INTEGER, `name` varchar(64));";
	
	private static String SqlCreateIndexRoomSets = "CREATE UNIQUE INDEX roomSetIndex on `"+TblRoomSets+"` (`world`,`x`,`y`,`z`);";
	
	// Room Reservations
	private static String SqlCreateTableRoomReservations = "CREATE TABLE `"+TblRoomReservations+"`" +
			"(`world` varchar(32) NOT NULL, `x` INTEGER, `y` INTEGER, `z` INTEGER, `library_id` INTEGER);";
	
	private static String SqlCreateIndexRoomReservations = "CREATE UNIQUE INDEX roomReservationIndex on `"+TblRoomReservations+"` (`world`,`x`,`y`,`z`);";
	
	// Library Rooms
	private static String SqlCreateTableLibraryRooms = "CREATE TABLE `"+TblLibraryRooms+"`" +
			"(`id` INTEGER PRIMARY KEY, `set_id` INTEGER, `filename` varchar(64), `name` varchar(64)," +
			"`door_n` BIT, `door_nne` BIT, `door_ene` BIT, `door_e` BIT, `door_ese` BIT, `door_sse` BIT, " +
			"`door_s` BIT, `door_ssw` BIT, `door_wsw` BIT, `door_w` BIT, `door_wnw` BIT, `door_nnw` BIT, " +
			"`door_u` BIT, `door_d` BIT, `theme_default` varchar(16), `themes` varchar(300));";
	
	// Library Room Sets
	private static String SqlCreateTableLibraryRoomSets = "CREATE TABLE `"+TblLibraryRoomSets+"`"+
			"(`id` INTEGER PRIMARY KEY, `filename` varchar(64), `title` varchar(64),"+
			"`size_x` INTEGER, `size_y` INTEGER, `size_z` INTEGER);";
	
	// Library Widgets
	private static String SqlCreateTableLibraryWidgets = "CREATE TABLE `"+TblLibraryWidgets+"`" +
			"(`id` INTEGER PRIMARY KEY, `filename` varchar(64), `size_class` INTEGER," +
			"`bound_x` INTEGER, `bound_y` INTEGER, `bound_z` INTEGER," + 
			"`origin_x` INTEGER, `origin_y` INTEGER, `origin_z` INTEGER);";
	
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
			
			// Initialize database
			Connection connDisk = DriverManager.getConnection(db);
	        this.initTables(connDisk);
	        connDisk.close();
			
			conn = DriverManager.getConnection("jdbc:sqlite::memory:");
			
			this.initTables(conn);
		} catch (ClassNotFoundException e) {
			Dungeonator.GetLogger().severe("Could not load database class!");
			e.printStackTrace();
		} catch (SQLException e) {
			Dungeonator.GetLogger().severe("Could not open database connection!");
			e.printStackTrace();
		}
		
		// Load from disk
		loadFromDisk();
		
		Dungeonator.GetLogger().info("DungeonDataStore(Sqlite) Initialized.");
	}
	
	/**
	 * Open a connection to the database and copy it into RAM. This results
	 * in better performance for queries but also means that any changes
	 * will not be persistent.
	 */
	private void loadFromDisk() {
	        try {
                Statement stmt = conn.createStatement();
                Statement stmt2 = conn.createStatement();
                ResultSet rs = null;
                
                // Set autocommit to avoid transaction issues while attaching/detaching
                conn.setAutoCommit(true);
            
                // Attach on-disk database to in-memory database
                String attachStmt = "ATTACH '" + rawDb + "' AS src";
                stmt.execute(attachStmt);

                // Copy data from on-disk database to in-memory database
                String tableNameQuery = "SELECT name FROM main.sqlite_master WHERE type='table'";
                rs = stmt.executeQuery(tableNameQuery);
                while(rs.next()) {
                        String sql = "INSERT INTO " + rs.getString(1) + " SELECT * FROM src." + rs.getString(1);
                        stmt2.execute(sql);
                }
                
                // Detach on-disk database
                String detachStmt = "DETACH src";
                stmt.execute(detachStmt);
                
                conn.setAutoCommit(false);
	        } catch (Exception e) {
	                e.printStackTrace();
	        }
	}
	
	public void saveToDisk()
	{
        try {
            Statement stmt = conn.createStatement();
            Statement stmt2 = conn.createStatement();
            ResultSet rs = null;
            
            // Set autocommit to avoid transaction issues while attaching/detaching
            conn.setAutoCommit(true);
        
            // Attach on-disk database to in-memory database
            String attachStmt = "ATTACH '" + rawDb + "' AS src";
            stmt.execute(attachStmt);

            // Copy data from on-disk database to in-memory database
            String tableNameQuery = "SELECT name FROM main.sqlite_master WHERE type='table'";
            rs = stmt.executeQuery(tableNameQuery);
            while(rs.next()) {
                    String sql = "DELETE FROM src." + rs.getString(1) + "; INSERT INTO src." + rs.getString(1) + " SELECT * FROM " + rs.getString(1);
                    stmt2.execute(sql);
            }
            
            // Detach on-disk database
            String detachStmt = "DETACH src";
            stmt.execute(detachStmt);
            
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            Dungeonator.GetLogger().severe("Could not save to disk.");
            e.printStackTrace();
        }
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
			    // Save to disk
			    this.saveToDisk();
			    
				conn.close();
			} catch (SQLException e) {
				Dungeonator.GetLogger().severe("Could not close database connection!");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Initializes the data store tables.
	 */
	private void initTables(Connection conn)
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
            	Dungeonator.GetLogger().info("[Dungeonator]: Active chunks table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableChunks);
                st.execute(SqlCreateIndexChunks);
                conn.commit();
                
                Dungeonator.GetLogger().info("[Dungeonator]: Active chunks table created.");
            }
			
			// Active Room Table
            rs = dbm.getTables(null, null, TblRooms, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.GetLogger().info("[Dungeonator]: Active rooms table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableRooms);
                st.execute(SqlCreateIndexRooms);
                conn.commit();
                
                Dungeonator.GetLogger().info("[Dungeonator]: Active rooms table created.");
            }
			
			// Library Room Table
            rs = dbm.getTables(null, null, TblLibraryRooms, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.GetLogger().info("[Dungeonator]: Library rooms table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableLibraryRooms);
                conn.commit();
                
                Dungeonator.GetLogger().info("[Dungeonator]: Library rooms table created.");
            }
            
            // Room Set Table
            rs = dbm.getTables(null, null, TblRoomSets, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.GetLogger().info("[Dungeonator]: Room sets table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableRoomSets);
                st.execute(SqlCreateIndexRoomSets);
                conn.commit();
                
                Dungeonator.GetLogger().info("[Dungeonator]: Room sets table created.");
            }
            
            // Library Room Set Table
            rs = dbm.getTables(null, null, TblLibraryRoomSets, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.GetLogger().info("[Dungeonator]: Library room sets table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableLibraryRoomSets);
                conn.commit();
                
                Dungeonator.GetLogger().info("[Dungeonator]: Library room sets table created.");
            }
            
            // Room Reservations Table
            rs = dbm.getTables(null, null, TblRoomReservations, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.GetLogger().info("[Dungeonator]: Room reservations table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableRoomReservations);
                st.execute(SqlCreateIndexRoomReservations);
                conn.commit();
                
                Dungeonator.GetLogger().info("[Dungeonator]: Room reservations table created.");
            }
            
            // Library Widget Table
            rs = dbm.getTables(null, null, TblLibraryWidgets, null);
            if (!rs.next())
            {
            	// Create table
            	Dungeonator.GetLogger().info("[Dungeonator]: Library widgets table not found, creating.");
            	
            	conn.setAutoCommit(false);
                st = conn.createStatement();
                st.execute(SqlCreateTableLibraryWidgets);
                conn.commit();
                
                Dungeonator.GetLogger().info("[Dungeonator]: Library widgets table created.");
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
        catch(Exception e) { Dungeonator.GetLogger().warning("[Dungeonator] " + e.getMessage()); }
        
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
        	ps = conn.prepareStatement("SELECT `"+TblRooms+"`.*,`"+TblLibraryRooms+"`.*," +
        			" `"+TblRooms+"`.`name` AS 'roomname',`"+TblLibraryRoomSets+"`.`filename` AS 'set_path',`"+TblLibraryRooms+"`.`set_id` AS 'setid'" +
        			" FROM `"+TblRooms+"`" +
        			" LEFT JOIN `"+TblLibraryRooms+"` ON(`"+TblRooms+"`.`library_id`=`"+TblLibraryRooms+"`.`id`)" +
        			" LEFT JOIN `"+TblLibraryRoomSets+"` ON(`"+TblLibraryRoomSets+"`.`id`=`"+TblLibraryRooms+"`.`set_id`)" +
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
            	dr[i].setLibraryRoomSetID(rs.getLong("setid"));
            	dr[i].setLibraryRoomSetPath(rs.getString("set_path"));
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
        catch(Exception e) { Dungeonator.GetLogger().warning("[Dungeonator] SQLiteDungeonDataStore#getChunkRooms: " + e.getMessage()); e.printStackTrace(); }
        
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
        catch(Exception e) { Dungeonator.GetLogger().warning("[Dungeonator] SQLiteDungeonDataStore#getRoom: " + e.getMessage()); e.printStackTrace(); }
        
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
	        		"(`id`,`set_id`,`filename`,`name`,`door_n`,`door_nne`,`door_ene`,`door_e`,`door_ese`," +
	        		"`door_sse`,`door_s`,`door_ssw`,`door_wsw`,`door_w`,`door_wnw`,`door_nnw`," +
	        		"`door_u`,`door_d`, `theme_default`, `themes`)" +
	        		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	        
	        // Column # counter
	        int col = 1;
	        
	        // Handle library id
	        long libraryId = room.getLibraryId();
	        if(libraryId > 0) { ps.setLong(col++, libraryId); } else { ps.setNull(col++, java.sql.Types.INTEGER); }
	        
	        // Handle set id
	        DungeonRoomSet roomSet = room.getRoomSet();
	        long setID = -1;
	        if(roomSet != null) { setID = roomSet.getLibraryID(); }
	        
	        ps.setLong(col++, setID);
	        ps.setString(col++, room.getFilename());
	        ps.setString(col++, room.getName());
	        ps.setBoolean(col++, room.hasDoorway(Direction.N));
	        ps.setBoolean(col++, room.hasDoorway(Direction.NNE));
	        ps.setBoolean(col++, room.hasDoorway(Direction.ENE));
	        ps.setBoolean(col++, room.hasDoorway(Direction.E));
	        ps.setBoolean(col++, room.hasDoorway(Direction.ESE));
	        ps.setBoolean(col++, room.hasDoorway(Direction.SSE));
	        ps.setBoolean(col++, room.hasDoorway(Direction.S));
	        ps.setBoolean(col++, room.hasDoorway(Direction.SSW));
	        ps.setBoolean(col++, room.hasDoorway(Direction.WSW));
	        ps.setBoolean(col++, room.hasDoorway(Direction.W));
	        ps.setBoolean(col++, room.hasDoorway(Direction.WNW));
	        ps.setBoolean(col++, room.hasDoorway(Direction.NNW));
	        ps.setBoolean(col++, room.hasDoorway(Direction.UP));
	        ps.setBoolean(col++, room.hasDoorway(Direction.DOWN));
	        ps.setString(col++, room.getDefaultTheme());
	        ps.setString(col++, room.getThemeCSV());
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
		
		// Exclude set rooms
		queryDoors.append("WHERE `set_id`=-1");
		
		// Append doorway conditions
		if(doorways != null && doorways.size() > 0)
		{
			queryDoors.append(" AND ");
			
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
	        	
	        	Dungeonator.GetLogger().info("Random Room: No rows with matching doorways with query:\n"+query);
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
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getRoomSet(java.lang.String, int, int, int)
	 */
	@Override
	public DungeonRoomSet getRoomSet(String world, int x, int y, int z) throws DataStoreGetException {
    	PreparedStatement ps = null;
        ResultSet rs = null;
        
        DungeonRoomSet drc = null;
		
		// Get from database
		try
        {
        	ps = conn.prepareStatement("SELECT * FROM `"+TblRoomSets+"`" +
        			" LEFT JOIN `"+TblLibraryRoomSets+"` ON(`"+TblRoomSets+"`.`library_id`=`"+TblLibraryRoomSets+"`.`id`)" +
        			" WHERE `world` = ? AND `x` = ? AND `y` = ? AND `z` = ?");
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            rs = ps.executeQuery();
             
            while (rs.next())
            {
            	// Successfully retrieved the room
            	drc = new DungeonRoomSet(
        			rs.getString("title"), rs.getString("filename"),
        			rs.getInt("x"), rs.getInt("y"), rs.getInt("z"),
        			rs.getInt("size_x"), rs.getInt("size_y"), rs.getInt("size_z"),
        			rs.getInt("library_id") 
            	);
            }
        }
        catch(Exception e) { Dungeonator.GetLogger().warning("[Dungeonator] SQLiteDungeonDataStore#getRoomSet: " + e.getMessage()); e.printStackTrace(); }
        
		return drc;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveRoomSet(com.aranai.dungeonator.dungeonchunk.DungeonRoomSet)
	 */
	@Override
	public boolean saveRoomSet(DungeonRoomSet set) throws DataStoreSaveException {
		Statement st = null;
        
        try
        {
	    	conn.setAutoCommit(false);
	    	st = conn.createStatement();
	        
	        st.execute("INSERT INTO `"+TblRoomSets+"` (`world`,`x`,`y`,`z`,`library_id`,`name`) VALUES (" +
	        		"'"+set.getOriginChunk().getWorldName()+"'," +
	        		"'"+set.getOriginX()+"'," +
	        		"'"+set.getOriginY()+"'," +
	        		"'"+set.getOriginZ()+"'," +
	        		"'"+set.getLibraryID()+"'," +
	        		"'"+set.getTitle()+"');");
	        
	        conn.commit();
        }
        catch(SQLException e) { e.printStackTrace(); return false; }
        catch(Exception e) { e.printStackTrace(); return false; }
        
		return true;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveLibraryRoomSet(com.aranai.dungeonator.dungeonchunk.DungeonRoomSet)
	 */
	@Override
	public boolean saveLibraryRoomSet(DungeonRoomSet set) throws DataStoreSaveException {
		boolean success = false;
        
        try
        {
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `"+TblLibraryRoomSets+"`" +
	        		"(`id`,`filename`,`title`,`size_x`,`size_y`,`size_z`)" +
	        		"VALUES (?, ?, ?, ?, ?, ?);");
	        
	        // Handle library id
	        long libraryID = set.getLibraryID();
	        if(libraryID > 0) { ps.setLong(1, libraryID); } else { ps.setNull(1, java.sql.Types.INTEGER); }
	        
	        ps.setString(2, set.getName());
	        ps.setString(3, set.getTitle());
	        ps.setInt(4, set.getSizeX());
	        ps.setInt(5, set.getSizeY());
	        ps.setInt(6, set.getSizeZ());
	        
	        ps.execute();
	        conn.commit();
	        
	        if(libraryID <= 0)
	        {
	        	// Set the library ID for the room set
	        	libraryID = ps.getGeneratedKeys().getLong(1);
	        	set.setLibraryID(libraryID);
	        }
	        
	        if(libraryID <= 0)
	        {
	        	Dungeonator.GetLogger().severe("Saved RoomSet but got no insert ID.");
	        }
	        else
	        {
	        	Dungeonator.GetLogger().info("Saved RoomSet and got ID "+libraryID);
	        }
	        
	        success = true;
        }
        catch(Exception e) { e.printStackTrace(); }
        
		return success;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getLibraryWidget(long)
	 */
	@Override
	public DungeonWidget getLibraryWidget(long id) throws DataStoreGetException {
		PreparedStatement ps = null;
        ResultSet rs = null;
        boolean loaded = false;
        
        DungeonWidget widget = null;
        
		// Get from database
		try
        {
        	ps = conn.prepareStatement("SELECT * FROM `"+TblLibraryWidgets+"` WHERE `id` = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
             
            while (rs.next()) {
            	// Get the size class
            	DungeonWidget.Size size = DungeonWidget.Size.GetByCode(rs.getInt("size_class"));
            	
            	// Get the origin
            	BlockVector origin = new BlockVector(rs.getInt("origin_x"), rs.getInt("origin_y"), rs.getInt("origin_z"));
            	
            	widget = new DungeonWidget(id, rs.getString("filename"), size, origin);
            	loaded = true;
            }
        }
        catch(Exception e) { Dungeonator.GetLogger().warning("[Dungeonator] " + e.getMessage()); }
        
        if(!loaded)
        {
        	return null;
        }
        
		return widget;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getRandomLibraryWidget(com.aranai.dungeonator.dungeonchunk.DungeonWidget.Size)
	 */
	public DungeonWidget getRandomLibraryWidget(DungeonWidget.Size size) throws DataStoreGetException
	{
		ResultSet rs = null;
		String query = "SELECT * FROM `"+TblLibraryWidgets+"` WHERE `size_class`='"+size.code()+"' ORDER BY RANDOM() LIMIT 1;";
		String filename = "";
		long libraryID = -1;
		BlockVector origin = null;
		
		// Get random record
		try
        {
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement(query);
	        rs = ps.executeQuery();
	        
	        int counter = 0;
	        
	        while (rs.next())
            {
            	// Successfully retrieved the widget
            	filename = rs.getString("filename");
            	libraryID = rs.getLong("id");
            	origin = new BlockVector(rs.getInt("origin_x"), rs.getInt("origin_y"), rs.getInt("origin_z"));
            	
            	counter++;
            }
	        
	        if(counter == 0)
	        {
	        	Dungeonator.GetLogger().info("Random Widget: No rows with matching size with query:\n"+query);
	        }
	        
	        if(filename.equals("")) { return null; }
	        
	        // Initialize DungeonWidget
	        DungeonWidget widget = new DungeonWidget(libraryID, filename, size, origin);
	        
	        conn.commit();
	        
	        return widget;
        }
        catch(Exception e) { e.printStackTrace(); }
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveLibraryWidget(com.aranai.dungeonator.dungeonchunk.DungeonWidget)
	 */
	@Override
	public boolean saveLibraryWidget(DungeonWidget widget) throws DataStoreSaveException {
		boolean success = false;
        
        try
        {
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `"+TblLibraryWidgets+"`" +
	        		"(`id`,`filename`,`size_class`,`origin_x`,`origin_y`,`origin_z`)" +
	        		"VALUES (?, ?, ?, ?, ?, ?);");
	        
	        // Handle library id
	        long libraryId = widget.getLibraryID();
	        if(libraryId > 0) { ps.setLong(1, libraryId); } else { ps.setNull(1, java.sql.Types.INTEGER); }
	        
	        // Set filename
	        ps.setString(2, widget.getFilename());
	        
	        // Set size class
	        ps.setInt(3, widget.getSize().code());
	        
	        // Set origin
	        BlockVector origin = widget.getOrigin();
	        ps.setInt(4, origin.getBlockX());
	        ps.setInt(5, origin.getBlockY());
	        ps.setInt(6, origin.getBlockZ());
	        
	        ps.execute();
	        conn.commit();
	        
	        success = true;
        }
        catch(Exception e) { e.printStackTrace(); }
        
		return success;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getReservedRooms(int, int, int, int, int, int)
	 */
	@Override
	public DungeonRoom[][][] getReservedRooms(String world, int x1, int y1, int z1, int x2, int y2, int z2) throws DataStoreGetException {
		PreparedStatement ps;
		ResultSet rs;
		
		// Initialize array
		DungeonRoom[][][] rooms = new DungeonRoom[(x2-x1)+1][16][(z2-z1)+1];
		
		// Get query results
		try
        {
        	ps = conn.prepareStatement(
        			"SELECT `"+TblRoomReservations+"`.*, `"+TblLibraryRooms+"`.*," +
        			" `"+TblLibraryRooms+"`.filename AS 'roomfile',`"+TblLibraryRoomSets+"`.filename AS 'setfolder'" +
        			" FROM `"+TblRoomReservations+"`" +
        			" LEFT JOIN `"+TblLibraryRooms+"` ON(`"+TblRoomReservations+"`.`library_id`=`"+TblLibraryRooms+"`.`id`)" +
        			" LEFT JOIN `"+TblLibraryRoomSets+"` ON(`"+TblLibraryRoomSets+"`.`id`=`set_id`)" +
        			" WHERE `world` = ? AND `x` >= ? AND `y` >= ? AND `z` >= ? AND `x` <= ? AND `y` <= ? AND `z` <= ?");
            ps.setString(1, world);
            ps.setInt(2, x1);
            ps.setInt(3, y1);
            ps.setInt(4, z1);
            ps.setInt(5, x2);
            ps.setInt(6, y2);
            ps.setInt(7, z2);
            rs = ps.executeQuery();
            
            while (rs.next())
            {
            	// Successfully retrieved the room
            	int realX = rs.getInt("x");
            	int realZ = rs.getInt("z");
            	int x = realX - x1;
            	int y = rs.getInt("y");
            	int z = realZ - z1;
            	
            	rooms[x][y][z] = new DungeonRoom(new DungeonChunk(null, DungeonRoomType.BASIC_TILE, realX, realZ), y);
            	rooms[x][y][z].setLoaded(true);
            	rooms[x][y][z].setLibraryId(rs.getLong("library_id"));
            	rooms[x][y][z].setName(rs.getString("name"));
            	rooms[x][y][z].setFilename(rs.getString("roomfile"));
            	
            	// This is a bit hackish: it would be nice to have a better way to track that this is a set room
            	rooms[x][y][z].setRoomSet(new DungeonRoomSet(rs.getString("setfolder")));
            	
            	// Set doorways
            	for(byte d : Direction.directionValues.values())
            	{
            		String s = ColDoorways.get(d);
            		if(s != null)
            		{
            			rooms[x][y][z].setDoorway(d, rs.getBoolean(s));
            		}
            	}
            }
        }
        catch(Exception e) { Dungeonator.GetLogger().warning("[Dungeonator] SQLiteDungeonDataStore#getReservedRooms: " + e.getMessage()); e.printStackTrace(); }
		
		return rooms;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveReservedRoom(java.lang.String, int, int, int, long)
	 */
	@Override
	public boolean saveReservedRoom(String world, int x, int y, int z, long id) throws DataStoreSaveException {
		boolean success = false;
        
        try
        {
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `"+TblRoomReservations+"`" +
	        		"(`world`,`x`,`y`,`z`,`library_id`)" +
	        		"VALUES (?, ?, ?, ?, ?);");
	        
	        ps.setString(1, world);
	        ps.setInt(2, x);
	        ps.setInt(3, y);
	        ps.setInt(4, z);
	        ps.setLong(5, id);
	        ps.execute();
	        conn.commit();
	        
	        success = true;
        }
        catch(Exception e) { e.printStackTrace(); }
        
		return success;
	}
	
	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#deleteReservedRoom(java.lang.String, int, int, int)
	 */
	@Override
	public boolean deleteReservedRoom(String world, int x, int y, int z) throws DataStoreDeleteException {
		boolean success = false;
        
        try
        {
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("DELETE FROM `"+TblRoomReservations+"`" +
	        		"WHERE `world` = ? AND `x` = ? AND `y` = ? AND `z` = ?;");
	        
	        ps.setString(1, world);
	        ps.setInt(2, x);
	        ps.setInt(3, y);
	        ps.setInt(4, z);
	        ps.execute();
	        conn.commit();
	        
	        success = true;
        }
        catch(Exception e) { e.printStackTrace(); }
        
		return success;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getAllReservedRooms(java.lang.String)
	 */
	@Override
	public Hashtable<String, Long> getAllReservedRooms(String world) throws DataStoreGetException {
		PreparedStatement ps;
		ResultSet rs;
		
		// Initialize hashtable
		Hashtable<String,Long> rooms = new Hashtable<String,Long>();
		
		// Get query results
		try
        {
        	ps = conn.prepareStatement("SELECT * FROM `"+TblRoomReservations+"`" +
        			" LEFT JOIN `"+TblLibraryRooms+"` ON(`"+TblRoomReservations+"`.`library_id`=`"+TblLibraryRooms+"`.`id`)" +
        			" WHERE `world` = ?");
            ps.setString(1, world);
            rs = ps.executeQuery();
            
            while (rs.next())
            {
            	// Successfully retrieved the room
            	String key = DungeonDataManager.GetReservationKey(rs.getString("world"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z"));
            	rooms.put(key, rs.getLong("library_id"));
            }
        }
        catch(Exception e) { Dungeonator.GetLogger().warning("[Dungeonator] SQLiteDungeonDataStore#getReservedRooms: " + e.getMessage()); e.printStackTrace(); }
		
		return rooms;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#getLibraryRoomSetsRandom(int)
	 */
	@Override
	public Vector<DungeonRoomSet> getLibraryRoomSetsRandom(int number) throws DataStoreGetException {
		// Get sets
		// Get room lists
		
		ResultSet rs = null;
		Vector<DungeonRoomSet> rooms = new Vector<DungeonRoomSet>();
		
		String query = "SELECT * FROM `"+TblLibraryRoomSets+"` ORDER BY RANDOM() LIMIT "+number+";";
		
		// Get random record
		try
        {
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement(query);
	        rs = ps.executeQuery();
	        
	        int counter = 0;
	        
	        while (rs.next())
            {
            	// Successfully retrieved the room set
            	String filename = rs.getString("filename");
            	String title = rs.getString("title");
            	long libraryID = rs.getLong("id");
            	int sizeX = rs.getInt("size_x");
            	int sizeY = rs.getInt("size_y");
            	int sizeZ = rs.getInt("size_z");
            	
            	rooms.add(new DungeonRoomSet(filename, title, sizeX, sizeY, sizeZ, 0, 0, 0, libraryID));
            	
            	counter++;
            }
	        
	        if(counter == 0)
	        {
	        	Dungeonator.GetLogger().info("Random Room Set: No Room Sets Available");
	        	conn.commit();
	        	return null;
	        }
	        
	        conn.commit();
	        
	        // Get room list for DungeonRoomSet
	        for(DungeonRoomSet s : rooms)
	        {
	        	String roomQuery = "SELECT * FROM `"+TblLibraryRooms+"` WHERE `set_id`="+s.getLibraryID()+";";
	        	
	        	ps = conn.prepareStatement(roomQuery);
	        	rs = ps.executeQuery();
	        	
	        	while(rs.next())
	        	{
	        		String[] coords = rs.getString("filename").split("\\.");
	        		System.out.println(rs.getString("filename"));
	        		s.setLibraryRoomID(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), rs.getLong("id"));
	        	}
	        	
	        	conn.commit();
	        }
	        
	        return rooms;
        }
        catch(Exception e) { e.printStackTrace(); }
		
		return null;
	}

}
