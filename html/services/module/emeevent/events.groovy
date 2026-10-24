import java.text.SimpleDateFormat

import org.entermediadb.asset.MediaArchive
import org.openedit.Data
import org.openedit.data.QueryBuilder

// Events for the app. Without params: upcoming and recent events, newest start
// first. query: name filter. rsvped=true: only events the signed-in user
// answered. eventid: a single event.
// Each event is a map with the rsvp counts and the signed-in user's response.
String iso(Date inDate)
{
	if (inDate == null)
	{
		return null;
	}
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	return format.format(inDate);
}

public void init()
{
	MediaArchive archive = context.getPageValue("mediaarchive");
	String userid = context.getUser() == null ? null : context.getUser().getId();
	String eventid = context.getRequestParameter("eventid");

	List<Data> hits = new ArrayList<Data>();
	if (eventid != null)
	{
		Data single = archive.getCachedData("collectiveevent", eventid);
		if (single != null)
		{
			hits.add(single);
		}
	}
	else
	{
		QueryBuilder query = archive.query("collectiveevent");
		String text = context.getRequestParameter("query");
		if (text != null && !text.trim().isEmpty())
		{
			query.match("name", text.trim());
		}
		else
		{
			query.all();
		}
		if (userid != null && Boolean.parseBoolean(context.getRequestParameter("rsvped")))
		{
			List<String> ids = new ArrayList<String>();
			for (Data rsvp : archive.query("collectiveeventrsvp").exact("rsvpuser", userid).search())
			{
				ids.add(rsvp.get("eventid"));
			}
			if (ids.isEmpty())
			{
				context.putPageValue("events", []);
				return;
			}
			query.orgroup("id", ids);
		}
		query.sort("startdateDown");
		hits.addAll(query.search(context).getPageOfHits());
	}

	List events = [];
	for (Data hit : hits)
	{
		Map event = [
			id: hit.getId(),
			name: hit.getName(),
			description: hit.get("eventdescription"),
			location: hit.get("eventlocation"),
			imageurl: hit.get("eventimageurl"),
			startdate: iso(hit.getDate("startdate")),
			enddate: iso(hit.getDate("enddate")),
			hostid: hit.get("eventhost"),
		];
		String hostid = hit.get("eventhost");
		def host = hostid == null ? null : archive.getUser(hostid);
		event.hostname = host == null ? null : host.getScreenName();

		int going = 0;
		int interested = 0;
		String mine = null;
		for (Data rsvp : archive.query("collectiveeventrsvp").exact("eventid", hit.getId()).search())
		{
			String status = rsvp.get("rsvpstatus");
			if ("going".equals(status))
			{
				going++;
			}
			else if ("interested".equals(status))
			{
				interested++;
			}
			if (userid != null && userid.equals(rsvp.get("rsvpuser")))
			{
				mine = status;
			}
		}
		event.goingcount = going;
		event.interestedcount = interested;
		event.myrsvp = mine;
		events.add(event);
	}
	context.putPageValue("events", events);
}

init();
