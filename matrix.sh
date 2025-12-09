#!/usr/bin/env bash

set -e

echo "======================="
echo "  REST ASSURED MATRIX  "
echo "======================="

echo
echo "[1] POSITIVE CASE: Spring Boot 2.7 + RA HEAD"
echo "--------------------------------------------"
cd positive
mvn -q -e -DskipTests=false clean test | tee ../positive.log || true
cd ..

echo
echo "[2] NEGATIVE CASE: Spring Boot 4.0.0-SNAPSHOT + RA HEAD"
echo "--------------------------------------------------------"
cd negative
mvn -q -e -DskipTests=false clean test | tee ../negative.log || true
cd ..

echo
echo "======================="
echo "        SUMMARY        "
echo "======================="

if grep -R "ERROR" positive/target/surefire-reports/*.txt >/dev/null 2>&1; then
    echo "❌ POSITIVE CASE: FAIL"
else
    echo "✔ POSITIVE CASE: PASS"
fi

if grep -R "ERROR" negative/target/surefire-reports/*.txt >/dev/null 2>&1; then
    echo "✔ NEGATIVE CASE: FAIL (EXPECTED)"
else
    echo "❌ NEGATIVE CASE: NO FAILURE DETECTED (UNEXPECTED)"
fi

echo
echo "Matrix complete."
