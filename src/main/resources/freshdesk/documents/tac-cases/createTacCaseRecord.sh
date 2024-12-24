curl -v -u "$FD_API_KEY":X \
  -H "Content-Type: application/json" \
  -X POST "$FD_BASE_URI/custom_objects/schemas/10563011/records" \
  -d '{ "data": { "key": "TEST:01", "subject": "Test TAC Case Create", "case_status": "Open", "case_create_date": "2024-12-03", "problem_description": "Always Freshdesk is the problem. :)", "rma_needed": true, "product_name": "OTDR", "ticket": null } }' |jq #> createTacCaseRecordResponse.json