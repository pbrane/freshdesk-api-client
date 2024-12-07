#echo "\n\tAPI_KEY: $FD_API_KEY"
#echo "\n\n"
curl -v -u $FD_API_KEY:X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URI/custom_objects/schemas/10159914" |jq #> rma-cases.json
