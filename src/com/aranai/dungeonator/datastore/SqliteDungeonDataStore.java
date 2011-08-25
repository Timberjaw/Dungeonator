package com.aranai.dungeonator.datastore;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.aranai.dungeonator.Dungeonator;
import com.aranai.dungeonator.dungeonchunk.DungeonChunk;
import com.aranai.dungeonator.dungeonchunk.DungeonRoom;

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
	 * SQL Strings
	 */
	
	private static String SqlCreateTableChunks = "CREATE TABLE `"+TblChunks+"`" +
			"(`world` varchar(32) NOT NULL, `x` REAL, `z` REAL, `type` INTEGER);";
	
	private static String SqlCreateIndexChunks = "CREATE UNIQUE INDEX chunkIndex on `"+TblChunks+"` (`world`,`x`,`z`);";
	
	private static String SqlCreateTableRooms = "CREATE TABLE `"+TblRooms+"`" +
			"(`world` varchar(32) NOT NULL, `x` REAL, `y` INTEGER, `z` REAL, `library_id` INTEGER, `name` varchar(64));";
	
	private static String SqlCreateIndexRooms = "CREATE UNIQUE INDEX roomIndex on `"+TblRooms+"` (`world`,`x`,`y`,`z`);";
	
	private static String SqlCreateTableLibraryRooms = "CREATE TABLE `"+TblLibraryRooms+"`" +
			"(`id` INTEGER PRIMARY KEY, `filename` varchar(64), `name` varchar(64));";
	
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
        
        DungeonChunk dc = new DungeonChunk(null);
		
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
             
            while (rs.next()) { }
        }
        catch(Exception e) { Dungeonator.getLogger().warning("[Dungeonator] " + e.getMessage()); }
        finally { if(conn != null) { try { conn.close(); } catch(Exception e) { e.printStackTrace(); } } }
        
		return dc;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveChunk(com.aranai.dungeonator.dungeonchunk.DungeonChunk)
	 */
	@Override
	public boolean saveChunk(DungeonChunk chunk) throws DataStoreSaveException {
		// TODO Auto-generated method stub
		return false;
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
        
        DungeonRoom dr = new DungeonRoom(plugin.getChunkManager().getChunk(world, x, z), y);
		
		// Get from database
		try
        {
    		Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(db);
        	ps = conn.prepareStatement("SELECT * FROM `"+TblRooms+"` WHERE `world` = ? AND `x` = ? AND `z` = ?");
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, z);
            rs = ps.executeQuery();
             
            while (rs.next())
            {
            	// Successfully retrieved the room
            	dr.setLibraryId(rs.getLong("library_id"));
            	dr.setName(rs.getString("name"));
            }
        }
        catch(Exception e) { Dungeonator.getLogger().warning("[Dungeonator] " + e.getMessage()); }
        finally { if(conn != null) { try { conn.close(); } catch(Exception e) { e.printStackTrace(); } } }
        
		return dr;
	}

	/* (non-Javadoc)
	 * @see com.aranai.dungeonator.datastore.IDungeonDataStore#saveRoom(com.aranai.dungeonator.dungeonchunk.DungeonRoom)
	 */
	@Override
	public boolean saveRoom(DungeonRoom room) throws DataStoreSaveException {
		boolean success = false;
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
	public DungeonRoom getLibraryRoom(String hash) throws DataStoreGetException {
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
	        PreparedStatement ps = conn.prepareStatement("REPLACE INTO `"+TblLibraryRooms+"` (`id`,`filename`,`name`) VALUES (?, ?, ?);");
	        
	        // Handle library id
	        long libraryId = room.getLibraryId();
	        if(libraryId > 0) { ps.setLong(1, libraryId); } else { ps.setNull(1, java.sql.Types.INTEGER); }
	        
	        ps.setString(2, room.getFilename());
	        ps.setString(3, room.getName());
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
	public DungeonRoom getLibraryRoomRandom() throws DataStoreGetException {
		ResultSet rs = null;
		String filename = "";
		DungeonRoom room = new DungeonRoom();
		
		// Get random record
		try
        {
	    	Class.forName("org.sqlite.JDBC");
	    	Connection conn = DriverManager.getConnection(db);
	    	conn.setAutoCommit(false);
	        PreparedStatement ps = conn.prepareStatement("SELECT * FROM `"+TblLibraryRooms+"` ORDER BY RANDOM() LIMIT 1;");
	        rs = ps.executeQuery();
	        
	        while (rs.next())
            {
            	// Successfully retrieved the room
            	filename = rs.getString("filename");
            }
	        
	        conn.commit();
	        conn.close();
	        
	        if(filename.equals("")) { return null; }
	        
	        // Initialize DungeonRoom
	        room.setFilename(filename);
	        
	        return room;
        }
        catch(Exception e) { }
		
		return null;
	}

}
