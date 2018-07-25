package massbank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author rmeier
 * @version 0.1, 18-04-2018
 * This class is called from command line to create a new temporary
 * database <i>tmpdbName</i>, fill it with all records found in <i>DataRootPath</i>
 * and move the new database to <i>dbName</i>.
 */
public class RefreshDatabase {
	private static final Logger logger = LogManager.getLogger(RefreshDatabase.class);
	
	public static void main(String[] args) throws FileNotFoundException, SQLException, ConfigurationException, IOException {
		try {
			logger.trace("Creating a new database \""+ Config.get().tmpdbName() +"\" and initialize a MassBank database scheme.");
			DatabaseManager.init_db(Config.get().tmpdbName());
			
			logger.trace("Creating a DatabaseManager for \"" + Config.get().tmpdbName() + "\".");
			DatabaseManager db  = new DatabaseManager(Config.get().tmpdbName());
			
			logger.info("Opening DataRootPath \"" + Config.get().DataRootPath() + "\" and iterate over content.");
			DirectoryStream<Path> path = Files.newDirectoryStream(FileSystems.getDefault().getPath(Config.get().DataRootPath()));
			for (Path contributorPath : path) {
				if (!Files.isDirectory(contributorPath)) continue;
				if (contributorPath.endsWith(".git")) continue;
				if (contributorPath.endsWith(".scripts")) continue;
				
				String contributor = contributorPath.getFileName().toString();
				logger.trace("Opening contributor path \"" + contributor + "\" and iterate over content.");
				DirectoryStream<Path> path2 = Files.newDirectoryStream(contributorPath);
				for (Path recordPath : path2) {
					logger.info("Validating \"" + recordPath + "\".");
					String recordAsString	= FileUtils.readFileToString(recordPath.toFile(), StandardCharsets.UTF_8);
					Record record = Validator.validate(recordAsString, contributor);
					if (record == null) {
						logger.error("Error reading and validating record \"" + recordPath.toString() + "\".");
						continue;
					}
					logger.trace("Writing record \"" + record.ACCESSION() + "\" to database.");
					db.persistAccessionFile(record);
				}
				path2.close();
			}
			path.close();
			logger.trace("Moving new database to MassBank database.");
			DatabaseManager.move_temp_db_to_main_massbank();
		}
		catch (Exception e) {
			logger.fatal(e);
			System.exit(1);
		}
	}
}
