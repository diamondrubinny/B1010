#!/usr/bin/env python3
"""Checks every component of the release SBOM against the OSV vulnerability database.

Usage:
    ./gradlew :app:cyclonedxDirectBom && cp app/build/reports/cyclonedx-direct/bom.json docs/sbom/app-sbom.json
    python3 tools/osv_scan.py docs/sbom/app-sbom.json
Exits with code 1 if any known vulnerability is found. A known-vulnerable control package is queried
first, so a broken query can never be mistaken for a clean result.
"""
import json
import os
import ssl
import sys
import urllib.request

CONTROL = "pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.9.0"


def query(purls):
    cafile = os.environ.get("SSL_CERT_FILE") or ("/root/.ccr/ca-bundle.crt" if os.path.exists("/root/.ccr/ca-bundle.crt") else None)
    ctx = ssl.create_default_context(cafile=cafile)
    body = json.dumps({"queries": [{"package": {"purl": p}} for p in purls]}).encode()
    req = urllib.request.Request("https://api.osv.dev/v1/querybatch", data=body, headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, context=ctx, timeout=60))["results"]


def main() -> int:
    sbom = json.load(open(sys.argv[1] if len(sys.argv) > 1 else "docs/sbom/app-sbom.json"))
    purls = sorted({c["purl"].split("?")[0] for c in sbom.get("components", []) if c.get("purl")})
    if not query([CONTROL])[0].get("vulns"):
        print("Control package returned no findings: the scan is not working.", file=sys.stderr)
        return 2
    hits = [(p, v["id"]) for p, r in zip(purls, query(purls)) for v in (r.get("vulns") or [])]
    print(f"Scanned {len(purls)} components: {len(hits)} known vulnerabilities")
    for p, vid in hits:
        print(f"  {vid}  {p}")
    return 1 if hits else 0


if __name__ == "__main__":
    sys.exit(main())
