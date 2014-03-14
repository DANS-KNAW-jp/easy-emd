package nl.knaw.dans.pf.language.ddm.datehandlers;

import nl.knaw.dans.pf.language.ddm.handlertypes.BasicDateHandler;
import nl.knaw.dans.pf.language.emd.types.BasicDate;

import org.xml.sax.SAXException;

public class TermsValidHandler extends BasicDateHandler
{
    @Override
    public void finishElement(final String uri, final String localName) throws SAXException
    {
        final BasicDate isoDate = createDate(uri, localName);
        if (isoDate == null)
            return;
        getTarget().getEmdDate().getTermsValid().add(createDate(uri, localName));
    }
}