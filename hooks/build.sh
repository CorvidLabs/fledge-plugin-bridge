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
PLUGIN_DIR="\$(cd "\$(dirname "\$0")/.." && pwd)"
JAR="\$PLUGIN_DIR/build/libs/fledge-plugin-bridge-${VERSION}.jar"
exec java -jar "\$JAR" "\$@"
WRAPPER

chmod +x bin/fledge-bridge
echo "Installed bin/fledge-bridge"
