# Introduction #

This document intends to outline the details of future planned releases of the Power\*Architect. Details included planned features, bug fixes, and enhancements, and estimated release dates.



# Details #
## Release 0.9.11 ##

### Changelist ###
  * An easier-to-use executable (.exe) installer for Windows users
  * A new Installation Guide
  * Framework to support multiple languages.
  * Item selection is now synchronized between the DB Tree and the Playpen. When something is selected in the Playpen, its corresponding item is selected on DB Tree.
  * Feature to check availability of newer releases
  * Undo/Redo now work with relationships, including moving connection points, and straightening lines.
  * Option to hide certain types columns
  * Ability to change the colour of tables and their text, and the ability to display them with rounded edges.
  * Auto-scrolling for the Playpen
  * Ability to reverse the direction of a relationship
  * Ability to focus on either the parent or child table of a relationship from its right-click menu
  * Ability to delete columns when columns and relationships are selected at the same time
  * Ability to align tables either horizontally or vertically on the Playpen
  * Ability to delete items by deleting their corresponding node in the DB Tree
  * Added restore window size preference on start
  * Relationships now change between identifying and non-identifying when foreign key columns are moved in or out of the child table's primary key.
  * Ability to select all child tables of a particular table in DB Tree
  * Expand all and collapse all options on the DB Tree
  * List of source connections in the top menu bar now updates properly
  * Columns in the Playpen are specially marked if they are a primary key, a foreign key, or an alternate key
  * Added work-around for Oracle JDBC driver bug: Reverse engineering Oracle databases no longer performs ANALYZE TABLES operation!
  * [Bug 1548](https://code.google.com/p/power-architect/issues/detail?id=548): BLOB and CLOB no longer specify precision/size in Oracle DDL
  * [Bug 1542](https://code.google.com/p/power-architect/issues/detail?id=542): Column insertion point correction during addition/removal
  * [Bug 1301](https://code.google.com/p/power-architect/issues/detail?id=301): Sorting tables by date in the Profile Manager now works properly
  * [Bug 1580](https://code.google.com/p/power-architect/issues/detail?id=580): Sorting tables by date is done dynamically during profiling
  * [Bug 1161](https://code.google.com/p/power-architect/issues/detail?id=161): Tables are now created in the correct location after changing the zoom level


## Release 0.9.12 ##
### Proposed Changelist ###
  * [Bug 1274](https://code.google.com/p/power-architect/issues/detail?id=274): SQL Runner runs out of connections if running SQL Statements repeatedly (reproducible on PostgreSQL 8.x). Possible alternative is to switch to another SQL tool, ex. SQL Workbench
  * [Bug 1040](https://code.google.com/p/power-architect/issues/detail?id=040): Disable precision and scale options in the Column Editor for column types and platforms that don't support them.
  * [Bug 1588](https://code.google.com/p/power-architect/issues/detail?id=588): Fix Print Dialog
  * [Bug 1099](https://code.google.com/p/power-architect/issues/detail?id=099): Add Labels and text regions. Also allow labels to be associated with particular tables.
  * [Bug 1139](https://code.google.com/p/power-architect/issues/detail?id=139): Open up connection properties dialog if trying to connect to a database with incomplete connection parameters
  * [Bug 1070](https://code.google.com/p/power-architect/issues/detail?id=070): Allow specifying of physical names for database objects
  * [Bug 1371](https://code.google.com/p/power-architect/issues/detail?id=371): Visualization of indexes. Suggestions include colouring, a separate 'index' checkbox column, or a column tag (like the PK and FK tags)
  * [Bug 1441](https://code.google.com/p/power-architect/issues/detail?id=441): Fix SAXException when canceling loading a project
  * [Bug 1510](https://code.google.com/p/power-architect/issues/detail?id=510): Automatic update checking (and downloading?)
  * [Bug 1512](https://code.google.com/p/power-architect/issues/detail?id=512): Implement a proper DDL Preferences GUI
  * [Bug 1553](https://code.google.com/p/power-architect/issues/detail?id=553): Allow enabling/disabling foreign key constraints


## Release 1.0 ##
### Proposed Changelist ###
  * [Bug 945](https://code.google.com/p/power-architect/issues/detail?id=45): Implement critics/proofreaders system
  * [Bug 1180](https://code.google.com/p/power-architect/issues/detail?id=180): Datasource settings in the application should update to recognize changes made to the pl.ini/xml file done done externally from the Power\*Architect
  * [Bug 1181](https://code.google.com/p/power-architect/issues/detail?id=181): Stop using SQLExceptionNode
  * [Bug 1313](https://code.google.com/p/power-architect/issues/detail?id=313): Add feature to view contents of a source database table (ex. right-click menu option in DB Tree)
  * [Bug 1184](https://code.google.com/p/power-architect/issues/detail?id=184): Properly implement support for views, and have Compare DM feature distinguish between the two
  * [Bug 1316](https://code.google.com/p/power-architect/issues/detail?id=316): Driver class doesn't auto-update according to the class name you clicked in the JDBC driver package view
  * [Bug 1231](https://code.google.com/p/power-architect/issues/detail?id=231): Automate release process as much as possible (May also consider converting the build process to use Apache Maven)
  * [Bug 1233](https://code.google.com/p/power-architect/issues/detail?id=233): Save Exception stacktraces in the project file
  * [Bug 1051](https://code.google.com/p/power-architect/issues/detail?id=051): Use Undo/Redo facilities to determine if a project has unsaved changes
  * [Bug 1062](https://code.google.com/p/power-architect/issues/detail?id=062): Make sure Undo/Redo facility works properly on all operations we want it to
  * [Bug 1109](https://code.google.com/p/power-architect/issues/detail?id=109): Allow Compare DM on a subset of a data model
  * [Bug 1110](https://code.google.com/p/power-architect/issues/detail?id=110): Support multiple target databases in one project
  * [Bug 1112](https://code.google.com/p/power-architect/issues/detail?id=112): Add a database connection progress bar and spinning icon
  * [Bug 1114](https://code.google.com/p/power-architect/issues/detail?id=114): Modify DDL generator to use Compare DM. This makes conflict resolution by dropping objects unnecessary
  * [Bug 1126](https://code.google.com/p/power-architect/issues/detail?id=126): Allow editing names of tables, relationships, and columns by just clicking on them in the Playpen
  * [Bug 1132](https://code.google.com/p/power-architect/issues/detail?id=132): Allow altering column type, precision, and scale without using the properties editor. Suggestions include a minimalized properties toolbar, or a right-click menu option.
  * [Bug 1156](https://code.google.com/p/power-architect/issues/detail?id=156): Parent property should be maintained only by parent objects
  * [Bug 1168](https://code.google.com/p/power-architect/issues/detail?id=168): Check consistency of formatting in Compare DM-generated DDL statements. (spacing, indentation, etc)
  * [Bug 1179](https://code.google.com/p/power-architect/issues/detail?id=179): Support for refreshing source databases
  * [Bug 1190](https://code.google.com/p/power-architect/issues/detail?id=190): Allow case-sensitive Compare DM
  * [Bug 1192](https://code.google.com/p/power-architect/issues/detail?id=192): Use different cursors for create table and create relationship
  * [Bug 1234](https://code.google.com/p/power-architect/issues/detail?id=234): Properly display exception messages in profile PDF reports
  * 1277: Implement a Connection Monitor that shows all open connections, statements run through them, and what state they are in.
  * 1297: Retain profile data types when loading profile results saved in the project
  * 1316: JDBC driver class should update according to the classname selected in the JDBC driver tree
  * 1326: Allow user to choose whether or not to save username and password fields in DB connection settings
  * 1329: Add option to show only schemas owned by the user (especially for Oracle and SQL Server)
  * 1334: Use validation framework in the SQL Power Library to explain why Compare DM is disabling the Start button.
  * 1342: Add a feature to allow users to compare data of subsets of tables.
  * 1344: Make PDF export progress bar fill up more accurately.
  * 1351: DDL should not specify precision with Image and Text data types when Forward Engineering to SQL Server
  * 1354: DDL validation should catch foreign keys with no columns (perhaps something for the critics/proofreaders system?)
  * 1372: Allow selecting target catalog when creating Kettle jobs
  * 1381: Fix license formatting on installers
  * 1388: Make sure absolutely NO windows have the Java coffee cup logo in the top left corner
  * 1393: For PostgreSQL, ignore precision of DATE data types when doing comparison
  * 1394: Do not show Compare DM statements that have no effect
  * 1398: Add top N values to the HTML report
  * 1403: Allow drag-n-drop in the Playpen to copy objects (with a modifier key)
  * 1432: Cleanup CoreUserSettings Preferences API
  * 1433: Allow the user to toggle column hijacking
  * 1434: Fix corrupt CSV reports when profile data contains CSV characters (commas for example)
  * 1436: Adding new columns to a table with existing relationship
  * 1439: Add critic to monitor for relationships with no columns
  * 1450: Reverse engineering a TIMESTAMP column from Oracle should return the TIMESTAMP data type
  * 1451: Reverse engineering a UTF-8 column from Oracle should set the column length to number of characters, not number of bytes
  * 1452: Forward engineer Oracle VARCHAR2 columns with number of characters (n CHAR) instead of number of bytes
  * 1457: Support dragging and dropping objects between session windows
  * 1458: Utilize the Apache DDLUtils library in our DDL generator?
  * 1464: Return to using an XML format for storing database type and connection information instead of the PL.INI
  * 1466: Allow moving the bend points in the relationship lines around
  * 1468: Reuse object IDs when saving projects so that the diffs when saving changes to a project are smaller
  * 1469: Allow printing/PDF export on subsets of a data model
  * 1470: Allow editing multiple columns or multiple tables at once. (iTunes style)
  * 1472: Support multiple playpens in a project
  * 1473: Full support for views
  * 1474: Support for entering seed data for tables
  * 1475: Ability to handle refactoring. Ex. Compare DM should differentiate, either automatically or with the user's help, difference between a new column/table/relationship/etc, and a renamed one. Also handle changed column data types, and changed primary keys
  * 1476: Ability to turn indexes on and off. (Not sure what this means exactly, but Mario requested it as Toad DM supports it)
  * 1480: Implement support for subtypes and supertypes
  * 1481: Support for column data domains
  * 1482: Show column domain values next to column
  * 1483: Support naming of relationships and labeling their endpoints
  * 1484: Support for alternate keys
  * 1488: Table and column definition sections in addition to remarks
  * 1489: Allow output of the data dictionary in HTML, CSV, and PDF. (castorp's XSLT seems to handle HTML for us already. See http://www.sqlpower.ca/forum/posts/list/1978.page)
  * 1490: Support separate logical and physical data models
  * 1492: Support for specifying a table partioning strategy
  * 1498: Support column ETL lineage from multiple sources
  * 1494: Support creating materialized views. (Depends on 1184)
  * 1515: Support side scrolling for mice that support them. (Ex. Apple's Mighty Mouse)
  * 1517: Display full build version, particularly for nightly builds which include the date
  * 1522: The forward engineer dialog's OK button remains enabled even after starting. It should be disabled
  * 1525: Allow modifying the log4j settings from the User Preferences
  * 1526: Support indexes on a function on a column. Currently, reverse engineering such an index causes an NPE
  * 1534: A project's print settings should be saved along with the project
  * 1536: Support for platform specific DDL options. Ex. this particular bug referred to being able to specify the MySQL storage engine to use
  * 1537: Allow the user to specify a DDL generator when creating a datasource type.
  * 1540: Support for colouring of tables based on ETL lineage
  * 1552: Fix remote profiling on Sybase
  * 1555: Support ON DELETE and ON UPDATE clauses for FK constraints
  * 1557: Fix auto-increment support for PostgreSQL
  * 1559: Support scaling the data model when doing PDF export
  * 1560: Forward engineering DDL contains constraint rules with spaces in their names. Replace them with underscrores.
  * 1562: Allowing viewing data of the profiles from the profiler.
  * 1563: Support for embedded scripting
  * 1564: Allowing showing printing page boundries in Playpen
  * 1565: Add SQL Power branding to the app, pdf printouts, reports, etc.
  * 1566: Allow user to specify the path relationship lines take (including where bends happen)
  * 1567: Support cut, copy, and pasting tables, columns, and relationships in the Playpen, and ideally between Architect sessions and into other apps like text editors
  * 1573: Add support for updating the data model from the results of a Compare DM
  * 1574: Support changing the font in the Playpen
  * 1577: Add more translations to the Power\*Architect and keep them up to date
  * 1583: Support check constraints on a column
  * 1587: Fix FK mapping (Details at http://trillian.sqlpower.ca/bugzilla/show_bug.cgi?id=1587)
  * 1588: Fix broken print dialog (http://www.sqlpower.ca/forum/posts/list/1982.page)
  * 1589: SQL Server DDL statements need to be ended with a semicolon
  * 1590: Fix problems with PK naming when forward engineering to MySQL and HSQLDB
  * 1591: Make sure auto-scrolling works properly
  * 1593: Option to exclude certain tables from forward engineering