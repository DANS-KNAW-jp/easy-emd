package nl.knaw.dans.easy.business.services;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.knaw.dans.common.lang.repo.DmoNamespace;
import nl.knaw.dans.common.lang.service.exceptions.CommonSecurityException;
import nl.knaw.dans.comp.dmocollections.testUtil.MockCollectionCreator;
import nl.knaw.dans.easy.data.Data;
import nl.knaw.dans.easy.data.collections.DmoCollectionsAccess;
import nl.knaw.dans.easy.domain.collections.ECollection;
import nl.knaw.dans.easy.domain.model.ECollectionEntry;
import nl.knaw.dans.easy.domain.user.EasyUserImpl;
import nl.knaw.dans.easy.security.CodedAuthz;
import nl.knaw.dans.easy.security.Security;
import nl.knaw.dans.easy.servicelayer.services.CollectionService;
import nl.knaw.dans.i.dmo.collections.DmoCollection;
import nl.knaw.dans.i.security.annotations.SecuredOperationUtil;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EasyCollectionServiceTest extends EasyMock
{
    
    private static CollectionService service;
    private static DmoCollectionsAccess access;
    private Map<DmoNamespace, DmoCollection> testCollections = new HashMap<DmoNamespace, DmoCollection>();
    
    private boolean verbose = false;
    
    @BeforeClass
    public static void beforeClass()
    {
        access = createMock(DmoCollectionsAccess.class);
        
        Data.unlock();
        Data data = new Data();
        data.setCollectionAccess(access);
        
        service = new EasyCollectionService();
        
    }
    
    @Before
    public void beforeTest()
    {
        testCollections.clear();
        reset(access);
    }
    
    @Test
    public void testSecuredOperationIds() throws Exception
    {
        SecuredOperationUtil.checkSecurityIds(EasyCollectionService.class);
    }
    
    @Test(expected = CommonSecurityException.class)
    public void testSecurityOnUpdateCollectionMembershipsNoUser() throws Exception
    {
        new Security(new CodedAuthz());
        service.updateCollectionMemberships(null, null, null);
    }
    
    @SuppressWarnings("serial")
    @Test(expected = NullPointerException.class)
    public void testSecurityOnUpdateCollectionMemberships() throws Exception
    {
        new Security(new CodedAuthz());
        service.updateCollectionMemberships(new EasyUserImpl()
        {
            @Override
            public boolean hasRole(Role... roles)
            {
                return true;
            }
            
            @Override
            public boolean isActive()
            {
                return true;
            }
            
        }, null, null);
    }
    
    @Test
    public void getCollectionEntries() throws Exception
    {
        int wide = 2; int deep = 2;
        expectGetRoot(ECollection.EasyCollection, wide, deep);
        
        replay(access);
        List<ECollectionEntry> entries = service.getCollectionEntries(ECollection.EasyCollection);
        if (verbose)
        {
            for (ECollectionEntry entry : entries)
            {
                System.err.println(entry);
            }
        }
        // size should be calculated size -1: root is not part of list
        assertEquals(MockCollectionCreator.calculateItems(wide, deep) - 1, entries.size());
        verify(access);
    }
    
    @Test
    public void getCollectionEntryMap() throws Exception
    {
        Iterator<ECollection> iter = ECollection.iterator();
        while (iter.hasNext())
        {
            expectGetRoot(iter.next(), 2, 3);
        }
        
        replay(access);
        Map<ECollection, List<ECollectionEntry>> entryMap = service.getCollectionEntries();
        assertEquals(ECollection.values().length, entryMap.size());
        verify(access);
    }

    private void expectGetRoot(ECollection eColl, int wide, int deep) throws Exception
    {
        expect(access.getRoot(eColl)).andReturn(getRoot(eColl.namespace, wide, deep)).anyTimes();
    }

    private DmoCollection getRoot(DmoNamespace namespace, int wide, int deep) throws Exception
    {
        DmoCollection root = testCollections.get(namespace);
        if (root == null)
        {
            root = MockCollectionCreator.createRoot(namespace, wide, deep);
            testCollections.put(namespace, root);
        }
        return root;
    }
    
}
