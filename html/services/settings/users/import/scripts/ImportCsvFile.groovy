import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.util.Row
import model.importer.BaseImporter
import org.openedit.WebPageRequest
import org.entermediadb.asset.MediaArchive
import org.openedit.data.Searcher
import org.openedit.event.WebEvent


class CsvImporter extends BaseImporter
{
	/**
	 * This is an example of making a field lower case
	 */
	protected void addProperties( Row inRow, Data inData)
	{
		super.addProperties( inRow, inData);
		//createLookUp(inSearcher.getCatalogId(),inData,"Division","val_divisions");
	}

}

WebPageRequest req = context;
MediaArchive archive = req.getPageValue("mediaarchive");
String searchtype = req.getRequestParameter("searchtype");
log.info("Importing CSV for search type: " + searchtype);
Searcher searcher = archive.getSearcherManager().getSearcher(archive.getCatalogId(), searchtype);
CsvImporter csvimporter = new CsvImporter();
csvimporter.setModuleManager(moduleManager);
csvimporter.setSearcher(searcher);
csvimporter.setContext(context);
csvimporter.setMakeId(false);
csvimporter.importData();
