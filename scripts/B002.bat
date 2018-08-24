@ECHO OFF
CD /D %~DP0
CALL CLASSPATH.bat

if exist \\HYITE-REC\public\recorded\ts\*.m2ts (
	java jp.terasoluna.fw.batch.executor.SyncBatchExecutor B002
)

ECHO %ERRORLEVEL%
