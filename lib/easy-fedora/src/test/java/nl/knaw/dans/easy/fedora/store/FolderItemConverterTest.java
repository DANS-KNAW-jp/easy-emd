package nl.knaw.dans.easy.fedora.store;

import static org.junit.Assert.assertEquals;
import nl.knaw.dans.common.fedora.fox.DigitalObject;
import nl.knaw.dans.common.lang.test.Tester;
import nl.knaw.dans.common.lang.xml.XMLSerializationException;
import nl.knaw.dans.easy.data.Data;
import nl.knaw.dans.easy.db.testutil.InMemoryDatabase;
import nl.knaw.dans.easy.domain.dataset.FileItemImpl;
import nl.knaw.dans.easy.domain.dataset.FolderItemImpl;
import nl.knaw.dans.easy.domain.model.user.CreatorRole;
import nl.knaw.dans.easy.fedora.db.FedoraFileStoreAccess;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class FolderItemConverterTest {

    private static final Logger logger = LoggerFactory.getLogger(FolderItemConverterTest.class);

    private boolean verbose = Tester.isVerbose();
    private FolderItemConverter converter = new FolderItemConverter();

    @Test
    public void testConverter() throws XMLSerializationException, Exception {

        InMemoryDatabase inMemoryDB = new InMemoryDatabase();
        new Data().setFileStoreAccess(new FedoraFileStoreAccess());

        FolderItemImpl fo1 = new FolderItemImpl("easy-folder:1");
        FileItemImpl fi1 = new FileItemImpl("easy-file:1");
        FolderItemImpl fo2 = new FolderItemImpl("easy-folder:2");
        FileItemImpl fi2 = new FileItemImpl("easy-file:2");
        fi1.setCreatorRole(CreatorRole.ARCHIVIST);
        fi2.setCreatorRole(CreatorRole.DEPOSITOR);

        fo1.addFileOrFolder(fi1);
        fo2.addFileOrFolder(fi2);
        fo1.addFileOrFolder(fo2);

        DigitalObject dob = converter.serialize(fo1);
        if (verbose)
            logger.debug("\n" + dob.asXMLString(4) + "\n");

        FolderItemImpl reconverted = new FolderItemImpl("easy-folder:1");
        converter.deserialize(dob, reconverted);
        assertEquals(fo1.getDatasetItemContainerMetadata().asXMLString(), reconverted.getDatasetItemContainerMetadata().asXMLString());
        inMemoryDB.close();
    }

}
