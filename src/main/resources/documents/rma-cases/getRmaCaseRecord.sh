curl -v -u "$FD_API_KEY":X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URI/custom_objects/schemas/10733585/records/_3-14" |jq > rmaCaseRecord_3-14.json