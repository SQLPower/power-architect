# Introduction #

The idea behind the Critics feature is to have a monitor of your data model that will notify the user of errors, mistakes, and bad practices in their data model. A simple analogy would be like how spell-checking works in modern word processors.

# Motivation #
  * A vocal group of users dislike some of the automatic data modelling features of the Architect. (For example, how columns can get hijacked for foreign key creation) Adding Critics would allow this automation to be disabled, and make the Architect easier to work with for different data modelling strategies.

  * Currently, one of the less pleasant User Interface aspects of the Architect is the QuickFix functionality when you try to forward engineer a data model into a platform, but your data model violates some rules for that platform. Implementing the Critics feature can allow us to completely remove the QuickFix feature.

  * There are a number of active bugs that are blocked by the implementation of the Critics functionality. These are mostly warning the user about incompatible or incorrect data models, and we believe the Critics feature is the most elegant and least obtrusive method of dealing with this.
    * [Bug 997](https://code.google.com/p/power-architect/issues/detail?id=97) - Check for out-of-range precision and scale
    * [Bug 1439](https://code.google.com/p/power-architect/issues/detail?id=439) - Check for invalid or empty column mappings for relationships
    * [Bug 1536](https://code.google.com/p/power-architect/issues/detail?id=536) - Warn user about using incompatible index types for the given storage engine (MySQL)

# High Level Description #

## Requirements for the next release (0.9.15) ##
  * Provide a basic framework that should make it fairly simple to implement critics for individual items (Should only at most an hour to whip up a critic for a particular error/warning)

  * At minimum support a basic set of critics that replace the DDL warnings checked by the current system. (Provided by the QuickFix system)

  * At least two levels of severity:
    * Errors = problems that will create DDL script that WILL fail if you attempt to run it on a target database.
    * Warnings = problems that will not cause the generated DDL script to fail, but are considered bad practice in database design.

  * Visual notification to the user of problems
    * Provide a visual list of items that critics have found
    * Highlighting of items in the playpen

  * Allow the user to disable and enable critics
    * If you're only working with Oracle, then you probably don't care about critics for SQL Server, MySQL, etc

  * Real-time monitoring of your data model?
    * Critics would be monitoring your data model as you edit it.
    * Would require some technical research into multi-threaded access of the SQL Objects
    * Optionally, to limit the scope, critics could be run on demand instead

## Anti-requirements (What NOT to do) ##
  * Obtrusive warning visualizations (ex. popup messages)
  * Poor performance (Slowing down the whole data modelling process)

## Items to consider for later? ##
  * User-defined critics
  * Support for platform-specific rules
    * ex. Oracle table names cannot be longer than 30 characters
  * Support for detecting bad database design practices
    * ex. Tables without primary keys