import org.entermediadb.asset.MediaArchive
import org.openedit.MultiValued

// JSON version of community/html/default/project/channel/subscribers/liketoggle.groovy,
// for the signed-in user instead of an email parameter.
public void init()
{
	String collectionid = context.getRequestParameter("collectionid");
	MediaArchive archive = context.getPageValue("mediaarchive");
	if (collectionid == null || context.getUser() == null)
	{
		return;
	}
	String userid = context.getUser().getId();

	MultiValued subscription = archive.query("librarycollectionlikes").exact("collectionid", collectionid).exact("followeruser", userid).searchOne();
	if (subscription != null)
	{
		subscription.setValue("enabled", !subscription.getBoolean("enabled"));
	}
	else
	{
		subscription = archive.getSearcher("librarycollectionlikes").createNewData();
		subscription.setValue("collectionid", collectionid);
		subscription.setValue("followeruser", userid);
		subscription.setValue("addeddate", new Date());
		subscription.setValue("enabled", true);
	}
	archive.saveData("librarycollectionlikes", subscription);

	context.putPageValue("subscription", subscription);
	context.putPageValue("followercount", archive.query("librarycollectionlikes").exact("collectionid", collectionid).exact("enabled", true).search(context).size());
}

init();
