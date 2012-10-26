package nl.knaw.dans.easy.db;

import nl.knaw.dans.easy.db.exceptions.DbException;

import org.hibernate.HibernateException;
import org.hibernate.classic.Session;

/**
 * This session factory creates a maximum of one session for one thread. The 
 * session can thus easily be reused by reusing this factory for getting the
 * session. This makes it possible to share session objects between objects
 * and methods without having to send the session along. 
 * @author lobo
 *
 */
public class ThreadLocalSessionFactory
{

    private static ThreadLocalSessionFactory INSTANCE;

    private ThreadLocal<SessionInfo> localSessionInfo = new ThreadLocal<SessionInfo>();

    private ThreadLocalSessionFactory()
    {

    }

    public static ThreadLocalSessionFactory instance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new ThreadLocalSessionFactory();
        }
        return INSTANCE;
    }

    /**
     * Get a new or existing Session object. If a session already was created
     * by this factory for this thread this existing session is returned, 
     * otherwise a new session is created and returned.
     * @return a new or existing session object
     */
    public Session openSession() throws HibernateException, DbException
    {
        SessionInfo sessionInfo = localSessionInfo.get();
        if (sessionInfo == null)
        {
            sessionInfo = new SessionInfo();
            localSessionInfo.set(sessionInfo);
        }

        if (sessionInfo.useCount == 0)
        {
            try
            {
                sessionInfo.session = DbUtil.getSessionFactory().openSession();
            }
            finally
            {
                // if an error of some kind occurs do not store the session info
                if (sessionInfo.session == null)
                    localSessionInfo.set(null);
            }
        }

        // increase in the last line of code, because an exception
        // might occur
        sessionInfo.useCount++;
        return sessionInfo.session;
    }

    /**
     * Call this instead of calling Session.close(). 
     */
    public void closeSession()
    {
        SessionInfo sessionInfo = localSessionInfo.get();
        if (sessionInfo == null)
            // this does not necessarily indicate a programmer error
            // as the openSession might have thrown an exception
            return;

        if (sessionInfo.useCount <= 0)
            throw new RuntimeException("closeSession() called once too many. " + "This indicates a programmer error");
        if (sessionInfo.session == null)
            throw new RuntimeException("Session is null at close? " + "This should not happen");

        sessionInfo.useCount--;
        if (sessionInfo.useCount == 0)
        {
            sessionInfo.session.close();
            localSessionInfo.set(null);
        }
    }

    private class SessionInfo
    {
        public Session session = null;
        public int useCount = 0;
    }

}
