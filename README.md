Colladoc
========

**Web application allowing to edit Scala symbols documentation.**

This application is based on existing Scaladoc 2 sources converted into full
featured web application using the Lift web framework. The application
internally uses the documentation model constructed using Scala compiler
frontend in the same way as Scaladoc 2 does.

The interface of this web application is extended in order to allow wiki-like
editing of Scala symbol comments. Application also allows to show different
versions of documentation related to single symbol for each users together
with displaying history of all changes in the form of aggregated timeline.

The Colladoc web application also provides REST interface which provide access
to the collected comments. This allows to export the changes and merge them
into original source code.
