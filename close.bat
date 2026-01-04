@echo off
echo Minecraft FluxCraft - Stopping Environment
echo =============================================
echo.
echo Stopping Docker services...
docker compose down
echo.
echo Environment stopped successfully!
echo.
pause
