import org.openedit.Data

// Stops the remaining actions when eventid is not an event, so connectchat.json
// does not create a channel for nothing. Hands the id to ChatModule.loadChatChannel.
String eventid = context.getRequestParameter("eventid");
Data event = eventid == null ? null : context.getPageValue("mediaarchive").getCachedData("collectiveevent", eventid);
if (event == null)
{
	context.setCancelActions(true);
}
else
{
	context.setRequestParameter("dataid", eventid);
	context.putPageValue("entity", event);
	context.putPageValue("eventdata", event);
}
