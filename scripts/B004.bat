@ECHO OFF
CD /D %~DP0
CALL CLASSPATH.bat

java jp.terasoluna.fw.batch.executor.SyncBatchExecutor B004

ECHO %ERRORLEVEL%
