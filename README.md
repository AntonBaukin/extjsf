## About ExtJSF project
Good day, fellows! For several years I've worked on B2B project having complex Web UI. The project is written on Java. During the initial research I've checked the most of known JSF 2.0 libraries and came to conclusion that all of them looked ugly. Then I've found that Sencha's Ext JS framework (version 3 at that moment) suits all my needs including excellent data grids. But the problem was that it's pure JavaScript framework, and I wanted JSF one. 

I've found a solution how to combine JSF 2.0 with Ext JS in a handy way. I use it for three years now, it works pretty well. 

The main concept here that you don't need tons of JSF components (tags) that correspond to each of widely applied ExtJS items. Just **21** tags will cover all your needs! Having the main concept adopted, more components may easily be created.

At the present moment I have everything required except the demo application. I need to extract existing implementation from the project and slightly refactor the JavaScript classes. My initial commit contains the original files as-is. I use 'temporary' branch to store and reorganize them.

## The project contents

`extjsf.js` script is the core of the library. It contains the classes to bind JSF components with Ext JS ones. It's based on `zetobj.js` supporting script. Optional `extux.js` script with `extux.css` do overwrite some parts of Ext JS 5.1 library — they are used in the demo.

The XHTML components themself. They are:
+ action-call.xhtml
+ checkbox-field.xhtml
+ checkboxes.xhtml
+ component.xhtml
+ data-grid.xhtml
+ data-store.xhtml
+ date-field.xhtml
+ drop-list.xhtml
+ form-panel.xhtml
+ hidden-field.xhtml
+ html.xhtml
+ menu.xhtml
+ root-component.xhtml
+ root-panel.xhtml
+ text-field.xhtml
+ time-field.xhtml
+ toolbar.xhtml
+ viewport.xhtml
+ winaction-button.xhtml
+ winaction-delegate.xhtml
+ winmain.xhtml

Server-side Java classes do include basic abstracts for the views (JSF back beans) and the models and some utilities. They are crucial for ExtJSF as they allow creating multi-view single page applications (i.e., portals) with rich content. Demo application also contains Eclipse MOXy JAXB library and Spring Framework. Persistence layer is omitted, all test data are generated.

Demo application is packaged in simple WAR file to run it in any Servlet container. 
