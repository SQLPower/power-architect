In order for everyone to be productive and happy, and not get bogged down trying
to read a mish-mash of different coding styles, we are fairly insistent on having
all contributions follow our established standards.

In order for you not to have to guess what those standards are, we'll describe them
here for you. :)

We mainly use Sun's conventions for coding standards which are described at
http://java.sun.com/docs/codeconv/. These formatting conventions also are also very similar to the default conventions used by Eclipse's auto-format feature.

Our exceptions to Sun's rules, and some notes:

  * **2.2** We don't use gnumake, so ignore the GNUmakefile thing

  * **3.1.1** The non-javadoc introductory comment should now be the GPL version 3 license notice for all code that is part of an open SQL Power project.

  * **3.1.3** (4) Always put the log4j Logger declaration first, followed by a blank line. No need to comment it.

  * **4.** Our tab size is currently 4, but we should all set our editors to use spaces instead of tabs.  4 is not a standard tab size (8 is usual), and tab stops of 4 will frustrate outside people trying to work with (or simply read over) our code.

  * **4.1** 80 characters is a bit of an old-fashioned limitation, but excessively long lines are difficult to read.  Use your judgment here.

  * **6.2** Sun's guide recommends declaring variables only at the beginning of a block.  I disagree: Code is more manageable and maintainable when variables are declared as late as possible within a block.

  * **6.3** I agree that you should initialize a variable when it's declared whenever possible.  However, when there is some logic associated with how to initialize a value, it is best to declare it and leave it uninitialized at first, then in each branch that follows, assign a value to the variable.  This way, the compiler will ensure that every possible path through the code ends up initializing the variable. Example:
```
int foo;
if (bar < 10) {
    foo = 12;
} else if (bar == 20) {
    foo = 9;
} else {
    foo = bar * 3;
}
System.out.println(foo);    // This line will not compile unless foo has 
                            // definitely been initialized (in this example, it has) 
```

  * **7.4** The last part shows an example of unacceptable code:
```
if (condition)
    statement;
```
> > We also consider this unacceptable.  However, we have a similar construction which is acceptable: The "guard" idiom.
```
if (condition) return;
```
> > or
```
if (condition) break;
```
> > or
```
if (condition) continue;
```
> > This is only acceptable (in fact, it is the preferred form for expressing the guard concept) if the `return`, `break`, or `continue` statement occurs on the same line as the `if`.  When the condition is long enough to warrant a line break before the `return`, `break`, or `continue` statement, you must use braces:

```
// WRONG--conditionally executed code is on next line but not within braces
if (condition || condition2 || condition3)
    return;

// CORRECT--Always use braces when the conditionally-executed code is not 
// on the same line as the if
if (condition || condition2 || condition3) {
    return;
}
```

  * **7.9** Our preferred variable name for an exception in a catch block is "e" or "ex", not "e1" as Eclipse likes to auto-generate.


> Our preferred name for an event object in an event handler is also "e". Therefore, you will often see catch blocks naming their exceptions "ex" in Swing-related code to avoid confusion and naming conflicts.

  * **9.** (Interface naming) Additionally, avoid the temptation to use the word "Interface" in an interface name, and the similar temptation to begin an interface name with an I.  Interface names should be given the cleanest, most natural names possible.  For instance, if you're creating an interface and implementation for a beverage frobbing aparatus, you should name the interface `BeverageFrobber`, and the implementation class `BeverageFrobberImpl`. If there will be several implementations that would benefit from extending an abstract base implementation, you would still create a `BeverageFrobber` interface, then create an abstract class `AbstractBeverageFrobber`, and extend it to the concrete classes named for their particular purpose (`BlendingBeverageFrobber`, `SippingBeverageFrobber`, `PouringBeverageFrobber`, and so on).
