Welcome to the Power\*Architect development community!  This document is your starting point for getting involved in contributing to the Architect's day-to-day development, and for how to have your say in the Architect's future direction.

We've organized the sections in relative order of importance, so the most important items should be near the top.  Don't be intimidated by the length of this document--you don't have to do everything listed here all at once!

# Subscribe To the Power\*Architect Mailing Lists #

There are three mailing lists associated with the Power\*Architect. All are are hosted
on Google Groups.  We strongly recommend that you subscribe to all three if you are interested in getting involved in Architect development.

## Development discussion ##
First, the **architect-developers** mailing list. This list is for discussion about specific design decisions, the future direction of the Architect, giving comments and suggestions, or any other concerns that you may have while making modifications.  For end-user support, your best bet is the Architect web forum, which is a separate venue.  We'll cover that later in this document.

You can subscribe to the **architect-developers**
mailing list at http://groups.google.com/group/architect-developers.

## Major Announcements ##
Next, **architect-announce** is a read-only list that we use to announce major
news about the Power\*Architect project.  We recommend all developers and end users
subscribe to this list so they don't miss any important news about the Architect.  Expect
less than one message per week from this list.

You can subscribe to the **architect-announce**
mailing list at http://groups.google.com/group/architect-announce.

## Commit log ##
Finally, **architect-commits** is a read-only list for commits to Power\*Architect. If you subscribe to this list, you'll be able to stay current on all the latest changes to the
Architect code base. This list gets an automatic email every time someone commits a change
to the Power\*Architect repository. Each email includes the commit comment (a short summary
of the change written by the developer who committed it), and a list of the modifications--including line-by-line diffs for changes to textual files.

You can subscribe to the **architect-commits** mailing list at http://groups.google.com/group/architect-commits.


# Get the Source Code #

## If you are using Eclipse ##
To check out the Power\*Architect source into an Eclipse project:
  1. If you don't yet have the Subversion feature available on your version of Eclipse, get [Subclipse](http://subclipse.tigris.org/install.html)
  1. Start Eclipse and create a new project (File -> New -> Project...)
  1. Under the SVN folder, select **Checkout Projects from SVN**. If you don't see the SVN folder, go back to step 1. ;)
  1. Create a new repository location to check out the SQL Power Library from. Power\*Architect requires this project to build. The URL for the new repository is http://sqlpower-library.googlecode.com/svn/
  1. To check out all of the latest source files for the SQL Power Library, select the **trunk** directory, then press next
  1. Ensure that the "Check out as a project in the workspace" and "Head Revision" options are selected (they are the defaults), then click **Finish**.
  1. Ensure the Eclipse project is set to compile with Java 5 language level, and against the JDK 5 libraries. Trying to compile against JDK 6 or newer will cause compile errors in the JDBC Wrappers.
  1. Repeat steps 2-6 for Power\*Architect, except use the following repository URL at step 4: http://power-architect.googlecode.com/svn/

## Other Development Environments ##
Currently, everyone on the Architect team has chosen to use Eclipse.  If you have a different favourite Java IDE, we'd be happy to post your instructions for checking out the Architect code here.  Just post to the architect-developers list!

## Command-line svn ##
If you just want to check out the source into a local directory or your development environment does not integrate with Subversion, use the command line **svn** tool:

> `svn checkout http://power-architect.googlecode.com/svn/trunk power-architect`


# Support Web Forum #

For end-user support, visit the SQL Power forums at
http://www.sqlpower.ca/forum/. You've probably already noticed that we
want to encourage all Architect users to hang out on the forum so they can ask questions,
get answers, trade tips and advice, and so on.


# Nuts and Bolts #

Ok, so you've joined the list, you've checked out the code, and you're ready to get your
hands dirty with some real development.  In order for us to incorporate your changes
with the least amount of hassle, here's the stuff you'll want to know about:

  * Our JavaFormattingConventions
  * Our JavaPractices

# How To Use The Build File #

The following section assumes that Eclipse is being used for development.

To build Power\*Architect from the source files Apache Ant version 1.7 or higher
is required. Ant 1.7 should be included with Eclipse 3.3 (Europa). If you are using an older version of Eclipse, see the following sub-section on how to install Apache Ant version 1.7 for
Eclipse. Once Ant 1.7 is installed Power\*Architect can be run by using the
build.xml file. To run Power\*Architect open the build.xml file and select the
target named run in the outline. Right click on the run target and select
Ant Build from the Run As menu option. Another target of interest is the alltest
target. This target will run the different test suites available with Power\*Architect.
The alltest target can be run by right clicking on the alltest target and selecting
the Ant Build from the Run As menu option.

> ## How To Install Apache Ant For Eclipse (3.2 and older) ##

> Apache Ant is available at http://ant.apache.org/bindownload.cgi. To install Ant 1.7 into Eclipse first unzip and install Ant to your local machine. Then under Eclipse's user preferences select the Ant runtime item in the list of preferences	on the left. Click on the Ant Home button and select the folder that you installed	Ant 1.7 into. Then click ok and Eclipse will use the new Ant.

# Bugzilla Database #

We recently made our Bugzilla database for Power\*Architect open to the public at http://trillian.sqlpower.ca/bugzilla

You can browse through existing bugs and pick something to investigate, or if you've found any new bugs, you can sign up for a Bugzilla account and report them.


# FishEye & Crucible #

We are using Atlassian's FishEye (http://www.atlassian.com/software/fisheye/) tool to monitor changes and generate statistics from the Power\*Architect's source code repository.  We are also using Atlassian's Crucible (http://www.atlassian.com/software/crucible/) tool for peer code review with FishEye. The FishEye server is available at http://trillian.sqlpower.ca/fisheye


# The Wishlist #

For developers new to Power\*Architect that want to start making contributions to Power\*Architect there is a wishlist included with the source code that the developers
have put together.






