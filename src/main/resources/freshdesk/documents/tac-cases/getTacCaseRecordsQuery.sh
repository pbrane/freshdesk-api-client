BEGIN=2025-01-01T00:00:00.00Z
END=2025-04-01T00:00:00.00Z
curl -v -u $FD_API_KEY:X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URI/custom_objects/schemas/14051436/records?created_time%5Bgte%5D=$BEGIN&created_time%5Blt%5D=$END&page_size=2" \
  |jq #> getTacCaseRecordsQuery.json

