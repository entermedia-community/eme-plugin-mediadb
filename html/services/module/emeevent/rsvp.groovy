import org.entermediadb.asset.MediaArchive
import org.openedit.Data

// Sets the signed-in user's response to an event: status = interested | going,
// or none to take the response back. One rsvp row per user and event.
public void init()
{
	MediaArchive archive = context.getPageValue("mediaarchive");
	String eventid = context.getRequestParameter("eventid");
	String status = context.getRequestParameter("status");
	if (context.getUser() == null)
	{
		context.putPageValue("error", "Sign in required");
		return;
	}
	if (eventid == null || archive.getCachedData("collectiveevent", eventid) == null)
	{
		context.putPageValue("error", "Event not found");
		return;
	}
	if (!["interested", "going", "none"].contains(status))
	{
		context.putPageValue("error", "status must be interested, going or none");
		return;
	}
	String userid = context.getUser().getId();

	Data rsvp = archive.query("collectiveeventrsvp").exact("eventid", eventid).exact("rsvpuser", userid).searchOne();
	if ("none".equals(status))
	{
		if (rsvp != null)
		{
			archive.getSearcher("collectiveeventrsvp").delete(rsvp, null);
		}
	}
	else
	{
		if (rsvp == null)
		{
			rsvp = archive.getSearcher("collectiveeventrsvp").createNewData();
			rsvp.setValue("eventid", eventid);
			rsvp.setValue("rsvpuser", userid);
		}
		rsvp.setValue("rsvpstatus", status);
		rsvp.setValue("rsvpdate", new Date());
		archive.saveData("collectiveeventrsvp", rsvp);
	}
	context.putPageValue("eventid", eventid);
	context.putPageValue("goingcount", archive.query("collectiveeventrsvp").exact("eventid", eventid).exact("rsvpstatus", "going").search().size());
	context.putPageValue("interestedcount", archive.query("collectiveeventrsvp").exact("eventid", eventid).exact("rsvpstatus", "interested").search().size());
	context.putPageValue("myrsvp", "none".equals(status) ? "" : status);
}

init();
