package nl.knaw.dans.easy.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nl.knaw.dans.common.lang.service.exceptions.ServiceException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Implementation of {@link HttpClientFacade} that uses Apache HTTP Client.
 */
public class ApacheHttpClientFacade implements HttpClientFacade
{
    @Override
    public int post(String url, String content) throws ServiceException
    {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        httppost.setEntity(createHttpEntity(content));
        CloseableHttpResponse response = null;
        try
        {
            response = httpclient.execute(httppost);
        }
        catch (ClientProtocolException e)
        {
            throw new ServiceException("Client Protocol Exception during HTTP exchange", e);
        }
        catch (IOException e)
        {
            throw new ServiceException("I/O Exception during HTTP exchange", e);
        }
        return response.getStatusLine().getStatusCode();
    }

    private HttpEntity createHttpEntity(String content)
    {
        try
        {
            return new StringEntity(content, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException("Could not use UTF-8 to encode HTTP entity ??");
        }
    }

    @Override
    public int delete(String url) throws ServiceException
    {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpDelete delete = new HttpDelete(url);
        CloseableHttpResponse response = null;
        try
        {
            response = httpclient.execute(delete);
        }
        catch (ClientProtocolException e)
        {
            throw new ServiceException("Client Protocol Exception during HTTP exchange", e);
        }
        catch (IOException e)
        {
            throw new ServiceException("I/O Exception during HTTP exchange", e);
        }
        return response.getStatusLine().getStatusCode();
    }
}