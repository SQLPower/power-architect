## Changes from RC1 to RC2 (backported from trunk) ##

  * [r2718](https://code.google.com/p/power-architect/source/detail?r=2718) - Fixed annoying 'select as you drag' behaviour
  * [r2731](https://code.google.com/p/power-architect/source/detail?r=2731) - Fixed NPE when loading projects with OLAP sessions
  * [r2753](https://code.google.com/p/power-architect/source/detail?r=2753) - Fix for [Bug 1659](https://code.google.com/p/power-architect/issues/detail?id=659) - Current project name is reflected in compare DM panel if project got renamed
  * [r2754](https://code.google.com/p/power-architect/source/detail?r=2754) - Layout fix: project name now spans all available columns in Compare DM so not cut off unnecessarily
  * [r2756](https://code.google.com/p/power-architect/source/detail?r=2756) - Fixed error messages not showing up in SQL Script executor
  * [r2757](https://code.google.com/p/power-architect/source/detail?r=2757) - Fixed [Bug 1658](https://code.google.com/p/power-architect/issues/detail?id=658): spaces in table names not converting to underscores
  * [r2776](https://code.google.com/p/power-architect/source/detail?r=2776) - Fixed Windows file association bug
  * [r2813](https://code.google.com/p/power-architect/source/detail?r=2813) - Fixed [Bug 1661](https://code.google.com/p/power-architect/issues/detail?id=661): can't produce upgrade script when older schema isn't in a physical database _and_ [bug 1334](https://code.google.com/p/power-architect/issues/detail?id=334): Compare DM doesn't explain why Start button is disabled
  * [r2815](https://code.google.com/p/power-architect/source/detail?r=2815) - Fixed [Bug 1662](https://code.google.com/p/power-architect/issues/detail?id=662): Indexes remain on tables even after columns removed
  * [r2818](https://code.google.com/p/power-architect/source/detail?r=2818) - Fixed [Bug 1692](https://code.google.com/p/power-architect/issues/detail?id=692): Screenshot runs off the page on user guide on page 34
  * [r2820](https://code.google.com/p/power-architect/source/detail?r=2820) - Fixed [Bug 1698](https://code.google.com/p/power-architect/issues/detail?id=698): Abstract SQL Server DDL generator should not appear in GUI

## Changes we plan to port from trunk to 0.9.12 branch for RC2 ##

  * None left!

## Notable Not-yet-fixed Bug Reports (in Bugzilla and Forums since RC 1) ##

  * None left!


## Documentation ##

  * The OLAP feature has not been documented at all yet in the User Guide.

# SQLPower Library #

## Changes from RC1 to RC2 (backported from trunk) ##

  * 636 - Removed check for missing username in attempt to get SQL Server integrated security logins working
  * 696 - Split up DBMD wrappers for SQL Server 2000 and 2005 because optimal way of listing schemas is different between the two platforms. See http://www.sqlpower.ca/forum/posts/list/0/1788.page for details.
  * 709 - Fixed [bug 1693](https://code.google.com/p/power-architect/issues/detail?id=693): Need SQL Server jdbc wrapper to strip parentheses from default value

## Changes we plan to incoroprate into RC2 ##

  * None left!

## Notable Not-marked-as-fixed Bugs in SQL Power Library since RC 1 ##

  * None left!