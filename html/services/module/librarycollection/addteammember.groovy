import org.entermediadb.asset.MediaArchive
import org.entermediadb.email.WebEmail
import org.openedit.Data
import org.openedit.users.User
import org.openedit.users.authenticate.PasswordGenerator

// Adds an existing user (userid) or a new one by email (email, firstName,
// lastName) to the project's team. Same rules as
// community/html/default/project/channel/subscribers/addtoteam.groovy.
public void init()
{
	MediaArchive archive = context.getPageValue("mediaarchive");
	if (!Boolean.TRUE.equals(context.getPageValue("caneditcollection")))
	{
		context.putPageValue("error", "Not allowed to edit this project");
		return;
	}

	String collectionid = context.getRequestParameter("collectionid");
	Data librarycol = collectionid == null ? null : archive.getCachedData("librarycollection", collectionid);
	if (librarycol == null)
	{
		context.putPageValue("error", "Project not found");
		return;
	}

	String teamuserid = context.getRequestParameter("userid");
	String email = context.getRequestParameter("email");
	if (email)
	{
		email = email.trim().toLowerCase();
	}

	User teamuser = null;
	if (teamuserid)
	{
		teamuser = archive.getUserManager().getUser(teamuserid);
	}
	else if (email)
	{
		teamuser = archive.getUserManager().getUserByEmail(email);
		if (teamuser == null)
		{
			teamuser = archive.getUserManager().createUser(null, new PasswordGenerator().generate());
			teamuser.setFirstName(context.getRequestParameter("firstName"));
			teamuser.setLastName(context.getRequestParameter("lastName"));
			teamuser.setEmail(email);
			teamuser.setEnabled(true);
			archive.getUserManager().saveUser(teamuser);
		}
	}
	if (teamuser == null)
	{
		context.putPageValue("error", "User not found");
		return;
	}
	teamuserid = teamuser.getId();

	Data subscription = archive.query("librarycollectionusers").exact("followeruser", teamuserid).exact("collectionid", collectionid).searchOne();
	if (subscription == null)
	{
		subscription = archive.getSearcher("librarycollectionusers").createNewData();
		subscription.setValue("collectionid", collectionid);
		subscription.setValue("followeruser", teamuserid);
		subscription.setValue("addeddate", new Date());
	}
	subscription.setValue("ontheteam", true);
	archive.getSearcher("librarycollectionusers").saveData(subscription);
	context.putPageValue("subscription", subscription);

	try
	{
		String apphome = context.getPageValue("apphome");
		WebEmail templatemail = archive.createSystemEmail(teamuser, apphome + "/theme/emails/collection-add-teammember.html");
		templatemail.setSubject("Added to Team " + librarycol.getName());
		Map objects = new HashMap();
		// One-time login link (single use, one hour) instead of embedding a key in the email
		objects.put("loginlinktoken", archive.getUserManager().createNewTempLoginLink(teamuser.getId(), teamuser.getEmail(), teamuser.getFirstName(), teamuser.getLastName(), teamuser.getScreenName()));
		objects.put("loginemail", java.net.URLEncoder.encode(teamuser.getEmail(), "UTF-8"));
		objects.put("user", context.getUser());
		objects.put("teamuser", teamuser);
		objects.put("librarycol", librarycol);
		objects.put("project", librarycol);
		objects.put("apphome", apphome);
		objects.put("siteroot", archive.getCatalogSettingValue("siteroot"));
		templatemail.send(objects);
	}
	catch (Exception e)
	{
		// The member is saved; only the notification failed.
		log.error("Could not send team email to " + teamuserid, e);
	}
}

init();
