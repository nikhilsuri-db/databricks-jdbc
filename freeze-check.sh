#!/bin/bash
set -e

CONFIG_FILE="development/.release-config.json"

if [[ ! -f "$CONFIG_FILE" ]]; then
  echo "✅ No release config file - passing check."
  exit 0
fi

FREEZE=$(jq '.freeze' "$CONFIG_FILE")

if [[ "$FREEZE" != "true" ]]; then
  echo "✅ Freeze is off - passing check."
  exit 0
fi

ALLOW_LIST=( $(jq -r '.allow_list[]' "$CONFIG_FILE") )

BRANCH="$PR_BRANCH"
ALLOWED=0

for ENTRY in $(jq -r '.allow_list[]' "$CONFIG_FILE"); do
  if [[ "$BRANCH" == "$ENTRY" ]]; then
    ALLOWED=1
    break
  fi
done

if [[ "$ALLOWED" == "1" ]]; then
  echo "✅ Branch '$BRANCH' is in allow_list. Passing check."
  exit 0
else
  echo "❌ Release freeze is ACTIVE!"
  REASON=$(jq -r '.reason' "$CONFIG_FILE")
  echo "Reason: $REASON"
  echo "Branch '$BRANCH' is NOT permitted to merge under current freeze rules."
  exit 1
fi