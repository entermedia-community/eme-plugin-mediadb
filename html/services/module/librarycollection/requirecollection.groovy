// Stops the remaining actions when collectionid is not a project, so
// connectchat.json does not create a topic or channel for nothing.
if (context.getPageValue("librarycol") == null)
{
	context.setCancelActions(true);
}
