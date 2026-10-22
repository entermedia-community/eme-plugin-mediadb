import org.entermediadb.asset.MediaArchive
import org.openedit.Data

// Params: collectionid, memberid (librarycollectionusers id), ontheteam
// (true/false), teamroles (comma separated collectiverole ids).
public void init()
{
	MediaArchive archive = context.getPageValue("mediaarchive");
	if (!Boolean.TRUE.equals(context.getPageValue("canviewsettings")))
	{
		context.putPageValue("error", "Not allowed to edit team members");
		return;
	}
	String collectionid = context.getRequestParameter("collectionid");
	String memberid = context.getRequestParameter("memberid");
	Data member = memberid == null ? null : archive.query("librarycollectionusers").exact("id", memberid).exact("collectionid", collectionid).searchOne();
	if (member == null)
	{
		context.putPageValue("error", "Team member not found");
		return;
	}
	String ontheteam = context.getRequestParameter("ontheteam");
	if (ontheteam != null)
	{
		member.setValue("ontheteam", Boolean.parseBoolean(ontheteam));
	}
	String roles = context.getRequestParameter("teamroles");
	if (roles != null)
	{
		List ids = roles.split(",").collect { it.trim() }.findAll { it };
		member.setValue("teamroles", ids);
	}
	archive.saveData("librarycollectionusers", member);
	context.putPageValue("member", member);
}

init();
