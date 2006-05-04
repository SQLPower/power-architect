REM This is a temporary way to launch the architect.  We will migrate to WebStart ASAP!
set CLASSPATH=.;architect.jar;commons-beanutils-bean-collections.jar;commons-beanutils-core.jar;commons-beanutils.jar;commons-collections-3.1.jar;commons-dbcp-1.2.1.jar;commons-digester.jar;commons-logging.jar;commons-pool-1.3.jar;forms-1.0.6.jar;jakarta-regexp-1.2.jar;jfcunit.jar;jlfgr-1_0.jar;junit.jar;log4j.jar;sqlpower.jar
javaw -Dlog4j.configuration=file:log4j.properties -jar architect.jar
