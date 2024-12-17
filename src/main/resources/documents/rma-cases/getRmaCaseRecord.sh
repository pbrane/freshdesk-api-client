curl -v -u "$FD_API_KEY":X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URI/custom_objects/schemas/10733585/records/_3-2" |jq > rmaCaseRecord_3-2.json