package nl.knaw.dans.easy.business.bean;

import nl.knaw.dans.easy.data.Data;
import nl.knaw.dans.easy.data.collections.DmoCollectionsAccessImpl;
import nl.knaw.dans.easy.domain.exceptions.ApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @see http://static.springsource.org/spring/docs/2.5.x/reference/beans.html#context-functionality-events
 * 
 * @author henkb
 */
public class EasyContextListener implements ApplicationListener
{

    private static final Logger logger = LoggerFactory.getLogger(EasyContextListener.class);

    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        logger.info("Application event ====> " + event);
        if (event instanceof ContextRefreshedEvent)
        {
            onContextRefreshed((ContextRefreshedEvent) event);
        }
        else if (event instanceof ContextClosedEvent)
        {
            onContextClosed((ContextClosedEvent) event);
        }
    }

    /**
     * Published when the ApplicationContext is initialized or refreshed, e.g. using the refresh() method
     * on the ConfigurableApplicationContext interface. "Initialized" here means that all beans are
     * loaded, post-processor beans are detected and activated, singletons are pre-instantiated, and the
     * ApplicationContext object is ready for use. A refresh may be triggered multiple times, as long as
     * the context hasn't been closed - provided that the chosen ApplicationContext actually supports
     * such "hot" refreshes (which e.g. XmlWebApplicationContext does but GenericApplicationContext
     * doesn't).
     * 
     * @param event
     */
    private void onContextRefreshed(ContextRefreshedEvent event)
    {
        initializeCollections();

    }

    private void initializeCollections()
    {
        DmoCollectionsAccessImpl easyCollectionsImpl = (DmoCollectionsAccessImpl) Data.getCollectionAccess();
        try
        {
            easyCollectionsImpl.initializeCollections();
        }
        catch (Exception e) // catch-all allowed on initialization.
        {
            throw new ApplicationException(e);
        }
    }

    /**
     * Published when the ApplicationContext is closed, using the close() method on the
     * ConfigurableApplicationContext interface. "Closed" here means that all singleton beans are
     * destroyed. A closed context has reached its end of life; it cannot be refreshed or restarted.
     * 
     * Alternatively you can define a destroy-method in your bean declaration:
     * <pre>
     *    &lt;bean name="myBean" class="org.foo.bar.MyBean" destroy-method="close" />
     * </pre>
     * 
     * @param event
     */
    private void onContextClosed(ContextClosedEvent event)
    {

    }

}
