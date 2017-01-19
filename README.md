# jive
## Synopsis
Web hosted tool to convert json files via a csv mapping file into a HIVE SQL script.
The generated script will create HIVE external tables over the json and populate
relational target tables from them.  Current version aimed to be Ambari view.
Coded to be deployable as servlet on for example Jetty.
## Setting Up IDE
Assuming IntelliJ being used.
Require JDK 8 installed.
1. Clone project.
2. In IntelliJ Select menu File|New|Project From Existing Sources
3. Select 'jive' folder + press OK
4. Select 'Import project from external model'
5. Select Maven + press Next
6. Check 'Search for projects recursively'
7. Check 'Import Maven projects automatically'
8. Check 'Create module groups for multi....'
9. Press Next until JDK page.
10. Select path to Java 8 JDK + press Next
11. Press Finish.  Select new window or not.
12. Select add vcs root in popup.
13. Run mvn clean install.
14. Run web project plugin jetty:run to test (Maven Projects view in IntelliJ).
## Updating version
In IntelliJ ide, Maven Projects view, root project, run Plugins|versions:set.
Enter new version.  Run mvn clean install.  DO the clean or view.xml used by
Ambari will net get new version (and will not overwrite old version if
deployed as Ambari view)!
## Ambari Deployment
1. From jive/web/target copy war file (not original version) to Ambari server
2. copy file to somewhere accessible to ambari user.
3. As Ambari user copy file to var/lib/ambari-service/resources/views
4. Restart Ambari 'ambart-service restart'
5. Log in as an admin user.  Create view (from new version).  Add users/groups.