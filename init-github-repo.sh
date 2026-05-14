#!/bin/bash
# GitHub Repository Initialization Script
# Run this after installing Git to push the CPCL printer library to GitHub

set -e

echo "=== CPCL Printer Android - GitHub Setup ==="
echo ""

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo "Error: Git is not installed. Please install Git first."
    echo "Download: https://git-scm.com/download"
    exit 1
fi

echo "Git version: $(git --version)"
echo ""

# Get GitHub credentials
echo "Enter your GitHub username:"
read GITHUB_USERNAME

echo "Enter your repository name (e.g., cpcl-printer-android):"
read REPO_NAME

echo "Enter your GitHub email:"
read GITHUB_EMAIL

# Configure git
git config user.name "$GITHUB_USERNAME"
git config user.email "$GITHUB_EMAIL"

echo ""
echo "=== Initializing Git Repository ==="

# Initialize git repo if not already
git init

# Create .gitignore if not exists
cat > .gitignore << 'EOF'
# Android
*.apk
*.ap_
*.dex
*.class
bin/
gen/
out/
build/
.gradle/
local.properties
proguard/
*.log
.navigation/
captures/
*.iml
.idea/
*.jks
*.keystore

# OS
.DS_Store
Thumbs.db

# Temporary files
*.tmp
*.temp
*.swp
*~
EOF

# Add all files
git add .

# Commit
git commit -m "Initial commit: Add CPCL label printer support

- Add CpclPrinter high-level API
- Add CpclPrinterCommands low-level API
- Support for text, barcodes, QR codes, graphics
- Compatible with Zebra, TSC, Honeywell printers
- Add usage examples and documentation"

echo ""
echo "=== Creating GitHub Repository ==="
echo "Please create a new repository on GitHub:"
echo "https://github.com/new"
echo ""
echo "Repository name: $REPO_NAME"
echo "Description: Android CPCL Label Printer Library - Fork of ESCPOS-ThermalPrinter-Android with CPCL support"
echo ""
echo "After creating the repository, press Enter to continue..."
read

# Add remote and push
echo "Adding remote origin..."
git remote add origin "https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"

echo "Pushing to GitHub..."
git branch -M main
git push -u origin main

echo ""
echo "=== Success! ==="
echo "Repository pushed to: https://github.com/$GITHUB_USERNAME/$REPO_NAME"
echo ""
echo "Next steps:"
echo "1. Visit your GitHub repository"
echo "2. Update the README with your project details"
echo "3. Create a release when ready"
echo "4. Submit issues or pull requests to improve the library"
