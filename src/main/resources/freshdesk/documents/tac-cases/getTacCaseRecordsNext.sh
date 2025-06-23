BEGIN=2025-01-01T00:00:00.000Z
END=2025-04-01T00:00:00.000Z
curl -v -u $FD_API_KEY:X \
  -H "Content-Type: application/json" \
  -X GET "$FD_BASE_URI/custom_objects/schemas/14051436/records?page_size=2&next_token=Lbr2zerj3WHNDsZ6FXG7Ik05mrsedJDLVxSGjAjDowX" \
  |jq #> getTacCaseRecordsQuery.json

#custom_objects/schemas/14051436/records?page_size=2
#custom_objects/schemas/14051436/records?page_size=2&next_token=Lbr2zerj3WHNDsZ6FXG7Ik05mrsedJDLVxSGjAjDowX