@echo off
chcp 65001 >nul
echo === CPCL Printer Android - GitHub Setup ===
echo.

REM Check if git is installed
git --version >nul 2>&1
if errorlevel 1 (
    echo Error: Git is not installed. Please install Git first.
    echo Download: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo Git version:
git --version
echo.

REM Get GitHub credentials
set /p GITHUB_USERNAME="Enter your GitHub username: "
set /p REPO_NAME="Enter your repository name (e.g., cpcl-printer-android): "
set /p GITHUB_EMAIL="Enter your GitHub email: "

REM Configure git
git config user.name "%GITHUB_USERNAME%"
git config user.email "%GITHUB_EMAIL%"

echo.
echo === Initializing Git Repository ===

REM Initialize git repo if not already
git init

REM Create .gitignore if not exists
if not exist .gitignore (
    echo # Android > .gitignore
    echo *.apk >> .gitignore
    echo *.ap_ >> .gitignore
    echo *.dex >> .gitignore
    echo *.class >> .gitignore
    echo bin/ >> .gitignore
    echo gen/ >> .gitignore
    echo out/ >> .gitignore
    echo build/ >> .gitignore
    echo .gradle/ >> .gitignore
    echo local.properties >> .gitignore
    echo proguard/ >> .gitignore
    echo *.log >> .gitignore
    echo .navigation/ >> .gitignore
    echo captures/ >> .gitignore
    echo *.iml >> .gitignore
    echo .idea/ >> .gitignore
    echo *.jks >> .gitignore
    echo *.keystore >> .gitignore
    echo. >> .gitignore
    echo # OS >> .gitignore
    echo .DS_Store >> .gitignore
    echo Thumbs.db >> .gitignore
    echo. >> .gitignore
    echo # Temporary files >> .gitignore
    echo *.tmp >> .gitignore
    echo *.temp >> .gitignore
    echo *.swp >> .gitignore
    echo *~ >> .gitignore
)

REM Add all files
git add .

REM Commit
git commit -m "Initial commit: Add CPCL label printer support

- Add CpclPrinter high-level API
- Add CpclPrinterCommands low-level API
- Support for text, barcodes, QR codes, graphics
- Compatible with Zebra, TSC, Honeywell printers
- Add usage examples and documentation"

echo.
echo === Creating GitHub Repository ===
echo Please create a new repository on GitHub:
echo https://github.com/new
echo.
echo Repository name: %REPO_NAME%
echo Description: Android CPCL Label Printer Library - Fork of ESCPOS-ThermalPrinter-Android with CPCL support
echo.
pause

REM Add remote and push
echo Adding remote origin...
git remote add origin https://github.com/%GITHUB_USERNAME%/%REPO_NAME%.git

echo Pushing to GitHub...
git branch -M main
git push -u origin main

echo.
echo === Success! ===
echo Repository pushed to: https://github.com/%GITHUB_USERNAME%/%REPO_NAME%
echo.
echo Next steps:
echo 1. Visit your GitHub repository
echo 2. Update the README with your project details
echo 3. Create a release when ready
echo 4. Submit issues or pull requests to improve the library
pause
