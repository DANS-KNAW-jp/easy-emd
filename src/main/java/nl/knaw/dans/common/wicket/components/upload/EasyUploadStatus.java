package nl.knaw.dans.common.wicket.components.upload;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lobo
 * This class contains the information concerning the upload process. It is being held
 * by an UploadProcess class.
 */
public class EasyUploadStatus extends UploadStatus
{
	/** Log. */
	private static final Logger LOG = LoggerFactory.getLogger(EasyUploadStatus.class);

	private Integer uploadId;

	public EasyUploadStatus(Integer uploadId, String message)
	{
		super(message);
		this.uploadId = uploadId;
	}

	public JSONObject toJSONObject()
	{
		JSONObject jobj = new JSONObject();
		try
		{
			jobj.put("uploadId", uploadId);
			jobj.put("message", getMessage());
			jobj.put("error", isError());
			jobj.put("finished", isFinished());
			jobj.put("percentComplete", getPercentComplete());
		}
		catch (JSONException e)
		{
			//TODO: send exception to general exception handler
			LOG.error("Caught error while serializing UploadStatus object to JSON.", e);
			return jobj;
		}
		return jobj;
	}

	public Integer getUploadId()
	{
		return uploadId;
	}
}
