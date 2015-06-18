# Introduction #

The Architect has gone 5 years without implementing the standard cut, copy, and paste clipboard operations. This page details how we plan to fix this problem.

# Copy and Cut #

By using Swing, some things already support clipboard operations:
  * Text fields

We should also add support for copy (but not cut) on the following items:
  * Anything you can currently drag into the playpen:
    * Source databases
    * Catalogs in source databases
    * Schemas in source databases
    * Tables in source databases
    * Columns in source databases
  * TBD: should it be possible to copy an index, index column, relationship, or relationship mapping?

We should add support for cut and copy on the following:
  * Anything in the playpen that can be selected
    * Tables
    * Columns
    * Relationships?
      * Cut: just delete
      * Copy: sure, why not?
      * Paste into playpen after a copy: Not reasonable
      * Paste into playpen after a cut: Like undoing the delete
      * Paste as text: could be relationship name, or the names of the two tables involved
  * What about the OLAP playpen?
    * We should be able to apply all the same ideas to it as the relational playpen. The more that comes "for free" because of the way we've implemented things, the better.


# Paste #

The meaning of the paste operation has to be decided when the paste happens, based on context. In Swing DnD nomenclature, we talk about "Data Flavors" accepted by the target of the paste operation, which could be inside another Java application or a native application on the system that's hosting the JRE. Let's enumerate the possible scenarios:

## Pasting into an Architect Playpen ##

This is the most common anticipated use case. In cases where the copy and paste mirrors a drag and drop operation in terms of source and target objects, it should do the same thing as the drag and drop would have done. For example: select a source schema, copy it, select playpen, then paste. This should be functionally identical to dragging the same source schema into the playpen. One question would be where to locate the components in the playpen (since the drop operation provides a location but the paste does not).

For operations that have no drag and drop counterpart, such as pasting a text selection into the playpen, we may be able to do some useful things with a (potentially multi-line) text selection:

  * Create a table per line of text when the paste target is playpen
  * Create a column per line of text when the paste target is a table (we have the "modeling something that was in Excel" use case in mind here, so this should be tested to ensure it works)

## Pasting as Text ##

This will be the usual case when pasting into a native app.

Possible representations of, for example, a SQLTable include:

  * The table's name
  * The table's name followed by a bulleted list of column names
  * DDL that creates the table (which dialect?)
  * An XML snippet in .architect project file format

I think the most sensible is the first option. You can already get the text of a DDL statement into the system clipboard via the DDL Generator, which also lets you specify the dialect and target schema name. And pasting XML is not all that appealing of an idea.

The same ideas can be applied to the other object types.

## Pasting into another SQL Power Swing app ##

### MatchMaker ###
### Wabit ###


# Supporting changes to Drag and Drop #

We don't currently support the Drag and Drop COPY operation for playpen-to-playpen column drags. They're always done as MOVE. Swing already supports the ability to change between MOVE and COPY by holding down a modifier key during the drag operation. We should pay attention to and respect these flags now that we're implementing copy and paste in the playpen.