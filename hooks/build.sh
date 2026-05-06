#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo "Building fledge-plugin-bridge..."
./gradlew jar --quiet

mkdir -p bin

cat > bin/fledge-bridge <<'WRAPPER'
#!/usr/bin/env bash
set -euo pipefail
PLUGIN_DIR="$(cd "$(dirname "$0")/.." && pwd)"
JAR="$PLUGIN_DIR/build/libs/fledge-plugin-bridge-0.1.0.jar"
exec java -jar "$JAR" "$@"
WRAPPER

chmod +x bin/fledge-bridge
echo "Installed bin/fledge-bridge"
