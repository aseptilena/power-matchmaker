package ca.sqlpower.matchmaker.dao.hibernate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TestingMatchMakerContext;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.WarningListener;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.util.Version;

public class TestingMatchMakerHibernateSession implements MatchMakerHibernateSession {

    private static final Logger logger = Logger.getLogger(TestingMatchMakerHibernateSession.class);
        
    private final ArchitectDataSource dataSource;
    private final SessionFactory hibernateSessionFactory;
    private TestingMatchMakerContext context;
    private final TestingConnection con;
    private SQLDatabase db;
    private List<String> warnings = new ArrayList<String>();
    private TranslateGroupParent tgp = new TranslateGroupParent(this);

	private Session hSession;
    
    /**
     * Creates a new session that is really connected to a datasource.  
     * This session does not create a SQLDatabase
     * 
     * @param dataSource an architect data source describing the connection
     * @throws RuntimeException
     */
    public TestingMatchMakerHibernateSession(ArchitectDataSource dataSource) throws RuntimeException {
        super();
        try {
            this.dataSource = dataSource;
            this.hibernateSessionFactory = HibernateTestUtil.buildHibernateSessionFactory(this.dataSource);
            this.con = DBTestUtil.connectToDatabase(this.dataSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        resetSession();
    }
    
    /**
     * Provides a disconnected SQLDatabase, but is not really connected to a datasource
     */
    public TestingMatchMakerHibernateSession() {
        db = new SQLDatabase();
        con = null;
        dataSource = null;
        hibernateSessionFactory = null;
    }

    public Session openSession() {
        return hSession;
    }

    public void resetSession() {
    	hSession = hibernateSessionFactory.openSession(getConnection());
    }
    /**
     * Enables or disables the connection associated with this session.  This is useful for
     * testing that Hibernate is correctly configured to eagerly fetch the data we expect it to.
     * 
     * @param disabled
     */
    public void setConnectionDisabled(boolean disabled) {
        con.setDisabled(disabled);
    }
    
    public Connection getConnection() {
        return con;
    }


    ///////// Unimplemented MatchMakerHibernateSession methods are below this line //////////
    
    public String createNewUniqueName() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.createNewUniqueName()");
        return null;
    }

    public PlFolder findFolder(String foldername) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.findFolder()");
        return null;
    }

    public String getAppUser() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getAppUser()");
        return null;
    }

    public MatchMakerSessionContext getContext() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getContext()");
        return null;
    }

    public <T extends MatchMakerObject> MatchMakerDAO<T> getDAO(Class<T> businessClass) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getDAO()");
        return null;
    }

    public String getDBUser() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getDBUser()");
        return null;
    }

    public SQLDatabase getDatabase() {
        return db;
    }
    
    public void setDatabase(SQLDatabase db) {
        this.db = db;
    }

    public List<PlFolder> getFolders() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getFolders()");
        return null;
    }

    public Match getMatchByName(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getMatchByName()");
        return null;
    }

    public Date getSessionStartTime() {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.getSessionStartTime()");
        return null;
    }

    public boolean isThisMatchNameAcceptable(Match match, String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.isThisMatchNameAcceptable()");
        return false;
    }

    public boolean isThisMatchNameAcceptable(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.isThisMatchNameAcceptable()");
        return false;
    }

    public long countMatchByName(String name) {
        // TODO Auto-generated method stub
        logger.debug("Stub call: TestingMatchMakerHibernateSession.countMatchByName()");
        return 0;
    }

    /**
     * Prints the message to syserr and appends it to the warnings list.
     * 
     * @see #getWarnings()
     */
    public void handleWarning(String message) {
        System.err.println("TestingMatchMakerSession.handleWarning(): got warning: "+message);
        warnings.add(message);
    }

    /**
     * Returns the real warning list.  Feel free to modify it if you want, but your changes
     * will affect the session's real list of warnings.
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Replaces this session's warning list.  If you set this to null or an unmodifiable
     * list, handleWarning() will stop working.
     */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public void addWarningListener(WarningListener l) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.addWarningListener()");
    }

    public void removeWarningListener(WarningListener l) {
        logger.debug("Stub call: TestingMatchMakerHibernateSession.removeWarningListener()");
    }

    public TranslateGroupParent getTranslations() {
        return tgp;
    }

	public void setContext(TestingMatchMakerContext context) {
		this.context = context;
	}
    
    public Version getPLSchemaVersion() {
        throw new UnsupportedOperationException("Called getPLSchmaVersion on mock object");
    }
}
