#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

# Extract version from build.gradle.kts so the wrapper JAR path stays
# correct across version bumps.
VERSION="$(grep -E '^version\s*=' build.gradle.kts | head -1 | sed 's/.*"\(.*\)".*/\1/')"
if [ -z "$VERSION" ]; then
    echo "ERROR: could not extract version from build.gradle.kts" >&2
    exit 1
fi

echo "Building fledge-plugin-bridge v${VERSION}..."
./gradlew jar --quiet

mkdir -p bin

cat > bin/fledge-bridge <<WRAPPER
#!/usr/bin/env bash
set -euo pipefail

# Resolve symlinks so the jar path is correct even when invoked via
# a symlink (e.g. plugins/bin/fledge-bridge -> plugins/fledge-plugin-bridge/bin/fledge-bridge).
SOURCE="\$0"
while [ -L "\$SOURCE" ]; do
  DIR="\$(cd "\$(dirname "\$SOURCE")" && pwd)"
  SOURCE="\$(readlink "\$SOURCE")"
  [[ "\$SOURCE" != /* ]] && SOURCE="\$DIR/\$SOURCE"
done
PLUGIN_DIR="\$(cd "\$(dirname "\$SOURCE")/.." && pwd)"

JAR="\$PLUGIN_DIR/build/libs/fledge-plugin-bridge-${VERSION}.jar"
exec java --enable-native-access=ALL-UNNAMED -jar "\$JAR" "\$@" 2>>"\${PLUGIN_DIR}/.bridge.log"
WRAPPER

chmod +x bin/fledge-bridge
echo "Installed bin/fledge-bridge"

# Windows wrapper (.bat) so `fledge bridge` works on Windows without
# requiring bash. The fledge CLI picks whichever binary matches the OS.
cat > bin/fledge-bridge.bat <<BATEOF
@echo off
setlocal

set "PLUGIN_DIR=%~dp0.."
set "JAR=%PLUGIN_DIR%\\build\\libs\\fledge-plugin-bridge-${VERSION}.jar"
java --enable-native-access=ALL-UNNAMED -jar "%JAR%" %* 2>>"%PLUGIN_DIR%\\.bridge.log"
BATEOF
echo "Installed bin/fledge-bridge.bat"
