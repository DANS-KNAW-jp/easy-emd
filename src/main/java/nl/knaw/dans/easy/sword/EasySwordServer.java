package nl.knaw.dans.easy.sword;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletResponse;

import nl.knaw.dans.easy.domain.model.Dataset;
import nl.knaw.dans.easy.domain.model.emd.EasyMetadata;
import nl.knaw.dans.easy.domain.model.user.EasyUser;

import org.purl.sword.atom.Author;
import org.purl.sword.atom.Content;
import org.purl.sword.atom.Contributor;
import org.purl.sword.atom.Generator;
import org.purl.sword.atom.InvalidMediaTypeException;
import org.purl.sword.atom.Link;
import org.purl.sword.atom.Summary;
import org.purl.sword.atom.Title;
import org.purl.sword.base.AtomDocumentRequest;
import org.purl.sword.base.AtomDocumentResponse;
import org.purl.sword.base.Collection;
import org.purl.sword.base.Deposit;
import org.purl.sword.base.DepositResponse;
import org.purl.sword.base.ErrorCodes;
import org.purl.sword.base.SWORDAuthenticationException;
import org.purl.sword.base.SWORDEntry;
import org.purl.sword.base.SWORDErrorException;
import org.purl.sword.base.SWORDException;
import org.purl.sword.base.Service;
import org.purl.sword.base.ServiceDocument;
import org.purl.sword.base.ServiceDocumentRequest;
import org.purl.sword.base.Workspace;
import org.purl.sword.server.SWORDServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasySwordServer implements SWORDServer
{
    /** TODO share this constant some how with the EASY application */
    private static final String DOWNLOAD_URL_FORMAT         = "%s/resources/easy/fileDownloadResource?params={'rootSid':'%s','downloadType':'zip','selectedItemList':['*']}";

    /** TODO share this constant some how with the EASY application */
    private static final String DATASET_PATH                = "/datasets/id/";

    /**
     * See {@linkplain http://www.swordapp.org/docs/sword-profile-1.3.html#b.5.5}<br>
     * Only a published state would be appropriate for code 201
     */
    private static final int    HTTP_RESPONSE_DATA_ACCEPTED = 202;

    private static String       policy                      = "easy.sword.server.policy not configured";
    private static String       treatment                   = "easy.sword.server.treatment not configured";

    static Logger               log                         = LoggerFactory.getLogger(EasySwordServer.class);

    /**
     * Provides a dumb but plausible service document - it contains an anonymous workspace and
     * collection, and one personalised for the onBehalfOf user.
     * 
     * @param onBehalfOf
     *        The user that the client is acting on behalf of
     * @throws SWORDAuthenticationException
     *         If the credentials are bad
     * @throws SWORDErrorException
     *         If something goes wrong, such as
     */
    public ServiceDocument doServiceDocument(final ServiceDocumentRequest sdr) throws SWORDAuthenticationException, SWORDErrorException, SWORDException
    {
        // Authenticate the user
        log.info(MessageFormat.format("SERVICE DOCUMENT user={0}; IP={1}; location={2}; onBehalfOf={3}", sdr.getUsername(), sdr.getIPAddress(),
                sdr.getLocation(), sdr.getOnBehalfOf()));
        final String userID = sdr.getUsername();
        final String password = sdr.getPassword();
        if (userID != null)
        {
            if (null == EasyBusinessFacade.getUser(userID, password))
                throw new SWORDAuthenticationException(userID + " not authenticated");
        }

        // Allow users to force the throwing of a SWORD error exception by setting
        // the OnBehalfOf user to 'error'
        if ((sdr.getOnBehalfOf() != null) && (sdr.getOnBehalfOf().equals("error")))
        {
            // Throw the error exception
            throw new SWORDErrorException(ErrorCodes.MEDIATION_NOT_ALLOWED, "Mediated deposits not allowed");
        }

        // Create and return a dummy ServiceDocument
        final ServiceDocument document = new ServiceDocument();
        final Service service = new Service("1.3", true, true);
        document.setService(service);
        final String locationBase = toLocationBase(sdr.getLocation());

        if (sdr.getLocation().contains("?nested="))
        {
            final Collection collection = createDummyCollection(1);
            collection.setTitle("Nested collection: " + sdr.getLocation().substring(sdr.getLocation().indexOf('?') + 1));
            collection.setLocation(locationBase + "/deposit/nested");
            collection.setMediation(false);
            service.addWorkspace(createWorkSpace(collection, "Nested service document workspace"));
        }
        else if (sdr.getUsername() != null)
        {
            final Collection collection = createDummyCollection(0.8f);
            collection.setTitle("Authenticated collection for " + userID);
            collection.setLocation(locationBase + "/deposit/" + userID);
            collection.setAbstract("A collection that " + userID + " can deposit into");
            collection.setService(locationBase + "/servicedocument?nested=authenticated");
            collection.setMediation(false);
            service.addWorkspace(createWorkSpace(collection, "Authenticated workspace for " + userID));
        }

        final String onBehalfOf = sdr.getOnBehalfOf();
        if ((onBehalfOf != null) && (!onBehalfOf.equals("")))
        {
            final Collection collection = createDummyCollection(0.8f);
            collection.setTitle("Personal collection for " + onBehalfOf);
            collection.setLocation(locationBase + "/deposit?user=" + onBehalfOf);
            collection.setAbstract("An abstract goes in here");
            collection.setMediation(true);
            service.addWorkspace(createWorkSpace(collection, "Personal workspace for " + onBehalfOf));
        }
        log.info("return service document to " + userID + " " + document.toString());
        return document;
    }

    private static String toLocationBase(final String inputLocation) throws SWORDErrorException
    {
        final URL url = toUrl(inputLocation);
        final String baseLocation = toBaseLocation(url);
        final String subPath = new File(url.getPath()).getParent();
        final String outputLocation = baseLocation + subPath;
        log.debug("location is: " + outputLocation + "    " + inputLocation);

        return outputLocation;
    }

    private static String toServer(final String inputLocation) throws SWORDErrorException
    {
        final String location = toBaseLocation(toUrl(inputLocation));
        log.debug("location is: " + location + "    " + inputLocation);

        return location;
    }

    private static String toBaseLocation(final URL url)
    {
        return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
    }

    private static URL toUrl(final String inputLocation) throws SWORDErrorException
    {
        final URL url;
        try
        {
            url = new URL(inputLocation);
        }
        catch (final MalformedURLException exception)
        {
            final String message = inputLocation + " Invalid location: " + exception.getMessage();
            log.error(message, exception);
            throw new SWORDErrorException(ErrorCodes.ERROR_BAD_REQUEST, message);
        }
        return url;
    }

    private Workspace createWorkSpace(final Collection collection, final String title)
    {
        final Workspace workspace = new Workspace();
        workspace.setTitle(title);
        workspace.addCollection(collection);
        return workspace;
    }

    private Collection createDummyCollection(final float qualityValue)
    {
        final Collection collection = new Collection();
        collection.setCollectionPolicy(policy);
        collection.setTreatment(treatment);
        collection.addAccepts("application/zip");
        collection.addAccepts("application/xml");
        collection.addAcceptPackaging("http://purl.org/net/sword-types/METSDSpaceSIP");
        collection.addAcceptPackaging("http://purl.org/net/sword-types/bagit", qualityValue);
        return collection;
    }

    public DepositResponse doDeposit(final Deposit deposit) throws SWORDAuthenticationException, SWORDErrorException, SWORDException
    {
        log.info(MessageFormat.format("DEPOSIT user={0}; IP={1}; location={2}; fileName={3}", deposit.getUsername(), deposit.getIPAddress(),
                deposit.getLocation(), deposit.getFilename()));

        final EasyUser user = EasyBusinessFacade.getUser(deposit.getUsername(), deposit.getPassword());
        if (user == null)
            throw new SWORDAuthenticationException(deposit.getUsername() + " not authenticated");

        // Check this is a collection that takes "on behalf of" deposits, else throw an error
        if (((deposit.getOnBehalfOf() != null) && (!deposit.getOnBehalfOf().equals(""))) && (!deposit.getLocation().contains("deposit?user=")))
            throw new SWORDErrorException(ErrorCodes.MEDIATION_NOT_ALLOWED, "Mediated deposit not allowed to this collection");

        final Dataset dataset = process(deposit, user);
        
        final String datasetUrl = toServer(deposit.getLocation()) + DATASET_PATH + dataset.getStoreId();
        final DepositResponse response = wrapResponse(wrapSwordEntry(deposit, user, dataset, datasetUrl), datasetUrl);
        log.info("return service document to " + deposit.getUsername() + " " + response.toString());
        return response;
    }

    private Dataset process(final Deposit deposit, final EasyUser user) throws SWORDException, SWORDErrorException
    {
        final UnzipResult unzipped = new UnzipResult(deposit.getFile());
        final EasyMetadata metadata = validate(user, unzipped.getEasyMetaData());
        final Dataset dataset = submit(deposit, user, unzipped, metadata);
        return dataset;
    }

    private EasyMetadata validate(final EasyUser user, byte[] easyMetaData) throws SWORDErrorException, SWORDException
    {
        EasyBusinessFacade.validateSyntax(easyMetaData);
        final EasyMetadata metadata = EasyBusinessFacade.unmarshallEasyMetaData(easyMetaData);
        EasyBusinessFacade.validateSemantics(user, metadata);
        return metadata;
    }

    private Dataset submit(final Deposit deposit, final EasyUser user, final UnzipResult unzipped, final EasyMetadata metadata) throws SWORDException,
            SWORDErrorException
    {
        final Dataset dataset;
        if (deposit.isNoOp())
            dataset = EasyBusinessFacade.mockSubmittedDataset(metadata, user);
        else
            dataset = EasyBusinessFacade.submitNewDataset(user, metadata, unzipped.getDataFolder(), unzipped.getFiles());
        unzipped.clearTemp();
        return dataset;
    }

    private static SWORDEntry wrapSwordEntry(final Deposit deposit, final EasyUser user, final Dataset dataset, final String datasetUrl)
            throws SWORDException, SWORDErrorException
    {
        final SWORDEntry swordEntry = new SWORDEntry();
        final EasyMetadata metadata = dataset.getEasyMetadata();
        final String serverURL = toServer(deposit.getLocation());

        swordEntry.setTitle(wrapTitle(dataset.getPreferredTitle()));
        swordEntry.setSummary(wrapSummary(metadata.getEmdDescription().toString()));
        swordEntry.addCategory(EasyBusinessFacade.formatAudience(metadata));
        swordEntry.setId(dataset.getPersistentIdentifier());
        swordEntry.setUpdated(metadata.getEmdDate().toString());
        swordEntry.addAuthors(wrapAuthor(user));

        // we won't support updating (task for archivists) so skip MediaLink
        // swordEntry.addLink(wrapEditMediaLink());
        swordEntry.addLink(wrapLink("edit", datasetUrl));
        swordEntry.setGenerator(wrapGenerator(serverURL));
        swordEntry.setContent(wrapContent(serverURL, dataset.getStoreId()));
        swordEntry.setTreatment(EasyBusinessFacade.getSubmussionNotification(user, dataset));
        swordEntry.setNoOp(deposit.isNoOp());
        // TODO swordEntry.setRights(rights);
        if (deposit.getOnBehalfOf() != null)
            swordEntry.addContributor(wrapContributor(deposit.getOnBehalfOf()));
        if (deposit.isVerbose())
            swordEntry.setVerboseDescription(EasyBusinessFacade.getLicenseAsHtml(user, deposit.isNoOp(), dataset));

        return swordEntry;
    }

    private DepositResponse wrapResponse(final SWORDEntry swordEntry, final String storeID)
    {
        final DepositResponse depostiResponse = new DepositResponse(Deposit.CREATED);
        depostiResponse.setEntry(swordEntry);
        depostiResponse.setLocation(storeID);
        depostiResponse.setHttpResponse(HTTP_RESPONSE_DATA_ACCEPTED);
        return depostiResponse;
    }

    private static Title wrapTitle(final String value)
    {
        final Title title = new Title();
        title.setContent(value);
        return title;
    }

    private static Summary wrapSummary(final String value)
    {
        final Summary summary = new Summary();
        summary.setContent(value);
        return summary;
    }

    /**
     * See {@linkplain http://www.swordapp.org/docs/sword-profile-1.3.html#b.9.6}<br>
     * If a server provides an edit-media link it SHOULD allow media resource updating with PUT as
     * described in [AtomPub] sections 9.3 and 9.6.
     */
    @SuppressWarnings("unused")
    private static Link wrapEditMediaLink()
    {
        final Link em = new Link();
        em.setRel("edit-media");
        em.setHref("http://www.myrepository.ac.uk/sdl/workflow/my deposit");
        return em;
    }

    private static Link wrapLink(final String rel, final String location)
    {
        final Link link = new Link();
        link.setRel(rel);
        link.setHref(location);
        return link;
    }

    private static Generator wrapGenerator(final String server)
    {
        final Generator generator = new Generator();
        generator.setContent("Easy SWORD Server");
        generator.setUri(server);
        generator.setVersion("1.3");
        return generator;
    }

    private static Content wrapContent(final String server, final String storeID) throws SWORDException
    {
        final Content content = new Content();
        final String mediaType = "application/zip";
        try
        {
            content.setType(mediaType);
        }
        catch (final InvalidMediaTypeException exception)
        {
            log.error(mediaType, exception);
            throw new SWORDException("", exception);
        }
        final String url = String.format(DOWNLOAD_URL_FORMAT, server, storeID);
        content.setSource(url);
        return content;
    }

    private static Contributor wrapContributor(final String onBehalfOf)
    {
        final Contributor contributor = new Contributor();
        contributor.setName(onBehalfOf);
        contributor.setEmail(onBehalfOf + "@myrepository.ac.uk");
        return contributor;
    }

    private static Author wrapAuthor(final EasyUser user)
    {
        final Author author = new Author();
        author.setName(user.getDisplayName());
        author.setEmail(user.getEmail());
        return author;
    }

    public AtomDocumentResponse doAtomDocument(final AtomDocumentRequest adr) throws SWORDAuthenticationException, SWORDErrorException, SWORDException
    {
        log.info(MessageFormat.format("ATOM DOC user={0}; IP={1}; location={2}", adr.getUsername(), adr.getIPAddress(), adr.getLocation()));
        if (null == EasyBusinessFacade.getUser(adr.getUsername(), adr.getPassword()))
            throw new SWORDAuthenticationException(adr.getUsername() + " not authenticated");

        return new AtomDocumentResponse(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    // TODO please peer review web.xml configuration
    public static void setPolicy(final String policy)
    {
        EasySwordServer.policy = policy;
    }

    // TODO please peer review web.xml configuration
    public static void setTreatment(final String treatment)
    {
        EasySwordServer.treatment = treatment;
    }
}
