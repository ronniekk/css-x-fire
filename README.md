CSS-X-Fire
==========

Sync your Firebug CSS changes back into IntelliJ IDE.

Installation / Usage
--------------------

All usage documentation is still hosted at [Google](http://code.google.com/p/css-x-fire/).

Developing
----------

CSS-X-Fire is both a plugin for the IDE and a Firebug extension (XPI). The entire package - this project - can be built, run, and debugged with IntelliJ IDEA 11 or newer.

### Setting up the development environment

Download and install [IntelliJ IDEA 11 or newer](http://www.jetbrains.com/idea/download/index.html). The community version is free of charge and works just fine.

In IntelliJ IDEA you will need to set up a Plugin SDK named *IDEA WS-111.19*. This will include the binaries from WebStorm 3 which CSS-X-Fire is linked against.

* Download WebStorm version 3.x as either a tarball or zip file from [Previous PhpStorm & WebStorm releases](http://confluence.jetbrains.net/pages/viewpage.action?pageId=41487696).
* Unpack the downloaded archive to a directory of your choice.
* In IntelliJ IDEA, open the "Project Structure" interface (CTRL+ALT+SHIFT+S on windows) and choose *Project* in the leftmost menu.
* Click the *New* button in the Project SDK section and choose *IntelliJ IDEA Plugin SDK*.
* A file chooser will pop up. Browse and select the directory where you unpacked the WebStorm archive.
* Click *Ok* in the next dialog. Note: I have not tested with anything else than JDK 1.6.
* Now the Project SDK dropdown will have a new entry called something like "WebStorm/PhpStorm WS-111.19". Click the *Edit* button.
* Change the name to *IDEA WS-111.19* in the top right textfield and click *OK*.

Now everything should be set up for developing. Checkout/clone this project and open it in IntelliJ IDEA.

### Debugging

When the development environment is set up correctly running and debugging is a breeze.

* Create a new *Run Configuration* of type *Plugin*.
* Set a name to something like WebStorm (optional).
* Add this to *VM Options*: `-Didea.platform.prefix=WebStorm`
* Click *OK*

That's it. Press the *Play* or *Debug* button in order to start a new instance of WebStorm with CSS-X-Fire. Happy hacking!




