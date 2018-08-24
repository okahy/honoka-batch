@ECHO OFF
CD /D %~DP0
CALL CLASSPATH.bat

if exist \\HYITE-REC\public\recorded\json\*.json (
	java jp.terasoluna.fw.batch.executor.SyncBatchExecutor B001
)

ECHO %ERRORLEVEL%
