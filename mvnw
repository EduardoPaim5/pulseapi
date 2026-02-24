#!/usr/bin/env sh

if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi

echo "Maven not found. Please install Maven or provide a Maven wrapper." >&2
exit 1
