## Motivation ##

  1. Provide a way of using every native type in all target databases without requiring a commitment to a single target platform up front
  1. Provide a good implementation of the notion of domains that people expect of a proper data modeling tool

## What are Domains and User-Definable Types? ##

Domains:
  * A logical concept that captures a semantic idea, not only a physical representation (postal code vs. variable length character field)
  * Consists of:
    * Authority tag: "Corporate Standard", "Sam", "Architect Build-in"
    * Domain name
    * Data type (a platform independent user-definable type; explained below)
    * Length (precision and scale)
    * Default value
    * Check Constraint or enumeration of allowed values
      * If enumerated, this can automatically be carried through to the physical representation as a proper enum or a check constraint (whatever is "normal" for each target platform)
    * Documentation/description of purpose

Types:
  * Just a physical representation, but keyed by database platform. /This is a key feature of SQL Power's Power\*Architect that no other tool provides./
  * Power\*Architect comes with a set of starter types that should be useful for most situations (i.e. anything you could achieve with previous versions of Power\*Architect)
    * Plus, you can define new ones and say which actual physical type to use in each target platform that you care about.
    * You can always come back later and start to care about additional platforms in the future
    * Documentation/description
  * Consists of these properties:
    * Platform independent:
      * Type name (this one item identifies the type and is not platform-dependant)
      * Basic type ("text," "number," "date/time," "boolean, " and "other.")
    * Platform dependent:
      * Physical data type
        * We will default this based on standard platform-dependent representations if Basic Type is not "Other." This will save time in the majority of cases.
      * Precision (can be specified with a value, set "not applicable," or left as a parameter)
      * Scale (can be specified with a value, set "not applicable," or left as a parameter)
      * Default value
        * inherited from domain or overridden by column definition if not specified here
      * Check Constraint or enumeration of allowed values
        * inherited from domain or overridden by column definition if not specified here

## How do you use them? ##

  * In Column Properties dialog, you can choose to use a type or a domain (ideally, we'd always use domains, but in practice, it's too tedious to define enough domains to cover every single attribute in the model)
    * if you choose a domain, all the particulars of the type are locked down; you can't vary them from what the domain and its type specify.
    * if you choose a data type, you can edit all the particulars as usual
  * There will be a project preference that lets you choose how to display column type information in the playpen:
    * Domain name
    * Type name + length (precision, scale)
    * Basic type ("text," "number," "date/time," "boolean, " and "other.")

## Implementation Considerations ##

  1. Don't compromise Architect's unique feature of platform-independent modeling
    * But make modeling for a homogeneous environment easy and straightforward too
  1. Must preserve forward compatibility for existing .architect project files
  1. Think about how this new feature might interact with, enhance, or be enhanced by the Architect's existing feature set:
    * Forward Engineering
      * Covered above
    * Reverse Engineering
      * Capture the source system data types as user-definable types
    * Profiling
      * No change
    * Compare DM
      * Could choose to compare at the level of physical types or user-definable types
    * HTML data model report
      * Add domain name and user-definable type information
      * Optionally include actual physical types for a particular (selection of) target platform(s)
      * Add a section that outlines the definition of each domain? Yes, a subsection of the data model
    * OLAP modeling
      * No change

## Specific Features We Will Implement ##

  * Description/documentation of purpose and correct usage
  * TODO: Need to consider how reverse engineering will work
  * Include an example library of semantic types that people could start with

## Not in Scope ##

  * Display preferences (number/date format, alignment, display width)
  * Preferred aggregation functions
  * Ability to define a hierarchy of domain types (such as a Monetary base class which would then have CAD, USD, and so on)
  * Ability to "extract" the domains from a project file so that they can be used in another project (this feature will only be available when connected to SQL Power's Power\*Architect Enterprise Server)