import java.text.SimpleDateFormat

import org.entermediadb.asset.MediaArchive
import org.openedit.Data

// Any signed-in user can create an event and becomes its host. Events are
// public. Params: name (required), eventdescription, eventlocation,
// startdate (required) and enddate as ISO 8601 (yyyy-MM-dd'T'HH:mm:ssXXX).
Date parseDate(String inValue)
{
	if (inValue == null || inValue.trim().isEmpty())
	{
		return null;
	}
	return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(inValue.trim());
}

public void init()
{
	MediaArchive archive = context.getPageValue("mediaarchive");
	if (context.getUser() == null)
	{
		context.putPageValue("error", "Sign in required");
		return;
	}
	String name = context.getRequestParameter("name");
	if (name == null || name.trim().isEmpty())
	{
		context.putPageValue("error", "name is required");
		return;
	}
	Date start = null;
	Date end = null;
	try
	{
		start = parseDate(context.getRequestParameter("startdate"));
		end = parseDate(context.getRequestParameter("enddate"));
	}
	catch (java.text.ParseException e)
	{
		context.putPageValue("error", "Dates must look like 2026-11-02T09:00:00-07:00");
		return;
	}
	if (start == null)
	{
		context.putPageValue("error", "startdate is required");
		return;
	}
	if (end != null && end.before(start))
	{
		context.putPageValue("error", "enddate is before startdate");
		return;
	}

	Data event = archive.getSearcher("collectiveevent").createNewData();
	event.setName(name.trim());
	event.setValue("eventdescription", context.getRequestParameter("eventdescription"));
	event.setValue("eventlocation", context.getRequestParameter("eventlocation"));
	event.setValue("startdate", start);
	event.setValue("enddate", end);
	event.setValue("eventhost", context.getUser().getId());
	event.setValue("createddate", new Date());
	archive.saveData("collectiveevent", event);

	// The host is going by default
	Data rsvp = archive.getSearcher("collectiveeventrsvp").createNewData();
	rsvp.setValue("eventid", event.getId());
	rsvp.setValue("rsvpuser", context.getUser().getId());
	rsvp.setValue("rsvpstatus", "going");
	rsvp.setValue("rsvpdate", new Date());
	archive.saveData("collectiveeventrsvp", rsvp);

	context.putPageValue("eventid", event.getId());
}

init();
