curl -v -u $FD_API_KEY:X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URL/api/v2/custom_objects/schemas/10498799" |jq > tac-cases.json
