curl -v -u "$FD_API_KEY":X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URI/custom_objects/schemas/10563011/records/_2-1" |jq > getTacCaseRecordByRecordId_2-1.json