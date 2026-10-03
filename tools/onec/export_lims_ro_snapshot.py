#!/usr/bin/env python3
"""Export the isolated lims_ro 1C mart into a sanitized LIMS snapshot.

The script deliberately excludes patient-level and diagnostic fields. It is
intended to run inside the isolated 1C Docker network, never in the LIMS
backend container.
"""

import argparse
import hashlib
import json
import os
from datetime import date, datetime
from decimal import Decimal
from pathlib import Path

import psycopg2


def json_value(value):
    if isinstance(value, (datetime, date)):
        return value.isoformat()
    if isinstance(value, Decimal):
        return str(value)
    raise TypeError(f"Unsupported JSON value: {type(value).__name__}")


def rows(cursor, query):
    cursor.execute(query)
    columns = [column.name for column in cursor.description]
    return [dict(zip(columns, row)) for row in cursor.fetchall()]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="postgres")
    parser.add_argument("--port", default=5432, type=int)
    parser.add_argument("--db", required=True)
    parser.add_argument("--user", required=True)
    parser.add_argument("--password", required=True)
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    conn = psycopg2.connect(
        host=args.host,
        port=args.port,
        dbname=args.db,
        user=args.user,
        password=args.password,
    )
    cur = conn.cursor()
    profile = rows(cur, "SELECT * FROM lims_ro.v_source_profile")[0]

    snapshot = {
        "status": "ready",
        "snapshotAt": profile["latest_medication_movement_at"],
        "periodFrom": rows(cur, "SELECT MIN(month) AS value FROM lims_ro.v_medication_movement_monthly")[0]["value"],
        "periodTo": rows(cur, "SELECT MAX(month) AS value FROM lims_ro.v_medication_movement_monthly")[0]["value"],
        "source": {
            "sourceName": "1C isolated read-only mart",
            "sourceKind": "ONEC_READONLY_MART",
            "sourceChecksum": profile["source_dt_sha256"],
            "exportedAt": datetime.utcnow(),
            "dataActualityAt": profile["latest_medication_movement_at"],
            "notes": "Patient-level rows are excluded. Movement record kinds remain unclassified until accounting validation.",
        },
        "nomenclature": rows(
            cur,
            """
            SELECT item_code AS code, item_name AS name, NULL::text AS unit,
                   article, is_medication, is_medical_device,
                   medicine_registration_number
            FROM lims_ro.v_nomenclature
            WHERE NOT is_deleted
            ORDER BY item_name
            """,
        ),
        "inventory": rows(
            cur,
            """
            SELECT medication_code AS "nomenclatureCode",
                   medication_name AS "nomenclatureName",
                   warehouse_name AS warehouse,
                   quantity_on_hand AS quantity,
                   NULL::text AS unit,
                   inventory_value AS cost,
                   lot_number AS "lotNumber",
                   expiry_date AS "expiryDate",
                   source_actuality_at AS "sourceActualityAt"
            FROM lims_ro.v_medication_stock_snapshot
            ORDER BY warehouse_name, medication_name, lot_number
            """,
        ),
        # Receipt/write-off direction has not been accountant-validated in the
        # restored source, so leaving these empty is safer than inventing facts.
        "receipts": [],
        "writeOffs": [],
        "counterparties": rows(
            cur,
            """
            SELECT _fld10142::text AS inn,
                   _description::text AS name,
                   'counterparty'::text AS type,
                   _code::text AS code
            FROM public._reference141
            WHERE NOT _marked
            ORDER BY _description
            """,
        ),
    }
    cur.close()
    conn.close()

    output = Path(args.out)
    output.parent.mkdir(parents=True, exist_ok=True)
    serialized = json.dumps(snapshot, ensure_ascii=False, default=json_value, separators=(",", ":"))
    output.write_text(serialized, encoding="utf-8")
    print(json.dumps({
        "output": str(output),
        "sha256": hashlib.sha256(serialized.encode("utf-8")).hexdigest(),
        "nomenclature": len(snapshot["nomenclature"]),
        "inventory": len(snapshot["inventory"]),
        "counterparties": len(snapshot["counterparties"]),
    }, ensure_ascii=False))


if __name__ == "__main__":
    main()
