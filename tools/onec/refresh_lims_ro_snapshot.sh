#!/usr/bin/env bash
# Export a sanitized snapshot from the isolated 1C mart and ingest it into LIMS.
# This script is intended for the 1C/LIMS host only. It never accepts a .dt file
# and never exports patient-level rows.
set -euo pipefail
umask 077

ONEC_ROOT="${ONEC_ROOT:-/opt/1c-kz-dev}"
LABA_ROOT="${LABA_ROOT:-/opt/laba}"
AUDIT_ROOT="${ONEC_ROOT}/run/dt-audit"
SNAPSHOT_PATH="${AUDIT_ROOT}/snapshot/onec-readonly-snapshot.json"
EXPORT_SCRIPT="${AUDIT_ROOT}/export_lims_ro_snapshot.py"
BACKEND_ENV="${LABA_ROOT}/backend/.env.backend"
LOCK_FILE="${AUDIT_ROOT}/refresh_lims_snapshot.lock"

mkdir -p "${AUDIT_ROOT}/snapshot"
exec 9>"${LOCK_FILE}"
flock -n 9 || {
  echo "A snapshot refresh is already running." >&2
  exit 0
}

[[ -r "${BACKEND_ENV}" ]] || { echo "Missing ${BACKEND_ENV}" >&2; exit 1; }
[[ -r "${EXPORT_SCRIPT}" ]] || { echo "Missing ${EXPORT_SCRIPT}" >&2; exit 1; }

pg_password="$(cat "${ONEC_ROOT}/compose/secrets/postgres_password")"

docker run --rm \
  --network onec_kz_dev_net \
  --read-only \
  --tmpfs /tmp:rw,nosuid,nodev,size=64m \
  --mount "type=bind,source=${AUDIT_ROOT},target=/audit" \
  onec-dt-map-audit:local \
  python /audit/export_lims_ro_snapshot.py \
    --host postgres \
    --port 5432 \
    --db kz_hospital_dt_20260903 \
    --user onec_dev \
    --password "${pg_password}" \
    --out /audit/snapshot/onec-readonly-snapshot.json >/dev/null

set -a
# shellcheck disable=SC1090
source "${BACKEND_ENV}"
set +a

auth="$(curl --fail --silent --show-error --request POST \
  --user "${DEV_AUTH_BOOTSTRAP_ADMIN_EMAIL}:${DEV_AUTH_BOOTSTRAP_ADMIN_PASSWORD}" \
  http://127.0.0.1:8081/auth)"
token="$(printf '%s' "${auth}" | python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])')"

response="$(curl --fail --silent --show-error \
  --header "Authorization: Bearer ${token}" \
  --form "sourceName=1C isolated read-only mart" \
  --form "file=@${SNAPSHOT_PATH};type=application/json" \
  http://127.0.0.1:8081/api/onec/snapshots/read-only)"

python3 - "${response}" <<'PY'
import json
import sys

result = json.loads(sys.argv[1])
print(
    "snapshot_id={id} idempotent={idempotent} "
    "nomenclature={nomenclatureCount} inventory={inventoryCount} "
    "counterparties={counterpartyCount}".format(**result)
)
PY
