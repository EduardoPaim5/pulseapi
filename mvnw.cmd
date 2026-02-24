@echo off
where mvn >nul 2>nul
if %ERRORLEVEL%==0 (
  mvn %*
  exit /b %ERRORLEVEL%
)
echo Maven not found. Please install Maven or provide a Maven wrapper.
exit /b 1
