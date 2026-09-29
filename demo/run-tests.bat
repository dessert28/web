#!/bin/bash
set -e
echo "=== Running Maven Tests for Demo Application ==="
mvn test -q
echo "=== Tests completed successfully ==="